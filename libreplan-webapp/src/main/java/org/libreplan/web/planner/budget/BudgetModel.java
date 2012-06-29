/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.web.planner.budget;

import static org.libreplan.web.I18nHelper._;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderStatusEnum;
import org.libreplan.business.templates.entities.Budget;
import org.libreplan.web.planner.order.PlanningStateCreator;
import org.libreplan.web.planner.order.PlanningStateCreator.IActionsOnRetrieval;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.libreplan.web.templates.budgettemplates.BudgetTemplatesModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.Desktop;

/**
 * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BudgetModel extends BudgetTemplatesModel implements IBudgetModel {

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private PlanningStateCreator planningStateCreator;

    private PlanningState planningState;

    @Override
    @Transactional
    public void saveThroughPlanningState(Desktop desktop,
            boolean showSaveMessage) {
        if (!getAssociatedOrder().getState().equals(OrderStatusEnum.BUDGET)) {
            throw new ValidationException(
                    _("The project budget cannot be modified once it has been closed"));
        }
        this.planningState = planningStateCreator.retrieveOrCreate(desktop,
                getAssociatedOrder(), new IActionsOnRetrieval() {

                    @Override
                    public void onRetrieval(PlanningState planningState) {
                        planningState.reattach();
                    }
                });
        if (showSaveMessage) {
            this.planningState.getSaveCommand().save(null);
        } else {
            this.planningState.getSaveCommand().save(null, null);
        }
    }

    @Override
    @Transactional
    public void closeBudget() {
        Budget budget = (Budget) getTemplate();
        Order order = budget.getAssociatedOrder();
        if (!order.getState().equals(OrderStatusEnum.BUDGET)) {
            throw new ValidationException(_("The budget is already closed"));
        }
        orderDAO.reattach(order);
        order.setState(OrderStatusEnum.OFFERED);
        budget.createOrderLineElementsForAssociatedOrder();
    }

    @Override
    public Order getAssociatedOrder() {
        return ((Budget) getTemplate()).getAssociatedOrder();
    }

    @Override
    public boolean isReadOnly() {
        return !getAssociatedOrder().getState().equals(OrderStatusEnum.BUDGET);
    }
}
