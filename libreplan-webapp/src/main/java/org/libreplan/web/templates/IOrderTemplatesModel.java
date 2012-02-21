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
package org.libreplan.web.templates;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.qualityforms.entities.QualityForm;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.web.tree.EntitiesTree;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public interface IOrderTemplatesModel {

    List<OrderElementTemplate> getRootTemplates();

    void createTemplateFrom(OrderElement orderElement);

    OrderElementTemplate getTemplate();

    void confirmSave();

    void initEdit(OrderElementTemplate template);

    EntitiesTree<OrderElementTemplate> getTemplatesTreeModel();

    boolean isTemplateTreeDisabled();

    void addLabelToConversation(Label label);

    List<Label> getLabels();

    Set<QualityForm> getAllQualityForms();

    OrderElementsOnConversation getOrderElementsOnConversation();

    void validateTemplateName(String name) throws IllegalArgumentException;

    void validateTemplateCode(String code) throws IllegalArgumentException;

    List<Criterion> getCriterionsFor(CriterionType criterionType);

    Map<CriterionType, List<Criterion>> getMapCriterions();

    Scenario getCurrentScenario();

    void confirmDelete(OrderElementTemplate template);

    boolean hasNotApplications(OrderElementTemplate template);
}
