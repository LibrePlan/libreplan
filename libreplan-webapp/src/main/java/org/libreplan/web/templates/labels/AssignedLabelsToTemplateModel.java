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
package org.libreplan.web.templates.labels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.templates.daos.IOrderElementTemplateDAO;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.web.orders.labels.AssignedLabelsModel;
import org.libreplan.web.templates.IOrderTemplatesModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AssignedLabelsToTemplateModel extends
        AssignedLabelsModel<OrderElementTemplate> implements
        IAssignedLabelsToTemplateModel {

    @Autowired
    private IOrderElementTemplateDAO templateDAO;

    private IOrderTemplatesModel templatesModel;

    @Override
    protected void addLabelToConversation(Label label) {
        templatesModel.addLabelToConversation(label);
    }

    @Override
    protected void addLabelToElement(OrderElementTemplate element, Label label) {
        element.addLabel(label);
    }

    @Override
    protected List<OrderElementTemplate> getChildren(
            OrderElementTemplate element) {
        return element.getChildren();
    }

    @Override
    protected List<Label> getLabels(OrderElementTemplate orderElement) {
        return new ArrayList<Label>(orderElement.getLabels());
    }

    @Override
    protected List<Label> getLabelsOnConversation() {
        if (templatesModel == null) {
            return Collections.emptyList();
        }
        return templatesModel.getLabels();
    }

    @Override
    protected OrderElementTemplate getParent(OrderElementTemplate element) {
        return element.getParent();
    }

    @Override
    protected void reattach(OrderElementTemplate element) {
        templateDAO.reattach(element);
    }

    @Override
    protected void removeLabel(OrderElementTemplate element, Label label) {
        element.removeLabel(label);
    }

    @Override
    public void setTemplatesModel(IOrderTemplatesModel templatesModel) {
        this.templatesModel = templatesModel;
    }

}
