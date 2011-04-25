/*
 * This file is part of NavalPlan
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
package org.navalplanner.web.planner.reassign;

import static org.navalplanner.business.i18n.I18nHelper._;
import static org.zkoss.ganttz.util.LongOperationFeedback.and;

import java.util.ArrayList;
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
import org.navalplanner.business.resources.daos.IResourcesSearcher;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.web.planner.order.PlanningState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.adapters.IDomainAndBeansMapper;
import org.zkoss.ganttz.data.Dependency;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.ganttz.util.IAction;
import org.zkoss.ganttz.util.LongOperationFeedback;
import org.zkoss.ganttz.util.LongOperationFeedback.IBackGroundOperation;
import org.zkoss.ganttz.util.LongOperationFeedback.IDesktopUpdate;
import org.zkoss.ganttz.util.LongOperationFeedback.IDesktopUpdatesEmitter;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.util.Clients;

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
    private IResourcesSearcher resourcesSearcher;

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
                        final List<WithAssociatedEntity> reassignations = getReassignations(
                                context, configuration);
                        LongOperationFeedback.progressive(getDesktop(context),
                                reassignations(context, reassignations));
                    }
                });
    }

    private IBackGroundOperation<IDesktopUpdate> reassignations(
            final IContext<TaskElement> context,
            final List<WithAssociatedEntity> reassignations) {
        return new IBackGroundOperation<IDesktopUpdate>() {

            @Override
            public void doOperation(
                    final IDesktopUpdatesEmitter<IDesktopUpdate> updater) {
                updater.doUpdate(showStart(reassignations.size()));
                GanttDiagramGraph<Task, Dependency>.DeferedNotifier notifications = null;
                try {
                    GanttDiagramGraph<Task, Dependency> ganttDiagramGraph = context
                            .getGanttDiagramGraph();
                    notifications = ganttDiagramGraph
                            .manualNotificationOn(doReassignations(
                                    ganttDiagramGraph, reassignations, updater));
                } finally {
                    if (notifications != null) {
                        // null if error
                        updater.doUpdate(and(doNotifications(notifications),
                                reloadCharts(context), showEnd()));
                    } else {
                        updater.doUpdate(showEnd());
                    }
                }
            }

        };
    }

    private IAction doReassignations(final GanttDiagramGraph<Task, Dependency> diagramGraph,
            final List<WithAssociatedEntity> reassignations,
            final IDesktopUpdatesEmitter<IDesktopUpdate> updater) {
        return new IAction() {

            @Override
            public void doAction() {
                int i = 1;
                final int total = reassignations.size();
                for (final WithAssociatedEntity each : reassignations) {
                    Task ganttTask = each.ganntTask;
                    final GanttDate previousBeginDate = ganttTask
                            .getBeginDate();
                    final GanttDate previousEnd = ganttTask.getEndDate();

                    transactionService
                            .runOnReadOnlyTransaction(reassignmentTransaction(each));
                    diagramGraph.enforceRestrictions(each.ganntTask);
                    ganttTask.fireChangesForPreviousValues(previousBeginDate,
                            previousEnd);
                    updater.doUpdate(showCompleted(i, total));
                    i++;
                }
            }
        };
    }

    private IDesktopUpdate showStart(final int total) {
        return new IDesktopUpdate() {
            @Override
            public void doUpdate() {
                Clients.showBusy(_("Doing {0} reassignations", total), true);
            }
        };
    }

    private IDesktopUpdate showCompleted(final int number, final int total) {
        return new IDesktopUpdate() {

            @Override
            public void doUpdate() {
                Clients.showBusy(_("Done {0} of {1}", number, total), true);
            }
        };
    }

    private IDesktopUpdate reloadCharts(final IContext<?> context) {
        return new IDesktopUpdate() {
            @Override
            public void doUpdate() {
                context.reloadCharts();
            }
        };
    }

    private IDesktopUpdate doNotifications(
            final GanttDiagramGraph<Task, Dependency>.DeferedNotifier notifier) {
        return new IDesktopUpdate() {
            @Override
            public void doUpdate() {
                notifier.doNotifications();
            }
        };
    }

    private IDesktopUpdate showEnd() {
        return new IDesktopUpdate() {

            @Override
            public void doUpdate() {
                Clients.showBusy(null, false);
            }
        };
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
        t.reassignAllocationsWithNewResources(
                planningState.getCurrentScenario(), resourcesSearcher);
    }

    @Override
    public String getName() {
        return _("Reassign");
    }

    @Override
    public String getImage() {
        return "/common/img/ico_reassign.png";
    }

    private Desktop getDesktop(final IContext<TaskElement> context) {
        return context.getRelativeTo().getDesktop();
    }

}
