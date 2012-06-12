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

import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.planner.order.ISaveCommand;
import org.libreplan.web.templates.budgettemplates.IBudgetTemplatesModel;
import org.libreplan.web.templates.budgettemplates.TemplatesTreeController;
import org.libreplan.web.tree.TreeComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Tree;
import org.zkoss.zul.api.Div;

/**
 * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
 */
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BudgetController extends GenericForwardComposer {

    @Autowired
    private IBudgetTemplatesModel model;

    private TreeComponent treeComponent;

    private Div editWindow;

    public void init(Order order, ISaveCommand saveCommand) {
        model.initEdit(order.getAssociatedBudgetObject());
        showEditWindow();
    }

    private void showEditWindow() {
        // openTemplateTree is not called if it's the first tab shown
        bindTemplatesTreeWithModel();
    }

    public void openTemplateTree() {
        if (treeComponent == null) {
            final TemplatesTreeController treeController = new TemplatesTreeController(
                    model, null);
            treeComponent = (TreeComponent) editWindow
                    .getFellow("orderElementTree");
            treeComponent.useController(treeController);
            controlSelectionWithOnClick(getTreeFrom(treeComponent));
            treeController.setReadOnly(false);
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

    private void bindTemplatesTreeWithModel() {
        if (treeComponent == null) {
            // if the tree is not initialized yet no bind has to be done
            return;
        }
        treeComponent.getController().bindModelIfNeeded();
    }
}
