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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.navalplanner.web.servlets.CallbackServlet;
import org.navalplanner.web.servlets.CallbackServlet.IServletRequestHandler;
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

    /**
     * Number of days to Thursday since the beginning of the week. In order to
     * calculate the middle of a week.
     */
    private final static int DAYS_TO_THURSDAY = 3;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IDayAssignmentDAO dayAssignmentDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    private Integer maximunValueForChart = 0;

    private IZoomLevelChangedListener zoomListener;

    private ZoomLevel zoomLevel = ZoomLevel.DETAIL_ONE;

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
        fillChart(chartComponent, timeTracker.getRealInterval(), timeTracker
                .getHorizontalSize());
        fillChartOnZoomChange(chartComponent, timeTracker);
    }

    private void fillChartOnZoomChange(final Timeplot chartComponent,
            final TimeTracker timeTracker) {

        zoomListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(final ZoomLevel detailLevel) {
                zoomLevel = detailLevel;

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

    private void fillChart(Timeplot chart, Interval interval, Integer size) {
        chart.getChildren().clear();
        chart.invalidate();
        maximunValueForChart = 0;

        Plotinfo plotInfoLoad = getLoadPlotInfo(interval.getStart(), interval
                .getFinish());
        plotInfoLoad.setFillColor("0000FF");

        Plotinfo plotInfoMax = getCalendarMaximumAvailabilityPlotInfo(interval
                .getStart(), interval.getFinish());
        plotInfoMax.setLineColor("FF0000");

        ValueGeometry valueGeometry = new DefaultValueGeometry();
        valueGeometry.setMin(0);
        valueGeometry.setMax(maximunValueForChart);
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
        List<DayAssignment> dayAssignments = dayAssignmentDAO.list(DayAssignment.class);
        SortedMap<LocalDate, Integer> mapDayAssignments = calculateHoursAdditionByDay(dayAssignments);

        String uri = getServletUri(mapDayAssignments, start, finish);

        PlotDataSource pds = new PlotDataSource();
        pds.setDataSourceUri(uri);
        pds.setSeparator(" ");

        Plotinfo plotInfo = new Plotinfo();
        plotInfo.setPlotDataSource(pds);

        return plotInfo;
    }

    private void printLine(PrintWriter writer, LocalDate day, Integer hours) {
        writer.println(day.toString("yyyyMMdd") + " " + hours);
    }

    private void fillZeroValueFromStart(PrintWriter writer, Date start,
            SortedMap<LocalDate, Integer> mapDayAssignments) {
        LocalDate day = new LocalDate(start);
        if (mapDayAssignments.isEmpty()) {
            printLine(writer, day, 0);
        } else if (day.compareTo(mapDayAssignments.firstKey()) < 0) {
            printLine(writer, day, 0);
            if (!day.equals(mapDayAssignments.firstKey().minusDays(1))) {
                printLine(writer, mapDayAssignments.firstKey().minusDays(1), 0);
            }
        }
    }

    private void fillZeroValueToFinish(PrintWriter writer, Date finish,
            SortedMap<LocalDate, Integer> mapDayAssignments) {
        LocalDate day = new LocalDate(finish);
        if (mapDayAssignments.isEmpty()) {
            printLine(writer, day, 0);
        } else if (day.compareTo(mapDayAssignments.lastKey()) > 0) {
            if (!day.equals(mapDayAssignments.lastKey().plusDays(1))) {
                printLine(writer, mapDayAssignments.lastKey().plusDays(1), 0);
            }
            printLine(writer, day, 0);
        }
    }

    private Plotinfo getCalendarMaximumAvailabilityPlotInfo(Date start,
            Date finish) {
        SortedMap<LocalDate, Integer> mapDayAssignments = calculateHoursAdditionByDay(
                resourceDAO.list(Resource.class), start, finish);

        String uri = getServletUri(mapDayAssignments, start, finish);

        PlotDataSource pds = new PlotDataSource();
        pds.setDataSourceUri(uri);
        pds.setSeparator(" ");

        Plotinfo plotInfo = new Plotinfo();
        plotInfo.setPlotDataSource(pds);

        return plotInfo;
    }

    private String getServletUri(
            final SortedMap<LocalDate, Integer> mapDayAssignments,
            final Date start, final Date finish) {
        if (mapDayAssignments.isEmpty()) {
            return "";
        }

        setMaximunValueForChartIfGreater(Collections.max(mapDayAssignments.values()));

        String uri = CallbackServlet
                .registerAndCreateURLFor(new IServletRequestHandler() {

                    @Override
                    public void handle(HttpServletRequest request,
                            HttpServletResponse response)
                            throws ServletException, IOException {
                        PrintWriter writer = response.getWriter();

                        fillZeroValueFromStart(writer, start, mapDayAssignments);

                        LocalDate firstDay = firstDay(mapDayAssignments);
                        LocalDate lastDay = lastDay(mapDayAssignments);

                        for (LocalDate day = firstDay; day.compareTo(lastDay) <= 0; day = nextDay(day)) {
                            Integer hours = mapDayAssignments.get(day) != null ? mapDayAssignments
                                    .get(day)
                                    : 0;
                            printLine(writer, day, hours);
                        }

                        fillZeroValueToFinish(writer, finish, mapDayAssignments);

                        writer.close();
                    }
                });
        return uri;
    }

    private LocalDate firstDay(SortedMap<LocalDate, Integer> mapDayAssignments) {
        LocalDate date = mapDayAssignments.firstKey();
        if (zoomByDay()) {
            return date;
        } else {
            return date.dayOfWeek().withMinimumValue().plusDays(
                    DAYS_TO_THURSDAY);
        }
    }

    private LocalDate lastDay(SortedMap<LocalDate, Integer> mapDayAssignments) {
        LocalDate date = mapDayAssignments.lastKey();
        if (zoomByDay()) {
            return date;
        } else {
            return date.dayOfWeek().withMinimumValue().plusDays(
                    DAYS_TO_THURSDAY);
        }
    }

    private LocalDate nextDay(LocalDate date) {
        if (zoomByDay()) {
            return date;
        } else {
            return date.plusWeeks(1);
        }
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

        if (zoomLevel.equals(ZoomLevel.DETAIL_FIVE)) {
            return map;
        } else {
            return groupByWeek(map);
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

        if (zoomLevel.equals(ZoomLevel.DETAIL_FIVE)) {
            return map;
        } else {
            return groupByWeek(map);
        }
    }

    private SortedMap<LocalDate, Integer> groupByWeek(
            SortedMap<LocalDate, Integer> map) {
        SortedMap<LocalDate, Integer> result = new TreeMap<LocalDate, Integer>();

        for (LocalDate day : map.keySet()) {
            LocalDate key = getKey(day);

            if (result.get(key) == null) {
                result.put(key, map.get(day));
            } else {
                result.put(key, result.get(key) + map.get(day));
            }
        }

        for (LocalDate day : result.keySet()) {
            result.put(day, result.get(day) / 7);
        }

        return result;
    }

    private LocalDate getKey(LocalDate date) {
        return date.dayOfWeek().withMinimumValue().plusDays(3);
    }

    private void setMaximunValueForChartIfGreater(Integer max) {
        if (maximunValueForChart < max) {
            maximunValueForChart = max;
        }
    }

    private org.zkoss.zk.ui.Component getChartLegend() {
        Div div = new Div();

        Executions.createComponents("/planner/_legendCompanyPlanner.zul", div,
                null);

        return div;
    }

    private boolean zoomByDay() {
        return zoomLevel.equals(ZoomLevel.DETAIL_FIVE);
    }

}
