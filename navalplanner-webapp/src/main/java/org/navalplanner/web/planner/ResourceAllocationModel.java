package org.navalplanner.web.planner;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IHoursGroupDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.planner.daos.IResourceAllocationDAO;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
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
 *
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
    private IHoursGroupDAO hoursGroupDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IResourceAllocationDAO resourceAllocationDAO;

    private Task task;

    private org.zkoss.ganttz.data.Task ganttTask;

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    @Transactional(readOnly = true)
    public void setTask(Task task) {
        try {
            task = (Task) taskElementDAO.find(task.getId());
            reattachResourceAllocations(task.getResourceAllocations());
            hoursGroupDAO.save(task.getHoursGroup());
            reattachHoursGroup(task.getHoursGroup());
            reattachCriterions(task.getHoursGroup().getCriterions());

            this.task = task;
        } catch (InstanceNotFoundException e) {

        }
    }

    private void reattachResourceAllocations(
            Set<ResourceAllocation> resourceAllocations) {
        resourceAllocations.size();
        for (ResourceAllocation resourceAllocation : resourceAllocations) {
            resourceAllocation.getPercentage();
            if (resourceAllocation instanceof SpecificResourceAllocation) {
                reattachSpecificResourceAllocation((SpecificResourceAllocation) resourceAllocation);
            }
            resourceAllocationDAO.save(resourceAllocation);
        }
    }

    private void reattachSpecificResourceAllocation(
            SpecificResourceAllocation resourceAllocation) {
        resourceAllocation.getWorker().getName();
        reattachCriterionSatisfactions(resourceAllocation.getWorker()
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

    @Override
    @Transactional(readOnly = true)
    public void addGenericResourceAllocation() {
        GenericResourceAllocation resourceAllocation = GenericResourceAllocation
                .create(task);
        resourceAllocation.setPercentage(new BigDecimal(0));
        task.addResourceAllocation(resourceAllocation);
        taskElementDAO.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public void addSpecificResourceAllocation(Worker worker) throws Exception {

        // ResourceAllocation already exists
        if (findSpecificResourceAllocationByWorker(worker) != null) {
            throw new IllegalArgumentException(_(
                    "{0} already assigned to resource allocation list", worker
                            .getName()));
        }

        // Prepare resourceAllocation
        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation
                .create(task);
        resourceAllocation.setWorker(worker);
        resourceAllocation.setPercentage((new BigDecimal(1)));

        reattachWorker(worker);
        // Check if worker was itself a generic resource
        if (worker.satisfiesCriterions(getCriterions())) {
            Set<GenericResourceAllocation> genericResourceAllocations = getGenericResourceAllocations();
            // Generic resources always match criterions, so we need to remove
            // one generic resource to leave room for a specific resource
            if (genericResourceAllocations.size() > 0) {
                removeResourceAllocation(genericResourceAllocations.iterator()
                        .next());
            }
        }
        task.addResourceAllocation(resourceAllocation);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<GenericResourceAllocation> getGenericResourceAllocations() {
        return task.getGenericResourceAllocations();
    }

    @Override
    public void removeResourceAllocation(ResourceAllocation resourceAllocation) {
        task.removeResourceAllocation(resourceAllocation);
    }

    @Override
    public Set<Criterion> getCriterions() {
        return (task != null) ? task.getHoursGroup().getCriterions()
                : new HashSet<Criterion>();
    }

    @Override
    public Set<ResourceAllocation> getResourceAllocations() {
        return (task != null) ? task.getResourceAllocations()
                : new HashSet<ResourceAllocation>();
    }

    @Override
    @Transactional(readOnly = true)
    public void removeSpecificResourceAllocation(
            SpecificResourceAllocation resourceAllocation) {
        boolean addGenericResourceAllocation = false;

        // On removing this resourceAllocation, it may be room for a new generic
        // resource allocation
        Worker worker = resourceAllocation.getWorker();
        if (worker.satisfiesCriterions(getCriterions())) {
            addGenericResourceAllocation = true;
        }
        resourceAllocationDAO.save(resourceAllocation);
        task.removeResourceAllocation(resourceAllocation);
        // Add new generic resource
        if (addGenericResourceAllocation) {
            addGenericResourceAllocation();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void updateGenericPercentages(BigDecimal totalPercentage) {
        Set<GenericResourceAllocation> genericResourceAllocations = getGenericResourceAllocations();
        BigDecimal percentagePerResource = totalPercentage;

        percentagePerResource = percentagePerResource
                .subtract(getSumPercentageSpecificResourceAllocations());
        if (genericResourceAllocations.size() > 0) {
            percentagePerResource = percentagePerResource.setScale(8).divide(
                    new BigDecimal(genericResourceAllocations.size()),
                    BigDecimal.ROUND_DOWN);

            // Percentage cannot be negative
            if (percentagePerResource.compareTo(new BigDecimal(0)) < 0) {
                percentagePerResource = new BigDecimal(0);
            }

            for (ResourceAllocation resourceAllocation : genericResourceAllocations) {
                resourceAllocation.setPercentage(percentagePerResource);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getSumPercentageSpecificResourceAllocations() {
        return getSumPercentage(task.getSpecificResourceAllocations());
    }

    @SuppressWarnings("unchecked")
    private BigDecimal getSumPercentage(Set resourceAllocations) {
        BigDecimal result = new BigDecimal(0);

        for (Iterator i = resourceAllocations.iterator(); i.hasNext();) {
            ResourceAllocation resourceAllocation = (ResourceAllocation) i.next();
            BigDecimal percentage = (resourceAllocation.getPercentage() != null) ? resourceAllocation
                    .getPercentage()
                    : new BigDecimal(0);
            result = result.add(percentage);
        }

        return result;
    }

    private SpecificResourceAllocation findSpecificResourceAllocationByWorker(Worker worker) {
        for (SpecificResourceAllocation resourceAllocation : task
                .getSpecificResourceAllocations()) {
            if (resourceAllocation.getWorker().getId().equals(worker.getId())) {
                return resourceAllocation;
            }
        }
        return null;
    }

    @Transactional(readOnly = true)
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

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getSumPercentageResourceAllocations() {
        return getSumPercentage(task.getResourceAllocations());
    }

    @Override
    @Transactional(readOnly = true)
    public int getNumberUnassignedResources() {
        List<Resource> resources = resourceDAO
                .getAllByCriterions(getCriterions());
        Set<ResourceAllocation> resourceAllocations = task
                .getResourceAllocations();

        return (resources.size() - resourceAllocations.size() > 0) ? resources
                .size()
                - resourceAllocations.size() : 0;
    }

    @Override
    public void setGanttTask(org.zkoss.ganttz.data.Task ganttTask) {
        this.ganttTask = ganttTask;
    }

    @Override
    @Transactional(readOnly = true)
    public void updateGanttTaskDuration() {
        taskElementDAO.save(task);
        task.getDuration();
        ganttTask.setEndDate(task.getEndDate());
    }

    @Override
    public void cancel() {
        task.clearResourceAllocations();
    }

    @Override
    @Transactional
    public void save() {
        taskElementDAO.save(task);
    }

}
