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
import static org.junit.Assert.assertThat;
import static org.navalplanner.business.test.planner.entities.DayAssignmentMatchers.haveHours;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

public class AllocationUntilFillingHoursTest {

    private List<ResourcesPerDayModification> allocations = new ArrayList<ResourcesPerDayModification>();

    private List<Resource> resources = new ArrayList<Resource>();

    private Task task;

    private LocalDate startDate;

    @Test(expected = IllegalArgumentException.class)
    public void allTasksOfAllocationsMustBeNotNull() {
        givenAllocationsWithoutTask();
        ResourceAllocation.allocating(allocations);
    }

    @Test(expected = IllegalArgumentException.class)
    public void allAllocationsMustBelongToTheSameTask() {
        givenAllocationsBelongingToDifferentTasks();
        ResourceAllocation.allocating(allocations);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustReceiveAtLeastOneAllocation() {
        ResourceAllocation
                .allocating(new ArrayList<ResourcesPerDayModification>());
    }

    @Test
    public void theNewEndDateIsWhenAllTheHoursAreAllocated() {
        givenSpecificAllocations(ResourcesPerDay.amount(2));
        LocalDate endDate = ResourceAllocation.allocating(allocations)
                .untilAllocating(32);
        assertThat(endDate, equalTo(startDate.plusDays(2)));
    }

    @Test
    public void allTheRequestedHoursAreAssignedFor() {
        givenSpecificAllocations(ResourcesPerDay.amount(2));
        ResourceAllocation.allocating(allocations).untilAllocating(32);
        ResourceAllocation<?> allocation = allocations.get(0)
                .getBeingModified();
        assertThat(allocation.getAssignments(), haveHours(16, 16));
    }

    @Test
    public void worksWellForSeveralSpecificAllocations() {
        givenSpecificAllocations(ResourcesPerDay.amount(1), ResourcesPerDay
                .amount(1));
        ResourceAllocation.allocating(allocations).untilAllocating(32);
        ResourceAllocation<?> first = allocations.get(0)
                .getBeingModified();
        ResourceAllocation<?> second = allocations.get(1)
                .getBeingModified();
        assertThat(first.getAssignments(), haveHours(8, 8));
        assertThat(second.getAssignments(), haveHours(8, 8));
    }

    @Test
    public void theRemainderIsProportinallyDistributed() {
        givenSpecificAllocations(ResourcesPerDay.amount(2), ResourcesPerDay
                .amount(1));
        ResourceAllocation.allocating(allocations).untilAllocating(60);
        ResourceAllocation<?> first = allocations.get(0)
                .getBeingModified();
        ResourceAllocation<?> second = allocations.get(1)
                .getBeingModified();
        assertThat(first.getAssignments(), haveHours(16, 16, 8));
        assertThat(second.getAssignments(), haveHours(8, 8, 4));
    }

    @Test
    public void withUnequalRatioWorksOk() {
        givenSpecificAllocations(ResourcesPerDay.amount(1), ResourcesPerDay
                .amount(new BigDecimal(0.5)));
        ResourceAllocation.allocating(allocations).untilAllocating(36);
        ResourceAllocation<?> first = allocations.get(0)
                .getBeingModified();
        ResourceAllocation<?> second = allocations.get(1)
                .getBeingModified();
        assertThat(first.getAssignments(), haveHours(8, 8, 8));
        assertThat(second.getAssignments(), haveHours(4, 4, 4));
    }

    @Test
    public void withGenericAllocationAlsoWorks() {
        givenWorkers(1);
        givenGenericAllocation(ResourcesPerDay.amount(2));
        givenSpecificAllocations(ResourcesPerDay.amount(1), ResourcesPerDay
                .amount(1));
        ResourceAllocation.allocating(allocations).untilAllocating(64);
        ResourceAllocation<?> generic = allocations.get(0)
                .getBeingModified();
        ResourceAllocation<?> firstSpecific = allocations.get(1)
                .getBeingModified();
        ResourceAllocation<?> secondSpecific = allocations.get(2)
                .getBeingModified();
        assertThat(generic.getAssignments(), haveHours(16, 16));
        assertThat(firstSpecific.getAssignments(), haveHours(8, 8));
        assertThat(secondSpecific.getAssignments(), haveHours(8, 8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void withGenericAllocationWithNoResourcesPerDay() {
        givenWorkers(1);
        givenGenericAllocation(ResourcesPerDay.amount(0));
        ResourceAllocation.allocating(allocations).untilAllocating(100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotDoAGenericAllocationWithoutWorkers() {
        givenWorkers(0);
        givenGenericAllocation(ResourcesPerDay.amount(2));
        ResourceAllocation.allocating(allocations).untilAllocating(100);
    }

    @Test
    public void withoutWorkersYouCanDoSpecificAllocation() {
        givenWorkers(0);
        givenSpecificAllocations(ResourcesPerDay.amount(2));
        ResourceAllocation.allocating(allocations).untilAllocating(100);
    }

    private void givenGenericAllocation(ResourcesPerDay resourcesPerDay) {
        createTaskIfNotCreatedYet();
        allocations.add(ResourcesPerDayModification.create(GenericResourceAllocation
                .create(task), resourcesPerDay, resources));
    }

    private void givenSpecificAllocations(
            ResourcesPerDay... specifiedResourcesPerDay) {
        createTaskIfNotCreatedYet();
        Worker worker = createWorker();
        for (ResourcesPerDay resourcesPerDay : specifiedResourcesPerDay) {
            SpecificResourceAllocation allocation = createSpecificResourceAllocationFor(
                    task, worker);
            allocations.add(ResourcesPerDayModification.create(allocation,
                    resourcesPerDay));
        }
    }

    private Worker createWorker() {
        Worker worker = createNiceMock(Worker.class);
        replay(worker);
        return worker;
    }

    private void createTaskIfNotCreatedYet() {
        if (task != null) {
            return;
        }
        task = createNiceMock(Task.class);
        if (startDate == null) {
            startDate = new LocalDate(2009, 10, 10);
        }
        expect(task.getStartDate()).andReturn(
                startDate.toDateTimeAtStartOfDay().toDate()).anyTimes();
        expect(task.getCriterions()).andReturn(
                Collections.<Criterion> emptySet()).anyTimes();
        replay(task);
    }

    private void givenAllocationsWithoutTask() {
        allocations.add(ResourcesPerDayModification.create(
                (SpecificResourceAllocation)
 createStubAllocationReturning(
                        SpecificResourceAllocation.class, null),
                ResourcesPerDay.amount(2)));
        allocations.add(ResourcesPerDayModification.create(
                createStubAllocationReturning(SpecificResourceAllocation.class,
                        null), ResourcesPerDay.amount(2)));
    }

    private void givenAllocationsBelongingToDifferentTasks() {
        Task task = createStubTask();
        allocations.add(ResourcesPerDayModification.create(
                createStubAllocationReturning(SpecificResourceAllocation.class,
                        task), ResourcesPerDay.amount(2)));
        allocations.add(ResourcesPerDayModification.create(
                createStubAllocationReturning(SpecificResourceAllocation.class,
                        task), ResourcesPerDay.amount(2)));
        Task other = createStubTask();
        allocations.add(ResourcesPerDayModification.create(
                createStubAllocationReturning(SpecificResourceAllocation.class,
                        other), ResourcesPerDay.amount(2)));
    }

    private Task createStubTask() {
        Task task = createNiceMock(Task.class);
        replay(task);
        return task;
    }

    private void givenWorkers(int n) {
        for (int i = 0; i < n; i++) {
            resources.add(createWorker());
        }
    }

    private SpecificResourceAllocation createSpecificResourceAllocationFor(
            Task task, Resource resource) {
        SpecificResourceAllocation result = SpecificResourceAllocation
                .create(task);
        result.setResource(resource);
        return result;
    }

    private <T extends ResourceAllocation<?>> T createStubAllocationReturning(
            Class<T> allocationClass, Task task) {
        T resourceAllocation = createNiceMock(allocationClass);
        expect(resourceAllocation.getTask()).andReturn(task).anyTimes();
        replay(resourceAllocation);
        return resourceAllocation;
    }

}
