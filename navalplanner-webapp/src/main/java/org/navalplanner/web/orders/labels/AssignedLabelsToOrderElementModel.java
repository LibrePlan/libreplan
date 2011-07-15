/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.orders.labels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.web.orders.IOrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AssignedLabelsToOrderElementModel extends
        AssignedLabelsModel<OrderElement> implements
        IAssignedLabelsToOrderElementModel {

    @Autowired
    private IOrderElementDAO orderDAO;

    private IOrderModel orderModel;

    @Override
    protected OrderElement getParent(OrderElement element) {
        return element.getParent();
    }

    @Override
    protected List<OrderElement> getChildren(OrderElement element) {
        return element.getChildren();
    }

    @Override
    protected List<Label> getLabels(OrderElement orderElement) {
        return new ArrayList<Label>(orderElement.getLabels());
    }

    @Override
    public void setOrderModel(IOrderModel orderModel) {
        this.orderModel = orderModel;
    }

    @Override
    protected void addLabelToConversation(Label label) {
        orderModel.addLabel(label);
    }

    @Override
    protected void addLabelToElement(OrderElement element, Label label) {
        element.addLabel(label);
    }

    @Override
    protected List<Label> getLabelsOnConversation() {
        if (orderModel == null) {
            return Collections.emptyList();
        }
        return orderModel.getLabels();
    }

    @Override
    protected void reattach(OrderElement element) {
        orderDAO.reattach(element);
        element.getName();
    }

    @Override
    protected void removeLabel(OrderElement element, Label label) {
        element.removeLabel(label);
    }

}
