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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
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

    private Integer maximunValueForChart = 0;

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

    private IZoomLevelChangedListener zoomListener;

    private void fillChartOnZoomChange(final Timeplot chartComponent,
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
        chart.setHeight("200px");
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
            printLine(writer, mapDayAssignments.firstKey().minusDays(1), 0);
        }
    }

    private void fillZeroValueToFinish(PrintWriter writer, Date finish,
            SortedMap<LocalDate, Integer> mapDayAssignments) {
        LocalDate day = new LocalDate(finish);
        if (mapDayAssignments.isEmpty()) {
            printLine(writer, day, 0);
        } else if (day.compareTo(mapDayAssignments.lastKey()) > 0) {
            printLine(writer, mapDayAssignments.lastKey().plusDays(1), 0);
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

                        for (LocalDate day : mapDayAssignments.keySet()) {
                            Integer hours = mapDayAssignments.get(day);
                            printLine(writer, day, hours);
                        }

                        fillZeroValueToFinish(writer, finish, mapDayAssignments);

                        writer.close();
                    }
                });
        return uri;
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
                } else {
                    hours += 8;
                }
            }

            map.put(date, hours);
        }

        return map;
    }

    private void setMaximunValueForChartIfGreater(Integer max) {
        if (maximunValueForChart < max) {
            maximunValueForChart = max;
        }
    }

}
