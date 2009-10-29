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

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
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

    private Combobox cbFilterType;

    private BandboxSearch bdFilter;

    private Tree tree;

    private OrderElementTreeitemRenderer renderer = new OrderElementTreeitemRenderer();

    private TreeViewStateSnapshot snapshotOfOpenedNodes;

    private final IOrderModel orderModel;

    private final OrderElementController orderElementController;

    private IPredicate predicate;

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
        if (tree.getSelectedCount() == 1) {
            getModel().addOrderElementAt(getSelectedNode());
        } else {
            getModel().addOrderElement();
        }
        filterByPredicateIfAny();
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
        comp.setVariable("orderElementTreeController", this, true);
    }

    public class OrderElementTreeitemRenderer implements TreeitemRenderer,
            RendererCtrl {

        private Map<OrderElement, Intbox> map = new HashMap<OrderElement, Intbox>();
        private Map<OrderElement, Textbox> mapC = new HashMap<OrderElement, Textbox>();

        public OrderElementTreeitemRenderer() {
        }

        @Override
        public void render(final Treeitem item, Object data) throws Exception {
            final OrderElement orderElementForThisRow = (OrderElement) data;
            item.setValue(data);
            if (snapshotOfOpenedNodes != null) {
                snapshotOfOpenedNodes.openIfRequired(item);
            }
            // Construct treecells
            int[] path = getModel().getPath(
                    orderElementForThisRow);
            String cssClass = "depth_" + path.length;

            Treecell cellForName = new Treecell();
            Label tasknumber = new Label(pathAsString(path));
            tasknumber.setSclass("tasknumber");
            tasknumber.addEventListener(Events.ON_DOUBLE_CLICK,
                    new EventListener() {

                        @Override
                        public void onEvent(Event event) throws Exception {
                            IOrderElementModel model = orderModel
                                    .getOrderElementModel(orderElementForThisRow);
                            orderElementController.openWindow(model);
                            // Util.reloadBindings(tree);
                        }

                    });

            cellForName.appendChild(tasknumber);
            cellForName.setSclass(cssClass);
            // It would be needed to expand the width for the numbers
            // to make it ready for 2 and 3 digit numbers
            cellForName.appendChild(Util.bind(new Textbox(),
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
                    }));

            Textbox textBoxCode = new Textbox();
            mapC.put(orderElementForThisRow, textBoxCode);
            Treecell cellForCode = new Treecell();
            cellForCode.appendChild(Util.bind(textBoxCode,
                    new Util.Getter<String>() {

                        @Override
                        public String get() {
                            return orderElementForThisRow.getCode();
                        }
                    }, new Util.Setter<String>() {

                        @Override
                        public void set(String value) {
                            orderElementForThisRow.setCode(value);
                        }
                    }));

            textBoxCode.setConstraint(new Constraint() {

                @Override
                public void validate(Component comp, Object value)
                        throws WrongValueException {
                    if (!orderElementForThisRow
                            .isFormatCodeValid((String) value)) {
                        throw new WrongValueException(
                                comp,
                                _("Value is not valid.\n Code cannot contain chars like '_' \n and should not be empty"));
                    }
                }
            });

            Treecell cellForHours = new Treecell();
            Intbox intboxHours = new Intbox();
            map.put(orderElementForThisRow, intboxHours);
            if (orderElementForThisRow instanceof OrderLine) {
                // If it's a leaf hours cell is editable
                Intbox intbox = Util.bind(intboxHours,
                        new Util.Getter<Integer>() {

                            @Override
                            public Integer get() {
                                return orderElementForThisRow.getWorkHours();
                            }
                        }, new Util.Setter<Integer>() {

                            @Override
                            public void set(Integer value) {
                                ((OrderLine) orderElementForThisRow)
                                        .setWorkHours(value);

                                List<OrderElement> parentNodes = getModel()
                                        .getParents(orderElementForThisRow);
                                // Remove the last element becuase it's an
                                // Order node, not an OrderElement
                                parentNodes.remove(parentNodes.size() - 1);

                                for (OrderElement node : parentNodes) {
                                    Intbox intbox = map.get(node);
                                    intbox.setValue(node.getWorkHours());
                                }
                            }
                        });
                // Checking hours value
                intbox.setConstraint(new Constraint() {

                    @Override
                    public void validate(Component comp, Object value)
                            throws WrongValueException {
                        if (!((OrderLine) orderElementForThisRow)
                                .isTotalHoursValid((Integer) value)) {
                            throw new WrongValueException(
                                    comp,
                                    _("Value is not valid, taking into account the current list of HoursGroup"));
                        }
                    }
                });

                cellForHours.appendChild(intbox);
            } else {
                // If it's a container hours cell is not editable
                cellForHours.appendChild(Util.bind(intboxHours,
                        new Util.Getter<Integer>() {

                            @Override
                            public Integer get() {
                                return orderElementForThisRow.getWorkHours();
                            }
                        }));
            }

            Treecell tcDateStart = new Treecell();
            tcDateStart.appendChild(Util.bind(new Datebox(),
                    new Util.Getter<Date>() {

                        @Override
                        public Date get() {
                            return orderElementForThisRow.getInitDate();
                        }
                    }, new Util.Setter<Date>() {

                        @Override
                        public void set(Date value) {
                            orderElementForThisRow.setInitDate(value);
                        }
                    }));
            Treecell tcDateEnd = new Treecell();
            tcDateEnd.appendChild(Util.bind(new Datebox(),
                    new Util.Getter<Date>() {

                        @Override
                        public Date get() {
                            return orderElementForThisRow.getEndDate();
                        }
                    }, new Util.Setter<Date>() {

                        @Override
                        public void set(Date value) {
                            orderElementForThisRow.setEndDate(value);
                        }
                    }));

            Treerow tr = null;
            /*
             * Since only one treerow is allowed, if treerow is not null, append
             * treecells to it. If treerow is null, contruct a new treerow and
             * attach it to item.
             */
            if (item.getTreerow() == null) {
                tr = new Treerow();
                tr.setParent(item);
            } else {
                tr = item.getTreerow();
                tr.getChildren().clear();
            }
            // Attach treecells to treerow
            tr.setDraggable("true");
            tr.setDroppable("true");

            cellForName.setParent(tr);
            cellForCode.setParent(tr);
            tcDateStart.setParent(tr);
            tcDateEnd.setParent(tr);
            cellForHours.setParent(tr);

            Treecell tcOperations = new Treecell();
            tcOperations.setParent(tr);

            Button editbutton = new Button("", "/common/img/ico_editar1.png");
            editbutton.setHoverImage("/common/img/ico_editar.png");
            editbutton.setParent(tcOperations);
            editbutton.setSclass("icono");
            editbutton.setTooltiptext(_("Edit"));
            editbutton.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    IOrderElementModel model = orderModel
                            .getOrderElementModel(orderElementForThisRow);
                    orderElementController.openWindow(model);
                }
            });

            Button upbutton = new Button("");
            if (isFirstLevelElement(item)
                    && isPredicateApplied()) {
                upbutton.setDisabled(true);
                upbutton.setImage("/common/img/ico_bajar_out.png");
                upbutton.setHoverImage("/common/img/ico_bajar_out.png");
                upbutton.setTooltiptext("");
            } else {
                upbutton.setDisabled(false);
                upbutton.setImage("/common/img/ico_bajar1.png");
                upbutton.setHoverImage("/common/img/ico_bajar.png");
                upbutton.setTooltiptext(_("Move down"));
            }
            upbutton.setParent(tcOperations);
            upbutton.setSclass("icono");
            upbutton.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    down(orderElementForThisRow);
                }
            });

            Button downbutton = new Button("");
            if (isFirstLevelElement(item)
                    && isPredicateApplied()) {
                downbutton.setDisabled(true);
                downbutton.setImage("/common/img/ico_subir_out.png");
                downbutton.setHoverImage("/common/img/ico_subir_out.png");
                downbutton.setTooltiptext("");
            } else {
                downbutton.setDisabled(false);
                downbutton.setImage("/common/img/ico_subir1.png");
                downbutton.setHoverImage("/common/img/ico_subir.png");
                downbutton.setTooltiptext(_("Move up"));
            }
            downbutton.setParent(tcOperations);
            downbutton.setSclass("icono");
            downbutton.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    up(orderElementForThisRow);
                }
            });

            final Button unindentbutton = new Button("");
            if ((isFirstLevelElement(item) || isSecondLevelElement(item))
                    && isPredicateApplied()) {
                unindentbutton.setDisabled(true);
                unindentbutton.setImage("/common/img/ico_izq_out.png");
                unindentbutton.setHoverImage("/common/img/ico_izq_out.png");
                unindentbutton.setTooltiptext("");
            } else {
                unindentbutton.setDisabled(false);
                unindentbutton.setImage("/common/img/ico_izq1.png");
                unindentbutton.setHoverImage("/common/img/ico_izq.png");
                unindentbutton.setTooltiptext(_("Unindent"));
            }
            unindentbutton.setParent(tcOperations);
            unindentbutton.setSclass("icono");
            unindentbutton.addEventListener(Events.ON_CLICK,
                    new EventListener() {
                        @Override
                        public void onEvent(Event event) throws Exception {
                            unindent(orderElementForThisRow);
                        }
                    });

            Button indentbutton = new Button("");
            if (isFirstLevelElement(item)
                    && isPredicateApplied()) {
                indentbutton.setDisabled(true);
                indentbutton.setImage("/common/img/ico_derecha_out.png");
                indentbutton.setHoverImage("/common/img/ico_derecha_out.png");
                indentbutton.setTooltiptext("");
            } else {
                indentbutton.setDisabled(false);
                indentbutton.setImage("/common/img/ico_derecha1.png");
                indentbutton.setHoverImage("/common/img/ico_derecha.png");
                indentbutton.setTooltiptext(_("Indent"));
            }
            indentbutton.setParent(tcOperations);
            indentbutton.setSclass("icono");
            indentbutton.setTooltiptext(_("Indent"));
            indentbutton.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    indent(orderElementForThisRow);
                }
            });

            Button removebutton = new Button("", "/common/img/ico_borrar1.png");
            removebutton.setHoverImage("/common/img/ico_borrar.png");
            removebutton.setParent(tcOperations);
            removebutton.setSclass("icono");
            removebutton.setTooltiptext(_("Delete"));
            removebutton.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    remove(orderElementForThisRow);
                    filterByPredicateIfAny();
                }
            });

            tr.addEventListener("onDrop", new EventListener() {

                @Override
                public void onEvent(org.zkoss.zk.ui.event.Event arg0)
                        throws Exception {
                    DropEvent dropEvent = (DropEvent) arg0;
                    move((Component) dropEvent.getTarget(),
                            (Component) dropEvent.getDragged());
                }
            });
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
     *
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
        }
    }

    private final String FILTER_BY_LABEL = _("Filter by Label");

    /**
     * Apply filter to order elements in current order
     *
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

    Button btnNew, btnDown, btnUp, btnUnindent, btnIndent, btnDelete;

    private void resetControlButtons() {
        btnNew.setDisabled(isPredicateApplied());
        btnIndent.setDisabled(true);
        btnUnindent.setDisabled(true);
        btnUp.setDisabled(true);
        btnDown.setDisabled(true);
        btnDelete.setDisabled(true);
    }

    /**
     * Disable control buttons (new, up, down, indent, unindent, delete)
     */
    public void updateControlButtons(Event event) {
        updateControlButtons((Tree) event.getTarget());
    }

    public void updateControlButtons(Tree tree) {
        final Treeitem item = tree.getSelectedItem();

        boolean disabledLevel1 = isPredicateApplied() && isFirstLevelElement(item);
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
        cbFilterType.setSelectedIndex(0);
        bdFilter.clear();
        predicate = null;
    }

}
