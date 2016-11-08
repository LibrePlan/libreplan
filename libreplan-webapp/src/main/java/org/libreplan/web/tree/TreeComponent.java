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
package org.libreplan.web.tree;

import static org.libreplan.web.I18nHelper._;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.libreplan.business.trees.ITreeNode;
import org.libreplan.web.orders.OrderElementTreeController;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Treeitem;

/**
 * Macro component for order elements tree and similar pages.
 * <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public abstract class TreeComponent extends HtmlMacroComponent {

    private static final String CONTROLLER_NAME = "treeController";

    protected Column codeColumn = new Column(_("Code"), "code") {
        @Override
        public <T extends ITreeNode<T>> void doCell(TreeController<T>.Renderer renderer, Treeitem item, T currentElement) {
            renderer.addCodeCell(currentElement);
        }
    };

    protected final Column nameAndDescriptionColumn = new Column(_("Name"), "name") {
        @Override
        public <T extends ITreeNode<T>> void doCell(TreeController<T>.Renderer renderer, Treeitem item, T currentElement) {
            renderer.addDescriptionCell(currentElement);
        }
    };

    protected final Column operationsColumn = new Column(_("Op."), "operations", _("Operations")) {
        @Override
        public <T extends ITreeNode<T>> void doCell(TreeController<T>.Renderer renderer, Treeitem item, T currentElement) {
            renderer.addOperationsCell(item, currentElement);
        }
    };

    protected final Column schedulingStateColumn = new Column(
            _("Scheduling state"),
            "scheduling_state",
            _("Fully, Partially or Unscheduled. (Drag and drop to move tasks)")) {

        @Override
        public <T extends ITreeNode<T>> void doCell(TreeController<T>.Renderer renderer, Treeitem item, T currentElement) {
            renderer.addSchedulingStateCell(currentElement);
        }
    };

    private void doAfterComposeOnController(Composer controller) {
        try {
            controller.doAfterCompose(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public abstract static class Column {

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

        /* TODO remove me, if ZK Load on demand issue will be resolved */
        public String getHflex() {
            return "name".equals(cssClass) ? "1" : "min";
        }

        /* TODO remove me, if ZK Load on demand issue will be resolved */
        public String getWidth() {
            if (cssClass.contains("scheduling_state")) {
                return "135px";
            } else if ("code".equals(cssClass)) {
                return "106px";
            } else if ("name".equals(cssClass)) {
                return "950px";
            } else if ("hours".equals(cssClass) || "budget".equals(cssClass) || "operations".equals(cssClass)) {
                return "50px";
            } else if ("estimated_init".equals(cssClass) || "estimated_end".equals(cssClass)) {
                return "100px";
            }

            return "";
        }

        public abstract <T extends ITreeNode<T>> void doCell(TreeController<T>.Renderer renderer, Treeitem item, T currentElement);
    }

    public abstract List<Column> getColumns();

    public void clear() {
        OrderElementTreeController controller = (OrderElementTreeController) getAttribute(CONTROLLER_NAME, true);
        controller.clear();
    }

    public void useController(TreeController<?> controller) {
        doAfterComposeOnController(controller);
        controller.setColumns(getColumns());
        this.setAttribute(CONTROLLER_NAME, controller, true);
    }

    public TreeController<?> getController() {
        return (TreeController<?>) getAttribute(CONTROLLER_NAME, true);
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
