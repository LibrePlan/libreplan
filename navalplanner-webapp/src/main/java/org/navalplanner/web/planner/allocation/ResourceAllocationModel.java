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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.common.ProportionalDistributor;
import org.navalplanner.business.orders.daos.IHoursGroupDAO;
import org.navalplanner.business.orders.entities.AggregatedHoursGroup;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.planner.order.PlanningState;
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
    private ITaskSourceDAO taskSourceDAO;

    private Task task;

    private org.zkoss.ganttz.data.Task ganttTask;

    @Autowired
    private IBaseCalendarDAO calendarDAO;

    private PlanningState planningState;

    private AllocationRowsHandler allocationRowsHandler;

    @Override
    @Transactional(readOnly = true)
    public void addSpecific(Collection<? extends Resource> resources) {
        reassociateResourcesWithSession();
        allocationRowsHandler
                .addSpecificResourceAllocationFor(reloadResources(resources));
    }

    private List<Resource> reloadResources(
            Collection<? extends Resource> resources) {
        List<Resource> result = new ArrayList<Resource>();
        for (Resource each : resources) {
            Resource reloaded = resourceDAO.findExistingEntity(each.getId());
            reattachResource(reloaded);
            result.add(reloaded);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ProportionalDistributor addDefaultAllocations() {
        reassociateResourcesWithSession();
        List<AggregatedHoursGroup> hoursGroups = task
                .getAggregatedByCriterions();
        int hours[] = new int[hoursGroups.size()];
        int i = 0;
        for (AggregatedHoursGroup each : hoursGroups) {
            hours[i++] = each.getHours();
            List<Resource> resourcesFound = resourceDAO
                    .findAllSatisfyingCriterions(each.getCriterions());
            allocationRowsHandler.addGeneric(each.getCriterions(),
                    reloadResources(resourcesFound), each.getHours());
        }
        return ProportionalDistributor.create(hours);
    }

    @Override
    @Transactional(readOnly = true)
    public void addGeneric(Set<Criterion> criterions,
            Collection<? extends Resource> resourcesMatched) {
        reassociateResourcesWithSession();
        List<Resource> reloadResources = reloadResources(resourcesMatched);
        allocationRowsHandler.addGeneric(criterions, reloadResources);
    }

    @Override
    public void cancel() {
        allocationRowsHandler = null;
    }

    @Override
    @Transactional(readOnly = true)
    public void accept() {
        stepsBeforeDoingAllocation();
        doTheAllocation(allocationRowsHandler
                .doAllocation());
    }

    @Override
    @Transactional(readOnly = true)
    public void accept(AllocationResult modifiedAllocationResult) {
        stepsBeforeDoingAllocation();
        doTheAllocation(modifiedAllocationResult);
    }

    @Override
    @Transactional(readOnly = true)
    public <T> T onAllocationContext(
            IResourceAllocationContext<T> resourceAllocationContext) {
        ensureResourcesAreReadyForDoingAllocation();
        return resourceAllocationContext.doInsideTransaction();
    }

    private void ensureResourcesAreReadyForDoingAllocation() {
        Set<Resource> resources = allocationRowsHandler
                .getAllocationResources();
        for (Resource each : resources) {
            reattachResource(each);
        }
    }

    private void stepsBeforeDoingAllocation() {
        ensureResourcesAreReadyForDoingAllocation();
        if (task.getCalendar() != null) {
            calendarDAO.reattachUnmodifiedEntity(task.getCalendar());
        }
        removeDeletedAllocations();
    }

    private void reassociateResourcesWithSession() {
        planningState.reassociateResourcesWithSession(resourceDAO);
    }

    private void removeDeletedAllocations() {
        Set<ResourceAllocation<?>> allocationsRequestedToRemove = allocationRowsHandler
                .getAllocationsRequestedToRemove();
        for (ResourceAllocation<?> resourceAllocation : allocationsRequestedToRemove) {
            task.removeResourceAllocation(resourceAllocation);
        }
    }

    private void doTheAllocation(AllocationResult allocationResult) {
        Date previousStartDate = ganttTask.getBeginDate();
        long previousLength = ganttTask.getLengthMilliseconds();
        allocationResult.applyTo(task);
        ganttTask.fireChangesForPreviousValues(previousStartDate,
                previousLength);
    }

    @Override
    @Transactional(readOnly = true)
    public AllocationRowsHandler initAllocationsFor(Task task,
            org.zkoss.ganttz.data.Task ganttTask, PlanningState planningState) {
        this.ganttTask = ganttTask;
        this.task = task;
        this.planningState = planningState;
        planningState.reassociateResourcesWithSession(resourceDAO);
        taskElementDAO.reattach(this.task);
        reattachTaskSource();
        loadCriterionsOfGenericAllocations();
        reattachHoursGroup(this.task.getHoursGroup());
        reattachCriterions(this.task.getHoursGroup().getValidCriterions());
        loadResources(this.task.getResourceAllocations());
        List<AllocationRow> initialRows = AllocationRow.toRows(this.task
                .getResourceAllocations());
        allocationRowsHandler = AllocationRowsHandler.create(
                task, initialRows, resourceDAO);
        return allocationRowsHandler;
    }

    private void loadCriterionsOfGenericAllocations() {
        Set<ResourceAllocation<?>> resourceAllocations = this.task
                .getResourceAllocations();
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof GenericResourceAllocation) {
                GenericResourceAllocation generic = (GenericResourceAllocation) resourceAllocation;
                generic.getCriterions().size();
            }
        }
    }

    private void reattachHoursGroup(HoursGroup hoursGroup) {
        hoursGroupDAO.reattachUnmodifiedEntity(hoursGroup);
        hoursGroup.getPercentage();
        reattachCriterions(hoursGroup.getValidCriterions());
    }

    private void reattachCriterions(Set<Criterion> criterions) {
        for (Criterion criterion : criterions) {
            reattachCriterion(criterion);
        }
    }

    private void loadResources(Set<ResourceAllocation<?>> resourceAllocations) {
        for (ResourceAllocation<?> each : resourceAllocations) {
            each.getAssociatedResources();
        }
    }

    private void reattachTaskSource() {
        TaskSource taskSource = task.getTaskSource();
        taskSourceDAO.reattach(taskSource);
        Set<HoursGroup> hoursGroups = taskSource.getHoursGroups();
        for (HoursGroup hoursGroup : hoursGroups) {
            reattachHoursGroup(hoursGroup);
        }
    }

    private void reattachCriterion(Criterion criterion) {
        criterion.getName();
        reattachCriterionType(criterion.getType());
    }

    private void reattachCriterionType(CriterionType criterionType) {
        criterionType.getName();
    }

    private void reattachResource(Resource resource) {
        resourceDAO.reattach(resource);
        reattachCriterionSatisfactions(resource.getCriterionSatisfactions());
        calendarDAO.reattachUnmodifiedEntity(resource.getCalendar());
        for (DayAssignment dayAssignment : resource.getAssignments()) {
            Hibernate.initialize(dayAssignment);
        }
    }

    private void reattachCriterionSatisfactions(
            Set<CriterionSatisfaction> criterionSatisfactions) {
        for (CriterionSatisfaction criterionSatisfaction : criterionSatisfactions) {
            criterionSatisfaction.getStartDate();
            reattachCriterion(criterionSatisfaction.getCriterion());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AggregatedHoursGroup> getHoursAggregatedByCriterions() {
        reattachTaskSource();
        List<AggregatedHoursGroup> result = task.getTaskSource()
                .getAggregatedByCriterions();
        ensuringAccesedPropertiesAreLoaded(result);
        return result;
    }

    private void ensuringAccesedPropertiesAreLoaded(
            List<AggregatedHoursGroup> result) {
        for (AggregatedHoursGroup each : result) {
            each.getCriterionsJoinedByComma();
            each.getHours();
        }
    }

    @Override
    public Integer getOrderHours() {
        if (task == null) {
            return 0;
        }
        return AggregatedHoursGroup.sum(task.getAggregatedByCriterions());
    }

}
