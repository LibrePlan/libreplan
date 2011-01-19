/*
 * This file is part of NavalPlan
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
package org.navalplanner.web.tree;

import static org.navalplanner.web.I18nHelper._;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.trees.ITreeNode;
import org.navalplanner.web.orders.OrderElementTreeController;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Treeitem;

/**
 * macro component for order elements tree and similar pages<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class TreeComponent extends HtmlMacroComponent {

    private static final String CONTROLLER_NAME = "treeController";

    public static abstract class Column {
        private String label;

        private String cssClass;

        private String tooltip;

        public Column(String label, String cssClass){
            this(label, cssClass, null);
        }

        public Column(String label, String cssClass, String tooltip) {
            this.label = label;
            if (!StringUtils.isEmpty(tooltip)) {
                this.tooltip = tooltip;
                cssClass += " help-tooltip";
            }
            this.cssClass = cssClass;
        }

        public String getLabel() {
            return label;
        }

        public String getCssClass() {
            return cssClass;
        }

        public String getTooltip() {
            return tooltip;
        }

        public abstract <T extends ITreeNode<T>> void doCell(
                TreeController<T>.Renderer renderer,
                Treeitem item, T currentElement);
    }

    protected final Column codeColumn = new Column(_("Code"), "code") {

        @Override
        public <T extends ITreeNode<T>> void doCell(
                TreeController<T>.Renderer renderer,
                Treeitem item, T currentElement) {
            renderer.addCodeCell(currentElement);
        }
    };
    protected final Column nameAndDescriptionColumn = new Column(_("Name"),
            "name") {

        @Override
        public <T extends ITreeNode<T>> void doCell(
                TreeController<T>.Renderer renderer,
                Treeitem item, T currentElement) {
            renderer.addDescriptionCell(currentElement);
        }
    };
    protected final Column operationsColumn = new Column(_("Operations"),
            "operations") {

        @Override
        public <T extends ITreeNode<T>> void doCell(
                TreeController<T>.Renderer renderer,
                Treeitem item, T currentElement) {
            renderer.addOperationsCell(item, currentElement);
        }
    };

    protected final Column schedulingStateColumn = new Column(
            _("Scheduling state"),
            "scheduling_state",
            _("Complete, Partially or Not Scheduled. (Drag and drop to move tasks)")) {

        @Override
        public <T extends ITreeNode<T>> void doCell(
                TreeController<T>.Renderer renderer,
                Treeitem item, T currentElement) {
            renderer.addSchedulingStateCell(currentElement);
        }

    };

    public abstract List<Column> getColumns();

    public void clear() {
        OrderElementTreeController controller = (OrderElementTreeController) getVariable(
                CONTROLLER_NAME, true);
        controller.clear();
    }

    public void useController(TreeController<?> controller) {
        doAfterComposeOnController(controller);
        controller.setColumns(getColumns());
        this.setVariable(CONTROLLER_NAME, controller, true);
    }

    public TreeController<?> getController() {
        return (TreeController<?>) getVariable(CONTROLLER_NAME, true);
    }

    private void doAfterComposeOnController(Composer controller) {
        try {
            controller.doAfterCompose(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getAddElementLabel() {
        return _("Add");
    }

    public boolean isCreateTemplateEnabled() {
        return true;
    }

    public boolean isCreateFromTemplateEnabled() {
        return false;
    }

    public String getRemoveElementLabel() {
        return _("Delete task");
    }
}
