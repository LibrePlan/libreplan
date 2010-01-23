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

import java.util.List;

import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.templates.entities.OrderElementTemplate;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.entrypoints.IURLHandlerRegistry;
import org.navalplanner.web.common.entrypoints.URLHandler;
import org.navalplanner.web.templates.advances.AdvancesAssignmentComponent;
import org.navalplanner.web.templates.labels.LabelsAssignmentToTemplateComponent;
import org.navalplanner.web.templates.materials.MaterialAssignmentTemplateComponent;
import org.navalplanner.web.templates.quality.QualityFormAssignerComponent;
import org.navalplanner.web.tree.TreeComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Window;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderTemplatesController extends GenericForwardComposer implements
        IOrderTemplatesControllerEntryPoints {

    @Autowired
    private IOrderTemplatesModel model;

    private OnlyOneVisible cachedOnlyOneVisible;

    private Window listWindow;

    private Window editWindow;

    @Autowired
    private IURLHandlerRegistry handlerRegistry;

    private EditTemplateWindowController editTemplateController;

    public List<OrderElementTemplate> getTemplates() {
        return model.getRootTemplates();
    }

    private OnlyOneVisible getVisibility() {
        if (cachedOnlyOneVisible == null) {
            cachedOnlyOneVisible = new OnlyOneVisible(listWindow, editWindow);
        }
        return cachedOnlyOneVisible;
    }

    public OrderElementTemplate getTemplate() {
        return model.getTemplate();
    }

    @Override
    public void goToCreateTemplateFrom(OrderElement orderElement) {
        model.createTemplateFrom(orderElement);
        showEditWindow();
    }

    public void goToEditForm(OrderElementTemplate template) {
        model.initEdit(template);
        showEditWindow();
    }

    private void showEditWindow() {
        bindAdvancesComponentWithCurrentTemplate();
        bindMaterialsControllerWithCurrentTemplate();
        bindLabelsControllerWithCurrentTemplate();
        bindQualityFormWithCurrentTemplate();
        bindEditTemplateWindowWithController();
        show(editWindow);
    }

    private void bindAdvancesComponentWithCurrentTemplate() {
        AdvancesAssignmentComponent c = (AdvancesAssignmentComponent) editWindow
                .getFellow("advancesAssignment");
        c.useModel(model);
    }

    private void bindMaterialsControllerWithCurrentTemplate() {
        MaterialAssignmentTemplateComponent materialsComponent = (MaterialAssignmentTemplateComponent) editWindow
                .getFellow("listOrderElementMaterials");
        materialsComponent.getController().openWindow(model.getTemplate());
    }

    private void bindLabelsControllerWithCurrentTemplate() {
        LabelsAssignmentToTemplateComponent labelsComponent = (LabelsAssignmentToTemplateComponent) editWindow
                .getFellow("listOrderElementLabels");
        labelsComponent.getController().openWindow(model);
    }

    private void bindQualityFormWithCurrentTemplate() {
        QualityFormAssignerComponent c = (QualityFormAssignerComponent) editWindow
                .getFellow("assignedQualityForms");
        c.useModel(model);
    }

    private void bindEditTemplateWindowWithController() {
        Window editTemplateWindow = (Window) editWindow
                .getFellow("editTemplateWindow");
        editTemplateController = EditTemplateWindowController.bindTo(model,
                editTemplateWindow);
    }

    public boolean isTemplateTreeDisabled() {
        return model.isTemplateTreeDisabled();
    }

    private void show(Component window) {
        Util.reloadBindings(window);
        getVisibility().showOnly(window);
    }

    public void saveAndExit() {
        model.confirmSave();
        show(listWindow);
    }

    public void cancel() {
        show(listWindow);
    }

    public void saveAndContinue() {
        model.confirmSave();
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        getVisibility().showOnly(listWindow);
        TreeComponent treeComponent = (TreeComponent) editWindow.getFellow("orderElementTree");
        treeComponent.useController(new TemplatesTreeController(model));
        final URLHandler<IOrderTemplatesControllerEntryPoints> handler = handlerRegistry
                .getRedirectorFor(IOrderTemplatesControllerEntryPoints.class);
        handler.registerListener(this, page);
    }

}
