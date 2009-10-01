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

package org.navalplanner.business.test.planner.entities;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.planner.entities.ResourceAllocation;
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
        task = Task.createTask(hoursGroup);
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

    public static TaskElement createValidTask() {
        HoursGroup hours = new HoursGroup();
        hours.setWorkingHours(20);
        return Task.createTask(hours);
    }

    @Test
    public void taskAddResourceAllocation() {
        assertThat(task.getResourceAllocations().size(), equalTo(0));

        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation.create(task);
        task.addResourceAllocation(resourceAllocation);

        assertThat(task.getResourceAllocations().size(), equalTo(1));
        assertThat(
                resourceAllocation.getTask().getResourceAllocations().size(),
                equalTo(1));
    }

    @Test
    public void taskRemoveResourceAllocation() {
        assertThat(task.getResourceAllocations().size(), equalTo(0));

        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation.create(task);
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
        task.addResourceAllocation(stubResourceAllocationWithAssignedHours(5));
        task.addResourceAllocation(stubResourceAllocationWithAssignedHours(3));
        assertThat(task.getAssignedHours(), equalTo(8));
    }

    private ResourceAllocation<?> stubResourceAllocationWithAssignedHours(
            int hours) {
        ResourceAllocation<?> resourceAllocation = createNiceMock(ResourceAllocation.class);
        expect(resourceAllocation.getAssignedHours()).andReturn(hours)
                .anyTimes();
        expect(resourceAllocation.getTask()).andReturn(task).anyTimes();
        replay(resourceAllocation);
        return resourceAllocation;
    }

}