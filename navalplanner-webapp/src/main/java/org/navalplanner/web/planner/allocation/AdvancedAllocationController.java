/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.planner.allocation;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.AssignmentFunction;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.StretchesFunction.Type;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.planner.allocation.streches.StrechesFunctionConfiguration;
import org.navalplanner.web.resourceload.ResourceLoadModel;
import org.zkoss.ganttz.timetracker.ICellForDetailItemRenderer;
import org.zkoss.ganttz.timetracker.IConvertibleToColumn;
import org.zkoss.ganttz.timetracker.PairOfLists;
import org.zkoss.ganttz.timetracker.TimeTrackedTable;
import org.zkoss.ganttz.timetracker.TimeTrackedTableWithLeftPane;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.TimeTrackerComponentWithoutColumns;
import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.api.Column;

public class AdvancedAllocationController extends GenericForwardComposer {

    public static class AllocationInput {
        private final AggregateOfResourceAllocations aggregate;

        private final IAdvanceAllocationResultReceiver resultReceiver;

        private final TaskElement task;

        public AllocationInput(AggregateOfResourceAllocations aggregate,
                TaskElement task,
                IAdvanceAllocationResultReceiver resultReceiver) {
            Validate.notNull(aggregate);
            Validate.notNull(resultReceiver);
            Validate.notNull(task);
            this.aggregate = aggregate;
            this.task = task;
            this.resultReceiver = resultReceiver;
        }

        List<ResourceAllocation<?>> getAllocationsSortedByStartDate() {
            return getAggregate().getAllocationsSortedByStartDate();
        }

        int getTotalHours() {
            return getAggregate().getTotalHours();
        }

        AggregateOfResourceAllocations getAggregate() {
            return aggregate;
        }

        String getTaskName() {
            return task.getName();
        }

        IAdvanceAllocationResultReceiver getResultReceiver() {
            return resultReceiver;
        }

        Interval calculateInterval() {
            List<ResourceAllocation<?>> all = getAllocationsSortedByStartDate();
            if (all.isEmpty()) {
                return new Interval(task.getStartDate(), task
                        .getEndDate());
            } else {
                LocalDate start = all.get(0).getStartDate();
                LocalDate end = getEnd(all);
                return new Interval(asDate(start), asDate(end));
            }
        }

        private static LocalDate getEnd(List<ResourceAllocation<?>> all) {
            ArrayList<ResourceAllocation<?>> reversed = reverse(all);
            LocalDate end = reversed.get(0).getEndDate();
            ListIterator<ResourceAllocation<?>> listIterator = reversed
                    .listIterator(1);
            while (listIterator.hasNext()) {
                ResourceAllocation<?> current = listIterator.next();
                if (current.getEndDate().compareTo(end) >= 0) {
                    end = current.getEndDate();
                } else {
                    return end;
                }
            }
            return end;
        }

        private static ArrayList<ResourceAllocation<?>> reverse(
                List<ResourceAllocation<?>> all) {
            ArrayList<ResourceAllocation<?>> reversed = new ArrayList<ResourceAllocation<?>>(
                    all);
            Collections.reverse(reversed);
            return reversed;
        }

        private static Date asDate(LocalDate start) {
            return start.toDateMidnight().toDate();
        }

    }

    public interface IAdvanceAllocationResultReceiver {
        public Restriction createRestriction();

        public void accepted(AggregateOfResourceAllocations modifiedAllocations);

        public void cancel();
    }

    public interface IBack {
        public void goBack();
    }

    public abstract static class Restriction {

        public interface IRestrictionSource {
            int getTotalHours();

            LocalDate getStart();

            LocalDate getEnd();

            CalculatedValue getCalculatedValue();

        }

        public static Restriction build(IRestrictionSource restrictionSource) {
            switch (restrictionSource.getCalculatedValue()) {
            case END_DATE:
                return Restriction
                        .fixedHours(restrictionSource
                        .getTotalHours());
            case NUMBER_OF_HOURS:
                return Restriction.onlyAssignOnInterval(restrictionSource
                        .getStart(), restrictionSource.getEnd());
            case RESOURCES_PER_DAY:
                return Restriction.emptyRestriction();
            default:
                throw new RuntimeException("unhandled case: "
                        + restrictionSource.getCalculatedValue());
            }
        }

