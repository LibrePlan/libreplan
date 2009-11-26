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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.SameWorkHoursEveryDay;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskMilestone;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.common.ViewSwitcher;
import org.navalplanner.web.planner.ITaskElementAdapter;
import org.navalplanner.web.planner.ITaskElementAdapter.IOnMoveListener;
import org.navalplanner.web.planner.allocation.IResourceAllocationCommand;
import org.navalplanner.web.planner.allocation.ResourceAllocationController;
import org.navalplanner.web.planner.calendar.CalendarAllocationController;
import org.navalplanner.web.planner.calendar.ICalendarAllocationCommand;
import org.navalplanner.web.planner.loadchart.LoadChart;
import org.navalplanner.web.planner.loadchart.LoadChartFiller;
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
import org.zkforge.timeplot.data.PlotDataSource;
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
import org.zkoss.zul.Label;
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

    private IZoomLevelChangedListener zoomListener;

    private ITaskElementAdapter taskElementAdapter;

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
        appendTabpanels(chartComponent, chartLoadTimeplot);

        configuration.setChartComponent(chartComponent);
        planner.setConfiguration(configuration);

        LoadChart loadChart = setupChart(orderReloaded, chartLoadTimeplot,
                planner.getTimeTracker());
        refillLoadChartWhenNeeded(planner, saveCommand, loadChart);
    }

    private void appendTabs(Tabbox chartComponent) {
        Tabs chartTabs = new Tabs();
        chartTabs.appendChild(new Tab(_("Load")));
        chartTabs.appendChild(new Tab(_("Earned value")));

        chartComponent.appendChild(chartTabs);
        chartTabs.setWidth("100px");
    }

    private void appendTabpanels(Tabbox chartComponent, Timeplot loadChart) {
        Tabpanels chartTabpanels = new Tabpanels();

        Tabpanel loadChartPannel = new Tabpanel();
        appendLoadChartAndLegend(loadChartPannel, loadChart);
        chartTabpanels.appendChild(loadChartPannel);

        Tabpanel earnedValueChartPannel = new Tabpanel();
        chartTabpanels.appendChild(earnedValueChartPannel);
        earnedValueChartPannel
                .appendChild(new Label("TODO: Earned value chart"));

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

    private void refillLoadChartWhenNeeded(Planner planner,
            ISaveCommand saveCommand, final LoadChart loadChart) {
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

    private LoadChart setupChart(Order orderReloaded, Timeplot chartComponent,
            TimeTracker timeTracker) {
        OrderLoadChartFiller loadChartFiller = new OrderLoadChartFiller(
                orderReloaded);
        LoadChart result = new LoadChart(chartComponent, loadChartFiller,
                timeTracker);
        result.fillChart();
        return result;
    }

    private IZoomLevelChangedListener fillOnZoomChange(final LoadChart loadChart) {
        zoomListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                loadChart.fillChart();
            }
        };
        return zoomListener;
    }

    private IAfterSaveListener fillChartOnSave(final LoadChart loadChart) {
        IAfterSaveListener result = new IAfterSaveListener() {

                    @Override
            public void onAfterSave() {
                loadChart.fillChart();
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

    private class OrderLoadChartFiller extends LoadChartFiller {

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
            resetMaximunValueForChart();

            List<DayAssignment> orderDayAssignments = order.getDayAssignments();
            SortedMap<LocalDate, Map<Resource, Integer>> orderDayAssignmentsGrouped = groupDayAssignmentsByDayAndResource(orderDayAssignments);

            List<DayAssignment> resourcesDayAssignments = new ArrayList<DayAssignment>();
            for (Resource resource : order.getResources()) {
                resourcesDayAssignments.addAll(resource.getAssignments());
            }
            SortedMap<LocalDate, Map<Resource, Integer>> resourceDayAssignmentsGrouped = groupDayAssignmentsByDayAndResource(resourcesDayAssignments);

            fillMaps(orderDayAssignmentsGrouped, resourceDayAssignmentsGrouped);
            groupByWeekMaps();

            Plotinfo plotOrderLoad = getPlotinfo(mapOrderLoad, interval
                    .getStart(), interval.getFinish());
            Plotinfo plotOrderOverload = getPlotinfo(mapOrderOverload, interval
                    .getStart(), interval.getFinish());
            Plotinfo plotMaxCapacity = getPlotinfo(mapMaxCapacity, interval
                    .getStart(), interval.getFinish());
            Plotinfo plotOtherLoad = getPlotinfo(mapOtherLoad, interval
                    .getStart(), interval.getFinish());
            Plotinfo plotOtherOverload = getPlotinfo(mapOtherOverload, interval
                    .getStart(), interval.getFinish());

            plotOrderLoad.setFillColor("0000FF");
            plotOrderOverload.setLineColor("00FFFF");
            plotMaxCapacity.setLineColor("FF0000");
            plotMaxCapacity.setLineWidth(2);
            plotOtherLoad.setFillColor("00FF00");
            plotOtherOverload.setLineColor("FFFF00");

            ValueGeometry valueGeometry = getValueGeometry(getMaximunValueForChart());
            TimeGeometry timeGeometry = getTimeGeometry(interval);

            plotOrderLoad.setValueGeometry(valueGeometry);
            plotOrderOverload.setValueGeometry(valueGeometry);
            plotMaxCapacity.setValueGeometry(valueGeometry);
            plotOtherLoad.setValueGeometry(valueGeometry);
            plotOtherOverload.setValueGeometry(valueGeometry);

            plotOrderLoad.setTimeGeometry(timeGeometry);
            plotOrderOverload.setTimeGeometry(timeGeometry);
            plotMaxCapacity.setTimeGeometry(timeGeometry);
            plotOtherLoad.setTimeGeometry(timeGeometry);
            plotOtherOverload.setTimeGeometry(timeGeometry);

            chart.appendChild(plotOrderLoad);
            chart.appendChild(plotOrderOverload);
            chart.appendChild(plotMaxCapacity);
            chart.appendChild(plotOtherLoad);
            chart.appendChild(plotOtherOverload);

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

        private Plotinfo getPlotinfo(
                SortedMap<LocalDate, Integer> mapDayAssignments, Date start,
                Date finish) {
            String uri = getServletUri(convertToBigDecimal(mapDayAssignments),
                    start, finish);

            PlotDataSource pds = new PlotDataSource();
            pds.setDataSourceUri(uri);
            pds.setSeparator(" ");

            Plotinfo plotInfo = new Plotinfo();
            plotInfo.setPlotDataSource(pds);

            return plotInfo;
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

}
