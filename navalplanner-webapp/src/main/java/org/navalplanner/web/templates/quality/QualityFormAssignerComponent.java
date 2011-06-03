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
package org.navalplanner.web.templates.quality;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.templates.entities.OrderElementTemplate;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.navalplanner.web.orders.AssignedTaskQualityFormsToOrderElementController;
import org.navalplanner.web.orders.AssignedTaskQualityFormsToOrderElementController.ICheckQualityFormAssigned;
import org.navalplanner.web.templates.IOrderTemplatesModel;
import org.zkoss.zk.ui.HtmlMacroComponent;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class QualityFormAssignerComponent extends HtmlMacroComponent {

    private OrderElementTemplate template;
    private IOrderTemplatesModel model;

    public void useModel(IOrderTemplatesModel model) {
        useModel(model, model.getTemplate());
    }

    public void useModel(IOrderTemplatesModel model,
            OrderElementTemplate template) {
        this.model = model;
        this.template = template;
    }

    public List<QualityForm> getNotAssignedQualityForms() {
        if (model == null) {
            return Collections.emptyList();
        }
        Set<QualityForm> result = model.getAllQualityForms();
        result.removeAll(template.getQualityForms());
        return new ArrayList<QualityForm>(result);
    }

    public List<QualityForm> getAssigned() {
        if (template == null) {
            return Collections.emptyList();
        }
        return new ArrayList<QualityForm>(template.getQualityForms());
    }

    public void onAssignTaskQualityForm() {
        ICheckQualityFormAssigned checkQualityFormAssigned = new ICheckQualityFormAssigned() {

            @Override
            public boolean isAssigned(QualityForm qualityForm) {
                return template.getQualityForms().contains(qualityForm);
            }
        };
        QualityForm qualityForm = AssignedTaskQualityFormsToOrderElementController
                .retrieveQualityFormFrom(getQualityFormFinder(),
                        checkQualityFormAssigned);
        template.addQualityForm(qualityForm);
        Util.reloadBindings(this);
    }

    public void remove(QualityForm qualityForm) {
        template.removeQualityForm(qualityForm);
        Util.reloadBindings(this);
    }

    private BandboxSearch getQualityFormFinder() {
        return (BandboxSearch) getFellow("qualityFormFinder");
    }

}