        private static Restriction emptyRestriction() {
            return new NoRestriction();
        }

        private static Restriction onlyAssignOnInterval(LocalDate start,
                LocalDate end){
            return new OnlyOnIntervalRestriction(start, end);
        }

        private static Restriction fixedHours(int hours) {
            return new FixedHoursRestriction(hours);
        }

        abstract LocalDate limitStartDate(LocalDate startDate);

        abstract LocalDate limitEndDate(LocalDate localDate);

        abstract boolean isDisabledEditionOn(DetailItem item);

        public abstract boolean isInvalidTotalHours(int totalHours);

        public abstract void showInvalidHours(IMessagesForUser messages,
                int totalHours);

        public abstract void markInvalidTotalHours(Row groupingRow,
                int currentHours);
    }

    private static class OnlyOnIntervalRestriction extends Restriction {
        private final LocalDate start;

        private final LocalDate end;

        private OnlyOnIntervalRestriction(LocalDate start, LocalDate end) {
            super();
            this.start = start;
            this.end = end;
        }

        private org.joda.time.Interval intervalAllowed() {
            return new org.joda.time.Interval(start.toDateTimeAtStartOfDay(),
                    end.toDateTimeAtStartOfDay());
        }

        @Override
        boolean isDisabledEditionOn(DetailItem item) {
            return !intervalAllowed().overlaps(
                    new org.joda.time.Interval(item.getStartDate(), item
                            .getEndDate()));
        }

        @Override
        public boolean isInvalidTotalHours(int totalHours) {
            return false;
        }

        @Override
        LocalDate limitEndDate(LocalDate argEnd) {
            return end.compareTo(argEnd) < 0 ? end : argEnd;
        }

        @Override
        LocalDate limitStartDate(LocalDate argStart) {
            return start.compareTo(argStart) > 0 ? start : argStart;
        }

        @Override
        public void showInvalidHours(IMessagesForUser messages, int totalHours) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void markInvalidTotalHours(Row groupingRow, int currentHours) {
            throw new UnsupportedOperationException();
        }
    }

    private static class FixedHoursRestriction extends Restriction {
        private final int hours;

        private FixedHoursRestriction(int hours) {
            this.hours = hours;
        }

        @Override
        boolean isDisabledEditionOn(DetailItem item) {
            return false;
        }

        @Override
        LocalDate limitEndDate(LocalDate endDate) {
            return endDate;
        }

        @Override
        LocalDate limitStartDate(LocalDate startDate) {
            return startDate;
        }

        @Override
        public boolean isInvalidTotalHours(int totalHours) {
            return this.hours != totalHours;
        }

        @Override
        public void showInvalidHours(IMessagesForUser messages, int totalHours) {
            messages.showMessage(Level.WARNING,
                    getMessage(totalHours));
        }

        private String getMessage(int totalHours) {
            return _("there must be {0} hours instead of {1}", hours,
                    totalHours);
        }

        @Override
        public void markInvalidTotalHours(Row groupingRow, int totalHours) {
            groupingRow.markErrorOnTotal(getMessage(totalHours));
        }
    }

    private static class NoRestriction extends Restriction {

        @Override
        boolean isDisabledEditionOn(DetailItem item) {
            return false;
        }

        @Override
        public boolean isInvalidTotalHours(int totalHours) {
            return false;
        }

        @Override
        LocalDate limitEndDate(LocalDate endDate) {
            return endDate;
        }

        @Override
        LocalDate limitStartDate(LocalDate startDate) {
            return startDate;
        }

        @Override
        public void markInvalidTotalHours(Row groupingRow, int currentHours) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void showInvalidHours(IMessagesForUser messages, int totalHours) {
            throw new UnsupportedOperationException();
        }
    }

    private IMessagesForUser messages;
    private Div insertionPointTimetracker;
    private Div insertionPointLeftPanel;
    private Div insertionPointRightPanel;

