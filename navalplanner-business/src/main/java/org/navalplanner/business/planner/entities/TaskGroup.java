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

package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.AssertTrue;
import org.navalplanner.business.common.entities.ProgressType;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.workingday.IntraDayDate;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 */
public class TaskGroup extends TaskElement {

    public static TaskGroup create(TaskSource taskSource) {
        TaskGroup taskGroup = new TaskGroup();
        return create(taskGroup, taskSource);
    }

    private List<TaskElement> taskElements = new ArrayList<TaskElement>();

    private PlanningData planningData;

    /**
     * Constructor for hibernate. Do not use!
     */
    public TaskGroup() {

    }

    public BigDecimal getCriticalPathProgressByDuration() {
        if (planningData == null) {
            return BigDecimal.ZERO;
        }
        return planningData.getProgressByDuration();
    }

    public BigDecimal getCriticalPathProgressByNumHours() {
        if (planningData == null) {
            return BigDecimal.ZERO;
        }
        return planningData.getProgressByNumHours();
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "order element associated to a task group must be not null")
    private boolean theOrderElementMustBeNotNull() {
        return getOrderElement() != null;
    }

    public void addTaskElement(TaskElement task) {
        Validate.notNull(task);
        task.setParent(this);
        addTaskElement(taskElements.size(), task);
        Date newPossibleEndDate = task.getEndDate();
        if (getEndDate() == null
                || getEndDate().compareTo(newPossibleEndDate) < 0) {
            setEndDate(newPossibleEndDate);
        }
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

    public void remove(TaskElement taskElement) {
        taskElements.remove(taskElement);
    }

    @Override
    public Set<ResourceAllocation<?>> getSatisfiedResourceAllocations() {
        Set<ResourceAllocation<?>> result = new HashSet<ResourceAllocation<?>>();
        List<TaskElement> children = this.getChildren();
        for (TaskElement child : children) {
            result.addAll(child.getSatisfiedResourceAllocations());
        }
        return result;
    }

    @Override
    public Set<ResourceAllocation<?>> getAllResourceAllocations() {
        Set<ResourceAllocation<?>> result = new HashSet<ResourceAllocation<?>>();
        for (TaskElement each : this.getChildren()) {
            result.addAll(each.getAllResourceAllocations());
        }
        return result;
    }

    @Override
    protected IDatesHandler createDatesHandler(Scenario scenario) {
        return new IDatesHandler() {

            @Override
            public void moveTo(IntraDayDate newStartDate) {
                setIntraDayStartDate(newStartDate);
            }

            @Override
            public void resizeTo(IntraDayDate endDate) {
                setIntraDayEndDate(endDate);
            }

            @Override
            public void moveEndTo(IntraDayDate newEnd) {
                setIntraDayEndDate(newEnd);
            }
        };
    }

    public void setTaskChildrenTo(List<TaskElement> children) {
        Validate.noNullElements(children);
        int positionOnTaskElements = 0;
        for (int i = 0; i < children.size(); i++) {
            TaskElement element = children.get(i);
            element.setParent(this);
            if (positionOnTaskElements >= taskElements.size()) {
                taskElements.add(element);
            } else {
                while (positionOnTaskElements < taskElements.size()
                        && isMilestone(taskElements.get(positionOnTaskElements))) {
                    positionOnTaskElements++;
                }
                if (positionOnTaskElements >= taskElements.size()) {
                    taskElements.add(element);
                } else {
                    taskElements.set(positionOnTaskElements, element);
                }
            }
            positionOnTaskElements++;
        }
        ListIterator<TaskElement> listIterator = taskElements
                .listIterator(positionOnTaskElements);
        while (listIterator.hasNext()) {
            TaskElement current = listIterator.next();
            if (!isMilestone(current)) {
                listIterator.remove();
            }
        }
    }

    private boolean isMilestone(TaskElement t) {
        // it can be null since removed elements are nullified in the list
        return t != null && t.isMilestone();
    }

    @Override
    protected void initializeEndDate() {
        List<Date> endDates = getEndDates(getChildren());
        setEndDate(Collections.max(endDates));
    }

    private List<Date> getEndDates(Collection<? extends TaskElement> children) {
        List<Date> result = new ArrayList<Date>();
        for (TaskElement each : children) {
            Validate.notNull(each.getEndDate());
            result.add(each.getEndDate());
        }
        return result;
    }

    @Override
    protected boolean canBeResized() {
        return true;
    }

    @Override
    public boolean canBeExplicitlyResized() {
        return false;
    }

    @Override
    public boolean isMilestone() {
        return false;
    }

    @Override
    public boolean hasLimitedResourceAllocation() {
        return false;
    }

    public IntraDayDate getSmallestStartDateFromChildren() {
        return Collections.min(getChildrenStartDates());
    }

    private List<IntraDayDate> getChildrenStartDates() {
        List<IntraDayDate> result = new ArrayList<IntraDayDate>();
        for (TaskElement each : getChildren()) {
            result.add(each.getIntraDayStartDate());
        }
        return result;
    }

    public void updateCriticalPathProgress(List<Task> criticalPath) {
        Validate.isTrue(getParent() == null);
        if (planningData == null) {
            planningData = PlanningData.create(this);
        }
        planningData.update(criticalPath);
    }

    /**
     * For a root task, retrieves the progress selected by the progressType
     * If there's not progressType, return taskElement.advancePercentage
     *
     */
    public BigDecimal getAdvancePercentage(ProgressType progressType) {
        if (isTaskRoot(this)) {
            if (progressType.equals(ProgressType.CRITICAL_PATH_DURATION)) {
                return getCriticalPathProgressByDuration();
            }
            if (progressType.equals(ProgressType.CRITICAL_PATH_NUMHOURS)) {
                return getCriticalPathProgressByNumHours();
            }
        }
        return super.getAdvancePercentage();
    }

    private boolean isTaskRoot(TaskGroup taskGroup) {
        return taskGroup.getParent() == null;
    }

}