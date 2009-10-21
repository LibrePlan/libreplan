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

package org.navalplanner.web.planner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.TaskMilestone;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;

public class PlanningState {
    private final ArrayList<TaskElement> initial;

    private final Set<TaskElement> toSave;

    private final Set<TaskElement> toRemove;

    private Set<Resource> resources = new HashSet<Resource>();

    private final TaskGroup rootTask;

    public PlanningState(TaskGroup rootTask,
            Collection<? extends TaskElement> initialState,
            Collection<? extends Resource> initialResources) {
        this.rootTask = rootTask;
        this.initial = new ArrayList<TaskElement>(initialState);
        this.toSave = new HashSet<TaskElement>(initialState);
        this.toRemove = new HashSet<TaskElement>();
        this.resources = new HashSet<Resource>(initialResources);
    }

    public Collection<? extends TaskElement> getTasksToSave() {
        return Collections.unmodifiableCollection(toSave);
    }

    public List<TaskElement> getInitial() {
        return new ArrayList<TaskElement>(initial);
    }

    public void reassociateResourcesWithSession(IResourceDAO resourceDAO) {
        for (Resource resource : resources) {
            resourceDAO.save(resource);
        }
        addingNewlyCreated(resourceDAO);
    }

    private void addingNewlyCreated(IResourceDAO resourceDAO) {
        resources.addAll(resourceDAO.list(Resource.class));
    }

    public Collection<? extends TaskElement> getToRemove() {
        return Collections.unmodifiableCollection(onlyNotTransient(toRemove));
    }

    private List<TaskElement> onlyNotTransient(
            Collection<? extends TaskElement> toRemove) {
        ArrayList<TaskElement> result = new ArrayList<TaskElement>();
        for (TaskElement taskElement : toRemove) {
            if (taskElement.getId() != null) {
                result.add(taskElement);
            }
        }
        return result;
    }

    public void removed(TaskElement taskElement) {
        taskElement.detach();
        if (!isTopLevel(taskElement)) {
            return;
        }
        toSave.remove(taskElement);
        toRemove.add(taskElement);
    }

    private boolean isTopLevel(TaskElement taskElement) {
        if (taskElement instanceof TaskMilestone) {
            return true;
        }
        return taskElement.getParent() == null;
    }

    public void added(TaskElement taskElement) {
        if (!isTopLevel(taskElement)) {
            return;
        }
        toRemove.remove(taskElement);
        toSave.add(taskElement);
    }

    public TaskGroup getRootTask() {
        return rootTask;
    }
}