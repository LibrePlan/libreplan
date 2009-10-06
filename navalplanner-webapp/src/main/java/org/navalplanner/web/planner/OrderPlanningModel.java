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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.common.ViewSwitcher;
import org.navalplanner.web.planner.allocation.ResourceAllocationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
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
    }

    @Override
    @Transactional(readOnly = true)
    public void createConfiguration(Order order, ViewSwitcher switcher,
            ResourceAllocationController resourceAllocationController,
            EditTaskController editTaskController,
            SplittingController splittingController,
            CalendarAllocationController calendarAllocationController,
            IConfigurationOnTransaction onTransaction) {
        Order orderReloaded = reload(order);
        if (!orderReloaded.isSomeTaskElementScheduled())
            throw new IllegalArgumentException(_(
                    "The order {0} must be scheduled", order));
        PlannerConfiguration<TaskElement> configuration = createConfiguration(orderReloaded);

        ISaveCommand saveCommand = getSaveCommand();
        saveCommand.setState(planningState);
        configuration.addGlobalCommand(saveCommand);

        IResourceLoadForOrderCommand resourceLoadForOrderCommand = getResourceLoadForOrderCommand();
        resourceLoadForOrderCommand.initialize(switcher, planningState);
        configuration.addGlobalCommand(resourceLoadForOrderCommand);

        IResourceAllocationCommand resourceAllocationCommand = getResourceAllocationCommand();
        resourceAllocationCommand.initialize(resourceAllocationController,
                planningState);
        configuration.addCommandOnTask(resourceAllocationCommand);

        ISplitTaskCommand splitCommand = getSplitCommand();
        splitCommand.setState(planningState);
        splitCommand.setSplitWindowController(splittingController);
        configuration.addCommandOnTask(splitCommand);

        IMergeTaskCommand mergeCommand = getMergeTaskCommand();
        mergeCommand.setState(planningState);
        configuration.addCommandOnTask(mergeCommand);

        IAddMilestoneCommand addMilestoneCommand = getAddMilestoneCommand();
        addMilestoneCommand.setState(planningState);
        configuration.addCommandOnTask(addMilestoneCommand);

        IEditTaskCommand editTaskCommand = getEditTaskCommand();
        editTaskCommand.setEditTaskController(editTaskController);
        configuration.setEditTaskCommand(editTaskCommand);

        ICalendarAllocationCommand calendarAllocationCommand = getCalendarAllocationCommand();
        calendarAllocationCommand
                .setCalendarAllocationController(calendarAllocationController);
        configuration.addCommandOnTask(calendarAllocationCommand);

        configuration.setChartComponent(getChartComponent(orderReloaded));

        onTransaction.use(configuration);
    }

    private PlannerConfiguration<TaskElement> createConfiguration(
            Order orderReloaded) {
        ITaskElementAdapter taskElementAdapter = getTaskElementAdapter();
        taskElementAdapter.setOrder(orderReloaded);
        planningState = new PlanningState(retainOnlyTopLevel(orderReloaded
                .getAssociatedTasks()), resourceDAO.list(Resource.class));
        forceLoadOfDependenciesCollections(planningState.getInitial());
        forceLoadOfWorkingHours(planningState.getInitial());
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

    private Chart getChartComponent(Order order) {
        XYModel xymodel = new SimpleXYModel();

        addLoad(order, xymodel);

        Chart chart = new Chart();
        chart.setType("time_series");
        chart.setWidth("1600px");
        chart.setHeight("175px");
        chart.setModel(xymodel);

        return chart;
    }

    private void addLoad(Order order, XYModel xymodel) {
        List<DayAssignment> dayAssignments = order.getDayAssignments();
        String title = "order";

        SortedMap<LocalDate, Integer> mapDayAssignments = calculateHoursAdditionByDay(dayAssignments);
        for (LocalDate day : mapDayAssignments.keySet()) {
            Integer hours = mapDayAssignments.get(day);
            xymodel.addValue(title, new Long(day.toDateTimeAtStartOfDay()
                    .getMillis()), hours);
        }

        addResourcesLoad(order, xymodel, mapDayAssignments.keySet());
    }

    private void addResourcesLoad(Order order, XYModel xymodel,
            Set<LocalDate> days) {
        List<DayAssignment> dayAssignments = new ArrayList<DayAssignment>();
        String title = "all";

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

    private SortedMap<LocalDate, Integer> calculateHoursAdditionByDay(
            List<DayAssignment> dayAssignments) {
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
            Integer hours = dayAssignment.getHours();

            if (map.get(day) == null) {
                map.put(day, hours);
            } else {
                map.put(day, map.get(day) + hours);
            }
        }

        return map;
    }

}
