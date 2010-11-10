/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

import static org.navalplanner.web.I18nHelper._;
import static org.navalplanner.web.planner.tabs.MultipleTabsPlannerController.BREADCRUMBS_SEPARATOR;

import java.util.List;

import javax.annotation.Resource;

import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.templates.entities.OrderElementTemplate;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.entrypoints.IURLHandlerRegistry;
import org.navalplanner.web.common.entrypoints.URLHandler;
import org.navalplanner.web.planner.tabs.IGlobalViewEntryPoints;
import org.navalplanner.web.templates.advances.AdvancesAssignmentComponent;
import org.navalplanner.web.templates.criterionrequirements.CriterionRequirementTemplateComponent;
import org.navalplanner.web.templates.historicalAssignment.OrderElementHistoricalAssignmentComponent;
import org.navalplanner.web.templates.historicalStatistics.OrderElementHistoricalStatisticsComponent;
import org.navalplanner.web.templates.labels.LabelsAssignmentToTemplateComponent;
import org.navalplanner.web.templates.materials.MaterialAssignmentTemplateComponent;
import org.navalplanner.web.templates.quality.QualityFormAssignerComponent;
import org.navalplanner.web.tree.TreeComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
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

    private Textbox name;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private TreeComponent treeComponent;

    @Resource
    private IGlobalViewEntryPoints globalView;

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
        bindCriterionRequirementControllerWithCurrentTemplate();
        bindLabelsControllerWithCurrentTemplate();
        bindQualityFormWithCurrentTemplate();
        bindEditTemplateWindowWithController();
        bindHistoricalArragenmentWithCurrentTemplate();
        bindHistoricalStatisticsWithCurrentTemplate();
        show(editWindow);
    }

    private <T extends Component> T findAtEditWindow(String id, Class<T> type) {
        return type.cast(editWindow.getFellow(id));
    }

    private void bindAdvancesComponentWithCurrentTemplate() {
        AdvancesAssignmentComponent c = findAtEditWindow("advancesAssignment",
                AdvancesAssignmentComponent.class);
        c.useModel(model);
    }

    private void bindMaterialsControllerWithCurrentTemplate() {
        MaterialAssignmentTemplateComponent c = findAtEditWindow(
                "listOrderElementMaterials",
                MaterialAssignmentTemplateComponent.class);
        c.getController().openWindow(model.getTemplate());
    }

    private void bindCriterionRequirementControllerWithCurrentTemplate() {
        CriterionRequirementTemplateComponent c = findAtEditWindow(
                "listOrderElementCriterionRequirements",
                CriterionRequirementTemplateComponent.class);
        c.getController().openWindow(model);
    }

    private void bindLabelsControllerWithCurrentTemplate() {
        LabelsAssignmentToTemplateComponent c = findAtEditWindow(
                "listOrderElementLabels",
                LabelsAssignmentToTemplateComponent.class);
        c.getController().openWindow(model);
    }

    private void bindQualityFormWithCurrentTemplate() {
        QualityFormAssignerComponent c = findAtEditWindow(
                "assignedQualityForms",
                QualityFormAssignerComponent.class);
        c.useModel(model);
    }

    private void bindEditTemplateWindowWithController() {
        Window editTemplateWindow = (Window) editWindow
                .getFellow("editTemplateWindow");
        editTemplateController = EditTemplateWindowController.bindTo(model,
                editTemplateWindow);
    }

    private void bindHistoricalArragenmentWithCurrentTemplate() {
        OrderElementHistoricalAssignmentComponent c = (OrderElementHistoricalAssignmentComponent) editWindow
                .getFellow("historicalAssignment");
        c.useModel(model, globalView);
    }

    private void bindHistoricalStatisticsWithCurrentTemplate() {
        OrderElementHistoricalStatisticsComponent c = (OrderElementHistoricalStatisticsComponent) editWindow
                .getFellow("historicalStatistics");
        c.useModel(model);
    }

    public boolean isTemplateTreeDisabled() {
        return model.isTemplateTreeDisabled();
    }

    private void show(Component window) {
        Util.reloadBindings(window);
        getVisibility().showOnly(window);
    }

    public void showEditionFor(OrderElementTemplate template) {
        editTemplateController.open(template);
    }

    public void saveAndExit() {
        if (isAllValid()) {
            model.confirmSave();
            messagesForUser.showMessage(Level.INFO, _("Template saved"));
            show(listWindow);
        }
    }

    public void cancel() {
        show(listWindow);
    }

    public void saveAndContinue() {
        if (isAllValid()) {
            model.confirmSave();
            model.initEdit(getTemplate());
            messagesForUser.showMessage(Level.INFO, _("Template saved"));
        }
    }

    private boolean isAllValid() {
        // validate template name
        name = (Textbox) editWindow.getFellowIfAny("name");
        if ((name != null) && (!name.isValid())) {
            selectTab("tabGeneralData");
            showInvalidWorkReportTypeName();
            return false;
        }
        return true;
    }

    private void selectTab(String str) {
        Tab tab = (Tab) editWindow.getFellowIfAny(str);
        if (tab != null) {
            tab.setSelected(true);
        }
    }

    private void showInvalidWorkReportTypeName() {
        try {
            model.validateTemplateName(name.getValue());
        } catch (IllegalArgumentException e) {
            throw new WrongValueException(name, _(e.getMessage()));
        }
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        getVisibility().showOnly(listWindow);

        final URLHandler<IOrderTemplatesControllerEntryPoints> handler = handlerRegistry
                .getRedirectorFor(IOrderTemplatesControllerEntryPoints.class);
        handler.registerListener(this, page);

        setBreadcrumbs(comp);
    }

    private void setBreadcrumbs(Component comp) {
        Component breadcrumbs = comp.getPage().getFellow("breadcrumbs");
        if (breadcrumbs.getChildren() != null) {
            breadcrumbs.getChildren().clear();
        }
        breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
        breadcrumbs.appendChild(new Label(_("Scheduling")));
        breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
        breadcrumbs.appendChild(new Label(_("Project Templates")));
    }

    public void openTemplateTree() {
        if (treeComponent == null) {
            final TemplatesTreeController treeController = new TemplatesTreeController(
                    model, this);
            treeComponent = (TreeComponent) editWindow.getFellow("orderElementTree");
            treeComponent.useController(treeController);
            treeController.setReadOnly(false);
        }

        final Tree tree = (Tree) treeComponent.getFellowIfAny("tree");
        if (tree.getModel() == null) {
            setTreeRenderer(treeComponent);
            reloadTree(treeComponent);
        }
    }

    private void reloadTree(TreeComponent orderElementsTree) {
        final Tree tree = (Tree) orderElementsTree.getFellowIfAny("tree");
        tree.setModel(orderElementsTree.getController().getTreeModel());
        tree.addEventListener(Events.ON_SELECT, new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                //undo the work done by this event
                //to be able to control it from the ON_CLICK event
                tree.clearSelection();
            }
        });
    }

    private void setTreeRenderer(TreeComponent orderElementsTree) {
        final Tree tree = (Tree) orderElementsTree.getFellowIfAny("tree");
        tree.setTreeitemRenderer(orderElementsTree.getController().getRenderer());
    }

    public Constraint validateTemplateName() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                try {
                    model.validateTemplateName((String) value);
                } catch (IllegalArgumentException e) {
                    throw new WrongValueException(comp, _(e.getMessage()));
                }
            }
        };
    }
}
