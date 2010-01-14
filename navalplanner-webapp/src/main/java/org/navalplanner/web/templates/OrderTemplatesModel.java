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
package org.navalplanner.web.templates;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.labels.daos.ILabelDAO;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.qualityforms.daos.IQualityFormDAO;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.templates.daos.IOrderElementTemplateDAO;
import org.navalplanner.business.templates.entities.OrderElementTemplate;
import org.navalplanner.web.orders.QualityFormsOnConversation;
import org.navalplanner.web.orders.labels.LabelsOnConversation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderTemplatesModel implements IOrderTemplatesModel {

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IOrderElementTemplateDAO dao;

    @Autowired
    private ILabelDAO labelDAO;

    @Autowired
    private IQualityFormDAO qualityFormDAO;

    @Autowired
    private IAdHocTransactionService transaction;

    private OrderElementTemplate template;

    private TemplatesTree treeModel;

    private LabelsOnConversation labelsOnConversation;

    private LabelsOnConversation getLabelsOnConversation() {
        if (labelsOnConversation == null) {
            labelsOnConversation = new LabelsOnConversation(labelDAO);
        }
        return labelsOnConversation;
    }

    private QualityFormsOnConversation qualityFormsOnConversation;

    private QualityFormsOnConversation getQualityFormsOnConversation() {
        if (qualityFormsOnConversation == null) {
            qualityFormsOnConversation = new QualityFormsOnConversation(
                    qualityFormDAO);
        }
        return qualityFormsOnConversation;
    }

    @Override
    public List<OrderElementTemplate> getRootTemplates() {
        return dao.getRootTemplates();
    }

    @Override
    public OrderElementTemplate getTemplate() {
        return template;
    }

    @Override
    public void confirmSave() {
        transaction.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                dao.save(template);
                return null;
            }
        });
        dontPoseAsTransient(template);
    }

    private void dontPoseAsTransient(OrderElementTemplate template) {
        template.dontPoseAsTransientObjectAnymore();
        List<OrderElementTemplate> childrenTemplates = template
                .getChildrenTemplates();
        for (OrderElementTemplate each : childrenTemplates) {
            dontPoseAsTransient(each);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void createTemplateFrom(OrderElement orderElement) {
        initializeAcompanyingObjectsOnConversation();
        orderElementDAO.loadOrderAvoidingProxyFor(orderElement);
        OrderElement reloaded = orderElementDAO
                .findExistingEntity(orderElement.getId());
        template = reloaded.createTemplate();
        treeModel = new TemplatesTree(template);
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(OrderElementTemplate template) {
        initializeAcompanyingObjectsOnConversation();
        this.template = dao.findExistingEntity(template.getId());
        treeModel = new TemplatesTree(this.template);
    }

    private void initializeAcompanyingObjectsOnConversation() {
        getLabelsOnConversation().initializeLabels();
        getQualityFormsOnConversation().initialize();
    }

    @Override
    public TemplatesTree getTemplatesTreeModel() {
        return treeModel;
    }

    @Override
    public boolean isTemplateTreeDisabled() {
        return template != null && template.isLeaf();
    }

    @Override
    public void addLabelToConversation(Label label) {
        getLabelsOnConversation().addLabel(label);
    }

    @Override
    public List<Label> getLabels() {
        return getLabelsOnConversation().getLabels();
    }

    @Override
    public Set<QualityForm> getAllQualityForms() {
        return new HashSet<QualityForm>(getQualityFormsOnConversation()
                .getQualityForms());
    }

}
