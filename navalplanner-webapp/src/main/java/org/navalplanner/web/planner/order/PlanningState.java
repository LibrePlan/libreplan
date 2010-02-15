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

package org.navalplanner.web.planner.order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.TaskMilestone;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.calendars.BaseCalendarModel;

public class PlanningState {

    public static PlanningState create(TaskGroup rootTask,
            Collection<? extends TaskElement> initialState,
            Collection<? extends Resource> initialResources,
            ICriterionDAO criterionDAO, IResourceDAO resourceDAO) {
        return new PlanningState(rootTask, initialState, initialResources,
                criterionDAO, resourceDAO);
    }

    private final ArrayList<TaskElement> initial;

    private final Set<TaskElement> toSave;

    private final Set<TaskElement> toRemove;

    private Set<Resource> resources = new HashSet<Resource>();

    private final TaskGroup rootTask;

    private final ICriterionDAO criterionDAO;

    private final IResourceDAO resourceDAO;

    private PlanningState(TaskGroup rootTask,
            Collection<? extends TaskElement> initialState,
            Collection<? extends Resource> initialResources,
            ICriterionDAO criterionDAO, IResourceDAO resourceDAO) {
        this.rootTask = rootTask;
        this.criterionDAO = criterionDAO;
        this.resourceDAO = resourceDAO;
        this.initial = new ArrayList<TaskElement>(initialState);
        this.toSave = new HashSet<TaskElement>(initialState);
        this.toRemove = new HashSet<TaskElement>();
        this.resources = doReattachments(new HashSet<Resource>(initialResources));
    }

    public Collection<? extends TaskElement> getTasksToSave() {
        return Collections.unmodifiableCollection(toSave);
    }

    public List<TaskElement> getInitial() {
        return new ArrayList<TaskElement>(initial);
    }

    public void reassociateResourcesWithSession() {
        for (Resource resource : resources) {
            resourceDAO.reattach(resource);
        }
        // ensuring no repeated instances of criterions
        reattachCriterions(getExistentCriterions(resources));
        addingNewlyCreated(resourceDAO);
    }

    private void reattachCriterions(Set<Criterion> criterions) {
        for (Criterion each : criterions) {
            criterionDAO.reattachUnmodifiedEntity(each);
        }
    }

    private Set<Criterion> getExistentCriterions(Set<Resource> resources) {
        Set<Criterion> result = new HashSet<Criterion>();
        for (Resource resource : resources) {
            for (CriterionSatisfaction each : resource
                    .getCriterionSatisfactions()) {
                result.add(each.getCriterion());
            }
        }
        return result;
    }

    private void addingNewlyCreated(IResourceDAO resourceDAO) {
        Set<Resource> newResources = getNewResources(resourceDAO);
        doReattachments(newResources);
        resources.addAll(newResources);
    }

    private <T extends Collection<Resource>> T doReattachments(T result) {
        for (Resource each : result) {
            reattachCalendarFor(each);
            // loading criterions so there are no repeated instances
            forceLoadOfCriterions(each);
        }
        return result;
    }

    private void reattachCalendarFor(Resource each) {
        if (each.getCalendar() != null) {
            BaseCalendarModel.forceLoadBaseCalendar(each.getCalendar());
        }
    }

    private void forceLoadOfCriterions(Resource resource) {
        Set<CriterionSatisfaction> criterionSatisfactions = resource
                .getCriterionSatisfactions();
        for (CriterionSatisfaction each : criterionSatisfactions) {
            each.getCriterion().getName();
            each.getCriterion().getType();
        }
    }

    private Set<Resource> getNewResources(IResourceDAO resourceDAO) {
        Set<Resource> result = new HashSet<Resource>(resourceDAO
                .list(Resource.class));
        result.removeAll(resources);
        return result;
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