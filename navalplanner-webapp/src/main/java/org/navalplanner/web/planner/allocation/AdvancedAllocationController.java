/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.AssignmentFunction;
import org.navalplanner.business.planner.entities.AssignmentFunction.ASSIGNMENT_FUNCTION_NAME;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SigmoidFunction;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.StretchesFunctionTypeEnum;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.web.common.EffortDurationBox;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.planner.allocation.streches.StrechesFunctionConfiguration;
import org.zkoss.ganttz.timetracker.ICellForDetailItemRenderer;
import org.zkoss.ganttz.timetracker.IConvertibleToColumn;
import org.zkoss.ganttz.timetracker.PairOfLists;
import org.zkoss.ganttz.timetracker.TimeTrackedTable;
import org.zkoss.ganttz.timetracker.TimeTrackedTableWithLeftPane;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.TimeTracker.IDetailItemFilter;
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
import org.zkoss.zkex.zul.LayoutRegion;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.api.Column;

/**
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
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

        EffortDuration getTotalEffort() {
            return getAggregate().getTotalEffort();
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
                LocalDate start = min(all.get(0)
                        .getStartConsideringAssignments(), all.get(0)
                        .getStartDate());
                LocalDate taskEndDate = LocalDate.fromDateFields(task
                        .getEndDate());
                LocalDate end = max(getEnd(all), taskEndDate);
                return new Interval(asDate(start), asDate(end));
            }
        }

        private LocalDate min(LocalDate... dates) {
            return Collections.min(Arrays.asList(dates), null);
        }

        private LocalDate max(LocalDate... dates) {
            return Collections.max(Arrays.asList(dates), null);
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

        boolean isAdvanceAssignmentOfSingleTask();
    }

    public abstract static class Restriction {

        public interface IRestrictionSource {

            EffortDuration getTotalEffort();

            LocalDate getStart();

            LocalDate getEnd();

            CalculatedValue getCalculatedValue();

        }

        public static Restriction build(IRestrictionSource restrictionSource) {
            switch (restrictionSource.getCalculatedValue()) {
            case END_DATE:
                return Restriction.fixedEffort(restrictionSource.getStart(),
                        restrictionSource.getTotalEffort());
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

        private static Restriction fixedEffort(LocalDate start,
                EffortDuration effort) {
            return new FixedEffortRestriction(start, effort);
        }

        abstract LocalDate limitStartDate(LocalDate startDate);

        abstract LocalDate limitEndDate(LocalDate localDate);

        abstract boolean isDisabledEditionOn(DetailItem item);

        public abstract boolean isInvalidTotalEffort(EffortDuration totalEffort);

        public abstract void showInvalidEffort(IMessagesForUser messages,
                EffortDuration totalEffort);

        public abstract void markInvalidEffort(Row groupingRow,
                EffortDuration currentEffort);
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
        public boolean isInvalidTotalEffort(EffortDuration totalEffort) {
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
        public void showInvalidEffort(IMessagesForUser messages,
                EffortDuration totalEffort) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void markInvalidEffort(Row groupingRow,
                EffortDuration currentEffort) {
            throw new UnsupportedOperationException();
        }
    }

    private static class FixedEffortRestriction extends Restriction {

        private final EffortDuration effort;

        private final LocalDate start;

        private FixedEffortRestriction(LocalDate start, EffortDuration effort) {
            this.start = start;
            this.effort = effort;
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
        LocalDate limitStartDate(LocalDate argStart) {
            return start.compareTo(argStart) > 0 ? start : argStart;
        }

        @Override
        public boolean isInvalidTotalEffort(EffortDuration totalEffort) {
            return this.effort.compareTo(totalEffort) != 0;
        }

        @Override
        public void showInvalidEffort(IMessagesForUser messages,
                EffortDuration totalEffort) {
            messages.showMessage(Level.WARNING, getMessage(totalEffort));
        }

        private String getMessage(EffortDuration totalEffort) {
            return _("there must be {0} effort instead of {1}",
                    effort.toFormattedString(), totalEffort.toFormattedString());
        }

        @Override
        public void markInvalidEffort(Row groupingRow,
                EffortDuration totalEffort) {
            groupingRow.markErrorOnTotal(getMessage(totalEffort));
        }
    }

    private static class NoRestriction extends Restriction {

        @Override
        boolean isDisabledEditionOn(DetailItem item) {
            return false;
        }

        @Override
        public boolean isInvalidTotalEffort(EffortDuration totalEffort) {
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
        public void markInvalidEffort(Row groupingRow,
                EffortDuration currentEffort) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void showInvalidEffort(IMessagesForUser messages,
                EffortDuration totalEffort) {
            throw new UnsupportedOperationException();
        }
    }

    private static final int VERTICAL_MAX_ELEMENTS = 25;

    private IMessagesForUser messages;
    private LayoutRegion insertionPointTimetracker;
    private Div insertionPointLeftPanel;
    private LayoutRegion insertionPointRightPanel;

    private Button paginationDownButton;
    private Button paginationUpButton;

    private Button verticalPaginationUpButton;
    private Button verticalPaginationDownButton;

    private TimeTracker timeTracker;

    private PaginatorFilter paginatorFilter;

    private Listbox advancedAllocationZoomLevel;

    private TimeTrackerComponentWithoutColumns timeTrackerComponent;
    private Grid leftPane;
    private TimeTrackedTable<Row> table;
    private IBack back;
    private List<AllocationInput> allocationInputs;
    private Component associatedComponent;

    private Listbox advancedAllocationHorizontalPagination;
    private Listbox advancedAllocationVerticalPagination;

    public AdvancedAllocationController(IBack back,
            List<AllocationInput> allocationInputs) {
        setInputData(back, allocationInputs);
    }

    private void setInputData(IBack back, List<AllocationInput> allocationInputs) {
        Validate.notNull(back);
        Validate.noNullElements(allocationInputs);
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
        normalLayout = comp.getFellow("normalLayout");
        noDataLayout = comp.getFellow("noDataLayout");
        onlyOneVisible = new OnlyOneVisible(normalLayout, noDataLayout);
        this.associatedComponent = comp;
        loadAndInitializeComponents();
        Clients.evalJavaScript("ADVANCE_ALLOCATIONS.listenToScroll();");

    }

    private void loadAndInitializeComponents() {
        messages = new MessagesForUser(associatedComponent
                .getFellow("messages"));
        if (allocationInputs.isEmpty()) {
            onlyOneVisible.showOnly(noDataLayout);
        } else {
            onlyOneVisible.showOnly(normalLayout);
            createComponents();
            insertComponentsInLayout();
            timeTrackerComponent.afterCompose();
            table.afterCompose();
        }
    }

    private class PaginatorFilter implements IDetailItemFilter {

        private DateTime intervalStart;
        private DateTime intervalEnd;

        private DateTime paginatorStart;
        private DateTime paginatorEnd;

        private ZoomLevel zoomLevel = ZoomLevel.DETAIL_ONE;

        @Override
        public Interval getCurrentPaginationInterval() {
            return new Interval(intervalStart.toDate(), intervalEnd.toDate());
        }

        private Period intervalIncrease() {
            switch (zoomLevel) {
            case DETAIL_ONE:
                return Period.years(5);
            case DETAIL_TWO:
                return Period.years(5);
            case DETAIL_THREE:
                return Period.years(2);
            case DETAIL_FOUR:
                return Period.months(6);
            case DETAIL_FIVE:
                return Period.weeks(6);
            }
            return Period.years(5);
        }

        public void populateHorizontalListbox() {
            advancedAllocationHorizontalPagination.getItems().clear();
            DateTimeFormatter df = DateTimeFormat.forPattern("dd/MMM/yyyy");
            if (intervalStart != null) {
                DateTime itemStart = intervalStart;
                DateTime itemEnd = intervalStart.plus(intervalIncrease());
                while (intervalEnd.isAfter(itemStart)) {
                    if (intervalEnd.isBefore(itemEnd)
                            || !intervalEnd.isAfter(itemEnd
                                    .plus(intervalIncrease()))) {
                        itemEnd = intervalEnd;
                    }
                    Listitem item = new Listitem(df.print(itemStart) + " - "
                            + df.print(itemEnd.minusDays(1)));
                    advancedAllocationHorizontalPagination.appendChild(item);
                    itemStart = itemEnd;
                    itemEnd = itemEnd.plus(intervalIncrease());
                }
            }
            advancedAllocationHorizontalPagination
                    .setDisabled(advancedAllocationHorizontalPagination
                            .getItems().size() < 2);
            advancedAllocationHorizontalPagination.setSelectedIndex(0);
        }

        public void goToHorizontalPage(int interval) {
            if (interval >= 0) {
                paginatorStart = intervalStart;
                for (int i = 0; i < interval; i++) {
                    paginatorStart = paginatorStart.plus(intervalIncrease());
                }
                paginatorEnd = paginatorStart.plus(intervalIncrease());
                // Avoid reduced intervals
                if (!intervalEnd.isAfter(paginatorEnd.plus(intervalIncrease()))) {
                    paginatorEnd = intervalEnd;
                }
                updatePaginationButtons();
            }
        }

        @Override
        public Collection<DetailItem> selectsFirstLevel(
                Collection<DetailItem> firstLevelDetails) {
            ArrayList<DetailItem> result = new ArrayList<DetailItem>();
            for (DetailItem each : firstLevelDetails) {
                if ((each.getStartDate() == null)
                        || !(each.getStartDate().isBefore(paginatorStart))
                        && (each.getStartDate().isBefore(paginatorEnd))) {
                    result.add(each);
                }
            }
            return result;
        }

        @Override
        public Collection<DetailItem> selectsSecondLevel(
                Collection<DetailItem> secondLevelDetails) {
            ArrayList<DetailItem> result = new ArrayList<DetailItem>();
            for (DetailItem each : secondLevelDetails) {
                if ((each.getStartDate() == null)
                        || !(each.getStartDate().isBefore(paginatorStart))
                        && (each.getStartDate().isBefore(paginatorEnd))) {
                    result.add(each);
                }
            }
            return result;
        }

        public void next() {
            paginatorStart = paginatorStart.plus(intervalIncrease());
            paginatorEnd = paginatorEnd.plus(intervalIncrease());
            // Avoid reduced last intervals
            if (!intervalEnd.isAfter(paginatorEnd.plus(intervalIncrease()))) {
                paginatorEnd = paginatorEnd.plus(intervalIncrease());
            }
            updatePaginationButtons();
        }

        public void previous() {
            paginatorStart = paginatorStart.minus(intervalIncrease());
            paginatorEnd = paginatorEnd.minus(intervalIncrease());
            updatePaginationButtons();
        }

        private void updatePaginationButtons() {
            paginationDownButton.setDisabled(isFirstPage());
            paginationUpButton.setDisabled(isLastPage());
        }

        public boolean isFirstPage() {
            return !(paginatorStart.isAfter(intervalStart));
        }

        public boolean isLastPage() {
            return ((paginatorEnd.isAfter(intervalEnd)) || (paginatorEnd
                    .isEqual(intervalEnd)));
        }

        public void setZoomLevel(ZoomLevel detailLevel) {
            zoomLevel = detailLevel;
        }

        public void setInterval(Interval realInterval) {
            intervalStart = realInterval.getStart().toDateTimeAtStartOfDay();
            intervalEnd = realInterval.getFinish().toDateTimeAtStartOfDay();
            paginatorStart = intervalStart;
            paginatorEnd = intervalStart.plus(intervalIncrease());
            if ((paginatorEnd.plus(intervalIncrease()).isAfter(intervalEnd))) {
                paginatorEnd = intervalEnd;
            }
            updatePaginationButtons();
        }

        @Override
        public void resetInterval() {
            setInterval(timeTracker.getRealInterval());
        }
    }

    private void createComponents() {
        timeTracker = new TimeTracker(addMarginTointerval(), self);
        paginatorFilter = new PaginatorFilter();
        paginatorFilter.setZoomLevel(timeTracker.getDetailLevel());
        paginatorFilter.setInterval(timeTracker.getRealInterval());
        paginationUpButton.setDisabled(isLastPage());
        advancedAllocationZoomLevel.setSelectedIndex(timeTracker
                .getDetailLevel().ordinal());
        timeTracker.setFilter(paginatorFilter);
        timeTracker.addZoomListener(new IZoomLevelChangedListener() {
            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                paginatorFilter.setZoomLevel(detailLevel);
                paginatorFilter.setInterval(timeTracker.getRealInterval());
                timeTracker.setFilter(paginatorFilter);
                populateHorizontalListbox();
                Clients.evalJavaScript("ADVANCE_ALLOCATIONS.listenToScroll();");
            }
        });
        timeTrackerComponent = new TimeTrackerComponentWithoutColumns(
                timeTracker, "timeTracker");
        timeTrackedTableWithLeftPane = new TimeTrackedTableWithLeftPane<Row, Row>(
                getDataSource(), getColumnsForLeft(), getLeftRenderer(),
                getRightRenderer(), timeTracker);
        table = timeTrackedTableWithLeftPane.getRightPane();
        table.setSclass("timeTrackedTableWithLeftPane");
        leftPane = timeTrackedTableWithLeftPane.getLeftPane();
        leftPane.setFixedLayout(true);
        Clients.evalJavaScript("ADVANCE_ALLOCATIONS.listenToScroll();");
        populateHorizontalListbox();
    }

    public void paginationDown() {
        paginatorFilter.previous();
        reloadComponent();

        advancedAllocationHorizontalPagination
                .setSelectedIndex(advancedAllocationHorizontalPagination
                        .getSelectedIndex() - 1);

    }

    public void paginationUp() {
        paginatorFilter.next();
        reloadComponent();
        advancedAllocationHorizontalPagination.setSelectedIndex(Math.max(0,
                advancedAllocationHorizontalPagination.getSelectedIndex()) + 1);
    }

    public void goToSelectedHorizontalPage() {
        paginatorFilter
                .goToHorizontalPage(advancedAllocationHorizontalPagination
                        .getSelectedIndex());
        reloadComponent();
    }

    private void populateHorizontalListbox() {
        advancedAllocationHorizontalPagination.setVisible(true);
        paginatorFilter.populateHorizontalListbox();
    }

    private void reloadComponent() {
        timeTrackedTableWithLeftPane.reload();
        timeTrackerComponent.recreate();
        // Reattach listener for zoomLevel changes. May be optimized
        timeTracker.addZoomListener(new IZoomLevelChangedListener() {
            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                paginatorFilter.setZoomLevel(detailLevel);
                paginatorFilter.setInterval(timeTracker.getRealInterval());
                timeTracker.setFilter(paginatorFilter);
                populateHorizontalListbox();
                Clients.evalJavaScript("ADVANCE_ALLOCATIONS.listenToScroll();");
            }
        });
        Clients.evalJavaScript("ADVANCE_ALLOCATIONS.listenToScroll();");
    }

    public boolean isFirstPage() {
        return paginatorFilter.isFirstPage();
    }

    public boolean isLastPage() {
        return paginatorFilter.isLastPage();
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
            EffortDuration totalEffort = allocationInput.getTotalEffort();
            Restriction restriction = allocationInput.getResultReceiver()
                    .createRestriction();
            if (restriction.isInvalidTotalEffort(totalEffort)) {
                Row groupingRow = groupingRows.get(allocationInput);
                restriction.markInvalidEffort(groupingRow, totalEffort);
            }
        }
        back.goBack();
        for (AllocationInput allocationInput : allocationInputs) {
            allocationInput.getResultReceiver().accepted(allocationInput
                    .getAggregate());
        }
    }

    public void onClick$saveButton() {
        for (AllocationInput allocationInput : allocationInputs) {
            EffortDuration totalEffort = allocationInput.getTotalEffort();
            Restriction restriction = allocationInput.getResultReceiver()
                    .createRestriction();
            if (restriction.isInvalidTotalEffort(totalEffort)) {
                Row groupingRow = groupingRows.get(allocationInput);
                restriction.markInvalidEffort(groupingRow, totalEffort);
            }
        }
        for (AllocationInput allocationInput : allocationInputs) {
            allocationInput.getResultReceiver().accepted(
                    allocationInput.getAggregate());
        }
        try {
            Messagebox.show(_("Advanced assignment saved"), _("Information"),
                    Messagebox.OK, Messagebox.INFORMATION);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void onClick$cancelButton() {
        back.goBack();
        for (AllocationInput allocationInput : allocationInputs) {
            allocationInput.getResultReceiver().cancel();
        }
    }

    public ListModel getZoomLevels() {
        ZoomLevel[] selectableZoomlevels = { ZoomLevel.DETAIL_ONE,
                ZoomLevel.DETAIL_TWO, ZoomLevel.DETAIL_THREE,
                ZoomLevel.DETAIL_FOUR, ZoomLevel.DETAIL_FIVE };
        return new SimpleListModel(selectableZoomlevels);
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

    private OnlyOneVisible onlyOneVisible;
    private Component normalLayout;
    private Component noDataLayout;
    private TimeTrackedTableWithLeftPane<Row, Row> timeTrackedTableWithLeftPane;

    private int verticalIndex = 0;
    private List<Integer> verticalPaginationIndexes;
    private int verticalPage;

    private List<Row> getRows() {
        if (rowsCached != null) {
            return filterRows(rowsCached);
        }
        rowsCached = new ArrayList<Row>();
        int position = 1;
        for (AllocationInput allocationInput : allocationInputs) {
            if (allocationInput.getAggregate()
                    .getAllocationsSortedByStartDate().isEmpty()) {
            } else {
                Row groupingRow = buildGroupingRow(allocationInput);
                groupingRow.setDescription(position + " " + allocationInput.getTaskName());
                groupingRows.put(allocationInput, groupingRow);
                rowsCached.add(groupingRow);
                List<Row> genericRows = genericRows(allocationInput);
                groupingRow.listenTo(genericRows);
                rowsCached.addAll(genericRows);
                List<Row> specificRows = specificRows(allocationInput);
                groupingRow.listenTo(specificRows);
                rowsCached.addAll(specificRows);
                position++;
            }
        }
        populateVerticalListbox();
        return filterRows(rowsCached);
    }

    private List<Row> filterRows(List<Row> rows) {
        verticalPaginationUpButton.setDisabled(verticalIndex <= 0);
        verticalPaginationDownButton
                .setDisabled((verticalIndex + VERTICAL_MAX_ELEMENTS) >= rows
                        .size());
        if(advancedAllocationVerticalPagination.getChildren().size() >= 2) {
            advancedAllocationVerticalPagination.setDisabled(false);
            advancedAllocationVerticalPagination.setSelectedIndex(
                    verticalPage);
        }
        else {
            advancedAllocationVerticalPagination.setDisabled(true);
        }
        return rows.subList(verticalIndex,
                verticalPage + 1 < verticalPaginationIndexes.size() ?
                       verticalPaginationIndexes.get(verticalPage + 1).intValue() :
                       rows.size());
    }

    public void verticalPagedown() {
        verticalPage++;
        verticalIndex = verticalPaginationIndexes.get(verticalPage);
        timeTrackedTableWithLeftPane.reload();
    }

    public void setVerticalPagedownButtonDisabled(boolean disabled) {
        verticalPaginationUpButton.setDisabled(disabled);
    }

    public void verticalPageup() {
        verticalPage--;
        verticalIndex = verticalPaginationIndexes.get(verticalPage);
        timeTrackedTableWithLeftPane.reload();
    }

    public void goToSelectedVerticalPage() {
        verticalPage = advancedAllocationVerticalPagination.
            getSelectedIndex();
        verticalIndex = verticalPaginationIndexes.get(verticalPage);
        timeTrackedTableWithLeftPane.reload();
    }

    public void populateVerticalListbox() {
        if (rowsCached != null) {
            verticalPage = 0;
            verticalPaginationIndexes = new ArrayList<Integer>();
            advancedAllocationVerticalPagination.getChildren().clear();
            for(int i=0; i<rowsCached.size(); i=
                    correctVerticalPageDownPosition(i+VERTICAL_MAX_ELEMENTS)) {
                int endPosition = correctVerticalPageUpPosition(Math.min(
                        rowsCached.size(), i+VERTICAL_MAX_ELEMENTS) - 1);
                String label = rowsCached.get(i).getDescription() + " - " +
                    rowsCached.get(endPosition).getDescription();
                Listitem item = new Listitem();
                item.appendChild(new Listcell(label));
                advancedAllocationVerticalPagination.appendChild(item);
                verticalPaginationIndexes.add(i);
            }
            if (!rowsCached.isEmpty()) {
                advancedAllocationVerticalPagination.setSelectedIndex(0);
            }
        }
    }

    private int correctVerticalPageUpPosition(int position) {
        int correctedPosition = position;
        //moves the pointer up until it finds the previous grouping row
        //or the beginning of the list
        while(correctedPosition > 0 &&
                !rowsCached.get(correctedPosition).isGroupingRow()) {
            correctedPosition--;
        }
        return correctedPosition;
    }

    private int correctVerticalPageDownPosition(int position) {
        int correctedPosition = position;
        //moves the pointer down until it finds the next grouping row
        //or the end of the list
        while(correctedPosition < rowsCached.size() &&
                !rowsCached.get(correctedPosition).isGroupingRow()) {
            correctedPosition++;
        }
        return correctedPosition;
    }

    private List<Row> specificRows(AllocationInput allocationInput) {
        List<Row> result = new ArrayList<Row>();
        for (SpecificResourceAllocation specificResourceAllocation : allocationInput.getAggregate()
                .getSpecificAllocations()) {
            result.add(createSpecificRow(specificResourceAllocation,
                    allocationInput.getResultReceiver().createRestriction(), allocationInput.task));
        }
        return result;
    }

    private Row createSpecificRow(
            SpecificResourceAllocation specificResourceAllocation,
            Restriction restriction, TaskElement task) {
        return Row.createRow(messages, restriction,
                specificResourceAllocation.getResource()
                        .getName(), 1, Arrays
                .asList(specificResourceAllocation), specificResourceAllocation
                .getResource().getShortDescription(),
                specificResourceAllocation.getResource().isLimitingResource(), task);
    }

    private List<Row> genericRows(AllocationInput allocationInput) {
        List<Row> result = new ArrayList<Row>();
        for (GenericResourceAllocation genericResourceAllocation : allocationInput.getAggregate()
                .getGenericAllocations()) {
            result.add(buildGenericRow(genericResourceAllocation,
                    allocationInput.getResultReceiver().createRestriction(), allocationInput.task));
        }
        return result;
    }

    private Row buildGenericRow(
            GenericResourceAllocation genericResourceAllocation,
            Restriction restriction, TaskElement task) {
        return Row.createRow(messages, restriction, Criterion
                .getCaptionFor(genericResourceAllocation.getCriterions()), 1, Arrays
                .asList(genericResourceAllocation), genericResourceAllocation
                .isLimiting(), task);
    }

    private Row buildGroupingRow(AllocationInput allocationInput) {
        Restriction restriction = allocationInput.getResultReceiver()
                .createRestriction();
        String taskName = allocationInput.getTaskName();
        Row groupingRow = Row.createRow(messages, restriction, taskName, 0,
                allocationInput.getAllocationsSortedByStartDate(), false, allocationInput.task);
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
        result.add(new ColumnOnRow(_("Efforts")) {
            @Override
            public Component cellFor(Row row) {
                return row.getAllEffort();
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
            public PairOfLists<Row, Row> call() {
                List<Row> rows = getRows();
                return new PairOfLists<Row, Row>(rows, rows);
            }
        };
    }

    private ICellForDetailItemRenderer<DetailItem, Row> getRightRenderer() {
        return new ICellForDetailItemRenderer<DetailItem, Row>() {

            @Override
            public Component cellFor(DetailItem item, Row data) {
                return data.effortOnInterval(item);
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

    private Interval addMarginTointerval() {
        Interval interval = intervalFromData();
        // No global margin is added by default
        return interval;
    }

    public boolean isAdvancedAllocationOfSingleTask() {
        return back.isAdvanceAssignmentOfSingleTask();
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
        column.setLabel(_(columnName));
        column.setSclass(columnName.toLowerCase());
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
 List<? extends ResourceAllocation<?>> allocations,
            String description, boolean limiting, TaskElement task) {
        Row newRow = new Row(messages, restriction, name, level, allocations,
                limiting, task);
        newRow.setDescription(description);
        return newRow;
    }

    static Row createRow(IMessagesForUser messages,
            AdvancedAllocationController.Restriction restriction, String name,
            int level, List<? extends ResourceAllocation<?>> allocations,
            boolean limiting, TaskElement task) {
        return new Row(messages, restriction, name, level, allocations,
                limiting, task);
    }

    public void markErrorOnTotal(String message) {
        throw new WrongValueException(allEffortInput, message);
    }

    private EffortDurationBox allEffortInput;

    private Label nameLabel;

    private List<CellChangedListener> listeners = new ArrayList<CellChangedListener>();

    private Map<DetailItem, Component> componentsByDetailItem = new WeakHashMap<DetailItem, Component>();

    private String name;

    private String description;

    private int level;

    private final AggregateOfResourceAllocations aggregate;

    private final AdvancedAllocationController.Restriction restriction;

    private final IMessagesForUser messages;

    private final String functionName;

    private TaskElement task;

    void listenTo(Collection<Row> rows) {
        for (Row row : rows) {
            listenTo(row);
        }
    }

    void listenTo(Row row) {
        row.add(new CellChangedListener() {

            @Override
            public void changeOnGlobal() {
                reloadAllEffort();
                reloadEffortsSameRowForDetailItems();
            }

            @Override
            public void changeOn(DetailItem detailItem) {
                Component component = componentsByDetailItem.get(detailItem);
                if (component == null) {
                    return;
                }
                reloadEffortOnInterval(component, detailItem);
                reloadAllEffort();
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

    Component getAllEffort() {
        if (allEffortInput == null) {
            allEffortInput = buildSumAllEffort();
            reloadAllEffort();
            addListenerIfNeeded(allEffortInput);
        }
        return allEffortInput;
    }

    private EffortDurationBox buildSumAllEffort() {
        return (isGroupingRow() || isLimiting) ? EffortDurationBox
                .notEditable() : new EffortDurationBox();
    }

    private void addListenerIfNeeded(Component allEffortComponent) {
        if (isGroupingRow() || isLimiting) {
            return;
        }
        final EffortDurationBox effortDurationBox = (EffortDurationBox) allEffortComponent;
        effortDurationBox.addEventListener(Events.ON_CHANGE,
                new EventListener() {

            @Override
                    public void onEvent(Event event) {
                        EffortDuration value = effortDurationBox
                                .getEffortDurationValue();

                getAllocation().withPreviousAssociatedResources().onIntervalWithinTask(
                        getAllocation().getStartDate(),
                        getAllocation().getEndDate())
                        .allocate(value);
                fireCellChanged();
                        reloadEffortsSameRowForDetailItems();
                        reloadAllEffort();
            }
        });
    }

    private void reloadEffortsSameRowForDetailItems() {
        for (Entry<DetailItem, Component> entry : componentsByDetailItem
                .entrySet()) {
            reloadEffortOnInterval(entry.getValue(), entry.getKey());
        }
    }

    private void reloadAllEffort() {
        if (allEffortInput == null) {
            return;
        }
        EffortDuration allEffort = aggregate.getTotalEffort();
        allEffortInput.setValue(allEffort);
        Clients.closeErrorBox(allEffortInput);
        if (isLimiting) {
            allEffortInput.setDisabled(true);
        }
        if (restriction.isInvalidTotalEffort(allEffort)) {
            restriction.showInvalidEffort(messages, allEffort);
        }
    }

    private Hbox hboxAssigmentFunctionsCombobox = null;

    Component getFunction() {
        if (isGroupingRow()) {
            return new Label();
        } else if (isLimiting) {
            return new Label(_("Limiting assignment"));
        } else {
            if (hboxAssigmentFunctionsCombobox == null) {
                initializeAssigmentFunctionsCombobox();
            }
            return hboxAssigmentFunctionsCombobox;
        }
    }

    private void initializeAssigmentFunctionsCombobox() {
        hboxAssigmentFunctionsCombobox = new Hbox();
        AssignmentFunctionCombobox assignmentFunctionsCombo = new AssignmentFunctionCombobox(
                functions, getAllocation().getAssignmentFunction());
        hboxAssigmentFunctionsCombobox.appendChild(assignmentFunctionsCombo);
        hboxAssigmentFunctionsCombobox
                .appendChild(getAssignmentFunctionsConfigureButton(assignmentFunctionsCombo));
    }

    /**
     * @author Diego Pino García <dpino@igalia.com>
     *
     *         Encapsulates the logic of the combobox used for selecting what
     *         type of assignment function to apply
     */
    class AssignmentFunctionCombobox extends Combobox {

        private String previousValue;

        public AssignmentFunctionCombobox(IAssignmentFunctionConfiguration[] functions,
                AssignmentFunction initialValue) {
            for (IAssignmentFunctionConfiguration each : functions) {
                Comboitem comboitem = comboItem(each);
                this.appendChild(comboitem);
                if (each.isTargetedTo(initialValue)) {
                    selectItem(comboitem);
                }
            }
            this.addEventListener(Events.ON_CHANGE, onChangeCombobox(this));
        }

        private Comboitem comboItem(IAssignmentFunctionConfiguration assignmentFunction) {
            Comboitem comboitem = new Comboitem(assignmentFunction.getName());
            comboitem.setValue(assignmentFunction);
            return comboitem;
        }

        private void selectItem(Comboitem comboitem) {
            String functionName = ((IAssignmentFunctionConfiguration) comboitem.getValue()).getName();
            this.setSelectedItem(comboitem);
            this.setValue(functionName);
            this.setPreviousValue(functionName);
        }

        private EventListener onChangeCombobox(final AssignmentFunctionCombobox combobox) {
            return new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    final String currentValue = combobox.getValue();
                    final String previousValue = combobox.getPreviousValue();

                    // Same value selected
                    if (currentValue.equals(previousValue)) {
                        return;
                    }
                    // Cannot apply function if task contains consolidated day assignments
                    final ResourceAllocation<?> resourceAllocation = getAllocation();
                    if (isSigmoid(currentValue)
                            && !resourceAllocation
                                    .getConsolidatedAssignments().isEmpty()) {
                        showCannotApplySigmoidFunction();
                        combobox.setValue(previousValue);
                        return;
                    }
                    // User didn't accept
                    if (showConfirmChangeFunctionDialog() != Messagebox.YES) {
                        combobox.setValue(previousValue);
                        return;
                    }
                    // Apply sigmoid function
                    IAssignmentFunctionConfiguration function = getSelectedFunction(combobox);
                    if (function != null) {
                        setPreviousValue(currentValue);
                        function.applyOn(resourceAllocation);
                    }
                }
            };
        }

        private String getPreviousValue() {
            return previousValue;
        }

        private void setPreviousValue(String value) {
            this.previousValue = value;
        }

        private boolean isSigmoid(String value) {
            return ASSIGNMENT_FUNCTION_NAME.SIGMOID.toString().equals(value);
        }

        private void showCannotApplySigmoidFunction() {
            try {
                Messagebox
                        .show(_("Task contains consolidated progress. Cannot apply sigmoid function."),
                                _("Error"), Messagebox.OK, Messagebox.ERROR);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private int showConfirmChangeFunctionDialog()
                throws InterruptedException {
            return Messagebox
                    .show(_("You are going to change the assignment function. Are you sure?"),
                            _("Confirm change"),
                            Messagebox.YES | Messagebox.NO, Messagebox.QUESTION);
        }

        private IAssignmentFunctionConfiguration getSelectedFunction(
                Combobox combobox) {
            Comboitem selectedItem = combobox.getSelectedItem();
            return (selectedItem != null) ? (IAssignmentFunctionConfiguration) selectedItem
                    .getValue() : null;
        }

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
            return ASSIGNMENT_FUNCTION_NAME.NONE.toString();
        }

        @Override
        public boolean isTargetedTo(AssignmentFunction function) {
            return function == null;
        }

        @Override
        public void applyOn(
                ResourceAllocation<?> resourceAllocation) {
            resourceAllocation.setAssignmentFunction(null);
            reloadEfforts();
        }

        private void reloadEfforts() {
            reloadEffortsSameRowForDetailItems();
            reloadAllEffort();
            fireCellChanged();
        }

    };

    private abstract class CommonStrechesConfiguration extends
            StrechesFunctionConfiguration {
        @Override
        protected void assignmentFunctionChanged() {
            reloadEffortsSameRowForDetailItems();
            reloadAllEffort();
            fireCellChanged();
        }

        @Override
        protected ResourceAllocation<?> getAllocation() {
            return Row.this.getAllocation();
        }

        @Override
        protected Component getParentOnWhichOpenWindow() {
            return allEffortInput.getParent();
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
        protected StretchesFunctionTypeEnum getType() {
            return StretchesFunctionTypeEnum.STRETCHES;
        }

        @Override
        public String getName() {
            return ASSIGNMENT_FUNCTION_NAME.STRETCHES.toString();
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
        protected StretchesFunctionTypeEnum getType() {
            return StretchesFunctionTypeEnum.INTERPOLATED;
        }

        @Override
        public String getName() {
            return ASSIGNMENT_FUNCTION_NAME.INTERPOLATION.toString();
        }
    };

    private IAssignmentFunctionConfiguration sigmoidFunction = new IAssignmentFunctionConfiguration() {

        @Override
        public void goToConfigure() {
            try {
                Messagebox.show(_("Sigmoid function applied to current resource"),
                        _("Sigmoid function"),
                        Messagebox.OK, Messagebox.INFORMATION);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getName() {
            return ASSIGNMENT_FUNCTION_NAME.SIGMOID.toString();
        }

        @Override
        public boolean isTargetedTo(AssignmentFunction function) {
            return function instanceof SigmoidFunction;
        }

        @Override
        public void applyOn(
                ResourceAllocation<?> resourceAllocation) {
            resourceAllocation.setAssignmentFunction(SigmoidFunction.create());
            reloadEfforts();
        }

        private void reloadEfforts() {
            reloadEffortsSameRowForDetailItems();
            reloadAllEffort();
            fireCellChanged();
        }

    };

    private IAssignmentFunctionConfiguration[] functions = {
            none,
            defaultStrechesFunction,
            strechesWithInterpolation,
            sigmoidFunction
    };

    private boolean isLimiting;

    private Button getAssignmentFunctionsConfigureButton(
            final Combobox assignmentFunctionsCombo) {
        final Button button = new Button("", "/common/img/ico_editar1.png");
        button.setHoverImage("/common/img/ico_editar.png");
        button.setSclass("icono");
        button.setTooltiptext(_("Configure"));
        button.addEventListener(Events.ON_CLICK, new EventListener() {

            @Override
            public void onEvent(Event event) {
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
            if (!StringUtils.isBlank(description)) {
                nameLabel.setTooltiptext(description);
            } else {
                nameLabel.setTooltiptext(name);
            }

            nameLabel.setSclass("level" + level);
        }
        return nameLabel;
    }

    private Row(IMessagesForUser messages,
            AdvancedAllocationController.Restriction restriction, String name,
            int level, List<? extends ResourceAllocation<?>> allocations,
            boolean limiting, TaskElement task) {
        this.messages = messages;
        this.restriction = restriction;
        this.name = name;
        this.level = level;
        this.isLimiting = limiting;
        this.task = task;
        this.aggregate = AggregateOfResourceAllocations
                .createFromSatisfied(new ArrayList<ResourceAllocation<?>>(allocations));
        this.functionName = getAssignmentFunctionName(allocations);
    }

    private String getAssignmentFunctionName(
            List<? extends ResourceAllocation<?>> allocations) {
        AssignmentFunction function = getAssignmentFunction(allocations);
        return (function != null) ? function.getName()
                : ASSIGNMENT_FUNCTION_NAME.NONE.toString();
    }

    private AssignmentFunction getAssignmentFunction(
            List<? extends ResourceAllocation<?>> allocations) {
        if (allocations != null) {
            ResourceAllocation<?> allocation = allocations.iterator().next();
            return allocation.getAssignmentFunction();
        }
        return null;
    }

    private EffortDuration getEffortForDetailItem(DetailItem item) {
        DateTime startDate = item.getStartDate();
        DateTime endDate = item.getEndDate();
        return this.aggregate.effortBetween(startDate.toLocalDate(), endDate
                .toLocalDate());
    }

    Component effortOnInterval(DetailItem item) {
        Component result = cannotBeEdited(item) ? new Label()
                : disableIfNeeded(item, new EffortDurationBox());
        reloadEffortOnInterval(result, item);
        componentsByDetailItem.put(item, result);
        addListenerIfNeeded(item, result);
        return result;
    }

    private boolean cannotBeEdited(DetailItem item) {
        return isGroupingRow() || doesNotIntersectWithTask(item)
                || isBeforeLatestConsolidation(item);
    }

    private EffortDurationBox disableIfNeeded(DetailItem item,
            EffortDurationBox effortDurationBox) {
        effortDurationBox.setDisabled(restriction.isDisabledEditionOn(item));
        return effortDurationBox;
    }

    private void addListenerIfNeeded(final DetailItem item,
            final Component component) {
        if (cannotBeEdited(item)) {
            return;
        }
        final EffortDurationBox effortBox = (EffortDurationBox) component;
        component.addEventListener(Events.ON_CHANGE, new EventListener() {

            @Override
            public void onEvent(Event event) {
                EffortDuration value = effortBox.getEffortDurationValue();
                LocalDate startDate = restriction.limitStartDate(item
                        .getStartDate().toLocalDate());
                LocalDate endDate = restriction.limitEndDate(item.getEndDate()
                        .toLocalDate());
                getAllocation().withPreviousAssociatedResources()
                                   .onIntervalWithinTask(startDate, endDate)
                                   .allocate(value);
                fireCellChanged(item);
                effortBox.setRawValue(getEffortForDetailItem(item));
                reloadAllEffort();
            }
        });
    }

    private void reloadEffortOnInterval(Component component, DetailItem item) {
        if (cannotBeEdited(item)) {
            Label label = (Label) component;
            label.setValue(getEffortForDetailItem(item).toFormattedString());
            label.setClass(getLabelClassFor(item));
        } else {
            EffortDurationBox effortDurationBox = (EffortDurationBox) component;
            effortDurationBox.setValue(getEffortForDetailItem(item));
            if (isLimiting) {
                effortDurationBox.setDisabled(true);
                effortDurationBox.setSclass(" limiting");
            }
        }
    }

    private String getLabelClassFor(DetailItem item) {
        if (isGroupingRow()) {
            return "calculated-hours";
        }
        if (doesNotIntersectWithTask(item)) {
            return "unmodifiable-hours";
        }
        if (isBeforeLatestConsolidation(item)) {
            return "consolidated-hours";
        }
        return "";
    }

    private boolean doesNotIntersectWithTask(DetailItem item) {
        return isBeforeTaskStartDate(item) || isAfterTaskEndDate(item);
    }

    private boolean isBeforeTaskStartDate(DetailItem item) {
        return task.getIntraDayStartDate().compareTo(
                item.getEndDate().toLocalDate()) >= 0;
    }

    private boolean isAfterTaskEndDate(DetailItem item) {
        return task.getIntraDayEndDate().compareTo(
                item.getStartDate().toLocalDate()) <= 0;
    }

    private boolean isBeforeLatestConsolidation(DetailItem item) {
        if(!((Task)task).hasConsolidations()) {
            return false;
        }
        LocalDate d = ((Task) task).getFirstDayNotConsolidated().getDate();
        DateTime firstDayNotConsolidated =
            new DateTime(d.getYear(), d.getMonthOfYear(),
                    d.getDayOfMonth(), 0, 0, 0, 0);
        return item.getStartDate().compareTo(firstDayNotConsolidated) < 0;
    }

    private ResourceAllocation<?> getAllocation() {
        if (isGroupingRow()) {
            throw new IllegalStateException("is grouping row");
        }
        return aggregate.getAllocationsSortedByStartDate().get(0);
    }

    public boolean isGroupingRow() {
        return level == 0;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
