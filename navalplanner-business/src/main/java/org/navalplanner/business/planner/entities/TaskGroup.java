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

package org.navalplanner.business.planner.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.AssertTrue;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderLine;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TaskGroup extends TaskElement {

    public static TaskGroup create() {
        TaskGroup taskGroup = new TaskGroup();
        taskGroup.setNewObject(true);
        return taskGroup;
    }

    private List<TaskElement> taskElements = new ArrayList<TaskElement>();

    /**
     * Constructor for hibernate. Do not use!
     */
    public TaskGroup() {

    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "order element associated to a task group must be not null")
    private boolean theOrderElementMustBeNotNull() {
        return getOrderElement() != null;
    }

    public void addTaskElement(TaskElement task) {
        addTaskElement(taskElements.size(), task);
    }

    public void addTaskElement(Integer index, TaskElement task) {
        Validate.notNull(task);
        task.setParent(this);
        taskElements.add(index, task);
    }

    @Override
    public List<TaskElement> getChildren() {
        return Collections.unmodifiableList(taskElements);
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public Integer defaultWorkHours() {
        return getOrderElement().getWorkHours();
    }

    public void remove(TaskElement taskElement) {
        taskElements.remove(taskElement);
    }

    public boolean canBeMerged() {
        return isAssociatedWithAnOrderLine() && !taskElements.isEmpty()
                && allSubTaskGroupsCanBeMerged()
                && allChildrenHaveTheSameHoursGroup()
                && sumOfHoursIsEqualToWorkingHours();
    }

    private boolean allSubTaskGroupsCanBeMerged() {
        for (TaskElement t : taskElements) {
            if (t instanceof TaskGroup) {
                TaskGroup group = (TaskGroup) t;
                if (!group.canBeMerged()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean sumOfHoursIsEqualToWorkingHours() {
        int sum = 0;
        for (TaskElement taskElement : taskElements) {
            sum += taskElement.getWorkHours();
        }
        return sum == getWorkHours();
    }

    private boolean allChildrenHaveTheSameHoursGroup() {
        HoursGroup hoursGroup = null;
        for (TaskElement taskElement : taskElements) {
            HoursGroup current = getHoursGroupFor(taskElement);
            if (current == null) {
                return false;
            }
            if (hoursGroup == null) {
                hoursGroup = current;
            }
            if (!current.equals(hoursGroup)) {
                return false;
            }
        }
        return true;
    }

    private HoursGroup getHoursGroupFor(TaskElement taskElement) {
        if (taskElement instanceof Task) {
            Task t = (Task) taskElement;
            return t.getHoursGroup();
        }
        return ((TaskGroup) taskElement).inferHoursGroupFromChildren();
    }

    private boolean isAssociatedWithAnOrderLine() {
        return getOrderElement() instanceof OrderLine;
    }

    public Task merge() {
        if (!canBeMerged()) {
            throw new IllegalStateException(
                    "merge must not be called on a TaskGroup such canBeMerged returns false");
        }
        HoursGroup hoursGroup = inferHoursGroupFromChildren();
        Task result = Task.createTask(hoursGroup);
        result.copyPropertiesFrom(this);
        result.shareOfHours = this.shareOfHours;
        copyDependenciesTo(result);
        copyParenTo(result);
        return result;
    }

    private HoursGroup inferHoursGroupFromChildren() {
        TaskElement taskElement = getChildren().get(0);
        if (taskElement instanceof Task) {
            Task t = (Task) taskElement;
            return t.getHoursGroup();
        } else {
            TaskGroup group = (TaskGroup) taskElement;
            return group.inferHoursGroupFromChildren();
        }
    }

    @Override
    public Set<ResourceAllocation<?>> getResourceAllocations() {
        Set<ResourceAllocation<?>> result = new HashSet<ResourceAllocation<?>>();

        List<TaskElement> children = this.getChildren();
        for (TaskElement child : children) {
            result.addAll(child.getResourceAllocations());
        }

        return result;
    }

    @Override
    protected void moveAllocations() {
        // do nothing
    }
}
