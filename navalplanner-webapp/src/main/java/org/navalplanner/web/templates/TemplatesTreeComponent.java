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

import java.util.ArrayList;
import java.util.List;

import org.navalplanner.business.templates.entities.OrderElementTemplate;
import org.navalplanner.business.trees.ITreeNode;
import org.navalplanner.web.templates.TemplatesTreeController.TemplatesTreeRenderer;
import org.navalplanner.web.tree.TreeComponent;
import org.navalplanner.web.tree.TreeController;
import org.zkoss.zul.Treeitem;

/**
 * Tree component for templates <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TemplatesTreeComponent extends TreeComponent {

    private abstract class TemplatesTreeColumn extends Column {

        TemplatesTreeColumn(String label, String cssClass, String tooltip) {
            super(label, cssClass, tooltip);
        }

        TemplatesTreeColumn(String label, String cssClass) {
            super(label, cssClass);
        }

        public final <T extends ITreeNode<T>> void doCell(
                TreeController<T>.Renderer renderer,
                Treeitem item, T currentElement) {
            doCell(TemplatesTreeRenderer.class.cast(renderer), item,
                    OrderElementTemplate.class.cast(currentElement));
        }

        protected abstract void doCell(TemplatesTreeRenderer renderer,
                Treeitem item, OrderElementTemplate currentElement);

    }

    public String getAddElementLabel() {
        return _("New Template element");
    }

    public boolean isCreateTemplateEnabled() {
        return true;
    }

    public String getRemoveElementLabel() {
        return _("Delete Template element");
    }

    @Override
    public List<Column> getColumns() {
        List<Column> result = new ArrayList<Column>();

        result.add(schedulingStateColumn);
        result.add(codeColumn);
        result.add(nameAndDescriptionColumn);
        result.add(new TemplatesTreeColumn(_("Hours"), "hours") {

            @Override
            protected void doCell(TemplatesTreeRenderer renderer,
                    Treeitem item, OrderElementTemplate currentElement) {
                renderer.addHoursCell(currentElement);
            }

        });
        result.add(new TemplatesTreeColumn(
                _("Must start after (days since beginning project)"),
                "estimated_init") {

            @Override
            protected void doCell(TemplatesTreeRenderer renderer,
                    Treeitem item, OrderElementTemplate currentElement) {
                renderer.addInitCell(currentElement);
            }

        });
        result.add(new TemplatesTreeColumn(
                _("Deadline (days since beggining project)"),
                "estimated_end") {

            @Override
            protected void doCell(TemplatesTreeRenderer renderer,
                    Treeitem item, OrderElementTemplate currentElement) {
                renderer.addEndCell(currentElement);
            }

        });
        result.add(operationsColumn);
        return result;
    }

}
