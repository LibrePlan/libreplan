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
package org.libreplan.business.orders.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.entities.SchedulingState.Type;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.daos.ITaskSourceDAO;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.util.deepcopy.OnCopy;
import org.libreplan.business.util.deepcopy.Strategy;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TaskSource extends BaseEntity {

    public static TaskSource create(SchedulingDataForVersion schedulingState,
            List<HoursGroup> hoursGroups) {
        TaskSource result = create(new TaskSource(schedulingState));
        result.setHoursGroups(new HashSet<HoursGroup>(hoursGroups));
        return result;
    }

    public static TaskSourceSynchronization mustAdd(
            TaskSource taskSource) {
        return new TaskSourceMustBeAdded(taskSource);
    }

    public static TaskSourceSynchronization mustAddGroup(TaskSource taskSource,
            List<TaskSourceSynchronization> childrenOfGroup) {
        return new TaskGroupMustBeAdded(taskSource, childrenOfGroup);
    }

    public static TaskSourceSynchronization mustRemove(TaskSource taskSource) {
        return new TaskSourceMustBeRemoved(taskSource);
    }

    public interface IOptionalPersistence {

        public void save(TaskSource taskSource);

        public void remove(TaskSource taskSource);
    }

    public static IOptionalPersistence persistTaskSources(
            ITaskSourceDAO taskSourceDAO) {
        return new RealPersistence(taskSourceDAO, true);
    }

    public static IOptionalPersistence persistButDontRemoveTaskSources(
            ITaskSourceDAO taskSourceDAO) {
        return new RealPersistence(taskSourceDAO, false);
    }

    public static IOptionalPersistence dontPersist() {
        return new NoPersistence();
    }

    private static class RealPersistence implements IOptionalPersistence {

        private final ITaskSourceDAO taskSourceDAO;

        private final boolean removeTaskSources;

        public RealPersistence(ITaskSourceDAO taskSourceDAO,
                boolean removeTaskSources) {
            Validate.notNull(taskSourceDAO);
            this.taskSourceDAO = taskSourceDAO;
            this.removeTaskSources = removeTaskSources;
        }

        @Override
        public void save(TaskSource taskSource) {
            taskSourceDAO.saveWithoutValidating(taskSource);
        }

        @Override
        public void remove(TaskSource taskSource) {
            if (!removeTaskSources) {
                return;
            }
            try {
                taskSourceDAO.remove(taskSource.getId());
            } catch (InstanceNotFoundException e) {
                throw new RuntimeException(e);
            }
            // Flushing is required in order to avoid violation of
            // unique
            // constraint. If flush is not done and there is a task
            // source
            // that must be removed and another is created for the same
            // order element the unique constraint
            // "tasksource_orderelement_key" would be violated by
            // hibernate
            taskSourceDAO.flush();
        }

    }

    private static class NoPersistence implements IOptionalPersistence {

        @Override
        public void save(TaskSource taskSource) {
        }

        @Override
        public void remove(TaskSource taskSource) {
        }

    }

    public static abstract class TaskSourceSynchronization {

        public abstract TaskElement apply(IOptionalPersistence persistence);
    }

    static class TaskSourceMustBeAdded extends TaskSourceSynchronization {

        private final TaskSource taskSource;

        public TaskSourceMustBeAdded(TaskSource taskSource) {
            this.taskSource = taskSource;
        }

        @Override
        public TaskElement apply(IOptionalPersistence persistence) {
            Task result = Task.createTask(taskSource);
            taskSource.setTask(result);
            persistence.save(taskSource);
            return result;
        }
    }

    static class TaskSourceForTaskModified extends TaskSourceSynchronization {

        private final TaskSource taskSource;

        TaskSourceForTaskModified(TaskSource taskSource) {
            this.taskSource = taskSource;
        }

        @Override
        public TaskElement apply(IOptionalPersistence persistence) {
            updateTaskWithOrderElement(taskSource.getTask(), taskSource.getOrderElement());
            updatePositionRestrictions();
            persistence.save(taskSource);
            return taskSource.getTask();
        }

        private void updatePositionRestrictions() {
            if (hasSomeAllocationDone(taskSource.getTask())) {
                return;
            }
            taskSource.getOrderElement().updatePositionConstraintOf(
                    (Task) taskSource.getTask());
        }


    }

    private static boolean hasSomeAllocationDone(TaskElement taskElement) {
        return !taskElement.getAllResourceAllocations().isEmpty();
    }

    /**
     * This method updates the task with a name and a start date. The start date
     * and end date should never be null, unless it's legacy data
     * @param task
     * @param orderElement
     */
    private static void updateTaskWithOrderElement(TaskElement task,
            OrderElement orderElement) {
        task.setName(orderElement.getName());
        Date orderInitDate = orderElement.getOrder().getInitDate();
        if (task.getIntraDayStartDate() == null && orderInitDate != null) {
            task.setStartDate(orderInitDate);
        }
        task.initializeDatesIfNeeded();
        task.updateDeadlineFromOrderElement();
    }

    public static abstract class TaskGroupSynchronization extends
            TaskSourceSynchronization {

        protected final TaskSource taskSource;

        private final List<TaskSourceSynchronization> synchronizations;

        protected TaskGroupSynchronization(TaskSource taskSource,
                List<TaskSourceSynchronization> synchronizations) {
            Validate.notNull(taskSource);
            Validate.notNull(synchronizations);
            this.taskSource = taskSource;
            this.synchronizations = synchronizations;
        }

        protected void setTask(TaskSource taskSource, TaskGroup result) {
            taskSource.setTask(result);
        }

        @Override
        public TaskElement apply(IOptionalPersistence persistence) {
            List<TaskElement> children = getChildren(persistence);
            return apply(children, persistence);
        }

        private List<TaskElement> getChildren(IOptionalPersistence persistence) {
            List<TaskElement> result = new ArrayList<TaskElement>();
            for (TaskSourceSynchronization each : synchronizations) {
                TaskElement t = each.apply(persistence);
                if (t != null) {
                    // TaskSourceMustBeRemoved gives null
                    result.add(t);
                }
            }
            return result;
        }

        protected abstract TaskElement apply(List<TaskElement> children,
                IOptionalPersistence persistence);
    }

    static class TaskGroupMustBeAdded extends TaskGroupSynchronization {

        private TaskGroupMustBeAdded(TaskSource taskSource,
                List<TaskSourceSynchronization> synchronizations) {
            super(taskSource, synchronizations);
        }

        @Override
        protected TaskElement apply(List<TaskElement> children,
                IOptionalPersistence persistence) {
            TaskGroup result = TaskGroup.create(taskSource);
            for (TaskElement taskElement : children) {
                result.addTaskElement(taskElement);
            }
            taskSource.setTask(result);
            persistence.save(taskSource);
            return result;
        }

    }

    static class TaskSourceForTaskGroupModified extends
            TaskGroupSynchronization {

        TaskSourceForTaskGroupModified(TaskSource taskSource,
                List<TaskSourceSynchronization> synchronizations) {
            super(taskSource, synchronizations);
        }

        @Override
        protected TaskElement apply(List<TaskElement> children,
                IOptionalPersistence persistence) {
            TaskGroup taskGroup = (TaskGroup) taskSource.getTask();
            taskGroup.setTaskChildrenTo(children);
            updateTaskWithOrderElement(taskGroup, taskSource.getOrderElement());
            persistence.save(taskSource);
            return taskGroup;
        }
    }

    public static class TaskSourceMustBeRemoved extends TaskSourceSynchronization {

        private final TaskSource taskSource;

        public TaskSourceMustBeRemoved(TaskSource taskSource) {
            this.taskSource = taskSource;
        }

        @Override
        public TaskElement apply(IOptionalPersistence optionalPersistence) {
            taskSource.getTask().detach();
            optionalPersistence.remove(taskSource);
            return null;
        }

    }

    public static TaskSource withHoursGroupOf(
            SchedulingDataForVersion schedulingState) {
        return create(new TaskSource(schedulingState));
    }

    public static TaskSource createForGroup(
            SchedulingDataForVersion schedulingState) {
        return create(new TaskSource(schedulingState));
    }

    @NotNull
    private TaskElement task;

    private SchedulingDataForVersion schedulingData;

    @OnCopy(Strategy.SHARE_COLLECTION_ELEMENTS)
    private Set<HoursGroup> hoursGroups = new HashSet<HoursGroup>();

    public TaskSource() {
    }

    public TaskSource(SchedulingDataForVersion schedulingState) {
        Validate.notNull(schedulingState);
        Validate.notNull(schedulingState.getOrderElement());
        this.schedulingData = schedulingState;
        OrderElement orderElement = schedulingState.getOrderElement();
        Type orderElementType = orderElement
                .getSchedulingState().getType();
        if (orderElementType == SchedulingState.Type.SCHEDULING_POINT) {
            this.setHoursGroups(new HashSet<HoursGroup>(orderElement
                    .getHoursGroups()));
        }
    }

    public TaskSourceSynchronization withCurrentHoursGroup(
            List<HoursGroup> hoursGroups) {
        setHoursGroups(new HashSet<HoursGroup>(hoursGroups));
        return new TaskSourceForTaskModified(this);
    }

    public TaskSourceSynchronization modifyGroup(
            List<TaskSourceSynchronization> childrenOfGroup) {
        return new TaskSourceForTaskGroupModified(this, childrenOfGroup);
    }

    private void setTask(TaskElement task) {
        this.task = task;
    }

    @Valid
    public TaskElement getTask() {
        return task;
    }

    public OrderElement getOrderElement() {
        return schedulingData.getOrderElement();
    }

    private void setHoursGroups(Set<HoursGroup> hoursGroups) {
        this.hoursGroups = hoursGroups;
    }

    public Set<HoursGroup> getHoursGroups() {
        return hoursGroups;
    }

    public List<AggregatedHoursGroup> getAggregatedByCriterions() {
        return AggregatedHoursGroup.aggregate(hoursGroups);
    }

    public void reattachTask(ITaskElementDAO taskElementDAO) {
        taskElementDAO.reattach(task);
    }

    public int getTotalHours() {
        int result = 0;
        for (HoursGroup each : hoursGroups) {
            result += each.getWorkingHours();
        }
        return result;
    }

    public void detachAssociatedTaskFromParent() {
        task.detach();
    }
}
