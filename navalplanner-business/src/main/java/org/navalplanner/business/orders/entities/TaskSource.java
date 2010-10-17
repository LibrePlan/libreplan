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
package org.navalplanner.business.orders.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.entities.SchedulingState.Type;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.util.deepcopy.OnCopy;
import org.navalplanner.business.util.deepcopy.Strategy;

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

    public static abstract class TaskSourceSynchronization {

        public TaskElement apply(ITaskSourceDAO taskSourceDAO) {
            return apply(taskSourceDAO, true);
        }

        public abstract TaskElement apply(ITaskSourceDAO taskSourceDAO,
                boolean preexistent);
    }

    static class TaskSourceMustBeAdded extends TaskSourceSynchronization {

        private final TaskSource taskSource;

        public TaskSourceMustBeAdded(TaskSource taskSource) {
            this.taskSource = taskSource;
        }

        @Override
        public TaskElement apply(ITaskSourceDAO taskSourceDAO,
                boolean preexistent) {
            Task result = Task.createTask(taskSource);
            taskSource.setTask(result);
            taskSourceDAO.saveWithoutValidating(taskSource);
            return result;
        }
    }

    static class TaskSourceForTaskModified extends TaskSourceSynchronization {

        private final TaskSource taskSource;

        TaskSourceForTaskModified(TaskSource taskSource) {
            this.taskSource = taskSource;
        }

        @Override
        public TaskElement apply(ITaskSourceDAO taskSourceDAO,
                boolean preeexistent) {
            updateTaskWithOrderElement(taskSource.getTask(), taskSource.getOrderElement());
            taskSourceDAO.saveWithoutValidating(taskSource);
            return taskSource.getTask();
        }

    }

    /**
     * This method updates the task with a name, scheduling strategy, start date
     * and end date. The start date and end date should never be null, unless
     * it's legacy data
     *
     * @param task
     * @param orderElement
     */
    private static void updateTaskWithOrderElement(TaskElement task,
            OrderElement orderElement) {

        task.setName(orderElement.getName());
        task.setForwardScheduling(orderElement.isForwardScheduling());

        if (task.isForwardScheduling()) {
            if (task.getStartDate() == null) {
                task.setStartDate(orderElement.getOrder().getInitDate());
            }
            if (task.getSatisfiedResourceAllocations().isEmpty()) {
                task.setEndDate(null);
            }
        } else {
            if (task.getEndDate() == null) {
                task.setEndDate(orderElement.getOrder().getDeadline());
            }
        }
        task.initializeEndDateIfDoesntExist();
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
        public TaskElement apply(ITaskSourceDAO taskSourceDAO,
                boolean preexistent) {
            List<TaskElement> children = getChildren(taskSourceDAO, preexistent);
            return apply(taskSourceDAO, children, preexistent);
        }

        private List<TaskElement> getChildren(ITaskSourceDAO taskSourceDAO,
                boolean preexistent) {
            List<TaskElement> result = new ArrayList<TaskElement>();
            for (TaskSourceSynchronization each : synchronizations) {
                TaskElement t = each.apply(taskSourceDAO, preexistent);
                if (t != null) {
                    // TaskSourceMustBeRemoved gives null
                    result.add(t);
                }
            }
            return result;
        }

        protected abstract TaskElement apply(ITaskSourceDAO taskSourceDAO,
                List<TaskElement> children, boolean preexistent);
    }

    static class TaskGroupMustBeAdded extends TaskGroupSynchronization {

        private TaskGroupMustBeAdded(TaskSource taskSource,
                List<TaskSourceSynchronization> synchronizations) {
            super(taskSource, synchronizations);
        }

        @Override
        protected TaskElement apply(ITaskSourceDAO taskSourceDAO,
                List<TaskElement> children, boolean preexistent) {
            TaskGroup result = TaskGroup.create(taskSource);
            for (TaskElement taskElement : children) {
                result.addTaskElement(taskElement);
            }
            taskSource.setTask(result);
            taskSourceDAO.saveWithoutValidating(taskSource);
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
        protected TaskElement apply(ITaskSourceDAO taskSourceDAO,
                List<TaskElement> children, boolean preexistent) {
            TaskGroup taskGroup = (TaskGroup) taskSource.getTask();
            taskGroup.setTaskChildrenTo(children);
            updateTaskWithOrderElement(taskGroup, taskSource.getOrderElement());
            taskSourceDAO.saveWithoutValidating(taskSource);
            return taskGroup;
        }
    }

    public static class TaskSourceMustBeRemoved extends TaskSourceSynchronization {

        private final TaskSource taskSource;

        public TaskSourceMustBeRemoved(TaskSource taskSource) {
            this.taskSource = taskSource;
        }

        @Override
        public TaskElement apply(ITaskSourceDAO taskSourceDAO,
                boolean preexistent) {
            taskSource.getTask().detachFromDependencies();
            taskSource.getTask().detachFromParent();
            if (preexistent) {
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
