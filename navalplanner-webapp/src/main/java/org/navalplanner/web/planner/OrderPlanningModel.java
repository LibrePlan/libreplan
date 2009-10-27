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

package org.navalplanner.web.planner;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Map.Entry;

import org.joda.time.LocalDate;
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
import org.navalplanner.web.planner.ISaveCommand.IAfterSaveListener;
import org.navalplanner.web.planner.ITaskElementAdapter.IOnMoveListener;
import org.navalplanner.web.planner.allocation.ResourceAllocationController;
import org.navalplanner.web.planner.calendar.CalendarAllocationController;
import org.navalplanner.web.planner.calendar.ICalendarAllocationCommand;
import org.navalplanner.web.planner.milestone.IAddMilestoneCommand;
import org.navalplanner.web.planner.splitting.IMergeTaskCommand;
import org.navalplanner.web.planner.splitting.ISplitTaskCommand;
import org.navalplanner.web.planner.splitting.SplittingController;
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
import org.zkforge.timeplot.geometry.DefaultValueGeometry;
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

    private LocalDate minDate;

    private LocalDate maxDate;

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
            SplittingController splittingController,
            CalendarAllocationController calendarAllocationController,
            List<ICommand<TaskElement>> additional) {
        Order orderReloaded = reload(order);
        if (!orderReloaded.isSomeTaskElementScheduled())
            throw new IllegalArgumentException(_(
                    "The order {0} must be scheduled", orderReloaded));
        PlannerConfiguration<TaskElement> configuration = createConfiguration(orderReloaded);
        addAdditional(additional, configuration);
        ISaveCommand saveCommand = buildSaveCommand();
        configuration.addGlobalCommand(saveCommand);

        configuration.addCommandOnTask(buildResourceAllocationCommand(resourceAllocationController));
        configuration.addCommandOnTask(buildSplitCommand(splittingController));
        configuration.addCommandOnTask(buildMergeTaskCommand());
        configuration.addCommandOnTask(buildMilestoneCommand());
        configuration
                .addCommandOnTask(buildCalendarAllocationCommand(calendarAllocationController));

        configuration.setEditTaskCommand(buildEditTaskCommand(editTaskController));

        Timeplot chartComponent = new Timeplot();
        configuration.setChartComponent(chartComponent);

        configuration.setChartLegend(getChartLegend());

        planner.setConfiguration(configuration);

        LoadChart loadChart = setupChart(orderReloaded, chartComponent, planner
                .getTimeTracker());
        refillLoadChartWhenNeeded(planner, saveCommand, loadChart);
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

    private IMergeTaskCommand buildMergeTaskCommand() {
        IMergeTaskCommand mergeCommand = getMergeTaskCommand();
        mergeCommand.setState(planningState);
        return mergeCommand;
    }

    private ISplitTaskCommand buildSplitCommand(
            SplittingController splittingController) {
        ISplitTaskCommand splitCommand = getSplitCommand();
        splitCommand.setState(planningState);
        splitCommand.setSplitWindowController(splittingController);
        return splitCommand;
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
        return new PlannerConfiguration<TaskElement>(taskElementAdapter,
                new TaskElementNavigator(), planningState.getInitial());
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

    protected abstract ISplitTaskCommand getSplitCommand();

    protected abstract IMergeTaskCommand getMergeTaskCommand();

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

    private org.zkoss.zk.ui.Component getChartLegend() {
        Div div = new Div();

        Executions.createComponents("/planner/_legendOrderPlanner.zul", div,
                null);

        return div;
    }

    private class OrderLoadChartFiller extends LoadChartFiller {

        private final Order order;

        public OrderLoadChartFiller(Order orderReloaded) {
            this.order = orderReloaded;
        }

        @Override
        public void fillChart(Timeplot chart, Interval interval, Integer size) {
            chart.getChildren().clear();
            chart.invalidate();
            resetMaximunValueForChart();
            Plotinfo plotInfoOrder = getLoadPlotInfo(order,
                    interval.getStart(), interval.getFinish());
            plotInfoOrder.setFillColor("0000FF");

            Plotinfo plotInfoCompany = getResourcesLoadPlotInfo(order, interval
                    .getStart(), interval.getFinish());
            plotInfoCompany.setFillColor("00FF00");

            Plotinfo plotInfoMax = getCalendarMaximumAvailabilityPlotInfo(
                    order, interval.getStart(), interval.getFinish());
            plotInfoMax.setLineColor("FF0000");

            ValueGeometry valueGeometry = new DefaultValueGeometry();
            valueGeometry.setMin(0);
            valueGeometry.setMax(getMaximunValueForChart());
            valueGeometry.setGridColor("#000000");
            valueGeometry.setAxisLabelsPlacement("left");

            plotInfoOrder.setValueGeometry(valueGeometry);
            plotInfoCompany.setValueGeometry(valueGeometry);
            plotInfoMax.setValueGeometry(valueGeometry);

            chart.appendChild(plotInfoMax);
            chart.appendChild(plotInfoOrder);
            chart.appendChild(plotInfoCompany);

            size = size + (16 * 2);
            chart.setWidth(size + "px");
            chart.setHeight("100px");
        }

        private Plotinfo getLoadPlotInfo(Order order, Date start, Date finish) {
            List<DayAssignment> dayAssignments = order.getDayAssignments();
            SortedMap<LocalDate, Integer> mapDayAssignments = calculateHoursAdditionByDay(dayAssignments);

            if (mapDayAssignments.isEmpty()) {
                minDate = new LocalDate();
                maxDate = new LocalDate();
            } else {
                SortedSet<LocalDate> keys = (SortedSet<LocalDate>) mapDayAssignments
                        .keySet();
                minDate = keys.first();
                maxDate = keys.last();
            }

            String uri = getServletUri(mapDayAssignments, start, finish);

            PlotDataSource pds = new PlotDataSource();
            pds.setDataSourceUri(uri);
            pds.setSeparator(" ");

            Plotinfo plotInfo = new Plotinfo();
            plotInfo.setPlotDataSource(pds);

            return plotInfo;
        }

        private Plotinfo getResourcesLoadPlotInfo(Order order, Date start,
                Date finish) {
            List<DayAssignment> dayAssignments = new ArrayList<DayAssignment>();

            Set<Resource> resources = order.getResources();
            for (Resource resource : resources) {
                dayAssignments.addAll(resource.getAssignments());
            }

            SortedMap<LocalDate, Integer> mapDayAssignments = calculateHoursAdditionByDay(
                    dayAssignments, minDate, maxDate);

            String uri = getServletUri(mapDayAssignments, start, finish);

            PlotDataSource pds = new PlotDataSource();
            pds.setDataSourceUri(uri);
            pds.setSeparator(" ");

            Plotinfo plotInfo = new Plotinfo();
            plotInfo.setPlotDataSource(pds);

            return plotInfo;
        }

        private Plotinfo getCalendarMaximumAvailabilityPlotInfo(Order order,
                Date start, Date finish) {
            SortedMap<LocalDate, Integer> mapDayAssignments = calculateCapacity(order.getDayAssignments());

            String uri = getServletUri(mapDayAssignments, start, finish);

            PlotDataSource pds = new PlotDataSource();
            pds.setDataSourceUri(uri);
            pds.setSeparator(" ");

            Plotinfo plotInfo = new Plotinfo();
            plotInfo.setPlotDataSource(pds);

            return plotInfo;
        }

        private SortedMap<LocalDate, Integer> calculateCapacity(
                List<DayAssignment> dayAssignments) {
            return new HoursByDayCalculator<Entry<LocalDate, Collection<Resource>>>() {

                @Override
                protected LocalDate getDayFor(
                        Entry<LocalDate, Collection<Resource>> element) {
                    return element.getKey();
                }

                @Override
                protected int getHoursFor(
                        Entry<LocalDate, Collection<Resource>> element) {
                    Collection<Resource> resources = element.getValue();
                    LocalDate day = element.getKey();
                    return sumHoursForDay(resources, day);
                }

            }.calculate(resourcesByDate(dayAssignments));
        }

        private Collection<Entry<LocalDate, Collection<Resource>>> resourcesByDate(
                List<DayAssignment> dayAssignments) {
            Map<LocalDate, Collection<Resource>> result = new HashMap<LocalDate, Collection<Resource>>();
            for (DayAssignment dayAssignment : dayAssignments) {
                LocalDate day = dayAssignment.getDay();
                if (!result.containsKey(day)) {
                    result.put(day, new HashSet<Resource>());
                }
                result.get(day).add(dayAssignment.getResource());
            }
            return result.entrySet();
        }

        private SortedMap<LocalDate, Integer> calculateHoursAdditionByDay(
                List<DayAssignment> dayAssignments) {
            return new DefaultDayAssignmentCalculator()
                    .calculate(dayAssignments);
        }

        private SortedMap<LocalDate, Integer> calculateHoursAdditionByDay(
                List<DayAssignment> dayAssignments, final LocalDate minDate,
                final LocalDate maxDate) {
            return new DefaultDayAssignmentCalculator() {
                protected boolean included(DayAssignment each) {
                    return each.includedIn(minDate, maxDate);
                };
            }.calculate(dayAssignments);
        }

    }

}
