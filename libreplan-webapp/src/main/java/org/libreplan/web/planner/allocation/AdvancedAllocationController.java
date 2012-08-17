/*
 * This file is part of LibrePlan
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

package org.libreplan.web.planner.allocation;

import static org.libreplan.web.I18nHelper._;

import java.math.BigDecimal;
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
import org.libreplan.business.planner.entities.AggregateOfExpensesLines;
import org.libreplan.business.planner.entities.AggregateOfResourceAllocations;
import org.libreplan.business.planner.entities.AssignmentFunction;
import org.libreplan.business.planner.entities.AssignmentFunction.AssignmentFunctionName;
import org.libreplan.business.planner.entities.CalculatedValue;
import org.libreplan.business.planner.entities.GenericResourceAllocation;
import org.libreplan.business.planner.entities.IAggregate;
import org.libreplan.business.planner.entities.ManualFunction;
import org.libreplan.business.planner.entities.PositionConstraintType;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.SigmoidFunction;
import org.libreplan.business.planner.entities.SpecificResourceAllocation;
import org.libreplan.business.planner.entities.StretchesFunctionTypeEnum;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.web.I18nHelper;
import org.libreplan.web.common.EffortDurationBox;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.OnlyOneVisible;
import org.libreplan.web.common.Util;
import org.libreplan.web.planner.allocation.AdvancedAllocationController.Restriction;
import org.libreplan.web.planner.allocation.streches.StrechesFunctionConfiguration;
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
import org.zkoss.zul.Button;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.LayoutRegion;
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

        private final AggregateOfExpensesLines aggregateExpenses;

        private final IAdvanceAllocationResultReceiver resultReceiver;

        private final TaskElement task;

        public AllocationInput(AggregateOfResourceAllocations aggregate,
                AggregateOfExpensesLines aggregateExpenses,
                TaskElement task,
                IAdvanceAllocationResultReceiver resultReceiver) {
            Validate.notNull(aggregate);
            Validate.notNull(resultReceiver);
            Validate.notNull(task);
            this.aggregate = aggregate;
            this.aggregateExpenses = aggregateExpenses;
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

        public AggregateOfExpensesLines getAggregateExpenses() {
            return aggregateExpenses;
        }

        private void updateDatesTask(LocalDate startInclusive,
                LocalDate endExclusive) {
            task.setStartDate(startInclusive.toDateTimeAtStartOfDay().toDate());
            if (task.isTask()) {
                if (((Task) task).getPositionConstraint().getConstraintType()
                        .equals(PositionConstraintType.START_NOT_EARLIER_THAN)) {
                    ((Task) task).getPositionConstraint().update(
                            PositionConstraintType.START_NOT_EARLIER_THAN,
                            IntraDayDate.startOfDay(startInclusive));
                }
            }
            task.setEndDate(endExclusive.toDateTimeAtStartOfDay().toDate());
            if (task.isTask()) {
                if (((Task) task).getPositionConstraint().getConstraintType()
                        .equals(PositionConstraintType.FINISH_NOT_LATER_THAN)) {
                    ((Task) task).getPositionConstraint().update(
                            PositionConstraintType.FINISH_NOT_LATER_THAN,
                            IntraDayDate.startOfDay(endExclusive));
                }
            }
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
                return Restriction.emptyRestriction();
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
    private Component insertionPointTimetracker;
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

    private boolean fixedZoomByUser = false;
    private ZoomLevel zoomLevel;

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
        if (fixedZoomByUser && (zoomLevel != null)) {
            timeTracker.setZoomLevel(zoomLevel);
        }
        paginatorFilter.setZoomLevel(timeTracker.getDetailLevel());
        paginatorFilter.setInterval(timeTracker.getRealInterval());
        paginationUpButton.setDisabled(isLastPage());
        advancedAllocationZoomLevel.setSelectedIndex(timeTracker
                .getDetailLevel().ordinal());
        timeTracker.setFilter(paginatorFilter);
        timeTracker.addZoomListener(new IZoomLevelChangedListener() {
            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                fixedZoomByUser = true;
                zoomLevel = detailLevel;

                paginatorFilter.setZoomLevel(detailLevel);
                paginatorFilter.setInterval(timeTracker.getRealInterval());
                timeTracker.setFilter(paginatorFilter);
                populateHorizontalListbox();
                Clients.evalJavaScript("ADVANCE_ALLOCATIONS.listenToScroll();");
            }
        });
        timeTrackerComponent = new TimeTrackerComponentWithoutColumns(
                timeTracker, "timetrackerheader");
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
        for (AllocationInput allocationInput : allocationInputs) {
            allocationInput.getResultReceiver().accepted(allocationInput
                    .getAggregate());
        }
        back.goBack();
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
            Messagebox.show(_("Changes applied"), _("Information"),
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
        List<Row> rowsExpenses = new ArrayList<Row>();
        List<Row> rowsAllocations = new ArrayList<Row>();
        int position = 1;
        for (AllocationInput allocationInput : allocationInputs) {
            if (allocationInput.getAggregate()
                    .getAllocationsSortedByStartDate().isEmpty()) {
            } else {
                Row groupingRow = buildRowAllocation(allocationInput);
                groupingRow.setDescription(position + " " + allocationInput.getTaskName());
                groupingRows.put(allocationInput, groupingRow);
                rowsAllocations.add(groupingRow);
                rowsCached.add(groupingRow);

                position++;

                RowExpenses rowExpenses = buildRowExpenses(allocationInput);
                rowExpenses.setDescription(position + " "
                        + allocationInput.getTaskName());
                rowsExpenses.add(rowExpenses);
                rowsCached.add(rowExpenses);
            }
        }

        rowsCached.add(this.buildTotalAllocations(rowsAllocations));
        rowsCached.add(this.buildTotalExpenses(rowsExpenses));
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
        return RowAllocation.createRow(messages, restriction,
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
        return RowAllocation.createRow(messages, restriction, Criterion
                .getCaptionFor(genericResourceAllocation.getCriterions()), 1, Arrays
                .asList(genericResourceAllocation), genericResourceAllocation
                .isLimiting(), task);
    }

    private Row buildRowAllocation(AllocationInput allocationInput) {
        String taskName = allocationInput.getTaskName();
        RowAllocation groupingRow = RowAllocation.createRow(messages,
                Restriction.emptyRestriction(), taskName, 1,
                allocationInput.getAllocationsSortedByStartDate(), false,
                allocationInput.task);
        return groupingRow;
    }

    private RowExpenses buildRowExpenses(AllocationInput allocationInput) {
        String taskName = allocationInput.getTaskName();
        RowExpenses rowExpenses = RowExpenses.createRow(messages,
                Restriction.emptyRestriction(), taskName, allocationInput.task,
                allocationInput.aggregateExpenses);
        return rowExpenses;
    }

    private Row buildTotalExpenses(List<Row> rows){
        return new RowTotalExpenses(rows);
    }

    private Row buildTotalAllocations(List<Row> rows) {
        return new RowTotalAllocation(rows);
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
        result.add(new ColumnOnRow(_("Task")) {

            @Override
            public Component cellFor(Row row) {
                if (!row.isRowExpenses()) {
                    Cell cell = new Cell();
                    cell.setAlign("center");
                    cell.setValign("center");
                    cell.setRowspan(2);
                    cell.appendChild(row.getNameLabel());
                    return cell;
                }
                return null;
            }
        });
        result.add(new ColumnOnRow(_("Total"), "150px") {
            @Override
            public Component cellFor(Row row) {
                Cell cell = new Cell();
                cell.setAlign("center");
                cell.setValign("center");
                cell.appendChild(row.getAllEffort());
                return cell;
            }
        });
        result.add(new ColumnOnRow(_("Type"), "120px") {
            @Override
            public Component cellFor(Row row) {
                Cell cell = new Cell();
                cell.setAlign("center");
                cell.setValign("center");
                cell.appendChild(row.getRowName());
                return cell;
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
    private String width = null;

    ColumnOnRow(String columnName) {
        this.columnName = columnName;
    }

    ColumnOnRow(String columnName, String width) {
        this.columnName = columnName;
        this.width = width;
    }

    public abstract Component cellFor(Row row);

    @Override
    public Column toColumn() {
        Column column = new org.zkoss.zul.Column();
        column.setLabel(_(columnName));
        column.setSclass(columnName.toLowerCase());
        if (width != null) {
            column.setWidth(width);
        }
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

class RowAllocation extends Row {

    static RowAllocation createRow(IMessagesForUser messages,
            AdvancedAllocationController.Restriction restriction, String name,
            int level, List<? extends ResourceAllocation<?>> allocations,
            String description, boolean limiting, TaskElement task) {
        AggregateOfResourceAllocations aggregate = ((allocations != null) ? AggregateOfResourceAllocations
                .createFromSatisfied(new ArrayList<ResourceAllocation<?>>(
                        allocations)) : null);
        ResourceAllocation<?> resourceAllocation = aggregate
                .getAllocationsSortedByStartDate().get(0);

        RowAllocation newRow = new RowAllocation(messages, restriction, name,
                level, allocations, resourceAllocation, aggregate, limiting,
                task);
        newRow.setDescription(description);
        return newRow;
    }

    static RowAllocation createRow(IMessagesForUser messages,
            AdvancedAllocationController.Restriction restriction, String name,
            int level, List<? extends ResourceAllocation<?>> allocations,
            boolean limiting, TaskElement task) {
        AggregateOfResourceAllocations aggregate = ((allocations != null) ? AggregateOfResourceAllocations
                .createFromSatisfied(new ArrayList<ResourceAllocation<?>>(
                        allocations)) : null);
        ResourceAllocation<?> resourceAllocation = aggregate
                .getAllocationsSortedByStartDate().get(0);

        return new RowAllocation(messages, restriction, name, level,
                allocations, resourceAllocation, aggregate, limiting, task);
    }

    protected RowAllocation(IMessagesForUser messages,
            AdvancedAllocationController.Restriction restriction, String name,
            int level, List<? extends ResourceAllocation<?>> allocations,
            ResourceAllocation<?> resourceAllocation, IAggregate aggregate,
            boolean limiting, TaskElement task) {
        setName(name);
        setTask(task);
        setRowName(new Label(TypeRow.Allocations.toString()));
        this.messages = messages;
        this.restriction = restriction;
        this.level = level;
        this.isLimiting = limiting;
        this.resourceAllocation = resourceAllocation;
        this.aggregate = aggregate;
        this.functionName = getAssignmentFunctionName(allocations);
    }

    private List<CellChangedListener> listeners = new ArrayList<CellChangedListener>();

    private int level;

    private ResourceAllocation<?> resourceAllocation;

    private final AdvancedAllocationController.Restriction restriction;

    private final IMessagesForUser messages;

    private final String functionName;

    private Label labelBudget;

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
                Component component = getComponentsByDetailItem().get(
                        detailItem);
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
        Hbox hbox = new Hbox();

        if (getAllEffortInput() == null) {
            setAllEffortInput(buildSumAllEffort());
            reloadAllEffort();
            addListenerIfNeeded(getAllEffortInput());
        }
        if (labelBudget == null) {
            BigDecimal budget = BigDecimal.ZERO;
            if (getTask() != null && getTask().getBudget() != null) {
                budget = getTask().getBudget();
            }
            labelBudget = new Label(" ( " + budget + " ) ");
        }
        hbox.appendChild(getAllEffortInput());
        hbox.appendChild(labelBudget);
        return hbox;
    }

    private void addListenerIfNeeded(Component allEffortComponent) {
        final EffortDurationBox effortDurationBox = (EffortDurationBox) allEffortComponent;
        effortDurationBox.addEventListener(Events.ON_CHANGE,
                new EventListener() {

                    @Override
                    public void onEvent(Event event) {
                        EffortDuration value = effortDurationBox
                                .getEffortDurationValue();

                        ResourceAllocation<?> resourceAllocation = getAllocation();
                        resourceAllocation
                                .withPreviousAssociatedResources()
                                .onIntervalWithinTask(
                                        resourceAllocation.getStartDate(),
                                        resourceAllocation.getEndDate())
                                .allocate(value);
                        AssignmentFunction assignmentFunction = resourceAllocation.getAssignmentFunction();
                        if (assignmentFunction != null) {
                            assignmentFunction.applyTo(resourceAllocation);
                        }

                        fireCellChanged();
                        reloadEffortsSameRowForDetailItems();
                        reloadAllEffort();
                    }
                });
    }

    private void reloadEffortsSameRowForDetailItems() {
        for (Entry<DetailItem, Component> entry : getComponentsByDetailItem()
                .entrySet()) {
            reloadEffortOnInterval(entry.getValue(), entry.getKey());
        }
    }

    private void reloadAllEffort() {
        if (getAllEffortInput() == null) {
            return;
        }
        EffortDuration allEffort = getAggregate().getTotalEffort();
        getAllEffortInput().setValue(allEffort);
        Clients.closeErrorBox(getAllEffortInput());
        if (isLimiting) {
            getAllEffortInput().setDisabled(true);
        }
        if (restriction.isInvalidTotalEffort(allEffort)) {
            restriction.showInvalidEffort(messages, allEffort);
        }
    }

    private Hbox hboxAssigmentFunctionsCombo = null;

    Component getFunction() {
        if (isGroupingRow()) {
            return new Label();
        } else if (isLimiting) {
            return new Label(_("Queue-based assignment"));
        } else {
            if (hboxAssigmentFunctionsCombo == null) {
                initializeAssigmentFunctionsCombo();
            }
            return hboxAssigmentFunctionsCombo;
        }
    }

    private AssignmentFunctionListbox assignmentFunctionsCombo = null;

    private Button assignmentFunctionsConfigureButton = null;

    private void initializeAssigmentFunctionsCombo() {
        hboxAssigmentFunctionsCombo = new Hbox();
        assignmentFunctionsCombo = new AssignmentFunctionListbox(
                functions, getAllocation().getAssignmentFunction());
        hboxAssigmentFunctionsCombo.appendChild(assignmentFunctionsCombo);
        assignmentFunctionsConfigureButton = getAssignmentFunctionsConfigureButton(assignmentFunctionsCombo);
        hboxAssigmentFunctionsCombo.appendChild(assignmentFunctionsConfigureButton);
    }

    /**
     * @author Diego Pino García <dpino@igalia.com>
     *
     *         Encapsulates the logic of the combobox used for selecting what
     *         type of assignment function to apply
     */
    class AssignmentFunctionListbox extends Listbox {

        private Listitem previousListitem;

        public AssignmentFunctionListbox(IAssignmentFunctionConfiguration[] functions,
                AssignmentFunction initialValue) {
            for (IAssignmentFunctionConfiguration each : functions) {
                Listitem listitem = listItem(each);
                this.appendChild(listitem);
                if (each.isTargetedTo(initialValue)) {
                    selectItemAndSavePreviousValue(listitem);
                }
            }
            this.addEventListener(Events.ON_SELECT, onSelectListbox());
            this.setMold("select");
            this.setStyle("font-size: 10px");
        }

        private void selectItemAndSavePreviousValue(Listitem listitem) {
            setSelectedItem(listitem);
            previousListitem = listitem;
        }

        private Listitem listItem(
                IAssignmentFunctionConfiguration assignmentFunction) {
            Listitem listitem = new Listitem(_(assignmentFunction.getName()));
            listitem.setValue(assignmentFunction);
            return listitem;
        }

        private EventListener onSelectListbox() {
            return new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    IAssignmentFunctionConfiguration function = (IAssignmentFunctionConfiguration) getSelectedItem()
                            .getValue();

                    // Cannot apply function if task contains consolidated day assignments
                    final ResourceAllocation<?> resourceAllocation = getAllocation();
                    if (function.isSigmoid()
                            && !resourceAllocation
                                    .getConsolidatedAssignments().isEmpty()) {
                        showCannotApplySigmoidFunction();
                        setSelectedItem(getPreviousListitem());
                        return;
                    }
                    // User didn't accept
                    if (showConfirmChangeFunctionDialog() != Messagebox.YES) {
                        setSelectedItem(getPreviousListitem());
                        return;
                    }
                    // Apply assignment function
                    if (function != null) {
                        setPreviousListitem(getSelectedItem());
                        function.applyOn(resourceAllocation);
                        updateAssignmentFunctionsConfigureButton(
                                assignmentFunctionsConfigureButton,
                                function.isConfigurable());
                    }
                }
            };
        }

        private Listitem getPreviousListitem() {
            return previousListitem;
        }

        private void setPreviousListitem(Listitem previousListitem) {
            this.previousListitem = previousListitem;
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
                    .show(_("Assignment function will be changed. Are you sure?"),
                            _("Confirm change"),
                            Messagebox.YES | Messagebox.NO, Messagebox.QUESTION);
        }

        private void setSelectedFunction(String functionName) {
            List<Listitem> children = getChildren();
            for (Listitem item : children) {
                IAssignmentFunctionConfiguration function = (IAssignmentFunctionConfiguration) item
                        .getValue();
                if (function.getName().equals(functionName)) {
                    setSelectedItem(item);
                }
            }
        }

    }

    private IAssignmentFunctionConfiguration flat = new IAssignmentFunctionConfiguration() {

        @Override
        public void goToConfigure() {
            throw new UnsupportedOperationException(
                    "Flat allocation is not configurable");
        }

        @Override
        public String getName() {
            return AssignmentFunctionName.FLAT.toString();
        }

        @Override
        public boolean isTargetedTo(AssignmentFunction function) {
            return function == null;
        }

        @Override
        public void applyOn(
                ResourceAllocation<?> resourceAllocation) {
            resourceAllocation.setAssignmentFunctionWithoutApply(null);
            resourceAllocation
                    .withPreviousAssociatedResources()
                    .onIntervalWithinTask(resourceAllocation.getStartDate(),
                            resourceAllocation.getEndDate())
                    .allocate(getAllEffortInput().getEffortDurationValue());
            reloadEfforts();
        }

        private void reloadEfforts() {
            reloadEffortsSameRowForDetailItems();
            reloadAllEffort();
            fireCellChanged();
        }

        @Override
        public boolean isSigmoid() {
            return false;
        }

        @Override
        public boolean isConfigurable() {
            return false;
        }

    };

    private IAssignmentFunctionConfiguration manualFunction = new IAssignmentFunctionConfiguration() {

        @Override
        public void goToConfigure() {
            throw new UnsupportedOperationException(
                    "Manual allocation is not configurable");
        }

        @Override
        public String getName() {
            return AssignmentFunctionName.MANUAL.toString();
        }

        @Override
        public boolean isTargetedTo(AssignmentFunction function) {
            return function instanceof ManualFunction;
        }

        @Override
        public void applyOn(ResourceAllocation<?> resourceAllocation) {
            resourceAllocation.setAssignmentFunctionAndApplyIfNotFlat(ManualFunction.create());
        }

        @Override
        public boolean isSigmoid() {
            return false;
        }

        @Override
        public boolean isConfigurable() {
            return false;
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
            return RowAllocation.this.getAllocation();
        }

        @Override
        protected Component getParentOnWhichOpenWindow() {
            return getAllEffortInput().getParent();
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
            return AssignmentFunctionName.STRETCHES.toString();
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
            return AssignmentFunctionName.INTERPOLATION.toString();
        }
    };

    private IAssignmentFunctionConfiguration sigmoidFunction = new IAssignmentFunctionConfiguration() {

        @Override
        public void goToConfigure() {
            throw new UnsupportedOperationException(
                    "Sigmoid function is not configurable");
        }

        @Override
        public String getName() {
            return AssignmentFunctionName.SIGMOID.toString();
        }

        @Override
        public boolean isTargetedTo(AssignmentFunction function) {
            return function instanceof SigmoidFunction;
        }

        @Override
        public void applyOn(
                ResourceAllocation<?> resourceAllocation) {
            resourceAllocation.setAssignmentFunctionAndApplyIfNotFlat(SigmoidFunction.create());
            reloadEfforts();
        }

        private void reloadEfforts() {
            reloadEffortsSameRowForDetailItems();
            reloadAllEffort();
            fireCellChanged();
        }

        @Override
        public boolean isSigmoid() {
            return true;
        }

        @Override
        public boolean isConfigurable() {
            return false;
        }

    };

    private IAssignmentFunctionConfiguration[] functions = {
            flat,
            manualFunction,
            defaultStrechesFunction,
            strechesWithInterpolation,
            sigmoidFunction
    };

    private boolean isLimiting;

    private Button getAssignmentFunctionsConfigureButton(
            final Listbox assignmentFunctionsListbox) {
        Button button = Util.createEditButton(new EventListener() {
            @Override
            public void onEvent(Event event) {
                IAssignmentFunctionConfiguration configuration = (IAssignmentFunctionConfiguration) assignmentFunctionsListbox
                        .getSelectedItem().getValue();
                configuration.goToConfigure();
            }
        });

        IAssignmentFunctionConfiguration configuration = (IAssignmentFunctionConfiguration) assignmentFunctionsListbox
                .getSelectedItem().getValue();
        updateAssignmentFunctionsConfigureButton(button,
                configuration.isConfigurable());
        return button;
    }

    private void updateAssignmentFunctionsConfigureButton(Button button,
            boolean configurable) {
        if (configurable) {
            button.setTooltiptext(_("Configure"));
            button.setDisabled(false);
        } else {
            button.setTooltiptext(_("Not configurable"));
            button.setDisabled(true);
        }
    }

    private String getAssignmentFunctionName(
            List<? extends ResourceAllocation<?>> allocations) {
        AssignmentFunction function = getAssignmentFunction(allocations);
        return (function != null) ? function.getName()
                : AssignmentFunctionName.FLAT.toString();
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
        EffortDuration duration = this.getAggregate().effortBetween(
                startDate.toLocalDate(), endDate
                .toLocalDate());
        return duration;
    }

    Component effortOnInterval(DetailItem item) {
        Component result = cannotBeEdited(item) ? new Label()
                : disableIfNeeded(item, new EffortDurationBox());
        reloadEffortOnInterval(result, item);
        getComponentsByDetailItem().put(item, result);
        addListenerIfNeeded(item, result);
        return result;
    }

    private boolean cannotBeEdited(DetailItem item) {
        return false;
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
                        .onIntervalUpdatingTask(startDate, endDate)
                                   .allocate(value);
                fireCellChanged(item);
                effortBox.setRawValue(getEffortForDetailItem(item));
                reloadAllEffort();
            }
        });
    }

    private void changeAssignmentFunctionToManual() {
        assignmentFunctionsCombo
                .setSelectedFunction(AssignmentFunctionName.MANUAL.toString());
        ResourceAllocation<?> allocation = getAllocation();
        if (!(allocation.getAssignmentFunction() instanceof ManualFunction)) {
            allocation.setAssignmentFunctionAndApplyIfNotFlat(ManualFunction.create());
        }
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
        return getTask().getIntraDayStartDate().compareTo(
                item.getEndDate().toLocalDate()) >= 0;
    }

    private boolean isAfterTaskEndDate(DetailItem item) {
        return getTask().getIntraDayEndDate().compareTo(
                item.getStartDate().toLocalDate()) <= 0;
    }

    private boolean isBeforeLatestConsolidation(DetailItem item) {
        if (!((Task) getTask()).hasConsolidations()) {
            return false;
        }
        LocalDate d = ((Task) getTask()).getFirstDayNotConsolidated().getDate();
        DateTime firstDayNotConsolidated = new DateTime(d.getYear(),
                d.getMonthOfYear(), d.getDayOfMonth(), 0, 0, 0, 0);
        return item.getStartDate().compareTo(firstDayNotConsolidated) < 0;
    }

    private ResourceAllocation<?> getAllocation() {
        return this.resourceAllocation;
    }

    boolean isRowExpenses() {
        return false;
    }
}