    private TimeTracker timeTracker;

    private TimeTrackerComponentWithoutColumns timeTrackerComponent;
    private Grid leftPane;
    private TimeTrackedTable<Row> table;
    private IBack back;
    private List<AllocationInput> allocationInputs;
    private Component associatedComponent;

    public AdvancedAllocationController(IBack back,
            List<AllocationInput> allocationInputs) {
        setInputData(back, allocationInputs);
    }

    private void setInputData(IBack back, List<AllocationInput> allocationInputs) {
        Validate.notNull(back);
        Validate.noNullElements(allocationInputs);
        Validate.isTrue(!allocationInputs.isEmpty());
        this.back = back;
        this.allocationInputs = allocationInputs;
    }

    public void reset(IBack back, List<AllocationInput> allocationInputs) {
        rowsCached = null;
        setInputData(back, allocationInputs);
        loadAndInitializeComponents();
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.associatedComponent = comp;
        loadAndInitializeComponents();
    }

    private void loadAndInitializeComponents() {
        messages = new MessagesForUser(associatedComponent
                .getFellow("messages"));
        createComponents();
        insertComponentsInLayout();
        timeTrackerComponent.afterCompose();
        table.afterCompose();
    }

    private void createComponents() {
        timeTracker = new TimeTracker(addYearMarginTointerval(), self);
        timeTrackerComponent = new TimeTrackerComponentWithoutColumns(
                timeTracker, "timeTracker");
        TimeTrackedTableWithLeftPane<Row, Row> timeTrackedTableWithLeftPane = new TimeTrackedTableWithLeftPane<Row, Row>(
                getDataSource(), getColumnsForLeft(), getLeftRenderer(),
                getRightRenderer(), timeTracker);
        Clients.evalJavaScript("ADVANCE_ALLOCATIONS.listenToScroll();");
        timeTracker.addZoomListener(new IZoomLevelChangedListener() {
            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                Clients.evalJavaScript("ADVANCE_ALLOCATIONS.listenToScroll();");
            }
        });
        table = timeTrackedTableWithLeftPane.getRightPane();
        leftPane = timeTrackedTableWithLeftPane.getLeftPane();
        leftPane.setFixedLayout(true);
    }

    private void insertComponentsInLayout() {
        insertionPointRightPanel.getChildren().clear();
        insertionPointRightPanel.appendChild(table);
        insertionPointLeftPanel.getChildren().clear();
        insertionPointLeftPanel.appendChild(leftPane);
        insertionPointTimetracker.getChildren().clear();
        insertionPointTimetracker.appendChild(timeTrackerComponent);
    }

    public void onClick$acceptButton() {
        for (AllocationInput allocationInput : allocationInputs) {
            int totalHours = allocationInput.getTotalHours();
            Restriction restriction = allocationInput.getResultReceiver()
                    .createRestriction();
            if (restriction.isInvalidTotalHours(totalHours)) {
                Row groupingRow = groupingRows.get(allocationInput);
                restriction.markInvalidTotalHours(groupingRow, totalHours);
            }
        }
        back.goBack();
        for (AllocationInput allocationInput : allocationInputs) {
            allocationInput.getResultReceiver().accepted(allocationInput
                    .getAggregate());
        }
    }

    public void onClick$cancelButton() {
        back.goBack();
        for (AllocationInput allocationInput : allocationInputs) {
            allocationInput.getResultReceiver().cancel();
        }
    }

    public ListModel getZoomLevels() {
        return new SimpleListModel(ZoomLevel.values());
    }

    public void setZoomLevel(final ZoomLevel zoomLevel) {
        timeTracker.setZoomLevel(zoomLevel);
    }

    public void onClick$zoomIncrease() {
        timeTracker.zoomIncrease();
    }

    public void onClick$zoomDecrease() {
        timeTracker.zoomDecrease();
    }

    private List<Row> rowsCached = null;
    private Map<AllocationInput, Row> groupingRows = new HashMap<AllocationInput, Row>();

    private List<Row> getRows() {
        if (rowsCached != null) {
            return rowsCached;
        }
        rowsCached = new ArrayList<Row>();
        for (AllocationInput allocationInput : allocationInputs) {
            Row groupingRow = buildGroupingRow(allocationInput);
            groupingRows.put(allocationInput, groupingRow);
            rowsCached.add(groupingRow);
            List<Row> genericRows = genericRows(allocationInput);
            groupingRow.listenTo(genericRows);
            rowsCached.addAll(genericRows);
            List<Row> specificRows = specificRows(allocationInput);
            groupingRow.listenTo(specificRows);
            rowsCached.addAll(specificRows);
        }
        return rowsCached;
    }

    private List<Row> specificRows(AllocationInput allocationInput) {
        List<Row> result = new ArrayList<Row>();
        for (SpecificResourceAllocation specificResourceAllocation : allocationInput.getAggregate()
                .getSpecificAllocations()) {
            result.add(createSpecificRow(specificResourceAllocation,
                    allocationInput.getResultReceiver().createRestriction()));
        }
        return result;
    }

    private Row createSpecificRow(
            SpecificResourceAllocation specificResourceAllocation,
            Restriction restriction) {
        return Row.createRow(messages, restriction,
                specificResourceAllocation.getResource()
                        .getShortDescription(), 1, Arrays
                        .asList(specificResourceAllocation));
    }

    private List<Row> genericRows(AllocationInput allocationInput) {
        List<Row> result = new ArrayList<Row>();
        for (GenericResourceAllocation genericResourceAllocation : allocationInput.getAggregate()
                .getGenericAllocations()) {
            result.add(buildGenericRow(genericResourceAllocation,
                    allocationInput.getResultReceiver().createRestriction()));
        }
        return result;
    }

    private Row buildGenericRow(
            GenericResourceAllocation genericResourceAllocation,
            Restriction restriction) {
        return Row.createRow(messages, restriction,
                ResourceLoadModel
                .getName(genericResourceAllocation.getCriterions()), 1, Arrays
                .asList(genericResourceAllocation));
    }

    private Row buildGroupingRow(AllocationInput allocationInput) {
        Restriction restriction = allocationInput.getResultReceiver()
                .createRestriction();
        String taskName = allocationInput.getTaskName();
        Row groupingRow = Row.createRow(messages, restriction, taskName
                + " (task)", 0, allocationInput
                .getAllocationsSortedByStartDate());
        return groupingRow;
    }

    private ICellForDetailItemRenderer<ColumnOnRow, Row> getLeftRenderer() {
        return new ICellForDetailItemRenderer<ColumnOnRow, Row>() {

            @Override
            public Component cellFor(ColumnOnRow column, Row row) {
                return column.cellFor(row);
            }
        };
    }

    private List<ColumnOnRow> getColumnsForLeft() {
        List<ColumnOnRow> result = new ArrayList<ColumnOnRow>();
        result.add(new ColumnOnRow(_("Name")) {

            @Override
            public Component cellFor(Row row) {
                return row.getNameLabel();
            }
        });
        result.add(new ColumnOnRow(_("Hours")) {
            @Override
            public Component cellFor(Row row) {
                return row.getAllHours();
            }
        });
        result.add(new ColumnOnRow(_("Function")) {
            @Override
            public Component cellFor(Row row) {
                return row.getFunction();
            }
        });

        return result;
    }

    private Callable<PairOfLists<Row, Row>> getDataSource() {
        return new Callable<PairOfLists<Row, Row>>() {

            @Override
            public PairOfLists<Row, Row> call() throws Exception {
                List<Row> rows = getRows();
                return new PairOfLists<Row, Row>(rows, rows);
            }
        };
    }

    private ICellForDetailItemRenderer<DetailItem, Row> getRightRenderer() {
        return new ICellForDetailItemRenderer<DetailItem, Row>() {

            @Override
            public Component cellFor(DetailItem item, Row data) {
                return data.hoursOnInterval(item);
            }
        };
    }

    private Interval intervalFromData() {
        Interval result = null;
        for (AllocationInput each : allocationInputs) {
            Interval intervalForInput = each.calculateInterval();
            result = result == null ? intervalForInput : result
                    .coalesce(intervalForInput);
        }
        return result;
    }

    private Interval addYearMarginTointerval() {
        Interval interval = intervalFromData();
        return new Interval(new DateTime(interval.getStart()).minusYears(1)
                .toDate(), new DateTime(interval.getFinish()).plusYears(1)
                .toDate());
    }

}

