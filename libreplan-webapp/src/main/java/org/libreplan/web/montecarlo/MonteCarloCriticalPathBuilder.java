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

package org.libreplan.web.montecarlo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.libreplan.business.planner.entities.Dependency;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         Constructs all the possible critical paths, departing from a list of
 *         elements that contain all the tasks which are in the critical path.
 *         The algorithm determines first all the possible starting tasks and
 *         navigates them forward until reaching an end.
 *
 *         Navigating from a {@link Task} to a {@link TaskGroup} is a bit
 *         troublesome. The algorithm considers than when there is a link
 *         between a task and a taskgroup, the task is connected with all the
 *         taskgroup's children which have: a) no incoming dependencies b) has
 *         no incoming dependencies from a task that it's not a children of that
 *         taskgroup.
 *
 *         Why the list of tasks in the critical path is not the only one
 *         critical path? It could be the case some of the tasks in that list
 *         finish at the same time (in parallel for instance). In those cases
 *         there are many critical paths and it's what this classes solves.
 */
public class MonteCarloCriticalPathBuilder {

    private List<Task> tasksInCriticalPath;

    private MonteCarloCriticalPathBuilder(List<Task> tasksInCriticalPath) {
        this.tasksInCriticalPath = tasksInCriticalPath;
    }

    public static MonteCarloCriticalPathBuilder create(
            List<Task> tasksInCriticalPath) {
        return new MonteCarloCriticalPathBuilder(tasksInCriticalPath);
    }

    /**
     * Constructs all possible paths starting from those tasks in the critical
     * path have no incoming dependencies or have incoming dependencies to other
     * tasks not in the critical path.
     *
     * Once all possible path were constructed, filter only those paths which
     * all their tasks are in the list of tasks in the critical path
     *
     * @param tasksInCriticalPath
     * @return
     */
    public List<List<Task>> buildAllPossibleCriticalPaths() {
        List<List<Task>> result = new ArrayList<List<Task>>();
        if (tasksInCriticalPath.size() == 1) {
            result.add(tasksInCriticalPath);
            return result;
        }
        List<List<Task>> allPaths = new ArrayList<List<Task>>();
        for (Task each : getStartingTasks(tasksInCriticalPath)) {
            allPaths.addAll(allPossiblePaths(each));
        }
        for (List<Task> path : allPaths) {
            if (isCriticalPath(path)) {
                result.add(path);
            }
        }
        return result;
    }

    private Collection<List<Task>> allPossiblePaths(Task task) {
        Collection<List<Task>> result = new ArrayList<List<Task>>();
        List<Task> path = Collections.singletonList(task);
        allPossiblePaths(path, result);
        return result;
    }

    private void allPossiblePaths(List<Task> path,
            Collection<List<Task>> allPaths) {
        TaskElement lastTask = getLastTask(path);
        List<Task> destinations = getDestinations(lastTask);
        if (!destinations.isEmpty()) {
            for (Task each : destinations) {
                allPossiblePaths(newPath(path, each), allPaths);
            }
        } else {
            allPaths.add(path);
        }
    }

    private TaskElement getLastTask(List<Task> path) {
        return path.get(path.size() - 1);
    }

    private List<Task> newPath(List<Task> path, Task task) {
        List<Task> result = new ArrayList<Task>();
        result.addAll(path);
        result.add(task);
        return result;
    }

    private List<Task> getDestinations(TaskElement task) {
        Set<Task> result = new HashSet<Task>();
        Set<Dependency> dependencies = getOutgoingDependencies(task);
        TaskGroup parent = task.getParent();
        if (parent != null) {
            if (parent.getEndDate().equals(task.getEndDate())) {
                result.addAll(getDestinations((task.getParent())));
            }
        }
        for (Dependency each : dependencies) {
            TaskElement destination = each.getDestination();
            if (isTask(destination)) {
                result.add((Task) destination);
            }
            if (isTaskGroup(destination)) {
                result.addAll(taskGroupChildren((TaskGroup) destination));
            }
        }
        return new ArrayList<Task>(result);
    }

    private Set<Task> taskGroupChildren(TaskGroup taskGroup) {
        Set<Task> result = new HashSet<Task>();
        for (TaskElement child : taskGroup.getChildren()) {
            if (isTask(child) && isStartingTask((Task) child)) {
                result.add((Task) child);
            }
            if (isTaskGroup(child)) {
                result.addAll(taskGroupChildren((TaskGroup) child));
            }
        }
        return result;
    }

    /**
     * A startingTask in a TaskGroup is a task that:
     *  a) Has no incoming dependencies
     *  b) All their incoming dependencies are from tasks in a different group
     *
     * @param task
     * @return
     */
    private boolean isStartingTask(Task task) {
        Set<Dependency> dependencies = getIncomingDependencies(task);
        return dependencies.isEmpty()
                && !parents(origins(dependencies)).contains(task.getParent());
    }

    private List<TaskGroup> parents(List<TaskElement> taskElements) {
        List<TaskGroup> result = new ArrayList<TaskGroup>();
        for (TaskElement each: taskElements) {
            result.add(each.getParent());
        }
        return result;
    }

    private List<TaskElement> origins(Set<Dependency> dependencies) {
        List<TaskElement> result = new ArrayList<TaskElement>();
        for (Dependency each: dependencies) {
            result.add(each.getOrigin());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Set<Dependency> getIncomingDependencies(TaskElement taskElement) {
        return taskElement != null ? taskElement
                .getDependenciesWithThisDestination() : Collections.EMPTY_SET;
    }

    @SuppressWarnings("unchecked")
    private Set<Dependency> getOutgoingDependencies(TaskElement taskElement) {
        return (taskElement != null) ? taskElement
                .getDependenciesWithThisOrigin() : Collections.EMPTY_SET;
    }

    private boolean isTaskGroup(TaskElement taskElement) {
        return taskElement instanceof TaskGroup;
    }

    private boolean isTask(TaskElement taskElement) {
        return taskElement instanceof Task;
    }

    private List<Task> getStartingTasks(List<Task> tasks) {
        List<Task> result = new ArrayList<Task>();
        for (Task each : tasks) {
            if (isStartingTask(each) && noneParentHasIncomingDependencies(each)) {
                result.add(each);
            }
        }
        return result;
    }

    private boolean noneParentHasIncomingDependencies(Task each) {
        TaskGroup parent = each.getParent();
        while (parent != null && getIncomingDependencies(parent).isEmpty()) {
            parent = parent.getParent();
        }
        return (parent == null);
    }

    private boolean isCriticalPath(List<Task> path) {
        for (Task each : path) {
            if (!tasksInCriticalPath.contains(each)) {
                return false;
            }
        }
        return true;
    }

}