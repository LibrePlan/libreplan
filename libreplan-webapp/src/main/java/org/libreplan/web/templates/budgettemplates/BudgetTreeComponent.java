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
package org.libreplan.web.templates.budgettemplates;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.List;

import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.business.trees.ITreeNode;
import org.libreplan.web.templates.budgettemplates.TemplatesTreeController.TemplatesTreeRenderer;
import org.libreplan.web.tree.TreeController;
import org.zkoss.zul.Treeitem;

public class BudgetTreeComponent extends TemplatesTreeComponent {

    public String getRemoveElementLabel() {
        return _("Delete budget element");
    }

    @Override
    public List<Column> getColumns() {
        List<Column> result = new ArrayList<Column>();

        result.add(new Column("Node", "") {

            @Override
            public <T extends ITreeNode<T>> void doCell(
                    TreeController<T>.Renderer renderer, Treeitem item,
                    T currentElement) {
                renderer.addSchedulingStateCell(currentElement);
            }

        });

        result.add(codeColumn);
        result.add(nameAndDescriptionColumn);
        result.add(new TemplatesTreeColumn(_("TOTAL"), "budget") {

            @Override
            protected void doCell(TemplatesTreeRenderer renderer,
                    Treeitem item, OrderElementTemplate currentElement) {
                renderer.addBudgetCell(currentElement);
            }

        });
        result.add(new TemplatesTreeColumn(_("Cost/Salary"), "budget") {

            @Override
            protected void doCell(TemplatesTreeRenderer renderer,
                    Treeitem item, OrderElementTemplate currentElement) {
                renderer.addCostSalaryCell(currentElement);
            }

        });
        result.add(new TemplatesTreeColumn(_("Type"), "budgettype") {

            @Override
            protected void doCell(TemplatesTreeRenderer renderer,
                    Treeitem item, OrderElementTemplate currentElement) {
                renderer.addBudgetLineTypeCell(currentElement);
            }

        });
        result.add(new TemplatesTreeColumn(_("Duration"), "units") {

            @Override
            protected void doCell(TemplatesTreeRenderer renderer,
                    Treeitem item, OrderElementTemplate currentElement) {
                renderer.addDurationCell(currentElement);
            }

        });
        result.add(new TemplatesTreeColumn(_("Quantity"), "units") {

            @Override
            protected void doCell(TemplatesTreeRenderer renderer,
                    Treeitem item, OrderElementTemplate currentElement) {
                renderer.addQuantityCell(currentElement);
            }

        });
        result.add(new TemplatesTreeColumn(_("Sev. pay"), "budget",
                _("Severance pay")) {

            @Override
            protected void doCell(TemplatesTreeRenderer renderer,
                    Treeitem item, OrderElementTemplate currentElement) {
                renderer.addIndemnizationSalaryCell(currentElement);
            }

        });
        result.add(new TemplatesTreeColumn(_("Hol. salary"), "budget",
                _("Holiday salary")) {

            @Override
            protected void doCell(TemplatesTreeRenderer renderer,
                    Treeitem item, OrderElementTemplate currentElement) {
                renderer.addHolidaySalaryCell(currentElement);
            }

        });
        result.add(new TemplatesTreeColumn(_("Start"), "estimated_init") {

            @Override
            protected void doCell(TemplatesTreeRenderer renderer,
                    Treeitem item, OrderElementTemplate currentElement) {
                renderer.addStartDateCell(currentElement);
            }

        });
        result.add(new TemplatesTreeColumn(_("End"), "estimated_end") {

            @Override
            protected void doCell(TemplatesTreeRenderer renderer,
                    Treeitem item, OrderElementTemplate currentElement) {
                renderer.addEndDateCell(currentElement);
            }
        });
        result.add(operationsColumn);
        return result;
    }
}
