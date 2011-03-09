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

package org.navalplanner.web.limitingresources;

import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.api.Tree;

public class LimitingResourcesLeftPane extends HtmlMacroComponent {

    private MutableTreeModel<LimitingResourceQueue> modelForTree;

    public LimitingResourcesLeftPane(
            MutableTreeModel<LimitingResourceQueue> treeModel) {
        this.modelForTree = treeModel;
    }

    @Override
    public void afterCompose() {
        super.afterCompose();
        getContainerTree().setModel(modelForTree);
        getContainerTree().setTreeitemRenderer(getRendererForTree());
    }

    private TreeitemRenderer getRendererForTree() {
        return new TreeitemRenderer() {
            @Override
            public void render(Treeitem item, Object data)
                    throws Exception {
                LimitingResourceQueue line = (LimitingResourceQueue) data;
                item.setOpen(false);
                item.setValue(line);

                Treerow row = new Treerow();
                Treecell cell = new Treecell();
                Component component = createComponent(line);
                item.appendChild(row);
                row.appendChild(cell);
                cell.appendChild(component);
            }

            private Component createComponent(LimitingResourceQueue line) {
                return isTopLevel(line) ? createFirstLevel(line)
                        : createSecondLevel(line);
            }

            private boolean isTopLevel(LimitingResourceQueue line) {
                int[] path = modelForTree.getPath(modelForTree.getRoot(), line);
                return path.length == 0;
            }
        };
    }

    private Tree getContainerTree() {
        return (Tree) getFellow("loadsTree");
    }

    private Component createFirstLevel(LimitingResourceQueue main) {
        Div result = createLabelWithName(main);
        result.setSclass("firstlevel");
        return result;
    }

    private Component createSecondLevel(LimitingResourceQueue loadTimeLine) {
        Div result = createLabelWithName(loadTimeLine);
        result.setSclass("secondlevel");
        return result;
    }

    private Div createLabelWithName(LimitingResourceQueue main) {
        Div result = new Div();
        Label label = new Label();
        final String conceptName = main.getResource().getName();
        label.setValue(conceptName);
        result.appendChild(label);
        return result;
    }
}