abstract class ColumnOnRow implements IConvertibleToColumn {
    private final String columnName;

    ColumnOnRow(String columnName) {
        this.columnName = columnName;
    }

    public abstract Component cellFor(Row row);

    @Override
    public Column toColumn() {
        Column column = new org.zkoss.zul.Column();
        column.setLabel(columnName);
        column.setSclass(((String) columnName).toLowerCase());
        return column;
    }

    public String getName() {
        return columnName;
    }
}

interface CellChangedListener {
    public void changeOn(DetailItem detailItem);

    public void changeOnGlobal();
}

class Row {



    static Row createRow(IMessagesForUser messages,
            AdvancedAllocationController.Restriction restriction,
            String name, int level,
            List<? extends ResourceAllocation<?>> allocations) {
        return new Row(messages, restriction, name, level, allocations);
    }

    public void markErrorOnTotal(String message) {
        throw new WrongValueException(allHoursInput, message);
    }

    private Component allHoursInput;

    private Label nameLabel;

    private List<CellChangedListener> listeners = new ArrayList<CellChangedListener>();

    private Map<DetailItem, Component> componentsByDetailItem = new WeakHashMap<DetailItem, Component>();

    private String name;

    private int level;

    private final AggregateOfResourceAllocations aggregate;

    private final AdvancedAllocationController.Restriction restriction;

