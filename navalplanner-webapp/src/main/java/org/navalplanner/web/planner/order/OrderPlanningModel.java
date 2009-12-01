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

package org.navalplanner.web.planner.order;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.SameWorkHoursEveryDay;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.ICostCalculator;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskMilestone;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.workreports.daos.IWorkReportLineDAO;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.web.common.ViewSwitcher;
import org.navalplanner.web.planner.ITaskElementAdapter;
import org.navalplanner.web.planner.ITaskElementAdapter.IOnMoveListener;
import org.navalplanner.web.planner.allocation.IResourceAllocationCommand;
import org.navalplanner.web.planner.allocation.ResourceAllocationController;
import org.navalplanner.web.planner.calendar.CalendarAllocationController;
import org.navalplanner.web.planner.calendar.ICalendarAllocationCommand;
import org.navalplanner.web.planner.chart.Chart;
import org.navalplanner.web.planner.chart.ChartFiller;
import org.navalplanner.web.planner.chart.EarnedValueChartFiller;
import org.navalplanner.web.planner.chart.IChartFiller;
import org.navalplanner.web.planner.milestone.IAddMilestoneCommand;
import org.navalplanner.web.planner.order.ISaveCommand.IAfterSaveListener;
import org.navalplanner.web.planner.taskedition.EditTaskController;
import org.navalplanner.web.planner.taskedition.IEditTaskCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkforge.timeplot.Plotinfo;
import org.zkforge.timeplot.Timeplot;
import org.zkforge.timeplot.geometry.TimeGeometry;
import org.zkforge.timeplot.geometry.ValueGeometry;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public abstract class OrderPlanningModel implements IOrderPlanningModel {

    @Autowired
    private IOrderDAO orderDAO;

    private PlanningState planningState;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IWorkReportLineDAO workReportLineDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    private List<IZoomLevelChangedListener> keepAliveZoomListeners = new ArrayList<IZoomLevelChangedListener>();

    private ITaskElementAdapter taskElementAdapter;

    @Autowired
    private ICostCalculator hoursCostCalculator;

    private final class TaskElementNavigator implements
            IStructureNavigator<TaskElement> {
        @Override
        public List<TaskElement> getChildren(TaskElement object) {
            return object.getChildren();
        }

        @Override
        public boolean isLeaf(TaskElement object) {
            return object.isLeaf();
        }

        @Override
        public boolean isMilestone(TaskElement object) {
            if (object != null) {
                return object instanceof TaskMilestone;
            }
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void setConfigurationToPlanner(Planner planner, Order order,
            ViewSwitcher switcher,
            ResourceAllocationController resourceAllocationController,
            EditTaskController editTaskController,
            CalendarAllocationController calendarAllocationController,
            List<ICommand<TaskElement>> additional) {
        Order orderReloaded = reload(order);
        if (!orderReloaded.isSomeTaskElementScheduled()) {
            throw new IllegalArgumentException(_(
                    "The order {0} must be scheduled", orderReloaded));
        }
        PlannerConfiguration<TaskElement> configuration = createConfiguration(orderReloaded);
        addAdditional(additional, configuration);
        ISaveCommand saveCommand = buildSaveCommand();
        configuration.addGlobalCommand(saveCommand);

        configuration.addCommandOnTask(buildResourceAllocationCommand(resourceAllocationController));
        configuration.addCommandOnTask(buildMilestoneCommand());
        configuration
                .addCommandOnTask(buildCalendarAllocationCommand(calendarAllocationController));

        configuration.setEditTaskCommand(buildEditTaskCommand(editTaskController));

        Tabbox chartComponent = new Tabbox();
        chartComponent.setOrient("vertical");
        appendTabs(chartComponent);

        Timeplot chartLoadTimeplot = new Timeplot();
        Timeplot chartEarnedValueTimeplot = new Timeplot();
        appendTabpanels(chartComponent, chartLoadTimeplot,
                chartEarnedValueTimeplot);

        configuration.setChartComponent(chartComponent);
        planner.setConfiguration(configuration);

        Chart loadChart = setupChart(orderReloaded,
                new OrderLoadChartFiller(orderReloaded), chartLoadTimeplot,
                planner.getTimeTracker());
        refillLoadChartWhenNeeded(planner, saveCommand, loadChart);
        Chart earnedValueChart = setupChart(orderReloaded,
                new CompanyEarnedValueChartFiller(orderReloaded),
                chartEarnedValueTimeplot, planner.getTimeTracker());
        refillLoadChartWhenNeeded(planner, saveCommand, earnedValueChart);
    }

    private void appendTabs(Tabbox chartComponent) {
        Tabs chartTabs = new Tabs();
        chartTabs.appendChild(new Tab(_("Load")));
        chartTabs.appendChild(new Tab(_("Earned value")));

        chartComponent.appendChild(chartTabs);
        chartTabs.setWidth("100px");
    }

    private void appendTabpanels(Tabbox chartComponent, Timeplot loadChart,
            Timeplot chartEarnedValueTimeplot) {
        Tabpanels chartTabpanels = new Tabpanels();

        Tabpanel loadChartPannel = new Tabpanel();
        appendLoadChartAndLegend(loadChartPannel, loadChart);
        chartTabpanels.appendChild(loadChartPannel);

        Tabpanel earnedValueChartPannel = new Tabpanel();
        appendEarnedValueChartAndLegend(earnedValueChartPannel,
                chartEarnedValueTimeplot);
        chartTabpanels.appendChild(earnedValueChartPannel);

        chartComponent.appendChild(chartTabpanels);
    }

    private void appendLoadChartAndLegend(Tabpanel loadChartPannel,
            Timeplot loadChart) {
        Hbox hbox = new Hbox();
        hbox.appendChild(getLoadChartLegend());

        Div div = new Div();
        div.appendChild(loadChart);
        div.setSclass("plannergraph");
        hbox.appendChild(div);

        loadChartPannel.appendChild(hbox);
    }

    private org.zkoss.zk.ui.Component getLoadChartLegend() {
        Div div = new Div();
        Executions.createComponents("/planner/_legendLoadChartOrder.zul", div,
                null);
        return div;
    }

    private void appendEarnedValueChartAndLegend(
            Tabpanel earnedValueChartPannel, Timeplot chartEarnedValueTimeplot) {
        Hbox hbox = new Hbox();
        hbox.appendChild(getEarnedValueChartLegend());

        Div div = new Div();
        div.appendChild(chartEarnedValueTimeplot);
        div.setSclass("plannergraph");
        hbox.appendChild(div);

        earnedValueChartPannel.appendChild(hbox);
    }

    private org.zkoss.zk.ui.Component getEarnedValueChartLegend() {
        Div div = new Div();
        Executions.createComponents("/planner/_legendEarnedValueChart.zul",
                div, null);
        return div;
    }

    private void refillLoadChartWhenNeeded(Planner planner,
            ISaveCommand saveCommand, final Chart loadChart) {
        planner.getTimeTracker().addZoomListener(fillOnZoomChange(loadChart));
        saveCommand.addListener(fillChartOnSave(loadChart));
        taskElementAdapter.addListener(new IOnMoveListener() {
            @Override
            public void moved(TaskElement taskElement) {
                loadChart.fillChart();
            }
        });
    }

    private void addAdditional(List<ICommand<TaskElement>> additional,
            PlannerConfiguration<TaskElement> configuration) {
        for (ICommand<TaskElement> c : additional) {
            configuration.addGlobalCommand(c);
        }
    }

    private ICalendarAllocationCommand buildCalendarAllocationCommand(
            CalendarAllocationController calendarAllocationController) {
        ICalendarAllocationCommand calendarAllocationCommand = getCalendarAllocationCommand();
        calendarAllocationCommand
                .setCalendarAllocationController(calendarAllocationController);
        return calendarAllocationCommand;
    }

    private IEditTaskCommand buildEditTaskCommand(
            EditTaskController editTaskController) {
        IEditTaskCommand editTaskCommand = getEditTaskCommand();
        editTaskCommand.setEditTaskController(editTaskController);
        return editTaskCommand;
    }

    private IAddMilestoneCommand buildMilestoneCommand() {
        IAddMilestoneCommand addMilestoneCommand = getAddMilestoneCommand();
        addMilestoneCommand.setState(planningState);
        return addMilestoneCommand;
    }

    private IResourceAllocationCommand buildResourceAllocationCommand(
            ResourceAllocationController resourceAllocationController) {
        IResourceAllocationCommand resourceAllocationCommand = getResourceAllocationCommand();
        resourceAllocationCommand.initialize(resourceAllocationController,
                planningState);
        return resourceAllocationCommand;
    }

    private ISaveCommand buildSaveCommand() {
        ISaveCommand saveCommand = getSaveCommand();
        saveCommand.setState(planningState);
        return saveCommand;
    }

    private Chart setupChart(Order orderReloaded,
            IChartFiller loadChartFiller, Timeplot chartComponent,
            TimeTracker timeTracker) {
        Chart result = new Chart(chartComponent, loadChartFiller,
                timeTracker);
        result.fillChart();
        return result;
    }

    private IZoomLevelChangedListener fillOnZoomChange(final Chart loadChart) {
        IZoomLevelChangedListener zoomListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                transactionService
                        .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
                            @Override
                            public Void execute() {
                                loadChart.fillChart();
                                return null;
                            }
                        });
            }
        };

        keepAliveZoomListeners.add(zoomListener);

        return zoomListener;
    }

    private IAfterSaveListener fillChartOnSave(final Chart loadChart) {
        IAfterSaveListener result = new IAfterSaveListener() {

                    @Override
            public void onAfterSave() {
                    transactionService
                        .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
                            @Override
                            public Void execute() {
                                loadChart.fillChart();
                                return null;
                            }
                        });
            }
        };
        return result;
    }

    private PlannerConfiguration<TaskElement> createConfiguration(
            Order orderReloaded) {
        taskElementAdapter = getTaskElementAdapter();
        taskElementAdapter.setOrder(orderReloaded);
        planningState = new PlanningState(orderReloaded
                .getAssociatedTaskElement(),
                orderReloaded.getAssociatedTasks(),
                resourceDAO.list(Resource.class));

        forceLoadOfDependenciesCollections(planningState.getInitial());
        forceLoadOfWorkingHours(planningState.getInitial());
        forceLoadOfLabels(planningState.getInitial());
        PlannerConfiguration<TaskElement> result = new PlannerConfiguration<TaskElement>(
                taskElementAdapter,
                new TaskElementNavigator(), planningState.getInitial());
        result.setNotBeforeThan(orderReloaded.getInitDate());
        result.setDependenciesConstraintsHavePriority(orderReloaded
                .getDependenciesConstraintsHavePriority());
        return result;
    }

    private void forceLoadOfWorkingHours(List<TaskElement> initial) {
        for (TaskElement taskElement : initial) {
            OrderElement orderElement = taskElement.getOrderElement();
            if (orderElement != null) {
                orderElement.getWorkHours();
            }
            if (!taskElement.isLeaf()) {
                forceLoadOfWorkingHours(taskElement.getChildren());
            }
        }
    }

    private void forceLoadOfDependenciesCollections(
            Collection<? extends TaskElement> elements) {
        for (TaskElement task : elements) {
            forceLoadOfDepedenciesCollections(task);
            if (!task.isLeaf()) {
                forceLoadOfDependenciesCollections(task.getChildren());
            }
        }
    }

    private void forceLoadOfDepedenciesCollections(TaskElement task) {
        task.getDependenciesWithThisOrigin().size();
        task.getDependenciesWithThisDestination().size();
    }

    private void forceLoadOfLabels(List<TaskElement> initial) {
        for (TaskElement taskElement : initial) {
            if (taskElement.isLeaf()) {
                OrderElement orderElement = taskElement.getOrderElement();
                if (orderElement != null) {
                    orderElement.getLabels().size();
                }
            } else {
                forceLoadOfLabels(taskElement.getChildren());
            }
        }
    }

    // spring method injection
    protected abstract ITaskElementAdapter getTaskElementAdapter();

    protected abstract ISaveCommand getSaveCommand();

    protected abstract IResourceAllocationCommand getResourceAllocationCommand();

    protected abstract IAddMilestoneCommand getAddMilestoneCommand();

    protected abstract IEditTaskCommand getEditTaskCommand();

    protected abstract ICalendarAllocationCommand getCalendarAllocationCommand();

    private Order reload(Order order) {
        try {
            return orderDAO.find(order.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private class OrderLoadChartFiller extends ChartFiller {

        private final Order order;

        private SortedMap<LocalDate, Integer> mapOrderLoad = new TreeMap<LocalDate, Integer>();
        private SortedMap<LocalDate, Integer> mapOrderOverload = new TreeMap<LocalDate, Integer>();
        private SortedMap<LocalDate, Integer> mapMaxCapacity = new TreeMap<LocalDate, Integer>();
        private SortedMap<LocalDate, Integer> mapOtherLoad = new TreeMap<LocalDate, Integer>();
        private SortedMap<LocalDate, Integer> mapOtherOverload = new TreeMap<LocalDate, Integer>();

        public OrderLoadChartFiller(Order orderReloaded) {
            this.order = orderReloaded;
        }

        @Override
        public void fillChart(Timeplot chart, Interval interval, Integer size) {
            chart.getChildren().clear();
            chart.invalidate();
            resetMinimumAndMaximumValueForChart();

            List<DayAssignment> orderDayAssignments = order.getDayAssignments();
            SortedMap<LocalDate, Map<Resource, Integer>> orderDayAssignmentsGrouped = groupDayAssignmentsByDayAndResource(orderDayAssignments);

            List<DayAssignment> resourcesDayAssignments = new ArrayList<DayAssignment>();
            for (Resource resource : order.getResources()) {
                resourcesDayAssignments.addAll(resource.getAssignments());
            }
            SortedMap<LocalDate, Map<Resource, Integer>> resourceDayAssignmentsGrouped = groupDayAssignmentsByDayAndResource(resourcesDayAssignments);

            fillMaps(orderDayAssignmentsGrouped, resourceDayAssignmentsGrouped);
            groupByWeekMaps();

            Plotinfo plotOrderLoad = createPlotinfo(
                    convertToBigDecimal(mapOrderLoad), interval);
            Plotinfo plotOrderOverload = createPlotinfo(
                    convertToBigDecimal(mapOrderOverload), interval);
            Plotinfo plotMaxCapacity = createPlotinfo(
                    convertToBigDecimal(mapMaxCapacity), interval);
            Plotinfo plotOtherLoad = createPlotinfo(
                    convertToBigDecimal(mapOtherLoad), interval);
            Plotinfo plotOtherOverload = createPlotinfo(
                    convertToBigDecimal(mapOtherOverload), interval);

            plotOrderLoad.setFillColor("0000FF");
            plotOrderOverload.setLineColor("00FFFF");
            plotMaxCapacity.setLineColor("FF0000");
            plotMaxCapacity.setLineWidth(2);
            plotOtherLoad.setFillColor("00FF00");
            plotOtherOverload.setLineColor("FFFF00");

            ValueGeometry valueGeometry = getValueGeometry();
            TimeGeometry timeGeometry = getTimeGeometry(interval);

            appendPlotinfo(chart, plotOrderLoad, valueGeometry, timeGeometry);
            appendPlotinfo(chart, plotOrderOverload, valueGeometry,
                    timeGeometry);
            appendPlotinfo(chart, plotMaxCapacity, valueGeometry, timeGeometry);
            appendPlotinfo(chart, plotOtherLoad, valueGeometry, timeGeometry);
            appendPlotinfo(chart, plotOtherOverload, valueGeometry,
                    timeGeometry);

            chart.setWidth(size + "px");
            chart.setHeight("100px");
        }

        private void groupByWeekMaps() {
            mapOrderLoad = groupByWeek(mapOrderLoad);
            mapOrderOverload = groupByWeek(mapOrderOverload);
            mapMaxCapacity = groupByWeek(mapMaxCapacity);
            mapOtherLoad = groupByWeek(mapOtherLoad);
            mapOtherOverload = groupByWeek(mapOtherOverload);
        }

        private void fillMaps(
                SortedMap<LocalDate, Map<Resource, Integer>> orderDayAssignmentsGrouped,
                SortedMap<LocalDate, Map<Resource, Integer>> resourceDayAssignmentsGrouped) {

            for (LocalDate day : orderDayAssignmentsGrouped.keySet()) {
                int maxCapacity = getMaxCapcity(orderDayAssignmentsGrouped, day);
                mapMaxCapacity.put(day, maxCapacity);

                Integer orderLoad = 0;
                Integer orderOverload = 0;
                Integer otherLoad = 0;
                Integer otherOverload = 0;

                for (Resource resource : orderDayAssignmentsGrouped.get(day)
                        .keySet()) {
                    int workableHours = getWorkableHours(resource, day);

                    Integer hoursOrder = orderDayAssignmentsGrouped.get(day).get(
                            resource);

                    Integer hoursOther = resourceDayAssignmentsGrouped.get(day)
                            .get(resource)
                            - hoursOrder;

                    if (hoursOrder <= workableHours) {
                        orderLoad += hoursOrder;
                        orderOverload += 0;
                    } else {
                        orderLoad += workableHours;
                        orderOverload += hoursOrder - workableHours;
                    }

                    if ((hoursOrder + hoursOther) <= workableHours) {
                        otherLoad += hoursOther;
                        otherOverload += 0;
                    } else {
                        if (hoursOrder <= workableHours) {
                            otherLoad += (workableHours - hoursOrder);
                            otherOverload += hoursOrder + hoursOther
                                    - workableHours;
                        } else {
                            otherLoad += 0;
                            otherOverload += hoursOther;
                        }
                    }
                }

                mapOrderLoad.put(day, orderLoad);
                mapOrderOverload.put(day, orderOverload + maxCapacity);
                mapOtherLoad.put(day, otherLoad + orderLoad);
                mapOtherOverload.put(day, otherOverload + orderOverload
                        + maxCapacity);
            }
        }

        private int getMaxCapcity(
                SortedMap<LocalDate, Map<Resource, Integer>> orderDayAssignmentsGrouped,
                LocalDate day) {
            int maxCapacity = 0;
            for (Resource resource : orderDayAssignmentsGrouped.get(day)
                    .keySet()) {
                maxCapacity += getWorkableHours(resource, day);
            }
            return maxCapacity;
        }

        private int getWorkableHours(Resource resource, LocalDate day) {
            BaseCalendar calendar = resource.getCalendar();

            int workableHours = SameWorkHoursEveryDay.getDefaultWorkingDay()
                    .getWorkableHours(day);
            if (calendar != null) {
                workableHours = calendar.getWorkableHours(day);
            }

            return workableHours;
        }

    }

    private class CompanyEarnedValueChartFiller extends EarnedValueChartFiller {

        private Order order;

        public CompanyEarnedValueChartFiller(Order orderReloaded) {
            this.order = orderReloaded;
        }

        @Override
        public void fillChart(Timeplot chart, Interval interval, Integer size) {
            chart.getChildren().clear();
            chart.invalidate();
            resetMinimumAndMaximumValueForChart();

            calculateValues(interval);

            Plotinfo bcws = createPlotInfo(
                    indicators.get(EarnedValueType.BCWS), interval, "0000FF");
            Plotinfo acwp = createPlotInfo(
                    indicators.get(EarnedValueType.ACWP), interval, "FF0000");
            Plotinfo bcwp = createPlotInfo(
                    indicators.get(EarnedValueType.BCWP), interval, "00FF00");
            Plotinfo cv = createPlotInfo(indicators.get(EarnedValueType.CV),
                    interval, "FFFF00");
            Plotinfo sv = createPlotInfo(indicators.get(EarnedValueType.SV),
                    interval, "00FFFF");

            ValueGeometry valueGeometry = getValueGeometry();
            TimeGeometry timeGeometry = getTimeGeometry(interval);

            appendPlotinfo(chart, bcws, valueGeometry, timeGeometry);
            appendPlotinfo(chart, acwp, valueGeometry, timeGeometry);
            appendPlotinfo(chart, bcwp, valueGeometry, timeGeometry);
            appendPlotinfo(chart, cv, valueGeometry, timeGeometry);
            appendPlotinfo(chart, sv, valueGeometry, timeGeometry);

            chart.setWidth(size + "px");
            chart.setHeight("100px");
        }

        protected void calculateBudgetedCostWorkScheduled(Interval interval) {
            List<TaskElement> list = order
                    .getAllChildrenAssociatedTaskElements();
            list.add(order.getAssociatedTaskElement());

            SortedMap<LocalDate, BigDecimal> estimatedCost = new TreeMap<LocalDate, BigDecimal>();

            for (TaskElement taskElement : list) {
                if (taskElement instanceof Task) {
                    addCost(estimatedCost, hoursCostCalculator
                            .getEstimatedCost((Task) taskElement));
                }
            }

            estimatedCost = accumulateResult(estimatedCost);
            indicators.put(EarnedValueType.BCWS, calculatedValueForEveryDay(
                    estimatedCost, interval.getStart(), interval.getFinish()));
        }

        protected void calculateActualCostWorkPerformed(Interval interval) {
            SortedMap<LocalDate, BigDecimal> workReportCost = getWorkReportCost();

            workReportCost = accumulateResult(workReportCost);
            indicators.put(EarnedValueType.ACWP, calculatedValueForEveryDay(
                    workReportCost, interval.getStart(), interval.getFinish()));
        }

        public SortedMap<LocalDate, BigDecimal> getWorkReportCost() {
            SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();

            List<WorkReportLine> workReportLines = workReportLineDAO
                    .findByOrderElementAndChildren(order);

            if (workReportLines.isEmpty()) {
                return result;
            }

            for (WorkReportLine workReportLine : workReportLines) {
                LocalDate day = new LocalDate(workReportLine.getWorkReport()
                        .getDate());
                BigDecimal cost = new BigDecimal(workReportLine.getNumHours());

                if (!result.containsKey(day)) {
                    result.put(day, BigDecimal.ZERO);
                }
                result.put(day, result.get(day).add(cost));
            }

            return result;
        }

        protected void calculateBudgetedCostWorkPerformed(Interval interval) {
            List<TaskElement> list = order
                    .getAllChildrenAssociatedTaskElements();
            list.add(order.getAssociatedTaskElement());

            SortedMap<LocalDate, BigDecimal> advanceCost = new TreeMap<LocalDate, BigDecimal>();

            for (TaskElement taskElement : list) {
                if (taskElement instanceof Task) {
                    addCost(advanceCost, hoursCostCalculator
                            .getAdvanceCost((Task) taskElement));
                }
            }

            advanceCost = accumulateResult(advanceCost);
            indicators.put(EarnedValueType.BCWP, calculatedValueForEveryDay(
                    advanceCost, interval.getStart(), interval.getFinish()));
        }

    }

}
