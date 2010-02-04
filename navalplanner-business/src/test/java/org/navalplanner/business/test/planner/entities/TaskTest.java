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

package org.navalplanner.business.test.planner.entities;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Test;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TaskTest {

    private Task task;
    private HoursGroup hoursGroup;

    public TaskTest() {
        hoursGroup = new HoursGroup();
        hoursGroup.setWorkingHours(3);
        Order order = new Order();
        order.setInitDate(new Date());
        OrderLine orderLine = OrderLine.create();
        order.add(orderLine);
        TaskSource taskSource = TaskSource.create(orderLine, Arrays
                .asList(hoursGroup));
        task = Task.createTask(taskSource);
    }

    @Test
    public void taskIsASubclassOfTaskElement() {
        assertTrue(task instanceof TaskElement);
    }

    @Test
    public void taskHasHoursSpecifiedAtOrderComingFromItsHoursGroup() {
        assertThat(task.getHoursSpecifiedAtOrder(), equalTo(hoursGroup.getWorkingHours()));
    }

    @Test
    public void taskMustHaveOneHoursGroup() {
        HoursGroup hoursGroup = task.getHoursGroup();
        assertNotNull(hoursGroup);
    }

    public static Task createValidTask() {
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setWorkingHours(3);
        OrderLine orderLine = OrderLine.create();
        Order order = new Order();
        order.setInitDate(new Date());
        order.add(orderLine);
        TaskSource taskSource = TaskSource.create(orderLine, Arrays
                .asList(hoursGroup));
        return Task.createTask(taskSource);
    }

    @Test
    public void addingEmptyResourceAllocationDoesntAddIt() {
        assertThat(task.getResourceAllocations().size(), equalTo(0));

        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation.create(task);
        task.addResourceAllocation(resourceAllocation);

        assertThat(task.getResourceAllocations().size(), equalTo(0));
    }

    @Test
    public void addingNoEmptyResourceAllocationAddsIt() {
        assertThat(task.getResourceAllocations().size(), equalTo(0));

        SpecificResourceAllocation resourceAllocation = stubResourceAllocationWithAssignedHours(
                task, 500);
        task.addResourceAllocation(resourceAllocation);
        assertThat(task.getResourceAllocations().size(), equalTo(1));
    }

    @Test
    public void taskRemoveResourceAllocation() {
        assertThat(task.getResourceAllocations().size(), equalTo(0));

        SpecificResourceAllocation resourceAllocation = stubResourceAllocationWithAssignedHours(
                task, 500);
        task.addResourceAllocation(resourceAllocation);

        assertThat(task.getResourceAllocations().size(), equalTo(1));

        task.removeResourceAllocation(resourceAllocation);
        assertThat(task.getResourceAllocations().size(), equalTo(0));
    }

    @Test
    public void aTaskWithoutAllocationsHasZeroAssignedHours() {
        assertThat(task.getAssignedHours(), equalTo(0));
    }

    @Test
    public void aTaskWithAllocationsReturnsTheSumOfItsAllocations() {
        task.addResourceAllocation(stubResourceAllocationWithAssignedHours(
                task, 5));
        task.addResourceAllocation(stubResourceAllocationWithAssignedHours(
                task, 3));
        assertThat(task.getAssignedHours(), equalTo(8));
    }

    @Test
    public void theDaysBetweenIsCalculatedBasedOnlyOnDatesNotHours() {
        task.setStartDate(new DateTime(2008, 10, 5, 23, 0, 0, 0).toDate());
        task.setEndDate(new DateTime(2008, 10, 6, 1, 0, 0, 0).toDate());
        assertThat(task.getDaysDuration(), equalTo(1));
    }

    private SpecificResourceAllocation stubResourceAllocationWithAssignedHours(
            Task task,
            int hours) {
        SpecificResourceAllocation resourceAllocation = createNiceMock(SpecificResourceAllocation.class);
        expect(resourceAllocation.getAssignedHours()).andReturn(hours)
                .anyTimes();
        expect(resourceAllocation.getTask()).andReturn(task).anyTimes();
        expect(resourceAllocation.hasAssignments()).andReturn(true).anyTimes();
        replay(resourceAllocation);
        return resourceAllocation;
    }

}