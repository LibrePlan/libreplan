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

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.resources.entities.Resource;

/**
 * It represents an {@link Order} with its related information. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Order extends OrderLineGroup {

    public static Order create() {
        Order order = new Order();
        order.setNewObject(true);

        OrderLineGroup.setupOrderLineGroup(order);

        return order;
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public Order() {

    }

    private String responsible;

    // TODO turn into a many to one relationship when Customer entity is defined
    private String customer;

    private Boolean dependenciesConstraintsHavePriority;

    @NotNull
    private BaseCalendar calendar;

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public boolean isDeadlineBeforeStart() {
        return getDeadline() != null && getDeadline().before(getInitDate());
    }

    public List<OrderElement> getOrderElements() {
        return new ArrayList<OrderElement>(getChildren());
    }

    public TaskGroup getAssociatedTaskElement() {
        return (TaskGroup) super.getAssociatedTaskElement();
    }

    public List<TaskElement> getAllChildrenAssociatedTaskElements() {
        List<TaskElement> result = new ArrayList<TaskElement>();

        List<OrderElement> children = getAllChildren();
        for (OrderElement orderElement : children) {
            result.add(orderElement.getAssociatedTaskElement());
        }

        return result;
    }

    public List<TaskElement> getAssociatedTasks() {
        ArrayList<TaskElement> result = new ArrayList<TaskElement>();
        TaskGroup taskGroup = getAssociatedTaskElement();
        result.addAll(taskGroup.getChildren());
        return result;
    }

    public boolean isSomeTaskElementScheduled() {
        return isScheduled();
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "the order must have a init date")
    private boolean theOrderMustHaveStartDate() {
        return getInitDate() != null;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "deadline must be after start date")
    private boolean theDeadlineMustBeAfterStart() {
        return !this.isDeadlineBeforeStart();
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "At least one HoursGroup is needed for each OrderElement")
    private boolean atLeastOneHoursGroupForEachOrderElement() {
        for (OrderElement orderElement : this.getOrderElements()) {
            if (!orderElement.checkAtLeastOneHoursGroup()) {
                return false;
            }
        }
        return true;
    }

    public List<DayAssignment> getDayAssignments() {
        List<DayAssignment> dayAssignments = new ArrayList<DayAssignment>();
        for (OrderElement orderElement : getAllOrderElements()) {
            Set<TaskElement> taskElements = orderElement.getTaskElements();
            for (TaskElement taskElement : taskElements) {
                dayAssignments.addAll(taskElement.getDayAssignments());
            }
        }
        return dayAssignments;
    }

    private List<OrderElement> getAllOrderElements() {
        List<OrderElement> result = new ArrayList<OrderElement>(
                this
                .getChildren());
        for (OrderElement orderElement : this.getChildren()) {
            result.addAll(orderElement.getAllChildren());
        }
        return result;
    }

    public Set<Resource> getResources() {
        Set<Resource> resources = new HashSet<Resource>();
        for (DayAssignment dayAssignment : getDayAssignments()) {
            resources.add(dayAssignment.getResource());
        }
        return resources;
    }

    @Override
    protected void applyStartConstraintTo(Task task) {
        // the initDate of order don't imply a start constraint at a task
    }

    public boolean getDependenciesConstraintsHavePriority() {
        return dependenciesConstraintsHavePriority != null
                && dependenciesConstraintsHavePriority;
    }

    public void setDependenciesConstraintsHavePriority(
            boolean dependenciesConstraintsHavePriority) {
        this.dependenciesConstraintsHavePriority = dependenciesConstraintsHavePriority;
    }

    public void setCalendar(BaseCalendar calendar) {
        this.calendar = calendar;
    }

    public BaseCalendar getCalendar() {
        return calendar;
    }

}
