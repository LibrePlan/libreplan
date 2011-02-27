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

import static org.navalplanner.business.common.AdHocTransactionService.readOnlyProxy;
import static org.navalplanner.business.workingday.EffortDuration.min;
import static org.navalplanner.business.workingday.EffortDuration.zero;
import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.entities.ProgressType;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderStatusEnum;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.DerivedAllocation;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ICostCalculator;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.TaskMilestone;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.IAssignmentsOnResourceCalculator;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.daos.IOrderVersionDAO;
import org.navalplanner.business.scenarios.daos.IScenarioDAO;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.users.daos.IOrderAuthorizationDAO;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.OrderAuthorization;
import org.navalplanner.business.users.entities.OrderAuthorizationType;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserRole;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;
import org.navalplanner.web.calendars.BaseCalendarModel;
import org.navalplanner.web.common.ViewSwitcher;
import org.navalplanner.web.planner.ITaskElementAdapter;
import org.navalplanner.web.planner.advances.AdvanceAssignmentPlanningController;
import org.navalplanner.web.planner.advances.IAdvanceAssignmentPlanningCommand;
import org.navalplanner.web.planner.allocation.IResourceAllocationCommand;
import org.navalplanner.web.planner.calendar.CalendarAllocationController;
import org.navalplanner.web.planner.calendar.ICalendarAllocationCommand;
import org.navalplanner.web.planner.chart.Chart;
import org.navalplanner.web.planner.chart.ChartFiller;
import org.navalplanner.web.planner.chart.EarnedValueChartFiller;
import org.navalplanner.web.planner.chart.EarnedValueChartFiller.EarnedValueType;
import org.navalplanner.web.planner.chart.IChartFiller;
import org.navalplanner.web.planner.consolidations.AdvanceConsolidationController;
import org.navalplanner.web.planner.consolidations.IAdvanceConsolidationCommand;
import org.navalplanner.web.planner.milestone.IAddMilestoneCommand;
import org.navalplanner.web.planner.milestone.IDeleteMilestoneCommand;
import org.navalplanner.web.planner.order.ISaveCommand.IAfterSaveListener;
import org.navalplanner.web.planner.order.PlanningState.IScenarioInfo;
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
import org.zkforge.timeplot.geometry.TimeGeometry;
import org.zkforge.timeplot.geometry.ValueGeometry;
import org.zkoss.ganttz.IChartVisibilityChangedListener;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.adapters.PlannerConfiguration.IPrintAction;
import org.zkoss.ganttz.adapters.PlannerConfiguration.IReloadChartListener;
import org.zkoss.ganttz.data.GanttDiagramGraph.IGraphChangeListener;
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.timetracker.zoom.IDetailItemModificator;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.SeveralModificators;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
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
@Scope(BeanDefinition.SCOPE_SINGLETON)
public abstract class OrderPlanningModel implements IOrderPlanningModel {

    public static final String COLOR_CAPABILITY_LINE = "#000000"; // Black

    public static final String COLOR_ASSIGNED_LOAD_GLOBAL = "#98D471"; // Green
    public static final String COLOR_OVERLOAD_GLOBAL = "#FDBE13"; // Orange

    public static final String COLOR_ASSIGNED_LOAD_SPECIFIC = "#AA80d5"; // Violet
    public static final String COLOR_OVERLOAD_SPECIFIC = "#FF5A11"; // Red

