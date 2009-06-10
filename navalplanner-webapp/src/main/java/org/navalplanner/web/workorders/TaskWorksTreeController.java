package org.navalplanner.web.workorders;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.navalplanner.business.workorders.entities.ProjectWork;
import org.navalplanner.business.workorders.entities.TaskWork;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.SimpleTreeNode;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.api.Tree;

/**
 * Controller for {@link WorkOrganization} view of WorkOrder entitites <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class TaskWorksTreeController extends GenericForwardComposer {

    private Tree tree;

    private TaskWorkTreeitemRenderer renderer = new TaskWorkTreeitemRenderer();

    private TreeViewStateSnapshot snapshotOfOpenedNodes;

    private final IProjectWorkModel projectWorkModel;

    public TaskWorkTreeitemRenderer getRenderer() {
        return renderer;
    }

    public TaskWorksTreeController(IProjectWorkModel projectWorkModel) {
        this.projectWorkModel = projectWorkModel;
    }

    public void indent() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        if (tree.getSelectedCount() == 1) {
            getTasksTreeModel().indent(getSelectedNode());
            Util.reloadBindings(tree);
        }
    }

    public TaskTreeModel getTasksTreeModel() {
        return projectWorkModel.getTasksTreeModel();
    }

    public void unindent() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        if (tree.getSelectedCount() == 1) {
            getTasksTreeModel().unindent(getSelectedNode());
            Util.reloadBindings(tree);
        }
    }

    public void up() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        if (tree.getSelectedCount() == 1) {
            getTasksTreeModel().up(getSelectedNode());
            Util.reloadBindings(tree);
        }
    }

    public void down() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        if (tree.getSelectedCount() == 1) {
            getTasksTreeModel().down(getSelectedNode());
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
        getTasksTreeModel().move(fromNode, toNode);
        Util.reloadBindings(tree);
    }

    public void addTaskWork() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        if (tree.getSelectedCount() == 1) {
            getTasksTreeModel().addTaskAt(getSelectedNode());
        } else {
            getTasksTreeModel().addTask();
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

    public void removeTaskWork() {
        Set<Treeitem> selectedItems = tree.getSelectedItems();
        for (Treeitem treeItem : selectedItems) {
            SimpleTreeNode value = (SimpleTreeNode) treeItem.getValue();
            getTasksTreeModel().removeNode(value);
        }
        Util.reloadBindings(tree);
    }

    void doEditFor(ProjectWork projectWork) {
        Util.reloadBindings(tree);
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("tasksTreeController", this, true);
    }

    private static interface Getter<T> {
        public T get();
    }

    private static interface Setter<T> {
        public void set(T value);
    }

    private static Textbox bind(Textbox textBox, Getter<String> getter) {
        textBox.setValue(getter.get());
        textBox.setDisabled(true);
        return textBox;
    }

    private static Textbox bind(final Textbox textBox,
            final Getter<String> getter, final Setter<String> setter) {
        textBox.setValue(getter.get());
        textBox.addEventListener("onChange", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                InputEvent newInput = (InputEvent) event;
                String value = newInput.getValue();
                setter.set(value);
                textBox.setValue(getter.get());
            }
        });
        return textBox;
    }

    private static Datebox bind(final Datebox dateBox,
            final Getter<Date> getter, final Setter<Date> setter) {
        dateBox.setValue(getter.get());
        dateBox.addEventListener("onChange", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                setter.set(dateBox.getValue());
                dateBox.setValue(getter.get());
            }
        });
        return dateBox;
    }

    public class TaskWorkTreeitemRenderer implements TreeitemRenderer {

        public void TaskWorkTreeitemRenderer() {
        }

        @Override
        public void render(Treeitem item, Object data) throws Exception {
            SimpleTreeNode t = (SimpleTreeNode) data;
            item.setValue(data);
            final TaskWork taskWork = (TaskWork) t.getData();
            if (snapshotOfOpenedNodes != null) {
                snapshotOfOpenedNodes.openIfRequired(item);
            }
            // Contruct treecells
            int[] path = getTasksTreeModel().getPath(t);
            Treecell cellForName = new Treecell(pathAsString(path));
            cellForName.appendChild(bind(new Textbox(), new Getter<String>() {

                @Override
                public String get() {
                    return taskWork.getName();
                }
            }, new Setter<String>() {

                @Override
                public void set(String value) {
                    taskWork.setName(value);
                }
            }));
            Treecell cellForHours = new Treecell();
            cellForHours.appendChild(bind(new Textbox(), new Getter<String>() {

                @Override
                public String get() {
                    return taskWork.getWorkHours() + "";
                }
            }));
            Treecell tcDateStart = new Treecell();
            tcDateStart.appendChild(bind(new Datebox(), new Getter<Date>() {

                @Override
                public Date get() {
                    return taskWork.getInitDate();
                }
            }, new Setter<Date>() {

                @Override
                public void set(Date value) {
                    taskWork.setInitDate(value);
                }
            }));
            Treecell tcDateEnd = new Treecell();
            tcDateEnd.appendChild(bind(new Datebox(), new Getter<Date>() {

                @Override
                public Date get() {
                    return taskWork.getEndDate();
                }
            }, new Setter<Date>() {

                @Override
                public void set(Date value) {
                    taskWork.setEndDate(value);
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