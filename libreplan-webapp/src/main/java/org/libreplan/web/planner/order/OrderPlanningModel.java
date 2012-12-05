/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.web.planner.order;

import static org.libreplan.business.planner.chart.ContiguousDaysLine.min;
import static org.libreplan.business.planner.chart.ContiguousDaysLine.sum;
import static org.libreplan.business.planner.chart.ContiguousDaysLine.toSortedMap;
import static org.libreplan.web.I18nHelper._;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.libreplan.business.common.AdHocTransactionService;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderStatusEnum;
import org.libreplan.business.planner.chart.ContiguousDaysLine;
import org.libreplan.business.planner.entities.ICostCalculator;
import org.libreplan.business.planner.entities.IOrderEarnedValueCalculator;
import org.libreplan.business.planner.entities.IOrderResourceLoadCalculator;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.resources.entities.CriterionSatisfaction;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.users.daos.IOrderAuthorizationDAO;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.OrderAuthorization;
import org.libreplan.business.users.entities.OrderAuthorizationType;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.web.calendars.BaseCalendarModel;
import org.libreplan.web.common.ViewSwitcher;
import org.libreplan.web.planner.adaptplanning.IAdaptPlanningCommand;
import org.libreplan.web.planner.advances.AdvanceAssignmentPlanningController;
import org.libreplan.web.planner.advances.IAdvanceAssignmentPlanningCommand;
import org.libreplan.web.planner.allocation.IAdvancedAllocationCommand;
import org.libreplan.web.planner.allocation.IResourceAllocationCommand;
import org.libreplan.web.planner.calendar.CalendarAllocationController;
import org.libreplan.web.planner.calendar.ICalendarAllocationCommand;
import org.libreplan.web.planner.chart.Chart;
import org.libreplan.web.planner.chart.EarnedValueChartFiller;
import org.libreplan.web.planner.chart.EarnedValueChartFiller.EarnedValueType;
import org.libreplan.web.planner.chart.IChartFiller;
import org.libreplan.web.planner.chart.LoadChartFiller;
import org.libreplan.web.planner.consolidations.AdvanceConsolidationController;
import org.libreplan.web.planner.consolidations.IAdvanceConsolidationCommand;
import org.libreplan.web.planner.milestone.IAddMilestoneCommand;
import org.libreplan.web.planner.milestone.IDeleteMilestoneCommand;
import org.libreplan.web.planner.order.ISaveCommand.IAfterSaveListener;
import org.libreplan.web.planner.order.PlanningStateCreator.IActionsOnRetrieval;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.libreplan.web.planner.reassign.IReassignCommand;
import org.libreplan.web.planner.taskedition.AdvancedAllocationTaskController;
import org.libreplan.web.planner.taskedition.EditTaskController;
import org.libreplan.web.planner.taskedition.ITaskPropertiesCommand;
import org.libreplan.web.print.CutyPrint;
import org.libreplan.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkforge.timeplot.Plotinfo;
import org.zkforge.timeplot.Timeplot;
import org.zkoss.ganttz.IChartVisibilityChangedListener;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.adapters.PlannerConfiguration.IPrintAction;
import org.zkoss.ganttz.adapters.PlannerConfiguration.IReloadChartListener;
import org.zkoss.ganttz.data.GanttDiagramGraph.IGraphChangeListener;
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.extensions.ICommandOnTask;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.timetracker.zoom.IDetailItemModificator;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.SeveralModificators;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.ganttz.util.ProfilingLogFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Vbox;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderPlanningModel implements IOrderPlanningModel {

    private static final Log LOG = LogFactory.getLog(OrderPlanningModel.class);

    private static final Log PROFILING_LOG = ProfilingLogFactory
            .getLog(OrderPlanningModel.class);

    public static <T extends Collection<Resource>> T loadRequiredDataFor(
            T resources) {
        for (Resource each : resources) {
            reattachCalendarFor(each);
            // loading criterions so there are no repeated instances
            forceLoadOfCriterions(each);
        }
        return resources;
    }

    private static void reattachCalendarFor(Resource each) {
        if (each.getCalendar() != null) {
            BaseCalendarModel.forceLoadBaseCalendar(each.getCalendar());
        }
    }

    static void forceLoadOfCriterions(Resource resource) {
        Set<CriterionSatisfaction> criterionSatisfactions = resource
                .getCriterionSatisfactions();
        for (CriterionSatisfaction each : criterionSatisfactions) {
            each.getCriterion().getName();
            each.getCriterion().getType();
        }
    }

    public static void configureInitialZoomLevelFor(Planner planner,
            ZoomLevel defaultZoomLevel) {
        if (!planner.isFixedZoomByUser()) {
            planner.setInitialZoomLevel(defaultZoomLevel);
        }
    }

    public static ZoomLevel calculateDefaultLevel(
            PlannerConfiguration<TaskElement> configuration) {
        if (configuration.getData().isEmpty()) {
            return ZoomLevel.DETAIL_ONE;
        }
        TaskElement earliest = Collections.min(configuration.getData(),
                TaskElement
                .getByStartDateComparator());
        TaskElement latest = Collections.max(configuration.getData(),
                TaskElement.getByEndAndDeadlineDateComparator());

        LocalDate startDate = earliest.getStartAsLocalDate();
        LocalDate endDate = latest.getBiggestAmongEndOrDeadline();
        return ZoomLevel.getDefaultZoomByDates(startDate, endDate);
    }

    @Autowired
    private IOrderDAO orderDAO;

    private PlanningState planningState;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IOrderAuthorizationDAO orderAuthorizationDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private IReassignCommand reassignCommand;

    @Autowired
    private IAdaptPlanningCommand adaptPlanningCommand;

    @Autowired
    private IResourceAllocationCommand resourceAllocationCommand;

    @Autowired
    private IAdvancedAllocationCommand advancedAllocationCommand;

    @Autowired
    private IAddMilestoneCommand addMilestoneCommand;

    @Autowired
    private IDeleteMilestoneCommand deleteMilestoneCommand;

    @Autowired
    private ITaskPropertiesCommand taskPropertiesCommand;

    @Autowired
    private IAdvanceConsolidationCommand advanceConsolidationCommand;

    @Autowired
    private IAdvanceAssignmentPlanningCommand advanceAssignmentPlanningCommand;

    @Autowired
    private ICalendarAllocationCommand calendarAllocationCommand;

    @Autowired
    private ISubcontractCommand subcontractCommand;

    private List<IZoomLevelChangedListener> keepAliveZoomListeners = new ArrayList<IZoomLevelChangedListener>();

    @Autowired
    private ICostCalculator hoursCostCalculator;

    @Autowired
    private IOrderEarnedValueCalculator earnedValueCalculator;

    @Autowired
    private IOrderResourceLoadCalculator resourceLoadCalculator;

    private List<Checkbox> earnedValueChartConfigurationCheckboxes = new ArrayList<Checkbox>();

    private List<IChartVisibilityChangedListener> keepAliveChartVisibilityListeners = new ArrayList<IChartVisibilityChangedListener>();

    private Planner planner;

    private String tabSelected = "load_tab";

    private static class NullSeparatorCommandOnTask<T> implements
            ICommandOnTask<T> {

        @Override
        public String getName() {
            return "separator";
        }

        @Override
        public String getIcon() {
            return null;
        }

        @Override
        public boolean isApplicableTo(T task) {
            return true;
        }

        @Override
        public void doAction(IContextWithPlannerTask<T> context, T task) {
            // Do nothing
        }

    }

    @Override
    @Transactional(readOnly = true)
    public void setConfigurationToPlanner(final Planner planner, Order order,
            ViewSwitcher switcher,
            EditTaskController editTaskController,
            AdvancedAllocationTaskController advancedAllocationTaskController,
            AdvanceAssignmentPlanningController advanceAssignmentPlanningController,
            AdvanceConsolidationController advanceConsolidationController,
            CalendarAllocationController calendarAllocationController,
            List<ICommand<TaskElement>> additional) {
        long time = System.currentTimeMillis();
        this.planner = planner;
        planningState = createPlanningStateFor(order);
        PlannerConfiguration<TaskElement> configuration = planningState
                .getConfiguration();
        PROFILING_LOG.debug("load data and create configuration took: "
                + (System.currentTimeMillis() - time) + " ms");
        User user;
        try {
            user = this.userDAO.findByLoginName(SecurityUtils
                    .getSessionUserLoginName());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        configuration.setExpandPlanningViewCharts(user
                .isExpandOrderPlanningViewCharts());
        addAdditional(additional, configuration);

        ZoomLevel defaultZoomLevel = OrderPlanningModel
                .calculateDefaultLevel(configuration);
        configureInitialZoomLevelFor(planner, defaultZoomLevel);

        final boolean writingAllowed = isWritingAllowedOnOrder();
        ISaveCommand saveCommand = setupSaveCommand(configuration,
                writingAllowed);
        setupEditingCapabilities(configuration, writingAllowed);

        configuration.addGlobalCommand(buildReassigningCommand());
        configuration.addGlobalCommand(buildCancelEditionCommand());
        configuration.addGlobalCommand(buildAdaptPlanningCommand());

        NullSeparatorCommandOnTask<TaskElement> separator = new NullSeparatorCommandOnTask<TaskElement>();

        final IResourceAllocationCommand resourceAllocationCommand = buildResourceAllocationCommand(editTaskController);

        final IAdvanceAssignmentPlanningCommand advanceAssignmentPlanningCommand = buildAdvanceAssignmentPlanningCommand(advanceAssignmentPlanningController);

        // Build context menu
        configuration.addCommandOnTask(buildMilestoneCommand());
        configuration.addCommandOnTask(buildDeleteMilestoneCommand());
        configuration.addCommandOnTask(separator);
        configuration
                .addCommandOnTask(buildTaskPropertiesCommand(editTaskController));
        configuration.addCommandOnTask(resourceAllocationCommand);
        configuration
                .addCommandOnTask(buildAdvancedAllocationCommand(advancedAllocationTaskController));
        configuration
                .addCommandOnTask(buildSubcontractCommand(editTaskController));
        configuration
                .addCommandOnTask(buildCalendarAllocationCommand(calendarAllocationController));
        configuration.addCommandOnTask(separator);
        configuration.addCommandOnTask(advanceAssignmentPlanningCommand);
        configuration
                .addCommandOnTask(buildAdvanceConsolidationCommand(advanceConsolidationController));

        configuration.setDoubleClickCommand(resourceAllocationCommand);
        addPrintSupport(configuration, order);
        Tabbox chartComponent = new Tabbox();
        chartComponent.setOrient("vertical");
        chartComponent.setHeight("200px");
        appendTabs(chartComponent);

        configuration.setChartComponent(chartComponent);
        configureModificators(planningState.getOrder(), configuration);
        long setConfigurationTime = System.currentTimeMillis();
        planner.setConfiguration(configuration);
        PROFILING_LOG.debug("setConfiguration on planner took: "
                + (System.currentTimeMillis() - setConfigurationTime) + " ms");
        long preparingChartsAndMisc = System.currentTimeMillis();
        // Prepare tabpanels
        Tabpanels chartTabpanels = new Tabpanels();

        // Create 'Load' tab
        Timeplot chartLoadTimeplot = createEmptyTimeplot();
        chartTabpanels.appendChild(createLoadTimeplotTab(chartLoadTimeplot));

        // Create 'Earned value' tab
        Timeplot chartEarnedValueTimeplot = createEmptyTimeplot();
        this.earnedValueChartFiller = createOrderEarnedValueChartFiller(planner.getTimeTracker());
        chartTabpanels.appendChild(createEarnedValueTab(chartEarnedValueTimeplot, earnedValueChartFiller));

        // Append tab panels
        chartComponent.appendChild(chartTabpanels);
        ChangeHooker changeHooker = new ChangeHooker(configuration, saveCommand);

        setupLoadChart(chartLoadTimeplot, planner, changeHooker);
        setupEarnedValueChart(chartEarnedValueTimeplot, earnedValueChartFiller,
                planner, changeHooker);
        setupAdvanceAssignmentPlanningController(planner, advanceAssignmentPlanningController);
        PROFILING_LOG
                .debug("preparing charts and miscellaneous took: "
                        + (System.currentTimeMillis() - preparingChartsAndMisc)
                        + " ms");

        // Calculate critical path progress, needed for 'Project global progress' chart in Dashboard view
        planner.addGraphChangeListenersFromConfiguration(configuration);
        long overalProgressContentTime = System.currentTimeMillis();
        PROFILING_LOG.debug("overalProgressContent took: "
                + (System.currentTimeMillis() - overalProgressContentTime));
    }

    private OrderEarnedValueChartFiller earnedValueChartFiller;

    private void setupAdvanceAssignmentPlanningController(final Planner planner,
            AdvanceAssignmentPlanningController advanceAssignmentPlanningController) {

        advanceAssignmentPlanningController.setReloadEarnedValueListener(new IReloadChartListener() {

            @Override
            public void reloadChart() {
                Registry.getTransactionService().runOnReadOnlyTransaction(new IOnTransaction<Void>() {

                    @Override
                    public Void execute() {
                        if (isExecutingOutsideZKExecution()) {
                            return null;
                        }
                        if (planner.isVisibleChart()) {
                            //update earned value chart
                            earnedValueChart.fillChart();
                            //update earned value legend
                            updateEarnedValueChartLegend();
                        }
                        return null;
                    }
                });
            }
        });
    }

    private Timeplot createEmptyTimeplot() {
        Timeplot timeplot = new Timeplot();
        timeplot.appendChild(new Plotinfo());
        return timeplot;
    }

    private Tabpanel createLoadTimeplotTab(
            Timeplot loadChart) {
        Tabpanel result = new Tabpanel();
        appendLoadChartAndLegend(result, loadChart);
        return result;
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

    private OrderEarnedValueChartFiller createOrderEarnedValueChartFiller(TimeTracker timeTracker) {
        OrderEarnedValueChartFiller result = new OrderEarnedValueChartFiller(
                planningState.getOrder());
        result.calculateValues(timeTracker.getRealInterval());
        return result;
    }

    private Tabpanel createEarnedValueTab(
            Timeplot chartEarnedValueTimeplot,
            OrderEarnedValueChartFiller earnedValueChartFiller) {
        Tabpanel result = new Tabpanel();
        appendEarnedValueChartAndLegend(result, chartEarnedValueTimeplot,
                earnedValueChartFiller);
        return result;
    }

    private Vbox earnedValueChartLegendContainer;
    private Datebox earnedValueChartLegendDatebox;

    private void appendEarnedValueChartAndLegend(
            Tabpanel earnedValueChartPannel, Timeplot chartEarnedValueTimeplot,
            final OrderEarnedValueChartFiller earnedValueChartFiller) {
        Vbox vbox = new Vbox();
        this.earnedValueChartLegendContainer = vbox;
        vbox.setClass("legend-container");
        vbox.setAlign("center");
        vbox.setPack("center");

        Hbox dateHbox = new Hbox();
        dateHbox.appendChild(new Label(_("Select date")));

        LocalDate initialDateForIndicatorValues =
                earnedValueChartFiller.initialDateForIndicatorValues();
        this.earnedValueChartLegendDatebox = new Datebox(initialDateForIndicatorValues
                .toDateTimeAtStartOfDay().toDate());
        this.earnedValueChartLegendDatebox.setConstraint(
                dateMustBeInsideVisualizationArea(earnedValueChartFiller));
        dateHbox.appendChild(this.earnedValueChartLegendDatebox);

        appendEventListenerToDateboxIndicators(earnedValueChartFiller, vbox);
        vbox.appendChild(dateHbox);

        vbox.appendChild(getEarnedValueChartConfigurableLegend(
                earnedValueChartFiller, initialDateForIndicatorValues));

        Hbox hbox = new Hbox();
        hbox.setSclass("earned-value-chart");

        hbox.appendChild(vbox);

        Div div = new Div();
        div.appendChild(chartEarnedValueTimeplot);
        div.setSclass("plannergraph");

        hbox.appendChild(div);

        earnedValueChartPannel.appendChild(hbox);
    }

    private void setupLoadChart(Timeplot chartLoadTimeplot, Planner planner,
            ChangeHooker changeHooker) {
        Chart loadChart = setupChart(planningState.getOrder(),
                new OrderLoadChartFiller(planningState.getOrder()),
                chartLoadTimeplot, planner);
        refillLoadChartWhenNeeded(changeHooker, planner, loadChart, false);
    }

    private Chart earnedValueChart;

    private void setupEarnedValueChart(Timeplot chartEarnedValueTimeplot,
            OrderEarnedValueChartFiller earnedValueChartFiller,
            Planner planner, ChangeHooker changeHooker) {
        earnedValueChart = setupChart(planningState.getOrder(),
                earnedValueChartFiller, chartEarnedValueTimeplot, planner);
        refillLoadChartWhenNeeded(changeHooker, planner, earnedValueChart, true);
        setEventListenerConfigurationCheckboxes(earnedValueChart);
    }

    enum ChangeTypes {
        ON_SAVE, ON_RELOAD_CHART_REQUESTED, ON_GRAPH_CHANGED;
    }

    class ChangeHooker {

        private PlannerConfiguration<TaskElement> configuration;

        private ISaveCommand saveCommand;

        private boolean wrapOnReadOnlyTransaction = false;

        ChangeHooker(PlannerConfiguration<TaskElement> configuration,
                final ISaveCommand saveCommand) {
            Validate.notNull(configuration);
            this.configuration = configuration;
            this.saveCommand = saveCommand;
        }

        ChangeHooker withReadOnlyTransactionWraping() {
            ChangeHooker result = new ChangeHooker(configuration, saveCommand);
            result.wrapOnReadOnlyTransaction = true;
            return result;
        }

        void hookInto(EnumSet<ChangeTypes> reloadOn,
                IReloadChartListener reloadChart) {
            Validate.notNull(reloadChart);
            hookIntoImpl(wrapIfNeeded(reloadChart), reloadOn);
        }

        private IReloadChartListener wrapIfNeeded(
                IReloadChartListener reloadChart) {
            if (!wrapOnReadOnlyTransaction) {
                return reloadChart;
            }
            return AdHocTransactionService.readOnlyProxy(transactionService,
                    IReloadChartListener.class, reloadChart);
        }

        private void hookIntoImpl(IReloadChartListener reloadChart,
                EnumSet<ChangeTypes> reloadOn) {
            if (saveCommand != null
                    && reloadOn.contains(ChangeTypes.ON_GRAPH_CHANGED)) {
                hookIntoSaveCommand(reloadChart);
            }
            if (reloadOn.contains(ChangeTypes.ON_RELOAD_CHART_REQUESTED)) {
                hookIntoReloadChartRequested(reloadChart);
            }
            if (reloadOn.contains(ChangeTypes.ON_GRAPH_CHANGED)) {
                hookIntoGraphChanged(reloadChart);
            }
        }

        private void hookIntoSaveCommand(final IReloadChartListener reloadChart) {
            IAfterSaveListener afterSaveListener = new IAfterSaveListener() {
                @Override
                public void onAfterSave() {
                    reloadChart.reloadChart();
                }
            };
            saveCommand.addListener(afterSaveListener);
        }

        private void hookIntoReloadChartRequested(
                IReloadChartListener reloadChart) {
            configuration.addReloadChartListener(reloadChart);
        }

        private void hookIntoGraphChanged(final IReloadChartListener reloadChart) {
            configuration
                    .addPostGraphChangeListener(new IGraphChangeListener() {

                        @Override
                        public void execute() {
                            reloadChart.reloadChart();
                        }
                    });
        }

    }

    private void addPrintSupport(
            PlannerConfiguration<TaskElement> configuration, final Order order) {
        configuration.setPrintAction(new IPrintAction() {
            @Override
            public void doPrint() {
                CutyPrint.print(order);
            }

            @Override
            public void doPrint(Map<String, String> parameters) {
                CutyPrint.print(order, parameters);
            }

            @Override
            public void doPrint(HashMap<String, String> parameters,
                    Planner planner) {
                CutyPrint.print(order, parameters, planner);

            }

        });
    }

    private IDeleteMilestoneCommand buildDeleteMilestoneCommand() {
        deleteMilestoneCommand.setState(planningState);
        return deleteMilestoneCommand;
    }

    private void configureModificators(Order orderReloaded,
            PlannerConfiguration<TaskElement> configuration) {
        // Either InitDate or DeadLine must be set, depending on forwards or
        // backwards planning
        configuration.setSecondLevelModificators(SeveralModificators.create(
                BankHolidaysMarker.create(orderReloaded.getCalendar()),
                createStartDeadlineMarker(orderReloaded)));
    }

    private IDetailItemModificator createStartDeadlineMarker(Order order) {
        final DateTime projectStart = new DateTime(order.getInitDate());
        final DateTime deadline = new DateTime(order.getDeadline());
        IDetailItemModificator detailItemModificator;

        if (order.getInitDate() != null) {
            if (order.getDeadline() != null) {
                // Both project Start and deadline markers
                detailItemModificator = new IDetailItemModificator() {
                    @Override
                    public DetailItem applyModificationsTo(DetailItem item,
                            ZoomLevel z) {
                        item.markDeadlineDay(deadline);
                        item.markProjectStart(projectStart);
                        return item;
                    }
                };
            } else {
                // Project Start without deadline
                detailItemModificator = new IDetailItemModificator() {
                    @Override
                    public DetailItem applyModificationsTo(DetailItem item,
                            ZoomLevel z) {
                        item.markProjectStart(projectStart);
                        return item;
                    }
                };
            }
        } else {
            // Only project deadline marker
            detailItemModificator = new IDetailItemModificator() {
                @Override
                public DetailItem applyModificationsTo(DetailItem item,
                        ZoomLevel z) {
                    item.markDeadlineDay(deadline);
                    return item;
                }
            };
        }
        return detailItemModificator;
    }

    private void selectTab(String tabName) {
        tabSelected = tabName;
    }

    private void appendTabs(Tabbox chartComponent) {
        Tabs chartTabs = new Tabs();
        chartTabs.appendChild(createTab(_("Load"), "load_tab"));
        chartTabs.appendChild(createTab(_("Earned value"), "earned_value_tab"));

        chartComponent.appendChild(chartTabs);
        chartTabs.setSclass("charts-tabbox");
    }

    private Tab createTab(String name, final String id) {
        Tab tab = new Tab(name);
        tab.setId(id);
        if (id.equals(tabSelected)) {
            tab.setSelected(true);
        }
        tab.addEventListener("onClick", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                selectTab(id);
            }
        });
        return tab;
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

    private Constraint dateMustBeInsideVisualizationArea(
            final OrderEarnedValueChartFiller earnedValueChartFiller) {
        return new Constraint() {

            @Override
            public void validate(org.zkoss.zk.ui.Component comp,
                    Object valueObject)
                    throws WrongValueException {
                Date value = (Date) valueObject;
                if (value != null
                        && !EarnedValueChartFiller.includes(
                                earnedValueChartFiller
                        .getIndicatorsDefinitionInterval(), LocalDate
                        .fromDateFields(value))) {
                    throw new WrongValueException(comp,
                            _("Date must be inside visualization area"));
                }

            }
        };
    }

    private void dateInfutureMessage(Datebox datebox) {
        Date value = datebox.getValue();
        Date today = LocalDate.fromDateFields(new Date())
                .toDateTimeAtStartOfDay().toDate();
        if (value != null && (value.compareTo(today) > 0)) {
            throw new WrongValueException(datebox, _("date in the future"));
        }
    }

    private void appendEventListenerToDateboxIndicators(
            final OrderEarnedValueChartFiller earnedValueChartFiller,
            final Vbox vbox) {
        earnedValueChartLegendDatebox.addEventListener(Events.ON_CHANGE,
                new EventListener() {

            @Override
            public void onEvent(Event event) {
                updateEarnedValueChartLegend();
                dateInfutureMessage(earnedValueChartLegendDatebox);
            }

        });
    }

    private void updateEarnedValueChartLegend() {
        try {
            //force the validation again (getValue alone doesn't work because
            //the result of the validation is cached)
            earnedValueChartLegendDatebox.setValue(
                    earnedValueChartLegendDatebox.getValue());
        }
        catch (WrongValueException e) {
            //the user moved the gantt and the legend became out of the
            //visualization area, reset to a correct date
            earnedValueChartLegendDatebox.setValue(earnedValueChartFiller.
                    initialDateForIndicatorValues().toDateTimeAtStartOfDay()
                    .toDate());
        }
        LocalDate date = new LocalDate(earnedValueChartLegendDatebox.getRawValue());
        org.zkoss.zk.ui.Component child = earnedValueChartLegendContainer
                .getFellow("indicatorsTable");
        updateEarnedValueChartLegend(date);
    }

    private void updateEarnedValueChartLegend(LocalDate date) {
        for (EarnedValueType type : EarnedValueType.values()) {
            Label valueLabel = (Label) earnedValueChartLegendContainer
                    .getFellow(type.toString());
            valueLabel.setValue(getLabelTextEarnedValueType(
                    earnedValueChartFiller, type, date));
        }
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

        earnedValueChartConfigurationCheckboxes.clear();
        for (EarnedValueType type : EarnedValueType.values()) {
            Checkbox checkbox = new Checkbox(type.getAcronym());
            checkbox.setTooltiptext(type.getName());
            checkbox.setAttribute("indicator", type);
            checkbox.setStyle("color: " + type.getColor());

            Label valueLabel = new Label(getLabelTextEarnedValueType(
                    earnedValueChartFiller, type, date));
            valueLabel.setId(type.toString());

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

    private String getLabelTextEarnedValueType(
            OrderEarnedValueChartFiller earnedValueChartFiller,
            EarnedValueType type, LocalDate date) {
        BigDecimal value = earnedValueChartFiller.getIndicator(type, date);
        String units = _("h");
        if (type.equals(EarnedValueType.CPI)
                || type.equals(EarnedValueType.SPI)) {
            value = value.multiply(new BigDecimal(100));
            units = "%";
        }
        return value.setScale(0, RoundingMode.HALF_UP) + " " + units;
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
                public void onEvent(Event event) {
                    transactionService
                            .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
                                @Override
                                public Void execute() {
                                    earnedValueChart.fillChart();
                                    //not necessary to update legend here
                                    return null;
                                }
                            });
                }

            });
        }
    }

    private void refillLoadChartWhenNeeded(ChangeHooker changeHooker,
            final Planner planner, final Chart loadChart,
            final boolean updateEarnedValueChartLegend) {
        planner.getTimeTracker().addZoomListener(
                fillOnZoomChange(loadChart, planner, updateEarnedValueChartLegend));
        planner
                .addChartVisibilityListener(fillOnChartVisibilityChange(loadChart));

        changeHooker.withReadOnlyTransactionWraping().hookInto(
                EnumSet.allOf(ChangeTypes.class), new IReloadChartListener() {

                    @Override
                    public void reloadChart() {
                        if (isExecutingOutsideZKExecution()) {
                            return;
                        }
                        if (planner.isVisibleChart()) {
                            loadChart.fillChart();
                            if(updateEarnedValueChartLegend) {
                                updateEarnedValueChartLegend();
                            }
                        }
                    }
                });
    }

    private boolean isExecutingOutsideZKExecution() {
        return Executions.getCurrent() == null;
    }

    private void addAdditional(List<ICommand<TaskElement>> additional,
            PlannerConfiguration<TaskElement> configuration) {
        for (ICommand<TaskElement> c : additional) {
            configuration.addGlobalCommand(c);
        }
    }

    private boolean isWritingAllowedOnOrder() {
        if (planningState.getSavedOrderState() == OrderStatusEnum.STORED
                && planningState.getOrder().getState() == OrderStatusEnum.STORED) {
            // STORED orders can't be saved, independently of user permissions
            return false;
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_EDIT_ALL_PROJECTS)) {
            return true;
        }
        return thereIsWriteAuthorizationFor(planningState.getOrder());
    }

    private boolean thereIsWriteAuthorizationFor(Order order) {
        String loginName = SecurityUtils.getSessionUserLoginName();
        try {
            User user = userDAO.findByLoginName(loginName);
            for (OrderAuthorization authorization : orderAuthorizationDAO
                    .listByOrderUserAndItsProfiles(order, user)) {
                if (authorization.getAuthorizationType() == OrderAuthorizationType.WRITE_AUTHORIZATION) {
                    return true;
                }
            }
        } catch (InstanceNotFoundException e) {
            LOG.warn("there isn't a logged user for:" + loginName, e);
            // this case shouldn't happen, we continue anyway disabling the
            // save button
        }
        return false;
    }

    private ISaveCommand setupSaveCommand(
            PlannerConfiguration<TaskElement> configuration,
            boolean writingAllowed) {
        ISaveCommand result = planningState.getSaveCommand();
        if (!writingAllowed) {
            result.setDisabled(true);
        }
        configuration.addGlobalCommand(result);
        return result;
    }

    private void setupEditingCapabilities(
            PlannerConfiguration<TaskElement> configuration,
            boolean writingAllowed) {
        configuration.setAddingDependenciesEnabled(writingAllowed);
        configuration.setEditingDatesEnabled(writingAllowed);
        configuration.setMovingTasksEnabled(writingAllowed);
        configuration.setResizingTasksEnabled(writingAllowed);
    }

    private ICalendarAllocationCommand buildCalendarAllocationCommand(
            CalendarAllocationController calendarAllocationController) {
        calendarAllocationCommand
                .setCalendarAllocationController(calendarAllocationController);
        return calendarAllocationCommand;
    }

    private ITaskPropertiesCommand buildTaskPropertiesCommand(
            EditTaskController editTaskController) {
        taskPropertiesCommand.initialize(editTaskController, planningState);
        return taskPropertiesCommand;
    }

    private IAdvanceAssignmentPlanningCommand buildAdvanceAssignmentPlanningCommand(
            AdvanceAssignmentPlanningController advanceAssignmentPlanningController) {
        advanceAssignmentPlanningCommand.initialize(
                advanceAssignmentPlanningController, planningState);
        return advanceAssignmentPlanningCommand;
    }

    private IAdvanceConsolidationCommand buildAdvanceConsolidationCommand(
            AdvanceConsolidationController advanceConsolidationController) {
        advanceConsolidationCommand.initialize(advanceConsolidationController,
                planningState);
        return advanceConsolidationCommand;
    }

    private IAddMilestoneCommand buildMilestoneCommand() {
        return addMilestoneCommand;
    }

    private IResourceAllocationCommand buildResourceAllocationCommand(
            EditTaskController editTaskController) {
        resourceAllocationCommand.initialize(editTaskController, planningState);
        return resourceAllocationCommand;
    }

    private IAdvancedAllocationCommand buildAdvancedAllocationCommand(
            AdvancedAllocationTaskController advancedAllocationTaskController) {
        advancedAllocationCommand.initialize(advancedAllocationTaskController,
                planningState);
        return advancedAllocationCommand;
    }

    private ICommand<TaskElement> buildReassigningCommand() {
        reassignCommand.setState(planningState);
        return reassignCommand;
    }

    private ICommand<TaskElement> buildAdaptPlanningCommand() {
        adaptPlanningCommand.setState(planningState);
        return adaptPlanningCommand;
    }

    private ICommand<TaskElement> buildCancelEditionCommand() {
        return new ICommand<TaskElement>() {

            @Override
            public String getName() {
                return _("Cancel");
            }

            @Override
            public void doAction(IContext<TaskElement> context) {

                try {
                    Messagebox
                            .show(_("Unsaved changes will be lost. Are you sure?"),
                                    _("Confirm exit dialog"), Messagebox.OK
                                            | Messagebox.CANCEL,
                                    Messagebox.QUESTION,
                            new org.zkoss.zk.ui.event.EventListener() {
                                @Override
                                public void onEvent(Event evt)
                                        throws InterruptedException {
                                    if (evt.getName().equals("onOK")) {
                                        Executions
                                                .sendRedirect("/planner/index.zul;company_scheduling");
                                    }
                                }
                            });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String getImage() {
                return "/common/img/ico_back.png";
            }

            @Override
            public boolean isDisabled() {
                return false;
            }

            @Override
            public boolean isPlannerCommand() {
                return false;
            }

        };
    }

    private Chart setupChart(Order orderReloaded,
            IChartFiller loadChartFiller, Timeplot chartComponent,
            Planner planner) {
        TimeTracker timeTracker = planner.getTimeTracker();
        Chart result = new Chart(chartComponent, loadChartFiller,
                timeTracker);
        result.setZoomLevel(planner.getZoomLevel());
        if (planner.isVisibleChart()) {
            result.fillChart();
        }
        return result;
    }

    private IChartVisibilityChangedListener fillOnChartVisibilityChange(
            final Chart loadChart) {
        IChartVisibilityChangedListener chartVisibilityChangedListener = new IChartVisibilityChangedListener() {

            @Override
            public void chartVisibilityChanged(final boolean visible) {
                transactionService
                        .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
                            @Override
                            public Void execute() {
                                if (visible) {
                                    loadChart.fillChart();
                                }
                                return null;
                            }
                        });
            }
        };
        keepAliveChartVisibilityListeners.add(chartVisibilityChangedListener);
        return chartVisibilityChangedListener;
    }

    private IZoomLevelChangedListener fillOnZoomChange(final Chart loadChart,
            final Planner planner, final boolean updateEarnedValueChartLegend) {
        IZoomLevelChangedListener zoomListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                loadChart.setZoomLevel(detailLevel);

                transactionService
                        .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
                            @Override
                            public Void execute() {
                                if (planner.isVisibleChart()) {
                                    loadChart.fillChart();
                                    if (updateEarnedValueChartLegend) {
                                        updateEarnedValueChartLegend();
                                    }
                                }
                                return null;
                            }
                        });
            }
        };

        keepAliveZoomListeners.add(zoomListener);

        return zoomListener;
    }

    @Autowired
    private PlanningStateCreator planningStateCreator;

    private PlanningState createPlanningStateFor(Order order) {
        return planningStateCreator.retrieveOrCreate(planner.getDesktop(),
                order, new IActionsOnRetrieval() {

                    @Override
                    public void onRetrieval(PlanningState planningState) {
                        planningState.reattach();
                        planningState.reassociateResourcesWithSession();
                    }
                });
    }

    /**
     *
     * @author Óscar González Fernández <ogonzalez@igalia.com>
     * @author Diego Pino García <dpino@igalia.com>
     *
     *         Calculates 'Resource Load' values and set them in the Order
     *         'Resource Load' chart
     */
    private class OrderLoadChartFiller extends LoadChartFiller {

        // Soft green
        private static final String COLOR_ASSIGNED_LOAD_GLOBAL = "#E0F3D3";

        // Soft red
        private static final String COLOR_OVERLOAD_GLOBAL = "#FFD4C2";

        private final Order order;

        public OrderLoadChartFiller(Order orderReloaded) {
            this.order = orderReloaded;
        }

        @Override
        protected String getOptionalJavascriptCall() {
            return "ganttz.GanttPanel.getInstance().timeplotContainerRescroll()";
        }

        @Override
        protected Plotinfo[] getPlotInfos(Interval interval) {
            resourceLoadCalculator.setOrder(order, planningState.getAssignmentsCalculator());

            ContiguousDaysLine<EffortDuration> maxCapacityOnResources = resourceLoadCalculator
                    .getMaxCapacityOnResources();
            ContiguousDaysLine<EffortDuration> orderLoad = resourceLoadCalculator
                    .getOrderLoad();
            ContiguousDaysLine<EffortDuration> allLoad = resourceLoadCalculator
                    .getAllLoad();
            ContiguousDaysLine<EffortDuration> orderOverload = resourceLoadCalculator
                    .getOrderOverload();
            ContiguousDaysLine<EffortDuration> allOverload = resourceLoadCalculator
                    .getAllOverload();

            Plotinfo plotOrderLoad = createPlotinfoFromDurations(
                    groupAsNeededByZoom(toSortedMap(ContiguousDaysLine.min(
                            orderLoad, maxCapacityOnResources))), interval);

            Plotinfo plotOtherLoad = createPlotinfoFromDurations(
                    groupAsNeededByZoom(toSortedMap(min(allLoad,
                            maxCapacityOnResources))), interval);

            Plotinfo plotMaxCapacity = createPlotinfoFromDurations(
                    groupAsNeededByZoom(toSortedMap(maxCapacityOnResources)),
                    interval);

            Plotinfo plotOrderOverload = createPlotinfoFromDurations(
                    groupAsNeededByZoom(toSortedMap(sum(orderOverload,
                            maxCapacityOnResources))), interval);

            Plotinfo plotOtherOverload = createPlotinfoFromDurations(
                    groupAsNeededByZoom(toSortedMap(sum(allOverload,
                            maxCapacityOnResources))), interval);

            plotOrderLoad.setFillColor(COLOR_ASSIGNED_LOAD);
            plotOrderLoad.setLineWidth(0);

            plotOtherLoad.setFillColor(COLOR_ASSIGNED_LOAD_GLOBAL);
            plotOtherLoad.setLineWidth(0);

            plotMaxCapacity.setLineColor(COLOR_CAPABILITY_LINE);
            plotMaxCapacity.setFillColor("#FFFFFF");
            plotMaxCapacity.setLineWidth(2);

            plotOrderOverload.setFillColor(COLOR_OVERLOAD);
            plotOrderOverload.setLineWidth(0);

            plotOtherOverload.setFillColor(COLOR_OVERLOAD_GLOBAL);
            plotOtherOverload.setLineWidth(0);

            return new Plotinfo[] { plotOtherOverload, plotOrderOverload,
                    plotMaxCapacity, plotOtherLoad, plotOrderLoad };
        }

    }

    /**
     *
     * @author Manuel Rego Casasnovas <mrego@igalia.com>
     * @author Diego Pino García <dpino@igalia.com>
     *
     *         Calculates 'Earned Value' indicators and set them in the Order
     *         'Earned Valued' chart
     *
     */
    class OrderEarnedValueChartFiller extends EarnedValueChartFiller {

        private Order order;

        public OrderEarnedValueChartFiller(Order orderReloaded) {
            this.order = orderReloaded;
            super.setEarnedValueCalculator(earnedValueCalculator);
        }

        @Override
        protected void calculateBudgetedCostWorkScheduled(Interval interval) {
            setIndicatorInInterval(EarnedValueType.BCWS, interval,
                    earnedValueCalculator
                            .calculateBudgetedCostWorkScheduled(order));
        }

        @Override
        protected void calculateActualCostWorkPerformed(Interval interval) {
            setIndicatorInInterval(EarnedValueType.ACWP, interval,
                    earnedValueCalculator
                            .calculateActualCostWorkPerformed(order));
        }

        @Override
        protected void calculateBudgetedCostWorkPerformed(Interval interval) {
            setIndicatorInInterval(EarnedValueType.BCWP, interval,
                    earnedValueCalculator
                            .calculateBudgetedCostWorkPerformed(order));
        }

        @Override
        protected Set<EarnedValueType> getSelectedIndicators() {
            return getEarnedValueSelectedIndicators();
        }

    }

    private ISubcontractCommand buildSubcontractCommand(
            EditTaskController editTaskController) {
        subcontractCommand.initialize(editTaskController, planningState);
        return subcontractCommand;
    }

    @Override
    public Order getOrder() {
        return planningState.getOrder();
    }

    @Override
    public PlanningState getPlanningState() {
        return planningState;
    }

    @Override
    @Transactional(readOnly = true)
    public void forceLoadLabelsAndCriterionRequirements() {
        orderDAO.reattach(planningState.getOrder());
        forceLoadLabels(planningState.getOrder());
        forceLoadCriterionRequirements(planningState.getOrder());
    }

    private void forceLoadLabels(OrderElement orderElement) {
        orderElement.getLabels().size();
        if (!orderElement.isLeaf()) {
            for (OrderElement element : orderElement.getChildren()) {
                forceLoadLabels(element);
            }
        }
    }

    private void forceLoadCriterionRequirements(OrderElement orderElement) {
        orderElement.getCriterionRequirements().size();
        for (HoursGroup hoursGroup : orderElement.getHoursGroups()) {
            hoursGroup.getCriterionRequirements().size();
        }

        if (!orderElement.isLeaf()) {
            for (OrderElement element : orderElement.getChildren()) {
                forceLoadCriterionRequirements(element);
            }
        }
    }

}
