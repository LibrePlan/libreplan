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

package org.navalplanner.business.test.planner.entities;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.test.planner.entities.DayAssignmentMatchers.haveHours;
import static org.navalplanner.business.workingday.EffortDuration.hours;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.IntraDayDate;
import org.navalplanner.business.workingday.ResourcesPerDay;

public class AllocationUntilFillingHoursTest {

    private List<ResourcesPerDayModification> allocations = new ArrayList<ResourcesPerDayModification>();

    private List<Resource> resources = new ArrayList<Resource>();

    private Task task;

    private IntraDayDate startDate;

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
    public void theEndDateIsTheDayAfterAllTheHoursAreAllocatedIfItIsCompletelyFilled() {
        givenSpecificAllocations(ResourcesPerDay.amount(2));
        IntraDayDate endDate = ResourceAllocation.allocating(allocations)
                .untilAllocating(32);
        assertThat(endDate.getDate(), equalTo(startDate.getDate().plusDays(2)));
        assertTrue(endDate.isStartOfDay());
    }

    @Test
    public void theEndDateIsTheSameDayIfItIsNotCompletelyFilled() {
        givenSpecificAllocations(ResourcesPerDay.amount(2));
        IntraDayDate endDate = ResourceAllocation.allocating(allocations)
                .untilAllocating(31);
        assertThat(endDate.getDate(), equalTo(startDate.getDate().plusDays(1)));
        assertThat(endDate.getEffortDuration(), equalTo(hours(15)));
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
    public void theResourcesPerDayIsCalculatedCorrectlyIfTheLastDayHasFilledAllHours() {
        givenSpecificAllocations(ResourcesPerDay.amount(1));
        ResourceAllocation.allocating(allocations).untilAllocating(32);
        ResourceAllocation<?> allocation = allocations.get(0)
                .getBeingModified();
        assertThat(allocation.getResourcesPerDay(),
                equalTo(ResourcesPerDay.amount(1)));
    }

    @Test
    public void theResourcesPerDayIsCalculatedCorrectlyIfHasEndedInTheMiddleOfTheEnd() {
        givenSpecificAllocations(ResourcesPerDay.amount(1));
        ResourceAllocation.allocating(allocations).untilAllocating(30);
        ResourceAllocation<?> allocation = allocations.get(0)
                .getBeingModified();
        assertThat(allocation.getResourcesPerDay(),
                equalTo(ResourcesPerDay.amount(1)));
    }

    @Test
    public void theResourcesPerDayIsCalculatedCorrectlyIfTheStartIsInTheMiddleOfADay() {
        givenStartDate(IntraDayDate.create(new LocalDate(2009, 10, 10),
                EffortDuration.hours(2)));
        givenSpecificAllocations(ResourcesPerDay.amount(1));
        ResourceAllocation.allocating(allocations).untilAllocating(8);
        ResourceAllocation<?> allocation = allocations.get(0)
                .getBeingModified();
        assertThat(allocation.getResourcesPerDay(),
                equalTo(ResourcesPerDay.amount(1)));
    }

    @Test
    public void theEndDateIsCalculatedCorrectlyIfTheStartIsInTheMiddleOfADayAndEndsTheSameDay() {
        givenStartDate(IntraDayDate.create(new LocalDate(2009, 10, 10),
                EffortDuration.hours(2)));
        givenSpecificAllocations(ResourcesPerDay.amount(1));
        ResourceAllocation.allocating(allocations).untilAllocating(4);
        ResourceAllocation<?> allocation = allocations.get(0)
                .getBeingModified();
        assertThat(allocation.getIntraDayEndDate(),
                equalTo(IntraDayDate.create(new LocalDate(2009, 10, 10),
                        EffortDuration.hours(6))));
    }

    @Test
    public void theResourcesPerDayAreKeptCorrectlyCalculatedAfterUpdatingTheEndInterval() {
        final ResourcesPerDay oneResourcePerDay = ResourcesPerDay.amount(1);
        givenSpecificAllocations(oneResourcePerDay);
        ResourceAllocation.allocating(allocations).untilAllocating(30);
        SpecificResourceAllocation allocation = (SpecificResourceAllocation) allocations
                .get(0)
                .getBeingModified();
        // hours per day: 8, 8, 8, 6
        allocation.onInterval(startDate.getDate(),
                startDate.getDate().plusDays(1))
                .allocateHours(6);
        // hours per day: 6, 8, 8, 6
        assertTrue(allocation.getResourcesPerDay().getAmount()
                .compareTo(oneResourcePerDay.getAmount()) < 0);

        allocation.onInterval(startDate.getDate().plusDays(3),
                startDate.getDate().plusDays(4)).allocateHours(8);
        // hours per day: 6, 8, 8, 8
        assertThat(allocation.getResourcesPerDay(), equalTo(oneResourcePerDay));
        // This last assertion is questionable. A solution would be to keep a
        // Spec object at ResourceAllocation with the desired parameters from
        // the user and then the real values. In the meantime doing an effort to
        // keep the original value

        allocation.onInterval(startDate.getDate().plusDays(4),
                startDate.getDate().plusDays(5))
                .allocateHours(8);
        // hours per day: 6, 8, 8, 8, 8
        assertTrue(allocation.getResourcesPerDay().getAmount()
                .compareTo(oneResourcePerDay.getAmount()) < 0);

        // hours per day: 6, 8, 8, 8, 10
        allocation.onInterval(startDate.getDate().plusDays(4),
                startDate.getDate().plusDays(5))
                .allocateHours(10);
        assertThat(allocation.getResourcesPerDay(), equalTo(oneResourcePerDay));
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
        GenericResourceAllocationTest.mockZeroLoad(worker);
        replay(worker);
        return worker;
    }

    private void givenStartDate(IntraDayDate start) {
        this.startDate = start;
    }

    private void createTaskIfNotCreatedYet() {
        if (task != null) {
            return;
        }
        task = createNiceMock(Task.class);
        if (startDate == null) {
            startDate = IntraDayDate.startOfDay(new LocalDate(2009, 10, 10));
        }
        expect(task.getStartDate()).andReturn(
                startDate.toDateTimeAtStartOfDay().toDate()).anyTimes();
        expect(task.getIntraDayStartDate()).andReturn(startDate).anyTimes();
        expect(task.getCriterions()).andReturn(
                Collections.<Criterion> emptySet()).anyTimes();
        expect(task.getFirstDayNotConsolidated()).andReturn(startDate)
                .anyTimes();
        replay(task);
    }

    private void givenAllocationsWithoutTask() {
        allocations.add(ResourcesPerDayModification.create(
                (SpecificResourceAllocation) createStubAllocationReturning(
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
