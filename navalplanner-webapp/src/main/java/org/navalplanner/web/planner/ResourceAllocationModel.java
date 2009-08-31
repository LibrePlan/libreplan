package org.navalplanner.web.planner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IHoursGroupDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.planner.daos.IResourceAllocationDAO;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.daos.IWorkerDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionCompounder;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.ICriterion;
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
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResourceAllocationModel implements IResourceAllocationModel {

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private IWorkerDAO workerDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IHoursGroupDAO hoursGroupDAO;

    @Autowired
    private IResourceAllocationDAO resourceAllocationDAO;

    private Task task;

    private org.zkoss.ganttz.data.Task ganttTask;

    private ResourceAllocation resourceAllocation;

    @Override
    @Transactional(readOnly = true)
    public void setTask(Task task) {
        taskElementDAO.save(task);
        task.getResourceAllocations().size();

        HoursGroup hoursGroup = task.getHoursGroup();
        hoursGroupDAO.save(hoursGroup);
        hoursGroup.getCriterions().size();

        this.task = task;
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public void addResourceAllocation() {
        ResourceAllocation resourceAllocation = SpecificResourceAllocation.create(task);
        task.addResourceAllocation(resourceAllocation);
    }

    @Override
    public void removeResourceAllocation(ResourceAllocation resourceAllocation) {
        task.removeResourceAllocation(resourceAllocation);
    }

    @Override
    @Transactional(readOnly = true)
    public Worker findWorkerByNif(String nif) {
        try {
            return workerDAO.findUniqueByNif(nif);
        } catch (InstanceNotFoundException e) {
            return null;
        }
    }

    @Override
    public void setWorker(SpecificResourceAllocation resourceAllocation,
            Worker worker) {
        resourceAllocation.setWorker(worker);
    }

    @Override
    public Set<Criterion> getCriterions() {
        if (task == null) {
            return new HashSet<Criterion>();
        }
        return task.getHoursGroup().getCriterions();
    }

    @Override
    public Set<ResourceAllocation> getResourceAllocations() {
        if (task == null) {
            return new HashSet<ResourceAllocation>();
        }
        return task.getResourceAllocations();
    }

    @Override
    @Transactional(readOnly = true)
    public void setResourceAllocation(ResourceAllocation resourceAllocation) {
        resourceAllocationDAO.save(resourceAllocation);

        Worker worker = ((SpecificResourceAllocation) resourceAllocation)
                .getWorker();
        if (worker != null) {
            workerDAO.save(worker);
            Set<CriterionSatisfaction> criterionSatisfactions = worker
                    .getAllSatisfactions();
            for (CriterionSatisfaction criterionSatisfaction : criterionSatisfactions) {
                criterionSatisfaction.getCriterion().getName();
                criterionSatisfaction.getCriterion().getType().getName();
            }
        }

        this.resourceAllocation = resourceAllocation;
    }

    @Override
    @Transactional(readOnly = true)
    public Worker getWorker() {
        if (resourceAllocation == null) {
            return null;
        }
        Worker worker = ((SpecificResourceAllocation) resourceAllocation)
                .getWorker();
        if (worker == null) {
            return null;
        }
        try {
            return workerDAO.find(worker.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean workerSatisfiesCriterions() {

        for (Criterion criterion : getCriterions()) {
            sessionFactory.getCurrentSession().lock(criterion, LockMode.NONE);
        }

        Worker worker = getWorker();

        if (worker == null) {
            return true;
        }
        ICriterion compositedCriterion = CriterionCompounder.buildAnd(
                new ArrayList<ICriterion>(getCriterions())).getResult();
        return compositedCriterion.isSatisfiedBy(worker);
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
    public void addSpecificResourceAllocation(Worker worker) {
        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation
                .create(task);
        resourceAllocation.setWorker(worker);
        resourceAllocation.setPercentage(new BigDecimal(1));
        task.addResourceAllocation(resourceAllocation);
    }

}