enum TypeRow {
    Expenses(_("Expenses")), Allocations(_("Allocations"));

    private static String _(String string) {
        return string;
    }

    private String option;

    private TypeRow(String option) {
        this.option = option;
    }

    public String toString() {
        return I18nHelper._(option);
    }

}

abstract class Row {
    abstract Component getAllEffort();

    abstract Component effortOnInterval(DetailItem item);

    abstract boolean isRowExpenses();

    abstract void listenTo(Collection<Row> rows);

    abstract void listenTo(Row row);

    abstract void add(CellChangedListener listener);

    private EffortDurationBox allEffortInput;

    private Label nameLabel;

    protected Label rowName;

    protected IAggregate aggregate;

    private Map<DetailItem, Component> componentsByDetailItem = new WeakHashMap<DetailItem, Component>();

    private String name;

    private String description;

    private TaskElement task;

    Component getRowName() {
        return rowName;
    }

    protected void setRowName(Label rowName) {
        this.rowName = rowName;
    }

    Component getNameLabel() {
        if (this.nameLabel == null) {
            nameLabel = new Label();
            nameLabel.setValue(name);
            if (!StringUtils.isBlank(description)) {
                nameLabel.setTooltiptext(description);
            } else {
                nameLabel.setTooltiptext(name);
            }
        }
        return nameLabel;
    }

