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

package org.zkoss.ganttz.resourceload;

import static org.zkoss.ganttz.i18n.I18nHelper._;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.ganttz.util.WeakReferencedListeners;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treechildren;

/**
 * Works with left pane of Resource Load page. Also works with right pane ( a little bit ).
 *
 * @author Óscar González Fernández
 * @author Manuel Rego Casasnovas
 * @author Susana Montes Pedreira
 * @author Lorenzo Tilve
 * @author Jacobo Aragunde Pérez
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
public class ResourceLoadLeftPane extends HtmlMacroComponent {

    private MutableTreeModel<LoadTimeLine> modelForTree;

    private final ResourceLoadList resourceLoadList;

    private WeakReferencedListeners<ISeeScheduledOfListener> scheduleListeners = WeakReferencedListeners.create();

    /**
     * {@link ResourceLoadLeftPane#onOpenEventQueue}, {@link OnOpenEvent} and proceedOnOpenEventQueue()
     * were created because of problem:
     * after migration from ZK5 to ZK8 onOpen event had been calling before render().
     * It produced a problem.
     * On onOpen event we are calculating closest items to treeItem.
     * render() was not called so, treeItem row had no value.
     * It made calculatedClosedItems(treeItem).isEmpty() to return true, even if it is not!
     *
     * http://forum.zkoss.org/question/101294/event-before-render-treeitem/
     */
    private OnOpenEvent onOpenEventQueue = null;

    /**
     * Made to know if {@link LoadTimeLine} was rendered.
     */
    private HashSet<LoadTimeLine> renderedLines;


    public ResourceLoadLeftPane(MutableTreeModel<LoadTimeLine> modelForTree, ResourceLoadList resourceLoadList) {
        this.resourceLoadList = resourceLoadList;
        this.modelForTree = modelForTree;
        this.renderedLines = new HashSet<>();
    }

    @Override
    public void afterCompose() {
        super.afterCompose();

        getContainerTree().setModel(modelForTree);
        getContainerTree().setItemRenderer(getRendererForTree());

        /* Force call overridden render() */
        try {
            if ( !this.resourceLoadList.getChildren().isEmpty() ) {
                getRendererForTree().render(
                        new Treeitem(""),
                        ((ResourceLoadComponent) this.resourceLoadList.getFirstChild()).getLoadLine(),
                        0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TreeitemRenderer getRendererForTree() {
        return new TreeitemRenderer() {
            @Override
            public void render(Treeitem treeitem, Object o, int index) throws Exception {
                LoadTimeLine line = (LoadTimeLine) o;
                treeitem.setOpen(false);
                treeitem.setValue(line);

                Treerow row = new Treerow();
                Treecell cell = new Treecell();
                Component component = createComponent(line);

                /* Clear existing Treerows */
                if ( !treeitem.getChildren().isEmpty() ) {
                    treeitem.getChildren().clear();
                }

                treeitem.appendChild(row);
                row.appendChild(cell);

                appendOperations(cell, line);

                cell.appendChild(component);

                collapse(line);
                addExpandedListener(treeitem, line);

                row.setSclass("resourceload-leftpanel-row");

                if ( onOpenEventQueue != null ) {
                    processOnOpenEventQueue();
                }

                renderedLines.add(line);
            }

            private void processOnOpenEventQueue() {
                if ( onOpenEventQueue.event.isOpen() ) {
                    List<LoadTimeLine> closed = calculatedClosedItems(onOpenEventQueue.treeitem);
                    expand(onOpenEventQueue.line, closed);
                } else {
                    collapse(onOpenEventQueue.line);
                }

                /*
                 * When queue processed, clean object, to make it kind of "unique" or "one time only".
                 */
                onOpenEventQueue = null;
            }

            private void appendOperations(final Treecell cell, final LoadTimeLine line) {
                if ( line.getRole().isVisibleScheduled() ) {
                    appendButtonPlan(cell, line);
                }
            }

            private void appendButtonPlan(final Treecell cell, final LoadTimeLine taskLine) {
                Button buttonPlan = new Button();
                buttonPlan.setSclass("icono");
                buttonPlan.setImage("/common/img/ico_planificador1.png");
                buttonPlan.setHoverImage("/common/img/ico_planificador.png");
                buttonPlan.setTooltiptext(_("See scheduling"));
                buttonPlan.addEventListener("onClick", event -> schedule(taskLine));

                cell.appendChild(buttonPlan);
            }

            /**
             * Do not replace it with lambda.
             */
            public void schedule(final LoadTimeLine taskLine) {
                scheduleListeners.fireEvent(
                        new WeakReferencedListeners.IListenerNotification<ISeeScheduledOfListener>() {
                            @Override
                            public void doNotify(ISeeScheduledOfListener listener) {
                                listener.seeScheduleOf(taskLine);
                            }
                        });
            }

            private void addExpandedListener(final Treeitem item, final LoadTimeLine line) {
                item.addEventListener("onOpen", event ->  {
                    OpenEvent openEvent = (OpenEvent) event;

                    if ( openEvent.isOpen() ) {

                        onOpenEventQueue = new OnOpenEvent(item, line, openEvent);

                        /* If line was rendered than we need to call expand manually */
                        if ( renderedLines.contains(line) ) {
                            processOnOpenEventQueue();
                        }

                    } else {
                        collapse(line);
                    }
                });
            }

            private Component createComponent(LoadTimeLine line) {
                return isTopLevel(line) ? createFirstLevel(line) : createSecondLevel(line);
            }

            private boolean isTopLevel(LoadTimeLine line) {
                return modelForTree.getPath(modelForTree.getRoot(), line).length == 0;
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
        List<LoadTimeLine> result = new ArrayList<>();
        Treechildren treeChildren = item.getTreechildren();

        if ( treeChildren != null ) {

            List<Treeitem> myTreeItems = treeChildren.getChildren();
            for (Treeitem child : myTreeItems) {

                if ( !child.isOpen() ) {
                    result.addAll(getLineChildrenBy(child));
                } else {
                    result.addAll(calculatedClosedItems(child));
                }
            }
        }

        return result;
    }

    private List<LoadTimeLine> getLineChildrenBy(Treeitem item) {
        List<LoadTimeLine> result = new ArrayList<>();
        LoadTimeLine line = getLineByTreeitem(item);

        if ( line != null ) {
            result.addAll(line.getAllChildren());
        }

        return result;
    }

    private LoadTimeLine getLineByTreeitem(Treeitem child) {
        LoadTimeLine line;

        try {
            line = child.getValue();
        } catch (Exception e) {
            return null;
        }

        return line;
    }

    private Tree getContainerTree() {
        return (Tree) getFellow("loadsTree");
    }

    private Component createFirstLevel(LoadTimeLine main) {
        Div result = createLabelWithName(main);
        result.setSclass("firstlevel");

        return result;
    }

    private Component createSecondLevel(LoadTimeLine loadTimeLine) {
        Div result = createLabelWithName(loadTimeLine);
        result.setSclass("secondlevel");

        return result;
    }

    private Div createLabelWithName(LoadTimeLine main) {
        Div result = new Div();
        Label label = new Label();
        final String conceptName = main.getConceptName();

        label.setValue(conceptName);
        result.appendChild(label);

        return result;
    }

    public void addSeeScheduledOfListener(ISeeScheduledOfListener seeScheduledOfListener) {
        scheduleListeners.addListener(seeScheduledOfListener);
    }

    /**
     * Info about onOpenEvent.
     */
    private class OnOpenEvent {

        private LoadTimeLine line;

        private Treeitem treeitem;

        private OpenEvent event;

        OnOpenEvent(Treeitem treeitem, LoadTimeLine line, OpenEvent event) {
            this.line = line;
            this.treeitem = treeitem;
            this.event = event;
        }
    }
}
