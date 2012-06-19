/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2010-2012 Igalia, S.L.
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
import java.util.List;
import java.util.Map;

import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.Registry;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.resources.daos.IResourcesSearcher;
import org.libreplan.web.dashboard.DashboardController;
import org.libreplan.web.planner.order.OrderPlanningController;
import org.libreplan.web.planner.order.PlanningStateCreator;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.libreplan.web.planner.tabs.CreatedOnDemandTab.IComponentCreator;
import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Nacho Barrientos <nacho@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 *
 */
public class DashboardTabCreator {

    public static ITab create(Mode mode,
            PlanningStateCreator planningStateCreator,
            DashboardController dashboardController,
            OrderPlanningController orderPlanningController,
            Component breadcrumbs,
            IResourcesSearcher resourcesSearcher) {
        return new DashboardTabCreator(mode, planningStateCreator,
                dashboardController, orderPlanningController, breadcrumbs,
                resourcesSearcher).build();
    }

    private final PlanningStateCreator planningStateCreator;
    private final Mode mode;
    private final DashboardController dashboardController;
    private final OrderPlanningController orderPlanningController;
    private final Component breadcrumbs;
    private final IResourcesSearcher resourcesSearcher;

    private DashboardTabCreator(Mode mode,
            PlanningStateCreator planningStateCreator,
            DashboardController dashboardController,
            OrderPlanningController orderPlanningController,
            Component breadcrumbs,
            IResourcesSearcher resourcesSearcher) {
        this.mode = mode;
        this.planningStateCreator = planningStateCreator;
        this.dashboardController = dashboardController;
        this.orderPlanningController = orderPlanningController;
        this.breadcrumbs = breadcrumbs;
        this.resourcesSearcher = resourcesSearcher;
    }

    private ITab build() {
        return TabOnModeType.forMode(mode)
            .forType(ModeType.GLOBAL, createDashboardTab())
            .forType(ModeType.ORDER, createDashboardTab())
            .forType(ModeType.BUDGET, createDashboardTab())
            .create();
    }

    private ITab createDashboardTab() {
        IComponentCreator componentCreator = new IComponentCreator() {

            @Override
            public org.zkoss.zk.ui.Component create(
                    org.zkoss.zk.ui.Component parent) {
                Map<String, Object> arguments = new HashMap<String, Object>();
                arguments.put("dashboardController", dashboardController);
                return Executions.createComponents(
                        "/dashboard/_dashboardfororder.zul", parent,
                        arguments);
            }

        };
        return new CreatedOnDemandTab(_("Dashboard"), "order-dashboard",
                componentCreator) {

            @Override
            protected void afterShowAction() {
                List<TaskElement> criticalPath = orderPlanningController.getCriticalPath();
                if (criticalPath == null) {
                    criticalPath = getCriticalPath(mode.getOrder(),
                            getDesktop());
                }
                PlanningState planningState = getPlanningState(mode.getOrder(), getDesktop());
                Order currentOrder = planningState.getOrder();
                dashboardController.setCurrentOrder(planningState,
                        criticalPath);
                breadcrumbs.getChildren().clear();
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(getSchedulingLabel()));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(_("Dashboard")));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(currentOrder.getName()));
            }
        };
    }

    private List<TaskElement> getCriticalPath(final Order order, final Desktop desktop) {
        CriticalPathBuilder builder = CriticalPathBuilder.create(
                planningStateCreator, resourcesSearcher);
        return builder.getCriticalPath(order, desktop);
    }

    PlanningState getPlanningState(final Order order, final Desktop desktop) {
        IAdHocTransactionService transactionService = Registry
                .getTransactionService();
        return transactionService
                .runOnTransaction(new IOnTransaction<PlanningState>() {
                    @Override
                    public PlanningState execute() {
                        return planningStateCreator.retrieveOrCreate(desktop,
                                order);
                    }
                });
    }
}
