package org.navalplanner.web.planner.allocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.navalplanner.business.orders.daos.IHoursGroupDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.planner.daos.IResourceAllocationDAO;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourceAllocationWithDesiredResourcesPerDay;
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
        resourceAllocationsBeingEdited.addSpecificResorceAllocationFor(worker);
    }

    @Override
    public Set<Criterion> getCriterions() {
        return (task != null) ? task.getHoursGroup().getCriterions()
                : new HashSet<Criterion>();
    }

    @Override
    public List<AllocationDTO> getAllocations() {
        if (resourceAllocationsBeingEdited == null) {
            return Collections.emptyList();
        }
        return resourceAllocationsBeingEdited.getCurrentAllocations();
    }

    @Override
    public void removeSpecificResourceAllocation(
            SpecificAllocationDTO allocation) {
        resourceAllocationsBeingEdited.remove(allocation);
    }

    @Override
    public void cancel() {
        resourceAllocationsBeingEdited = null;
    }

    @Override
    @Transactional(readOnly = true)
    public void save() {
        planningState.reassociateResourcesWithSession(resourceDAO);
        Set<ResourceAllocation<?>> allocationsRequestedToRemove = resourceAllocationsBeingEdited
                .getAllocationsRequestedToRemove();
        for (ResourceAllocation<?> resourceAllocation : allocationsRequestedToRemove) {
            task.removeResourceAllocation(resourceAllocation);
        }
        mergeDTOsToTask();
    }

    private void mergeDTOsToTask() {
        List<ResourceAllocationWithDesiredResourcesPerDay> resourceAllocations = toResourceAllocations();
        if (task.isFixedDuration()) {
            ResourceAllocation.allocating(resourceAllocations).withResources(
                    getResourcesMatchingCriterions()).allocateOnTaskLength();
        } else {
            LocalDate end = ResourceAllocation.allocating(resourceAllocations)
                    .withResources(getResourcesMatchingCriterions())
                    .untilAllocating(task.getHoursSpecifiedAtOrder());
            ganttTask.setEndDate(end.toDateTimeAtStartOfDay().toDate());
        }
    }

    private List<ResourceAllocationWithDesiredResourcesPerDay> toResourceAllocations() {
        List<ResourceAllocationWithDesiredResourcesPerDay> result = new ArrayList<ResourceAllocationWithDesiredResourcesPerDay>();
        for (AllocationDTO allocation : resourceAllocationsBeingEdited
                .getCurrentAllocations()) {
            result.add(createOrModify(allocation).withDesiredResourcesPerDay(
                    allocation.getResourcesPerDay()));
        }
        return result;
    }

    private List<Resource> getResourcesMatchingCriterions() {
        return resourceDAO.getAllByCriterions(getCriterions());
    }

    private ResourceAllocation<?> createOrModify(AllocationDTO allocation) {
        if (allocation.isModifying()) {
            return reloadResourceIfNeeded(allocation.getOrigin());
        } else {
            ResourceAllocation<?> result = createAllocation(allocation);
            task.addResourceAllocation(result);
            return result;
        }
    }

    private ResourceAllocation<?> reloadResourceIfNeeded(
            ResourceAllocation<?> origin) {
        if (origin instanceof SpecificResourceAllocation) {
            SpecificResourceAllocation specific = (SpecificResourceAllocation) origin;
            specific.setResource(getFromDB(specific.getResource()));
        }
        return origin;
    }

    private ResourceAllocation<?> createAllocation(AllocationDTO allocation) {
        if (allocation instanceof SpecificAllocationDTO) {
            SpecificAllocationDTO specific = (SpecificAllocationDTO) allocation;
            return createSpecific(specific.getResource());
        } else {
            return GenericResourceAllocation.create(task);
        }
    }

    private ResourceAllocation<?> createSpecific(Resource resource) {
        resource = getFromDB(resource);
        SpecificResourceAllocation result = SpecificResourceAllocation
                .create(task);
        result.setResource(resource);
        return result;
    }

    private Resource getFromDB(Resource resource) {
        return resourceDAO.findExistingEntity(resource.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public void initAllocationsFor(Task task,
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
        List<AllocationDTO> currentAllocations = addDefaultGenericIfNeeded(asDTOs(this.task
                .getResourceAllocations()));
        resourceAllocationsBeingEdited = new ResourceAllocationsBeingEdited(
                currentAllocations);
    }

    private void reattachResourceAllocations(
            Set<ResourceAllocation<?>> resourceAllocations) {
        resourceAllocations.size();
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            resourceAllocation.getResourcesPerDay();
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

    private void reattachResource(Resource resource) {
        resourceDAO.save(resource);
        reattachCriterionSatisfactions(resource.getCriterionSatisfactions());
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
            List<AllocationDTO> result = new ArrayList<AllocationDTO>();
            result.add(0, GenericAllocationDTO.createDefault());
            result.addAll(currentGeneric);
            return result;
        }
        return dtos;
    }

    private List<AllocationDTO> asDTOs(
            Collection<? extends ResourceAllocation<?>> resourceAllocations) {
        List<AllocationDTO> result = new ArrayList<AllocationDTO>();
        result.addAll(toGenericAllocations(resourceAllocations));
        result.addAll(toSpecificAllocations(resourceAllocations));
        return result;
    }

    private List<SpecificAllocationDTO> toSpecificAllocations(
            Collection<? extends ResourceAllocation<?>> resourceAllocations) {
        List<SpecificAllocationDTO> result = new ArrayList<SpecificAllocationDTO>();
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof SpecificResourceAllocation) {
                SpecificResourceAllocation specific = (SpecificResourceAllocation) resourceAllocation;
                result.add(SpecificAllocationDTO.from(specific));
            }
        }
        return result;
    }

    private Collection<GenericAllocationDTO> toGenericAllocations(
            Collection<? extends ResourceAllocation<?>> resourceAllocations) {
        ArrayList<GenericAllocationDTO> result = new ArrayList<GenericAllocationDTO>();
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof GenericResourceAllocation) {
                result.add(GenericAllocationDTO
                        .from((GenericResourceAllocation) resourceAllocation));
            }
        }
        return result;
    }

}
