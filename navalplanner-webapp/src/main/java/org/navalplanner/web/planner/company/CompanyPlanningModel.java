/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

import static org.navalplanner.web.I18nHelper._;
import static org.navalplanner.web.resourceload.ResourceLoadModel.asDate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.entities.ProgressType;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.hibernate.notification.PredefinedDatabaseSnapshots;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderStatusEnum;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.TaskMilestone;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.templates.entities.OrderTemplate;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserRole;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.web.orders.assigntemplates.TemplateFinderPopup;
import org.navalplanner.web.orders.assigntemplates.TemplateFinderPopup.IOnResult;
import org.navalplanner.web.planner.ITaskElementAdapter;
import org.navalplanner.web.planner.chart.Chart;
import org.navalplanner.web.planner.chart.ChartFiller;
import org.navalplanner.web.planner.chart.EarnedValueChartFiller;
import org.navalplanner.web.planner.chart.EarnedValueChartFiller.EarnedValueType;
import org.navalplanner.web.planner.chart.IChartFiller;
import org.navalplanner.web.planner.order.BankHolidaysMarker;
import org.navalplanner.web.planner.order.OrderPlanningModel;
import org.navalplanner.web.planner.tabs.MultipleTabsPlannerController;
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
import org.zkoss.ganttz.IPredicate;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.adapters.PlannerConfiguration.IPrintAction;
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.extensions.ICommandOnTask;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkex.zul.api.South;
import org.zkoss.zul.Button;
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
 * Model for company planning view.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public abstract class CompanyPlanningModel implements ICompanyPlanningModel {

    public static final String COLOR_ASSIGNED_LOAD_GLOBAL = "#98D471"; // green
    public static final String COLOR_CAPABILITY_LINE = "#000000"; // black
    public static final String COLOR_OVERLOAD_GLOBAL = "#FDBE13";

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    private List<IZoomLevelChangedListener> keepAliveZoomListeners = new ArrayList<IZoomLevelChangedListener>();

    private List<Checkbox> earnedValueChartConfigurationCheckboxes = new ArrayList<Checkbox>();

    private MultipleTabsPlannerController tabs;

    private List<IChartVisibilityChangedListener> keepAliveChartVisibilityListeners = new ArrayList<IChartVisibilityChangedListener>();

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    private Scenario currentScenario;

    @Autowired
    private PredefinedDatabaseSnapshots databaseSnapshots;

    private LocalDate filterStartDate;
    private LocalDate filterFinishDate;
    private static final EnumSet<OrderStatusEnum> STATUS_VISUALIZED = EnumSet
            .of(OrderStatusEnum.ACCEPTED, OrderStatusEnum.OFFERED,
                    OrderStatusEnum.STARTED,
                    OrderStatusEnum.SUBCONTRACTED_PENDING_ORDER);

    public void setPlanningControllerEntryPoints(
            MultipleTabsPlannerController entryPoints) {
        this.tabs = entryPoints;
    }

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
        setConfigurationToPlanner(planner, additional, null);
    }

    @Override
    public void setTabsController(MultipleTabsPlannerController tabsController) {
        this.tabs = tabsController;
    }

    @Override
    @Transactional(readOnly = true)
    public void setConfigurationToPlanner(final Planner planner,
            Collection<ICommandOnTask<TaskElement>> additional,
            ICommandOnTask<TaskElement> doubleClickCommand,
            IPredicate predicate) {
        currentScenario = scenarioManager.getCurrent();
        final PlannerConfiguration<TaskElement> configuration = createConfiguration(predicate);
        boolean expandPlanningViewChart = configurationDAO.
                getConfiguration().isExpandCompanyPlanningViewCharts();
        configuration.setExpandPlanningViewCharts(expandPlanningViewChart);

        final Tabbox chartComponent = new Tabbox();
        chartComponent.setOrient("vertical");
        chartComponent.setHeight("200px");
        appendTabs(chartComponent);

        configuration.setChartComponent(chartComponent);
        if (doubleClickCommand != null) {
            configuration.setDoubleClickCommand(doubleClickCommand);
        }

        if (SecurityUtils.isUserInRole(UserRole.ROLE_CREATE_ORDER)) {
            ICommand<TaskElement> createNewOrderCommand = new ICommand<TaskElement>() {

                @Override
                public String getName() {
                    return _("Create new order");
                }

                @Override
                public String getImage() {
                    return "/common/img/ico_add.png";
                }

                @Override
                public void doAction(IContext<TaskElement> context) {
                    tabs.goToCreateForm();
                }

            };

            configuration.addGlobalCommand(createNewOrderCommand);

            ICommand<TaskElement> createNewOrderFromTemplateCommand = new ICommand<TaskElement>() {

                @Override
                public String getName() {
                    return _("Create new order from template");
                }

                @Override
                public String getImage() {
                    return "/common/img/ico_copy.png";
                }

                @Override
                public void doAction(IContext<TaskElement> context) {
                    TemplateFinderPopup templateFinderPopup = (TemplateFinderPopup) planner
                            .getFellowIfAny("templateFinderPopup");
                    Button createOrderFromTemplateButton = planner
                            .findCommandComponent(getName());
                    if (templateFinderPopup != null) {
                        templateFinderPopup.openForOrderCreation(
                                createOrderFromTemplateButton, "after_start",
                                new IOnResult<OrderTemplate>() {
                                    @Override
                                    public void found(OrderTemplate template) {
                                        goToCreateOtherOrderFromTemplate(template);
                                    }
                                });
                    }
                }

            };

            configuration.addGlobalCommand(createNewOrderFromTemplateCommand);
        }

        addAdditionalCommands(additional, configuration);
        addPrintSupport(configuration);
        disableSomeFeatures(configuration);

        ZoomLevel defaultZoomLevel = OrderPlanningModel
                .calculateDefaultLevel(configuration);
        OrderPlanningModel.configureInitialZoomLevelFor(planner,
                defaultZoomLevel);

        configuration.setSecondLevelModificators(new BankHolidaysMarker());
        planner.setConfiguration(configuration);

        if(expandPlanningViewChart) {
            //if the chart is expanded, we load the data now
            setupChartAndItsContent(planner, chartComponent);
        }
        else {
            //if the chart is not expanded, we load the data later with a listener
            ((South) planner.getFellow("graphics")).addEventListener("onOpen",
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        transactionService
                        .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
                            @Override
                            public Void execute() {
                                setupChartAndItsContent(planner, chartComponent);
                                return null;
                            }
                        });
                        //data is loaded only once, then we remove the listener
                        event.getTarget().removeEventListener("onOpen", this);
                    }
                });
        }
    }

    private void setupChartAndItsContent(Planner planner,
            Tabbox chartComponent) {
        Timeplot chartLoadTimeplot = createEmptyTimeplot();
        Timeplot chartEarnedValueTimeplot = createEmptyTimeplot();
        CompanyEarnedValueChartFiller earnedValueChartFiller = new CompanyEarnedValueChartFiller();
        earnedValueChartFiller.calculateValues(planner.getTimeTracker()
                .getRealInterval());
        appendTabpanels(chartComponent, chartLoadTimeplot,
                chartEarnedValueTimeplot, earnedValueChartFiller);

        setupChart(chartLoadTimeplot, new CompanyLoadChartFiller(), planner);
        Chart earnedValueChart = setupChart(chartEarnedValueTimeplot,
                earnedValueChartFiller, planner);
        setEventListenerConfigurationCheckboxes(earnedValueChart);
    }


    private Timeplot createEmptyTimeplot() {
        Timeplot timeplot = new Timeplot();
        timeplot.appendChild(new Plotinfo());
        return timeplot;
    }

    private void appendTabs(Tabbox chartComponent) {
        Tabs chartTabs = new Tabs();
        chartTabs.appendChild(new Tab(_("Load")));
        chartTabs.appendChild(new Tab(_("Earned value")));

        chartComponent.appendChild(chartTabs);
        chartTabs.setWidth("124px");
    }

    private void appendTabpanels(Tabbox chartComponent, Timeplot loadChart,
            Timeplot chartEarnedValueTimeplot,
            CompanyEarnedValueChartFiller earnedValueChartFiller) {
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

    private void appendEventListenerToDateboxIndicators(
            final CompanyEarnedValueChartFiller earnedValueChartFiller,
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

    public static void appendLoadChartAndLegend(Tabpanel loadChartPannel,
            Timeplot loadChart) {
        Hbox hbox = new Hbox();
        hbox.appendChild(getLoadChartLegend());

        Div div = new Div();
        div.appendChild(loadChart);
        div.setSclass("plannergraph");
        hbox.appendChild(div);

        loadChartPannel.appendChild(hbox);
    }

    public static org.zkoss.zk.ui.Component getLoadChartLegend() {
        Hbox hbox = new Hbox();
        hbox.setClass("legend-container");
        hbox.setAlign("center");
        hbox.setPack("center");
        Executions.createComponents("/planner/_legendLoadChartCompany.zul",
                hbox, null);
        return hbox;
    }

    private void appendEarnedValueChartAndLegend(
            Tabpanel earnedValueChartPannel, Timeplot chartEarnedValueTimeplot,
            CompanyEarnedValueChartFiller earnedValueChartFiller) {
        Vbox vbox = new Vbox();
        vbox.setClass("legend-container");
        vbox.setAlign("center");
        vbox.setPack("center");

        Hbox dateHbox = new Hbox();
        dateHbox.appendChild(new Label(_("Select date:")));

        LocalDate initialDate = earnedValueChartFiller
                .initialDateForIndicatorValues();
        Datebox datebox = new Datebox(initialDate.toDateTimeAtStartOfDay()
                .toDate());
        dateHbox.appendChild(datebox);

        appendEventListenerToDateboxIndicators(earnedValueChartFiller, vbox,
                datebox);
        vbox.appendChild(dateHbox);

        vbox.appendChild(getEarnedValueChartConfigurableLegend(
                earnedValueChartFiller, initialDate));

        Hbox hbox = new Hbox();
        hbox.setSclass("earned-value-chart");

        hbox.appendChild(vbox);

        Div div = new Div();
        div.appendChild(chartEarnedValueTimeplot);
        div.setSclass("plannergraph");

        hbox.appendChild(div);

        earnedValueChartPannel.appendChild(hbox);
    }

    private org.zkoss.zk.ui.Component getEarnedValueChartConfigurableLegend(
            CompanyEarnedValueChartFiller earnedValueChartFiller, LocalDate date) {
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

            BigDecimal value = earnedValueChartFiller.getIndicator(type,
                    date);
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

    private void disableSomeFeatures(
            PlannerConfiguration<TaskElement> configuration) {
        configuration.setAddingDependenciesEnabled(false);
        configuration.setMovingTasksEnabled(false);
        configuration.setResizingTasksEnabled(false);
        configuration.setCriticalPathEnabled(false);
        configuration.setExpandAllEnabled(false);
        configuration.setFlattenTreeEnabled(false);
        configuration.setRenamingTasksEnabled(false);
        configuration.setTreeEditable(false);
        configuration.setShowAllResourcesEnabled(false);
    }

    private void addAdditionalCommands(
            Collection<ICommandOnTask<TaskElement>> additional,
            PlannerConfiguration<TaskElement> configuration) {
        for (ICommandOnTask<TaskElement> t : additional) {
            configuration.addCommandOnTask(t);
        }
    }

    private void addPrintSupport(PlannerConfiguration<TaskElement> configuration) {
        configuration.setPrintAction(new IPrintAction() {
            @Override
            public void doPrint() {
                CutyPrint.print();
            }

            @Override
            public void doPrint(Map<String, String> parameters) {
                CutyPrint.print(parameters);
            }

            @Override
            public void doPrint(HashMap<String, String> parameters,
                    Planner planner) {
                CutyPrint.print(parameters, planner);
            }

        });
    }

    private Chart setupChart(Timeplot chartComponent,
            IChartFiller loadChartFiller, Planner planner) {
        TimeTracker timeTracker = planner.getTimeTracker();
        Chart loadChart = new Chart(chartComponent, loadChartFiller,
                timeTracker);
        loadChart.setZoomLevel(planner.getZoomLevel());
        if (planner.isVisibleChart()) {
            loadChart.fillChart();
        }
        timeTracker.addZoomListener(fillOnZoomChange(planner, loadChart));
        planner
                .addChartVisibilityListener(fillOnChartVisibilityChange(loadChart));
        return loadChart;
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

    private IZoomLevelChangedListener fillOnZoomChange(final Planner planner,
            final Chart loadChart) {

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

    private PlannerConfiguration<TaskElement> createConfiguration(
            IPredicate predicate) {
        ITaskElementAdapter taskElementAdapter = getTaskElementAdapter();
        taskElementAdapter.setPreventCalculateResourcesText(true);
        taskElementAdapter.useScenario(currentScenario);
        List<TaskElement> toShow;
        toShow = retainOnlyTopLevel(predicate);

        return new PlannerConfiguration<TaskElement>(taskElementAdapter,
                new TaskElementNavigator(), toShow);
    }

    private List<TaskElement> retainOnlyTopLevel(IPredicate predicate) {
        List<TaskElement> result = new ArrayList<TaskElement>();
        User user;
        List<Order> ordersToShow = new ArrayList<Order>();

        try {
            user = userDAO.findByLoginName(SecurityUtils.getSessionUserLoginName());
        }
        catch(InstanceNotFoundException e) {
            //this case shouldn't happen, because it would mean that there isn't a logged user
            //anyway, if it happenned we return an empty list
            return result;
        }
        List<Order> list = orderDAO.getOrdersByReadAuthorizationByScenario(
                user, currentScenario);
        for (Order order : list) {
            order.useSchedulingDataFor(currentScenario, false);
            TaskGroup associatedTaskElement = order.getAssociatedTaskElement();

            if (associatedTaskElement != null
                    && STATUS_VISUALIZED.contains(order.getState())
                    && (predicate == null || predicate.accepts(order))) {
                associatedTaskElement.setSimplifiedAssignedStatusCalculationEnabled(true);
                result.add(associatedTaskElement);
                ordersToShow.add(order);
            }
        }
        Collections.sort(result,new Comparator<TaskElement>(){
            @Override
            public int compare(TaskElement arg0, TaskElement arg1) {
                return arg0.getStartDate().compareTo(arg1.getStartDate());
            }
        });
        setDefaultFilterValues(ordersToShow);
        return result;
    }

    private void setDefaultFilterValues(List<? extends Order> list) {
        Date startDate = null;
        Date endDate = null;
        for (Order each : list) {
            TaskGroup associatedTaskElement = each.getAssociatedTaskElement();
            startDate = Collections.min(notNull(startDate, each.getInitDate()));
            endDate = Collections.max(notNull(endDate, each.getDeadline(),
                    associatedTaskElement.getEndDate()));
        }
        filterStartDate = startDate != null ? LocalDate
                .fromDateFields(startDate) : null;
        filterFinishDate = endDate != null ? LocalDate.fromDateFields(endDate)
                : null;
    }

    private static <T> List<T> notNull(T... values) {
        List<T> result = new ArrayList<T>();
        for (T each : values) {
            if (each != null) {
                result.add(each);
            }
        }
        return result;
    }

    // spring method injection
    protected abstract ITaskElementAdapter getTaskElementAdapter();

    @Override
    public LocalDate getFilterStartDate() {
        return filterStartDate;
    }

    @Override
    public LocalDate getFilterFinishDate() {
        return filterFinishDate;
    }

    private class CompanyLoadChartFiller extends ChartFiller {

        @Override
        public void fillChart(Timeplot chart, Interval interval, Integer size) {
            chart.getChildren().clear();
            chart.invalidate();

            String javascript = "zkTasklist.timeplotcontainer_rescroll();";
            Clients.evalJavaScript(javascript);

            resetMinimumAndMaximumValueForChart();

            LocalDate start = filterStartDate != null ? filterStartDate
                    : interval.getStart();
            LocalDate finish = filterFinishDate != null ? filterFinishDate
                    : interval.getFinish();

            Plotinfo plotInfoLoad = createPlotinfoFromDurations(
                    getLoad(start, finish), interval);
            plotInfoLoad.setFillColor(COLOR_ASSIGNED_LOAD_GLOBAL);
            plotInfoLoad.setLineWidth(0);

            Plotinfo plotInfoMax = createPlotinfoFromDurations(
                    getCalendarMaximumAvailability(start, finish), interval);
            plotInfoMax.setLineColor(COLOR_CAPABILITY_LINE);
            plotInfoMax.setFillColor("#FFFFFF");
            plotInfoMax.setLineWidth(2);

            Plotinfo plotInfoOverload = createPlotinfoFromDurations(
                    getOverload(start, finish), interval);
            plotInfoOverload.setFillColor(COLOR_OVERLOAD_GLOBAL);
            plotInfoOverload.setLineWidth(0);

            ValueGeometry valueGeometry = getValueGeometry();
            TimeGeometry timeGeometry = getTimeGeometry(interval);

            appendPlotinfo(chart, plotInfoLoad, valueGeometry, timeGeometry);
            appendPlotinfo(chart, plotInfoMax, valueGeometry, timeGeometry);
            appendPlotinfo(chart, plotInfoOverload, valueGeometry, timeGeometry);

            chart.setWidth(size + "px");
            chart.setHeight("150px");
        }

        private SortedMap<LocalDate, EffortDuration> getLoad(LocalDate start,
                LocalDate finish) {
            return groupAsNeededByZoom(databaseSnapshots.
                    snapshotResourceLoadChartData().getLoad().subMap(start, finish));
        }

        private SortedMap<LocalDate, EffortDuration> getOverload(
                LocalDate start, LocalDate finish) {
            return groupAsNeededByZoom(
                    databaseSnapshots.snapshotResourceLoadChartData().getOverload().subMap(start, finish));
        }

        private SortedMap<LocalDate, EffortDuration> getCalendarMaximumAvailability(
                LocalDate start, LocalDate finish) {
            return databaseSnapshots.snapshotResourceLoadChartData().getAvailability().subMap(start, finish);
        }

    }

    private class CompanyEarnedValueChartFiller extends EarnedValueChartFiller {

        protected void calculateBudgetedCostWorkScheduled(Interval interval) {
            Map<TaskElement, SortedMap<LocalDate, BigDecimal>> estimatedCostPerTask =
                databaseSnapshots.snapshotEstimatedCostPerTask();
            Collection<TaskElement> list = filterTasksByDate(
                    estimatedCostPerTask.keySet(),
                    asDate(filterStartDate), asDate(filterFinishDate));

            SortedMap<LocalDate, BigDecimal> estimatedCost = new TreeMap<LocalDate, BigDecimal>();

            for (TaskElement taskElement : list) {
                addCost(estimatedCost, estimatedCostPerTask.get(taskElement));
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

        private SortedMap<LocalDate, BigDecimal> getWorkReportCost() {
            SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();

            Collection<WorkReportLine> workReportLines = filterWorkReportLinesByDate(
                    databaseSnapshots.snapshotWorkReportLines(),
                    asDate(filterStartDate), asDate(filterFinishDate));

            if (workReportLines.isEmpty()) {
                return result;
            }

            for (WorkReportLine workReportLine : workReportLines) {
                LocalDate day = new LocalDate(workReportLine.getDate());
                BigDecimal cost = new BigDecimal(workReportLine.getNumHours());

                if (!result.containsKey(day)) {
                    result.put(day, BigDecimal.ZERO);
                }
                result.put(day, result.get(day).add(cost));
            }

            return result;
        }

        protected void calculateBudgetedCostWorkPerformed(Interval interval) {
            Map<TaskElement, SortedMap<LocalDate, BigDecimal>> advanceCostPerTask =
                databaseSnapshots.snapshotAdvanceCostPerTask();
            Collection<TaskElement> list = filterTasksByDate(
                    advanceCostPerTask.keySet(),
                    asDate(filterStartDate), asDate(filterFinishDate));

            SortedMap<LocalDate, BigDecimal> advanceCost = new TreeMap<LocalDate, BigDecimal>();

            for (TaskElement taskElement : list) {
                addCost(advanceCost, advanceCostPerTask.get(taskElement));
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

        private Collection<TaskElement> filterTasksByDate(
                Collection<TaskElement> tasks, Date startDate, Date endDate) {
            if(startDate == null && endDate == null) {
                return tasks;
            }
            for(TaskElement task : tasks) {
                if((startDate != null && task.getEndDate().compareTo(startDate)<0) ||
                    (endDate != null && task.getStartDate().compareTo(endDate)>0)) {
                    tasks.remove(task);
                }
            }
            return tasks;
        }

        private Collection<WorkReportLine> filterWorkReportLinesByDate(
                Collection<WorkReportLine> lines, Date startDate, Date endDate) {
            if(startDate == null && endDate == null) {
                return lines;
            }
            for(WorkReportLine line: lines) {
                if((startDate != null && line.getDate().compareTo(startDate)<0) ||
                    (endDate != null && line.getDate().compareTo(endDate)>0)) {
                    lines.remove(line);
                }
            }
            return lines;
        }
    }

    public void goToCreateOtherOrderFromTemplate(OrderTemplate template) {
        tabs.goToCreateotherOrderFromTemplate(template);
    }

    @Transactional(readOnly=true)
    public ProgressType getProgressTypeFromConfiguration() {
        return configurationDAO.getConfiguration().getProgressType();
    }

}