    protected EffortDurationBox buildSumAllEffort() {
        EffortDurationBox box = new EffortDurationBox();
        box.setWidth("40px");
        return box;
    }

    public void markErrorOnTotal(String message) {
        throw new WrongValueException(getAllEffortInput(), message);
    }

    public boolean isGroupingRow() {
        return false;
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

    protected Map<DetailItem, Component> getComponentsByDetailItem() {
        return componentsByDetailItem;
    }

    public void setAllEffortInput(EffortDurationBox allEffortInput) {
        this.allEffortInput = allEffortInput;
    }

    public EffortDurationBox getAllEffortInput() {
        return allEffortInput;
    }

    public void setNameLabel(Label nameLabel) {
        this.nameLabel = nameLabel;
    }

    public void setTask(TaskElement task) {
        this.task = task;
    }

    public TaskElement getTask() {
        return task;
    }

    protected IAggregate getAggregate() {
        return aggregate;
    }

}

class RowExpenses extends Row {

    protected RowExpenses(IMessagesForUser messages, Restriction restriction,
            String name, TaskElement task,
            AggregateOfExpensesLines aggregateExpenses) {
        setName(name);
        setTask(task);
        setRowName(new Label(TypeRow.Expenses.toString()));
        this.aggregate = aggregateExpenses;
    }

