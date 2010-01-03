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

package org.navalplanner.web.orders;

import static org.navalplanner.web.I18nHelper._;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.SchedulingState;
import org.navalplanner.business.orders.entities.SchedulingState.ITypeChangedListener;
import org.navalplanner.business.orders.entities.SchedulingState.Type;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.Util.Getter;
import org.navalplanner.web.common.Util.Setter;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.navalplanner.web.templates.IOrderTemplatesControllerEntryPoints;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.RendererCtrl;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

/**
 * Controller for {@link OrderElement} tree view of {@link Order} entities <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class OrderElementTreeController extends GenericForwardComposer {

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private Combobox cbFilterType;

    private BandboxSearch bdFilter;

    private Tree tree;

    private OrderElementTreeitemRenderer renderer = new OrderElementTreeitemRenderer();

    private TreeViewStateSnapshot snapshotOfOpenedNodes;

    private final IOrderModel orderModel;

    private final OrderElementController orderElementController;

    private transient IPredicate predicate;

    @Resource
    private IOrderTemplatesControllerEntryPoints orderTemplates;

    public List<org.navalplanner.business.labels.entities.Label> getLabels() {
        return orderModel.getLabels();
    }

    public OrderElementTreeitemRenderer getRenderer() {
        return renderer;
    }

    public OrderElementTreeController(IOrderModel orderModel,
            OrderElementController orderElementController) {
        this.orderModel = orderModel;
        this.orderElementController = orderElementController;
    }

    public void indent() {
        if (tree.getSelectedCount() == 1) {
            indent(getSelectedNode());
        }
    }

    private void indent(OrderElement orderElement) {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        getModel().indent(orderElement);
        filterByPredicateIfAny();
    }

    public TreeModel getOrderElementTreeModel() {
        return (getModel() != null) ? getModel().asTree() : null;
    }

    private OrderElementTreeModel getModel() {
        return orderModel.getOrderElementTreeModel();
    }

    public void unindent() {
        if (tree.getSelectedCount() == 1) {
            unindent(getSelectedNode());
        }
    }

    private void unindent(OrderElement orderElement) {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        getModel().unindent(orderElement);
        filterByPredicateIfAny();
    }

    public void up() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        if (tree.getSelectedCount() == 1) {
            up(getSelectedNode());
        }
    }

    public void up(OrderElement orderElement) {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        getModel().up(orderElement);
        filterByPredicateIfAny();
    }

    public void createTemplate() {
        if (tree.getSelectedCount() == 1) {
            createTemplate(getSelectedNode());
        }
    }

    private void createTemplate(OrderElement selectedNode) {
        orderTemplates.goToCreateTemplateFrom(selectedNode);
    }

    public void down() {
        if (tree.getSelectedCount() == 1) {
            down(getSelectedNode());
        }
    }

    public void down(OrderElement orderElement) {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        getModel().down(orderElement);
        filterByPredicateIfAny();
    }

    private OrderElement getSelectedNode() {
        return (OrderElement) tree.getSelectedItemApi().getValue();
    }

    public void move(Component dropedIn, Component dragged) {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);

        Treerow from = (Treerow) dragged;
        OrderElement fromNode = (OrderElement) ((Treeitem) from.getParent())
                .getValue();
        if (dropedIn instanceof Tree) {
            getModel().moveToRoot(fromNode);
        }
        if (dropedIn instanceof Treerow) {
            Treerow to = (Treerow) dropedIn;
            OrderElement toNode = (OrderElement) ((Treeitem) to.getParent())
                    .getValue();

            getModel().move(fromNode, toNode);
        }
        filterByPredicateIfAny();
    }

    public void addOrderElement() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        try {
            if (tree.getSelectedCount() == 1) {
                getModel().addOrderElementAt(getSelectedNode());
            } else {
                getModel().addOrderElement();
            }
            filterByPredicateIfAny();
        } catch (IllegalStateException e) {
            messagesForUser.showMessage(Level.ERROR, e.getMessage());
        }
    }

    private void filterByPredicateIfAny() {
        if (predicate != null) {
            filterByPredicate();
        }
    }

    private void filterByPredicate() {
        OrderElementTreeModel orderElementTreeModel = orderModel
                .getOrderElementsFilteredByPredicate(predicate);
        tree.setModel(orderElementTreeModel.asTree());
        tree.invalidate();
    }

    private static class TreeViewStateSnapshot {
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

    public void removeOrderElement() {
        Set<Treeitem> selectedItems = tree.getSelectedItems();
        for (Treeitem treeItem : selectedItems) {
            remove((OrderElement) treeItem.getValue());
        }
        filterByPredicateIfAny();
    }

    private void remove(OrderElement orderElement) {
        getModel().removeNode(orderElement);
    }

    void doEditFor(Order order) {
        Util.reloadBindings(tree);
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
    }

    public class OrderElementTreeitemRenderer implements TreeitemRenderer,
            RendererCtrl {

        private Map<OrderElement, Intbox> hoursIntBoxByOrderElement = new HashMap<OrderElement, Intbox>();
        private Treerow currentTreeRow;

        public OrderElementTreeitemRenderer() {
        }

        private Treecell addCell(Component... components) {
            return addCell(null, components);
        }

        private Treecell addCell(String cssClass, Component... components) {
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
            final OrderElement currentOrderElement = (OrderElement) data;
            addSchedulingStateCell(currentOrderElement);
            addCodeCell(currentOrderElement);
            addHoursCell(currentOrderElement);
            addTaskNumberCell(currentOrderElement);
            addInitDateCell(currentOrderElement);
            addEndDateCell(currentOrderElement);
            addOperationsCell(item, currentOrderElement);

            onDropMoveFromDraggedToTarget();
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

        private void addTaskNumberCell(final OrderElement orderElementForThisRow) {
            int[] path = getModel().getPath(orderElementForThisRow);
            String cssClass = "depth_" + path.length;

            Label taskNumber = new Label(pathAsString(path));
            taskNumber.setSclass("tasknumber");
            taskNumber.addEventListener(Events.ON_DOUBLE_CLICK,
                    new EventListener() {

                        @Override
                        public void onEvent(Event event) throws Exception {
                            IOrderElementModel model = orderModel
                                    .getOrderElementModel(orderElementForThisRow);
                            orderElementController.openWindow(model);
                            // Util.reloadBindings(tree);
                        }

                    });

            // TODO It would be needed to expand the width for the numbers
            // to make it ready for 2 and 3 digit numbers
            Textbox textBox = Util.bind(new Textbox(),
                    new Util.Getter<String>() {

                        @Override
                        public String get() {
                            return orderElementForThisRow.getName();
                        }
                    }, new Util.Setter<String>() {

                        @Override
                        public void set(String value) {
                            orderElementForThisRow.setName(value);
                        }
                    });

            addCell(cssClass, taskNumber, textBox);
        }

        private String pathAsString(int[] path) {
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

        private String getDecorationFromState(SchedulingState state) {
            String cssclass = "not-scheduled";
            if (state.isCompletelyScheduled()) {
                cssclass = "completely-scheduled";
            } else if (state.isPartiallyScheduled()) {
                cssclass = "partially-scheduled";
            }
            return cssclass;
        }

        private void addSchedulingStateCell(
                final OrderElement currentOrderElement) {
            SchedulingStateToggler schedulingStateToggler = new SchedulingStateToggler(currentOrderElement
                    .getSchedulingState());
            final Treecell cell = addCell(
                    getDecorationFromState(currentOrderElement
                            .getSchedulingState()), schedulingStateToggler);
            cell.addEventListener("onDoubleClick", new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    IOrderElementModel model = orderModel
                            .getOrderElementModel(currentOrderElement);
                    orderElementController.openWindow(model);
                }
            });
            currentOrderElement.getSchedulingState().addTypeChangeListener(
                    new ITypeChangedListener() {

                        @Override
                        public void typeChanged(Type newType) {
                            cell
                                    .setSclass(getDecorationFromState(currentOrderElement
                                            .getSchedulingState()));
                        }
                    });
            schedulingStateToggler.afterCompose();
        }

        private void addCodeCell(final OrderElement orderElement) {
            Textbox textBoxCode = new Textbox();
            Util.bind(textBoxCode, new Util.Getter<String>() {
                @Override
                public String get() {
                    return orderElement.getCode();
                }
            }, new Util.Setter<String>() {

                @Override
                public void set(String value) {
                    orderElement.setCode(value);
                }
            });
            textBoxCode.setConstraint(new Constraint() {
                @Override
                public void validate(Component comp, Object value)
                        throws WrongValueException {
                    if (!orderElement.isFormatCodeValid((String) value)) {
                        throw new WrongValueException(
                                comp,
                                _("Value is not valid.\n Code cannot contain chars like '_' \n and should not be empty"));
                    }
                }
            });

            if (orderModel.isCodeAutogenerated()) {
                textBoxCode.setDisabled(true);
            }

            addCell(textBoxCode);
        }

        private void addInitDateCell(final OrderElement currentOrderElement) {
            addCell(Util.bind(new Datebox(), new Util.Getter<Date>() {

                @Override
                public Date get() {
                    return currentOrderElement.getInitDate();
                }
            }, new Util.Setter<Date>() {

                @Override
                public void set(Date value) {
                    currentOrderElement.setInitDate(value);
                }
            }));
        }

        private void addEndDateCell(final OrderElement currentOrderElement) {
            addCell(Util.bind(new Datebox(), new Util.Getter<Date>() {

                @Override
                public Date get() {
                    return currentOrderElement.getDeadline();
                }
            }, new Util.Setter<Date>() {

                @Override
                public void set(Date value) {
                    currentOrderElement.setDeadline(value);
                }
            }));
        }

        private void addHoursCell(final OrderElement currentOrderElement) {
            Intbox intboxHours = buildHoursIntboxFor(currentOrderElement);
            hoursIntBoxByOrderElement.put(currentOrderElement, intboxHours);
            addCell(intboxHours);
        }

        private Intbox buildHoursIntboxFor(
                final OrderElement currentOrderElement) {
            Intbox result = new Intbox();
            if (currentOrderElement instanceof OrderLine) {
                OrderLine orderLine = (OrderLine) currentOrderElement;
                Util.bind(result, getHoursGetterFor(currentOrderElement),
                        getHoursSetterFor(orderLine));
                result.setConstraint(getHoursConstraintFor(orderLine));
            } else {
                // If it's a container hours cell is not editable
                Util.bind(result, getHoursGetterFor(currentOrderElement));
            }
            return result;
        }

        private Getter<Integer> getHoursGetterFor(
                final OrderElement currentOrderElement) {
            return new Util.Getter<Integer>() {
                @Override
                public Integer get() {
                    return currentOrderElement.getWorkHours();
                }
            };
        }

        private Constraint getHoursConstraintFor(final OrderLine orderLine) {
            return new Constraint() {
                @Override
                public void validate(Component comp, Object value)
                        throws WrongValueException {
                    if (!orderLine.isTotalHoursValid((Integer) value)) {
                        throw new WrongValueException(
                                comp,
                                _("Value is not valid, taking into account the current list of HoursGroup"));
                    }
                }
            };
        }

        private Setter<Integer> getHoursSetterFor(final OrderLine orderLine) {
            return new Util.Setter<Integer>() {
                @Override
                public void set(Integer value) {
                    orderLine.setWorkHours(value);
                    List<OrderElement> parentNodes = getModel().getParents(
                            orderLine);
                    // Remove the last element because it's an
                    // Order node, not an OrderElement
                    parentNodes.remove(parentNodes.size() - 1);
                    for (OrderElement node : parentNodes) {
                        Intbox intbox = hoursIntBoxByOrderElement.get(node);
                        intbox.setValue(node.getWorkHours());
                    }
                }
            };
        }

        private void addOperationsCell(final Treeitem item,
                final OrderElement currentOrderElement) {
            addCell(createEditButton(currentOrderElement),
                    createTemplateButton(currentOrderElement),
                    createUpButton(item,currentOrderElement),
                    createDownListener(item,currentOrderElement),
                    createUnindentButton(item, currentOrderElement),
                    createIndentButton(item, currentOrderElement),
                    createRemoveButton(currentOrderElement));
        }

        private Button createEditButton(final OrderElement currentOrderElement) {
            Button editbutton = createButton("/common/img/ico_editar1.png",
                    _("Edit"), "/common/img/ico_editar.png", "icono",
                    new EventListener() {
                        @Override
                        public void onEvent(Event event) throws Exception {
                            IOrderElementModel model = orderModel
                                    .getOrderElementModel(currentOrderElement);
                            orderElementController.openWindow(model);
                        }
                    });
            return editbutton;
        }

        private Component createTemplateButton(
                final OrderElement currentOrderElement) {
            Button templateButton = createButton(
                    "/common/img/ico_derived1.png", _("Create Template"),
                    "/common/img/ico_derived.png",
                    "icono",
                    new EventListener() {
                        @Override
                        public void onEvent(Event event) throws Exception {
                            createTemplate(currentOrderElement);
                        }
                    });
            return templateButton;
        }

        private Button createUpButton(final Treeitem item,
                final OrderElement currentOrderElement) {
            EventListener upButtonListener = new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    down(currentOrderElement);
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

        private Button createDownListener(final Treeitem item,
                final OrderElement currentOrderElement) {
            EventListener downButtonListener = new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    up(currentOrderElement);
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

        private Button createUnindentButton(final Treeitem item,
                final OrderElement currentOrderElement) {
            EventListener unindentListener = new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    unindent(currentOrderElement);
                }
            };
            final Button result;
            if ((isFirstLevelElement(item) || isSecondLevelElement(item))
                    && isPredicateApplied()) {
                result = createButton("/common/img/ico_izq_out.png",
                        "", "/common/img/ico_izq_out.png", "icono",
                        unindentListener);
                result.setDisabled(true);
            } else {
                result = createButton("/common/img/ico_izq1.png",
                        _("Unindent"), "/common/img/ico_izq.png", "icono",
                        unindentListener);
            }
            return result;
        }

        private Button createIndentButton(final Treeitem item,
                final OrderElement currentOrderElement) {
            EventListener indentListener = new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    indent(currentOrderElement);
                }
            };
            final Button result;
            if (isFirstLevelElement(item) && isPredicateApplied()) {
                result = createButton("/common/img/ico_derecha_out.png",
                        "", "/common/img/ico_derecha_out.png", "icono",
                        indentListener);
            } else {
                result = createButton("/common/img/ico_derecha1.png",
                        _("Indent"), "/common/img/ico_derecha.png", "icono",
                        indentListener);
            }
            return result;
        }

        private Button createRemoveButton(final OrderElement currentOrderElement) {
            final Button result = createButton(
                    "/common/img/ico_borrar1.png", _("Delete"),
                    "/common/img/ico_borrar.png", "icono", new EventListener() {
                        @Override
                        public void onEvent(Event event) throws Exception {
                            remove(currentOrderElement);
                            filterByPredicateIfAny();
                        }
                    });
            return result;
        }

        private Button createButton(String image, String tooltip,
                String hoverImage, String styleClass,
                EventListener eventListener) {
            Button result = new Button("", image);
            result.setHoverImage(hoverImage);
            result.setSclass(styleClass);
            result.setTooltiptext(tooltip);
            result.addEventListener(Events.ON_CLICK, eventListener);
            return result;
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

    private boolean isFirstLevelElement(Treeitem item) {
        return (item.getLevel() == 0);
    }

    private boolean isSecondLevelElement(Treeitem item) {
        return (item.getLevel() == 1);
    }

    private boolean isPredicateApplied() {
        return (predicate != null);
    }

    private final String SHOW_ALL = _("Show all");

    /**
     * Show all order elements in current order
     * @param event
     */
    public void onShowAll(Event event) {
        final String selectedOption = ((Combobox) event.getTarget()).getValue();
        if (SHOW_ALL.equals(selectedOption)) {
            // Delete predicate and set back to original tree model
            if (predicate != null) {
                bdFilter.clear();
                predicate = null;
                Util.reloadBindings(tree);
            }
            bdFilter.setDisabled(true);
        } else {
            bdFilter.setDisabled(false);
        }
    }

    private final String FILTER_BY_LABEL = _("Filter by Label");

    /**
     * Apply filter to order elements in current order
     * @param event
     */
    public void onApplyFilter(Event event) {
        final String selectedOption = cbFilterType.getValue();
        // Filter order elements by label
        if (FILTER_BY_LABEL.equals(selectedOption)) {
            org.navalplanner.business.labels.entities.Label label = getSelectedLabel();
            if (label == null) {
                label = org.navalplanner.business.labels.entities.Label
                        .create("");
            }
            // Create predicate and filter order elements by predicate
            predicate = new LabelOrderElementPredicate(label);
            filterByPredicate();
        }
    }

    private org.navalplanner.business.labels.entities.Label getSelectedLabel() {
        return (org.navalplanner.business.labels.entities.Label) bdFilter
                .getSelectedElement();
    }

    public boolean isItemSelected() {
        return (tree.getSelectedItem() != null);
    }

    public boolean isNotItemSelected() {
        return !isItemSelected();
    }

    private Button btnNew, btnDown, btnUp, btnUnindent, btnIndent, btnDelete;

    private void resetControlButtons() {
        final boolean disabled = tree.getSelectedItem() == null;

        btnNew.setDisabled(isPredicateApplied());
        btnIndent.setDisabled(disabled);
        btnUnindent.setDisabled(disabled);
        btnUp.setDisabled(disabled);
        btnDown.setDisabled(disabled);
        btnDelete.setDisabled(disabled);
    }

    /**
     * Disable control buttons (new, up, down, indent, unindent, delete)
     */
    public void updateControlButtons(Event event) {
        updateControlButtons((Tree) event.getTarget());
    }

    public void updateControlButtons(Tree tree) {
        final Treeitem item = tree.getSelectedItem();

        boolean disabledLevel1 = isPredicateApplied()
                && isFirstLevelElement(item);
        boolean disabledLevel2 = isPredicateApplied()
                && (isFirstLevelElement(item) || isSecondLevelElement(item));

        btnNew.setDisabled(false);
        btnDown.setDisabled(disabledLevel1);
        btnUp.setDisabled(disabledLevel1);
        btnUnindent.setDisabled(disabledLevel2);
        btnIndent.setDisabled(disabledLevel1);
        btnDelete.setDisabled(false);
    }

    /**
     * Clear {@link BandboxSearch} for Labels, and initializes
     * {@link IPredicate}
     */
    public void clear() {
        selectDefaultTab();
        cbFilterType.setSelectedIndex(0); // Select show all option
        bdFilter.setDisabled(true); // Disable when show all option is selected
        bdFilter.clear();
        predicate = null;
    }

    Tab tabGeneralData;

    private void selectDefaultTab() {
        tabGeneralData.setSelected(true);
    }

}
