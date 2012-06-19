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
package org.libreplan.web.planner.tabs;

import static org.libreplan.web.I18nHelper._;
import static org.libreplan.web.planner.tabs.MultipleTabsPlannerController.BREADCRUMBS_SEPARATOR;
import static org.libreplan.web.planner.tabs.MultipleTabsPlannerController.getSchedulingLabel;

import java.util.HashMap;
import java.util.Map;

import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.common.Util;
import org.libreplan.web.planner.order.IOrderPlanningGate;
import org.libreplan.web.planner.tabs.CreatedOnDemandTab.IComponentCreator;
import org.libreplan.web.resourceload.ResourceLoadController;
import org.libreplan.web.security.SecurityUtils;
import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class ResourcesLoadTabCreator {

    private final IOrderPlanningGate orderPlanningGate;

    public static ITab create(Mode mode,
            ResourceLoadController resourceLoadController,
            ResourceLoadController resourceLoadControllerGlobal,
            IOrderPlanningGate orderPlanningGate,
            Component breadcrumbs) {
        return new ResourcesLoadTabCreator(mode, resourceLoadController,
                resourceLoadControllerGlobal, orderPlanningGate,
                breadcrumbs)
                .build();
    }

    private final Mode mode;
    private final ResourceLoadController resourceLoadController;

    private final ResourceLoadController resourceLoadControllerGlobal;

    private final Component breadcrumbs;

    private ResourcesLoadTabCreator(Mode mode,
            ResourceLoadController resourceLoadController,
            ResourceLoadController resourceLoadControllerGlobal,
            IOrderPlanningGate orderPlanningGate,
            Component breadcrumbs) {
        this.mode = mode;
        this.resourceLoadController = resourceLoadController;
        this.resourceLoadControllerGlobal = resourceLoadControllerGlobal;
        this.orderPlanningGate = orderPlanningGate;
        this.breadcrumbs = breadcrumbs;
    }

    private ITab build() {
        return TabOnModeType.forMode(mode)
            .forType(ModeType.GLOBAL, createGlobalResourcesLoadTab())
            .forType(ModeType.ORDER, createOrderResourcesLoadTab())
            .forType(ModeType.BUDGET, createOrderResourcesLoadTab())
            .create();
    }

    private ITab createOrderResourcesLoadTab() {
        IComponentCreator componentCreator = new IComponentCreator() {

            @Override
            public org.zkoss.zk.ui.Component create(
                    org.zkoss.zk.ui.Component parent) {
                Map<String, Object> arguments = new HashMap<String, Object>();
                arguments.put("resourceLoadController", resourceLoadController);
                return Executions.createComponents(
                        "/resourceload/_resourceloadfororder.zul", parent,
                        arguments);
            }

        };
        return new CreatedOnDemandTab(_("Resources Load"), "order-load",
                componentCreator) {

            @Override
            protected void afterShowAction() {
                breadcrumbs.getChildren().clear();
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(getSchedulingLabel()));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(_("Resources Load")));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                Order currentOrder = mode.getOrder();
                resourceLoadController
                        .setPlanningControllerEntryPoints(orderPlanningGate);
                resourceLoadController.filterBy(currentOrder);
                resourceLoadController.reload();
                breadcrumbs.appendChild(new Label(currentOrder.getName()));
            }
        };
    }

    private ITab createGlobalResourcesLoadTab() {

        final IComponentCreator componentCreator = new IComponentCreator() {

            @Override
            public org.zkoss.zk.ui.Component create(
                    org.zkoss.zk.ui.Component parent) {
                Map<String, Object> arguments = new HashMap<String, Object>();
                arguments.put("resourceLoadController",
                        resourceLoadControllerGlobal);
                return Executions.createComponents(
                        "/resourceload/_resourceload.zul", parent, arguments);
            }

        };
        return new CreatedOnDemandTab(_("Resource Usage"), "company-load",
                componentCreator) {
            @Override
            protected void beforeShowAction() {
                if (!SecurityUtils
                        .isSuperuserOrUserInRoles(UserRole.ROLE_PLANNING)) {
                    Util.sendForbiddenStatusCodeInHttpServletResponse();
                }
            }

            @Override
            protected void afterShowAction() {
                resourceLoadControllerGlobal
                        .setPlanningControllerEntryPoints(orderPlanningGate);
                resourceLoadControllerGlobal.filterBy(null);
                resourceLoadControllerGlobal.reload();
                if (breadcrumbs.getChildren() != null) {
                    breadcrumbs.getChildren().clear();
                }
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(getSchedulingLabel()));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(_("Resource Usage")));
            }
        };
    }

}
