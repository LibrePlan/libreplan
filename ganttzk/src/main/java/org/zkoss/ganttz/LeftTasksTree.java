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

package org.zkoss.ganttz;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zkoss.ganttz.LeftTasksTreeRow.ILeftTasksTreeNavigator;
import org.zkoss.ganttz.adapters.IDisabilityConfiguration;
import org.zkoss.ganttz.data.Position;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.data.TaskContainer;
import org.zkoss.ganttz.data.TaskContainer.IExpandListener;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;

/**
 * Tree element to display tasks structure in the planning Gantt.
 * <br />
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class LeftTasksTree extends HtmlMacroComponent {

    private DetailsForBeans detailsForBeans = new DetailsForBeans();

    private final DeferredFiller deferredFiller = new DeferredFiller();

    private final List<Task> tasks;

    private MutableTreeModel<Task> tasksTreeModel;

    private Tree tasksTree;

    private CommandContextualized<?> goingDownInLastArrowCommand;

    private final IDisabilityConfiguration disabilityConfiguration;

    private FilterAndParentExpandedPredicates predicate;

    private final List<Task> visibleTasks = new ArrayList<>();

    private Planner planner;

    public LeftTasksTree(IDisabilityConfiguration disabilityConfiguration,
                         Planner planner,
                         FilterAndParentExpandedPredicates predicate) {

        this.disabilityConfiguration = disabilityConfiguration;
        this.tasks = planner.getTaskList().getAllTasks();
        this.predicate = predicate;
        this.planner = planner;
    }

    @Override
    public void afterCompose() {
        setClass("listdetails");
        super.afterCompose();
        tasksTree = (Tree) getFellow("tasksTree");
        tasksTreeModel = MutableTreeModel.create(Task.class);
        fillModel(tasks, true);
        tasksTree.setModel(tasksTreeModel);
        tasksTree.setItemRenderer(getTaskBeanRenderer());

        /* Force call overridden render() */
        try {
            if ( !tasks.isEmpty() ) {
                getTaskBeanRenderer().render(new Treeitem(""), tasks.get(0), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private final class TaskBeanRenderer implements TreeitemRenderer<Task> {
        @Override
        public void render(final Treeitem treeitem, Task o, int i) throws Exception {
            Task task = o;
            treeitem.setOpen(isOpened(task));

            if ( task instanceof TaskContainer ) {

                final TaskContainer container = (TaskContainer) task;

                IExpandListener expandListener = new IExpandListener() {
                    @Override
                    public void expandStateChanged(boolean isNowExpanded) {
                        treeitem.setOpen(isNowExpanded);
                    }
                };

                container.addExpandListener(expandListener);

            }

            LeftTasksTreeRow leftTasksTreeRow =
                    LeftTasksTreeRow.create(disabilityConfiguration, task, new TreeNavigator(task), planner);

            if ( task.isContainer() ) {
                expandWhenOpened((TaskContainer) task, treeitem);
            }

            /* Clear existing Treerows */
            if ( !treeitem.getChildren().isEmpty() ) {
                treeitem.getChildren().clear();
            }

            Component row = disabilityConfiguration.isTreeEditable()
                    ? Executions
                    .getCurrent()
                    .createComponents("~./ganttz/zul/leftTasksTreeRow.zul", treeitem, null)
                    : Executions
                    .getCurrent()
                    .createComponents("~./ganttz/zul/leftTasksTreeRowLabels.zul", treeitem, null);

            leftTasksTreeRow.doAfterCompose(row);
            detailsForBeans.put(task, leftTasksTreeRow);
            deferredFiller.isBeingRendered(task, treeitem);
        }

        private void expandWhenOpened(final TaskContainer taskBean, Treeitem item) {
            item.addEventListener("onOpen", event -> {
                OpenEvent openEvent = (OpenEvent) event;
                taskBean.setExpanded(openEvent.isOpen());
            });
        }
    }

    private TaskBeanRenderer getTaskBeanRenderer() {
        return new TaskBeanRenderer();
    }

    public boolean isOpened(Task task) {
        return task.isLeaf() || task.isExpanded();
    }

    private static final class DetailsForBeans {

        private Map<Task, LeftTasksTreeRow> map = new HashMap<>();

        private Set<Task> focusRequested = new HashSet<>();

        public void put(Task task, LeftTasksTreeRow leftTasksTreeRow) {
            map.put(task, leftTasksTreeRow);

            if ( focusRequested.contains(task) ) {
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

    private final class TreeNavigator implements ILeftTasksTreeNavigator {

        private final int[] pathToNode;

        private final Task task;

        private TreeNavigator(Task task) {
            this.task = task;
            this.pathToNode = tasksTreeModel.getPath(tasksTreeModel.getRoot(), task);
        }

        @Override
        public LeftTasksTreeRow getAboveRow() {
            Task parent = getParent(pathToNode);
            int lastPosition = pathToNode[pathToNode.length - 1];

            if ( lastPosition != 0 ) {
                return getChild(parent, lastPosition - 1);
            } else if ( tasksTreeModel.getRoot() != parent ) {
                return getDetailFor(parent);
            }

            return null;
        }

        private LeftTasksTreeRow getChild(Task parent, int position) {
            return getDetailFor(tasksTreeModel.getChild(parent, position));
        }

        private LeftTasksTreeRow getDetailFor(Task child) {
            return detailsForBeans.get(child);
        }

        @Override
        public LeftTasksTreeRow getBelowRow() {
            if ( isExpanded() && hasChildren() ) {
                return getChild(task, 0);
            }

            for (ChildAndParent childAndParent : group(task, tasksTreeModel.getParents(task))) {
                if ( childAndParent.childIsNotLast() ) {
                    return getDetailFor(childAndParent.getNextToChild());
                }
            }

            // It's the last one, it has none below
            return null;
        }

        public List<ChildAndParent> group(Task origin, List<Task> parents) {
            ArrayList<ChildAndParent> result = new ArrayList<>();
            Task child = origin;

            for (Task parent : parents) {
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
                return tasksTreeModel.getChild(parent, getPositionOfChild() + 1);
            }

            public boolean childIsNotLast() {
                return getPositionOfChild() < numberOfChildrenForParent() - 1;
            }

            private int numberOfChildrenForParent() {
                return tasksTreeModel.getChildCount(parent);
            }

            private int getPositionOfChild() {
                if ( positionOfChildCached != null ) {
                    return positionOfChildCached;
                }

                int[] path = tasksTreeModel.getPath(parent, child);

                positionOfChildCached = path[path.length - 1];

                return positionOfChildCached;
            }
        }

        private boolean hasChildren() {
            return task.isContainer() && !task.getTasks().isEmpty();
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

    /**
     * This class is a workaround for an issue with zk {@link Tree}.
     * Once the tree is created, adding a node with children is troublesome.
     * Only the top element is added to the tree, although the element has children.
     * The Tree discards the adding event for the children because the parent says it's not loaded.
     *
     * This is the condition that is not satisfied:
     * <br />
     * <code>
     *     if( parent != null && (!(parent instanceof Treeitem) || ((Treeitem) parent).isLoaded()) ) {
     *         // ...
     *     }
     * </code>
     * <br />
     *
     * This problem is present in zk 3.6.1 at least.
     *
     * @author Óscar González Fernández <ogonzalez@igalia.com>
     * @see Tree#onTreeDataChange
     */
    private class DeferredFiller {

        private Set<Task> pendingToAddChildren = new HashSet<>();

        private Method setLoadedMethod = null;

        public void addParentOfPendingToAdd(Task parent) {
            pendingToAddChildren.add(parent);
        }

        public void isBeingRendered(final Task parent, final Treeitem item) {
            if ( !pendingToAddChildren.contains(parent) ) {
                return;
            }

            markLoaded(item);
            fillModel(parent, 0, parent.getTasks(), false);
            pendingToAddChildren.remove(parent);
        }

        private void markLoaded(Treeitem item) {
            try {
                Method method = getSetLoadedMethod();
                method.invoke(item, true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Method getSetLoadedMethod() {
            if ( setLoadedMethod != null ) {
                return setLoadedMethod;
            }

            try {
                Method method = Treeitem.class.getDeclaredMethod("setLoaded", Boolean.TYPE);
                method.setAccessible(true);
                setLoadedMethod = method;

                return setLoadedMethod;

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void fillModel(Collection<? extends Task> tasks, boolean firstTime) {
        fillModel(this.tasksTreeModel.getRoot(), 0, tasks, firstTime);
    }

    /**
     * This method is used to fill {@link LeftTasksTree} model.
     * It is using recursive go-round to go over all tasks.
     * For top-level tasks parent is null, for child tasks the parent is parent task.
     *
     * @param parent parent for current {@link Task}
     * @param insertionPosition
     * @param children child elements of a current {@link Task}
     * @param firstTime
     */
    private void fillModel(Task parent,
                           Integer insertionPosition,
                           Collection<? extends Task> children,
                           final boolean firstTime) {

        if ( predicate.isFilterContainers() ) {
            parent = this.tasksTreeModel.getRoot();
        }

        if ( firstTime ) {

            for (Task node : children) {

                if ( predicate.accpetsFilterPredicateAndContainers(node) ) {

                    if ( !visibleTasks.contains(node) ) {
                        this.tasksTreeModel.add(parent, node);
                        visibleTasks.add(node);
                    }

                } else {

                    if ( visibleTasks.contains(node) ) {
                        this.tasksTreeModel.remove(node);
                        visibleTasks.remove(node);
                    }
                }

                if ( node.isContainer() ) {
                    fillModel(node, 0, node.getTasks(), firstTime);
                }
            }

        } else {

            for (Task node : children) {

                if ( node.isContainer() ) {

                    if ( predicate.accpetsFilterPredicateAndContainers(node) ) {

                        if ( !visibleTasks.contains(node) ) {
                            this.deferredFiller.addParentOfPendingToAdd(node);
                        }
                    }
                }
            }

            // The node must be added after, so the multistepTreeFiller is ready
            for (Task node : children) {

                if ( predicate.accpetsFilterPredicateAndContainers(node) ) {

                    if ( !visibleTasks.contains(node) ) {
                        this.tasksTreeModel.add(parent, insertionPosition, Collections.singletonList(node));
                        visibleTasks.add(node);
                    }

                } else {
                    if (visibleTasks.contains(node)) {
                        this.tasksTreeModel.remove(node);
                        removeTaskAndAllChildren(visibleTasks, node);
                    }
                }

                if (node.isContainer()) {
                    fillModel(node, 0, node.getTasks(), firstTime);
                }

                if (visibleTasks.contains(node)) {
                    insertionPosition++;
                }
            }
        }
    }

    private void removeTaskAndAllChildren(List<Task> visibleTasks, Task task) {
        visibleTasks.remove(task);

        if ( task.isContainer() ) {
            for (Task node : task.getTasks()) {
                removeTaskAndAllChildren(visibleTasks, node);
            }
        }
    }

    public void taskRemoved(Task taskRemoved) {
        tasksTreeModel.remove(taskRemoved);
    }

    void addTask(Position position, Task task) {
        if ( position.isAppendToTop() ) {
            fillModel(Collections.singletonList(task), false);
            detailsForBeans.requestFocusFor(task);
        } else {
            List<Task> toAdd = Collections.singletonList(task);
            fillModel(position.getParent(), position.getInsertionPosition(), toAdd, false);
        }
    }

    public void addTasks(Position position, Collection<? extends Task> newTasks) {
        Task root = tasksTreeModel.getRoot();

        if ( position.isAppendToTop() ) {
            fillModel(root, tasksTreeModel.getChildCount(root), newTasks, false);
        } else if ( position.isAtTop() ) {
            fillModel(root, position.getInsertionPosition(), newTasks, false);
        } else {
            fillModel(position.getParent(), position.getInsertionPosition(), newTasks, false);
        }
    }

    public CommandContextualized<?> getGoingDownInLastArrowCommand() {
        return goingDownInLastArrowCommand;
    }

    public void setGoingDownInLastArrowCommand(CommandContextualized<?> goingDownInLastArrowCommand) {
        this.goingDownInLastArrowCommand = goingDownInLastArrowCommand;
    }

    public void setPredicate(FilterAndParentExpandedPredicates predicate) {
        this.predicate = predicate;

        visibleTasks.clear();
        tasksTreeModel = MutableTreeModel.create(Task.class);
        fillModel(tasks, true);
        tasksTree.setModel(tasksTreeModel);
    }

}
