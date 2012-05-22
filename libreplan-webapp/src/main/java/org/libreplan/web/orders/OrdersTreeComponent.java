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
package org.libreplan.web.orders;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.List;

import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.trees.ITreeNode;
import org.libreplan.web.orders.OrderElementTreeController.OrderElementTreeitemRenderer;
import org.libreplan.web.tree.TreeComponent;
import org.libreplan.web.tree.TreeController;
import org.zkoss.zul.Treeitem;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class OrdersTreeComponent extends TreeComponent {

    abstract class OrdersTreeColumn extends Column {
        OrdersTreeColumn(String label, String cssClass, String tooltip) {
            super(label, cssClass, tooltip);
        }

        OrdersTreeColumn(String label, String cssClass) {
            super(label, cssClass);
        }

        @Override
        public <T extends ITreeNode<T>> void doCell(
                TreeController<T>.Renderer renderer,
                Treeitem item, T currentElement) {
            OrderElementTreeitemRenderer treeRenderer = OrderElementTreeitemRenderer.class
                    .cast(renderer);
            doCell(treeRenderer, OrderElement.class.cast(currentElement));
        }

        protected abstract void doCell(
                OrderElementTreeitemRenderer treeRenderer,
                OrderElement currentElement);

    }

    public List<Column> getColumns() {
        List<Column> columns = new ArrayList<Column>();
        columns.add(schedulingStateColumn);
        columns.add(codeColumn);
        columns.add(nameAndDescriptionColumn);
        columns.add(new OrdersTreeColumn(_("Budget"), "budget",
                _("Total task budget")) {

            @Override
            protected void doCell(OrderElementTreeitemRenderer treeRenderer,
                    OrderElement currentElement) {
                treeRenderer.addBudgetCell(currentElement);
            }

        });
        columns.add(new OrdersTreeColumn(_("Start"),
                        "estimated_init",
                _("Cost prevision start date")) {

            @Override
            protected void doCell(OrderElementTreeitemRenderer treeRenderer,
                    OrderElement currentElement) {
                treeRenderer.addInitDateCell(currentElement);
            }

        });
        columns.add(new OrdersTreeColumn(_("End"),
                        "estimated_end",
                _("Cost prevision end date")) {

            @Override
            protected void doCell(OrderElementTreeitemRenderer treeRenderer,
                    OrderElement currentElement) {
                treeRenderer.addEndDateCell(currentElement);
            }
        });
        columns.add(operationsColumn);
        return columns;
    }

    @Override
    public boolean isCreateFromTemplateEnabled() {
        return true;
    }
}
