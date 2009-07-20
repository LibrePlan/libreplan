package org.zkoss.ganttz;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.LeftTasksTreeRow.ILeftTasksTreeNavigator;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.data.TaskContainer;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;

public class LeftTasksTree extends HtmlMacroComponent {

    private final class TaskBeanRenderer implements TreeitemRenderer {
        public void render(Treeitem item, Object data) throws Exception {
            Task task = (Task) data;
            item.setOpen(isOpened(task));
            final int[] path = tasksTreeModel.getPath(tasksTreeModel.getRoot(),
                    task);
            String cssClass = "depth_" + path.length;
            LeftTasksTreeRow leftTasksTreeRow = LeftTasksTreeRow.create(task,
                    new TreeNavigator(tasksTreeModel, task));
            if (task.isContainer()) {
                expandWhenOpened((TaskContainer) task, item);
            }
            Component row = Executions.getCurrent().createComponents(
                    "~./ganttz/zul/leftTasksTreeRow.zul", item, null);
            leftTasksTreeRow.doAfterCompose(row);
            List<Object> rowChildren = row.getChildren();
            List<Treecell> treeCells = Planner.findComponentsOfType(
                    Treecell.class, rowChildren);
            for (Treecell cell : treeCells) {
                cell.setSclass(cssClass);
            }
            detailsForBeans.put(task, leftTasksTreeRow);
        }

        private void expandWhenOpened(final TaskContainer taskBean,
                Treeitem item) {
            item.addEventListener("onOpen", new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    OpenEvent openEvent = (OpenEvent) event;
                    taskBean.setExpanded(openEvent.isOpen());
                }
            });
        }

    }

    public boolean isOpened(Task task) {
        return task.isLeaf() || task.isExpanded();
    }

    private final class DetailsForBeans {
        private Map<Task, LeftTasksTreeRow> map = new HashMap<Task, LeftTasksTreeRow>();

        private Set<Task> focusRequested = new HashSet<Task>();

        public void put(Task task, LeftTasksTreeRow leftTasksTreeRow) {
            map.put(task, leftTasksTreeRow);
            if (focusRequested.contains(task)) {
                focusRequested.remove(task);
                leftTasksTreeRow.receiveFocus();
            }
        }

        public void requestFocusFor(Task task) {
            focusRequested.add(task);
        }

        public LeftTasksTreeRow get(Task taskbean) {
            return map.get(taskbean);
        }

    }

    private DetailsForBeans detailsForBeans = new DetailsForBeans();

    private final class TreeNavigator implements ILeftTasksTreeNavigator {
        private final int[] pathToNode;
        private final Task task;

        private TreeNavigator(TreeModel treemodel, Task task) {
            this.task = task;
            this.pathToNode = tasksTreeModel.getPath(tasksTreeModel.getRoot(),
                    task);
        }

        @Override
        public LeftTasksTreeRow getAboveRow() {
            Task parent = getParent(pathToNode);
            int lastPosition = pathToNode[pathToNode.length - 1];
            if (lastPosition != 0) {
                return getChild(parent, lastPosition - 1);
            } else if (tasksTreeModel.getRoot() != parent) {
                return getDetailFor(parent);
            }
            return null;
        }

        private LeftTasksTreeRow getChild(Task parent, int position) {
            Task child = tasksTreeModel.getChild(parent, position);
            return getDetailFor(child);
        }

        private LeftTasksTreeRow getDetailFor(Task child) {
            return detailsForBeans.get(child);
        }

        @Override
        public LeftTasksTreeRow getBelowRow() {
            if (isExpanded() && hasChildren()) {
                return getChild(task, 0);
            }
            for (ChildAndParent childAndParent : group(task, tasksTreeModel
                    .getParents(task))) {
                if (childAndParent.childIsNotLast()) {
                    return getDetailFor(childAndParent.getNextToChild());
                }
            }
            // it's the last one, it has none below
            return null;
        }

        public List<ChildAndParent> group(Task origin,
                List<Task> parents) {
            ArrayList<ChildAndParent> result = new ArrayList<ChildAndParent>();
            Task child = origin;
            Task parent;
            ListIterator<Task> listIterator = parents.listIterator();
            while (listIterator.hasNext()) {
                parent = listIterator.next();
                result.add(new ChildAndParent(child, parent));
                child = parent;
            }
            return result;
        }

        private class ChildAndParent {
            private final Task parent;

            private final Task child;

            private Integer positionOfChildCached;

            private ChildAndParent(Task child, Task parent) {
                this.parent = parent;
                this.child = child;
            }

            public Task getNextToChild() {
                return tasksTreeModel
                        .getChild(parent, getPositionOfChild() + 1);
            }

            public boolean childIsNotLast() {
                return getPositionOfChild() < numberOfChildrenForParent() - 1;
            }

            private int numberOfChildrenForParent() {
                return tasksTreeModel.getChildCount(parent);
            }

            private int getPositionOfChild() {
                if (positionOfChildCached != null)
                    return positionOfChildCached;
                int[] path = tasksTreeModel.getPath(parent, child);
                return positionOfChildCached = path[path.length - 1];
            }
        }

        private boolean hasChildren() {
            return task.isContainer() && task.getTasks().size() > 0;
        }

        private boolean isExpanded() {
            return task.isContainer() && task.isExpanded();
        }

        private Task getParent(int[] path) {
            Task current = tasksTreeModel.getRoot();
            for (int i = 0; i < path.length - 1; i++) {
                current = tasksTreeModel.getChild(current, path[i]);
            }
            return current;
        }

    }

    private static Log LOG = LogFactory.getLog(LeftTasksTree.class);

    private TaskRemovedListener taskRemovedListener;

    private final List<Task> tasks;

    private MutableTreeModel<Task> tasksTreeModel;

    private Tree tasksTree;

    private CommandContextualized<?> goingDownInLastArrowCommand;

    public LeftTasksTree(List<Task> tasks) {
        this.tasks = tasks;
    }

    private static void fillModel(MutableTreeModel<Task> treeModel,
            List<Task> tasks) {
        for (Task task : tasks) {
            fillModel(treeModel, treeModel.getRoot(), task);
        }
    }

    private static void fillModel(MutableTreeModel<Task> treeModel,
            Task parent, Task node) {
        treeModel.add(parent, node);
        if (node.isContainer()) {
            for (Task child : node.getTasks()) {
                fillModel(treeModel, node, child);
            }
        }
    }

    Planner getPlanner() {
        return (Planner) getParent();
    }

    public void taskRemoved(Task taskRemoved) {
        tasksTreeModel.remove(taskRemoved);
    }

    private static Date threeMonthsLater(Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 3);
        return calendar.getTime();
    }

    @Override
    public void afterCompose() {
        setClass("listdetails");
        super.afterCompose();
        tasksTree = (Tree) getFellow("tasksTree");
        tasksTreeModel = MutableTreeModel.create(Task.class);
        fillModel(tasksTreeModel, tasks);
        tasksTree.setModel(tasksTreeModel);
        tasksTree.setTreeitemRenderer(new TaskBeanRenderer());
    }

    void addTask(Task task) {
        detailsForBeans.requestFocusFor(task);
        tasksTreeModel.add(tasksTreeModel.getRoot(), task);
    }

    public CommandContextualized<?> getGoingDownInLastArrowCommand() {
        return goingDownInLastArrowCommand;
    }

    public void setGoingDownInLastArrowCommand(
            CommandContextualized<?> goingDownInLastArrowCommand) {
        this.goingDownInLastArrowCommand = goingDownInLastArrowCommand;
    }

}
