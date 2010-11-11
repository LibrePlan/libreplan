/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.ProportionalDistributor;
import org.navalplanner.business.orders.daos.IHoursGroupDAO;
import org.navalplanner.business.orders.entities.AggregatedHoursGroup;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.DerivedAllocation;
import org.navalplanner.business.planner.entities.DerivedAllocationGenerator.IWorkerFinder;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.MachineWorkersConfigurationUnit;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.planner.order.PlanningState;
import org.navalplanner.web.resources.search.IResourceSearchModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;

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
    private IResourceSearchModel searchModel;

    @Autowired
    private IHoursGroupDAO hoursGroupDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    private Task task;

    @Autowired
    private ICriterionDAO criterionDAO;

    private PlanningState planningState;

    private AllocationRowsHandler allocationRowsHandler;

    private IContextWithPlannerTask<TaskElement> context;

    @Autowired
    private IAdHocTransactionService transactionService;

    private Date currentStartDate;

    @Override
    @Transactional(readOnly = true)
    public void addSpecific(Collection<? extends Resource> resources) {
        reassociateResourcesWithSession();
        allocationRowsHandler
                .addSpecificResourceAllocationFor(reloadResources(resources));
    }

    @SuppressWarnings("unchecked")
    private <T extends Resource> List<T> reloadResources(
            Collection<? extends T> resources) {
        List<T> result = new ArrayList<T>();
        for (T each : resources) {
            Resource reloaded = resourceDAO.findExistingEntity(each.getId());
            reattachResource(reloaded);
            result.add((T) reloaded);
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
            List<? extends Resource> resourcesFound = searchModel
                    .searchBy(each.getResourceType())
                    .byCriteria(each.getCriterions()).execute();
            allocationRowsHandler.addGeneric(each.getResourceType(),
                    each.getCriterions(), reloadResources(resourcesFound),
                    each.getHours());
        }
        return ProportionalDistributor.create(hours);
    }

    @Override
    @Transactional(readOnly = true)
    public void addGeneric(ResourceEnum resourceType,
            Collection<? extends Criterion> criteria,
            Collection<? extends Resource> resourcesMatched) {
        reassociateResourcesWithSession();
        List<Resource> reloadResources = reloadResources(resourcesMatched);
        allocationRowsHandler.addGeneric(resourceType, criteria,
                reloadResources);
    }

    @Override
    public void cancel() {
        if (currentStartDate != null) {
            task.setStartDate(currentStartDate);
        }
        allocationRowsHandler = null;
        currentStartDate = null;
    }

    @Override
    public void accept() {
        if (context != null) {
            applyAllocationWithDateChangesNotification(new IOnTransaction<Void>() {
                @Override
                public Void execute() {
                    stepsBeforeDoingAllocation();
                    allocationRowsHandler.doAllocation().applyTo(
                            planningState.getCurrentScenario(), task);
                    return null;
                }
            });
        }
    }

    @Override
    public void accept(final AllocationResult modifiedAllocationResult) {
        if (context != null) {
            applyAllocationWithDateChangesNotification(new IOnTransaction<Void>() {
                @Override
                public Void execute() {
                    stepsBeforeDoingAllocation();
                    modifiedAllocationResult.applyTo(planningState
                            .getCurrentScenario(), task);
                    return null;
                }
            });
        }
    }

    private void applyAllocationWithDateChangesNotification(
            IOnTransaction<?> allocationDoer) {
        org.zkoss.ganttz.data.Task ganttTask = context.getTask();
        GanttDate previousStartDate = ganttTask.getBeginDate();
        GanttDate previousEnd = ganttTask.getEndDate();
        transactionService.runOnReadOnlyTransaction(allocationDoer);
        ganttTask.fireChangesForPreviousValues(previousStartDate, previousEnd);
    }

    private void stepsBeforeDoingAllocation() {
        ensureResourcesAreReadyForDoingAllocation();
        removeDeletedAllocations();
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

    private void reassociateResourcesWithSession() {
        planningState.reassociateResourcesWithSession();
    }

    private void removeDeletedAllocations() {
        Set<ResourceAllocation<?>> allocationsRequestedToRemove = allocationRowsHandler
                .getAllocationsRequestedToRemove();
        for (ResourceAllocation<?> resourceAllocation : allocationsRequestedToRemove) {
            task.removeResourceAllocation(resourceAllocation);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AllocationRowsHandler initAllocationsFor(Task task,
            IContextWithPlannerTask<TaskElement> context,
            PlanningState planningState) {
        this.context = context;
        this.task = task;
        this.currentStartDate = task.getStartDate();
        this.planningState = planningState;
        planningState.reassociateResourcesWithSession();
        taskElementDAO.reattach(this.task);
        reattachTaskSource();
        loadCriterionsOfGenericAllocations();
        reattachHoursGroup(this.task.getHoursGroup());
        reattachCriterions(this.task.getHoursGroup().getValidCriterions());
        loadResources(this.task.getSatisfiedResourceAllocations());
        loadDerivedAllocations(this.task.getSatisfiedResourceAllocations());
        List<AllocationRow> initialRows = AllocationRow.toRows(
                task.getNonLimitingResourceAllocations(), searchModel);
        allocationRowsHandler = AllocationRowsHandler.create(task, initialRows,
                createWorkerFinder());
        return allocationRowsHandler;
    }

    private IWorkerFinder createWorkerFinder() {
        return new IWorkerFinder() {

            @Override
            public Collection<Worker> findWorkersMatching(
                    Collection<? extends Criterion> requiredCriteria) {
                reassociateResourcesWithSession();
                List<Worker> workers = new ArrayList<Worker>();
                if (!requiredCriteria.isEmpty()) {
                    workers = searchModel.searchWorkers()
                            .byCriteria(requiredCriteria).execute();
                }
                return reloadResources(workers);
            }
        };
    }

    private void loadCriterionsOfGenericAllocations() {
        Set<ResourceAllocation<?>> resourceAllocations = this.task
                .getSatisfiedResourceAllocations();
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

    private void loadMachine(Machine eachMachine) {
        for (MachineWorkersConfigurationUnit eachUnit : eachMachine
                .getConfigurationUnits()) {
            Hibernate.initialize(eachUnit);
        }
    }

    private void loadDerivedAllocations(
            Set<ResourceAllocation<?>> resourceAllocations) {
        for (ResourceAllocation<?> each : resourceAllocations) {
            for (DerivedAllocation eachDerived : each.getDerivedAllocations()) {
                Hibernate.initialize(eachDerived);
                eachDerived.getAssignments();
                eachDerived.getAlpha();
                eachDerived.getName();
            }
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
        criterionDAO.reattachUnmodifiedEntity(criterion);
        criterion.getName();
        reattachCriterionType(criterion.getType());
    }

    private void reattachCriterionType(CriterionType criterionType) {
        criterionType.getName();
    }

    private void reattachResource(Resource resource) {
        resourceDAO.reattach(resource);
        reattachCriterionSatisfactions(resource.getCriterionSatisfactions());
        for (DayAssignment dayAssignment : resource.getAssignments()) {
            Hibernate.initialize(dayAssignment);
        }
        if (resource instanceof Machine) {
            loadMachine((Machine) resource);
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

    @Override
    public Date getTaskEnd() {
        if (task == null) {
            return null;
        }
        return task.getEndDate();
    }

    @Override
    public void setStartDate(Date date) {
        if (task != null) {
            task.setStartDate(date);
        }
    }

}
