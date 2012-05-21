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
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
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
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
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
import org.zkoss.ganttz.IPredicate;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.adapters.PlannerConfiguration.IPrintAction;
import org.zkoss.ganttz.extensions.ICommandOnTask;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Emitter;
import org.zkoss.ganttz.util.Emitter.IEmissionListener;
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
import org.zkoss.zul.South;
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

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private ICompanyEarnedValueCalculator earnedValueCalculator;

    private List<IZoomLevelChangedListener> keepAliveZoomListeners = new ArrayList<IZoomLevelChangedListener>();

    private List<Checkbox> earnedValueChartConfigurationCheckboxes = new ArrayList<Checkbox>();

    private MultipleTabsPlannerController tabs;

    private List<IChartVisibilityChangedListener> keepAliveChartVisibilityListeners = new ArrayList<IChartVisibilityChangedListener>();

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private TaskElementAdapter taskElementAdapterCreator;

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

    private static final class TaskElementNavigator implements
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

        User user;
        try {
            user = this.userDAO.findByLoginName(SecurityUtils
                    .getSessionUserLoginName());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        boolean expandPlanningViewChart = user
                .isExpandCompanyPlanningViewCharts();
        configuration.setExpandPlanningViewCharts(expandPlanningViewChart);

        final Tabbox chartComponent = new Tabbox();
        chartComponent.setOrient("vertical");
        chartComponent.setHeight("200px");
        appendTabs(chartComponent);
        appendTabpanels(chartComponent);

        configuration.setChartComponent(chartComponent);
        if (doubleClickCommand != null) {
            configuration.setDoubleClickCommand(doubleClickCommand);
        }

        addAdditionalCommands(additional, configuration);
        addPrintSupport(configuration);
        disableSomeFeatures(configuration);

        ZoomLevel defaultZoomLevel = OrderPlanningModel
                .calculateDefaultLevel(configuration);
        OrderPlanningModel.configureInitialZoomLevelFor(planner,
                defaultZoomLevel);

        configuration.setSecondLevelModificators(BankHolidaysMarker.create(getDefaultCalendar()));
        planner.setConfiguration(configuration);

        if(expandPlanningViewChart) {
            //if the chart is expanded, we load the data now
            setupChartAndItsContent(planner, chartComponent);
        }
        else {
            //if the chart is not expanded, we load the data later with a listener
            ((South) planner.getFellow("graphics"))
                    .addEventListener("onOpen",
                new EventListener() {
                    @Override
                    public void onEvent(Event event) {
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

    private BaseCalendar getDefaultCalendar() {
        return configurationDAO.getConfiguration().getDefaultCalendar();
    }

    private void setupChartAndItsContent(final Planner planner,
           final Tabbox chartComponent) {
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
        transactionService.runOnReadOnlyTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
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
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                return null;
            }
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

    private void appendEventListenerToDateboxIndicators(
            final CompanyEarnedValueChartFiller earnedValueChartFiller,
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

    private void dateInfutureMessage(Datebox datebox) {
        Date value = datebox.getValue();
        Date today = LocalDate.fromDateFields(new Date())
                .toDateTimeAtStartOfDay().toDate();
        if (value != null && (value.compareTo(today) > 0)) {
            throw new WrongValueException(datebox, _("date in future"));
        }
    }

    public static Tabpanel appendLoadChartAndLegend(Tabpanel loadChartPannel,
            Timeplot loadChart) {
        return appendLoadChartAndLegend(loadChartPannel,
                Emitter.withInitial(loadChart));
    }

    public static Tabpanel appendLoadChartAndLegend(Tabpanel loadChartPannel,
            Emitter<Timeplot> loadChartEmitter) {
        Hbox hbox = new Hbox();
        hbox.appendChild(getLoadChartLegend());

        final Div div = new Div();
        Timeplot timePlot = loadChartEmitter.getLastValue();
        if (timePlot != null) {
            div.appendChild(timePlot);
        }
        loadChartEmitter.addListener(new IEmissionListener<Timeplot>() {

            @Override
            public void newEmission(Timeplot timePlot) {
                div.getChildren().clear();
                if (timePlot != null) {
                    div.appendChild(timePlot);
                }
            }
        });
        div.setSclass("plannergraph");
        hbox.appendChild(div);

        loadChartPannel.appendChild(hbox);
        return loadChartPannel;
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
        dateHbox.appendChild(new Label(_("Select date")));

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

            BigDecimal value = earnedValueChartFiller.getIndicator(type, date) != null ? earnedValueChartFiller
                    .getIndicator(type, date)
                    : BigDecimal.ZERO;
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
        configuration.setMoneyCostBarEnabled(false);
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
        return new PlannerConfiguration<TaskElement>(
                taskElementAdapterCreator.createForCompany(currentScenario),
                new TaskElementNavigator(), retainOnlyTopLevel(predicate));
    }

    private List<TaskElement> retainOnlyTopLevel(IPredicate predicate) {
        List<TaskElement> result = new ArrayList<TaskElement>();
        User user;

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
                    && (predicate == null || predicate
                            .accepts(associatedTaskElement))) {
                associatedTaskElement.setSimplifiedAssignedStatusCalculationEnabled(true);
                result.add(associatedTaskElement);
            }
        }
        Collections.sort(result,new Comparator<TaskElement>(){
            @Override
            public int compare(TaskElement arg0, TaskElement arg1) {
                return arg0.getStartDate().compareTo(arg1.getStartDate());
            }
        });
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public IPredicate getDefaultPredicate(Boolean includeOrderElements) {
        User user;
        if (currentScenario == null) {
            currentScenario = scenarioManager.getCurrent();
        }
        try {
            user = userDAO.findByLoginName(SecurityUtils.getSessionUserLoginName());
        }
        catch(InstanceNotFoundException e) {
            //this case shouldn't happen, because it would mean that there isn't a logged user
            //anyway, if it happened we return an empty list
            return null;
        }
        List<Order> list = orderDAO.getOrdersByReadAuthorizationByScenario(
                user, currentScenario);
        Date startDate = null;
        Date endDate = null;
        for (Order each : list) {
            each.useSchedulingDataFor(currentScenario, false);
            TaskGroup associatedTaskElement = each.getAssociatedTaskElement();
            if (associatedTaskElement != null
                    && STATUS_VISUALIZED.contains(each.getState())) {
                startDate = Collections.min(notNull(startDate, each.getInitDate(),
                        associatedTaskElement.getStartDate()));
                endDate = Collections.max(notNull(endDate, each.getDeadline(),
                        associatedTaskElement.getEndDate()));
            }
        }
        filterStartDate = startDate != null ? LocalDate
                .fromDateFields(startDate) : null;
        filterFinishDate = endDate != null ? LocalDate.fromDateFields(endDate)
                : null;
        return new TaskGroupPredicate(null, startDate, endDate,
                includeOrderElements);
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

    @Override
    public LocalDate getFilterStartDate() {
        return filterStartDate;
    }

    @Override
    public LocalDate getFilterFinishDate() {
        return filterFinishDate;
    }

    private AvailabilityTimeLine.Interval getFilterInterval() {
        return AvailabilityTimeLine.Interval.create(getFilterStartDate(),
                getFilterFinishDate());
    }

    private class CompanyLoadChartFiller extends StandardLoadChartFiller {

        @Override
        protected String getOptionalJavascriptCall() {
            return "ganttz.GanttPanel.getInstance().timeplotContainerRescroll()";
        }

        @Override
        protected ILoadChartData getDataOn(Interval interval) {
            ResourceLoadChartData data = databaseSnapshots
                    .snapshotResourceLoadChartData();
            return data.on(getStart(filterStartDate, interval),
                    getEnd(filterFinishDate, interval));
        }

    }

    /**
     *
     * @author Manuel Rego Casasnovas <mrego@igalia.com>
     * @author Diego Pino García <dpino@igalia.com>
     *
     *         Calculates 'Earned Value' indicators and set them in the Company
     *         'Earned Valued' chart
     *
     */
    private class CompanyEarnedValueChartFiller extends EarnedValueChartFiller {

        public CompanyEarnedValueChartFiller() {
            super.setEarnedValueCalculator(earnedValueCalculator);
        }

        @Override
        protected void calculateBudgetedCostWorkScheduled(Interval interval) {
            setIndicatorInInterval(EarnedValueType.BCWS, interval,
                    earnedValueCalculator
                            .calculateBudgetedCostWorkScheduled(getFilterInterval()));
        }

        @Override
        protected void calculateActualCostWorkPerformed(Interval interval) {
            setIndicatorInInterval(EarnedValueType.ACWP, interval,
                    earnedValueCalculator
                            .calculateActualCostWorkPerformed(getFilterInterval()));
        }

        @Override
        protected void calculateBudgetedCostWorkPerformed(Interval interval) {
            setIndicatorInInterval(EarnedValueType.BCWP, interval,
                    earnedValueCalculator
                            .calculateBudgetedCostWorkPerformed(getFilterInterval()));
        }

        @Override
        protected Set<EarnedValueType> getSelectedIndicators() {
            return getEarnedValueSelectedIndicators();
        }

    }

    @Override
    @Transactional(readOnly=true)
    public ProgressType getProgressTypeFromConfiguration() {
        return configurationDAO.getConfiguration().getProgressType();
    }

}
