/*
 * This file is part of LibrePlan
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

package org.libreplan.web.planner.company;

import static org.libreplan.web.I18nHelper._;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.entities.ProgressType;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.hibernate.notification.PredefinedDatabaseSnapshots;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderStatusEnum;
import org.libreplan.business.planner.chart.ILoadChartData;
import org.libreplan.business.planner.chart.ResourceLoadChartData;
import org.libreplan.business.planner.entities.ICompanyEarnedValueCalculator;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.planner.entities.TaskMilestone;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.common.FilterUtils;
import org.libreplan.web.common.components.finders.FilterPair;
import org.libreplan.web.common.components.finders.TaskGroupFilterEnum;
import org.libreplan.web.planner.TaskElementAdapter;
import org.libreplan.web.planner.TaskGroupPredicate;
import org.libreplan.web.planner.chart.Chart;
import org.libreplan.web.planner.chart.EarnedValueChartFiller;
import org.libreplan.web.planner.chart.EarnedValueChartFiller.EarnedValueType;
import org.libreplan.web.planner.chart.IChartFiller;
import org.libreplan.web.planner.chart.StandardLoadChartFiller;
import org.libreplan.web.planner.order.BankHolidaysMarker;
import org.libreplan.web.planner.order.OrderPlanningModel;
import org.libreplan.web.planner.tabs.MultipleTabsPlannerController;
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
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.adapters.PlannerConfiguration.IPrintAction;
import org.zkoss.ganttz.extensions.ICommandOnTask;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Emitter;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
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
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CompanyPlanningModel implements ICompanyPlanningModel {

    /** All the status but CANCELLED and STORED */
    private static final EnumSet<OrderStatusEnum> STATUS_VISUALIZED = OrderStatusEnum.getVisibleStatus();

    private static final String CENTER = "center";

    private static final String INDICATOR = "indicator";

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private ICompanyEarnedValueCalculator earnedValueCalculator;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private TaskElementAdapter taskElementAdapterCreator;

    @Autowired
    private PredefinedDatabaseSnapshots databaseSnapshots;

    private List<IZoomLevelChangedListener> keepAliveZoomListeners = new ArrayList<>();

    private List<Checkbox> earnedValueChartConfigurationCheckboxes = new ArrayList<>();

    private List<IChartVisibilityChangedListener> keepAliveChartVisibilityListeners = new ArrayList<>();

    private Scenario currentScenario;

    private LocalDate filterStartDate;

    private LocalDate filterFinishDate;

    private Boolean filterExcludeFinishedProject;

    private static final class TaskElementNavigator implements IStructureNavigator<TaskElement> {

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
            return object != null && object instanceof TaskMilestone;
        }
    }

    @Override
    public void setTabsController(MultipleTabsPlannerController tabsController) {
    }

    @Override
    @Transactional(readOnly = true)
    public void setConfigurationToPlanner(final Planner planner, Collection<ICommandOnTask<TaskElement>> additional,
                                          ICommandOnTask<TaskElement> doubleClickCommand,
                                          TaskGroupPredicate predicate) {

        currentScenario = scenarioManager.getCurrent();
        final PlannerConfiguration<TaskElement> configuration = createConfiguration(predicate);

        User user;
        try {
            user = this.userDAO.findByLoginName(SecurityUtils.getSessionUserLoginName());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        boolean expandPlanningViewChart = user.isExpandCompanyPlanningViewCharts();
        configuration.setExpandPlanningViewCharts(expandPlanningViewChart);

        boolean projectsFilterFinished = user.isProjectsFilterFinishedOn();
        configuration.setFilterExcludeFinishedProject(projectsFilterFinished);

        final Tabbox chartComponent = new Tabbox();
        chartComponent.setOrient("vertical");
        chartComponent.setHeight("200px");
        appendTabs(chartComponent);
        appendTabpanels(chartComponent);

        configuration.setChartComponent(chartComponent);
        if ( doubleClickCommand != null ) {
            configuration.setDoubleClickCommand(doubleClickCommand);
        }

        addAdditionalCommands(additional, configuration);
        addPrintSupport(configuration);
        disableSomeFeatures(configuration);
        setDefaultButtonState(configuration,user);

        planner.setInitialZoomLevel(getZoomLevel(configuration));

        configuration.setSecondLevelModifiers(BankHolidaysMarker.create(getDefaultCalendar()));
        planner.setConfiguration(configuration);

        setupZoomLevelListener(planner);

        if (expandPlanningViewChart) {
            // If the chart is expanded, we load the data now
            setupChartAndItsContent(planner, chartComponent);
        }
        else {
            // If the chart is not expanded, we load the data later with a listener
            planner.getFellow("graphics").addEventListener("onOpen", new EventListener() {
                @Override
                public void onEvent(Event event) {
                    transactionService.runOnReadOnlyTransaction((IOnTransaction<Void>) () -> {
                        setupChartAndItsContent(planner, chartComponent);
                        return null;
                    });

                    // Data is loaded only once, then we remove the listener
                    event.getTarget().removeEventListener("onOpen", this);
                }
            });
        }
    }

    private void setDefaultButtonState(PlannerConfiguration<TaskElement> configuration, User user) {

// set initial button show mode
    if ( user.isShowAdvancesOn() ) {
        configuration.setShowAdvancesOn(user.isShowAdvancesOn());
	}

    if ( user.isShowReportedHoursOn() ) {
        configuration.setShowReportedHoursOn(user.isShowReportedHoursOn());
    }

    if ( user.isShowLabelsOn() ) {
        configuration.setShowLabelsOn(user.isShowLabelsOn());
    }

}

    private ZoomLevel getZoomLevel(PlannerConfiguration<TaskElement> configuration) {
        ZoomLevel sessionZoom = FilterUtils.readZoomLevelCompanyView();

        if ( sessionZoom != null ) {
            return sessionZoom;
        }

        return OrderPlanningModel.calculateDefaultLevel(configuration);
    }

    private void setupZoomLevelListener(Planner planner) {
        planner.getTimeTracker().addZoomListener(getSessionZoomLevelListener());
    }

    private IZoomLevelChangedListener getSessionZoomLevelListener() {
        IZoomLevelChangedListener zoomListener = detailLevel -> FilterUtils.writeZoomLevelCompanyView(detailLevel);

        keepAliveZoomListeners.add(zoomListener);

        return zoomListener;
    }

    private BaseCalendar getDefaultCalendar() {
        return configurationDAO.getConfiguration().getDefaultCalendar();
    }

    private void setupChartAndItsContent(final Planner planner, final Tabbox chartComponent) {
        Timeplot chartLoadTimeplot = createEmptyTimeplot();

        appendTab(chartComponent, appendLoadChartAndLegend(new Tabpanel(), chartLoadTimeplot));

        setupChart(chartLoadTimeplot, new CompanyLoadChartFiller(), planner);

        chartComponent.getTabs().getLastChild().addEventListener(Events.ON_SELECT, new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                createOnDemandEarnedValueTimePlot(chartComponent, planner);
                event.getTarget().removeEventListener(Events.ON_SELECT, this);
            }
        });
    }

    private void createOnDemandEarnedValueTimePlot(final Tabbox chartComponent, final Planner planner){
        transactionService.runOnReadOnlyTransaction((IOnTransaction<Void>) () -> {
            Timeplot chartEarnedValueTimeplot = createEmptyTimeplot();
            CompanyEarnedValueChartFiller earnedValueChartFiller = new CompanyEarnedValueChartFiller();
            earnedValueChartFiller.calculateValues(planner.getTimeTracker().getRealInterval());
            Tabpanel earnedValueTabpanel = new Tabpanel();

            appendEarnedValueChartAndLegend(earnedValueTabpanel, chartEarnedValueTimeplot, earnedValueChartFiller);
            appendTab(chartComponent, earnedValueTabpanel);
            Chart chart = setupChart(chartEarnedValueTimeplot, earnedValueChartFiller, planner);
            setEventListenerConfigurationCheckboxes(chart);

            Writer out = new StringWriter();
            try {
                earnedValueTabpanel.redraw(out);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        });
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

    private void appendTabpanels(Tabbox chartComponent){
        chartComponent.appendChild(new Tabpanels());
    }

    private void appendTab(Tabbox chartComponent, Tabpanel panel){
        chartComponent.getTabpanels().appendChild(panel);
    }

    private void appendEventListenerToDateboxIndicators(final CompanyEarnedValueChartFiller earnedValueChartFiller,
                                                        final Vbox vbox,
                                                        final Datebox datebox) {

        datebox.addEventListener(Events.ON_CHANGE,  event -> {
            LocalDate date = new LocalDate(datebox.getValue());
            updateEarnedValueChartLegend(vbox, earnedValueChartFiller, date);
            dateInFutureMessage(datebox);
        });
    }

    private void updateEarnedValueChartLegend(Vbox vbox,
                                              CompanyEarnedValueChartFiller earnedValueChartFiller,
                                              LocalDate date) {

        for (EarnedValueType type : EarnedValueType.values()) {
            Label valueLabel = (Label) vbox.getFellow(type.toString());
            valueLabel.setValue(getLabelTextEarnedValueType(earnedValueChartFiller, type, date));
        }
    }

    private void dateInFutureMessage(Datebox datebox) {
        Date value = datebox.getValue();
        Date today = LocalDate.fromDateFields(new Date()).toDateTimeAtStartOfDay().toDate();

        if ( value != null && (value.compareTo(today) > 0) ) {
            throw new WrongValueException(datebox, _("date in the future"));
        }
    }

    public static Tabpanel appendLoadChartAndLegend(Tabpanel loadChartPanel, Timeplot loadChart) {
        return appendLoadChartAndLegend(loadChartPanel, Emitter.withInitial(loadChart));
    }

    public static Tabpanel appendLoadChartAndLegend(Tabpanel loadChartPanel, Emitter<Timeplot> loadChartEmitter) {
        Hbox hbox = new Hbox();
        hbox.appendChild(getLoadChartLegend());

        final Div div = new Div();
        Timeplot timePlot = loadChartEmitter.getLastValue();

        if (timePlot != null) {
            div.appendChild(timePlot);
        }

        loadChartEmitter.addListener(timePlot1 -> {
            div.getChildren().clear();
            if (timePlot1 != null) {
                div.appendChild(timePlot1);
            }
        });

        div.setSclass("plannergraph");
        hbox.appendChild(div);

        loadChartPanel.appendChild(hbox);

        return loadChartPanel;
    }

    public static org.zkoss.zk.ui.Component getLoadChartLegend() {
        Hbox hbox = new Hbox();
        hbox.setClass("legend-container");
        hbox.setAlign(CENTER);
        hbox.setPack(CENTER);
        Executions.createComponents("/planner/_legendLoadChartCompany.zul", hbox, null);

        return hbox;
    }

    private void appendEarnedValueChartAndLegend(Tabpanel earnedValueChartPanel,
                                                 Timeplot chartEarnedValueTimeplot,
                                                 CompanyEarnedValueChartFiller earnedValueChartFiller) {

        Vbox vbox = new Vbox();
        vbox.setClass("legend-container");
        vbox.setAlign(CENTER);
        vbox.setPack(CENTER);

        Hbox dateHbox = new Hbox();
        dateHbox.appendChild(new Label(_("Select date")));

        LocalDate initialDate = earnedValueChartFiller.initialDateForIndicatorValues();
        Datebox datebox = new Datebox(initialDate.toDateTimeAtStartOfDay().toDate());
        dateHbox.appendChild(datebox);

        appendEventListenerToDateboxIndicators(earnedValueChartFiller, vbox, datebox);
        vbox.appendChild(dateHbox);

        vbox.appendChild(getEarnedValueChartConfigurableLegend(earnedValueChartFiller, initialDate));

        Hbox hbox = new Hbox();
        hbox.setSclass("earned-value-chart");

        hbox.appendChild(vbox);

        Div div = new Div();
        div.appendChild(chartEarnedValueTimeplot);
        div.setSclass("plannergraph");

        hbox.appendChild(div);

        earnedValueChartPanel.appendChild(hbox);
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

        earnedValueChartConfigurationCheckboxes.clear();
        for (EarnedValueType type : EarnedValueType.values()) {
            Checkbox checkbox = new Checkbox(type.getAcronym());
            checkbox.setTooltiptext(type.getName());
            checkbox.setAttribute(INDICATOR, type);
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
                    break;

                default:
                    break;
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

    private String getLabelTextEarnedValueType(CompanyEarnedValueChartFiller earnedValueChartFiller,
                                               EarnedValueType type, LocalDate date) {

        BigDecimal value = earnedValueChartFiller.getIndicator(type, date) != null
                ? earnedValueChartFiller.getIndicator(type, date)
                : BigDecimal.ZERO;

        String units = _("h");

        if (type.equals(EarnedValueType.CPI) || type.equals(EarnedValueType.SPI)) {
            value = value.multiply(new BigDecimal(100));
            units = "%";
        }

        return value.intValue() + " " + units;
    }

    private void markAsSelectedDefaultIndicators() {
        for (Checkbox checkbox : earnedValueChartConfigurationCheckboxes) {

            EarnedValueType type = (EarnedValueType) checkbox.getAttribute(INDICATOR);

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
        Set<EarnedValueType> result = new HashSet<>();

        for (Checkbox checkbox : earnedValueChartConfigurationCheckboxes) {
            if (checkbox.isChecked()) {

                EarnedValueType type = (EarnedValueType) checkbox.getAttribute(INDICATOR);
                result.add(type);
            }
        }

        return result;
    }

    private void setEventListenerConfigurationCheckboxes(final Chart earnedValueChart) {
        for (Checkbox checkbox : earnedValueChartConfigurationCheckboxes) {
            checkbox.addEventListener(Events.ON_CHECK, event ->
                    transactionService.runOnReadOnlyTransaction((IOnTransaction<Void>) () -> {
                        earnedValueChart.fillChart();
                        return null;
                    }));
        }
    }

    private void disableSomeFeatures(PlannerConfiguration<TaskElement> configuration) {
        configuration.setAddingDependenciesEnabled(false);
        configuration.setMovingTasksEnabled(false);
        configuration.setResizingTasksEnabled(false);
        configuration.setCriticalPathEnabled(false);
        configuration.setExpandAllEnabled(false);
        configuration.setFlattenTreeEnabled(false);
        configuration.setRenamingTasksEnabled(false);
        configuration.setTreeEditable(false);
        configuration.setShowAllResourcesEnabled(false);
        configuration.setMoneyCostBarEnabled(false);
    }

    private void addAdditionalCommands(Collection<ICommandOnTask<TaskElement>> additional,
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
            public void doPrint(Map<String, String> parameters, Planner planner) {
                CutyPrint.print(parameters, planner);
            }

        });
    }

    private Chart setupChart(Timeplot chartComponent, IChartFiller loadChartFiller, Planner planner) {
        TimeTracker timeTracker = planner.getTimeTracker();
        Chart loadChart = new Chart(chartComponent, loadChartFiller, timeTracker);
        loadChart.setZoomLevel(planner.getZoomLevel());

        if (planner.isVisibleChart()) {
            loadChart.fillChart();
        }

        timeTracker.addZoomListener(fillOnZoomChange(planner, loadChart));
        planner.addChartVisibilityListener(fillOnChartVisibilityChange(loadChart));

        return loadChart;
    }

    private IChartVisibilityChangedListener fillOnChartVisibilityChange(final Chart loadChart) {
        IChartVisibilityChangedListener chartVisibilityChangedListener =
                visible -> transactionService.runOnReadOnlyTransaction((IOnTransaction<Void>) () -> {
                    if (visible) {
                        loadChart.fillChart();
                    }

                    return null;
                });

        keepAliveChartVisibilityListeners.add(chartVisibilityChangedListener);

        return chartVisibilityChangedListener;
    }

    private IZoomLevelChangedListener fillOnZoomChange(final Planner planner, final Chart loadChart) {

        IZoomLevelChangedListener zoomListener = detailLevel -> {
            loadChart.setZoomLevel(detailLevel);

            transactionService.runOnReadOnlyTransaction((IOnTransaction<Void>) () -> {
                if (planner.isVisibleChart()) {
                    loadChart.fillChart();
                }

                return null;
            });
        };

        keepAliveZoomListeners.add(zoomListener);

        return zoomListener;
    }

    private PlannerConfiguration<TaskElement> createConfiguration(TaskGroupPredicate predicate) {
        return new PlannerConfiguration<>(
                taskElementAdapterCreator.createForCompany(currentScenario),
                new TaskElementNavigator(),
                retainOnlyTopLevel(predicate));
    }

    private List<TaskElement> retainOnlyTopLevel(TaskGroupPredicate predicate) {
        List<TaskElement> result = new ArrayList<>();

        List<Order> list = getOrders(predicate);
        for (Order order : list) {
            order.useSchedulingDataFor(currentScenario, false);
            TaskGroup associatedTaskElement = order.getAssociatedTaskElement();

            if (associatedTaskElement != null) {
                if (predicate != null) {
                    if (!predicate.accepts(associatedTaskElement)) {
                        // If predicate doesn't accept the element we move on to the next order
                        continue;
                    }
                }
                associatedTaskElement.setSimplifiedAssignedStatusCalculationEnabled(true);
                result.add(associatedTaskElement);
            }
        }
        Collections.sort(result, (arg0, arg1) -> arg0.getStartDate().compareTo(arg1.getStartDate()));

        return result;
    }

    private List<Order> getOrders(TaskGroupPredicate predicate) {
        String username = SecurityUtils.getSessionUserLoginName();

        Date startDate = predicate.getStartDate();
        Date endDate = predicate.getFinishDate();
        Boolean excludeFinishedProject = predicate.getExcludeFinishedProjects();
        List<org.libreplan.business.labels.entities.Label> labels = new ArrayList<>();
        List<Criterion> criteria = new ArrayList<>();
        ExternalCompany customer = null;
        OrderStatusEnum state = null;

        for (FilterPair filterPair : predicate.getFilters()) {
            TaskGroupFilterEnum type = (TaskGroupFilterEnum) filterPair.getType();

            switch (type) {

                case Label:
                    labels.add((org.libreplan.business.labels.entities.Label) filterPair.getValue());
                    break;

                case Criterion:
                    criteria.add((Criterion) filterPair.getValue());
                    break;

                case ExternalCompany:
                    if (customer != null) {
                        // It's impossible to have an Order associated to more than 1 customer
                        return Collections.emptyList();
                    }
                    customer = (ExternalCompany) filterPair.getValue();
                    break;

                case State:
                    if (state != null) {
                        // It's impossible to have an Order associated with more than 1 state
                        return Collections.emptyList();
                    }
                    state = (OrderStatusEnum) filterPair.getValue();
                    break;

                default:
                    break;
            }
        }

        return orderDAO.getOrdersByReadAuthorizationBetweenDatesByLabelsCriteriaCustomerAndState(
                username, currentScenario, startDate, endDate, labels, criteria, customer, state, excludeFinishedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskGroupPredicate getDefaultPredicate() {

        Date startDate = FilterUtils.readProjectsStartDate();
        Date endDate = FilterUtils.readProjectsEndDate();
        String name = FilterUtils.readProjectsName();
        Boolean projectsFinished = FilterUtils.readExcludeFinishedProjects();

        if ( projectsFinished == null ) {
            User user = getUser();
            projectsFinished = user.isProjectsFilterFinishedOn();
        }

        boolean calculateStartDate = startDate == null;
        boolean calculateEndDate = endDate == null;

        // Filter predicate needs to be calculated based on the projects dates
        if ( (calculateStartDate) || (calculateEndDate) ) {

            if ( currentScenario == null ) {
                currentScenario = scenarioManager.getCurrent();
            }

            List<Order> list = orderDAO.getOrdersByReadAuthorizationByScenario(
                    SecurityUtils.getSessionUserLoginName(), currentScenario);

            for (Order each : list) {
                each.useSchedulingDataFor(currentScenario, false);
                TaskGroup associatedTaskElement = each.getAssociatedTaskElement();

                if ( associatedTaskElement != null && STATUS_VISUALIZED.contains(each.getState()) ) {

                    if ( calculateStartDate ) {
                        startDate = Collections.min(
                                notNull(startDate, each.getInitDate(), associatedTaskElement.getStartDate()));
                    }

                    if ( calculateEndDate ) {
                        endDate = Collections.max(
                                notNull(endDate, each.getDeadline(), associatedTaskElement.getEndDate()));
                    }
                }
            }
        }
        filterStartDate = startDate != null ? LocalDate.fromDateFields(startDate) : null;
        filterFinishDate = endDate != null ? LocalDate.fromDateFields(endDate) : null;
        //filterExcludeFinishedProject =  projectsFinished != null ? projectsFinished : false;

        return new TaskGroupPredicate(null, startDate, endDate, name, projectsFinished);
    }

    private static <T> List<T> notNull(T... values) {
        List<T> result = new ArrayList<>();

        for (T each : values) {
            if (each != null) {
                result.add(each);
            }
        }

        return result;
    }

    @Override
    public Date getFilterStartDate() {
        return filterStartDate == null ? null : filterStartDate.toDateTimeAtStartOfDay().toDate();
    }
    @Override
    public Date getFilterFinishDate() {
        return filterStartDate == null ? null : filterFinishDate.toDateTimeAtStartOfDay().toDate();
    }

    private class CompanyLoadChartFiller extends StandardLoadChartFiller {

        @Override
        protected String getOptionalJavascriptCall() {
            return "ganttz.GanttPanel.getInstance().timeplotContainerRescroll()";
        }

        @Override
        protected ILoadChartData getDataOn(Interval interval) {
            ResourceLoadChartData data = databaseSnapshots.snapshotResourceLoadChartData();
            return data.on(getStart(filterStartDate, interval), getEnd(filterFinishDate, interval));
        }

    }

    /**
     * Calculates 'Earned Value' indicators and set them in the Company 'Earned Valued' chart.
     *
     * @author Manuel Rego Casasnovas <mrego@igalia.com>
     * @author Diego Pino García <dpino@igalia.com>
     */
    private class CompanyEarnedValueChartFiller extends EarnedValueChartFiller {

        public CompanyEarnedValueChartFiller() {
            super.setEarnedValueCalculator(earnedValueCalculator);
        }

        @Override
        protected void calculateBudgetedCostWorkScheduled(Interval interval) {
            setIndicatorInInterval(
                    EarnedValueType.BCWS,
                    interval,
                    earnedValueCalculator.calculateBudgetedCostWorkScheduled(getFilterInterval()));
        }

        @Override
        protected void calculateActualCostWorkPerformed(Interval interval) {
            setIndicatorInInterval(
                    EarnedValueType.ACWP,
                    interval,
                    earnedValueCalculator.calculateActualCostWorkPerformed(getFilterInterval()));
        }

        @Override
        protected void calculateBudgetedCostWorkPerformed(Interval interval) {
            setIndicatorInInterval(
                    EarnedValueType.BCWP,
                    interval,
                    earnedValueCalculator.calculateBudgetedCostWorkPerformed(getFilterInterval()));
        }

        @Override
        protected Set<EarnedValueType> getSelectedIndicators() {
            return getEarnedValueSelectedIndicators();
        }

        private AvailabilityTimeLine.Interval getFilterInterval() {
            return AvailabilityTimeLine.Interval.create(filterStartDate, filterFinishDate);
        }

    }

    @Override
    @Transactional(readOnly=true)
    public ProgressType getProgressTypeFromConfiguration() {
        return configurationDAO.getConfiguration().getProgressType();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser() {
        User user;
        try {
            user = this.userDAO.findByLoginName(SecurityUtils.getSessionUserLoginName());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        // Attach filter bandbox elements
        if (user.getProjectsFilterLabel() != null) {
            user.getProjectsFilterLabel().getFinderPattern();
        }

        return user;
    }

    public Boolean getFilterExcludeFinishedProject() {
        return filterExcludeFinishedProject;
    }

    public void setFilterExcludeFinishedProject(Boolean filterExcludeFinishedProject) {
        this.filterExcludeFinishedProject = filterExcludeFinishedProject;
    }

}
