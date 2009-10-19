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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
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
import org.navalplanner.web.planner.allocation.ResourceAllocationController;
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

    private ILoadChartFiller loadChartFiller = new OrderLoadChartFiller();

    private Order order;

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
        configuration.addGlobalCommand(buildSaveCommand());

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

        setupChart(orderReloaded, chartComponent, planner.getTimeTracker());
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

    private void setupChart(Order orderReloaded, Timeplot chartComponent,
            TimeTracker timeTracker) {
        this.order = orderReloaded;
        loadChartFiller.fillChart(chartComponent, timeTracker
                .getRealInterval(), timeTracker.getHorizontalSize());
        fillChartOnZoomChange(orderReloaded, chartComponent, timeTracker);
    }

    private void fillChartOnZoomChange(final Order order,
            final Timeplot chartComponent, final TimeTracker timeTracker) {
        this.order = order;

        zoomListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                loadChartFiller.setZoomLevel(detailLevel);

                loadChartFiller.fillChart(chartComponent, timeTracker
                        .getRealInterval(), timeTracker.getHorizontalSize());
            }
        };

        timeTracker.addZoomListener(zoomListener);
    }

    private PlannerConfiguration<TaskElement> createConfiguration(
            Order orderReloaded) {
        ITaskElementAdapter taskElementAdapter = getTaskElementAdapter();
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

    private SortedMap<LocalDate, Integer> calculateHoursAdditionByDay(
            List<DayAssignment> dayAssignments) {
        return calculateHoursAdditionByDay(dayAssignments, false, null, null);
    }

    private SortedMap<LocalDate, Integer> calculateHoursAdditionByDay(
            List<DayAssignment> dayAssignments, LocalDate minDate,
            LocalDate maxDate) {
        return calculateHoursAdditionByDay(dayAssignments, false, minDate,
                maxDate);
    }

    private SortedMap<LocalDate, Integer> calculateHoursAdditionByDay(
            List<DayAssignment> dayAssignments, boolean calendarHours) {
        return calculateHoursAdditionByDay(dayAssignments, calendarHours, null,
                null);
    }

    /**
     * Calculate the hours by day for all the {@link DayAssignment} in the list.
     *
     * @param dayAssignments
     *            The list of {@link DayAssignment}
     * @param calendarHours
     *            If <code>true</code> the resource's calendar will be used to
     *            calculate the available hours. Otherwise, the
     *            {@link DayAssignment} hours will be used.
     * @param minDate
     *            If it's not <code>null</code>, just {@link DayAssignment} from
     *            this date will be used.
     * @param maxDate
     *            If it's not <code>null</code>, just {@link DayAssignment} to
     *            this date will be used.
     *
     * @return A map { day => hours } sorted by date
     */
    private SortedMap<LocalDate, Integer> calculateHoursAdditionByDay(
            List<DayAssignment> dayAssignments, boolean calendarHours,
            LocalDate minDate, LocalDate maxDate) {
        SortedMap<LocalDate, Integer> map = new TreeMap<LocalDate, Integer>();

        if (dayAssignments.isEmpty()) {
            return map;
        }

        Collections.sort(dayAssignments, new Comparator<DayAssignment>() {

            @Override
            public int compare(DayAssignment o1, DayAssignment o2) {
                return o1.getDay().compareTo(o2.getDay());
            }

        });

        Set<Resource> resroucesAlreadyUsed = new HashSet<Resource>();

        for (DayAssignment dayAssignment : dayAssignments) {
            LocalDate day = dayAssignment.getDay();
            Integer hours = 0;

            if (minDate != null) {
                if (day.compareTo(minDate) < 0) {
                    continue;
                }
            }

            if (maxDate != null) {
                if (day.compareTo(maxDate) > 0) {
                    continue;
                }
            }

            if (calendarHours) {
                Resource resource = dayAssignment.getResource();

                if (map.get(day) == null) {
                    resroucesAlreadyUsed.clear();
                }

                if (!resroucesAlreadyUsed.contains(resource)) {
                    resroucesAlreadyUsed.add(resource);
                    ResourceCalendar calendar = resource.getCalendar();
                    if (calendar != null) {
                        hours = calendar.getWorkableHours(dayAssignment
                                .getDay());
                    } else {
                        hours = SameWorkHoursEveryDay.getDefaultWorkingDay()
                                .getWorkableHours(dayAssignment.getDay());
                    }
                }
            } else {
                hours = dayAssignment.getHours();
            }

            if (map.get(day) == null) {
                map.put(day, hours);
            } else {
                map.put(day, map.get(day) + hours);
            }
        }

        if (loadChartFiller.zoomByDay()) {
            return map;
        } else {
            return loadChartFiller.groupByWeek(map);
        }
    }

    private org.zkoss.zk.ui.Component getChartLegend() {
        Div div = new Div();

        Executions.createComponents("/planner/_legendOrderPlanner.zul", div,
                null);

        return div;
    }

    private class OrderLoadChartFiller extends LoadChartFiller {

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
            SortedMap<LocalDate, Integer> mapDayAssignments = calculateHoursAdditionByDay(
                    order.getDayAssignments(), true);

            String uri = getServletUri(mapDayAssignments, start, finish);

            PlotDataSource pds = new PlotDataSource();
            pds.setDataSourceUri(uri);
            pds.setSeparator(" ");

            Plotinfo plotInfo = new Plotinfo();
            plotInfo.setPlotDataSource(pds);

            return plotInfo;
        }

    }

}
