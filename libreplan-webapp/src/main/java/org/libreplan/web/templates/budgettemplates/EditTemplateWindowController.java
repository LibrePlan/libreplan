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
package org.libreplan.web.templates.budgettemplates;

import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.web.common.Util;
import org.libreplan.web.templates.advances.AdvancesAssignmentComponent;
import org.libreplan.web.templates.criterionrequirements.CriterionRequirementTemplateComponent;
import org.libreplan.web.templates.labels.LabelsAssignmentToTemplateComponent;
import org.libreplan.web.templates.materials.MaterialAssignmentTemplateComponent;
import org.libreplan.web.templates.quality.QualityFormAssignerComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Window;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class EditTemplateWindowController extends GenericForwardComposer {

    private static final String ATTRIBUTE_NAME = EditTemplateWindowController.class
            .getSimpleName();

    public static EditTemplateWindowController bindTo(
            IBudgetTemplatesModel model, Window editTemplateWindow) {
        ensureWindowIsClosed(editTemplateWindow);
        if (editTemplateWindow.getAttribute(ATTRIBUTE_NAME) != null) {
            return (EditTemplateWindowController) editTemplateWindow
                    .getAttribute(ATTRIBUTE_NAME);
        }
        EditTemplateWindowController controller = new EditTemplateWindowController(
                editTemplateWindow,
                model);
        editTemplateWindow.setAttribute(ATTRIBUTE_NAME, controller);
        doAfterCompose(editTemplateWindow, controller);
        return controller;
    }

    private static void ensureWindowIsClosed(Window editTemplateWindow) {
        editTemplateWindow.setVisible(true);
        editTemplateWindow.setVisible(false);
    }

    private static void doAfterCompose(Window editTemplateWindow,
            EditTemplateWindowController controller) {
        try {
            controller.doAfterCompose(editTemplateWindow);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final IBudgetTemplatesModel model;
    private final Window editTemplateWindow;

    public EditTemplateWindowController(Window editTemplateWindow,
            IBudgetTemplatesModel model) {
        this.editTemplateWindow = editTemplateWindow;
        this.model = model;
    }

    public void open(OrderElementTemplate template) {
        try {
            editTemplateWindow.setMode("modal");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        bindOrderElementLabels(template);
        bindAssignedQualityForms(template);
        Util.reloadBindings(editTemplateWindow);
    }

    private <T extends Component> T find(String id, Class<T> type) {
        return type.cast(editTemplateWindow.getFellow(id));
    }

    private void bindOrderElementLabels(OrderElementTemplate template) {
        LabelsAssignmentToTemplateComponent component = find(
                "listOrderElementLabels",
                LabelsAssignmentToTemplateComponent.class);
        component.getController().setTemplate(template);
        component.getController().openWindow(model);
    }

    private void bindAssignedQualityForms(OrderElementTemplate template) {
        QualityFormAssignerComponent c = find("assignedQualityForms",
                QualityFormAssignerComponent.class);
        c.useModel(model, template);
    }

    public void onClick$backButton() {
        editTemplateWindow.setVisible(false);
    }


}
