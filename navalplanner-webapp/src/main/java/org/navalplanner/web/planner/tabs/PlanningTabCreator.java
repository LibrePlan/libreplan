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
import static org.navalplanner.web.planner.tabs.MultipleTabsPlannerController.BREADCRUMBS_SEPARATOR;
import static org.navalplanner.web.planner.tabs.MultipleTabsPlannerController.PLANNIFICATION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.planner.company.CompanyPlanningController;
import org.navalplanner.web.planner.order.OrderPlanningController;
import org.navalplanner.web.planner.tabs.CreatedOnDemandTab.IComponentCreator;
import org.zkoss.ganttz.extensions.ICommandOnTask;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class PlanningTabCreator {

    public static final String ENTERPRISE_VIEW = _("Company Scheduling");

    public static final String ORDER_ENTERPRISE_VIEW = _("Order Scheduling");

    private final Mode mode;
    private final CompanyPlanningController companyPlanningController;
    private final Component breadcrumbs;

    private final OrderPlanningController orderPlanningController;

    private final IOrderDAO orderDAO;

    public static ITab create(Mode mode,
            CompanyPlanningController companyPlanningController,
            OrderPlanningController orderPlanningController,
            IOrderDAO orderDAO,
            Component breadcrumbs) {
        return new PlanningTabCreator(mode, companyPlanningController,
                orderPlanningController, breadcrumbs, orderDAO).create();
    }

    private PlanningTabCreator(Mode mode,
            CompanyPlanningController companyPlanningController,
            OrderPlanningController orderPlanningController,
            Component breadcrumbs, IOrderDAO orderDAO) {
        this.mode = mode;
        this.companyPlanningController = companyPlanningController;
        this.orderPlanningController = orderPlanningController;
        this.breadcrumbs = breadcrumbs;
        this.orderDAO = orderDAO;
    }

    private ITab create() {
        return TabOnModeType.forMode(mode)
                    .forType(ModeType.GLOBAL, createGlobalPlanningTab())
                    .forType(ModeType.ORDER, createOrderPlanningTab())
                    .create();
    }

    private ITab createGlobalPlanningTab() {
        final IComponentCreator componentCreator = new IComponentCreator() {

            @Override
            public org.zkoss.zk.ui.Component create(
                    org.zkoss.zk.ui.Component parent) {
                List<ICommandOnTask<TaskElement>> commands = new ArrayList<ICommandOnTask<TaskElement>>();
                ICommandOnTask<TaskElement> scheduleCommand = new ICommandOnTask<TaskElement>() {

                    @Override
                    public void doAction(
                            IContextWithPlannerTask<TaskElement> context,
                            TaskElement task) {
                        OrderElement orderElement = task.getOrderElement();
                        if (orderElement instanceof Order) {
                            Order order = (Order) orderElement;
                            mode.goToOrderMode(order);
                        }
                    }

                    @Override
                    public String getName() {
                        return _("Schedule");
                    }
                };
                commands.add(scheduleCommand);
                companyPlanningController.setAdditional(commands);
                companyPlanningController
                        .setDoubleClickCommand(scheduleCommand);
                HashMap<String, Object> args = new HashMap<String, Object>();
                args
                        .put("companyPlanningController",
                                companyPlanningController);
                return Executions.createComponents("/planner/_company.zul",
                        parent, args);
            }

        };
        return new CreatedOnDemandTab(ENTERPRISE_VIEW, componentCreator) {
            @Override
            protected void afterShowAction() {
                companyPlanningController.setConfigurationForPlanner();
                breadcrumbs.getChildren().clear();
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(PLANNIFICATION));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(ENTERPRISE_VIEW));
            }
        };
    }

    private ITab createOrderPlanningTab() {

        final IComponentCreator componentCreator = new IComponentCreator() {

            @Override
            public org.zkoss.zk.ui.Component create(
                    org.zkoss.zk.ui.Component parent) {
                Map<String, Object> arguments = new HashMap<String, Object>();
                orderPlanningController.setOrder(mode.getOrder());
                arguments.put("orderPlanningController",
                        orderPlanningController);
                org.zkoss.zk.ui.Component result = Executions.createComponents(
                        "/planner/order.zul", parent, arguments);
                Util.createBindingsFor(result);
                return result;
            }

        };
        return new CreatedOnDemandTab(ORDER_ENTERPRISE_VIEW, componentCreator) {
            @Override
            protected void afterShowAction() {

                orderPlanningController.setOrder(reload(mode.getOrder()));
                Map<String, Object> arguments = new HashMap<String, Object>();
                arguments.put("orderPlanningController",
                        orderPlanningController);

                if (breadcrumbs.getChildren() != null) {
                    breadcrumbs.getChildren().clear();
                }
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(PLANNIFICATION));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(ORDER_ENTERPRISE_VIEW));
                if (mode.isOf(ModeType.ORDER)) {
                    breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                    breadcrumbs
                            .appendChild(new Label(mode.getOrder().getName()));
                }

            }
        };
    }

    protected Order reload(Order order) {
        try {
            return orderDAO.find(order.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