    private final IMessagesForUser messages;

    void listenTo(Collection<Row> rows) {
        for (Row row : rows) {
            listenTo(row);
        }
    }

    void listenTo(Row row) {
        row.add(new CellChangedListener() {

            @Override
            public void changeOnGlobal() {
                reloadAllHours();
                reloadHoursSameRowForDetailItems();
            }

            @Override
            public void changeOn(DetailItem detailItem) {
                Component component = componentsByDetailItem.get(detailItem);
                if (component == null) {
                    return;
                }
                reloadHoursOnInterval(component, detailItem);
                reloadAllHours();
            }
        });
    }

    void add(CellChangedListener listener) {
        listeners.add(listener);
    }

    private void fireCellChanged(DetailItem detailItem) {
        for (CellChangedListener cellChangedListener : listeners) {
            cellChangedListener.changeOn(detailItem);
        }
    }

    private void fireCellChanged() {
        for (CellChangedListener cellChangedListener : listeners) {
            cellChangedListener.changeOnGlobal();
        }
    }

    Component getAllHours() {
        if (allHoursInput == null) {
            allHoursInput = buildAllHours();
            reloadAllHours();
            addListenerIfNeeded(allHoursInput);
        }
        return allHoursInput;
    }

    private Component buildAllHours() {
        return isGroupingRow() ? new Label() : noNegativeIntbox();
    }

