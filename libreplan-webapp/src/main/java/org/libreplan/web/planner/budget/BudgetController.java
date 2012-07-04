/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.web.planner.budget;

import static org.libreplan.web.I18nHelper._;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.web.common.Util;
import org.libreplan.web.planner.order.ISaveCommand;
import org.libreplan.web.planner.tabs.IGlobalViewEntryPoints;
import org.libreplan.web.templates.budgettemplates.EditTemplateWindowController;
import org.libreplan.web.templates.budgettemplates.IEditionSubwindowController;
import org.libreplan.web.templates.budgettemplates.TemplatesTreeController;
import org.libreplan.web.templates.labels.LabelsAssignmentToTemplateComponent;
import org.libreplan.web.templates.quality.QualityFormAssignerComponent;
import org.libreplan.web.tree.TreeComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Window;
import org.zkoss.zul.api.Div;

/**
 * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
 */
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BudgetController extends GenericForwardComposer implements
        IEditionSubwindowController {

    @Autowired
    private IBudgetModel model;

    private TreeComponent treeComponent;

    private Div editWindow;

    private Button saveOrderAndContinueButton;

    private Button cancelEditionButton;

    private Button closeBudgetButton;

    private EditTemplateWindowController editTemplateController;

    private IGlobalViewEntryPoints entryPointsController;

    public void init(Order order, ISaveCommand saveCommand) {
        model.initEdit(order, editWindow.getDesktop());
        showEditWindow();
    }

    private void showEditWindow() {
        // openTemplateTree is not called if it's the first tab shown
        openTemplateTree();
        bindLabelsControllerWithCurrentTemplate();
        bindQualityFormWithCurrentTemplate();
        bindEditTemplateWindowWithController();
        setupGlobalButtons();

        Util.createBindingsFor(editWindow);
    }

    private void bindEditTemplateWindowWithController() {
        Window editTemplateWindow = (Window) editWindow
                .getFellow("editTemplateWindow");
        editTemplateController = EditTemplateWindowController.bindTo(model,
                editTemplateWindow);
    }

    public void openTemplateTree() {
        if (treeComponent == null) {
            final TemplatesTreeController treeController = new TemplatesTreeController(
                    model, this);
            treeComponent = (TreeComponent) editWindow
                    .getFellow("orderElementTree");
            treeComponent.useController(treeController);
            controlSelectionWithOnClick(getTreeFrom(treeComponent));
            treeController.setReadOnly(model.isReadOnly());
            setTreeRenderer(treeComponent);
        }
        bindTemplatesTreeWithModel();
    }

    private Tree getTreeFrom(TreeComponent treeComponent) {
        return (Tree) treeComponent.getFellowIfAny("tree");
    }

    private void controlSelectionWithOnClick(final Tree tree) {
        tree.addEventListener(Events.ON_SELECT, new EventListener() {
            @Override
            public void onEvent(Event event) {
                // undo the work done by this event
                // to be able to control it from the ON_CLICK event
                tree.clearSelection();
            }
        });
    }

    private void setTreeRenderer(TreeComponent orderElementsTree) {
        final Tree tree = (Tree) orderElementsTree.getFellowIfAny("tree");
        tree.setTreeitemRenderer(orderElementsTree.getController()
                .getRenderer());
    }

    private <T extends Component> T findAtEditWindow(String id, Class<T> type) {
        return type.cast(editWindow.getFellow(id));
    }

    private void bindTemplatesTreeWithModel() {
        if (treeComponent == null) {
            // if the tree is not initialized yet no bind has to be done
            return;
        }
        treeComponent.getController().bindModelIfNeeded();
    }

    public void showEditionFor(OrderElementTemplate template) {
        editTemplateController.open(template);
    }

    private void bindLabelsControllerWithCurrentTemplate() {
        LabelsAssignmentToTemplateComponent c = findAtEditWindow(
                "listOrderElementLabels",
                LabelsAssignmentToTemplateComponent.class);
        c.getController().openWindow(model);
    }

    private void bindQualityFormWithCurrentTemplate() {
        QualityFormAssignerComponent c = findAtEditWindow(
                "assignedQualityForms", QualityFormAssignerComponent.class);
        c.useModel(model);
    }

    private void setupGlobalButtons() {
        Hbox perspectiveButtonsInsertionPoint = (Hbox) page
                .getFellow("perspectiveButtonsInsertionPoint");
        perspectiveButtonsInsertionPoint.getChildren().clear();

        saveOrderAndContinueButton.setParent(perspectiveButtonsInsertionPoint);
        cancelEditionButton.setParent(perspectiveButtonsInsertionPoint);

        saveOrderAndContinueButton.setVisible(true);
        cancelEditionButton.setVisible(true);

        saveOrderAndContinueButton.addEventListener(Events.ON_CLICK,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        try {
                            model.saveThroughPlanningState(true);
                        } catch (ValidationException e) {
                            Messagebox.show(e.getMessage());
                        }
                    }
                });

        cancelEditionButton.addEventListener(Events.ON_CLICK,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        try {
                            Messagebox.show(
                                    _("Unsaved changes will be lost. Are you sure?"),
                                    _("Confirm exit dialog"),
                                    Messagebox.OK | Messagebox.CANCEL,
                                    Messagebox.QUESTION,
                                    new org.zkoss.zk.ui.event.EventListener() {
                                        public void onEvent(Event evt)
                                                throws InterruptedException {
                                            if (evt.getName().equals("onOK")) {
                                                Executions.sendRedirect(
                                                        "/planner/index.zul;company_scheduling");
                                            }
                                        }
                                    });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });

        closeBudgetButton.addEventListener(Events.ON_CLICK,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        try {
                            Messagebox.show(
                                    _("The budget will be saved, and you won't be able to change it later. Are you sure?"),
                                    _("Confirm budget close"),
                                    Messagebox.OK | Messagebox.CANCEL,
                                    Messagebox.QUESTION,
                                    new org.zkoss.zk.ui.event.EventListener() {
                                        public void onEvent(Event evt)
                                                throws InterruptedException {
                                            if (evt.getName().equals("onOK")) {
                                                try {
                                                    model.closeBudget();
                                                    model.saveThroughPlanningState(false);
                                                    entryPointsController.goToOrderDetails(model.getAssociatedOrder());
                                                } catch (ValidationException e) {
                                                    Messagebox.show(e.getMessage());
                                                }
                                            }
                                        }
                                    });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
        if (model.isReadOnly()) {
            saveOrderAndContinueButton.setDisabled(true);
            closeBudgetButton.setDisabled(true);
        }

    }

    public void setEntryPointsController(
            IGlobalViewEntryPoints entryPointsController) {
        this.entryPointsController = entryPointsController;
    }
}
