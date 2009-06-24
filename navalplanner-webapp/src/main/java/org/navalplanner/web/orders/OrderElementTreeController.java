package org.navalplanner.web.orders;

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
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.SimpleTreeNode;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.api.Tree;

/**
 * Controller for {@link OrderElement} tree view of {@link Order} entities <br />
 *
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class OrderElementTreeController extends GenericForwardComposer {

    private Tree tree;

    private OrderElementTreeitemRenderer renderer = new OrderElementTreeitemRenderer();

    private TreeViewStateSnapshot snapshotOfOpenedNodes;

    private final IOrderModel orderModel;

    private final OrderElementController orderElementController;

    public OrderElementTreeitemRenderer getRenderer() {
        return renderer;
    }

    public OrderElementTreeController(IOrderModel orderModel,
            OrderElementController orderElementController) {
        this.orderModel = orderModel;
        this.orderElementController = orderElementController;
    }

    public void indent() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        if (tree.getSelectedCount() == 1) {
            getOrderElementTreeModel().indent(getSelectedNode());
            Util.reloadBindings(tree);
        }
    }

    public OrderElementModel getOrderElementTreeModel() {
        return orderModel.getOrderElementTreeModel();
    }

    public void unindent() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        if (tree.getSelectedCount() == 1) {
            getOrderElementTreeModel().unindent(getSelectedNode());
            Util.reloadBindings(tree);
        }
    }

    public void up() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        if (tree.getSelectedCount() == 1) {
            getOrderElementTreeModel().up(getSelectedNode());
            Util.reloadBindings(tree);
        }
    }

    public void down() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        if (tree.getSelectedCount() == 1) {
            getOrderElementTreeModel().down(getSelectedNode());
            Util.reloadBindings(tree);
        }
    }

    private SimpleTreeNode getSelectedNode() {
        return (SimpleTreeNode) tree.getSelectedItemApi().getValue();
    }

    public void move(Component dropedIn, Component dragged) {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        Treerow from = (Treerow) dragged;
        Treerow to = (Treerow) dropedIn;
        SimpleTreeNode fromNode = (SimpleTreeNode) ((Treeitem) from.getParent())
                .getValue();
        SimpleTreeNode toNode = (SimpleTreeNode) ((Treeitem) to.getParent())
                .getValue();
        getOrderElementTreeModel().move(fromNode, toNode);
        Util.reloadBindings(tree);
    }

    public void addOrderElement() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        if (tree.getSelectedCount() == 1) {
            getOrderElementTreeModel().addOrderElementAt(getSelectedNode());
        } else {
            getOrderElementTreeModel().addOrderElement();
        }
        Util.reloadBindings(tree);
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
            SimpleTreeNode node = (SimpleTreeNode) treeitem.getValue();
            return node.getData();
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
            SimpleTreeNode value = (SimpleTreeNode) treeItem.getValue();
            getOrderElementTreeModel().removeNode(value);
        }
        Util.reloadBindings(tree);
    }

    void doEditFor(Order order) {
        Util.reloadBindings(tree);
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("orderElementTreeController", this, true);
    }

    public class OrderElementTreeitemRenderer implements TreeitemRenderer {

        private Map<SimpleTreeNode, Intbox> map = new HashMap<SimpleTreeNode, Intbox>();

        public OrderElementTreeitemRenderer() {
        }

        @Override
        public void render(Treeitem item, Object data) throws Exception {
            final SimpleTreeNode t = (SimpleTreeNode) data;
            item.setValue(data);
            final OrderElement orderElement = (OrderElement) t.getData();
            if (snapshotOfOpenedNodes != null) {
                snapshotOfOpenedNodes.openIfRequired(item);
            }
            // Construct treecells
            int[] path = getOrderElementTreeModel().getPath(t);
            Treecell cellForName = new Treecell(pathAsString(path));
            cellForName.appendChild(Util.bind(new Textbox(),
                    new Util.Getter<String>() {

                @Override
                public String get() {
                    return orderElement.getName();
                }
            }, new Util.Setter<String>() {

                @Override
                public void set(String value) {
                    orderElement.setName(value);
                }
            }));
            Treecell cellForHours = new Treecell();
            Intbox intboxHours = new Intbox();
            map.put(t, intboxHours);
            if (orderElement instanceof OrderLine) {
                // If it's a leaf hours cell is editable
                cellForHours.appendChild(Util.bind(intboxHours,
                        new Util.Getter<Integer>() {

                            @Override
                            public Integer get() {
                                return orderElement.getWorkHours();
                            }
                        }, new Util.Setter<Integer>() {

                            @Override
                            public void set(Integer value) {
                                ((OrderLine) orderElement).setWorkHours(value);

                                List<SimpleTreeNode> parentNodes = getOrderElementTreeModel()
                                        .getParents(t);
                                // Remove the last element becuase it's an
                                // Order node, not an OrderElement
                                parentNodes.remove(parentNodes.size() - 1);

                                for (SimpleTreeNode node : parentNodes) {
                                    Intbox intbox = map.get(node);
                                    OrderElement parentOrderElement = (OrderElement) node
                                            .getData();
                                    intbox.setValue(parentOrderElement
                                            .getWorkHours());
                                }
                            }
                        }));
            } else {
                // If it's a container hours cell is not editable
                cellForHours.appendChild(Util.bind(intboxHours,
                        new Util.Getter<Integer>() {

                            @Override
                            public Integer get() {
                                return orderElement.getWorkHours();
                            }
                        }));
            }
            Treecell tcDateStart = new Treecell();
            tcDateStart.appendChild(Util.bind(new Datebox(),
                    new Util.Getter<Date>() {

                @Override
                public Date get() {
                    return orderElement.getInitDate();
                }
            }, new Util.Setter<Date>() {

                @Override
                public void set(Date value) {
                    orderElement.setInitDate(value);
                }
            }));
            Treecell tcDateEnd = new Treecell();
            tcDateEnd.appendChild(Util.bind(new Datebox(),
                    new Util.Getter<Date>() {

                @Override
                public Date get() {
                    return orderElement.getEndDate();
                }
            }, new Util.Setter<Date>() {

                @Override
                public void set(Date value) {
                    orderElement.setEndDate(value);
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
            tcDateStart.setParent(tr);
            tcDateEnd.setParent(tr);
            cellForHours.setParent(tr);
            // item.setOpen(false);

            tr.addEventListener("onDrop", new EventListener() {

                @Override
                public void onEvent(org.zkoss.zk.ui.event.Event arg0)
                        throws Exception {
                    DropEvent dropEvent = (DropEvent) arg0;
                    move((Component) dropEvent.getTarget(),
                            (Component) dropEvent.getDragged());
                }
            });

            tr.addEventListener(Events.ON_DOUBLE_CLICK, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    orderElementController.openPopup(orderElement);
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
    }

}
