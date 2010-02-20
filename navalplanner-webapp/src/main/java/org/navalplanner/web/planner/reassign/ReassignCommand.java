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
package org.navalplanner.web.planner.reassign;

import static org.navalplanner.business.i18n.I18nHelper._;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.web.planner.order.PlanningState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.adapters.IDomainAndBeansMapper;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.extensions.IContext;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReassignCommand implements IReassignCommand {

    private PlanningState planningState;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    public interface IConfigurationResult {
        public void result(ReassignConfiguration configuration);
    }

    @Override
    public void setState(PlanningState planningState) {
        Validate.notNull(planningState);
        this.planningState = planningState;
    }

    @Override
    public void doAction(final IContext<TaskElement> context) {
        ReassignController.openOn(context.getRelativeTo(),
                new IConfigurationResult() {
                    @Override
                    public void result(final ReassignConfiguration configuration) {
                        List<WithAssociatedEntity> reassignations = getReassignations(
                                context, configuration);
                        doReassignations(reassignations);
                        context.reloadCharts();
                    }
                });
    }

    private static class WithAssociatedEntity {
        static WithAssociatedEntity create(
                IDomainAndBeansMapper<TaskElement> mapper, Task each) {
            return new WithAssociatedEntity(mapper
                    .findAssociatedDomainObject(each), each);
        }

        private TaskElement domainEntity;

        private Task ganntTask;

        WithAssociatedEntity(TaskElement domainEntity, Task ganntTask) {
            Validate.notNull(domainEntity);
            Validate.notNull(ganntTask);
            this.domainEntity = domainEntity;
            this.ganntTask = ganntTask;
        }

    }

    private List<WithAssociatedEntity> getReassignations(
            IContext<TaskElement> context, ReassignConfiguration configuration) {
        Validate.notNull(configuration);
        List<Task> taskToReassign = configuration.filterForReassignment(context
                .getTasksOrderedByStartDate());
        return withEntities(context.getMapper(), taskToReassign);
    }

    private List<WithAssociatedEntity> withEntities(
            IDomainAndBeansMapper<TaskElement> mapper,
            List<Task> forReassignment) {
        List<WithAssociatedEntity> result = new ArrayList<WithAssociatedEntity>();
        for (Task each : forReassignment) {
            result.add(WithAssociatedEntity.create(mapper, each));
        }
        return result;
    }

    private void doReassignations(List<WithAssociatedEntity> reassignations) {
        for (final WithAssociatedEntity each : reassignations) {
            Date previousBeginDate = each.ganntTask.getBeginDate();
            long previousLength = each.ganntTask.getLengthMilliseconds();
            transactionService
                    .runOnReadOnlyTransaction(reassignmentTransaction(each));
            each.ganntTask.fireChangesForPreviousValues(previousBeginDate,
                    previousLength);
            each.ganntTask.reloadResourcesText();
        }
    }

    private IOnTransaction<Void> reassignmentTransaction(
            final WithAssociatedEntity withAssociatedEntity) {
        return new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                reattach(withAssociatedEntity);
                reassign(withAssociatedEntity.domainEntity);
                return null;
            }
        };
    }

    private void reattach(WithAssociatedEntity each) {
        planningState.reassociateResourcesWithSession();
        Set<Long> idsOfTypesAlreadyAttached = new HashSet<Long>();
        taskElementDAO.reattach(each.domainEntity);
        Set<ResourceAllocation<?>> resourceAllocations = each.domainEntity
                .getSatisfiedResourceAllocations();
        List<GenericResourceAllocation> generic = ResourceAllocation.getOfType(
                GenericResourceAllocation.class, resourceAllocations);
        reattachCriterionTypesToAvoidLazyInitializationExceptionOnType(
                idsOfTypesAlreadyAttached, generic);
    }

    private void reattachCriterionTypesToAvoidLazyInitializationExceptionOnType(
            Set<Long> idsOfTypesAlreadyAttached,
            List<GenericResourceAllocation> generic) {
        for (GenericResourceAllocation eachGenericAllocation : generic) {
            Set<Criterion> criterions = eachGenericAllocation.getCriterions();
            for (Criterion eachCriterion : criterions) {
                CriterionType type = eachCriterion.getType();
                if (!idsOfTypesAlreadyAttached.contains(type.getId())) {
                    idsOfTypesAlreadyAttached.add(type.getId());
                    criterionTypeDAO.reattachUnmodifiedEntity(type);
                }
            }
        }
    }


    private void reassign(TaskElement taskElement) {
        org.navalplanner.business.planner.entities.Task t = (org.navalplanner.business.planner.entities.Task) taskElement;
        t.reassignAllocationsWithNewResources(resourceDAO);
    }

    @Override
    public String getName() {
        return _("Reassign");
    }

    @Override
    public String getImage() {
        return "/common/img/ico_reassign.png";
    }

}
