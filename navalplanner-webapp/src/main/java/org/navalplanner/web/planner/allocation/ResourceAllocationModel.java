package org.navalplanner.web.planner.allocation;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.orders.daos.IHoursGroupDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.planner.daos.IResourceAllocationDAO;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.daos.IWorkerDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
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
    private IWorkerDAO workerDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IHoursGroupDAO hoursGroupDAO;

    @Autowired
    private IResourceAllocationDAO resourceAllocationDAO;

    private Task task;

    private org.zkoss.ganttz.data.Task ganttTask;

    private List<AllocationDTO> currentAllocations;

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    @Transactional(readOnly = true)
    public void addSpecificResourceAllocation(Worker worker) throws Exception {

        if (alreadyExistsAllocationFor(worker)) {
            throw new IllegalArgumentException(_(
                    "{0} already assigned to resource allocation list", worker
                            .getName()));
        }
        SpecificAllocationDTO allocation = SpecificAllocationDTO
                .forResource(worker);
        currentAllocations.add(allocation);
    }

    private boolean alreadyExistsAllocationFor(Worker worker) {
        return !getAllocationsFor(worker).isEmpty();
    }

    private List<SpecificAllocationDTO> getAllocationsFor(Worker worker) {
        List<SpecificAllocationDTO> found = SpecificAllocationDTO.withResource(
                SpecificAllocationDTO.getSpecific(currentAllocations), worker);
        return found;
    }

    @Override
    public Set<Criterion> getCriterions() {
        return (task != null) ? task.getHoursGroup().getCriterions()
                : new HashSet<Criterion>();
    }

    @Override
    public List<AllocationDTO> getAllocations() {
        if (currentAllocations == null) {
            return Collections.emptyList();
        }
        return currentAllocations;
    }

    @Override
    public void removeSpecificResourceAllocation(
            SpecificAllocationDTO allocation) {
        currentAllocations.remove(allocation);
        if (allocation.isModifying()) {
            task.removeResourceAllocation(allocation.getOrigin());
        }
    }

    @Override
    public void cancel() {
        currentAllocations = null;
    }

    @Override
    @Transactional(readOnly = true)
    public void save() {
        mergeDTOsToTask();
    }

    private void mergeDTOsToTask() {
        if (task.isFixedDuration()) {
            allocationForFixedTask();
        } else {
            throw new RuntimeException(
                    "TODO: allocation for tasks of variable duration. Now only works with fixed duration tasks");
        }
    }

    private void allocationForFixedTask() {
        for (AllocationDTO allocation : currentAllocations) {
            ResourceAllocation resourceAllocation = createOrModify(allocation);
            doAllocation(resourceAllocation, allocation.getResourcesPerDay());
        }
    }

    private List<Resource> getResourcesMatchingCriterions() {
        return resourceDAO.getAllByCriterions(getCriterions());
    }

    private ResourceAllocation createOrModify(AllocationDTO allocation) {
        if (allocation.isModifying()) {
            return allocation.getOrigin();
        } else {
            ResourceAllocation result = createAllocation(allocation);
            task.addResourceAllocation(result);
            return result;
        }
    }

    private ResourceAllocation createAllocation(AllocationDTO allocation) {
        if (allocation instanceof SpecificAllocationDTO) {
            SpecificAllocationDTO specific = (SpecificAllocationDTO) allocation;
            return createSpecific(specific.getResource());
        } else {
            return GenericResourceAllocation.create(task);
        }
    }

    private void doAllocation(ResourceAllocation allocation,
            ResourcesPerDay resourcesPerDay) {
        if (allocation instanceof GenericResourceAllocation) {
            doAllocation((GenericResourceAllocation) allocation,
                    resourcesPerDay);
        } else {
            SpecificResourceAllocation specific = (SpecificResourceAllocation) allocation;
            doAllocation(specific, resourcesPerDay);
        }
    }

    private void doAllocation(SpecificResourceAllocation specific,
            ResourcesPerDay resourcesPerDay) {
        specific.allocate(resourcesPerDay);
    }

    private void doAllocation(GenericResourceAllocation generic,
            ResourcesPerDay resourcesPerDay) {
        generic.forResources(getResourcesMatchingCriterions()).allocate(
                resourcesPerDay);
    }

    private ResourceAllocation createSpecific(Resource resource) {
        SpecificResourceAllocation result = SpecificResourceAllocation
                .create(task);
        result.setResource(resource);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public void initAllocationsFor(Task task,
            org.zkoss.ganttz.data.Task ganttTask) {
        this.ganttTask = ganttTask;
        this.task = task;
        taskElementDAO.save(this.task);
        reattachResourceAllocations(this.task.getResourceAllocations());
        hoursGroupDAO.save(this.task.getHoursGroup());
        reattachHoursGroup(this.task.getHoursGroup());
        reattachCriterions(this.task.getHoursGroup().getCriterions());
        currentAllocations = addDefaultGenericIfNeeded(asDTOs(this.task
                .getResourceAllocations()));
    }

    private void reattachResourceAllocations(
            Set<ResourceAllocation> resourceAllocations) {
        resourceAllocations.size();
        for (ResourceAllocation resourceAllocation : resourceAllocations) {
            resourceAllocation.getResourcesPerDay();
            if (resourceAllocation instanceof SpecificResourceAllocation) {
                reattachSpecificResourceAllocation((SpecificResourceAllocation) resourceAllocation);
            }
            resourceAllocationDAO.save(resourceAllocation);
        }
    }

    private void reattachSpecificResourceAllocation(
            SpecificResourceAllocation resourceAllocation) {
        resourceAllocation.getResource().getDescription();
        reattachCriterionSatisfactions(resourceAllocation.getResource()
                .getCriterionSatisfactions());
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

    private void reattachWorker(Worker worker) {
        workerDAO.save(worker);
        reattachCriterionSatisfactions(worker.getCriterionSatisfactions());
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
            Collection<? extends ResourceAllocation> resourceAllocations) {
        List<AllocationDTO> result = new ArrayList<AllocationDTO>();
        result.addAll(toGenericAllocations(resourceAllocations));
        result.addAll(toSpecificAllocations(resourceAllocations));
        return result;
    }

    private List<SpecificAllocationDTO> toSpecificAllocations(
            Collection<? extends ResourceAllocation> resourceAllocations) {
        List<SpecificAllocationDTO> result = new ArrayList<SpecificAllocationDTO>();
        for (ResourceAllocation resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof SpecificResourceAllocation) {
                SpecificResourceAllocation specific = (SpecificResourceAllocation) resourceAllocation;
                result.add(SpecificAllocationDTO.from(specific));
            }
        }
        return result;
    }

    private Collection<GenericAllocationDTO> toGenericAllocations(
            Collection<? extends ResourceAllocation> resourceAllocations) {
        ArrayList<GenericAllocationDTO> result = new ArrayList<GenericAllocationDTO>();
        for (ResourceAllocation resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof GenericResourceAllocation) {
                result.add(GenericAllocationDTO
                        .from((GenericResourceAllocation) resourceAllocation));
            }
        }
        return result;
    }

}
