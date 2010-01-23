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

import org.navalplanner.business.templates.entities.OrderElementTemplate;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Window;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class EditTemplateWindowController extends GenericForwardComposer {

    private static final String ATTRIBUTE_NAME = EditTemplateWindowController.class
            .getSimpleName();

    public static EditTemplateWindowController bindTo(
            IOrderTemplatesModel model, Window editTemplateWindow) {
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

    private final IOrderTemplatesModel model;
    private final Window editTemplateWindow;

    public EditTemplateWindowController(Window editTemplateWindow,
            IOrderTemplatesModel model) {
        this.editTemplateWindow = editTemplateWindow;
        this.model = model;
    }

    public void open(OrderElementTemplate template) {
        try {
            editTemplateWindow.setMode("modal");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void onClick$backButton() {
        editTemplateWindow.setVisible(false);
    }


}
