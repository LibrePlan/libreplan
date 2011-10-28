/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.web.planner.limiting.allocation;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.orders.daos.IHoursGroupDAO;
import org.libreplan.business.orders.entities.AggregatedHoursGroup;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.daos.ITaskSourceDAO;
import org.libreplan.business.planner.entities.GenericResourceAllocation;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.libreplan.business.resources.daos.ICriterionDAO;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.ResourceEnum;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;

/**
 * Provides logical operations for limiting resource assignations in @{Task}
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LimitingResourceAllocationModel implements ILimitingResourceAllocationModel {

    @Autowired
    private IHoursGroupDAO hoursGroupDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ITaskElementDAO taskDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    private IContextWithPlannerTask<TaskElement> context;

    private Task task;

    private PlanningState planningState;

    private List<LimitingAllocationRow> limitingAllocationRows = new ArrayList<LimitingAllocationRow>();

    private LimitingResourceAllocationController limitingResourceAllocationController;

    @Override
    @Transactional(readOnly=true)
    public void init(IContextWithPlannerTask<TaskElement> context, Task task,
            PlanningState planningState) {
        this.context = context;
        this.task = task;
        this.planningState = planningState;

        initializeCriteria(task);
        limitingAllocationRows = LimitingAllocationRow.toRows(task);
    }

    private void initializeCriteria(Task task) {
        for (ResourceAllocation<?> each: task.getLimitingResourceAllocations()) {
            if (isGeneric(each)) {
                initializeCriteria((GenericResourceAllocation) each);
            }
        }
    }

    private boolean isGeneric(ResourceAllocation<?> resourceAllocation) {
        return resourceAllocation instanceof GenericResourceAllocation;
    }

    private void initializeCriteria(GenericResourceAllocation generic) {
        for (Criterion each : generic.getCriterions()) {
            initializeCriterion(each);
        }
    }

    private void initializeCriterion(Criterion criterion) {
        criterionDAO.reattach(criterion);
        Hibernate.initialize(criterion.getType());
    }

    @Override
    @Transactional(readOnly=true)
    public Integer getOrderHours() {
        if (task == null) {
            return 0;
        }
        return AggregatedHoursGroup.sum(getHoursAggregatedByCriteria());
    }

    @Override
    @Transactional(readOnly = true)
    public void addGeneric(ResourceEnum resourceType,
            Collection<? extends Criterion> criteria,
            Collection<? extends Resource> resources) {

        if (resources.isEmpty()) {
            getMessagesForUser()
                    .showMessage(Level.ERROR,
                            _("there are no resources for required criteria: {0}. " +
                                    "So the generic allocation can't be added",
                            Criterion.getCaptionFor(resourceType, criteria)));
        }

        if (resources.size() >= 1) {
            if (planningState != null) {
                planningState.reassociateResourcesWithSession();
            }
            addGenericResourceAllocation(resourceType, criteria,
                    reloadResources(resources));
        }
    }

    private List<Resource> reloadResources(
            Collection<? extends Resource> resources) {
        List<Resource> result = new ArrayList<Resource>();
        for (Resource each: resources) {
            result.add(resourceDAO.findExistingEntity(each.getId()));
        }
        return result;
    }

    private IMessagesForUser getMessagesForUser() {
        return limitingResourceAllocationController.getMessagesForUser();
    }

    private void addGenericResourceAllocation(ResourceEnum resourceType,
            Collection<? extends Criterion> criteria,
            Collection<? extends Resource> resources) {

        if (isNew(criteria, resources)) {
            limitingAllocationRows.clear();
            limitingAllocationRows.add(LimitingAllocationRow.create(
                    resourceType, criteria, resources, task,
                    LimitingAllocationRow.DEFAULT_PRIORITY));
        }
    }

    private boolean isNew(Collection<? extends Criterion> criteria,
            Collection<? extends Resource> resources) {
        LimitingAllocationRow allocationRow = getLimitingAllocationRow();

        if (allocationRow == null || allocationRow.isSpecific()) {
            return true;
        }

        Set<Long> allocatedResourcesIds = allocationRow.getResourcesIds();
        for (Resource each: resources) {
            if (!allocatedResourcesIds.contains(each.getId())) {
                return true;
            }
        }

        Set<Long> allocatedCriteriaIds = allocationRow.getCriteriaIds();
        for (Criterion each: criteria) {
            if (!allocatedCriteriaIds.contains(each.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public void addSpecific(Collection<? extends Resource> resources) {

        if (!areAllLimitingResources(resources)) {
            getMessagesForUser().showMessage(Level.ERROR,
                    _("All resources must be limiting. "));
            return;
        }

        if (resources.size() >= 1) {
            if (planningState != null) {
                planningState.reassociateResourcesWithSession();
            }
            List<Resource> reloaded = reloadResources(
                    Collections.singleton(getFirstChild(resources)));
            addSpecificResourceAllocation(getFirstChild(reloaded));
        }
    }

    private boolean areAllLimitingResources(
            Collection<? extends Resource> resources) {
        for (Resource resource : resources) {
            if (!resource.isLimitingResource()) {
                return false;
            }
        }
        return true;
    }

    public Resource getFirstChild(Collection<? extends Resource> collection) {
        return collection.iterator().next();
    }

    private void addSpecificResourceAllocation(Resource resource) {
        if (isNew(resource)) {
            limitingAllocationRows.clear();
            LimitingAllocationRow allocationRow = LimitingAllocationRow.create(
                    resource, task, LimitingAllocationRow.DEFAULT_PRIORITY);
            limitingAllocationRows.add(allocationRow);
        }
    }

    private boolean isNew(Resource resource) {
        LimitingAllocationRow allocationRow = getLimitingAllocationRow();

        if (allocationRow == null || allocationRow.isGeneric()) {
            return true;
        }

        final Resource taskResource = getAssociatedResource();
        if (taskResource != null) {
            return (!resource.getId().equals(taskResource.getId()));
        }
        return true;
    }

    private Resource getAssociatedResource() {
        ResourceAllocation<?> resourceAllocation = getAssociatedResourceAllocation();
        if (resourceAllocation != null) {
            List<Resource> resources = resourceAllocation.getAssociatedResources();
            if (resources != null && resources.size() >= 1) {
                return (Resource) resources.iterator().next();
            }
        }
        return null;
    }

    private ResourceAllocation<?> getAssociatedResourceAllocation() {
        LimitingAllocationRow allocationRow = getLimitingAllocationRow();
        if (allocationRow != null) {
            return allocationRow.getResourceAllocation();
        }
        return null;
    }

    private LimitingAllocationRow getLimitingAllocationRow() {
        if (limitingAllocationRows.size() >= 1) {
            return limitingAllocationRows.get(0);
        }
        return null;
    }

    @Override
    public List<LimitingAllocationRow> getResourceAllocationRows() {
        return Collections.unmodifiableList(limitingAllocationRows);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AggregatedHoursGroup> getHoursAggregatedByCriteria() {
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

    /**
     * Re-attach {@link TaskSource}
     */
    private void reattachTaskSource() {
        TaskSource taskSource = task.getTaskSource();
        taskSourceDAO.reattach(taskSource);
        Set<HoursGroup> hoursGroups = taskSource.getHoursGroups();
        for (HoursGroup hoursGroup : hoursGroups) {
            reattachHoursGroup(hoursGroup);
        }
    }

    private void reattachHoursGroup(HoursGroup hoursGroup) {
        hoursGroupDAO.reattachUnmodifiedEntity(hoursGroup);
        hoursGroup.getPercentage();
        reattachCriteria(hoursGroup.getValidCriterions());
    }

    private void reattachCriteria(Set<Criterion> criterions) {
        for (Criterion criterion : criterions) {
            reattachCriterion(criterion);
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

    @Override
    @Transactional(readOnly=true)
    public void confirmSave() {
        applyAllocationWithDateChangesNotification(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                taskDAO.reattach(task);

                ResourceAllocation<?> resourceAllocation = getAssociatedResourceAllocation();
                if (resourceAllocation != null) {
                    if (resourceAllocation.isNewObject()) {
                        addAssociatedLimitingResourceQueueElement(task,
                                resourceAllocation);
                    } else {
                        if (!resourceAllocation.hasAssignments()) {
                            task.resizeToHours(resourceAllocation
                                    .getIntendedTotalHours());
                        }
                    }
                }
                taskDAO.save(task);
                return null;
            }
        });
    }

    private void applyAllocationWithDateChangesNotification(
            IOnTransaction<?> allocationDoer) {
        if (context != null) {
            org.zkoss.ganttz.data.Task ganttTask = context.getTask();
            GanttDate previousStartDate = ganttTask.getBeginDate();
            GanttDate previousEnd = ganttTask.getEndDate();
            transactionService.runOnReadOnlyTransaction(allocationDoer);
            ganttTask.fireChangesForPreviousValues(previousStartDate,
                    previousEnd);
        } else {
            // Update hours of a Task from Limiting Resource view
            transactionService.runOnReadOnlyTransaction(allocationDoer);
        }
    }

    private void addAssociatedLimitingResourceQueueElement(Task task, ResourceAllocation<?> resourceAllocation) {
        LimitingResourceQueueElement element = LimitingResourceQueueElement.create();
        resourceAllocation.setLimitingResourceQueueElement(element);
        task.setResourceAllocation(resourceAllocation);
    }

    @Override
    public void setLimitingResourceAllocationController(
            LimitingResourceAllocationController limitingResourceAllocationController) {
        this.limitingResourceAllocationController = limitingResourceAllocationController;
    }

}
