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

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.planner.CompanyPlanningController;
import org.navalplanner.web.planner.tabs.CreatedOnDemandTab.IComponentCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.adapters.TabsConfiguration;
import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

/**
 * Creates and handles several tabs
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MultipleTabsPlannerController {

    private static final String ENTERPRISE_VIEW = _("Enterprise");

    private static final String RESOURCE_LOAD_VIEW = _("Resource Load");

    private static final String ORDERS_VIEW = _("Orders");

    private TabsConfiguration tabsConfiguration;

    private Mode mode = Mode.initial();

    @Autowired
    private CompanyPlanningController companyPlanningController;

    public TabsConfiguration getTabs() {
        if (tabsConfiguration == null) {
            tabsConfiguration = buildTabsConfiguration();
        }
        return tabsConfiguration;
    }

    private TabsConfiguration buildTabsConfiguration() {
        return TabsConfiguration.create().add(createEnterpriseTab()).add(
                createResourcesLoadTab()).add(createOrdersTab());
    }

    private ITab createEnterpriseTab() {
        return TabOnModeType.forMode(mode)
                  .forType(ModeType.GLOBAL,createGlobalEnterpriseTab())
                  .forType(ModeType.ORDER, createOrderEnterpriseTab())
                  .create();
    }

    private org.zkoss.zk.ui.Component withUpAndDownButton(
            org.zkoss.zk.ui.Component component) {
        Div result = new Div();
        result.appendChild(component);
        Button up = new Button();
        up.setLabel("up");
        up.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                mode.up();
            }
        });
        Button down = new Button();
        down.setLabel("down");
        down.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                mode.goToOrderMode(new Order());
            }
        });
        result.appendChild(up);
        result.appendChild(down);
        return result;
    }

    private ITab createGlobalEnterpriseTab() {
        return new CreatedOnDemandTab(ENTERPRISE_VIEW, new IComponentCreator() {

            @Override
            public org.zkoss.zk.ui.Component create(
                    org.zkoss.zk.ui.Component parent) {
                HashMap<String, Object> args = new HashMap<String, Object>();
                args.put("companyPlanningController",
                        companyPlanningController);
                return Executions.createComponents("/planner/_company.zul",
                        parent,
                        args);
            }
        });
    }

    private ITab createOrderEnterpriseTab() {
        return new CreatedOnDemandTab(ENTERPRISE_VIEW, new IComponentCreator() {

            @Override
            public org.zkoss.zk.ui.Component create(
                    org.zkoss.zk.ui.Component parent) {
                return withUpAndDownButton(new Label(
                        "on enterprise view. mode: "
                        + mode.getType()));
            }
        });
    }

    private ITab createResourcesLoadTab() {
        return TabOnModeType.forMode(mode)
                .forType(ModeType.GLOBAL, createGlobalResourcesLoadTab())
                .forType(ModeType.ORDER, createOrderResourcesLoadTab())
                .create();
    }

    private ITab createOrderResourcesLoadTab() {
        return new CreatedOnDemandTab(RESOURCE_LOAD_VIEW, new IComponentCreator() {

            @Override
            public org.zkoss.zk.ui.Component create(
                    org.zkoss.zk.ui.Component parent) {
                        return withUpAndDownButton(new Label(
                                "on resource load view. mode: "
                                        + mode.getType()));
            }
        });
    }

    private ITab createGlobalResourcesLoadTab() {
        return new CreatedOnDemandTab(RESOURCE_LOAD_VIEW,
                new IComponentCreator() {

                    @Override
                    public org.zkoss.zk.ui.Component create(
                            org.zkoss.zk.ui.Component parent) {
                        return Executions.createComponents(
                                        "/resourceload/_resourceload.zul",
                                parent, null);
                    }
                });
    }

    private ITab createOrdersTab() {
        return TabOnModeType.forMode(mode)
                .forType(ModeType.GLOBAL, createGlobalOrdersTab())
                .forType(ModeType.ORDER, createOrderOrdersTab())
                .create();
    }

    private ITab createGlobalOrdersTab() {
        return new CreatedOnDemandTab(ORDERS_VIEW,
                new IComponentCreator() {

                    @Override
                    public org.zkoss.zk.ui.Component create(
                            org.zkoss.zk.ui.Component parent) {
                org.zkoss.zk.ui.Component result = Executions.createComponents(
                        "/orders/_ordersTab.zul",
                                        parent,
                                        null);
                createBindingsFor(result);
                Util.reloadBindings(result);
                return result;
            }

        });
    }

    @SuppressWarnings("unchecked")
    private void createBindingsFor(org.zkoss.zk.ui.Component result) {
        List<org.zkoss.zk.ui.Component> children = new ArrayList<org.zkoss.zk.ui.Component>(
                result.getChildren());
        for (org.zkoss.zk.ui.Component child : children) {
            createBindingsFor(child);
        }
        setBinderFor(result);
    }

    private void setBinderFor(org.zkoss.zk.ui.Component result) {
        AnnotateDataBinder binder = new AnnotateDataBinder(result, true);
        result.setVariable("binder", binder, true);
        binder.loadAll();
    }

    private ITab createOrderOrdersTab() {
        return new CreatedOnDemandTab(ORDERS_VIEW, new IComponentCreator() {

            @Override
            public org.zkoss.zk.ui.Component create(
                    org.zkoss.zk.ui.Component parent) {
                return withUpAndDownButton(new Label("on order view. mode: "
                        + mode.getType()));
            }
        });
    }

}
