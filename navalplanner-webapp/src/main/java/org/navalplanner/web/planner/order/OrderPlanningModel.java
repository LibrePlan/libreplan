/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

import static org.navalplanner.business.planner.chart.ContiguousDaysLine.min;
import static org.navalplanner.business.planner.chart.ContiguousDaysLine.sum;
import static org.navalplanner.business.planner.chart.ContiguousDaysLine.toSortedMap;
import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
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
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine;
import org.navalplanner.business.common.AdHocTransactionService;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.entities.ProgressType;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderStatusEnum;
import org.navalplanner.business.planner.chart.ContiguousDaysLine;
import org.navalplanner.business.planner.chart.ResourceLoadChartData;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.ICostCalculator;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.TaskMilestone;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.users.daos.IOrderAuthorizationDAO;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.OrderAuthorization;
import org.navalplanner.business.users.entities.OrderAuthorizationType;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserRole;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.web.calendars.BaseCalendarModel;
import org.navalplanner.web.common.ViewSwitcher;
import org.navalplanner.web.planner.TaskElementAdapter;
import org.navalplanner.web.planner.advances.AdvanceAssignmentPlanningController;
import org.navalplanner.web.planner.advances.IAdvanceAssignmentPlanningCommand;
import org.navalplanner.web.planner.allocation.IResourceAllocationCommand;
import org.navalplanner.web.planner.calendar.CalendarAllocationController;
import org.navalplanner.web.planner.calendar.ICalendarAllocationCommand;
import org.navalplanner.web.planner.chart.Chart;
import org.navalplanner.web.planner.chart.EarnedValueChartFiller;
import org.navalplanner.web.planner.chart.EarnedValueChartFiller.EarnedValueType;
import org.navalplanner.web.planner.chart.IChartFiller;
import org.navalplanner.web.planner.chart.LoadChartFiller;
import org.navalplanner.web.planner.consolidations.AdvanceConsolidationController;
import org.navalplanner.web.planner.consolidations.IAdvanceConsolidationCommand;
import org.navalplanner.web.planner.milestone.IAddMilestoneCommand;
import org.navalplanner.web.planner.milestone.IDeleteMilestoneCommand;
import org.navalplanner.web.planner.order.ISaveCommand.IAfterSaveListener;
import org.navalplanner.web.planner.order.PlanningStateCreator.IActionsOnRetrieval;
import org.navalplanner.web.planner.order.PlanningStateCreator.PlanningState;
import org.navalplanner.web.planner.reassign.IReassignCommand;
import org.navalplanner.web.planner.taskedition.EditTaskController;
import org.navalplanner.web.planner.taskedition.ITaskPropertiesCommand;
import org.navalplanner.web.print.CutyPrint;
import org.navalplanner.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkforge.timeplot.Plotinfo;
import org.zkforge.timeplot.Timeplot;
import org.zkoss.ganttz.IChartVisibilityChangedListener;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.adapters.PlannerConfiguration.IPrintAction;
import org.zkoss.ganttz.adapters.PlannerConfiguration.IReloadChartListener;
import org.zkoss.ganttz.data.GanttDiagramGraph.IGraphChangeListener;
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.extensions.ICommandOnTask;
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
import org.zkoss.zul.Progressmeter;
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
    private SaveCommandBuilder saveCommandBuilder;

    @Autowired
    private IReassignCommand reassignCommand;

    @Autowired
    private IResourceAllocationCommand resourceAllocationCommand;

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
    private TaskElementAdapter taskElementAdapterCreator;

    @Autowired
    private ICostCalculator hoursCostCalculator;

    private List<Checkbox> earnedValueChartConfigurationCheckboxes = new ArrayList<Checkbox>();

    private List<IChartVisibilityChangedListener> keepAliveChartVisibilityListeners = new ArrayList<IChartVisibilityChangedListener>();

    private Scenario currentScenario;

    private Planner planner;

    private OverAllProgressContent overallProgressContent;

    private static final class TaskElementNavigator implements
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
            AdvanceAssignmentPlanningController advanceAssignmentPlanningController,
            AdvanceConsolidationController advanceConsolidationController,
            CalendarAllocationController calendarAllocationController,
            List<ICommand<TaskElement>> additional) {
        long time = System.currentTimeMillis();
        this.planner = planner;
        planningState = createPlanningStateFor(order);
        currentScenario = scenarioManager.getCurrent();
        PlannerConfiguration<TaskElement> configuration = createConfiguration(order);
        PROFILING_LOG.info("load data and create configuration took: "
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

        final boolean writingAllowed = isWritingAllowedOn(planningState
                .getOrder());
        ISaveCommand saveCommand = setupSaveCommand(configuration,
                writingAllowed);
        setupEditingCapabilities(configuration, writingAllowed);

        configuration.addGlobalCommand(buildReassigningCommand());

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
        PROFILING_LOG.info("setConfiguration on planner took: "
                + (System.currentTimeMillis() - setConfigurationTime) + " ms");
        long preparingChartsAndMisc = System.currentTimeMillis();
        // Prepare tabpanels
        Tabpanels chartTabpanels = new Tabpanels();

        // Create 'Load' tab
        Timeplot chartLoadTimeplot = createEmptyTimeplot();
        chartTabpanels.appendChild(createLoadTimeplotTab(chartLoadTimeplot));

        // Create 'Earned value' tab
        Timeplot chartEarnedValueTimeplot = createEmptyTimeplot();
        OrderEarnedValueChartFiller earnedValueChartFiller = createOrderEarnedValueChartFiller(planner.getTimeTracker());
        chartTabpanels.appendChild(createEarnedValueTab(chartEarnedValueTimeplot, earnedValueChartFiller));

        // Create 'Overall progress' tab
        Hbox chartOverallProgressTimeplot = new Hbox();
        Tabpanel overallProgressTab = createOverallProgressTab(chartOverallProgressTimeplot);
        chartTabpanels.appendChild(overallProgressTab);

        // Append tab panels
        chartComponent.appendChild(chartTabpanels);
        ChangeHooker changeHooker = new ChangeHooker(configuration, saveCommand);

        setupLoadChart(chartLoadTimeplot, planner, changeHooker);
        setupEarnedValueChart(chartEarnedValueTimeplot, earnedValueChartFiller,
                planner, changeHooker);
        setupOverallProgress(planner, changeHooker);
        setupAdvanceAssignmentPlanningController(planner, advanceAssignmentPlanningController);
        PROFILING_LOG
                .info("preparing charts and miscellaneous took: "
                        + (System.currentTimeMillis() - preparingChartsAndMisc)
                        + " ms");

        planner.addGraphChangeListenersFromConfiguration(configuration);
        long overalProgressContentTime = System.currentTimeMillis();
        overallProgressContent = new OverAllProgressContent(overallProgressTab);
        overallProgressContent.updateAndRefresh();
        PROFILING_LOG.info("overalProgressContent took: "
                + (System.currentTimeMillis() - overalProgressContentTime));
    }

    private void setupAdvanceAssignmentPlanningController(final Planner planner,
            AdvanceAssignmentPlanningController advanceAssignmentPlanningController) {

        advanceAssignmentPlanningController.reloadOverallProgressListener(new IReloadChartListener() {

            @Override
            public void reloadChart() {
                Registry.getTransactionService().runOnReadOnlyTransaction(new IOnTransaction<Void>() {

                    @Override
                    public Void execute() {
                        if (isExecutingOutsideZKExecution()) {
                            return null;
                        }
                        if (planner.isVisibleChart()) {
                            overallProgressContent.updateAndRefresh();
                        }
                        return null;
                    }
                });
            }
        });
    }

    private Tabpanel createOverallProgressTab(
            Hbox chartOverallProgressTimeplot) {
        Tabpanel result = new Tabpanel();
        org.zkoss.zk.ui.Component component = Executions.createComponents(
                "/planner/_tabPanelOverallProgress.zul", result, null);
        component.setParent(result);
        return result;
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

    private void appendEarnedValueChartAndLegend(
            Tabpanel earnedValueChartPannel, Timeplot chartEarnedValueTimeplot,
            final OrderEarnedValueChartFiller earnedValueChartFiller) {
        Vbox vbox = new Vbox();
        vbox.setClass("legend-container");
        vbox.setAlign("center");
        vbox.setPack("center");

        Hbox dateHbox = new Hbox();
        dateHbox.appendChild(new Label(_("Select date:")));

        LocalDate initialDateForIndicatorValues = earnedValueChartFiller.initialDateForIndicatorValues();
        Datebox datebox = new Datebox(initialDateForIndicatorValues
                .toDateTimeAtStartOfDay().toDate());
        datebox.setConstraint(dateMustBeInsideVisualizationArea(earnedValueChartFiller));
        dateHbox.appendChild(datebox);

        appendEventListenerToDateboxIndicators(earnedValueChartFiller, vbox,
                datebox);
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
        refillLoadChartWhenNeeded(changeHooker, planner, loadChart);
    }

    private void setupEarnedValueChart(Timeplot chartEarnedValueTimeplot,
            OrderEarnedValueChartFiller earnedValueChartFiller,
            Planner planner, ChangeHooker changeHooker) {
        Chart earnedValueChart = setupChart(planningState.getOrder(),
                earnedValueChartFiller, chartEarnedValueTimeplot, planner);
        refillLoadChartWhenNeeded(changeHooker, planner, earnedValueChart);
        setEventListenerConfigurationCheckboxes(earnedValueChart);
    }

    private void setupOverallProgress(final Planner planner,
            ChangeHooker changeHooker) {

        changeHooker.withReadOnlyTransactionWraping().hookInto(
                EnumSet.allOf(ChangeTypes.class), new IReloadChartListener() {

                    @Override
                    public void reloadChart() {
                        if (isExecutingOutsideZKExecution()) {
                            return;
                        }
                        if (planner.isVisibleChart()) {
                            overallProgressContent.updateAndRefresh();
                        }
                    }
      });
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
        if (orderReloaded.getDeadline() != null) {
            configuration.setSecondLevelModificators(SeveralModificators
                            .create(BankHolidaysMarker.create(orderReloaded
                                    .getCalendar()),
                            createDeadlineShower(orderReloaded.getDeadline())));
        } else {
            configuration.setSecondLevelModificators(BankHolidaysMarker.create(orderReloaded.getCalendar()));
        }
    }

    private IDetailItemModificator createDeadlineShower(Date orderDeadline) {
        final DateTime deadline = new DateTime(orderDeadline);
        IDetailItemModificator deadlineMarker = new IDetailItemModificator() {

            @Override
            public DetailItem applyModificationsTo(DetailItem item,
                    ZoomLevel zoomlevel) {
                item.markDeadlineDay(deadline);
                return item;
            }
        };
        return deadlineMarker;
    }

    private void appendTabs(Tabbox chartComponent) {
        Tabs chartTabs = new Tabs();
        chartTabs.appendChild(new Tab(_("Load")));
        chartTabs.appendChild(new Tab(_("Earned value")));
        chartTabs.appendChild(new Tab(_("Overall progress")));

        chartComponent.appendChild(chartTabs);
        chartTabs.setSclass("charts-tabbox");
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
                            _("the date must be inside the visualization area"));
                }

            }
        };
    }

    private void dateInfutureMessage(Datebox datebox) {
        Date value = datebox.getValue();
        Date today = LocalDate.fromDateFields(new Date())
                .toDateTimeAtStartOfDay().toDate();
        if (value != null && (value.compareTo(today) > 0)) {
            throw new WrongValueException(datebox, _("date in future"));
        }
    }

    private void appendEventListenerToDateboxIndicators(
            final OrderEarnedValueChartFiller earnedValueChartFiller,
            final Vbox vbox, final Datebox datebox) {
        datebox.addEventListener(Events.ON_CHANGE, new EventListener() {

            @Override
            public void onEvent(Event event) {
                LocalDate date = new LocalDate(datebox.getValue());
                org.zkoss.zk.ui.Component child = vbox
                        .getFellow("indicatorsTable");
                vbox.removeChild(child);
                vbox.appendChild(getEarnedValueChartConfigurableLegend(
                        earnedValueChartFiller, date));
                dateInfutureMessage(datebox);
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
                public void onEvent(Event event) {
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

    private void refillLoadChartWhenNeeded(ChangeHooker changeHooker,
            final Planner planner, final Chart loadChart) {
        planner.getTimeTracker().addZoomListener(
                fillOnZoomChange(loadChart, planner));
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

    private boolean isWritingAllowedOn(Order order) {
        if (order.getState() == OrderStatusEnum.STORED) {
            //STORED orders can't be saved, independently of user permissions
            return false;
        }
        if (SecurityUtils.isUserInRole(UserRole.ROLE_EDIT_ALL_ORDERS)) {
            return true;
        }
        return thereIsWriteAuthorizationFor(order);
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
        if (writingAllowed) {
            ISaveCommand result = buildSaveCommand(configuration);
            configuration.addGlobalCommand(result);
            return result;
        }
        return null;
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
        addMilestoneCommand.setState(planningState);
        return addMilestoneCommand;
    }

    private IResourceAllocationCommand buildResourceAllocationCommand(
            EditTaskController editTaskController) {
        resourceAllocationCommand.initialize(editTaskController, planningState);
        return resourceAllocationCommand;
    }

    private ISaveCommand buildSaveCommand(
            PlannerConfiguration<TaskElement> configuration) {
        return saveCommandBuilder.build(planningState, configuration);
    }

    private ICommand<TaskElement> buildReassigningCommand() {
        reassignCommand.setState(planningState);
        return reassignCommand;
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
            final Planner planner) {
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
                                }
                                return null;
                            }
                        });
            }
        };

        keepAliveZoomListeners.add(zoomListener);

        return zoomListener;
    }

    private PlannerConfiguration<TaskElement> createConfiguration(Order order) {
        PlannerConfiguration<TaskElement> result = new PlannerConfiguration<TaskElement>(
                taskElementAdapterCreator
                        .createForOrder(currentScenario, order),
                new TaskElementNavigator(), planningState.getInitial());
        result.setNotBeforeThan(order.getInitDate());
        result.setNotAfterThan(order.getDeadline());
        result.setDependenciesConstraintsHavePriority(order
                .getDependenciesConstraintsHavePriority());
        result.setScheduleBackwards(order.isScheduleBackwards());
        return result;
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

    public static final String COLOR_ASSIGNED_LOAD_GLOBAL = "#E0F3D3"; // Soft
    // green
    public static final String COLOR_OVERLOAD_GLOBAL = "#FFD4C2"; // Soft red

    private class OrderLoadChartFiller extends LoadChartFiller {

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
            List<DayAssignment> orderDayAssignments = order.getDayAssignments();
            ContiguousDaysLine<List<DayAssignment>> orderAssignments = ContiguousDaysLine
                    .byDay(orderDayAssignments);
            ContiguousDaysLine<List<DayAssignment>> allAssignments = allAssignments(orderAssignments);

            ContiguousDaysLine<EffortDuration> maxCapacityOnResources = orderAssignments
                    .transform(ResourceLoadChartData
                            .extractAvailabilityOnAssignedResources());
            ContiguousDaysLine<EffortDuration> orderLoad = orderAssignments
                    .transform(ResourceLoadChartData.extractLoad());
            ContiguousDaysLine<EffortDuration> allLoad = allAssignments
                    .transform(ResourceLoadChartData.extractLoad());
            ContiguousDaysLine<EffortDuration> orderOverload = orderAssignments
                    .transform(ResourceLoadChartData.extractOverload());
            ContiguousDaysLine<EffortDuration> allOverload = allAssignments
                    .transform(ResourceLoadChartData.extractOverload());

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

        private ContiguousDaysLine<List<DayAssignment>> allAssignments(
                ContiguousDaysLine<List<DayAssignment>> orderAssignments) {
            if (orderAssignments.isNotValid()) {
                return ContiguousDaysLine.<List<DayAssignment>> invalid();
            }
            return allAssignmentsOnResourcesAt(orderAssignments.getStart(),
                    orderAssignments.getEndExclusive());
        }

        private ContiguousDaysLine<List<DayAssignment>> allAssignmentsOnResourcesAt(
                LocalDate startInclusive, LocalDate endExclusive) {
            AvailabilityTimeLine.Interval interval = AvailabilityTimeLine.Interval
                    .create(startInclusive, endExclusive);
            List<DayAssignment> resourcesDayAssignments = new ArrayList<DayAssignment>();
            for (Resource resource : order.getResources()) {
                resourcesDayAssignments.addAll(insideInterval(interval,
                        planningState.getAssignmentsCalculator()
                                .getAssignments(resource)));
            }
            return ContiguousDaysLine.byDay(resourcesDayAssignments);
        }

        private List<DayAssignment> insideInterval(
                AvailabilityTimeLine.Interval interval,
                List<DayAssignment> assignments) {
            List<DayAssignment> result = new ArrayList<DayAssignment>();
            for (DayAssignment each : assignments) {
                if (interval.includes(each.getDay())) {
                    result.add(each);
                }
            }
            return result;
        }

    }

    class OrderEarnedValueChartFiller extends EarnedValueChartFiller {

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
            List<TaskElement> list = order
                    .getAllChildrenAssociatedTaskElements();
            list.add(order.getAssociatedTaskElement());

            SortedMap<LocalDate, BigDecimal> workReportCost = new TreeMap<LocalDate, BigDecimal>();

            for (TaskElement taskElement : list) {
                if (taskElement instanceof Task) {
                    addCost(workReportCost, hoursCostCalculator
                            .getWorkReportCost((Task) taskElement));
                }
            }

            workReportCost = accumulateResult(workReportCost);
            addZeroBeforeTheFirstValue(workReportCost);
            indicators.put(EarnedValueType.ACWP, calculatedValueForEveryDay(
                    workReportCost, interval.getStart(), interval.getFinish()));
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

    private ISubcontractCommand buildSubcontractCommand(
            EditTaskController editTaskController) {
        subcontractCommand.initialize(editTaskController, planningState);
        return subcontractCommand;
    }

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

    /**
     *
     * @author Diego Pino García<dpino@igalia.com>
     *
     * Helper class to show the content of a OverallProgress panel
     *
     */
    private class OverAllProgressContent {

        private Progressmeter progressCriticalPathByDuration;

        private Label lbCriticalPathByDuration;

        private Progressmeter progressCriticalPathByNumHours;

        private Label lbCriticalPathByNumHours;

        private Progressmeter progressSpread;

        private Label lbProgressSpread;

        private Progressmeter progressAllByNumHours;

        private Label lbProgressAllByNumHours;

        public OverAllProgressContent(Tabpanel tabpanel) {
            initializeProgressCriticalPathByDuration(tabpanel);
            initializeProgressCriticalPathByNumHours(tabpanel);
            initializeProgressSpread(tabpanel);
            initializeProgressAllByNumHours(tabpanel);

            tabpanel.setVariable("overall_progress_content", this, true);
        }

        private void initializeProgressCriticalPathByNumHours(Tabpanel tabpanel) {
            progressCriticalPathByNumHours = (Progressmeter) tabpanel
                    .getFellow("progressCriticalPathByNumHours");
            lbCriticalPathByNumHours = (Label) tabpanel
                    .getFellow("lbCriticalPathByNumHours");
            ((Label) tabpanel.getFellow("textCriticalPathByNumHours"))
                    .setValue(_(ProgressType.CRITICAL_PATH_NUMHOURS.toString()));
        }

        private void initializeProgressCriticalPathByDuration(Tabpanel tabpanel) {
            progressCriticalPathByDuration = (Progressmeter) tabpanel
                    .getFellow("progressCriticalPathByDuration");
            lbCriticalPathByDuration = (Label) tabpanel
                    .getFellow("lbCriticalPathByDuration");
            ((Label) tabpanel.getFellow("textCriticalPathByDuration"))
                    .setValue(_(ProgressType.CRITICAL_PATH_DURATION.toString()));
        }

        public void initializeProgressSpread(Tabpanel tabpanel) {
            progressSpread = (Progressmeter) tabpanel
                    .getFellow("progressSpread");
            lbProgressSpread = (Label) tabpanel.getFellow("lbProgressSpread");
            ((Label) tabpanel.getFellow("textProgressSpread"))
                    .setValue(_(ProgressType.SPREAD_PROGRESS.toString()));
        }

        public void initializeProgressAllByNumHours(Tabpanel tabpanel) {
            progressAllByNumHours = (Progressmeter) tabpanel
                    .getFellow("progressAllByNumHours");
            lbProgressAllByNumHours = (Label) tabpanel
                    .getFellow("lbProgressAllByNumHours");
            ((Label) tabpanel.getFellow("textProgressAllByNumHours"))
                    .setValue(_(ProgressType.ALL_NUMHOURS.toString()));
        }

        public void refresh() {
            if (planningState.isEmpty()) {
                return;
            }
            TaskGroup rootTask = planningState.getRootTask();

            setProgressSpread(rootTask.getAdvancePercentage());
            setProgressAllByNumHours(rootTask.getProgressAllByNumHours());
            setCriticalPathByDuration(rootTask
                    .getCriticalPathProgressByDuration());
            setCriticalPathByNumHours(rootTask
                    .getCriticalPathProgressByNumHours());
        }

        private void updateAndRefresh() {
            if (planningState.isEmpty()) {
                return;
            }
            update();
            refresh();
        }

        private void update() {
            TaskGroup rootTask = planningState.getRootTask();
            updateCriticalPathProgress(rootTask);
        }

        private void updateCriticalPathProgress(TaskGroup rootTask) {
            if (planner != null) {
                rootTask.updateCriticalPathProgress((List<TaskElement>) planner
                        .getCriticalPath());
            }
        }

        private void setProgressSpread(BigDecimal value) {
            if (value == null) {
                value = BigDecimal.ZERO;
            }
            value = value.multiply(BigDecimal.valueOf(100));
            value = value.setScale(2, BigDecimal.ROUND_HALF_EVEN);
            lbProgressSpread.setValue(value.toString() + " %");
            progressSpread.setValue(value.intValue());
        }

        private void setProgressAllByNumHours(BigDecimal value) {
            if (value == null) {
                value = BigDecimal.ZERO;
            }
            value = value.multiply(BigDecimal.valueOf(100));
            value = value.setScale(2, BigDecimal.ROUND_HALF_EVEN);
            lbProgressAllByNumHours.setValue(value.toString() + " %");
            progressAllByNumHours.setValue(value.intValue());
        }

        public void setCriticalPathByDuration(BigDecimal value) {
            if (value == null) {
                value = BigDecimal.ZERO;
            }
            value = value.multiply(BigDecimal.valueOf(100));
            value = value.setScale(2, BigDecimal.ROUND_HALF_EVEN);
            lbCriticalPathByDuration.setValue(value.toString() + " %");
            progressCriticalPathByDuration.setValue(value.intValue());
        }

        public void setCriticalPathByNumHours(BigDecimal value) {
            if (value == null) {
                value = BigDecimal.ZERO;
            }
            value = value.multiply(BigDecimal.valueOf(100));
            value = value.setScale(2, BigDecimal.ROUND_HALF_EVEN);
            lbCriticalPathByNumHours.setValue(value.toString() + " %");
            progressCriticalPathByNumHours.setValue(value.intValue());
        }

    }

}
