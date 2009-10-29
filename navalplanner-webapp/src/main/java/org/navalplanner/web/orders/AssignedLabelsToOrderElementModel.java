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

package org.navalplanner.web.orders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.labels.daos.ILabelDAO;
import org.navalplanner.business.labels.daos.ILabelTypeDAO;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AssignedLabelsToOrderElementModel implements
        IAssignedLabelsToOrderElementModel {

    @Autowired
    IOrderElementDAO orderDAO;

    @Autowired
    ILabelTypeDAO labelTypeDAO;

    @Autowired
    ILabelDAO labelDAO;

    OrderElement orderElement;

    IOrderModel orderModel;

    @Override
    public OrderElement getOrderElement() {
        return orderElement;
    }

    @Override
    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    @Override
    @Transactional(readOnly = true)
    public void init(OrderElement orderElement) {
        this.orderElement = orderElement;
        initializeOrderElement(this.orderElement);
    }

    private void initializeOrderElement(OrderElement orderElement) {
        reattachLabels();
        orderDAO.save(orderElement);
        orderElement.getName();
        if (orderElement.getParent() != null) {
            orderElement.getParent().getName();
        }
        initializeLabels(orderElement.getLabels());
    }

    private void reattachLabels() {
        for (Label label : orderModel.getLabels()) {
            labelDAO.save(label);
        }
    }

    private void initializeLabels(Collection<Label> labels) {
        for (Label label : labels) {
            initializeLabel(label);
        }
    }

    public void initializeLabel(Label label) {
        label.getName();
        label.getType().getName();
    }

    @Transactional(readOnly = true)
    public List<Label> getLabels() {
        List<Label> result = new ArrayList<Label>();
        if (orderElement != null && orderElement.getLabels() != null) {
            reattachLabels();
            result.addAll(orderElement.getLabels());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Label> getInheritedLabels() {
        List<Label> result = new ArrayList<Label>();
        if (orderElement != null) {
            reattachLabels();
            OrderLineGroup parent = orderElement.getParent();
            while (parent != null) {
                initializeOrderElement(parent);
                result.addAll(parent.getLabels());
                parent = parent.getParent();
            }
        }
        return result;
    }

    @Override
    public Label createLabel(String labelName, LabelType labelType) {
        Label label = Label.create(labelName);
        label.setType(labelType);
        orderModel.addLabel(label);
        return label;
    }

    @Override
    @Transactional(readOnly = true)
    public void assignLabel(Label label) {
        reattachLabels();
        orderElement.addLabel(label);
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteLabel(Label label) {
        reattachLabels();
        orderElement.removeLabel(label);
    }

    @Override
    @Transactional(readOnly = true)
    public Label findLabelByNameAndType(String labelName, LabelType labelType) {
        Label label = findLabelByNameAndTypeName(labelName, labelType.getName());
        if (label != null) {
            initializeLabel(label);
        }
        return label;
    }

    /**
     * Search {@link Label} by name and type in cache of labels
     *
     * @param labelName
     * @param labelTypeName
     * @return
     */
    private Label findLabelByNameAndTypeName(String labelName,
            String labelTypeName) {
        for (Label label : orderModel.getLabels()) {
            if (label.getName().equals(labelName)
                    && label.getType().getName().equals(labelTypeName)) {
                return label;
            }
        }
        return null;
    }

    @Override
    public boolean isAssigned(Label label) {
        final Set<Label> labels = orderElement.getLabels();

        for (Label element : labels) {
            if (element.getId().equals(label.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setOrderModel(IOrderModel orderModel) {
        this.orderModel = orderModel;
    }

    @Override
    public List<Label> getAllLabels() {
        return (orderModel != null) ? orderModel.getLabels() : new ArrayList<Label>();
    }

}