    private void addListenerIfNeeded(Component allHoursComponent) {
        if (isGroupingRow()) {
            return;
        }
        final Intbox intbox = (Intbox) allHoursComponent;
        intbox.addEventListener(Events.ON_CHANGE, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                Integer value = intbox.getValue();
                getAllocation().withPreviousAssociatedResources().onInterval(
                        getAllocation().getStartDate(),
                        getAllocation().getEndDate())
                        .allocateHours(value);
                fireCellChanged();
                reloadHoursSameRowForDetailItems();
                reloadAllHours();
            }
        });
    }

    private void reloadHoursSameRowForDetailItems() {
        for (Entry<DetailItem, Component> entry : componentsByDetailItem
                .entrySet()) {
            reloadHoursOnInterval(entry.getValue(), entry.getKey());
        }
    }

    private void reloadAllHours() {
        if (isGroupingRow()) {
            Label label = (Label) allHoursInput;
            int totalHours = aggregate.getTotalHours();
            label.setValue(totalHours + "");
            Clients.closeErrorBox(label);
            if (restriction.isInvalidTotalHours(totalHours)) {
                restriction.showInvalidHours(messages, totalHours);
            }
        } else {
            Intbox intbox = (Intbox) allHoursInput;
            intbox.setValue(aggregate.getTotalHours());
        }
    }

    Component getFunction() {
        if (isGroupingRow()) {
            return new Label();
        } else {
            Hbox hbox = new Hbox();

            Combobox assignmentFunctionsCombo = getAssignmentFunctionsCombo();
            appendListener(assignmentFunctionsCombo);

            hbox.appendChild(assignmentFunctionsCombo);
            hbox
                    .appendChild(getAssignmentFunctionsConfigureButton(assignmentFunctionsCombo));

            return hbox;
        }
    }

    private void appendListener(final Combobox assignmentFunctionsCombo) {
        assignmentFunctionsCombo.addEventListener(Events.ON_CHANGE,
                new EventListener() {

                    @Override
                    public void onEvent(Event event) throws Exception {
                        ResourceAllocation<?> resourceAllocation = getAllocation();
                        AssignmentFunction assignmentFunction = resourceAllocation
                                .getAssignmentFunction();
                        IAssignmentFunctionConfiguration choosen = (IAssignmentFunctionConfiguration) assignmentFunctionsCombo
                                .getSelectedItem().getValue();
                        boolean hasChanged = !choosen
                                .isTargetedTo(assignmentFunction);
                        boolean noPreviousAllocation = assignmentFunction == null;
                        if (hasChanged
                                && (noPreviousAllocation || isChangeConfirmed())) {
                            choosen
                                    .applyDefaultFunction(resourceAllocation);
                        }
                    }

                    private boolean isChangeConfirmed()
                            throws InterruptedException {
                        int status = Messagebox
                                .show(
                                        _("You are going to change the assignment function. Are you sure?"),
                                        _("Confirm change"), Messagebox.YES
                                                | Messagebox.NO,
                                        Messagebox.QUESTION);
                        return Messagebox.YES == status;
                    }
                });
    }

    private IAssignmentFunctionConfiguration none = new IAssignmentFunctionConfiguration() {

        @Override
        public void goToConfigure() {
            try {
                Messagebox.show(
                        _("You need to select some function to configure"),
                        _("Warning"), Messagebox.OK, Messagebox.EXCLAMATION);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getName() {
            return _("None");
        }

        @Override
        public boolean isTargetedTo(AssignmentFunction function) {
            return function == null;
        }

        @Override
        public void applyDefaultFunction(
                ResourceAllocation<?> resourceAllocation) {
            resourceAllocation.setAssignmentFunction(null);
        }
    };

    private abstract class CommonStrechesConfiguration extends
            StrechesFunctionConfiguration {
        @Override
        protected void assignmentFunctionChanged() {
            reloadHoursSameRowForDetailItems();
            reloadAllHours();
            fireCellChanged();
        }

        @Override
        protected ResourceAllocation<?> getAllocation() {
            return Row.this.getAllocation();
        }

        @Override
        protected Component getParentOnWhichOpenWindow() {
            return allHoursInput.getParent();
        }
    }

    private IAssignmentFunctionConfiguration defaultStrechesFunction = new CommonStrechesConfiguration() {

        @Override
        protected String getTitle() {
            return _("Stretches list");
        }

        @Override
        protected boolean getChartsEnabled() {
            return true;
        }

        @Override
        protected Type getType() {
            return Type.DEFAULT;
        }

        @Override
        public String getName() {
            return _("Stretches");
        }
    };

    private IAssignmentFunctionConfiguration strechesWithInterpolation = new CommonStrechesConfiguration() {

        @Override
        protected String getTitle() {
            return _("Stretches with Interpolation");
        }

        @Override
        protected boolean getChartsEnabled() {
            return false;
        }

        @Override
        protected Type getType() {
            return Type.INTERPOLATED;
        }

        @Override
        public String getName() {
            return _("Interpolation");
        }
    };

    private IAssignmentFunctionConfiguration[] functions = { none,
            defaultStrechesFunction, strechesWithInterpolation };

    private Combobox getAssignmentFunctionsCombo() {
        AssignmentFunction assignmentFunction = getAllocation()
                .getAssignmentFunction();
        Combobox result = new Combobox();
        for (IAssignmentFunctionConfiguration each : functions) {
            Comboitem comboitem = new Comboitem(each.getName());
            comboitem.setValue(each);
            result.appendChild(comboitem);
            if (each.isTargetedTo(assignmentFunction)) {
                result.setSelectedItem(comboitem);
            }
        }
        return result;
    }

    private Button getAssignmentFunctionsConfigureButton(
            final Combobox assignmentFunctionsCombo) {
        final Button button = new Button(_("Configure"));

        button.addEventListener(Events.ON_CLICK, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                IAssignmentFunctionConfiguration configuration = (IAssignmentFunctionConfiguration) assignmentFunctionsCombo
                        .getSelectedItem().getValue();
                configuration.goToConfigure();
            }
        });
        return button;
    }

    Component getNameLabel() {
        if (nameLabel == null) {
            nameLabel = new Label();
            nameLabel.setValue(name);
        }
        return nameLabel;
    }

    private Row(IMessagesForUser messages,
            AdvancedAllocationController.Restriction restriction,
            String name, int level,
            List<? extends ResourceAllocation<?>> allocations) {
        this.messages = messages;
        this.restriction = restriction;
        this.name = name;
        if (level != 0) {
            this.name = "  · " + this.name;
        }
        this.level = level;
        this.aggregate = new AggregateOfResourceAllocations(
                new ArrayList<ResourceAllocation<?>>(allocations));
    }

    private Integer getHoursForDetailItem(DetailItem item) {
        DateTime startDate = item.getStartDate();
        DateTime endDate = item.getEndDate();
        return this.aggregate.hoursBetween(startDate.toLocalDate(), endDate
                .toLocalDate());
    }

    Component hoursOnInterval(DetailItem item) {
        Component result = isGroupingRow() ? new Label() : disableIfNeeded(
                item, noNegativeIntbox());
        reloadHoursOnInterval(result, item);
        componentsByDetailItem.put(item, result);
        addListenerIfNeeded(item, result);
        return result;
    }

    private Intbox disableIfNeeded(DetailItem item, Intbox intBox) {
        intBox.setDisabled(restriction.isDisabledEditionOn(item));
        return intBox;
    }

    private Intbox noNegativeIntbox() {
        Intbox result = new Intbox();
        result.setConstraint("no negative, no empty");
        return result;
    }

    private void addListenerIfNeeded(final DetailItem item,
            final Component component) {
        if (isGroupingRow()) {
            return;
        }
        final Intbox intbox = (Intbox) component;
        component.addEventListener(Events.ON_CHANGE, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                Integer value = intbox.getValue();
                LocalDate startDate = restriction.limitStartDate(item
                        .getStartDate().toLocalDate());
                LocalDate endDate = restriction.limitEndDate(item.getEndDate()
                        .toLocalDate());
                getAllocation().withPreviousAssociatedResources()
                                   .onInterval(startDate, endDate)
                                   .allocateHours(value);
                fireCellChanged(item);
                reloadAllHours();
            }
        });
    }

    private void reloadHoursOnInterval(Component component, DetailItem item) {
        if (isGroupingRow()) {
            Label label = (Label) component;
            label.setValue(getHoursForDetailItem(item) + "");
            label.setClass("calculated-hours");
        } else {
            Intbox intbox = (Intbox) component;
            intbox.setValue(getHoursForDetailItem(item));
        }
    }

    private ResourceAllocation<?> getAllocation() {
        if (isGroupingRow()) {
            throw new IllegalStateException("is grouping row");
        }
        return aggregate.getAllocationsSortedByStartDate().get(0);
    }

    private boolean isGroupingRow() {
        return level == 0;
    }

}
