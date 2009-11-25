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

package org.navalplanner.web.planner.company;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.SameWorkHoursEveryDay;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.daos.IDayAssignmentDAO;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.TaskMilestone;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.planner.ITaskElementAdapter;
import org.navalplanner.web.planner.loadchart.LoadChart;
import org.navalplanner.web.planner.loadchart.LoadChartFiller;
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
import org.zkoss.ganttz.extensions.ICommandOnTask;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Div;


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

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IDayAssignmentDAO dayAssignmentDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    private IZoomLevelChangedListener zoomListener;

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
            Collection<ICommandOnTask<TaskElement>> additional) {
        PlannerConfiguration<TaskElement> configuration = createConfiguration();
        Timeplot chartComponent = new Timeplot();
        configuration.setChartComponent(chartComponent);
        addAdditionalCommands(additional, configuration);

        disableSomeFeatures(configuration);

        configuration.setChartLegend(getChartLegend());

        planner.setConfiguration(configuration);

        setupChart(chartComponent, planner.getTimeTracker());
    }

    private void disableSomeFeatures(
            PlannerConfiguration<TaskElement> configuration) {
        configuration.setAddingDependenciesEnabled(false);
        configuration.setMovingTasksEnabled(false);
        configuration.setResizingTasksEnabled(false);
    }

    private void addAdditionalCommands(
            Collection<ICommandOnTask<TaskElement>> additional,
            PlannerConfiguration<TaskElement> configuration) {
        for (ICommandOnTask<TaskElement> t : additional) {
            configuration.addCommandOnTask(t);
        }
    }

    private void setupChart(Timeplot chartComponent, TimeTracker timeTracker) {
        LoadChart loadChart = new LoadChart(chartComponent,
                new CompanyLoadChartFiller(), timeTracker);
        loadChart.fillChart();
        timeTracker.addZoomListener(fillOnZoomChange(loadChart));
    }

    private IZoomLevelChangedListener fillOnZoomChange(
            final LoadChart loadChart) {

        zoomListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(final ZoomLevel detailLevel) {
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
        return zoomListener;
    }

    private PlannerConfiguration<TaskElement> createConfiguration() {
        ITaskElementAdapter taskElementAdapter = getTaskElementAdapter();
        List<TaskElement> toShow = sortByStartDate(retainOnlyTopLevel());
        forceLoadOfDependenciesCollections(toShow);
        forceLoadOfWorkingHours(toShow);
        forceLoadOfLabels(toShow);
        return new PlannerConfiguration<TaskElement>(taskElementAdapter,
                new TaskElementNavigator(), toShow);
    }

    private List<TaskElement> sortByStartDate(List<TaskElement> list) {
        List<TaskElement> result = new ArrayList<TaskElement>(list);
        Collections.sort(result, new Comparator<TaskElement>() {
            @Override
            public int compare(TaskElement o1, TaskElement o2) {
                if (o1.getStartDate() == null) {
                    return -1;
                }
                if (o2.getStartDate() == null) {
                    return 1;
                }
                return o1.getStartDate().compareTo(o2.getStartDate());
            }
        });
        return result;
    }

    private List<TaskElement> retainOnlyTopLevel() {
        List<Order> list = orderDAO.list(Order.class);
        List<TaskElement> result = new ArrayList<TaskElement>();
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
            OrderElement orderElement = taskElement.getOrderElement();
            if (orderElement != null) {
                orderElement.getLabels().size();
            }
        }
    }

    // spring method injection
    protected abstract ITaskElementAdapter getTaskElementAdapter();

    private org.zkoss.zk.ui.Component getChartLegend() {
        Div div = new Div();

        Executions.createComponents("/planner/_legendCompanyPlanner.zul", div,
                null);

        return div;
    }

    private class CompanyLoadChartFiller extends LoadChartFiller {

        @Override
        public void fillChart(Timeplot chart, Interval interval, Integer size) {
            chart.getChildren().clear();
            chart.invalidate();
            resetMaximunValueForChart();

            Plotinfo plotInfoLoad = getLoadPlotInfo(interval.getStart(),
                    interval.getFinish());
            plotInfoLoad.setFillColor("0000FF");

            Plotinfo plotInfoMax = getCalendarMaximumAvailabilityPlotInfo(
                    interval.getStart(), interval.getFinish());
            plotInfoMax.setLineColor("FF0000");
            plotInfoMax.setLineWidth(1);

            Plotinfo plotInfoOverload = getOverloadPlotInfo(
                    interval.getStart(), interval.getFinish());
            plotInfoOverload.setLineColor("00FF00");
            plotInfoOverload.setLineWidth(1);

            ValueGeometry valueGeometry = getValueGeometry(getMaximunValueForChart());
            valueGeometry.setGridType("short");
            TimeGeometry timeGeometry = getTimeGeometry(interval);

            plotInfoLoad.setValueGeometry(valueGeometry);
            plotInfoMax.setValueGeometry(valueGeometry);
            plotInfoOverload.setValueGeometry(valueGeometry);

            plotInfoLoad.setTimeGeometry(timeGeometry);
            plotInfoMax.setTimeGeometry(timeGeometry);
            plotInfoOverload.setTimeGeometry(timeGeometry);

            chart.appendChild(plotInfoMax);
            chart.appendChild(plotInfoLoad);
            chart.appendChild(plotInfoOverload);

            size = size + (16 * 2);
            chart.setWidth(size + "px");
            chart.setHeight("100px");
        }

        private Plotinfo getLoadPlotInfo(Date start, Date finish) {
            List<DayAssignment> dayAssignments = dayAssignmentDAO
                    .list(DayAssignment.class);

            SortedMap<LocalDate, Map<Resource, Integer>> dayAssignmentGrouped = groupDayAssignmentsByDayAndResource(dayAssignments);
            SortedMap<LocalDate, Integer> mapDayAssignments = calculateHoursAdditionByDayWithoutOverload(dayAssignmentGrouped);

            String uri = getServletUri(mapDayAssignments, start, finish);

            PlotDataSource pds = new PlotDataSource();
            pds.setDataSourceUri(uri);
            pds.setSeparator(" ");

            Plotinfo plotInfo = new Plotinfo();
            plotInfo.setPlotDataSource(pds);

            return plotInfo;
        }

        private Plotinfo getOverloadPlotInfo(Date start, Date finish) {
            List<DayAssignment> dayAssignments = dayAssignmentDAO
                    .list(DayAssignment.class);

            SortedMap<LocalDate, Map<Resource, Integer>> dayAssignmentGrouped = groupDayAssignmentsByDayAndResource(dayAssignments);
            SortedMap<LocalDate, Integer> mapDayAssignments = calculateHoursAdditionByDayJustOverload(dayAssignmentGrouped);
            SortedMap<LocalDate, Integer> mapMaxAvailability = calculateHoursAdditionByDay(
                    resourceDAO.list(Resource.class), start, finish);

            for (LocalDate day : mapDayAssignments.keySet()) {
                if ((day.compareTo(new LocalDate(start)) >= 0)
                        && (day.compareTo(new LocalDate(finish)) <= 0)) {
                    Integer overloadHours = mapDayAssignments.get(day);
                    Integer maxHours = mapMaxAvailability.get(day);
                    mapDayAssignments.put(day, overloadHours + maxHours);
                }
            }

            String uri = getServletUri(mapDayAssignments, start, finish);

            PlotDataSource pds = new PlotDataSource();
            pds.setDataSourceUri(uri);
            pds.setSeparator(" ");

            Plotinfo plotInfo = new Plotinfo();
            plotInfo.setPlotDataSource(pds);

            return plotInfo;
        }

        private SortedMap<LocalDate, Integer> calculateHoursAdditionByDayWithoutOverload(
                SortedMap<LocalDate, Map<Resource, Integer>> dayAssignmentGrouped) {
            SortedMap<LocalDate, Integer> map = new TreeMap<LocalDate, Integer>();

            for (LocalDate day : dayAssignmentGrouped.keySet()) {
                int result = 0;

                for (Resource resource : dayAssignmentGrouped.get(day).keySet()) {
                    BaseCalendar calendar = resource.getCalendar();

                    int workableHours = SameWorkHoursEveryDay
                            .getDefaultWorkingDay().getWorkableHours(day);
                    if (calendar != null) {
                        workableHours = calendar.getWorkableHours(day);
                    }

                    int assignedHours = dayAssignmentGrouped.get(day).get(
                            resource);

                    if (assignedHours <= workableHours) {
                        result += assignedHours;
                    } else {
                        result += workableHours;
                    }
                }

                map.put(day, result);
            }

            return convertAsNeededByZoom(map);
        }

        private SortedMap<LocalDate, Integer> calculateHoursAdditionByDayJustOverload(
                SortedMap<LocalDate, Map<Resource, Integer>> dayAssignmentGrouped) {
            SortedMap<LocalDate, Integer> map = new TreeMap<LocalDate, Integer>();

            for (LocalDate day : dayAssignmentGrouped.keySet()) {
                int result = 0;

                for (Resource resource : dayAssignmentGrouped.get(day).keySet()) {
                    BaseCalendar calendar = resource.getCalendar();

                    int workableHours = SameWorkHoursEveryDay
                            .getDefaultWorkingDay().getWorkableHours(day);
                    if (calendar != null) {
                        workableHours = calendar.getWorkableHours(day);
                    }

                    int assignedHours = dayAssignmentGrouped.get(day).get(
                            resource);

                    if (assignedHours > workableHours) {
                        result += assignedHours - workableHours;
                    }
                }

                map.put(day, result);
            }

            return convertAsNeededByZoom(map);
        }

        private Plotinfo getCalendarMaximumAvailabilityPlotInfo(Date start,
                Date finish) {
            SortedMap<LocalDate, Integer> mapDayAssignments = calculateHoursAdditionByDay(
                    resourceDAO.list(Resource.class), start, finish);

            String uri = getServletUri(mapDayAssignments,
                    start, finish);

            PlotDataSource pds = new PlotDataSource();
            pds.setDataSourceUri(uri);
            pds.setSeparator(" ");

            Plotinfo plotInfo = new Plotinfo();
            plotInfo.setPlotDataSource(pds);

            return plotInfo;
        }

        private SortedMap<LocalDate, Integer> calculateHoursAdditionByDay(
                List<Resource> resources, Date start, Date finish) {
            return new HoursByDayCalculator<Entry<LocalDate, List<Resource>>>() {

                @Override
                protected LocalDate getDayFor(
                        Entry<LocalDate, List<Resource>> element) {
                    return element.getKey();
                }

                @Override
                protected int getHoursFor(
                        Entry<LocalDate, List<Resource>> element) {
                    LocalDate day = element.getKey();
                    List<Resource> resources = element.getValue();
                    return sumHoursForDay(resources, day);
                }

            }.calculate(getResourcesByDateBetween(
                    resources, start, finish));
        }

        private Set<Entry<LocalDate, List<Resource>>> getResourcesByDateBetween(
                List<Resource> resources, Date start, Date finish) {
            LocalDate end = new LocalDate(finish);
            Map<LocalDate, List<Resource>> result = new HashMap<LocalDate, List<Resource>>();
            for (LocalDate date = new LocalDate(start); date.compareTo(end) <= 0; date = date
                    .plusDays(1)) {
                result.put(date, resources);
            }
            return result.entrySet();
        }

    }

}