    public static RowExpenses createRow(IMessagesForUser messages,
            Restriction restriction, String taskName, TaskElement task,
            AggregateOfExpensesLines aggregateExpenses) {
        return new RowExpenses(messages, restriction, taskName, task,
                aggregateExpenses);
    }

    @Override
    Component getAllEffort() {
        if (getAllEffortInput() == null) {
            setAllEffortInput(buildSumAllEffort());
            getAllEffortInput().setWidth("90px");
            getAllEffortInput().setReadonly(true);
            reloadAllEffort();
        }
        return getAllEffortInput();
    }

    @Override
    Component effortOnInterval(DetailItem item) {
        Label result = new Label();
        reloadEffortOnInterval(result, item);
        getComponentsByDetailItem().put(item, result);
        return result;
    }

    @Override
    boolean isRowExpenses() {
        return true;
    }

    private void reloadAllEffort() {
        if (getAllEffortInput() == null) {
            return;
        }
        EffortDuration allEffort = aggregate.getTotalEffort();
        getAllEffortInput().setValue(allEffort);
    }

    private void reloadEffortOnInterval(Component component, DetailItem item) {
        Label label = (Label) component;
        label.setValue(getEffortForDetailItem(item).toFormattedString());
        label.setClass("unmodifiable-hours");
    }

