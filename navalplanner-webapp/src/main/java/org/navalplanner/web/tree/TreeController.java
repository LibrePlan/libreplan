/*
 * This file is part of ###PROJECT_NAME###
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
package org.navalplanner.web.tree;

import static org.navalplanner.web.I18nHelper._;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.orders.entities.SchedulingState;
import org.navalplanner.business.orders.entities.SchedulingState.ITypeChangedListener;
import org.navalplanner.business.orders.entities.SchedulingState.Type;
import org.navalplanner.business.templates.entities.OrderElementTemplate;
import org.navalplanner.business.trees.ITreeNode;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.orders.SchedulingStateToggler;
import org.navalplanner.web.tree.TreeComponent.Column;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.RendererCtrl;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

public abstract class TreeController<T extends ITreeNode<T>> extends
        GenericForwardComposer {

    private static final Log LOG = LogFactory.getLog(TreeController.class);

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    protected Tree tree;

    protected TreeViewStateSnapshot snapshotOfOpenedNodes;

    private final Class<T> type;

    public abstract TreeitemRenderer getRenderer();

    protected TreeController(Class<T> type) {
        this.type = type;
    }

    public void indent() {
        if (tree.getSelectedCount() == 1) {
            indent(getSelectedNode());
        }
    }

    protected void indent(T element) {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        getModel().indent(element);
        filterByPredicateIfAny();
    }

    public TreeModel getTreeModel() {
        return (getModel() != null) ? getModel().asTree() : null;
    }

    protected abstract EntitiesTree<T> getModel();

    public void unindent() {
        if (tree.getSelectedCount() == 1) {
            unindent(getSelectedNode());
        }
    }

    protected void unindent(T element) {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        getModel().unindent(element);
        filterByPredicateIfAny();
    }

    public void up() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        if (tree.getSelectedCount() == 1) {
            up(getSelectedNode());
        }
    }

    public void up(T element) {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        getModel().up(element);
        filterByPredicateIfAny();
    }

    public void down() {
        if (tree.getSelectedCount() == 1) {
            down(getSelectedNode());
        }
    }

    public void down(T element) {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        getModel().down(element);
        filterByPredicateIfAny();
    }

    protected T getSelectedNode() {
        return type.cast(tree.getSelectedItemApi().getValue());
    }

    public void move(Component dropedIn, Component dragged) {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);

        Treerow from = (Treerow) dragged;
        T fromNode = type.cast(((Treeitem) from.getParent()).getValue());
        if (dropedIn instanceof Tree) {
            getModel().moveToRoot(fromNode);
        }
        if (dropedIn instanceof Treerow) {
            Treerow to = (Treerow) dropedIn;
            T toNode = type.cast(((Treeitem) to.getParent()).getValue());

            getModel().move(fromNode, toNode);
        }
        filterByPredicateIfAny();
    }

    public void addElement() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        try {
            if (tree.getSelectedCount() == 1) {
                getModel().addElementAt(getSelectedNode());
            } else {
                getModel().addElement();
            }
            filterByPredicateIfAny();
        } catch (IllegalStateException e) {
            LOG.warn("exception ocurred adding element", e);
            messagesForUser.showMessage(Level.ERROR, e.getMessage());
        }
    }

    protected abstract void filterByPredicateIfAny();

    protected static class TreeViewStateSnapshot {
        private final Set<Object> all;
        private final Set<Object> dataOpen;

        private TreeViewStateSnapshot(Set<Object> dataOpen, Set<Object> all) {
            this.dataOpen = dataOpen;
            this.all = all;
        }

        public static TreeViewStateSnapshot snapshotOpened(Tree tree) {
            Iterator<Treeitem> itemsIterator = tree.getTreechildrenApi()
                    .getItems().iterator();
            Set<Object> dataOpen = new HashSet<Object>();
            Set<Object> all = new HashSet<Object>();
            while (itemsIterator.hasNext()) {
                Treeitem treeitem = (Treeitem) itemsIterator.next();
                Object value = getAssociatedValue(treeitem);
                if (treeitem.isOpen()) {
                    dataOpen.add(value);
                }
                all.add(value);
            }
            return new TreeViewStateSnapshot(dataOpen, all);
        }

        private static Object getAssociatedValue(Treeitem treeitem) {
            return treeitem.getValue();
        }

        public void openIfRequired(Treeitem item) {
            Object value = getAssociatedValue(item);
            item.setOpen(isNewlyCreated(value) || wasOpened(value));
        }

        private boolean wasOpened(Object value) {
            return dataOpen.contains(value);
        }

        private boolean isNewlyCreated(Object value) {
            return !all.contains(value);
        }
    }

    public void removeElement() {
        Set<Treeitem> selectedItems = tree.getSelectedItems();
        for (Treeitem treeItem : selectedItems) {
            remove(type.cast(treeItem.getValue()));
        }
        filterByPredicateIfAny();
    }

    protected void remove(T element) {
        getModel().removeNode(element);
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
    }

    public boolean isItemSelected() {
        return tree.getSelectedItem() != null;
    }

    public boolean isNotItemSelected() {
        return !isItemSelected();
    }

    protected Button btnNew, btnDown, btnUp, btnUnindent, btnIndent, btnDelete;

    private List<Column> columns;


    protected TreeViewStateSnapshot getSnapshotOfOpenedNodes() {
        return snapshotOfOpenedNodes;
    }

    private void resetControlButtons() {
        final boolean disabled = tree.getSelectedItem() == null;
        btnNew.setDisabled(isNewButtonDisabled());
        btnIndent.setDisabled(disabled);
        btnUnindent.setDisabled(disabled);
        btnUp.setDisabled(disabled);
        btnDown.setDisabled(disabled);
        btnDelete.setDisabled(disabled);
    }

    protected abstract boolean isNewButtonDisabled();

    protected boolean isFirstLevelElement(Treeitem item) {
        return item.getLevel() == 0;
    }

    protected boolean isSecondLevelElement(Treeitem item) {
        return item.getLevel() == 1;
    }

    public abstract class Renderer implements TreeitemRenderer,
            RendererCtrl {

        private Treerow currentTreeRow;

        public Renderer() {
        }

        protected Treecell addCell(Component... components) {
            return addCell(null, components);
        }

        protected Treecell addCell(String cssClass, Component... components) {
            Treecell cell = new Treecell();
            if (cssClass != null) {
                cell.setSclass(cssClass);
            }
            for (Component component : components) {
                cell.appendChild(component);
            }
            currentTreeRow.appendChild(cell);
            return cell;
        }

        @Override
        public void render(final Treeitem item, Object data) throws Exception {
            item.setValue(data);
            applySnapshot(item);
            currentTreeRow = getTreeRowWithoutChildrenFor(item);
            final T currentElement = type.cast(data);
            createCells(item, currentElement);
            onDropMoveFromDraggedToTarget();
        }

        protected void checkInvalidValues(
                ClassValidator<OrderElementTemplate> validator,
                String property, Integer value, final Intbox component) {
            InvalidValue[] invalidValues = validator.getPotentialInvalidValues(
                    property, value);
            if (invalidValues.length > 0) {
                throw new WrongValueException(component, invalidValues[0]
                        .getMessage());
            }
        }

        private void createCells(Treeitem item, T currentElement) {
            for (Column each : columns) {
                each.doCell(this, item, currentElement);
            }
            item.setTooltiptext(createTooltipText(currentElement));
        }

        private void applySnapshot(final Treeitem item) {
            if (snapshotOfOpenedNodes != null) {
                snapshotOfOpenedNodes.openIfRequired(item);
            }
        }

        private Treerow getTreeRowWithoutChildrenFor(final Treeitem item) {
            Treerow result = createOrRetrieveFor(item);
            // Attach treecells to treerow
            result.setDraggable("true");
            result.setDroppable("true");
            result.getChildren().clear();
            return result;
        }

        protected String pathAsString(int[] path) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < path.length; i++) {
                if (i != 0) {
                    result.append(".");
                }
                result.append(path[i] + 1);
            }
            return result.toString();
        }

        private Treerow createOrRetrieveFor(final Treeitem item) {
            if (item.getTreerow() == null) {
                Treerow result = new Treerow();
                result.setParent(item);
                return result;
            } else {
                return item.getTreerow();
            }
        }

        private void onDropMoveFromDraggedToTarget() {
            currentTreeRow.addEventListener("onDrop", new EventListener() {
                @Override
                public void onEvent(org.zkoss.zk.ui.event.Event event)
                        throws Exception {
                    DropEvent dropEvent = (DropEvent) event;
                    move((Component) dropEvent.getTarget(),
                            (Component) dropEvent.getDragged());
                }
            });
        }

        public void addSchedulingStateCell(final T currentElement) {
            final SchedulingState schedulingState = getSchedulingStateFrom(currentElement);
            SchedulingStateToggler schedulingStateToggler = new SchedulingStateToggler(
                    schedulingState);
            final Treecell cell = addCell(
                    getDecorationFromState(getSchedulingStateFrom(currentElement)),
                    schedulingStateToggler);
            cell.addEventListener("onDoubleClick", new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    onDoubleClickForSchedulingStateCell(currentElement);
                }
            });
            schedulingState.addTypeChangeListener(
                    new ITypeChangedListener() {

                        @Override
                        public void typeChanged(Type newType) {
                            cell.setSclass(getDecorationFromState(schedulingState));
                        }
                    });
            schedulingStateToggler.afterCompose();
        }

        protected abstract SchedulingState getSchedulingStateFrom(
                T currentElement);

        private String getDecorationFromState(SchedulingState state) {
            String cssclass = "not-scheduled";
            if (state.isCompletelyScheduled()) {
                cssclass = "completely-scheduled";
            } else if (state.isPartiallyScheduled()) {
                cssclass = "partially-scheduled";
            }
            return cssclass;
        }

        protected abstract void addCodeCell(final T element);

        protected abstract void addDescriptionCell(final T element);

        protected abstract void addOperationsCell(final Treeitem item,
                final T currentElement);

        protected abstract void onDoubleClickForSchedulingStateCell(
                T currentElement);

        protected Button createUpButton(final Treeitem item,
                final T currentElement) {
            EventListener upButtonListener = new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    down(currentElement);
                }
            };
            Button result;
            if (isFirstLevelElement(item) && isPredicateApplied()) {
                result = createButton("/common/img/ico_bajar_out.png", "",
                        "/common/img/ico_bajar_out.png", "icono",
                        upButtonListener);
                result.setDisabled(true);
            } else {
                result = createButton("/common/img/ico_bajar1.png",
                        _("Move down"), "/common/img/ico_bajar.png", "icono",
                        upButtonListener);
            }
            return result;
        }

        protected Button createDownButton(final Treeitem item, final T element) {
            EventListener downButtonListener = new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    up(element);
                }
            };
            Button result;
            if (isFirstLevelElement(item) && isPredicateApplied()) {
                result = createButton("/common/img/ico_subir_out.png", "",
                        "/common/img/ico_subir_out.png", "icono",
                        downButtonListener);
                result.setDisabled(true);
            } else {
                result = createButton("/common/img/ico_subir1.png",
                        _("Move up"), "/common/img/ico_subir.png", "icono",
                        downButtonListener);
            }
            return result;
        }

        protected Button createUnindentButton(final Treeitem item,
                final T element) {
            EventListener unindentListener = new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    unindent(element);
                }
            };
            final Button result;
            if ((isFirstLevelElement(item) || isSecondLevelElement(item))
                    && isPredicateApplied()) {
                result = createButton("/common/img/ico_izq_out.png", "",
                        "/common/img/ico_izq_out.png", "icono",
                        unindentListener);
                result.setDisabled(true);
            } else {
                result = createButton("/common/img/ico_izq1.png",
                        _("Unindent"), "/common/img/ico_izq.png", "icono",
                        unindentListener);
            }
            return result;
        }

        protected Button createIndentButton(final Treeitem item, final T element) {
            EventListener indentListener = new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    indent(element);
                }
            };
            final Button result;
            if (isFirstLevelElement(item) && isPredicateApplied()) {
                result = createButton("/common/img/ico_derecha_out.png", "",
                        "/common/img/ico_derecha_out.png", "icono",
                        indentListener);
            } else {
                result = createButton("/common/img/ico_derecha1.png",
                        _("Indent"), "/common/img/ico_derecha.png", "icono",
                        indentListener);
            }
            return result;
        }

        protected Button createRemoveButton(final T currentElement) {
            final Button result = createButton("/common/img/ico_borrar1.png",
                    _("Delete"), "/common/img/ico_borrar.png", "icono",
                    new EventListener() {
                        @Override
                        public void onEvent(Event event) throws Exception {
                            remove(currentElement);
                            filterByPredicateIfAny();
                        }
                    });
            return result;
        }

        protected Button createButton(String image, String tooltip,
                String hoverImage, String styleClass,
                EventListener eventListener) {
            Button result = new Button("", image);
            result.setHoverImage(hoverImage);
            result.setSclass(styleClass);
            result.setTooltiptext(tooltip);
            result.addEventListener(Events.ON_CLICK, eventListener);
            return result;
        }

        @Override
        public void doCatch(Throwable ex) throws Throwable {

        }

        @Override
        public void doFinally() {
            resetControlButtons();
        }

        @Override
        public void doTry() {

        }
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    /**
     * Disable control buttons (new, up, down, indent, unindent, delete)
     */
    public void updateControlButtons(Event event) {
        updateControlButtons((Tree) event.getTarget());
    }

    public void updateControlButtons(Tree tree) {
        final Treeitem item = tree.getSelectedItem();
        if (item == null) {
            resetControlButtons();
            return;
        }
        boolean disabledLevel1 = isNewButtonDisabled()
                && isFirstLevelElement(item);
        boolean disabledLevel2 = isNewButtonDisabled()
                && (isFirstLevelElement(item) || isSecondLevelElement(item));

        btnNew.setDisabled(false);
        btnDown.setDisabled(disabledLevel1);
        btnUp.setDisabled(disabledLevel1);
        btnUnindent.setDisabled(disabledLevel2);
        btnIndent.setDisabled(disabledLevel1);
        btnDelete.setDisabled(false);
    }

    protected abstract boolean isPredicateApplied();

    protected abstract String createTooltipText(T currentElement);

}
