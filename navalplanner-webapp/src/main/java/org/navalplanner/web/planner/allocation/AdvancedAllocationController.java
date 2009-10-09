/*
 * This file is part of ###PROJECT_NAME###
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
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.web.common.ViewSwitcher;
import org.navalplanner.web.resourceload.ResourceLoadModel;
import org.zkoss.ganttz.timetracker.ICellForDetailItemRenderer;
import org.zkoss.ganttz.timetracker.IConvertibleToColumn;
import org.zkoss.ganttz.timetracker.PairOfLists;
import org.zkoss.ganttz.timetracker.TimeTrackedTable;
import org.zkoss.ganttz.timetracker.TimeTrackedTableWithLeftPane;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.TimeTrackerComponentWithoutColumns;
import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.api.Column;

public class AdvancedAllocationController extends GenericForwardComposer {

    public interface IAdvanceAllocationResultReceiver {
        public Restriction getRestriction();

        public void accepted(AllocationResult modifiedAllocationResult);

        public void cancel();
    }

    public static class Restriction {
        public static Restriction onlyAssignOnInterval(LocalDate start,
                LocalDate end){
            return new OnlyOnIntervalRestriction(start, end);
        }

        public static Restriction fixedHours(int hours) {
            return new FixedHoursRestriction(hours);
        }
    }

    private static class OnlyOnIntervalRestriction extends Restriction {
        private final LocalDate start;

        private final LocalDate end;

        private OnlyOnIntervalRestriction(LocalDate start, LocalDate end) {
            super();
            this.start = start;
            this.end = end;
        }

        public LocalDate getStart() {
            return start;
        }

        public LocalDate getEnd() {
            return end;
        }
    }

    private static class FixedHoursRestriction extends Restriction {
        private final int hours;

        private FixedHoursRestriction(int hours) {
            this.hours = hours;
        }

        public int getHours() {
            return hours;
        }
    }

    private Div insertionPointTimetracker;
    private Div insertionPointLeftPanel;
    private Div insertionPointRightPanel;

    private TimeTracker timeTracker;

    private TimeTrackerComponentWithoutColumns timeTrackerComponent;
    private Grid leftPane;
    private TimeTrackedTable<Row> table;
    private final ViewSwitcher switcher;
    private final AllocationResult allocationResult;
    private final IAdvanceAllocationResultReceiver resultReceiver;

    public AdvancedAllocationController(ViewSwitcher switcher,
            AllocationResult allocationResult,
            IAdvanceAllocationResultReceiver resultReceiver) {
        this.switcher = switcher;
        this.allocationResult = allocationResult;
        this.resultReceiver = resultReceiver;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        createComponents();
        insertComponentsInLayout();
        timeTrackerComponent.afterCompose();
        table.afterCompose();
    }

    private void createComponents() {
        timeTracker = new TimeTracker(intervalFromData());
        timeTrackerComponent = new TimeTrackerComponentWithoutColumns(
                timeTracker, "timeTracker");
        TimeTrackedTableWithLeftPane<Row, Row> timeTrackedTableWithLeftPane = new TimeTrackedTableWithLeftPane<Row, Row>(
                getDataSource(), getColumnsForLeft(), getLeftRenderer(),
                getRightRenderer(), timeTracker);

        table = timeTrackedTableWithLeftPane.getRightPane();
        leftPane = timeTrackedTableWithLeftPane.getLeftPane();
    }

    private void insertComponentsInLayout() {
        insertionPointRightPanel.appendChild(table);
        insertionPointLeftPanel.appendChild(leftPane);
        insertionPointTimetracker.appendChild(timeTrackerComponent);
    }

    public void onClick$acceptButton() {
        switcher.goToPlanningOrderView();
        resultReceiver.accepted(allocationResult);
    }

    public void onClick$cancelButton() {
        switcher.goToPlanningOrderView();
        resultReceiver.cancel();
    }

    public void onClick$zoomIncrease() {
        timeTracker.zoomIncrease();
    }

    public void onClick$zoomDecrease() {
        timeTracker.zoomDecrease();
    }

    private List<Row> rowsCached = null;

    private List<Row> getRows() {
        if (rowsCached != null) {
            return rowsCached;
        }
        rowsCached = new ArrayList<Row>();
        Row groupingRow = buildGroupingRow();
        rowsCached.add(groupingRow);
        List<Row> genericRows = genericRows();
        groupingRow.listenTo(genericRows);
        rowsCached.addAll(genericRows);
        List<Row> specificRows = specificRows();
        groupingRow.listenTo(specificRows);
        rowsCached.addAll(specificRows);
        return rowsCached;
    }

    private List<Row> specificRows() {
        List<Row> result = new ArrayList<Row>();
        for (SpecificResourceAllocation specificResourceAllocation : allocationResult
                .getSpecificAllocations()) {
            result.add(createSpecificRow(specificResourceAllocation));
        }
        return result;
    }

    private Row createSpecificRow(
            SpecificResourceAllocation specificResourceAllocation) {
        return Row.createRow(specificResourceAllocation.getResource()
                        .getDescription(), 1, Arrays
                        .asList(specificResourceAllocation));
    }

    private List<Row> genericRows() {
        List<Row> result = new ArrayList<Row>();
        for (GenericResourceAllocation genericResourceAllocation : allocationResult
                .getGenericAllocations()) {
            result.add(buildGenericRow(genericResourceAllocation));
        }
        return result;
    }

    private Row buildGenericRow(
            GenericResourceAllocation genericResourceAllocation) {
        return Row.createRow(ResourceLoadModel
                .getName(genericResourceAllocation.getCriterions()), 1, Arrays
                .asList(genericResourceAllocation));
    }

    private Row buildGroupingRow() {
        String taskName = allocationResult.getTask().getName();
        Row groupingRow = Row.createRow(taskName + " (task)", 0,
                allocationResult
                .getAllSortedByStartDate());
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
        List<ResourceAllocation<?>> all = allocationResult
                .getAllSortedByStartDate();
        if (all.isEmpty()) {
            return new Interval(allocationResult.getTask().getStartDate(),
                    allocationResult.getTask().getEndDate());
        } else {
            LocalDate start = all.get(0).getStartDate();
            LocalDate end = getEnd(all);
            return new Interval(asDate(start), asDate(end));
        }
    }

    private LocalDate getEnd(List<ResourceAllocation<?>> all) {
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

    private ArrayList<ResourceAllocation<?>> reverse(
            List<ResourceAllocation<?>> all) {
        ArrayList<ResourceAllocation<?>> reversed = new ArrayList<ResourceAllocation<?>>(
                all);
        Collections.reverse(reversed);
        return reversed;
    }

    private Date asDate(LocalDate start) {
        return start.toDateMidnight().toDate();
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

    static Row createRow(String name, int level,
            List<? extends ResourceAllocation<?>> allocations) {
        return new Row(name, level, allocations);
    }

    private Component allHoursInput;

    private Label nameLabel;

    private List<CellChangedListener> listeners = new ArrayList<CellChangedListener>();

    private Map<DetailItem, Component> componentsByDetailItem = new WeakHashMap<DetailItem, Component>();

    private String name;

    private int level;

    private final AggregateOfResourceAllocations aggregate;

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
        Intbox intbox = (Intbox) allHoursComponent;
        intbox.addEventListener(Events.ON_CHANGE, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                fireCellChanged();
            }
        });
    }

    private void reloadAllHours() {
        if (isGroupingRow()) {
            Label label = (Label) allHoursInput;
            label.setValue(aggregate.getTotalHours() + "");
        } else {
            Intbox intbox = (Intbox) allHoursInput;
            intbox.setValue(aggregate.getTotalHours());
        }
    }

    Component getFunction() {
        if (isGroupingRow()) {
            return new Label();
        } else {
            Combobox combobox = new Combobox();
            return combobox;
        }
    }

    Component getNameLabel() {
        if (nameLabel == null) {
            nameLabel = new Label();
            nameLabel.setValue(name);
        }
        return nameLabel;
    }

    private Row(String name, int level,
            List<? extends ResourceAllocation<?>> allocations) {
        this.name = name;
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
        Component result = isGroupingRow() ? new Label() : noNegativeIntbox();
        reloadHoursOnInterval(result, item);
        componentsByDetailItem.put(item, result);
        addListenerIfNeeded(item, result);
        return result;
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
                getAllocation().withPreviousAssociatedResources().onInterval(
                        item.getStartDate().toLocalDate(),
                        item.getEndDate().toLocalDate()).allocateHours(value);
                fireCellChanged(item);
                reloadAllHours();
            }
        });
    }

    private void reloadHoursOnInterval(Component component, DetailItem item) {
        if (isGroupingRow()) {
            Label label = (Label) component;
            label.setValue(getHoursForDetailItem(item) + "");
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
        return aggregate.getAllocationsSortedByStartDate().size() > 1;
    }

}