    private EffortDuration getEffortForDetailItem(DetailItem item) {
        DateTime startDate = item.getStartDate();
        DateTime endDate = item.getEndDate();
        return aggregate.effortBetween(startDate.toLocalDate(),
                endDate.toLocalDate());
    }

    @Override
    void listenTo(Collection<Row> rows) {
    }

    @Override
    void listenTo(Row row) {
    }

    @Override
    void add(CellChangedListener listener) {
    }

}

abstract class RowTotals extends Row {

    protected TypeRow typeRow;
    protected List<Row> rows;

    @Override
    abstract Component getAllEffort();

    @Override
    Component effortOnInterval(DetailItem item) {
        Label result = new Label();
        reloadEffortOnInterval(result, item);
        getComponentsByDetailItem().put(item, result);
        return result;
    }

    protected void reloadAllEffort() {
        if (getAllEffortInput() == null) {
            return;
        }
        EffortDuration allEffort = calculateTotal();
        getAllEffortInput().setValue(allEffort);
    }

    private EffortDuration calculateTotal() {
        EffortDuration total = EffortDuration.zero();
        for (Row row : rows) {
            total = total.plus(row.getAggregate().getTotalEffort());
        }
        return total;
    }

    private EffortDuration calculateTotalBetween(LocalDate start, LocalDate end) {
        EffortDuration total = EffortDuration.zero();
        for(Row row : rows){
            total = total.plus(row.getAggregate().effortBetween(start, end));
        }
        return total;
    }

