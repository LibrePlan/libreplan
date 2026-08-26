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
package org.libreplan.web.templates.quality;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.libreplan.business.qualityforms.entities.QualityForm;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.libreplan.web.orders.AssignedTaskQualityFormsToOrderElementController;
import org.libreplan.web.orders.AssignedTaskQualityFormsToOrderElementController.ICheckQualityFormAssigned;
import org.libreplan.web.templates.IOrderTemplatesModel;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

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

    public ListModel<QualityForm> getAssigned() {
        if (template == null) {
            return new ListModelList<>(Collections.emptyList());
        }
        return new ListModelList<>(template.getQualityForms());
    }

    /**
     * The rows used to be declared with a ZUML "each" template (self="@{each=...}") - under this
     * app's AnnotateBinder/ZK 10 stack that only ever clones the row's FIRST child for each
     * iteration. Building rows programmatically via RowRenderer sidesteps that "each" bug entirely
     * (same fix pattern as elsewhere in this sweep, e.g. WorkerCRUDController.getWorkersRenderer()).
     */
    public RowRenderer getAssignedRenderer() {
        return (Row row, Object data, int i) -> {
            final QualityForm qualityForm = (QualityForm) data;
            row.setValue(qualityForm);

            row.appendChild(new Label());
            row.appendChild(new Label(qualityForm.getName()));
            row.appendChild(new Label(String.valueOf(qualityForm.getQualityFormType())));

            Hbox hbox = new Hbox();
            Button delete = new Button();
            delete.setSclass("icono");
            delete.setImage("/common/img/ico_borrar1.png");
            delete.setHoverImage("/common/img/ico_borrar.png");
            delete.addEventListener(Events.ON_CLICK, event -> remove(qualityForm));
            hbox.appendChild(delete);
            row.appendChild(hbox);
        };
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