    private static final Log LOG = LogFactory.getLog(OrderPlanningModel.class);

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
                TaskElement.getByEndDateComparator());

        LocalDate startDate = LocalDate.fromDateFields(earliest
                .getStartDate());
        LocalDate endDate = LocalDate.fromDateFields(latest.getEndDate());
        return ZoomLevel.getDefaultZoomByDates(startDate, endDate);
    }

    @Autowired
    private IOrderDAO orderDAO;

    private PlanningState planningState;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ITaskElementDAO taskDAO;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IOrderAuthorizationDAO orderAuthorizationDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    private List<IZoomLevelChangedListener> keepAliveZoomListeners = new ArrayList<IZoomLevelChangedListener>();

    private ITaskElementAdapter taskElementAdapter;

    @Autowired
    private ICostCalculator hoursCostCalculator;

    private List<Checkbox> earnedValueChartConfigurationCheckboxes = new ArrayList<Checkbox>();

    private Order orderReloaded;

    private List<IChartVisibilityChangedListener> keepAliveChartVisibilityListeners = new ArrayList<IChartVisibilityChangedListener>();

    @Autowired
    private IConfigurationDAO configurationDAO;

    private IAssignmentsOnResourceCalculator assigmentsOnResourceCalculator = new Resource.AllResourceAssignments();

    private Scenario currentScenario;

    @Autowired
    private IOrderVersionDAO orderVersionDAO;

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    private OverAllProgressContent overallProgressContent;

    private final class ReturningNewAssignments implements
            IAssignmentsOnResourceCalculator {

        private Set<DayAssignment> previousAssignmentsSet;

        public ReturningNewAssignments(List<DayAssignment> previousAssignments) {
            this.previousAssignmentsSet = new HashSet<DayAssignment>(
                    previousAssignments);
        }

        @Override
        public List<DayAssignment> getAssignments(Resource resource) {
            List<DayAssignment> result = new ArrayList<DayAssignment>();
            for (DayAssignment each : resource.getAssignments()) {
                if (!previousAssignmentsSet.contains(each)) {
                    result.add(each);
                }
            }
            return result;
        }

    }

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
    public void setConfigurationToPlanner(final Planner planner, Order order,
            ViewSwitcher switcher,
            EditTaskController editTaskController,
            AdvanceAssignmentPlanningController advanceAssignmentPlanningController,
            AdvanceConsolidationController advanceConsolidationController,
            CalendarAllocationController calendarAllocationController,
            List<ICommand<TaskElement>> additional) {
        currentScenario = scenarioManager.getCurrent();
        orderReloaded = reload(order);
        PlannerConfiguration<TaskElement> configuration = createConfiguration(planner, orderReloaded);
        configuration.setExpandPlanningViewCharts(configurationDAO
                .getConfiguration().isExpandOrderPlanningViewCharts());

        addAdditional(additional, configuration);

        ZoomLevel defaultZoomLevel = OrderPlanningModel
                .calculateDefaultLevel(configuration);
        configureInitialZoomLevelFor(planner, defaultZoomLevel);

        final boolean writingAllowed = isWritingAllowedOn(orderReloaded);
        ISaveCommand saveCommand = setupSaveCommand(configuration,
                writingAllowed);
        setupEditingCapabilities(configuration, writingAllowed);

        configuration.addGlobalCommand(buildReassigningCommand());

        final IResourceAllocationCommand resourceAllocationCommand =
            buildResourceAllocationCommand(editTaskController);
        configuration.addCommandOnTask(resourceAllocationCommand);
        configuration.addCommandOnTask(buildMilestoneCommand());
        configuration.addCommandOnTask(buildDeleteMilestoneCommand());
        configuration
                .addCommandOnTask(buildCalendarAllocationCommand(calendarAllocationController));
        configuration
                .addCommandOnTask(buildTaskPropertiesCommand(editTaskController));

        final IAdvanceAssignmentPlanningCommand advanceAssignmentPlanningCommand =
            buildAdvanceAssignmentPlanningCommand(advanceAssignmentPlanningController);
        configuration.addCommandOnTask(advanceAssignmentPlanningCommand);
        configuration
                .addCommandOnTask(buildAdvanceConsolidationCommand(advanceConsolidationController));
        configuration
                .addCommandOnTask(buildSubcontractCommand(editTaskController));

        configuration.setDoubleClickCommand(resourceAllocationCommand);
        addPrintSupport(configuration, order);
        Tabbox chartComponent = new Tabbox();
        chartComponent.setOrient("vertical");
        chartComponent.setHeight("200px");
        appendTabs(chartComponent);

        configuration.setChartComponent(chartComponent);
        configureModificators(orderReloaded, configuration);
        planner.setConfiguration(configuration);

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

        setupLoadChart(chartLoadTimeplot, planner, configuration, saveCommand);
        setupEarnedValueChart(chartEarnedValueTimeplot, earnedValueChartFiller, planner, configuration, saveCommand);
        setupOverallProgress(saveCommand);

        planner.addGraphChangeListenersFromConfiguration(configuration);
        overallProgressContent = new OverAllProgressContent(overallProgressTab);
        overallProgressContent.refresh();
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
        OrderEarnedValueChartFiller result = new OrderEarnedValueChartFiller(orderReloaded);
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
            PlannerConfiguration<TaskElement> configuration,
            ISaveCommand saveCommand) {
        Chart loadChart = setupChart(orderReloaded, new OrderLoadChartFiller(
                orderReloaded), chartLoadTimeplot, planner);
        refillLoadChartWhenNeeded(configuration, planner, saveCommand,
                loadChart);
    }

    private void setupEarnedValueChart(Timeplot chartEarnedValueTimeplot,
            OrderEarnedValueChartFiller earnedValueChartFiller,
            Planner planner, PlannerConfiguration<TaskElement> configuration,
            ISaveCommand saveCommand) {
        Chart earnedValueChart = setupChart(orderReloaded,
                earnedValueChartFiller, chartEarnedValueTimeplot, planner);
        refillLoadChartWhenNeeded(configuration, planner, saveCommand,
                earnedValueChart);
        setEventListenerConfigurationCheckboxes(earnedValueChart);
    }

    private void setupOverallProgress(final ISaveCommand saveCommand) {

        // Refresh progress chart after saving
        saveCommand.addListener(new IAfterSaveListener() {
            @Override
            public void onAfterSave() {
                transactionService.runOnTransaction(new IOnTransaction<Void>() {
                    @Override
                    public Void execute() {
                        overallProgressContent.refresh();
                        return null;
                    }
                });
            }
        });

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
        IDeleteMilestoneCommand result = getDeleteMilestoneCommand();
        result.setState(planningState);
        return result;
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
            PlannerConfiguration<?> configuration, final Planner planner,
            ISaveCommand saveCommand, final Chart loadChart) {
        planner.getTimeTracker().addZoomListener(
                fillOnZoomChange(loadChart, planner));
        planner
                .addChartVisibilityListener(fillOnChartVisibilityChange(loadChart));
        if(saveCommand != null) {
            saveCommand.addListener(fillChartOnSave(loadChart, planner));
        }
        configuration.addPostGraphChangeListener(readOnlyProxy(
                transactionService, IGraphChangeListener.class, new IGraphChangeListener() {
                    @Override
                    public void execute() {
                        if (isExecutingOutsideZKExecution()) {
                            return;
                        }
                        if (planner.isVisibleChart()) {
                            loadChart.fillChart();
                        }
                    }
                }));
        configuration.addReloadChartListener(readOnlyProxy(transactionService,
                IReloadChartListener.class, new IReloadChartListener() {
                    @Override
                    public void reloadChart() {
                        if (planner.isVisibleChart()) {
                            loadChart.fillChart();
                        }
                    }
                }));
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
            ISaveCommand result = buildSaveCommand();
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
        ICalendarAllocationCommand calendarAllocationCommand = getCalendarAllocationCommand();
        calendarAllocationCommand
                .setCalendarAllocationController(calendarAllocationController);
        return calendarAllocationCommand;
    }

    private ITaskPropertiesCommand buildTaskPropertiesCommand(
            EditTaskController editTaskController) {
        ITaskPropertiesCommand taskPropertiesCommand = getTaskPropertiesCommand();
        taskPropertiesCommand.initialize(editTaskController,
                planningState);
        return taskPropertiesCommand;
    }

    private IAdvanceAssignmentPlanningCommand buildAdvanceAssignmentPlanningCommand(
            AdvanceAssignmentPlanningController advanceAssignmentPlanningController) {
        IAdvanceAssignmentPlanningCommand advanceAssignmentPlanningCommand = getAdvanceAssignmentPlanningCommand();
        advanceAssignmentPlanningCommand.initialize(
                advanceAssignmentPlanningController, planningState);
        return advanceAssignmentPlanningCommand;
    }

    private IAdvanceConsolidationCommand buildAdvanceConsolidationCommand(
            AdvanceConsolidationController advanceConsolidationController) {
        IAdvanceConsolidationCommand advanceConsolidationCommand = getAdvanceConsolidationCommand();
        advanceConsolidationCommand.initialize(advanceConsolidationController,
                planningState);
        return advanceConsolidationCommand;
    }

    private IAddMilestoneCommand buildMilestoneCommand() {
        IAddMilestoneCommand addMilestoneCommand = getAddMilestoneCommand();
        addMilestoneCommand.setState(planningState);
        return addMilestoneCommand;
    }

    private IResourceAllocationCommand buildResourceAllocationCommand(
            EditTaskController editTaskController) {
        IResourceAllocationCommand resourceAllocationCommand = getResourceAllocationCommand();
        resourceAllocationCommand.initialize(editTaskController,
                planningState);
        return resourceAllocationCommand;
    }

    private ISaveCommand buildSaveCommand() {
        ISaveCommand saveCommand = getSaveCommand();
        saveCommand.setState(planningState);
        return saveCommand;
    }

    private ICommand<TaskElement> buildReassigningCommand() {
        IReassignCommand result = getReassignCommand();
        result.setState(planningState);
        return result;
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

    private IAfterSaveListener fillChartOnSave(final Chart loadChart,
            final Planner planner) {
        IAfterSaveListener result = new IAfterSaveListener() {

                    @Override
            public void onAfterSave() {
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
        return result;
    }

    private PlannerConfiguration<TaskElement> createConfiguration(
            Planner planner, Order orderReloaded) {
        taskElementAdapter = getTaskElementAdapter();
        taskElementAdapter.useScenario(currentScenario);
        planningState = createPlanningStateFor(planner, orderReloaded);
        taskElementAdapter
                .setInitDate(asLocalDate(orderReloaded.getInitDate()));
        taskElementAdapter
                .setDeadline(asLocalDate(orderReloaded.getDeadline()));
        PlannerConfiguration<TaskElement> result = new PlannerConfiguration<TaskElement>(
                taskElementAdapter,
                new TaskElementNavigator(), planningState.getInitial());
        result.setNotBeforeThan(orderReloaded.getInitDate());
        result.setNotAfterThan(orderReloaded.getDeadline());
        result.setDependenciesConstraintsHavePriority(orderReloaded
                .getDependenciesConstraintsHavePriority());
        result.setScheduleBackwards(orderReloaded.isScheduleBackwards());
        return result;
    }

    private LocalDate asLocalDate(Date date) {
        return date != null ? LocalDate.fromDateFields(date) : null;
    }

    private PlanningState createPlanningStateFor(
            Planner planner, Order orderReloaded) {
        if (!orderReloaded.isSomeTaskElementScheduled()) {
            return PlanningState.createEmpty(currentScenario);
        }
        final List<Resource> allResources = resourceDAO.list(Resource.class);
        criterionDAO.list(Criterion.class);
        TaskGroup taskElement = orderReloaded.getAssociatedTaskElement();
        forceLoadOfChildren(Arrays.asList(taskElement));
        forceLoadDayAssignments(orderReloaded.getResources());
        switchAllocationsToScenario(currentScenario, taskElement);
        final IScenarioInfo scenarioInfo = buildScenarioInfo(orderReloaded);
        PlanningState result = PlanningState.create(planner, taskElement, orderReloaded
                .getAssociatedTasks(), allResources, criterionDAO, resourceDAO,
                scenarioInfo);
        forceLoadOfDependenciesCollections(result.getInitial());
        forceLoadOfWorkingHours(result.getInitial());
        forceLoadOfLabels(result.getInitial());
        return result;
    }

    private void forceLoadDayAssignments(Set<Resource> resources) {
        for (Resource resource : resources) {
            resource.getAssignments().size();
        }
    }

    private IScenarioInfo buildScenarioInfo(Order orderReloaded) {
        if (orderReloaded.isUsingTheOwnerScenario()) {
            return createOwnerScenarioInfoFor(orderReloaded);
        }
        final List<DayAssignment> previousAssignments = orderReloaded
                .getDayAssignments();
        OrderVersion previousVersion = currentScenario
                .getOrderVersion(orderReloaded);
        OrderVersion newVersion = OrderVersion
                .createInitialVersion(currentScenario);
        orderReloaded.writeSchedulingDataChangesTo(currentScenario, newVersion);
        assigmentsOnResourceCalculator = new ReturningNewAssignments(
                previousAssignments);
        switchAllocationsToScenario(currentScenario, orderReloaded
                .getAssociatedTaskElement());
        return createScenarioInfoForNotOwnerScenario(orderReloaded, previousVersion,
                newVersion);
    }

    private IScenarioInfo createOwnerScenarioInfoFor(Order orderReloaded) {
        return PlanningState.ownerScenarioInfo(orderVersionDAO,
                currentScenario,
                currentScenario.getOrderVersion(orderReloaded));
    }

    private IScenarioInfo createScenarioInfoForNotOwnerScenario(
            Order orderReloaded, OrderVersion previousVersion,
            OrderVersion newVersion) {
        return PlanningState.forNotOwnerScenario(orderDAO, scenarioDAO,
                taskSourceDAO, orderReloaded,
                previousVersion, currentScenario, newVersion);
    }

    private void forceLoadOfChildren(Collection<? extends TaskElement> initial) {
        for (TaskElement each : initial) {
            forceLoadOfDataAssociatedTo(each);
            if (each instanceof TaskGroup) {
                findChildrenWithQueryToAvoidProxies((TaskGroup) each);
                List<TaskElement> children = each.getChildren();
                forceLoadOfChildren(children);
            }
        }
    }

    public static void forceLoadOfDataAssociatedTo(TaskElement each) {
        forceLoadOfResourceAllocationsResources(each);
        forceLoadOfCriterions(each);
        if (each.getCalendar() != null) {
            BaseCalendarModel.forceLoadBaseCalendar(each.getCalendar());
        }
        each.hasConsolidations();
    }

    private static void switchAllocationsToScenario(Scenario scenario,
            TaskElement task) {
        for (ResourceAllocation<?> each : task.getAllResourceAllocations()) {
            each.switchToScenario(scenario);
        }
    }

    /**
     * Forcing the load of all criterions so there are no different criterion
     * instances for the same criteiron at database
     */
    private static void forceLoadOfCriterions(TaskElement taskElement) {
        List<GenericResourceAllocation> generic = ResourceAllocation.getOfType(
                GenericResourceAllocation.class, taskElement
                        .getSatisfiedResourceAllocations());
        for (GenericResourceAllocation each : generic) {
            for (Criterion eachCriterion : each.getCriterions()) {
                eachCriterion.getName();
            }
        }
    }

    /**
     * Forcing the load of all resources so the resources at planning state and
     * at allocations are the same
     */
    private static void forceLoadOfResourceAllocationsResources(
            TaskElement taskElement) {
        Set<ResourceAllocation<?>> resourceAllocations = taskElement
                .getAllResourceAllocations();
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
            if (taskElement.getTaskSource() != null) {
                taskElement.getTaskSource().getTotalHours();
                OrderElement orderElement = taskElement.getOrderElement();
                if (orderElement != null) {
                    orderElement.getWorkHours();
                }
                if (!taskElement.isLeaf()) {
                    forceLoadOfWorkingHours(taskElement.getChildren());
                }
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

    protected abstract IReassignCommand getReassignCommand();

    protected abstract IResourceAllocationCommand getResourceAllocationCommand();

    protected abstract IAddMilestoneCommand getAddMilestoneCommand();

    protected abstract IDeleteMilestoneCommand getDeleteMilestoneCommand();

    protected abstract ITaskPropertiesCommand getTaskPropertiesCommand();

    protected abstract IAdvanceConsolidationCommand getAdvanceConsolidationCommand();

    protected abstract IAdvanceAssignmentPlanningCommand getAdvanceAssignmentPlanningCommand();

    protected abstract ICalendarAllocationCommand getCalendarAllocationCommand();

    private Order reload(Order order) {
        Order result = orderDAO.findExistingEntity(order.getId());
        result.useSchedulingDataFor(currentScenario);
        return result;
    }

    private class OrderLoadChartFiller extends ChartFiller {

        private final Order order;

        private SortedMap<LocalDate, EffortDuration> mapOrderLoad = new TreeMap<LocalDate, EffortDuration>();
        private SortedMap<LocalDate, EffortDuration> mapOrderOverload = new TreeMap<LocalDate, EffortDuration>();
        private SortedMap<LocalDate, EffortDuration> mapMaxCapacity = new TreeMap<LocalDate, EffortDuration>();
        private SortedMap<LocalDate, EffortDuration> mapOtherLoad = new TreeMap<LocalDate, EffortDuration>();
        private SortedMap<LocalDate, EffortDuration> mapOtherOverload = new TreeMap<LocalDate, EffortDuration>();

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
            resetMaps();

            List<DayAssignment> orderDayAssignments = order.getDayAssignments();
            SortedMap<LocalDate, Map<Resource, EffortDuration>> orderDayAssignmentsGrouped = groupDurationsByDayAndResource(orderDayAssignments);

            List<DayAssignment> resourcesDayAssignments = new ArrayList<DayAssignment>();
            for (Resource resource : order.getResources()) {
                resourcesDayAssignments.addAll(assigmentsOnResourceCalculator
                        .getAssignments(resource));
            }
            SortedMap<LocalDate, Map<Resource, EffortDuration>> resourceDayAssignmentsGrouped = groupDurationsByDayAndResource(resourcesDayAssignments);

            fillMaps(orderDayAssignmentsGrouped, resourceDayAssignmentsGrouped);
            convertAsNeededByZoomMaps();

            Plotinfo plotOrderLoad = createPlotinfoFromDurations(
                    mapOrderLoad, interval);
            Plotinfo plotOrderOverload = createPlotinfoFromDurations(
                    mapOrderOverload,
                    interval);
            Plotinfo plotMaxCapacity = createPlotinfoFromDurations(
                    mapMaxCapacity, interval);
            Plotinfo plotOtherLoad = createPlotinfoFromDurations(
                    mapOtherLoad, interval);
            Plotinfo plotOtherOverload = createPlotinfoFromDurations(
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

        private void resetMaps() {
            mapOrderLoad.clear();
            mapOrderOverload.clear();
            mapMaxCapacity.clear();
            mapOtherLoad.clear();
            mapOtherOverload.clear();
        }

        private void convertAsNeededByZoomMaps() {
            mapOrderLoad = groupAsNeededByZoom(mapOrderLoad);
            mapOrderOverload = groupAsNeededByZoom(mapOrderOverload);
            mapMaxCapacity = groupAsNeededByZoom(mapMaxCapacity);
            mapOtherLoad = groupAsNeededByZoom(mapOtherLoad);
            mapOtherOverload = groupAsNeededByZoom(mapOtherOverload);
        }

        private void fillMaps(
                SortedMap<LocalDate, Map<Resource, EffortDuration>> orderDayAssignmentsGrouped,
                SortedMap<LocalDate, Map<Resource, EffortDuration>> resourceDayAssignmentsGrouped) {

            for (LocalDate date : orderDayAssignmentsGrouped.keySet()) {
                final EffortDuration maxCapacity = getSumCapacities(
                        orderDayAssignmentsGrouped, date);
                final PartialDay day = PartialDay.wholeDay(date);
                EffortDuration orderLoad = zero();
                EffortDuration orderOverload = zero();
                EffortDuration otherLoad = zero();
                EffortDuration otherOverload = zero();

                for (Resource resource : orderDayAssignmentsGrouped.get(date)
                        .keySet()) {
                    final EffortDuration resourceCapacityHours = calendarCapacityFor(
                            resource, day);

                    final EffortDuration durationAtOrder = orderDayAssignmentsGrouped
                            .get(date).get(resource);

                    final EffortDuration totalDurationForResource;
                    totalDurationForResource = retrieveTotalDurationForResource(
                            resourceDayAssignmentsGrouped, date, resource);

                    final EffortDuration durationOther;
                    durationOther = totalDurationForResource.minus(min(
                            durationAtOrder, totalDurationForResource));

                    final EffortDuration allDuration = durationAtOrder
                            .plus(durationOther);

                    final EffortDuration orderLoadIncrement;
                    orderLoadIncrement = min(durationAtOrder,
                            resourceCapacityHours);

                    final EffortDuration orderOverloadIncrement;
                    orderOverloadIncrement = durationAtOrder
                            .minus(orderLoadIncrement);

                    final EffortDuration otherLoadIncrement;
                    otherLoadIncrement = min(allDuration, resourceCapacityHours)
                            .minus(min(resourceCapacityHours, durationAtOrder));

                    final EffortDuration otherOverloadIncrement;
                    otherOverloadIncrement = durationOther
                            .minus(otherLoadIncrement);

                    orderLoad = orderLoad.plus(orderLoadIncrement);
                    orderOverload = orderOverload.plus(orderOverloadIncrement);
                    otherLoad = otherLoad.plus(otherLoadIncrement);
                    otherOverload = otherOverload.plus(otherOverloadIncrement);
                }
                mapMaxCapacity.put(date, maxCapacity);
                mapOrderLoad.put(date, orderLoad);
                mapOrderOverload.put(date, orderOverload.plus(maxCapacity));
                mapOtherLoad.put(date, otherLoad.plus(orderLoad));
                mapOtherOverload.put(date, otherOverload.plus(orderOverload)
                        .plus(maxCapacity));
            }
        }

        private EffortDuration getSumCapacities(
                SortedMap<LocalDate, Map<Resource, EffortDuration>> orderDayAssignmentsGrouped,
                LocalDate date) {
            PartialDay day = PartialDay.wholeDay(date);
            EffortDuration result = zero();
            for (Resource resource : orderDayAssignmentsGrouped.get(date)
                    .keySet()) {
                result = result.plus(calendarCapacityFor(resource, day));
            }
            return result;
        }

        private EffortDuration retrieveTotalDurationForResource(
                SortedMap<LocalDate, Map<Resource, EffortDuration>> resourceDayAssignmentsGrouped,
                LocalDate day, Resource resource) {
            // FIXME review why is null sometimes
            if (resourceDayAssignmentsGrouped.get(day) != null
                    && resourceDayAssignmentsGrouped.get(day).get(resource) != null) {
                return resourceDayAssignmentsGrouped.get(day).get(resource);
            }
            return zero();
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
        ISubcontractCommand subcontractCommand = getSubcontractCommand();
        subcontractCommand.initialize(editTaskController,
                planningState);
        return subcontractCommand;
    }

    protected abstract ISubcontractCommand getSubcontractCommand();

    public Order getOrder() {
        return orderReloaded;
    }

    @Override
    public PlanningState getPlanningState() {
        return planningState;
    }

    @Override
    @Transactional(readOnly = true)
    public void forceLoadLabelsAndCriterionRequirements() {
        orderDAO.reattach(orderReloaded);
        forceLoadLabels(orderReloaded);
        forceLoadCriterionRequirements(orderReloaded);
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

        private Progressmeter progressAdvancePercentage;

        private Label lbAdvancePercentage;

        private Button btnRefresh;


        public OverAllProgressContent(Tabpanel tabpanel) {
            initializeProgressCriticalPathByDuration(tabpanel);
            initializeProgressCriticalPathByNumHours(tabpanel);
            initializeProgressAdvancePercentage(tabpanel);

            btnRefresh = (Button) tabpanel.getFellow("btnRefresh");
            tabpanel.setVariable("overall_progress_content", this, true);
            btnRefresh.addEventListener(Events.ON_CLICK, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    if (planningState.isEmpty()) {
                        return;
                    }
                    transactionService
                    .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
                        @Override
                        public Void execute() {
                            update();
                            refresh();
                            return null;
                        }
                    });
                }
            });
        }

        private void initializeProgressCriticalPathByNumHours(Tabpanel tabpanel) {
            progressCriticalPathByNumHours = (Progressmeter) tabpanel
                    .getFellow("progressCriticalPathByNumHours");
            lbCriticalPathByNumHours = (Label) tabpanel
                    .getFellow("lbCriticalPathByNumHours");
            ((Label) tabpanel.getFellow("textCriticalPathByNumHours"))
                    .setValue(ProgressType.CRITICAL_PATH_NUMHOURS.toString());
        }

        private void initializeProgressCriticalPathByDuration(Tabpanel tabpanel) {
            progressCriticalPathByDuration = (Progressmeter) tabpanel
                    .getFellow("progressCriticalPathByDuration");
            lbCriticalPathByDuration = (Label) tabpanel
                    .getFellow("lbCriticalPathByDuration");
            ((Label) tabpanel.getFellow("textCriticalPathByDuration"))
                    .setValue(ProgressType.CRITICAL_PATH_DURATION.toString());
        }

        public void initializeProgressAdvancePercentage(Tabpanel tabpanel) {
            progressAdvancePercentage = (Progressmeter) tabpanel
                    .getFellow("progressAdvancePercentage");
            lbAdvancePercentage = (Label) tabpanel
                    .getFellow("lbAdvancePercentage");
            ((Label) tabpanel.getFellow("textAdvancePercentage"))
                    .setValue(ProgressType.SPREAD_PROGRESS.toString());
        }

        public void refresh() {
            if (planningState.isEmpty()) {
                return;
            }
            TaskGroup rootTask = planningState.getRootTask();

            setAdvancePercentage(rootTask.getAdvancePercentage());
            setCriticalPathByDuration(rootTask
                    .getCriticalPathProgressByDuration());
            setCriticalPathByNumHours(rootTask
                    .getCriticalPathProgressByNumHours());
        }

        private void update() {
            TaskGroup rootTask = planningState.getRootTask();
            updateCriticalPathProgress(rootTask);
        }

        private void updateCriticalPathProgress(TaskGroup rootTask) {
            taskElementDAO.save(rootTask);
            rootTask
                    .updateCriticalPathProgress((List<TaskElement>) planningState
                            .getPlanner().getCriticalPath());
        }

        private void setAdvancePercentage(BigDecimal value) {
            if (value == null) {
                value = BigDecimal.ZERO;
            }
            value = value.multiply(BigDecimal.valueOf(100));
            value = value.setScale(2, BigDecimal.ROUND_HALF_EVEN);
            lbAdvancePercentage.setValue(value.toString() + " %");
            progressAdvancePercentage.setValue(value.intValue());
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
