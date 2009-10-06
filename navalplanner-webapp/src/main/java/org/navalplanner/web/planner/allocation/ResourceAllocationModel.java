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

package org.navalplanner.web.planner.allocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.navalplanner.business.orders.daos.IHoursGroupDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.planner.daos.IResourceAllocationDAO;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.planner.PlanningState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to {@link Task}
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResourceAllocationModel implements IResourceAllocationModel {

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IHoursGroupDAO hoursGroupDAO;

    @Autowired
    private IResourceAllocationDAO resourceAllocationDAO;

    private Task task;

    private org.zkoss.ganttz.data.Task ganttTask;

    private PlanningState planningState;

    private ResourceAllocationsBeingEdited resourceAllocationsBeingEdited;

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    @Transactional(readOnly = true)
    public void addSpecificResourceAllocation(Worker worker) throws Exception {
        planningState.reassociateResourcesWithSession(resourceDAO);
        Resource reloaded = resourceDAO.findExistingEntity(worker.getId());
        reattachResource(reloaded);
        resourceAllocationsBeingEdited
                .addSpecificResourceAllocationFor(reloaded);
    }

    @Override
    public Set<Criterion> getCriterions() {
        return (task != null) ? task.getHoursGroup().getCriterions()
                : new HashSet<Criterion>();
    }

    @Override
    public void cancel() {
        resourceAllocationsBeingEdited = null;
    }

    @Override
    @Transactional(readOnly = true)
    public void accept() {
        planningState.reassociateResourcesWithSession(resourceDAO);
        removeDeletedAllocations();
        doTheAllocation();
    }

    private void removeDeletedAllocations() {
        Set<ResourceAllocation<?>> allocationsRequestedToRemove = resourceAllocationsBeingEdited
                .getAllocationsRequestedToRemove();
        for (ResourceAllocation<?> resourceAllocation : allocationsRequestedToRemove) {
            task.removeResourceAllocation(resourceAllocation);
        }
    }

    private void doTheAllocation() {
        ResourceAllocationsBeingEdited allocator = resourceAllocationsBeingEdited
                .taskModifying();
        allocator.doAllocation();
        Integer newDaysDuration = allocator.getDaysDuration();
        if (task.getDaysDuration() != newDaysDuration) {
            task.setDaysDuration(newDaysDuration);
            ganttTask.setEndDate(task.getEndDate());
        }
        task.setCalculatedValue(allocator.getCalculatedValue());
    }

    private List<Resource> getResourcesMatchingCriterions() {
        return resourceDAO.getAllByCriterions(getCriterions());
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceAllocationsBeingEdited initAllocationsFor(Task task,
            org.zkoss.ganttz.data.Task ganttTask, PlanningState planningState) {
        this.ganttTask = ganttTask;
        this.task = task;
        this.planningState = planningState;
        planningState.reassociateResourcesWithSession(resourceDAO);
        taskElementDAO.save(this.task);
        reattachResourceAllocations(this.task.getResourceAllocations());
        hoursGroupDAO.save(this.task.getHoursGroup());
        reattachHoursGroup(this.task.getHoursGroup());
        reattachCriterions(this.task.getHoursGroup().getCriterions());
        List<AllocationDTO> currentAllocations = addDefaultGenericIfNeeded(AllocationDTO.toDTOs(this.task
                .getResourceAllocations()));
        resourceAllocationsBeingEdited = ResourceAllocationsBeingEdited
                .noTaskModifying(task, currentAllocations, resourceDAO,
                        reattachResources(getResourcesMatchingCriterions()));
        return resourceAllocationsBeingEdited;
    }

    private void reattachResourceAllocations(
            Set<ResourceAllocation<?>> resourceAllocations) {
        resourceAllocations.size();
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            resourceAllocation.getAssignments();
            if (resourceAllocation instanceof SpecificResourceAllocation) {
                reattachSpecificResourceAllocation((SpecificResourceAllocation) resourceAllocation);
            }
            resourceAllocationDAO.save(resourceAllocation);
        }
    }

    private void reattachSpecificResourceAllocation(
            SpecificResourceAllocation resourceAllocation) {
        Resource resource = resourceAllocation.getResource();
        reattachResource(resource);
    }

    private void reattachHoursGroup(HoursGroup hoursGroup) {
        hoursGroup.getPercentage();
    }

    private void reattachCriterions(Set<Criterion> criterions) {
        for (Criterion criterion : criterions) {
            reattachCriterion(criterion);
        }
    }

    private void reattachCriterion(Criterion criterion) {
        criterion.getName();
        reattachCriterionType(criterion.getType());
    }

    private void reattachCriterionType(CriterionType criterionType) {
        criterionType.getName();
    }

    private List<Resource> reattachResources(
            List<Resource> resourcesMatchingCriterions) {
        for (Resource resource : resourcesMatchingCriterions) {
            reattachResource(resource);
        }
        return resourcesMatchingCriterions;
    }

    private void reattachResource(Resource resource) {
        resourceDAO.save(resource);
        reattachCriterionSatisfactions(resource.getCriterionSatisfactions());
        resource.getAssignments();
        if (resource.getCalendar() != null) {
            resource.getCalendar().getWorkableHours(new LocalDate());
        }
    }

    private void reattachCriterionSatisfactions(
            Set<CriterionSatisfaction> criterionSatisfactions) {
        for (CriterionSatisfaction criterionSatisfaction : criterionSatisfactions) {
            criterionSatisfaction.getStartDate();
            reattachCriterion(criterionSatisfaction.getCriterion());
        }
    }

    private List<AllocationDTO> addDefaultGenericIfNeeded(
            List<AllocationDTO> dtos) {
        List<GenericAllocationDTO> currentGeneric = AllocationDTO
                .getGeneric(dtos);
        if (currentGeneric.isEmpty()) {
            dtos.add(0, GenericAllocationDTO.createDefault());
        }
        return dtos;
    }

}
