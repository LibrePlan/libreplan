/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 *
 * Copyright (C) 2011-2012 Igalia S.L
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

package org.libreplan.web.orders;

import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.web.templates.IOrderTemplatesControllerEntryPoints;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treeitem;

import static org.libreplan.web.I18nHelper._;

/**
 * Encapsulates the operations (up, down, indent, unindent, etc) for an element of the tree.
 * The element can be an OrderElement or a TemplateElement
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
public abstract class TreeElementOperationsController<T> {

    protected Tree tree;

    public void editSelectedElement() {
        if (tree.getSelectedCount() == 1) {
            showEditElement(tree.getSelectedItem());
        } else {
            showSelectAnElementError();
        }
    }

    protected void showSelectAnElementError() {
        Messagebox.show(_("Please select a task"));
    }

    protected abstract void showEditElement(Treeitem treeitem);

    public void moveSelectedElementUp() {
        if (tree.getSelectedCount() == 1) {
            Treeitem item =  tree.getSelectedItem();
            up(item.getValue());
            Treeitem brother = (Treeitem) item.getPreviousSibling();

            if (brother != null) {
                brother.setSelected(true);
            }
        } else {
            showSelectAnElementError();
        }
    }

    protected abstract void up(T element);

    public void moveSelectedElementDown() {
        if (tree.getSelectedCount() == 1) {
            Treeitem item =  tree.getSelectedItem();
            down(item.getValue());
            Treeitem brother = (Treeitem) item.getNextSibling();

            if (brother != null) {
                brother.setSelected(true);
            }
        } else {
            showSelectAnElementError();
        }
    }

    protected abstract void down(T element);

    public void indentSelectedElement() {
        if (tree.getSelectedCount() == 1) {
            int page = tree.getActivePage();
            indent(getSelectedElement());

            if (tree.getPageCount() > page) {
                tree.setActivePage(page);
            }
        } else {
            showSelectAnElementError();
        }
    }

    protected abstract T getSelectedElement();

    protected abstract void indent(T element);

    public void unindentSelectedElement() {
        if (tree.getSelectedCount() == 1) {
            unindent(getSelectedElement());
        } else {
            showSelectAnElementError();
        }
    }

    protected abstract void unindent(T element);

    public void deleteSelectedElement() {
        if (tree.getSelectedCount() == 1) {
            remove(getSelectedElement());
        } else {
            showSelectAnElementError();
        }
    }

    protected abstract void remove(T element);

}

/**
 * Implements tree operations for an {@link OrderElement}
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
class OrderElementOperations extends TreeElementOperationsController<OrderElement> {

    private OrderElementTreeController treeController;

    private IOrderModel orderModel;

    private OrderElementController orderElementController;

    private IOrderTemplatesControllerEntryPoints orderTemplates;

    private OrderElementOperations() {}

    public static OrderElementOperations build() {
        return new OrderElementOperations();
    }

    public OrderElementOperations tree(Tree tree) {
        super.tree = tree;

        return this;
    }

    public OrderElementOperations treeController(OrderElementTreeController treeController) {
        this.treeController = treeController;

        return this;
    }

    public OrderElementOperations orderModel(IOrderModel orderModel) {
        this.orderModel = orderModel;

        return this;
    }

    public OrderElementOperations orderElementController(OrderElementController orderElementController) {
        this.orderElementController = orderElementController;

        return this;
    }

    public OrderElementOperations orderTemplates(IOrderTemplatesControllerEntryPoints orderTemplates) {
        this.orderTemplates = orderTemplates;

        return this;
    }

    @Override
    protected OrderElement getSelectedElement() {
        return treeController.getSelectedNode();
    }

    @Override
    protected void up(OrderElement element) {
        treeController.up(element);
    }

    @Override
    protected void down(OrderElement element) {
        treeController.down(element);
    }

    @Override
    protected void indent(OrderElement element) {
        treeController.indent(element);
    }

    @Override
    protected void unindent(OrderElement element) {
        treeController.unindent(element);
    }

    @Override
    protected void remove(OrderElement element) {
        treeController.remove(element);
    }

    @Override
    protected void showEditElement(Treeitem item) {
        OrderElement orderElement = item.getValue();
        treeController.markModifiedTreeitem(item.getTreerow());
        IOrderElementModel model = orderModel.getOrderElementModel(orderElement);
        orderElementController.openWindow(model);
        treeController.refreshRow(item);
    }

    public void createTemplateFromSelectedElement() {
        if (tree.getSelectedCount() == 1) {
            createTemplate(getSelectedElement());
        } else {
            showSelectAnElementError();
        }
    }

    private void createTemplate(OrderElement element) {
        if (element.isNewObject()) {
            notifyTemplateCantBeCreated();

            return;
        }
        if (showConfirmCreateTemplateDialog() == Messagebox.OK) {
            orderTemplates.goToCreateTemplateFrom(element);
        }
    }

    private int showConfirmCreateTemplateDialog() {
        return Messagebox.show(
                _("Unsaved changes will be lost. Would you like to continue?"),
                _("Confirm create template"), Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);
    }

    private void notifyTemplateCantBeCreated() {
        Messagebox.show(
                _("Templates can only be created out of existent tasks." +
                        "You are trying to create a template out of a new task.\n" +
                        "Please save your project before proceeding."),
                _("Operation cannot be done"), Messagebox.OK, Messagebox.INFORMATION);
    }

}