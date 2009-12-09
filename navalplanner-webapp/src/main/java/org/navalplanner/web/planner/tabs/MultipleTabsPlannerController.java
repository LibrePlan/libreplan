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
package org.navalplanner.web.planner.tabs;

import static org.navalplanner.web.I18nHelper._;
import static org.zkoss.ganttz.adapters.TabsConfiguration.configure;

import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.web.common.entrypoints.URLHandler;
import org.navalplanner.web.common.entrypoints.URLHandlerRegistry;
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
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.TabSwitcher;
import org.zkoss.ganttz.TabsRegistry;
import org.zkoss.ganttz.adapters.State;
import org.zkoss.ganttz.adapters.TabsConfiguration;
import org.zkoss.ganttz.adapters.TabsConfiguration.ChangeableTab;
import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.ganttz.extensions.TabProxy;
import org.zkoss.ganttz.resourceload.ResourcesLoadPanel.IToolbarCommand;
import org.zkoss.ganttz.util.LongOperationFeedback;
import org.zkoss.ganttz.util.LongOperationFeedback.ILongOperation;
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

    public static final String PLANNIFICATION = _("Scheduling");

    public static final String BREADCRUMBS_SEPARATOR = "/common/img/migas_separacion.gif";

    private Mode mode = Mode.initial();

    @Autowired
    private CompanyPlanningController companyPlanningController;

    @Autowired
    private OrderCRUDController orderCRUDController;

    private ITab planningTab;

    private ITab resourceLoadTab;

    private ITab ordersTab;

    private TabSwitcher tabsSwitcher;

    @Autowired
    private OrderPlanningController orderPlanningController;

    @Autowired
    private ResourceLoadController resourceLoadController;

    @Autowired
    private ResourceLoadController resourceLoadControllerGlobal;

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
    private URLHandlerRegistry registry;

    private TabsConfiguration buildTabsConfiguration() {
        planningTab = PlanningTabCreator.create(mode,
                companyPlanningController, orderPlanningController, orderDAO,
                breadcrumbs);
        resourceLoadTab = ResourcesLoadTabCreator.create(mode,
                resourceLoadController, upCommand(),
                resourceLoadControllerGlobal,
                breadcrumbs);
        ordersTab = OrdersTabCreator.create(mode, orderCRUDController,
                breadcrumbs, new IOrderPlanningGate() {

                    @Override
                    public void goToScheduleOf(Order order) {
                        mode.goToOrderMode(order);
                        getTabsRegistry().show(planningTab);
                    }

                    @Override
                    public void goToOrderDetails(Order order) {
                        mode.goToOrderMode(order);
                        getTabsRegistry().show(ordersTab);
                    }

                });
        final State<Void> typeChanged = typeChangedState();
        ITab advancedAllocation = AdvancedAllocationTabCreator.create(mode,
                transactionService, orderDAO, taskElementDAO, resourceDAO,
                returnToPlanningTab());
        return TabsConfiguration.create()
            .add(tabWithNameReloading(doFeedbackOn(planningTab), typeChanged))
            .add(tabWithNameReloading(doFeedbackOn(resourceLoadTab), typeChanged))
            .add(tabWithNameReloading(doFeedbackOn(ordersTab), typeChanged))
            .add(visibleOnlyAtOrderMode(doFeedbackOn(advancedAllocation)));
    }

    private ITab doFeedbackOn(ITab tab) {
        return new TabProxy(tab) {
            @Override
            public void show() {
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
        };
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
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        Planner.registerNeededScripts();
        tabsSwitcher = (TabSwitcher) comp;
        breadcrumbs = comp.getPage().getFellow("breadcrumbs");
        tabsSwitcher.setConfiguration(buildTabsConfiguration());
        final URLHandler<IGlobalViewEntryPoints> handler = registry
                .getRedirectorFor(IGlobalViewEntryPoints.class);
        if (!handler.applyIfMatches(this)) {
            goToCompanyScheduling();
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
                return _("Up");
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
    public void goToOrdersList() {
        getTabsRegistry().show(ordersTab);
    }

}
