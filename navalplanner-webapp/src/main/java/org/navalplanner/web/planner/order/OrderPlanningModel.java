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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.hibernate.Hibernate;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.SameWorkHoursEveryDay;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.DerivedAllocation;
import org.navalplanner.business.planner.entities.ICostCalculator;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.TaskMilestone;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.workreports.daos.IWorkReportLineDAO;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.web.common.ViewSwitcher;
import org.navalplanner.web.planner.ITaskElementAdapter;
import org.navalplanner.web.planner.ITaskElementAdapter.IOnMoveListener;
import org.navalplanner.web.planner.allocation.IResourceAllocationCommand;
import org.navalplanner.web.planner.allocation.ResourceAllocationController;
import org.navalplanner.web.planner.calendar.CalendarAllocationController;
import org.navalplanner.web.planner.calendar.ICalendarAllocationCommand;
import org.navalplanner.web.planner.chart.Chart;
import org.navalplanner.web.planner.chart.ChartFiller;
import org.navalplanner.web.planner.chart.EarnedValueChartFiller;
import org.navalplanner.web.planner.chart.IChartFiller;
import org.navalplanner.web.planner.chart.EarnedValueChartFiller.EarnedValueType;
import org.navalplanner.web.planner.milestone.IAddMilestoneCommand;
import org.navalplanner.web.planner.milestone.IDeleteMilestoneCommand;
import org.navalplanner.web.planner.order.ISaveCommand.IAfterSaveListener;
import org.navalplanner.web.planner.taskedition.ITaskPropertiesCommand;
import org.navalplanner.web.planner.taskedition.TaskPropertiesController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkforge.timeplot.Plotinfo;
import org.zkforge.timeplot.Timeplot;
import org.zkforge.timeplot.geometry.TimeGeometry;
import org.zkforge.timeplot.geometry.ValueGeometry;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.adapters.PlannerConfiguration.IReloadChartListener;
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.timetracker.zoom.IDetailItemModificator;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Vbox;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public abstract class OrderPlanningModel implements IOrderPlanningModel {

    public static final String COLOR_CAPABILITY_LINE = "#000000"; // Black

    public static final String COLOR_ASSIGNED_LOAD_GLOBAL = "#98D471"; // Green
    public static final String COLOR_OVERLOAD_GLOBAL = "#FDBE13"; // Orange

    public static final String COLOR_ASSIGNED_LOAD_SPECIFIC = "#AA80d5"; // Violet
    public static final String COLOR_OVERLOAD_SPECIFIC = "#FF5A11"; // Red

    @Autowired
    private IOrderDAO orderDAO;

    private PlanningState planningState;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IWorkReportLineDAO workReportLineDAO;

    @Autowired
    private ITaskElementDAO taskDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    private List<IZoomLevelChangedListener> keepAliveZoomListeners = new ArrayList<IZoomLevelChangedListener>();

    private ITaskElementAdapter taskElementAdapter;

    @Autowired
    private ICostCalculator hoursCostCalculator;

    private List<Checkbox> earnedValueChartConfigurationCheckboxes = new ArrayList<Checkbox>();

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
            TaskPropertiesController taskPropertiesController,
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

        final IResourceAllocationCommand resourceAllocationCommand = buildResourceAllocationCommand(resourceAllocationController);
        configuration.addCommandOnTask(resourceAllocationCommand);
        configuration.addCommandOnTask(buildMilestoneCommand());
        configuration.addCommandOnTask(buildDeleteMilestoneCommand());
        configuration
                .addCommandOnTask(buildCalendarAllocationCommand(calendarAllocationController));
        configuration
                .addCommandOnTask(buildTaskPropertiesCommand(taskPropertiesController));
        configuration.setDoubleClickCommand(resourceAllocationCommand);
        Tabbox chartComponent = new Tabbox();
        chartComponent.setOrient("vertical");
        chartComponent.setHeight("200px");
        appendTabs(chartComponent);

        configuration.setChartComponent(chartComponent);
        showDeadlineIfExists(orderReloaded, configuration);
        planner.setConfiguration(configuration);

        Timeplot chartLoadTimeplot = new Timeplot();
        Timeplot chartEarnedValueTimeplot = new Timeplot();
        OrderEarnedValueChartFiller earnedValueChartFiller = new OrderEarnedValueChartFiller(
                orderReloaded);
        earnedValueChartFiller.calculateValues(planner.getTimeTracker()
                .getRealInterval());
        appendTabpanels(chartComponent, chartLoadTimeplot,
                chartEarnedValueTimeplot, earnedValueChartFiller);

        Chart loadChart = setupChart(orderReloaded,
                new OrderLoadChartFiller(orderReloaded), chartLoadTimeplot,
                planner.getTimeTracker());
        refillLoadChartWhenNeeded(configuration, planner, saveCommand,
                loadChart);
        Chart earnedValueChart = setupChart(orderReloaded,
                earnedValueChartFiller,
                chartEarnedValueTimeplot, planner.getTimeTracker());
        refillLoadChartWhenNeeded(configuration, planner, saveCommand,
                earnedValueChart);
        setEventListenerConfigurationCheckboxes(earnedValueChart);
    }

    private IDeleteMilestoneCommand buildDeleteMilestoneCommand() {
        IDeleteMilestoneCommand result = getDeleteMilestoneCommand();
        result.setState(planningState);
        return result;
    }

    private void showDeadlineIfExists(Order orderReloaded,
            PlannerConfiguration<TaskElement> configuration) {
        if (orderReloaded.getDeadline() != null) {
            configuration
                    .setSecondLevelModificators(createDeadlineShower(orderReloaded
                            .getDeadline()));
        }
    }

    private IDetailItemModificator createDeadlineShower(Date orderDeadline) {
        final DateTime deadline = new DateTime(orderDeadline);
        return new IDetailItemModificator() {

            @Override
            public DetailItem applyModificationsTo(DetailItem item) {
                item.markDeadlineDay(deadline);
                return item;
            }
        };
    }

    private void appendTabs(Tabbox chartComponent) {
        Tabs chartTabs = new Tabs();
        chartTabs.appendChild(new Tab(_("Load")));
        chartTabs.appendChild(new Tab(_("Earned value")));

        chartComponent.appendChild(chartTabs);
        chartTabs.setSclass("charts-tabbox");
    }

    private void appendTabpanels(Tabbox chartComponent, Timeplot loadChart,
            Timeplot chartEarnedValueTimeplot,
            OrderEarnedValueChartFiller earnedValueChartFiller) {
        Tabpanels chartTabpanels = new Tabpanels();

        Tabpanel loadChartPannel = new Tabpanel();
        appendLoadChartAndLegend(loadChartPannel, loadChart);
        chartTabpanels.appendChild(loadChartPannel);

        Tabpanel earnedValueChartPannel = new Tabpanel();
        appendEarnedValueChartAndLegend(earnedValueChartPannel,
                chartEarnedValueTimeplot, earnedValueChartFiller);
        chartTabpanels.appendChild(earnedValueChartPannel);

        chartComponent.appendChild(chartTabpanels);
    }

    private void appendLoadChartAndLegend(Tabpanel loadChartPannel,
            Timeplot loadChart) {
        Hbox hbox = new Hbox();
        hbox.appendChild(getLoadChartLegend());
        hbox.setSclass("load-chart");

        Div div = new Div();
        div.appendChild(loadChart);
        div.setSclass("plannergraph");
        hbox.appendChild(div);

        loadChartPannel.appendChild(hbox);
    }

    private org.zkoss.zk.ui.Component getLoadChartLegend() {
        Hbox hbox = new Hbox();
        hbox.setClass("legend-container");
        hbox.setAlign("center");
        hbox.setPack("center");
        Executions.createComponents("/planner/_legendLoadChartOrder.zul", hbox,
                null);
        return hbox;
    }

    private void appendEarnedValueChartAndLegend(
            Tabpanel earnedValueChartPannel, Timeplot chartEarnedValueTimeplot,
            OrderEarnedValueChartFiller earnedValueChartFiller) {
        Vbox vbox = new Vbox();
        vbox.setClass("legend-container");
        vbox.setAlign("center");
        vbox.setPack("center");

        Hbox dateHbox = new Hbox();
        dateHbox.appendChild(new Label(_("Select date:")));

        LocalDate date = new LocalDate();
        Datebox datebox = new Datebox(date.toDateTimeAtStartOfDay().toDate());
        dateHbox.appendChild(datebox);

        appendEventListenerToDateboxIndicators(earnedValueChartFiller, vbox,
                datebox);
        vbox.appendChild(dateHbox);

        vbox.appendChild(getEarnedValueChartConfigurableLegend(
                earnedValueChartFiller, date));

        Hbox hbox = new Hbox();
        hbox.setSclass("earned-value-chart");

        hbox.appendChild(vbox);

        Div div = new Div();
        div.appendChild(chartEarnedValueTimeplot);
        div.setSclass("plannergraph");

        hbox.appendChild(div);

        earnedValueChartPannel.appendChild(hbox);
    }

    private void appendEventListenerToDateboxIndicators(
            final OrderEarnedValueChartFiller earnedValueChartFiller,
            final Vbox vbox, final Datebox datebox) {
        datebox.addEventListener(Events.ON_CHANGE, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                LocalDate date = new LocalDate(datebox.getValue());
                org.zkoss.zk.ui.Component child = vbox
                        .getFellow("indicatorsTable");
                vbox.removeChild(child);
                vbox.appendChild(getEarnedValueChartConfigurableLegend(
                        earnedValueChartFiller, date));
            }

        });
    }

    private org.zkoss.zk.ui.Component getEarnedValueChartConfigurableLegend(
            OrderEarnedValueChartFiller earnedValueChartFiller, LocalDate date) {
        Hbox mainhbox = new Hbox();
        mainhbox.setId("indicatorsTable");

        Vbox vbox = new Vbox();
        vbox.setId("earnedValueChartConfiguration");
        vbox.setClass("legend");

        Vbox column1 = new Vbox();
        Vbox column2 = new Vbox();
        column1.setSclass("earned-parameter-column");
        column2.setSclass("earned-parameter-column");

        int columnNumber = 0;

        for (EarnedValueType type : EarnedValueType.values()) {
            Checkbox checkbox = new Checkbox(type.getAcronym());
            checkbox.setTooltiptext(type.getName());
            checkbox.setAttribute("indicator", type);
            checkbox.setStyle("color: " + type.getColor());

            BigDecimal value = earnedValueChartFiller.getIndicator(type, date);
            String units = _("h");
            if (type.equals(EarnedValueType.CPI)
                    || type.equals(EarnedValueType.SPI)) {
                value = value.multiply(new BigDecimal(100));
                units = "%";
            }
            Label valueLabel = new Label(value.intValue() + " " + units);

            Hbox hbox = new Hbox();
            hbox.appendChild(checkbox);
            hbox.appendChild(valueLabel);

            columnNumber = columnNumber + 1;
            switch (columnNumber) {
            case 1:
                column1.appendChild(hbox);
                break;
            case 2:
                column2.appendChild(hbox);
                columnNumber = 0;
            }
            earnedValueChartConfigurationCheckboxes.add(checkbox);

        }

        Hbox hbox = new Hbox();
        hbox.appendChild(column1);
        hbox.appendChild(column2);

        vbox.appendChild(hbox);
        mainhbox.appendChild(vbox);

        markAsSelectedDefaultIndicators();

        return mainhbox;
    }

    private void markAsSelectedDefaultIndicators() {
        for (Checkbox checkbox : earnedValueChartConfigurationCheckboxes) {
            EarnedValueType type = (EarnedValueType) checkbox
                    .getAttribute("indicator");
            switch (type) {
            case BCWS:
            case ACWP:
            case BCWP:
                checkbox.setChecked(true);
                break;

            default:
                checkbox.setChecked(false);
                break;
            }
        }
    }

    private Set<EarnedValueType> getEarnedValueSelectedIndicators() {
        Set<EarnedValueType> result = new HashSet<EarnedValueType>();
        for (Checkbox checkbox : earnedValueChartConfigurationCheckboxes) {
            if (checkbox.isChecked()) {
                EarnedValueType type = (EarnedValueType) checkbox
                        .getAttribute("indicator");
                result.add(type);
            }
        }
        return result;
    }

    private void setEventListenerConfigurationCheckboxes(
            final Chart earnedValueChart) {
        for (Checkbox checkbox : earnedValueChartConfigurationCheckboxes) {
            checkbox.addEventListener(Events.ON_CHECK, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    transactionService
                            .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
                                @Override
                                public Void execute() {
                                    earnedValueChart.fillChart();
                                    return null;
                                }
                            });
                }

            });
        }
    }

    private void refillLoadChartWhenNeeded(
            PlannerConfiguration<?> configuration, Planner planner,
            ISaveCommand saveCommand, final Chart loadChart) {
        planner.getTimeTracker().addZoomListener(fillOnZoomChange(loadChart));
        saveCommand.addListener(fillChartOnSave(loadChart));
        taskElementAdapter.addListener(new IOnMoveListener() {
            @Override
            public void moved(TaskElement taskElement) {
                loadChart.fillChart();
            }
        });
        configuration.addReloadChartListener(new IReloadChartListener() {

            @Override
            public void reloadChart() {
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

    private ITaskPropertiesCommand buildTaskPropertiesCommand(
            TaskPropertiesController taskPropertiesController) {
        ITaskPropertiesCommand taskPropertiesCommand = getTaskPropertiesCommand();
        taskPropertiesCommand
                .setTaskPropertiesController(taskPropertiesController);
        return taskPropertiesCommand;
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

    private Chart setupChart(Order orderReloaded,
            IChartFiller loadChartFiller, Timeplot chartComponent,
            TimeTracker timeTracker) {
        Chart result = new Chart(chartComponent, loadChartFiller,
                timeTracker);
        result.fillChart();
        return result;
    }

    private IZoomLevelChangedListener fillOnZoomChange(final Chart loadChart) {
        IZoomLevelChangedListener zoomListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                loadChart.setZoomLevel(detailLevel);

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

        keepAliveZoomListeners.add(zoomListener);

        return zoomListener;
    }

    private IAfterSaveListener fillChartOnSave(final Chart loadChart) {
        IAfterSaveListener result = new IAfterSaveListener() {

                    @Override
            public void onAfterSave() {
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
        return result;
    }

    private PlannerConfiguration<TaskElement> createConfiguration(
            Order orderReloaded) {
        taskElementAdapter = getTaskElementAdapter();
        taskElementAdapter.setOrder(orderReloaded);
        TaskGroup taskElement = orderReloaded
                .getAssociatedTaskElement();
        final List<Resource> allResources = resourceDAO.list(Resource.class);
        forceLoadOfChildren(Arrays.asList(taskElement));
        planningState = new PlanningState(taskElement, orderReloaded
                .getAssociatedTasks(), allResources);
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

    private void forceLoadOfChildren(Collection<? extends TaskElement> initial) {
        for (TaskElement each : initial) {
            forceLoadOfResourceAllocationsResources(each);
            if (each instanceof TaskGroup) {
                findChildrenWithQueryToAvoidProxies((TaskGroup) each);
                List<TaskElement> children = each.getChildren();
                forceLoadOfChildren(children);
            }
        }
    }

    /**
     * Forcing the load of all resources so the resources at planning state and
     * at allocations are the same
     */
    private void forceLoadOfResourceAllocationsResources(TaskElement taskElement) {
        Set<ResourceAllocation<?>> resourceAllocations = taskElement
                .getResourceAllocations();
        for (ResourceAllocation<?> each : resourceAllocations) {
            each.getAssociatedResources();
            for (DerivedAllocation eachDerived : each.getDerivedAllocations()) {
                eachDerived.getResources();
            }
        }
    }

    private void findChildrenWithQueryToAvoidProxies(TaskGroup group) {
        for (TaskElement eachTask : taskDAO.findChildrenOf(group)) {
            Hibernate.initialize(eachTask);
        }
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

    protected abstract IDeleteMilestoneCommand getDeleteMilestoneCommand();

    protected abstract ITaskPropertiesCommand getTaskPropertiesCommand();

    protected abstract ICalendarAllocationCommand getCalendarAllocationCommand();

    private Order reload(Order order) {
        try {
            return orderDAO.find(order.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private class OrderLoadChartFiller extends ChartFiller {

        private final Order order;

        private SortedMap<LocalDate, BigDecimal> mapOrderLoad = new TreeMap<LocalDate, BigDecimal>();
        private SortedMap<LocalDate, BigDecimal> mapOrderOverload = new TreeMap<LocalDate, BigDecimal>();
        private SortedMap<LocalDate, BigDecimal> mapMaxCapacity = new TreeMap<LocalDate, BigDecimal>();
        private SortedMap<LocalDate, BigDecimal> mapOtherLoad = new TreeMap<LocalDate, BigDecimal>();
        private SortedMap<LocalDate, BigDecimal> mapOtherOverload = new TreeMap<LocalDate, BigDecimal>();

        public OrderLoadChartFiller(Order orderReloaded) {
            this.order = orderReloaded;
        }

        @Override
        public void fillChart(Timeplot chart, Interval interval, Integer size) {
            chart.getChildren().clear();
            chart.invalidate();

            String javascript = "zkTasklist.timeplotcontainer_rescroll();";
            Clients.evalJavaScript(javascript);

            resetMinimumAndMaximumValueForChart();

            List<DayAssignment> orderDayAssignments = order.getDayAssignments();
            SortedMap<LocalDate, Map<Resource, Integer>> orderDayAssignmentsGrouped = groupDayAssignmentsByDayAndResource(orderDayAssignments);

            List<DayAssignment> resourcesDayAssignments = new ArrayList<DayAssignment>();
            for (Resource resource : order.getResources()) {
                resourcesDayAssignments.addAll(resource.getAssignments());
            }
            SortedMap<LocalDate, Map<Resource, Integer>> resourceDayAssignmentsGrouped = groupDayAssignmentsByDayAndResource(resourcesDayAssignments);

            fillMaps(orderDayAssignmentsGrouped, resourceDayAssignmentsGrouped);
            convertAsNeededByZoomMaps();

            Plotinfo plotOrderLoad = createPlotinfo(
                    mapOrderLoad, interval);
            Plotinfo plotOrderOverload = createPlotinfo(
                    mapOrderOverload,
                    interval);
            Plotinfo plotMaxCapacity = createPlotinfo(
                    mapMaxCapacity, interval);
            Plotinfo plotOtherLoad = createPlotinfo(
                    mapOtherLoad, interval);
            Plotinfo plotOtherOverload = createPlotinfo(
                    mapOtherOverload,
                    interval);

            plotOrderLoad.setFillColor(COLOR_ASSIGNED_LOAD_SPECIFIC);
            plotOrderLoad.setLineWidth(0);

            plotOtherLoad.setFillColor(COLOR_ASSIGNED_LOAD_GLOBAL);
            plotOtherLoad.setLineWidth(0);

            plotMaxCapacity.setLineColor(COLOR_CAPABILITY_LINE);
            plotMaxCapacity.setFillColor("#FFFFFF");
            plotMaxCapacity.setLineWidth(2);

            plotOrderOverload.setFillColor(COLOR_OVERLOAD_SPECIFIC);
            plotOrderOverload.setLineWidth(0);

            plotOtherOverload.setFillColor(COLOR_OVERLOAD_GLOBAL);
            plotOtherOverload.setLineWidth(0);

            ValueGeometry valueGeometry = getValueGeometry();
            TimeGeometry timeGeometry = getTimeGeometry(interval);

            // Stacked area: load - otherLoad - max - overload - otherOverload
            appendPlotinfo(chart, plotOrderLoad, valueGeometry, timeGeometry);
            appendPlotinfo(chart, plotOtherLoad, valueGeometry, timeGeometry);
            appendPlotinfo(chart, plotMaxCapacity, valueGeometry, timeGeometry);
            appendPlotinfo(chart, plotOrderOverload, valueGeometry,
                    timeGeometry);
            appendPlotinfo(chart, plotOtherOverload, valueGeometry,
                    timeGeometry);

            chart.setWidth(size + "px");
            chart.setHeight("150px");
        }

        private void convertAsNeededByZoomMaps() {
            mapOrderLoad = convertAsNeededByZoom(mapOrderLoad);
            mapOrderOverload = convertAsNeededByZoom(mapOrderOverload);
            mapMaxCapacity = convertAsNeededByZoom(mapMaxCapacity);
            mapOtherLoad = convertAsNeededByZoom(mapOtherLoad);
            mapOtherOverload = convertAsNeededByZoom(mapOtherOverload);
        }

        private void fillMaps(
                SortedMap<LocalDate, Map<Resource, Integer>> orderDayAssignmentsGrouped,
                SortedMap<LocalDate, Map<Resource, Integer>> resourceDayAssignmentsGrouped) {

            for (LocalDate day : orderDayAssignmentsGrouped.keySet()) {
                Integer maxCapacity = getMaxCapcity(orderDayAssignmentsGrouped,
                        day);
                mapMaxCapacity.put(day, new BigDecimal(maxCapacity));

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

                mapOrderLoad.put(day, new BigDecimal(orderLoad));
                mapOrderOverload.put(day, new BigDecimal(orderOverload
                        + maxCapacity));
                mapOtherLoad.put(day, new BigDecimal(otherLoad + orderLoad));
                mapOtherOverload.put(day, new BigDecimal(otherOverload
                        + orderOverload + maxCapacity));
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
                    .getCapacityAt(day);
            if (calendar != null) {
                workableHours = calendar.getCapacityAt(day);
            }

            return workableHours;
        }

    }

    private class OrderEarnedValueChartFiller extends EarnedValueChartFiller {

        private Order order;

        public OrderEarnedValueChartFiller(Order orderReloaded) {
            this.order = orderReloaded;
        }

        protected void calculateBudgetedCostWorkScheduled(Interval interval) {
            List<TaskElement> list = order
                    .getAllChildrenAssociatedTaskElements();
            list.add(order.getAssociatedTaskElement());

            SortedMap<LocalDate, BigDecimal> estimatedCost = new TreeMap<LocalDate, BigDecimal>();

            for (TaskElement taskElement : list) {
                if (taskElement instanceof Task) {
                    addCost(estimatedCost, hoursCostCalculator
                            .getEstimatedCost((Task) taskElement));
                }
            }

            estimatedCost = accumulateResult(estimatedCost);
            addZeroBeforeTheFirstValue(estimatedCost);
            indicators.put(EarnedValueType.BCWS, calculatedValueForEveryDay(
                    estimatedCost, interval.getStart(), interval.getFinish()));
        }

        protected void calculateActualCostWorkPerformed(Interval interval) {
            SortedMap<LocalDate, BigDecimal> workReportCost = getWorkReportCost();

            workReportCost = accumulateResult(workReportCost);
            addZeroBeforeTheFirstValue(workReportCost);
            indicators.put(EarnedValueType.ACWP, calculatedValueForEveryDay(
                    workReportCost, interval.getStart(), interval.getFinish()));
        }

        public SortedMap<LocalDate, BigDecimal> getWorkReportCost() {
            SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();

            List<WorkReportLine> workReportLines = workReportLineDAO
                    .findByOrderElementAndChildren(order);

            if (workReportLines.isEmpty()) {
                return result;
            }

            for (WorkReportLine workReportLine : workReportLines) {
                LocalDate day = new LocalDate(workReportLine.getWorkReport()
                        .getDate());
                BigDecimal cost = new BigDecimal(workReportLine.getNumHours());

                if (!result.containsKey(day)) {
                    result.put(day, BigDecimal.ZERO);
                }
                result.put(day, result.get(day).add(cost));
            }

            return result;
        }

        protected void calculateBudgetedCostWorkPerformed(Interval interval) {
            List<TaskElement> list = order
                    .getAllChildrenAssociatedTaskElements();
            list.add(order.getAssociatedTaskElement());

            SortedMap<LocalDate, BigDecimal> advanceCost = new TreeMap<LocalDate, BigDecimal>();

            for (TaskElement taskElement : list) {
                if (taskElement instanceof Task) {
                    addCost(advanceCost, hoursCostCalculator
                            .getAdvanceCost((Task) taskElement));
                }
            }

            advanceCost = accumulateResult(advanceCost);
            addZeroBeforeTheFirstValue(advanceCost);
            indicators.put(EarnedValueType.BCWP, calculatedValueForEveryDay(
                    advanceCost, interval.getStart(), interval.getFinish()));
        }

        @Override
        protected Set<EarnedValueType> getSelectedIndicators() {
            return getEarnedValueSelectedIndicators();
        }

    }

}
