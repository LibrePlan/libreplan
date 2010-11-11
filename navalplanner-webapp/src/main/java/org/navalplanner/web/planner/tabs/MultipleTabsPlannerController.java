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
package org.navalplanner.web.planner.tabs;

import static org.navalplanner.web.I18nHelper._;
import static org.zkoss.ganttz.adapters.TabsConfiguration.configure;

import java.util.Map;

import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.templates.entities.OrderTemplate;
import org.navalplanner.web.common.entrypoints.URLHandler;
import org.navalplanner.web.common.entrypoints.URLHandlerRegistry;
import org.navalplanner.web.limitingresources.LimitingResourcesController;
import org.navalplanner.web.montecarlo.MonteCarloController;
import org.navalplanner.web.orders.OrderCRUDController;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.IBack;
import org.navalplanner.web.planner.company.CompanyPlanningController;
import org.navalplanner.web.planner.order.IOrderPlanningGate;
import org.navalplanner.web.planner.order.OrderPlanningController;
import org.navalplanner.web.planner.tabs.Mode.ModeTypeChangedListener;
import org.navalplanner.web.resourceload.ResourceLoadController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.TabSwitcher;
import org.zkoss.ganttz.TabsRegistry;
import org.zkoss.ganttz.TabsRegistry.IBeforeShowAction;
import org.zkoss.ganttz.adapters.State;
import org.zkoss.ganttz.adapters.TabsConfiguration;
import org.zkoss.ganttz.adapters.TabsConfiguration.ChangeableTab;
import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.ganttz.extensions.TabProxy;
import org.zkoss.ganttz.resourceload.ResourcesLoadPanel.IToolbarCommand;
import org.zkoss.ganttz.util.LongOperationFeedback;
import org.zkoss.ganttz.util.LongOperationFeedback.ILongOperation;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Composer;

