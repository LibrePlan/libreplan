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

package org.zkoss.ganttz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.zkoss.ganttz.data.Position;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.data.TaskContainer;
import org.zkoss.ganttz.data.TaskContainer.IExpandListener;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.ext.AfterCompose;

/**
 * This class contains the information of a task container. It can be modified
 * and notifies of the changes to the interested parties. <br/>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class TaskContainerComponent extends TaskComponent implements
        AfterCompose {

    public static TaskContainerComponent asTask(Task taskContainerBean,
            TaskList taskList) {
        return new TaskContainerComponent((TaskContainer) taskContainerBean,
                taskList);
    }

    private List<TaskComponent> subtaskComponents = new ArrayList<TaskComponent>();
    private final TaskList taskList;

    private transient IExpandListener expandListener;

    public TaskContainerComponent(TaskContainer taskContainer, TaskList taskList) {
        super(taskContainer, taskList.getDisabilityConfiguration());
        if (!taskContainer.isContainer()) {
            throw new IllegalArgumentException();
        }
        this.expandListener = new IExpandListener() {

            @Override
            public void expandStateChanged(boolean isNowExpanded) {
                if (isNowExpanded) {
                    open();
                } else {
                    close();
                }
                updateClass();
            }
        };
        taskContainer.addExpandListener(expandListener);
        this.taskList = taskList;
        for (Task task : taskContainer.getTasks()) {
            getCurrentComponents().add(createChild(task));
        }
    }

    private TaskComponent createChild(Task task) {
        return TaskComponent.asTaskComponent(task, this.taskList, false);
    }

    @Override
    protected void publishDescendants(Map<Task, TaskComponent> resultAccumulated) {
        for (TaskComponent taskComponent : getCurrentComponents()) {
            taskComponent.publishTaskComponents(resultAccumulated);
        }
    }

    @Override
    protected void remove() {
        if (isExpanded()) {
            for (TaskComponent subtaskComponent : getCurrentComponents()) {
                subtaskComponent.remove();
            }
        }
        super.remove();
    }

    private void add(Integer insertionPosition,
            Collection<? extends Task> newTasks) {
        List<TaskComponent> taskComponents = new ArrayList<TaskComponent>();
        for (Task task : newTasks) {
            taskComponents.add(createChild(task));
        }

        if (insertionPosition == null) {
            subtaskComponents.addAll(taskComponents);
        } else {
            subtaskComponents.addAll(insertionPosition, taskComponents);
        }

        if (isExpanded()) {
            TaskComponent previous = insertionPosition == 0 ? this
                    : subtaskComponents.get(insertionPosition - 1);
            addAllAt(previous, taskComponents, true);
        }
    }

    public void open() {
        open(true);
    }

    public void open(boolean recolocate) {
        Component previous = this;
        List<TaskComponent> toAdd = getCurrentComponents();
        addAllAt(previous, toAdd, recolocate);
    }

    private void addAllAt(Component previous, List<TaskComponent> toAdd,
            boolean recolate) {
        for (TaskComponent subtaskComponent : toAdd) {
            taskList.addTaskComponent(previous.getNextSibling(),
                    subtaskComponent, recolate);
            previous = subtaskComponent;
        }
    }

    private List<TaskComponent> getCurrentComponents() {
        ListIterator<TaskComponent> listIterator = subtaskComponents
                .listIterator();
        // one of the subtask components created previously could have been
        // removed, so we only return the valid ones
        while (listIterator.hasNext()) {
            if (!getTaskContainer().contains(listIterator.next().getTask())) {
                listIterator.remove();
            }
        }
        return subtaskComponents;
    }

    private static int find(List<TaskComponent> currentComponents, Task task) {
        int i = 0;
        for (TaskComponent t : currentComponents) {
            if (t.getTask().equals(task)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public boolean isExpanded() {
        return getTaskContainer().isExpanded();
    }

    private TaskContainer getTaskContainer() {
        return (TaskContainer) getTask();
    }

    @Override
    protected String calculateClass() {
        return super.calculateClass() + " "
                + (getTaskContainer().isExpanded() ? "expanded" : "closed");
    }

    private void close() {
        for (TaskComponent subtaskComponent : getCurrentComponents()) {
            if (subtaskComponent instanceof TaskContainerComponent) {
                TaskContainerComponent container = (TaskContainerComponent) subtaskComponent;
                container.close();
            }
            taskList.hideTaskComponent(subtaskComponent);
            taskList.redrawDependencies();
        }
    }

    public void insert(Position position, Collection<? extends Task> newTasks) {
        if (position.getParent().equals(getTask())) {
            add(position.getInsertionPosition(), newTasks);
        } else {
            Task mostRemoteAncestor = position.getMostRemoteAncestor();
            Validate.isTrue(mostRemoteAncestor.equals(getTask()));
            position = position.pop();
            Task next = position.getMostRemoteAncestor();
            List<TaskComponent> currentComponents = getCurrentComponents();
            int find = find(currentComponents, next);
            TaskComponent taskComponent = currentComponents.get(find);
            if (taskComponent instanceof TaskContainerComponent) {
                TaskContainerComponent container = (TaskContainerComponent) taskComponent;
                container.insert(position, newTasks);
            } else {
                // TODO turn TaskComponent into container
            }
        }

    }

}