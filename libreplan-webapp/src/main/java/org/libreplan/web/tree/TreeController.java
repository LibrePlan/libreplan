/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.libreplan.business.orders.entities.SchedulingState;
import org.libreplan.business.orders.entities.SchedulingState.ITypeChangedListener;
import org.libreplan.business.orders.entities.SchedulingState.Type;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.business.trees.ITreeNode;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.Util.Getter;
import org.libreplan.web.common.Util.Setter;
import org.libreplan.web.orders.DynamicDatebox;
import org.libreplan.web.orders.SchedulingStateToggler;
import org.libreplan.web.tree.TreeComponent.Column;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.RendererCtrl;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.impl.api.InputElement;

/**
 * Tree controller for project WBS structures
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public abstract class TreeController<T extends ITreeNode<T>> extends
        GenericForwardComposer {

    private static final Log LOG = LogFactory.getLog(TreeController.class);

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    protected Tree tree;

    protected TreeViewStateSnapshot viewStateSnapshot;

    private final Class<T> type;

    public abstract Renderer getRenderer();

    protected TreeController(Class<T> type) {
        this.type = type;
    }

    public void indent() {
        if (tree.getSelectedCount() == 1) {
            indent(getSelectedNode());
        }
    }

    public void indent(T element) {
        viewStateSnapshot = TreeViewStateSnapshot.takeSnapshot(tree);
        getModel().indent(element);
        filterByPredicateIfAny();
    }

    public TreeModel getTreeModel() {
        return (getModel() != null) ? getModel().asTree() : null;
    }

    public void bindModelIfNeeded() {
        if (tree.getModel() != getTreeModel()) {
            tree.setModel(getTreeModel());
        }
    }

    protected abstract EntitiesTree<T> getModel();

    public void unindent() {
        if (tree.getSelectedCount() == 1) {
            unindent(getSelectedNode());
        }
    }

    public void unindent(T element) {
        viewStateSnapshot = TreeViewStateSnapshot.takeSnapshot(tree);
        getModel().unindent(element);
        filterByPredicateIfAny();
    }

    public void up() {
        viewStateSnapshot = TreeViewStateSnapshot.takeSnapshot(tree);
        if (tree.getSelectedCount() == 1) {
            up(getSelectedNode());
        }
    }

    public void up(T element) {
        viewStateSnapshot = TreeViewStateSnapshot.takeSnapshot(tree);
        getModel().up(element);
        filterByPredicateIfAny();
    }

    public void down() {
        if (tree.getSelectedCount() == 1) {
            down(getSelectedNode());
        }
    }

    public void down(T element) {
        viewStateSnapshot = TreeViewStateSnapshot.takeSnapshot(tree);
        getModel().down(element);
        filterByPredicateIfAny();
    }

    public T getSelectedNode() {
        Treeitem item = tree.getSelectedItem();
        if (item != null) {
            Object value = item.getValue();
            return value != null ? type.cast(value) : null;
        }
        return null;
    }

    public void move(Component dropedIn, Component dragged) {
        if ((isPredicateApplied())
                || (dropedIn.getUuid().equals(dragged.getUuid()))) {
            return;
        }

        viewStateSnapshot = TreeViewStateSnapshot.takeSnapshot(tree);

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
        viewStateSnapshot = TreeViewStateSnapshot.takeSnapshot(tree);
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

    public void addElement(Component cmp) {
        viewStateSnapshot = TreeViewStateSnapshot.takeSnapshot(tree);
        Textbox name = (Textbox) cmp.getFellow("newOrderElementName");
        Intbox hours = (Intbox) cmp.getFellow("newOrderElementHours");

        if (StringUtils.isEmpty(name.getValue())) {
            throw new WrongValueException(name, _("cannot be empty"));
        }

        if (hours.getValue() == null) {
            hours.setValue(0);
        }

        Textbox nameTextbox = null;

        // Parse hours
        try {
            if (tree.getSelectedCount() == 1) {
                T node = getSelectedNode();

                T newNode = getModel().addElementAt(node, name.getValue(),
                        hours.getValue());
                getRenderer().refreshHoursValueForThisNodeAndParents(newNode);
                getRenderer().refreshBudgetValueForThisNodeAndParents(newNode);

                if (node.isLeaf() && !node.isEmptyLeaf()) {
                    // Then a new container will be created
                    nameTextbox = getRenderer().getNameTextbox(node);
                }
            } else {
                getModel().addElement(name.getValue(), hours.getValue());
            }
            filterByPredicateIfAny();
        } catch (IllegalStateException e) {
            LOG.warn("exception ocurred adding element", e);
            messagesForUser.showMessage(Level.ERROR, e.getMessage());
        }

        name.setValue("");
        hours.setValue(0);

        if (nameTextbox != null) {
            nameTextbox.focus();
        } else {
            name.focus();
        }
    }

    protected abstract void filterByPredicateIfAny();

    protected static class TreeViewStateSnapshot {
        private final Set<Object> all;
        private final Set<Object> dataOpen;
        private final Object selected;

        private TreeViewStateSnapshot(Set<Object> dataOpen, Set<Object> all,
                Object selected) {
            this.dataOpen = dataOpen;
            this.all = all;
            this.selected = selected;
        }

        public static TreeViewStateSnapshot takeSnapshot(Tree tree) {
            Set<Object> dataOpen = new HashSet<Object>();
            Set<Object> all = new HashSet<Object>();
            Object selected = null;
            if (tree != null && tree.getTreechildrenApi() != null) {
                final Iterator<Treeitem> itemsIterator = tree
                        .getTreechildrenApi().getItems().iterator();
                while (itemsIterator.hasNext()) {
                    Treeitem treeitem = (Treeitem) itemsIterator.next();
                    Object value = getAssociatedValue(treeitem);
                    if (treeitem.isOpen()) {
                        dataOpen.add(value);
                    }
                    if (treeitem.isSelected()) {
                        selected = value;
                    }
                    all.add(value);
                }
            }
            return new TreeViewStateSnapshot(dataOpen, all, selected);
        }

        private static Object getAssociatedValue(Treeitem treeitem) {
            return treeitem.getValue();
        }

        public void restorePreviousViewState(Treeitem item) {
            Object value = getAssociatedValue(item);
            item.setOpen(isNewlyCreated(value) || wasOpened(value));
            item.setSelected(value == selected);
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

    public void remove(T element) {
        List<T> parentNodes = getModel().getParents(element);
        getModel().removeNode(element);
        getRenderer().refreshHoursValueForNodes(parentNodes);
        getRenderer().refreshBudgetValueForNodes(parentNodes);
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

    protected Button btnNew;

    private List<Column> columns;


    protected TreeViewStateSnapshot getSnapshotOfOpenedNodes() {
        return viewStateSnapshot;
    }

    private void resetControlButtons() {
        btnNew.setDisabled(isNewButtonDisabled());
    }

    protected abstract boolean isNewButtonDisabled();

    protected boolean isFirstLevelElement(Treeitem item) {
        return item.getLevel() == 0;
    }

    protected boolean isFirstItem(T element) {
        List children = element.getParent().getChildren();
        return (children.get(0).equals(element));
    }

    protected boolean isLastItem(T element) {
        List children = element.getParent().getChildren();
        return (children.get(children.size() - 1).equals(element));
    }

    private enum Navigation {
        LEFT, UP, RIGHT, DOWN;
        public static Navigation getIntentFrom(KeyEvent keyEvent) {
            return values()[keyEvent.getKeyCode() - 37];
        }
    }

    public abstract class Renderer implements TreeitemRenderer,
            RendererCtrl {

        private class KeyboardNavigationHandler {

            private Map<Treerow, List<InputElement>> navigableElementsByRow = new HashMap<Treerow, List<InputElement>>();

            void register(final InputElement inputElement) {
                inputElement.setCtrlKeys("#up#down");
                registerNavigableElement(inputElement);
                inputElement.addEventListener("onCtrlKey", new EventListener() {
                    private Treerow treerow = getCurrentTreeRow();

                    @Override
                    public void onEvent(Event event) {
                        Navigation navigation = Navigation
                                .getIntentFrom((KeyEvent) event);
                        moveFocusTo(inputElement, navigation, treerow);
                    }
                });
            }

            private void registerNavigableElement(InputElement inputElement) {
                Treerow treeRow = getCurrentTreeRow();
                if (!navigableElementsByRow.containsKey(treeRow)) {
                    navigableElementsByRow.put(treeRow,
                            new ArrayList<InputElement>());
                }
                navigableElementsByRow.get(treeRow).add(inputElement);
            }

            private void moveFocusTo(InputElement inputElement,
                    Navigation navigation, Treerow treerow) {
                List<InputElement> boxes = getNavigableElements(treerow);
                int position = boxes.indexOf(inputElement);
                if (position > boxes.size() - 1) {
                    return;
                }
                switch (navigation) {
                case UP:
                    focusGoUp(treerow, position);
                    break;
                case DOWN:
                    focusGoDown(treerow, position);
                    break;
                case LEFT:
                    if (position == 0) {
                        focusGoUp(treerow, boxes.size() - 1);
                    } else {
                        if (boxes.get(position - 1).isDisabled()) {
                            moveFocusTo(boxes.get(position - 1),
                                    Navigation.LEFT, treerow);
                        } else {
                            boxes.get(position - 1).focus();
                        }
                    }
                    break;
                case RIGHT:
                    if (position == boxes.size() - 1) {
                        focusGoDown(treerow, 0);
                    } else {
                        if (boxes.get(position + 1).isDisabled()) {
                            moveFocusTo(boxes.get(position + 1),
                                    Navigation.RIGHT, treerow);
                        } else {
                            boxes.get(position + 1).focus();
                        }
                    }
                    break;
                }
            }

            private void focusGoUp(Treerow treerow, int position) {
                Treeitem parent = (Treeitem) treerow.getParent();
                @SuppressWarnings("unchecked")
                List<Treeitem> treeItems = parent.getParent().getChildren();
                int myPosition = parent.indexOf();

                if (myPosition > 0) {
                    // the current node is not the first brother
                    Treechildren treechildren = treeItems.get(myPosition - 1)
                            .getTreechildren();
                    if (treechildren == null
                            || treechildren.getChildren().size() == 0) {
                        // the previous brother doesn't have children,
                        // or it has children but they are unloaded
                        Treerow upTreerow = treeItems.get(myPosition - 1)
                                .getTreerow();

                        focusCorrectBox(upTreerow, position, Navigation.LEFT);
                    } else {
                        // we have to move to the last child of the previous
                        // brother
                        Treerow upTreerow = findLastTreerow(treeItems
                                .get(myPosition - 1));

                        while (!upTreerow.isVisible()) {
                            upTreerow = ((Treeitem) upTreerow.getParent()
                                    .getParent().getParent()).getTreerow();
                        }

                        focusCorrectBox(upTreerow, position, Navigation.LEFT);
                    }
                } else {
                    // the node is the first brother
                    if (parent.getParent().getParent() instanceof Treeitem) {
                        // the node has a parent, so we move up to it
                        Treerow upTreerow = ((Treeitem) parent.getParent()
                                .getParent()).getTreerow();

                        focusCorrectBox(upTreerow, position, Navigation.LEFT);
                    }
                }
            }

            private Treerow findLastTreerow(Treeitem item) {
                if (item.getTreechildren() == null) {
                    return item.getTreerow();
                }
                @SuppressWarnings("unchecked")
                List<Treeitem> children = item.getTreechildren().getChildren();
                Treeitem lastchild = children.get(children.size() - 1);

                return findLastTreerow(lastchild);
            }

            private void focusGoDown(Treerow treerow, int position) {
                Treeitem parent = (Treeitem) treerow.getParent();
                focusGoDown(parent, position, false);
            }

            private void focusGoDown(Treeitem parent, int position,
                    boolean skipChildren) {
                if (parent.getTreechildren() == null || skipChildren) {
                    // Moving from a node to its brother
                    @SuppressWarnings("unchecked")
                    List<Treeitem> treeItems = parent.getParent().getChildren();
                    int myPosition = parent.indexOf();

                    if (myPosition < treeItems.size() - 1) {
                        // the current node is not the last one
                        Treerow downTreerow = treeItems.get(myPosition + 1)
                                .getTreerow();

                        focusCorrectBox(downTreerow, position, Navigation.RIGHT);
                    } else {
                        // the node is the last brother
                        if (parent.getParent().getParent() instanceof Treeitem) {
                            focusGoDown((Treeitem) parent.getParent()
                                    .getParent(), position, true);
                        }
                    }
                } else {
                    // Moving from a parent node to its children
                    Treechildren treechildren = parent.getTreechildren();

                    if (treechildren.getChildren().size() == 0) {
                        // the children are unloaded yet
                        focusGoDown(parent, position, true);
                        return;
                    }
                    Treerow downTreerow = ((Treeitem) treechildren
                            .getChildren().get(0)).getTreerow();

                    if (!downTreerow.isVisible()) {
                        // children are loaded but not visible
                        focusGoDown(parent, position, true);
                        return;
                    }

                    focusCorrectBox(downTreerow, position, Navigation.RIGHT);
                }
            }

            private void focusCorrectBox(Treerow treerow, int position,
                    Navigation whereIfDisabled) {
                List<InputElement> boxes = getNavigableElements(treerow);
                if (position < boxes.size() - 1) {
                    if (boxes.get(position).isDisabled()) {
                        moveFocusTo(boxes.get(position), whereIfDisabled,
                                treerow);
                    } else {
                        boxes.get(position).focus();
                    }
                }
            }

            private List<InputElement> getNavigableElements(Treerow row) {
                if (!navigableElementsByRow.containsKey(row)) {
                    return Collections.emptyList();
                }
                return Collections.unmodifiableList(navigableElementsByRow
                        .get(row));
            }

        }

        private Map<T, Textbox> nameTextboxByElement = new HashMap<T, Textbox>();

        private Map<T, Intbox> hoursIntBoxByElement = new HashMap<T, Intbox>();

        private Map<T, Decimalbox> budgetDecimalboxByElement = new HashMap<T, Decimalbox>();

        private KeyboardNavigationHandler navigationHandler = new KeyboardNavigationHandler();

        private Treerow currentTreeRow;

        public Treerow getCurrentTreeRow() {
            return currentTreeRow;
        }

        public Renderer() {
        }

        protected Textbox getNameTextbox(T key) {
            return nameTextboxByElement.get(key);
        }

        protected void putNameTextbox(T key, Textbox textbox) {
            nameTextboxByElement.put(key, textbox);
        }

        protected void registerFocusEvent(final InputElement inputElement) {
            inputElement.addEventListener(Events.ON_FOCUS,
                    new EventListener() {

                private Treeitem item = (Treeitem) getCurrentTreeRow().getParent();

                @Override
                public void onEvent(Event event) {
                    item.setSelected(true);
                    Util.reloadBindings(item.getParent());
                }
            });
        }

        protected void addDateCell(final DynamicDatebox dinamicDatebox,
                final String dateboxName) {

            Component cell = Executions.getCurrent().createComponents(
                    "/common/components/dynamicDatebox.zul", null, null);
            try {
                dinamicDatebox.doAfterCompose(cell);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            addCell(cell);
            registerListeners(dinamicDatebox.getDateTextBox());
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
                if (component instanceof InputElement) {
                    registerListeners((InputElement) component);
                }
            }
            currentTreeRow.appendChild(cell);
            return cell;
        }

        private void registerListeners(InputElement associatedInput) {
            registerFocusEvent(associatedInput);
            navigationHandler.register(associatedInput);
        }

        @Override
        public void render(final Treeitem item, Object data) {
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
            if (viewStateSnapshot != null) {
                viewStateSnapshot.restorePreviousViewState(item);
            }
        }

        private Treerow getTreeRowWithoutChildrenFor(final Treeitem item) {
            Treerow result = createOrRetrieveFor(item);
            // Attach treecells to treerow
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
                public void onEvent(org.zkoss.zk.ui.event.Event event) {
                    DropEvent dropEvent = (DropEvent) event;
                    move((Component) dropEvent.getTarget(),
                            (Component) dropEvent.getDragged().getParent());
                }
            });
        }

        public void addSchedulingStateCell(final T currentElement) {
            final SchedulingState schedulingState = getSchedulingStateFrom(currentElement);
            SchedulingStateToggler schedulingStateToggler = new SchedulingStateToggler(
                    schedulingState);
            schedulingStateToggler.setReadOnly(readOnly);
            final Treecell cell = addCell(
                    getDecorationFromState(getSchedulingStateFrom(currentElement)),
                    schedulingStateToggler);
            cell.addEventListener("onDoubleClick", new EventListener() {
                @Override
                public void onEvent(Event event) {

                    markModifiedTreeitem((Treerow) cell.getParent());
                    onDoubleClickForSchedulingStateCell(currentElement);
                }
            });
            cell.addEventListener(Events.ON_CLICK, new EventListener() {

                private Treeitem item = (Treeitem) getCurrentTreeRow().getParent();

                @Override
                public void onEvent(Event event) {
                    item.getTree().toggleItemSelection(item);
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
            cell.setDraggable("true");
        }

        protected abstract SchedulingState getSchedulingStateFrom(
                T currentElement);

        private String getDecorationFromState(SchedulingState state) {
            return state.getCssClass();
        }

        protected abstract void addCodeCell(final T element);

        protected abstract void addDescriptionCell(final T element);

        public void addBudgetCell(final T currentElement) {
            Decimalbox decimalboxBudget = buildBudgetDecimalboxFor(currentElement);
            budgetDecimalboxByElement.put(currentElement, decimalboxBudget);
            if (readOnly) {
                decimalboxBudget.setDisabled(true);
            }
            addCell(decimalboxBudget);
        }

        private Decimalbox buildBudgetDecimalboxFor(final T element) {
            Decimalbox result = new DecimalboxDirectValue();
            if (element.isLeaf()) {
                Util.bind(result, getBudgetGetterFor(element),
                        getBudgetSetterFor(element));
                result.setConstraint(getBudgetConstraintFor(element));
            } else {
                // If it's a container budget cell is not editable
                Util.bind(result, getBudgetGetterFor(element));
            }
            result.setFormat(Util.getMoneyFormat());
            return result;
        }

        private Getter<BigDecimal> getBudgetGetterFor(final T element) {
            return new Util.Getter<BigDecimal>() {
                @Override
                public BigDecimal get() {
                    return getBudgetHandler().getBudgetFor(element);
                }
            };
        }

        private Setter<BigDecimal> getBudgetSetterFor(final T element) {
            return new Util.Setter<BigDecimal>() {
                @Override
                public void set(BigDecimal value) {
                    getBudgetHandler().setBudgetHours(element, value);
                    List<T> parentNodes = getModel().getParents(element);
                    // Remove the last element because it's an
                    // Order node, not an OrderElement
                    parentNodes.remove(parentNodes.size() - 1);
                    for (T node : parentNodes) {
                        DecimalboxDirectValue decimalbox = (DecimalboxDirectValue) budgetDecimalboxByElement
                                .get(node);
                        BigDecimal budget = getBudgetHandler().getBudgetFor(
                                node);
                        if (isInCurrentPage(decimalbox)) {
                            decimalbox.setValue(budget);
                        } else {
                            decimalbox.setValueDirectly(budget);
                        }
                    }
                }

                private boolean isInCurrentPage(DecimalboxDirectValue intbox) {
                    Treeitem treeItem = (Treeitem) intbox.getParent()
                            .getParent().getParent();
                    List<Treeitem> treeItems = new ArrayList<Treeitem>(
                            tree.getItems());
                    int position = treeItems.indexOf(treeItem);

                    if (position < 0) {
                        throw new RuntimeException("Treeitem " + treeItem
                                + " has to belong to tree.getItems() list");
                    }

                    return (position / tree.getPageSize()) == tree
                            .getActivePage();
                }
            };
        }

        public void updateBudgetFor(T element) {
            if (!readOnly && element.isLeaf()) {
                Decimalbox decimalbox = budgetDecimalboxByElement.get(element);
                decimalbox.invalidate();
                refreshBudgetValueForThisNodeAndParents(element);
            }
        }

        public void updateNameFor(T element) {
            if (!readOnly) {
                Textbox textbox = nameTextboxByElement.get(element);
                textbox.setValue(getNameHandler().getNameFor(element));
            }
        }

        public void refreshBudgetValueForThisNodeAndParents(T node) {
            List<T> nodeAndItsParents = getModel().getParents(node);
            nodeAndItsParents.add(node);
            refreshBudgetValueForNodes(nodeAndItsParents);
        }

        public void refreshBudgetValueForNodes(List<T> nodes) {
            for (T node : nodes) {
                Decimalbox decimalbox = budgetDecimalboxByElement.get(node);
                // For the Order node there is no associated decimalbox
                if (decimalbox != null) {
                    BigDecimal currentBudget = getBudgetHandler().getBudgetFor(node);
                    decimalbox.setValue(currentBudget);
                }
            }
        }

        private Constraint getBudgetConstraintFor(final T line) {
            return new Constraint() {
                @Override
                public void validate(Component comp, Object value)
                        throws WrongValueException {
                    if (((BigDecimal) value).compareTo(BigDecimal.ZERO) < 0) {
                        throw new WrongValueException(comp,
                                _("Budget value cannot be negative"));
                    }
                }

            };
        }

        public void addHoursCell(final T currentElement) {
            Intbox intboxHours = buildHoursIntboxFor(currentElement);
            hoursIntBoxByElement.put(currentElement, intboxHours);
            if (readOnly) {
                intboxHours.setDisabled(true);
            }
            Treecell cellHours = addCell(intboxHours);
            setReadOnlyHoursCell(currentElement, intboxHours, cellHours);
        }

        private void setReadOnlyHoursCell(T element,
                Intbox boxHours, Treecell tc) {
            if (!readOnly && element.isLeaf()) {
                if (getHoursGroupHandler().hasMoreThanOneHoursGroup(element)) {
                    boxHours.setReadonly(true);
                    tc.setTooltiptext(_("Not editable for containing more that an hours group."));
                } else {
                    boxHours.setReadonly(false);
                    tc.setTooltiptext("");
                }
            }
        }

        private Intbox buildHoursIntboxFor(final T element) {
            Intbox result = new IntboxDirectValue();
            if (element.isLeaf()) {
                Util.bind(result, getHoursGetterFor(element),
                        getHoursSetterFor(element));
                result.setConstraint(getHoursConstraintFor(element));
            } else {
                // If it's a container hours cell is not editable
                Util.bind(result, getHoursGetterFor(element));
            }
            return result;
        }

        private Getter<Integer> getHoursGetterFor(final T element) {
            return new Util.Getter<Integer>() {
                @Override
                public Integer get() {
                    return getHoursGroupHandler().getWorkHoursFor(element);
                }
            };
        }

        private Setter<Integer> getHoursSetterFor(final T element) {
            return new Util.Setter<Integer>() {
                @Override
                public void set(Integer value) {
                    getHoursGroupHandler().setWorkHours(element, value);
                    List<T> parentNodes = getModel().getParents(element);
                    // Remove the last element because it's an
                    // Order node, not an OrderElement
                    parentNodes.remove(parentNodes.size() - 1);
                    for (T node : parentNodes) {
                        IntboxDirectValue intbox = (IntboxDirectValue) hoursIntBoxByElement
                                .get(node);
                        Integer hours = getHoursGroupHandler()
                                .getWorkHoursFor(node);
                        if (isInCurrentPage(intbox)) {
                            intbox.setValue(hours);
                        } else {
                            intbox.setValueDirectly(hours);
                        }
                    }
                }

                private boolean isInCurrentPage(IntboxDirectValue intbox) {
                    Treeitem treeItem = (Treeitem) intbox.getParent().getParent().getParent();
                    List<Treeitem> treeItems = new ArrayList<Treeitem>(
                            tree.getItems());
                    int position = treeItems.indexOf(treeItem);

                    if (position < 0) {
                        throw new RuntimeException("Treeitem " + treeItem
                                + " has to belong to tree.getItems() list");
                    }

                    return (position / tree.getPageSize()) == tree
                            .getActivePage();
                }
            };
        }

        public void updateHoursFor(T element) {
            if (!readOnly && element.isLeaf()) {
                Intbox boxHours = (Intbox) hoursIntBoxByElement.get(element);
                Treecell tc = (Treecell) boxHours.getParent();
                setReadOnlyHoursCell(element, boxHours, tc);
                boxHours.invalidate();
                refreshHoursValueForThisNodeAndParents(element);
            }
        }

        public void refreshHoursValueForThisNodeAndParents(T node) {
            List<T> nodeAndItsParents = getModel().getParents(node);
            nodeAndItsParents.add(node);
            refreshHoursValueForNodes(nodeAndItsParents);
        }

        public void refreshHoursValueForNodes(List<T> nodes) {
            for (T node : nodes) {
                Intbox intbox = hoursIntBoxByElement.get(node);
                // For the Order node there is no associated intbox
                if (intbox != null) {
                    Integer currentHours = getHoursGroupHandler()
                            .getWorkHoursFor(node);
                    intbox.setValue(currentHours);
                }
            }
        }

        private Constraint getHoursConstraintFor(final T line) {
            return new Constraint() {
                @Override
                public void validate(Component comp, Object value)
                        throws WrongValueException {
                    if (!getHoursGroupHandler().isTotalHoursValid(line, ((Integer) value))) {
                        throw new WrongValueException(
                                comp,
                                _("Value is not valid, taking into account the current list of HoursGroup"));
                    }
                }

            };
        }

        protected abstract void addOperationsCell(final Treeitem item,
                final T currentElement);

        protected abstract void onDoubleClickForSchedulingStateCell(
                T currentElement);

        protected Button createDownButton(final Treeitem item,
                final T currentElement) {
            EventListener downButtonListener = new EventListener() {
                @Override
                public void onEvent(Event event) {
                    down(currentElement);
                }
            };
            Button result;
            if (isPredicateApplied() || isLastItem(currentElement) || readOnly) {
                result = createButton("/common/img/ico_bajar_out.png", "",
                        "/common/img/ico_bajar_out.png", "icono",
                        downButtonListener);
                result.setDisabled(true);
            } else {
                result = createButton("/common/img/ico_bajar1.png",
                        _("Move down"), "/common/img/ico_bajar.png", "icono",
                        downButtonListener);
            }
            return result;
        }

        protected Button createUpButton(final Treeitem item, final T element) {
            EventListener upButtonListener = new EventListener() {
                @Override
                public void onEvent(Event event) {
                    up(element);
                }
            };
            Button result;
            if (isPredicateApplied() || isFirstItem(element) || readOnly) {
                result = createButton("/common/img/ico_subir_out.png", "",
                        "/common/img/ico_subir_out.png", "icono",
                        upButtonListener);
                result.setDisabled(true);
            } else {
                result = createButton("/common/img/ico_subir1.png",
                        _("Move up"), "/common/img/ico_subir.png", "icono",
                        upButtonListener);
            }
            return result;
        }

        protected Button createUnindentButton(final Treeitem item,
                final T element) {
            EventListener unindentListener = new EventListener() {
                @Override
                public void onEvent(Event event) {
                    unindent(element);
                }
            };
            final Button result;
            if (isPredicateApplied() || isFirstLevelElement(item) || readOnly) {
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
                public void onEvent(Event event) {
                    indent(element);
                }
            };
            final Button result;
            if (isPredicateApplied() || isFirstItem(element) || readOnly) {
                result = createButton("/common/img/ico_derecha_out.png", "",
                        "/common/img/ico_derecha_out.png", "icono",
                        indentListener);
                result.setDisabled(true);
            } else {
                result = createButton("/common/img/ico_derecha1.png",
                        _("Indent"), "/common/img/ico_derecha.png", "icono",
                        indentListener);
            }
            return result;
        }

        protected Button createRemoveButton(final T currentElement) {
            EventListener removeListener = new EventListener() {
                @Override
                public void onEvent(Event event) {
                    remove(currentElement);
                    filterByPredicateIfAny();
                }
            };
            final Button result;
            if(readOnly) {
                result = createButton("/common/img/ico_borrar_out.png",
                        _("Delete"), "/common/img/ico_borrar_out.png", "icono",
                        removeListener);
                result.setDisabled(readOnly);
            }
            else {
                result = createButton("/common/img/ico_borrar1.png",
                        _("Delete"), "/common/img/ico_borrar.png", "icono",
                        removeListener);
            }
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
        public void doCatch(Throwable ex) {

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

    public interface IHoursGroupHandler<T> {

        boolean hasMoreThanOneHoursGroup(T element);

        boolean isTotalHoursValid(T line, Integer value);

        Integer getWorkHoursFor(T element);

        void setWorkHours(T element, Integer value);
    }

    protected abstract IHoursGroupHandler<T> getHoursGroupHandler();

    public interface IBudgetHandler<T> {

        BigDecimal getBudgetFor(T element);

        void setBudgetHours(T element, BigDecimal budget);
    }

    protected abstract IBudgetHandler<T> getBudgetHandler();

    public interface INameHandler<T> {

        String getNameFor(T element);

    }

    protected abstract INameHandler<T> getNameHandler();

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
        btnNew.setDisabled(false);
    }

    protected abstract boolean isPredicateApplied();

    protected abstract String createTooltipText(T currentElement);

    protected Set<Treecell> cellsMarkedAsModified = new HashSet<Treecell>();

    public void markModifiedTreeitem(Treerow item) {
        Treecell tc = (Treecell) item.getFirstChild();
        // Check if marked label has been previously added
        if (!(tc.getLastChild() instanceof org.zkoss.zul.Label)) {
            org.zkoss.zul.Label modifiedMark = new org.zkoss.zul.Label("*");
            modifiedMark.setTooltiptext(_("Modified"));
            modifiedMark.setSclass("modified-mark");
            tc.appendChild(modifiedMark);
            cellsMarkedAsModified.add(tc);
        }
    }

    public void resetCellsMarkedAsModified() {
        for(Treecell cell : cellsMarkedAsModified) {
            cell.removeChild(cell.getLastChild());
        }
        cellsMarkedAsModified.clear();
    }

    protected boolean readOnly = true;

    public void setReadOnly(boolean readOnly) {
        if(this.readOnly != readOnly) {
            this.readOnly = readOnly;
            ((Button)orderElementTreeComponent.getFellowIfAny("btnNew")).setDisabled(readOnly);
            ((Button)orderElementTreeComponent.getFellowIfAny("btnNewFromTemplate")).setDisabled(readOnly);
            ((Textbox)orderElementTreeComponent.getFellowIfAny("newOrderElementName")).setDisabled(readOnly);
            ((Intbox)orderElementTreeComponent.getFellowIfAny("newOrderElementHours")).setDisabled(readOnly);
            Util.reloadBindings(orderElementTreeComponent);
        }
    }

    protected TreeComponent orderElementTreeComponent;

    public void setTreeComponent(TreeComponent orderElementsTree) {
        this.orderElementTreeComponent = orderElementsTree;
    }

    /**
     * This class is to give visibility to method
     * {@link Intbox#setValueDirectly} which is marked as protected in
     * {@link Intbox} class.
     *
     * <br />
     *
     * This is needed to prevent calling {@link AbstractComponent#smartUpdate}
     * when the {@link Intbox} is not in current page. <tt>smartUpdate</tt> is
     * called by {@link Intbox#setValue(Integer)}. This call causes a JavaScript
     * error when trying to update {@link Intbox} that are not in current page
     * in the tree.
     */
    private class IntboxDirectValue extends Intbox {
        @Override
        public void setValueDirectly(Object value) {
            super.setValueDirectly(value);
        }
    }

    /**
     * This class is to give visibility to method
     * {@link Decimalbox#setValueDirectly} which is marked as protected in
     * {@link Decimalbox} class.
     *
     * <br />
     *
     * This is needed to prevent calling {@link AbstractComponent#smartUpdate}
     * when the {@link Decimalbox} is not in current page. <tt>smartUpdate</tt>
     * is called by {@link Decimalbox#setValue(Integer)}. This call causes a
     * JavaScript error when trying to update {@link Decimalbox} that are not in
     * current page in the tree.
     */
    private class DecimalboxDirectValue extends Decimalbox {
        @Override
        public void setValueDirectly(Object value) {
            super.setValueDirectly(value);
        }
    }

}
