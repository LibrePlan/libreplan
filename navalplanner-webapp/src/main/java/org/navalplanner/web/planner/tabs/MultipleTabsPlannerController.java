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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.orders.OrderCRUDController;
import org.navalplanner.web.planner.CompanyPlanningController;
import org.navalplanner.web.planner.IOrderPlanningGate;
import org.navalplanner.web.planner.OrderPlanningController;
import org.navalplanner.web.planner.tabs.CreatedOnDemandTab.IComponentCreator;
import org.navalplanner.web.planner.tabs.Mode.ModeTypeChangedListener;
import org.navalplanner.web.resourceload.ResourceLoadController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.TabSwitcher;
import org.zkoss.ganttz.TabsRegistry;
import org.zkoss.ganttz.adapters.TabsConfiguration;
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.ganttz.resourceload.ResourcesLoadPanel.IToolbarCommand;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;

/**
 * Creates and handles several tabs
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MultipleTabsPlannerController implements Composer {

    private final class OrderPlanningTab extends CreatedOnDemandTab {
        private Order lastOrder;
        private ICommand<TaskElement> upCommand = new ICommand<TaskElement>() {

            @Override
            public void doAction(IContext<TaskElement> context) {
                mode.up();
            }

            @Override
            public String getName() {
                return _("Up");
            }
        };

        OrderPlanningTab(String name, IComponentCreator componentCreator) {
            super(name, componentCreator);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void afterShowAction() {
            if (mode.isOf(ModeType.ORDER) && lastOrder != mode.getOrder()) {
                lastOrder = mode.getOrder();
                orderPlanningController.setOrder(lastOrder, upCommand);
            }
        }
    }

    private static final String ORDERS_VIEW = _("Orders List");

    private static final String ORDER_ORDERS_VIEW = _("Order Details");

    public static final String PLANNIFICATION = _("Plannification");

    public static final String BREADCRUMBS_SEPARATOR = "/common/img/migas_separacion.gif";

    private TabsConfiguration tabsConfiguration;

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

    private IComponentCreator ordersTabCreator = new IComponentCreator() {

        private org.zkoss.zk.ui.Component result;

        @Override
        public org.zkoss.zk.ui.Component create(
                org.zkoss.zk.ui.Component parent) {
            if (result != null) {
                return result;
            }
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("orderController", setupOrderCrudController());
            result = Executions.createComponents(
                    "/orders/_ordersTab.zul", parent, args);
            createBindingsFor(result);
            Util.reloadBindings(result);
            return result;
        }

    };

    private org.zkoss.zk.ui.Component breadcrumbs;

    public TabsConfiguration getTabs() {
        if (tabsConfiguration == null) {
            tabsConfiguration = buildTabsConfiguration();
            mode.addListener(new ModeTypeChangedListener() {

                @Override
                public void typeChanged(ModeType oldType, ModeType newType) {
                    for (ITab tab : tabsConfiguration.getTabs()) {
                        tabsSwitcher.getTabsRegistry().loadNewName(tab);
                    }
                }
            });
        }
        return tabsConfiguration;
    }

    private TabsConfiguration buildTabsConfiguration() {
        planningTab = PlanningTabCreator.create(mode, companyPlanningController,
                orderPlanningController, breadcrumbs);
        resourceLoadTab = ResourcesLoadTabCreator.create(mode,
                resourceLoadController, upCommand(), breadcrumbs);
        ordersTab = createOrdersTab();
        return TabsConfiguration.create().add(planningTab).add(resourceLoadTab)
                .add(ordersTab);
    }

    private ITab createOrdersTab() {
        return TabOnModeType.forMode(mode)
                .forType(ModeType.GLOBAL, createGlobalOrdersTab())
                .forType(ModeType.ORDER, createOrderOrdersTab())
                .create();
    }

    private ITab createGlobalOrdersTab() {
        return new CreatedOnDemandTab(ORDERS_VIEW, ordersTabCreator) {
            @Override
            protected void afterShowAction() {
                orderCRUDController.goToList();
                if (breadcrumbs.getChildren() != null) {
                    breadcrumbs.getChildren().clear();
                }
                breadcrumbs.appendChild(new Label("Orders > Orders list"));
            }
        };
    }

    private OrderCRUDController setupOrderCrudController() {
        orderCRUDController.setPlanningControllerEntryPoints(new IOrderPlanningGate() {

            @Override
            public void goToScheduleOf(Order order) {
                mode.goToOrderMode(order);
                getTabsRegistry().show(planningTab);
            }
        });
        orderCRUDController.setActionOnUp(new Runnable() {
            public void run() {
                mode.up();
                orderCRUDController.goToList();
            }
        });
        return orderCRUDController;
    }

    @SuppressWarnings("unchecked")
    static void createBindingsFor(org.zkoss.zk.ui.Component result) {
        List<org.zkoss.zk.ui.Component> children = new ArrayList<org.zkoss.zk.ui.Component>(
                result.getChildren());
        for (org.zkoss.zk.ui.Component child : children) {
            createBindingsFor(child);
        }
        setBinderFor(result);
    }

    private static void setBinderFor(org.zkoss.zk.ui.Component result) {
        AnnotateDataBinder binder = new AnnotateDataBinder(result, true);
        result.setVariable("binder", binder, true);
        binder.loadAll();
    }

    private ITab createOrderOrdersTab() {
        return new CreatedOnDemandTab(ORDER_ORDERS_VIEW, ordersTabCreator) {
            @Override
            protected void afterShowAction() {
                breadcrumbs.getChildren().clear();
                breadcrumbs.appendChild(new Label(ORDER_ORDERS_VIEW));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                if (mode.isOf(ModeType.ORDER)) {
                    orderCRUDController.goToEditForm(mode.getOrder());
                    breadcrumbs
                            .appendChild(new Label(mode.getOrder().getName()));
                }

            }
        };
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        tabsSwitcher = (TabSwitcher) comp;
        breadcrumbs = comp.getPage().getFellow("breadcrumbs");
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

}
