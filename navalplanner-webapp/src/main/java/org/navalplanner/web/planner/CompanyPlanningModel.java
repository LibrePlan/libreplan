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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
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

    private ILoadChartFiller loadChartFiller = new CompanyLoadChartFiller();

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

        configuration.setAddingDependenciesEnabled(false);

        configuration.setChartLegend(getChartLegend());

        planner.setConfiguration(configuration);

        setupChart(chartComponent, planner.getTimeTracker());
    }

    private void addAdditionalCommands(
            Collection<ICommandOnTask<TaskElement>> additional,
            PlannerConfiguration<TaskElement> configuration) {
        for (ICommandOnTask<TaskElement> t : additional) {
            configuration.addCommandOnTask(t);
        }
    }

    private void setupChart(Timeplot chartComponent, TimeTracker timeTracker) {
        loadChartFiller.fillChart(chartComponent,
                timeTracker.getRealInterval(), timeTracker
                .getHorizontalSize());
        fillChartOnZoomChange(chartComponent, timeTracker);
    }

    private void fillChartOnZoomChange(final Timeplot chartComponent,
            final TimeTracker timeTracker) {

        zoomListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(final ZoomLevel detailLevel) {
                loadChartFiller.setZoomLevel(detailLevel);

                transactionService
                        .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
                    @Override
                    public Void execute() {
                        loadChartFiller.fillChart(chartComponent,
                                        timeTracker.getRealInterval(),
                                        timeTracker.getHorizontalSize());
                        return null;
                    }
                });
            }
        };

        timeTracker.addZoomListener(zoomListener);
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

        if (loadChartFiller.zoomByDay()) {
            return map;
        } else {
            return loadChartFiller.groupByWeek(map);
        }
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
                } else {
                    hours += SameWorkHoursEveryDay.getDefaultWorkingDay()
                            .getWorkableHours(date);
                }
            }

            map.put(date, hours);
        }

        if (loadChartFiller.zoomByDay()) {
            return map;
        } else {
            return loadChartFiller.groupByWeek(map);
        }
    }

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

            ValueGeometry valueGeometry = new DefaultValueGeometry();
            valueGeometry.setMin(0);
            valueGeometry.setMax(getMaximunValueForChart());
            valueGeometry.setGridColor("#000000");
            valueGeometry.setAxisLabelsPlacement("left");

            plotInfoLoad.setValueGeometry(valueGeometry);
            plotInfoMax.setValueGeometry(valueGeometry);

            chart.appendChild(plotInfoMax);
            chart.appendChild(plotInfoLoad);

            size = size + (16 * 2);
            chart.setWidth(size + "px");
            chart.setHeight("100px");
        }

        private Plotinfo getLoadPlotInfo(Date start, Date finish) {
            List<DayAssignment> dayAssignments = dayAssignmentDAO
                    .list(DayAssignment.class);
            SortedMap<LocalDate, Integer> mapDayAssignments = calculateHoursAdditionByDay(dayAssignments);

            String uri = getServletUri(mapDayAssignments,
                    start, finish);

            PlotDataSource pds = new PlotDataSource();
            pds.setDataSourceUri(uri);
            pds.setSeparator(" ");

            Plotinfo plotInfo = new Plotinfo();
            plotInfo.setPlotDataSource(pds);

            return plotInfo;
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

    }

}