    protected void reloadEffortOnInterval(Component component, DetailItem item) {
        Label label = (Label) component;
        label.setValue(getEffortForDetailItem(item).toFormattedString());
        label.setClass("unmodifiable-hours");
    }

    private EffortDuration getEffortForDetailItem(DetailItem item) {
        LocalDate startDate = item.getStartDate().toLocalDate();
        LocalDate endDate = item.getEndDate().toLocalDate();
        return calculateTotalBetween(startDate, endDate);
    }

    @Override
    void listenTo(Collection<Row> rows) {
    }

    @Override
    void listenTo(Row row) {
    }

    @Override
    void add(CellChangedListener listener) {
    }

    public TypeRow getTypeRow() {
        return typeRow;
    }
}

class RowTotalExpenses extends RowTotals {

    public RowTotalExpenses(List<Row> rows) {
        super.typeRow = TypeRow.Expenses;
        super.rows = rows;
        setName(_("Total"));
        setRowName(new Label(typeRow.toString()));
    }

    public Component getAllEffort() {
        if (getAllEffortInput() == null) {
            setAllEffortInput(buildSumAllEffort());
            getAllEffortInput().setWidth("90px");
            getAllEffortInput().setReadonly(true);
            reloadAllEffort();
        }
        return getAllEffortInput();
    }

