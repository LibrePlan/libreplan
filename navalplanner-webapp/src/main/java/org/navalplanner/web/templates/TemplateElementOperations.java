/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 *
 * Copyright (C) 2011 Igalia S.L
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
import org.navalplanner.web.orders.TreeElementOperationsController;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treeitem;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
class TemplateElementOperations extends TreeElementOperationsController<OrderElementTemplate> {

    private TemplatesTreeController treeController;

    private OrderTemplatesController orderTemplatesController;

    public static TemplateElementOperations build() {
        return new TemplateElementOperations();
    }

    private TemplateElementOperations() {

    }

    public TemplateElementOperations tree(Tree tree) {
        super.tree = tree;
        return this;
    }

    public TemplateElementOperations treeController(TemplatesTreeController treeController) {
        this.treeController = treeController;
        return this;
    }

    public TemplateElementOperations orderTemplatesController(
            OrderTemplatesController orderTemplatesController) {
        this.orderTemplatesController = orderTemplatesController;
        return this;
    }

    @Override
    protected void showEditElement(Treeitem item) {
        OrderElementTemplate orderElement = (OrderElementTemplate) item.getValue();
        treeController.markModifiedTreeitem(item.getTreerow());
        orderTemplatesController.showEditionFor(orderElement);
        treeController.refreshRow(item);
    }

    @Override
    protected void up(OrderElementTemplate element) {
        treeController.up(element);
    }

    @Override
    protected void down(OrderElementTemplate element) {
        treeController.down(element);
    }

    @Override
    protected OrderElementTemplate getSelectedElement() {
        return treeController.getSelectedNode();
    }

    @Override
    protected void indent(OrderElementTemplate element) {
        treeController.indent(element);
    }

    @Override
    protected void unindent(OrderElementTemplate element) {
        treeController.unindent(element);
    }

    @Override
    protected void remove(OrderElementTemplate element) {
        treeController.remove(element);
    }

}
