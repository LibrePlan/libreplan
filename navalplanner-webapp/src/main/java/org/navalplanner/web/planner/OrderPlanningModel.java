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
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
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
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zul.Chart;
import org.zkoss.zul.SimpleXYModel;
import org.zkoss.zul.XYModel;

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
            return object instanceof TaskMilestone;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void setConfigurationToPlanner(Planner planner, Order order,
            ViewSwitcher switcher,
            ResourceAllocationController resourceAllocationController,
            EditTaskController editTaskController,
            SplittingController splittingController,
            CalendarAllocationController calendarAllocationController) {
        Order orderReloaded = reload(order);
        if (!orderReloaded.isSomeTaskElementScheduled())
            throw new IllegalArgumentException(_(
                    "The order {0} must be scheduled", orderReloaded));
        PlannerConfiguration<TaskElement> configuration = createConfiguration(orderReloaded);

        configuration.addGlobalCommand(buildSaveCommand());
        configuration.addGlobalCommand(buildResourceLoadForOrderCommand(switcher));

        configuration.addCommandOnTask(buildResourceAllocationCommand(resourceAllocationController));
        configuration.addCommandOnTask(buildSplitCommand(splittingController));
        configuration.addCommandOnTask(buildMergeTaskCommand());
        configuration.addCommandOnTask(buildMilestoneCommand());
        configuration
                .addCommandOnTask(buildCalendarAllocationCommand(calendarAllocationController));

        configuration.setEditTaskCommand(buildEditTaskCommand(editTaskController));

        Chart chartComponent = new Chart();
        configuration.setChartComponent(chartComponent);

        planner.setConfiguration(configuration);

        setupChart(orderReloaded, chartComponent, planner.getTimeTracker());
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

    private IResourceLoadForOrderCommand buildResourceLoadForOrderCommand(
            ViewSwitcher switcher) {
        IResourceLoadForOrderCommand resourceLoadForOrderCommand = getResourceLoadForOrderCommand();
        resourceLoadForOrderCommand.initialize(switcher, planningState);
        return resourceLoadForOrderCommand;
    }

    private ISaveCommand buildSaveCommand() {
        ISaveCommand saveCommand = getSaveCommand();
        saveCommand.setState(planningState);
        return saveCommand;
    }

    private void setupChart(Order orderReloaded, Chart chartComponent,
            TimeTracker timeTracker) {
        fillChart(orderReloaded, chartComponent, timeTracker
                .getRealInterval(), timeTracker.getHorizontalSize());
        fillChartOnZoomChange(orderReloaded, chartComponent, timeTracker);
    }

    private IZoomLevelChangedListener zoomListener;

    private void fillChartOnZoomChange(final Order order, final Chart chartComponent, final TimeTracker timeTracker) {

        zoomListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                fillChart(order, chartComponent, timeTracker.getRealInterval(),
                        timeTracker.getHorizontalSize());
            }
        };

        timeTracker.addZoomListener(zoomListener);
    }

    private PlannerConfiguration<TaskElement> createConfiguration(
            Order orderReloaded) {
        ITaskElementAdapter taskElementAdapter = getTaskElementAdapter();
        taskElementAdapter.setOrder(orderReloaded);
        planningState = new PlanningState(retainOnlyTopLevel(orderReloaded
                .getAssociatedTasks()), resourceDAO.list(Resource.class));
        forceLoadOfDependenciesCollections(planningState.getInitial());
        forceLoadOfWorkingHours(planningState.getInitial());
        forceLoadOfLabels(planningState.getInitial());
        return new PlannerConfiguration<TaskElement>(taskElementAdapter,
                new TaskElementNavigator(), planningState.getInitial());
    }

    private Collection<? extends TaskElement> retainOnlyTopLevel(
            List<TaskElement> associatedTasks) {
        Set<TaskElement> descendantsFromOther = new HashSet<TaskElement>();
        for (TaskElement taskElement : associatedTasks) {
            descandants(descendantsFromOther, taskElement);
        }
        ArrayList<TaskElement> result = new ArrayList<TaskElement>();
        for (TaskElement taskElement : associatedTasks) {
            if (!descendantsFromOther.contains(taskElement)) {
                result.add(taskElement);
            }
        }
        return result;
    }

    private void descandants(Set<TaskElement> accumulated,
            TaskElement taskElement) {
        if (taskElement.isLeaf()) {
            return;
        }
        for (TaskElement t : taskElement.getChildren()) {
            accumulated.add(t);
            descandants(accumulated, t);
        }
    }

    private void forceLoadOfWorkingHours(List<TaskElement> initial) {
        for (TaskElement taskElement : initial) {
            taskElement.getOrderElement().getWorkHours();
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
                taskElement.getOrderElement().getLabels().size();
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

    protected abstract IResourceLoadForOrderCommand getResourceLoadForOrderCommand();

    private Order reload(Order order) {
        try {
            return orderDAO.find(order.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillChart(Order order, Chart chart, Interval interval,
            Integer size) {
        XYModel xymodel = new SimpleXYModel();

        addLoad(order, xymodel, interval.getStart(), interval.getFinish());
        addCalendarMaximumAvailability(order, xymodel, interval.getStart(),
                interval.getFinish());

        chart.setType("time_series");
        chart.setWidth(size + "px");
        chart.setHeight("175px");
        chart.setModel(xymodel);
    }

    private void addLoad(Order order, XYModel xymodel, Date start, Date finish) {
        List<DayAssignment> dayAssignments = order.getDayAssignments();
        String title = "order";

        SortedMap<LocalDate, Integer> mapDayAssignments = calculateHoursAdditionByDay(dayAssignments);
        for (LocalDate day : mapDayAssignments.keySet()) {
            Integer hours = mapDayAssignments.get(day);
            xymodel.addValue(title, day.toDateTimeAtStartOfDay().getMillis(),
                    hours);
        }

        fillZeroValueFromStart(xymodel, start, title, mapDayAssignments);
        fillZeroValueToFinish(xymodel, finish, title, mapDayAssignments);

        String titleResorucesLoad = "all";
        addResourcesLoad(order, xymodel, mapDayAssignments.keySet(), titleResorucesLoad);

        fillZeroValueFromStart(xymodel, start, titleResorucesLoad,
                mapDayAssignments);
        fillZeroValueToFinish(xymodel, finish, titleResorucesLoad,
                mapDayAssignments);
    }

    private void fillZeroValueFromStart(XYModel xymodel, Date start,
            String title,
            SortedMap<LocalDate, Integer> mapDayAssignments) {
        if (mapDayAssignments.isEmpty()) {
            xymodel.addValue(title, start.getTime(), 0);
        } else if ((new LocalDate(start)).compareTo(mapDayAssignments
                .firstKey()) < 0) {
            xymodel.addValue(title, start.getTime(), 0);
            xymodel.addValue(title, mapDayAssignments.firstKey().minusDays(1)
                    .toDateTimeAtStartOfDay().getMillis(), 0);
        }
    }

    private void fillZeroValueToFinish(XYModel xymodel, Date finish,
            String title, SortedMap<LocalDate, Integer> mapDayAssignments) {
        if (mapDayAssignments.isEmpty()) {
            xymodel.addValue(title, finish.getTime(), 0);
        } else if ((new LocalDate(finish)).compareTo(mapDayAssignments
                .lastKey()) > 0) {
            xymodel.addValue(title, mapDayAssignments.lastKey().plusDays(1)
                    .toDateTimeAtStartOfDay().getMillis(), 0);
            xymodel.addValue(title, finish.getTime(), 0);
        }
    }

    private void addResourcesLoad(Order order, XYModel xymodel,
            Set<LocalDate> days, String title) {
        List<DayAssignment> dayAssignments = new ArrayList<DayAssignment>();

        Set<Resource> resources = order.getResources();
        for (Resource resource : resources) {
            dayAssignments.addAll(resource.getAssignments());
        }

        SortedMap<LocalDate, Integer> mapDayAssignments = calculateHoursAdditionByDay(dayAssignments);
        for (LocalDate day : mapDayAssignments.keySet()) {
            if (days.contains(day)) {
                Integer hours = mapDayAssignments.get(day);
                xymodel.addValue(title, new Long(day.toDateTimeAtStartOfDay()
                        .getMillis()), hours);
            }
        }
    }

    private void addCalendarMaximumAvailability(Order order, XYModel xymodel,
            Date start, Date finish) {
        String title = "max";

        SortedMap<LocalDate, Integer> mapDayAssignments = calculateHoursAdditionByDay(
                order.getDayAssignments(), true);
        for (LocalDate day : mapDayAssignments.keySet()) {
            Integer hours = mapDayAssignments.get(day);
            xymodel.addValue(title, new Long(day.toDateTimeAtStartOfDay()
                    .getMillis()), hours);
        }

        fillZeroValueFromStart(xymodel, start, title, mapDayAssignments);
        fillZeroValueToFinish(xymodel, finish, title, mapDayAssignments);
    }

    private SortedMap<LocalDate, Integer> calculateHoursAdditionByDay(
            List<DayAssignment> dayAssignments) {
        return calculateHoursAdditionByDay(dayAssignments, false);
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
     * @return A map { day => hours } sorted by date
     */
    private SortedMap<LocalDate, Integer> calculateHoursAdditionByDay(
            List<DayAssignment> dayAssignments, boolean calendarHours) {
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

        for (DayAssignment dayAssignment : dayAssignments) {
            LocalDate day = dayAssignment.getDay();
            Integer hours = 0;

            if (calendarHours) {
                ResourceCalendar calendar = dayAssignment.getResource()
                        .getCalendar();
                if (calendar != null) {
                    hours = calendar.getWorkableHours(dayAssignment.getDay());
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

        return map;
    }

}