    @Override
    public boolean isRowExpenses() {
        return true;
    }
}

class RowTotalAllocation extends RowTotals {

    private Label labelBudget;
    private List<CellChangedListener> listeners = new ArrayList<CellChangedListener>();

    public RowTotalAllocation(List<Row> rows) {
        super.typeRow = TypeRow.Allocations;
        super.rows = rows;
        setName(_("Total"));
        setRowName(new Label(typeRow.toString()));
        listenTo(rows);
    }

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
                Component component = getComponentsByDetailItem().get(
                        detailItem);
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


    public Component getAllEffort() {
        Hbox hbox = new Hbox();
        if (getAllEffortInput() == null) {
            setAllEffortInput(buildSumAllEffort());
            getAllEffortInput().setReadonly(true);
            reloadAllEffort();
        }
        if (labelBudget == null) {
            labelBudget = new Label(" ( " + calculateTotalBudget() + " ) ");
        }
        hbox.appendChild(getAllEffortInput());
        hbox.appendChild(labelBudget);
        return hbox;
    }

    private BigDecimal calculateTotalBudget() {
        BigDecimal budget = BigDecimal.ZERO;
        for (Row row : rows) {
            if (row.getTask() != null && row.getTask().getBudget() != null) {
                budget = budget.add(row.getTask().getBudget());
            }
        }
        return budget;
    }
    @Override
    public boolean isRowExpenses() {
        return false;
    }

    private void reloadEffortsSameRowForDetailItems() {
        for (Entry<DetailItem, Component> entry : getComponentsByDetailItem()
                .entrySet()) {
            reloadEffortOnInterval(entry.getValue(), entry.getKey());
        }
    }
}