/**
 * Creates and handles several tabs
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MultipleTabsPlannerController implements Composer,
        IGlobalViewEntryPoints {

    private final class TabWithLoadingFeedback extends TabProxy {
        private boolean feedback = true;

        private TabWithLoadingFeedback(ITab tab) {
            super(tab);
        }

        @Override
        public void show() {
            if (feedback) {
                showWithFeedback();
            } else {
                showWithoutFeedback();
            }
        }

        private void showWithFeedback() {
            LongOperationFeedback.execute(tabsSwitcher,
                new ILongOperation() {

                    @Override
                    public String getName() {
                        return _("changing perspective");
                    }

                    @Override
                    public void doAction() throws Exception {
                        proxiedTab.show();
                    }
                });
        }

        private void showWithoutFeedback() {
            proxiedTab.show();
        }

        public void toggleToNoFeedback() {
            feedback = false;
        }

        public void toggleToFeedback() {
            feedback = true;
        }
    }

    public static final String PLANNIFICATION = _("Scheduling");

    public static final String BREADCRUMBS_SEPARATOR = "/common/img/migas_separacion.gif";

    private Mode mode = Mode.initial();

    @Autowired
    private CompanyPlanningController companyPlanningController;

    @Autowired
    private OrderCRUDController orderCRUDController;

    private TabWithLoadingFeedback planningTab;

    private ITab resourceLoadTab;

    private ITab limitingResourcesTab;

    private ITab monteCarloTab;

    private ITab ordersTab;

    private ITab advancedAllocationTab;

    private TabSwitcher tabsSwitcher;

    @Autowired
    private OrderPlanningController orderPlanningController;

    @Autowired
    private ResourceLoadController resourceLoadController;

    @Autowired
    private ResourceLoadController resourceLoadControllerGlobal;

    @Autowired
    private LimitingResourcesController limitingResourcesController;

    @Autowired
    private MonteCarloController monteCarloController;

    @Autowired
    private LimitingResourcesController limitingResourcesControllerGlobal;

    private org.zkoss.zk.ui.Component breadcrumbs;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private URLHandlerRegistry registry;

    @Autowired
    private IScenarioManager scenarioManager;

    private TabsConfiguration buildTabsConfiguration() {

        Map<String, String[]> parameters = getURLQueryParametersMap();

        planningTab = doFeedbackOn(PlanningTabCreator.create(mode,
                companyPlanningController, orderPlanningController, orderDAO,
                breadcrumbs, parameters, this));

        resourceLoadTab = ResourcesLoadTabCreator.create(mode,
                resourceLoadController, upCommand(),
                resourceLoadControllerGlobal, new IOrderPlanningGate() {

                    @Override
                    public void goToScheduleOf(Order order) {
                        getTabsRegistry()
                                .show(planningTab, changeModeTo(order));
                    }

                    @Override
                    public void goToOrderDetails(Order order) {
                        // it do nothing
                    }

                    @Override
                    public void goToTaskResourceAllocation(Order order,
                            TaskElement task) {
                        orderPlanningController.setShowedTask(task);
                        getTabsRegistry()
                                .show(planningTab, changeModeTo(order));
                    }

                }, breadcrumbs);

        limitingResourcesTab = LimitingResourcesTabCreator.create(mode,
                limitingResourcesController, upCommand(),
                limitingResourcesControllerGlobal, breadcrumbs);

        ordersTab = OrdersTabCreator.create(mode, orderCRUDController,
                breadcrumbs, new IOrderPlanningGate() {

                    @Override
                    public void goToScheduleOf(Order order) {
                        getTabsRegistry()
                                .show(planningTab, changeModeTo(order));
                    }

                    @Override
                    public void goToOrderDetails(Order order) {
                        getTabsRegistry().show(ordersTab, changeModeTo(order));
                    }

                    @Override
                    public void goToTaskResourceAllocation(Order order,
                            TaskElement task) {
                        // do nothing
                    }

                }, parameters);

        final boolean isMontecarloVisible = isMonteCarloVisible();
        if (isMontecarloVisible) {
            monteCarloTab = MonteCarloTabCreator.create(mode, monteCarloController,
                    orderPlanningController, breadcrumbs);
        }

        final State<Void> typeChanged = typeChangedState();
        advancedAllocationTab = doFeedbackOn(AdvancedAllocationTabCreator
                .create(mode,
                transactionService, orderDAO, taskElementDAO, resourceDAO,
                scenarioManager.getCurrent(), returnToPlanningTab()));

        TabsConfiguration tabsConfiguration = TabsConfiguration.create()
            .add(tabWithNameReloading(planningTab, typeChanged))
            .add(tabWithNameReloading(resourceLoadTab, typeChanged))
            .add(tabWithNameReloading(limitingResourcesTab, typeChanged))
            .add(tabWithNameReloading(ordersTab, typeChanged))
            .add(visibleOnlyAtOrderMode(advancedAllocationTab));

        if (isMontecarloVisible) {
            tabsConfiguration.add(visibleOnlyAtOrderMode(monteCarloTab));
        }

        return tabsConfiguration;
    }

    private boolean isMonteCarloVisible() {
        Boolean result = configurationDAO.getConfiguration().isMonteCarloMethodTabVisible();
        return result != null ? result.booleanValue() : false;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String[]> getURLQueryParametersMap() {
        return Executions.getCurrent().getParameterMap();
    }

    private TabWithLoadingFeedback doFeedbackOn(ITab tab) {
        return new TabWithLoadingFeedback(tab);
    }

    private IBack returnToPlanningTab() {
        return new IBack() {
            private String eventName = "onShowPlanningTab";
            {
                tabsSwitcher.addEventListener(eventName, showPlanningTab());
            }

            private EventListener showPlanningTab() {
                return new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        getTabsRegistry().show(planningTab);
                    }
                };
            }

            @Override
            public void goBack() {
                notGoBackImmediately();
            }

            private void notGoBackImmediately() {
                Events.postEvent(new Event(eventName, tabsSwitcher));
            }

            @Override
            public boolean isAdvanceAssignmentOfSingleTask() {
                return false;
            }
        };
    }

    private ChangeableTab tabWithNameReloading(ITab tab,
            final State<Void> typeChanged) {
        return configure(tab).reloadNameOn(typeChanged);
    }

    private State<Void> typeChangedState() {
        final State<Void> typeChanged = State.create();
        mode.addListener(new ModeTypeChangedListener() {

            @Override
            public void typeChanged(ModeType oldType, ModeType newType) {
                typeChanged.changeValueTo(null);
            }
        });
        return typeChanged;
    }

    private ChangeableTab visibleOnlyAtOrderMode(ITab tab) {
        final State<Boolean> state = State.create(mode.isOf(ModeType.ORDER));
        ChangeableTab result = configure(tab).visibleOn(state);
        mode.addListener(new ModeTypeChangedListener() {

            @Override
            public void typeChanged(ModeType oldType, ModeType newType) {
                state.changeValueTo(ModeType.ORDER == newType);
            }
        });
        return result;
    }

    @Override
    @Transactional(readOnly=true)
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        Planner.registerNeededScripts();
        tabsSwitcher = (TabSwitcher) comp;
        breadcrumbs = comp.getPage().getFellow("breadcrumbs");
        tabsSwitcher.setConfiguration(buildTabsConfiguration());
        final URLHandler<IGlobalViewEntryPoints> handler = registry
                .getRedirectorFor(IGlobalViewEntryPoints.class);
        if (!handler.applyIfMatches(this)) {
            planningTab.toggleToNoFeedback();
            goToCompanyScheduling();
            planningTab.toggleToFeedback();
        }
        handler.registerListener(this, comp.getPage());
    }

    private TabsRegistry getTabsRegistry() {
        return tabsSwitcher.getTabsRegistry();
    }

    private IToolbarCommand upCommand() {
        return new IToolbarCommand() {

            @Override
            public void doAction() {
                mode.up();
            }

            @Override
            public String getLabel() {
                return _("Up to company view");
            }

            @Override
            public String getImage() {
                return "/common/img/ico_up.png";
            }
        };
    }

    @Override
    public void goToCompanyScheduling() {
        getTabsRegistry().show(planningTab);
    }

    @Override
    public void goToCompanyLoad() {
        getTabsRegistry().show(resourceLoadTab);
    }

    @Override
    public void goToCompanyLimitingResources() {
        getTabsRegistry().show(limitingResourcesTab);
    }

    @Override
    public void goToOrdersList() {
        // ordersTab.show();
        getTabsRegistry().show(ordersTab);
    }

    public void goToCreateForm() {
        getTabsRegistry().show(ordersTab);
        orderCRUDController.goToCreateForm();
    }

    @Override
    public void goToOrder(Order order) {
        getTabsRegistry().show(planningTab, changeModeTo(order));
    }

    @Override
    public void goToOrderElementDetails(OrderElement orderElement, Order order) {
        getTabsRegistry().show(ordersTab, changeModeTo(order));
        orderCRUDController.highLight(orderElement);
    }

    @Override
    public void goToLimitingResources() {
        getTabsRegistry().show(limitingResourcesTab);
    }

    @Override
    public void goToOrderDetails(Order order) {
        getTabsRegistry().show(ordersTab, changeModeTo(order));
    }

    @Override
    public void goToResourcesLoad(Order order) {
        getTabsRegistry().show(resourceLoadTab, changeModeTo(order));
    }

    @Override
    public void goToAdvancedAllocation(Order order) {
        getTabsRegistry().show(advancedAllocationTab, changeModeTo(order));
    }

    @Override
    public void goToCreateotherOrderFromTemplate(OrderTemplate template) {
        getTabsRegistry().show(ordersTab);
        orderCRUDController.showCreateFormFromTemplate(template);
    }

    private IBeforeShowAction changeModeTo(final Order order) {
        return new IBeforeShowAction() {
            @Override
            public void doAction() {
                mode.goToOrderMode(order);
            }
        };
    }
}
