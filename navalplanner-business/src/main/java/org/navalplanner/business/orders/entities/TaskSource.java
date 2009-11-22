/*
 * This file is part of ###PROJECT_NAME###
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
package org.navalplanner.business.orders.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TaskSource extends BaseEntity {

    public static TaskSource create(OrderElement orderElement,
            List<HoursGroup> hoursGroups) {
        TaskSource result = create(new TaskSource(orderElement));
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
        public abstract TaskElement apply(ITaskSourceDAO taskSourceDAO);

        protected void saveTaskSource(ITaskSourceDAO taskSourceDAO,
                TaskSource taskSource) {
            taskSourceDAO.save(taskSource);
        }
    }

    static class TaskSourceMustBeAdded extends TaskSourceSynchronization {

        private final TaskSource taskSource;

        public TaskSourceMustBeAdded(TaskSource taskSource) {
            this.taskSource = taskSource;
        }

        @Override
        public TaskElement apply(ITaskSourceDAO taskSourceDAO) {
            Task result = Task.createTask(taskSource);
            taskSource.setTask(result);
            taskSourceDAO.save(taskSource);
            return result;
        }
    }

    static class TaskSourceForTaskModified extends TaskSourceSynchronization {

        private final TaskSource taskSource;

        TaskSourceForTaskModified(TaskSource taskSource) {
            this.taskSource = taskSource;
        }

        @Override
        public TaskElement apply(ITaskSourceDAO taskSourceDAO) {
            saveTaskSource(taskSourceDAO, taskSource);
            return taskSource.getTask();
        }
    }

    static abstract class TaskGroupSynchronization extends
            TaskSourceSynchronization {

        protected final TaskSource taskSource;

        private final List<TaskSourceSynchronization> synchronizations;

        TaskGroupSynchronization(TaskSource taskSource,
                List<TaskSourceSynchronization> synchronizations) {
            Validate.notNull(taskSource);
            Validate.notNull(synchronizations);
            this.taskSource = taskSource;
            this.synchronizations = synchronizations;
        }

        @Override
        public TaskElement apply(ITaskSourceDAO taskSourceDAO) {
            List<TaskElement> children = getChildren(taskSourceDAO);
            return apply(taskSourceDAO, children);
        }

        private List<TaskElement> getChildren(ITaskSourceDAO taskSourceDAO) {
            List<TaskElement> result = new ArrayList<TaskElement>();
            for (TaskSourceSynchronization each : synchronizations) {
                TaskElement t = each.apply(taskSourceDAO);
                if (t != null) {
                    // TaskSourceMustBeRemoved gives null
                    result.add(t);
                }
            }
            return result;
        }

        protected abstract TaskElement apply(ITaskSourceDAO taskSourceDAO,
                List<TaskElement> children);
    }

    static class TaskGroupMustBeAdded extends TaskGroupSynchronization {

        private TaskGroupMustBeAdded(TaskSource taskSource,
                List<TaskSourceSynchronization> synchronizations) {
            super(taskSource, synchronizations);
        }

        @Override
        protected TaskElement apply(ITaskSourceDAO taskSourceDAO,
                List<TaskElement> children) {
            TaskGroup result = TaskGroup.create(taskSource);
            for (TaskElement taskElement : children) {
                result.addTaskElement(taskElement);
            }
            taskSource.setTask(result);
            saveTaskSource(taskSourceDAO, taskSource);
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
                List<TaskElement> children) {
            TaskGroup taskGroup = (TaskGroup) taskSource.getTask();
            taskGroup.addChildren(children);
            taskSourceDAO.save(taskSource);
            return taskGroup;
        }
    }

    static class TaskSourceMustBeRemoved extends TaskSourceSynchronization {

        private final TaskSource taskSource;

        public TaskSourceMustBeRemoved(TaskSource taskSource) {
            this.taskSource = taskSource;
        }

        @Override
        public TaskElement apply(ITaskSourceDAO taskSourceDAO) {
            try {
                taskSourceDAO.remove(taskSource.getId());
            } catch (InstanceNotFoundException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

    }

    public static TaskSource withHoursGroupOf(OrderElement orderElement) {
        return create(new TaskSource(orderElement));
    }

    public static TaskSource createForGroup(OrderElement orderElement) {
        return create(new TaskSource(orderElement));
    }

    @NotNull
    private OrderElement orderElement;

    @NotNull
    private TaskElement task;

    private Set<HoursGroup> hoursGroups = new HashSet<HoursGroup>();

    public TaskSource() {
    }

    public TaskSource(OrderElement orderElement) {
        Validate.notNull(orderElement);
        this.setOrderElement(orderElement);
        this.setHoursGroups(new HashSet<HoursGroup>(orderElement
                .getHoursGroups()));
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

    private void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    public TaskElement getTask() {
        return task;
    }

    public OrderElement getOrderElement() {
        return orderElement;
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

    public void reloadTask(ITaskElementDAO taskElementDAO) {
        taskElementDAO.save(task);
    }

    public int getTotalHours() {
        int result = 0;
        for (HoursGroup each : hoursGroups) {
            result += each.getWorkingHours();
        }
        return result;
    }
}
