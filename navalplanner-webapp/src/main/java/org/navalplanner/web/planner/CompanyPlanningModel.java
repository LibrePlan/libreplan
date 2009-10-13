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
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.daos.IDayAssignmentDAO;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.TaskMilestone;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.common.ViewSwitcher;
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
 * Model for company planning view.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public abstract class CompanyPlanningModel implements ICompanyPlanningModel {

    @Autowired
    private IOrderDAO orderDAO;

    private PlanningState planningState;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IDayAssignmentDAO dayAssignmentDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    private final class TaskElementNavigator implements
            IStructureNavigator<TaskElement> {
        @Override
        public List<TaskElement> getChildren(TaskElement object) {
            return null;
        }

        @Override
        public boolean isLeaf(TaskElement object) {
            return true;
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
    public void setConfigurationToPlanner(Planner planner,
            ViewSwitcher switcher, EditTaskController editTaskController) {
        PlannerConfiguration<TaskElement> configuration = createConfiguration();

        configuration.addGlobalCommand(buildSaveCommand());
        configuration
                .addGlobalCommand(buildResourceLoadForOrderCommand(switcher));

        configuration
                .setEditTaskCommand(buildEditTaskCommand(editTaskController));

        Chart chartComponent = new Chart();
        configuration.setChartComponent(chartComponent);

        planner.setConfiguration(configuration);

        setupChart(chartComponent, planner.getTimeTracker());
    }

    private IEditTaskCommand buildEditTaskCommand(
            EditTaskController editTaskController) {
        IEditTaskCommand editTaskCommand = getEditTaskCommand();
        editTaskCommand.setEditTaskController(editTaskController);
        return editTaskCommand;
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

    private void setupChart(Chart chartComponent, TimeTracker timeTracker) {
        fillChart(chartComponent, timeTracker.getRealInterval(), timeTracker
                .getHorizontalSize());
        fillChartOnZoomChange(chartComponent, timeTracker);
    }

    private IZoomLevelChangedListener zoomListener;

    private void fillChartOnZoomChange(final Chart chartComponent,
            final TimeTracker timeTracker) {

        zoomListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                transactionService
                        .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
                    @Override
                    public Void execute() {
                        fillChart(chartComponent,
                                timeTracker.getRealInterval(), timeTracker
                                        .getHorizontalSize());
                        return null;
                    }
                });
            }
        };

        timeTracker.addZoomListener(zoomListener);
    }

    private PlannerConfiguration<TaskElement> createConfiguration() {
        ITaskElementAdapter taskElementAdapter = getTaskElementAdapter();
        planningState = new PlanningState(retainOnlyTopLevel(), resourceDAO
                .list(Resource.class));
        forceLoadOfDependenciesCollections(planningState.getInitial());
        forceLoadOfWorkingHours(planningState.getInitial());
        forceLoadOfLabels(planningState.getInitial());
        return new PlannerConfiguration<TaskElement>(taskElementAdapter,
                new TaskElementNavigator(), planningState.getInitial());
    }

    private Collection<? extends TaskElement> retainOnlyTopLevel() {
        List<Order> list = orderDAO.list(Order.class);
        Set<TaskElement> result = new HashSet<TaskElement>();
        for (Order order : list) {
            TaskGroup associatedTaskElement = order.getAssociatedTaskElement();
            if (associatedTaskElement != null) {
                result.add(associatedTaskElement);
            }
        }
        return result;
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
            taskElement.getOrderElement().getLabels().size();
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

    private void fillChart(Chart chart, Interval interval, Integer size) {
        XYModel xymodel = new SimpleXYModel();

        addLoad(xymodel, interval.getStart(), interval.getFinish());
        addCalendarMaximumAvailability(xymodel, interval.getStart(), interval
                .getFinish());

        chart.setType("time_series");
        chart.setWidth(size + "px");
        chart.setHeight("175px");
        chart.setModel(xymodel);
    }

    private void addLoad(XYModel xymodel, Date start, Date finish) {
        List<DayAssignment> dayAssignments = dayAssignmentDAO
                .list(DayAssignment.class);
        String title = "load";

        SortedMap<LocalDate, Integer> mapDayAssignments = calculateHoursAdditionByDay(dayAssignments);
        for (LocalDate day : mapDayAssignments.keySet()) {
            Integer hours = mapDayAssignments.get(day);
            xymodel.addValue(title, day.toDateTimeAtStartOfDay().getMillis(),
                    hours);
        }

        fillZeroValueFromStart(xymodel, start, title, mapDayAssignments);
        fillZeroValueToFinish(xymodel, finish, title, mapDayAssignments);
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

    private void addCalendarMaximumAvailability(XYModel xymodel, Date start,
            Date finish) {
        String title = "max";

        SortedMap<LocalDate, Integer> mapDayAssignments = calculateHoursAdditionByDay(
                resourceDAO.list(Resource.class), start, finish);
        for (LocalDate day : mapDayAssignments.keySet()) {
            Integer hours = mapDayAssignments.get(day);
            xymodel.addValue(title, new Long(day.toDateTimeAtStartOfDay()
                    .getMillis()), hours);
        }

        fillZeroValueFromStart(xymodel, start, title, mapDayAssignments);
        fillZeroValueToFinish(xymodel, finish, title, mapDayAssignments);
    }

    /**
     * Calculate the hours by day for all the {@link DayAssignment} in the list.
     *
     * @param dayAssignments
     *            The list of {@link DayAssignment}
     * @return A map { day => hours } sorted by date
     */
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

    private SortedMap<LocalDate, Integer> calculateHoursAdditionByDay(
            List<Resource> resources, Date start, Date finish) {
        SortedMap<LocalDate, Integer> map = new TreeMap<LocalDate, Integer>();

        LocalDate end = new LocalDate(finish);

        for (LocalDate date = new LocalDate(start); date.compareTo(end) <= 0; date = date
                .plusDays(1)) {
            Integer hours = 0;
            for (Resource resource : resources) {
                ResourceCalendar calendar = resource.getCalendar();
                if (calendar != null) {
                    hours += calendar.getWorkableHours(date);
                }
            }

            map.put(date, hours);
        }

        return map;
    }

}
