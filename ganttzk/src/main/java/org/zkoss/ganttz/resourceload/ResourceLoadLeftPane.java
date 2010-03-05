/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.zkoss.ganttz.resourceload;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.api.Tree;

public class ResourceLoadLeftPane extends HtmlMacroComponent {

    private MutableTreeModel<LoadTimeLine> modelForTree;
    private final ResourceLoadList resourceLoadList;

    public ResourceLoadLeftPane(
MutableTreeModel<LoadTimeLine> modelForTree,
            ResourceLoadList resourceLoadList) {
        this.resourceLoadList = resourceLoadList;
        this.modelForTree = modelForTree;
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
                LoadTimeLine line = (LoadTimeLine) data;
                item.setOpen(false);
                item.setValue(line);

                Treerow row = new Treerow();
                Treecell cell = new Treecell();
                Component component = createComponent(line);
                item.appendChild(row);
                row.appendChild(cell);
                cell.appendChild(component);
                collapse(line);
                addExpandedListener(item, line);
            }

            private void addExpandedListener(final Treeitem item,
                    final LoadTimeLine line) {
                item.addEventListener("onOpen", new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        OpenEvent openEvent = (OpenEvent) event;
                        if (openEvent.isOpen()) {
                            List<LoadTimeLine> closed = calculatedClosedItems(item);
                            expand(line, closed);
                        } else {
                            collapse(line);
                        }
                    }
                });
            }

            private Component createComponent(LoadTimeLine line) {
                return isTopLevel(line) ? createFirstLevel(line)
                        : createSecondLevel(line);
            }

            private boolean isTopLevel(LoadTimeLine line) {
                int[] path = modelForTree.getPath(modelForTree.getRoot(), line);
                return path.length == 0;
            }
        };
    }

    private void collapse(LoadTimeLine line) {
        resourceLoadList.collapse(line);
    }

    private void expand(LoadTimeLine line, List<LoadTimeLine> closed) {
        resourceLoadList.expand(line, closed);
    }

    private List<LoadTimeLine> calculatedClosedItems(Treeitem item) {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        Treechildren treeChildren = item.getTreechildren();
        if (treeChildren != null) {
            List<Treeitem> myTreeItems = (List<Treeitem>) treeChildren
                    .getChildren();
            Iterator<Treeitem> iterator = myTreeItems.iterator();
            while (iterator.hasNext()) {
                Treeitem child = (Treeitem) iterator.next();
                if (!child.isOpen()) {
                    result.addAll(getLineChildrenBy(child));
                } else {
                    result.addAll(calculatedClosedItems(child));
                }
            }
        }
        return result;
    }

    private List<LoadTimeLine> getLineChildrenBy(Treeitem item) {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        LoadTimeLine line = getLineByTreeitem(item);
        if (line != null) {
            result.addAll(line.getAllChildren());
        }
        return result;
    }

    private LoadTimeLine getLineByTreeitem(Treeitem child) {
        LoadTimeLine line = null;
        try {
            line = (LoadTimeLine) child.getValue();
        } catch (Exception e) {
            return null;
        }
        return line;
    }

    private Tree getContainerTree() {
        return (Tree) getFellow("loadsTree");
    }

    private Component createFirstLevel(LoadTimeLine principal) {
        Div result = createLabelWithName(principal);
        result.setSclass("firstlevel");
        return result;
    }

    private Component createSecondLevel(LoadTimeLine loadTimeLine) {
        Div result = createLabelWithName(loadTimeLine);
        result.setSclass("secondlevel");
        return result;
    }

    private Div createLabelWithName(LoadTimeLine principal) {
        Div result = new Div();
        Label label = new Label();
        final String conceptName = principal.getConceptName();
        label.setValue(conceptName);
        limitValue(result, label, 40);
        result.appendChild(label);
        return result;
    }

    private static void limitValue(Div parent, Label label, int maxLength) {
        String originalValue = label.getValue();
        if (originalValue == null || originalValue.length() <= maxLength) {
            return;
        }
        label.setValue(originalValue.substring(0, maxLength - 3) + "...");
        label.setTooltip(createPopup(parent, originalValue));
    }

    private static Popup createPopup(Div parent, String originalValue) {
        Popup result = new Popup();
        result.appendChild(new Label(originalValue));
        parent.appendChild(result);
        return result;
    }
}
