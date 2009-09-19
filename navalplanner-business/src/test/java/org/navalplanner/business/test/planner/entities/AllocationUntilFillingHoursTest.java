package org.navalplanner.business.test.planner.entities;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.navalplanner.business.test.planner.entities.DayAssignmentMatchers.haveHours;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.ResourceAllocation.ResourceAllocationWithDesiredResourcesPerDay;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

public class AllocationUntilFillingHoursTest {

    private List<ResourceAllocationWithDesiredResourcesPerDay> allocations = new ArrayList<ResourceAllocationWithDesiredResourcesPerDay>();

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
                .allocating(new ArrayList<ResourceAllocationWithDesiredResourcesPerDay>());
    }

    @Test
    public void theNewEndDateIsWhenAllTheHoursAreAllocated() {
        givenSpecificAllocations(ResourcesPerDay.amount(2));
        LocalDate endDate = ResourceAllocation.allocating(allocations)
                .withResources(resources).untilAllocating(32);
        assertThat(endDate, equalTo(startDate.plusDays(2)));
    }

    @Test
    public void allTheRequestedHoursAreAssignedFor() {
        givenSpecificAllocations(ResourcesPerDay.amount(2));
        ResourceAllocation.allocating(allocations).withResources(resources)
                .untilAllocating(32);
        ResourceAllocation<?> allocation = allocations.get(0)
                .getResourceAllocation();
        assertThat(allocation.getAssignments(), haveHours(16, 16));
    }

    @Test
    public void worksWellForSeveralSpecificAllocations() {
        givenSpecificAllocations(ResourcesPerDay.amount(1), ResourcesPerDay
                .amount(1));
        ResourceAllocation.allocating(allocations).withResources(resources)
                .untilAllocating(32);
        ResourceAllocation<?> first = allocations.get(0)
                .getResourceAllocation();
        ResourceAllocation<?> second = allocations.get(1)
                .getResourceAllocation();
        assertThat(first.getAssignments(), haveHours(8, 8));
        assertThat(second.getAssignments(), haveHours(8, 8));
    }

    @Test
    public void theRemainderIsProportinallyDistributed() {
        givenSpecificAllocations(ResourcesPerDay.amount(2), ResourcesPerDay
                .amount(1));
        ResourceAllocation.allocating(allocations).withResources(resources)
                .untilAllocating(60);
        ResourceAllocation<?> first = allocations.get(0)
                .getResourceAllocation();
        ResourceAllocation<?> second = allocations.get(1)
                .getResourceAllocation();
        assertThat(first.getAssignments(), haveHours(16, 16, 8));
        assertThat(second.getAssignments(), haveHours(8, 8, 4));
    }

    @Test
    public void withUnequalRatioWorksOk() {
        givenSpecificAllocations(ResourcesPerDay.amount(1), ResourcesPerDay
                .amount(new BigDecimal(0.5)));
        ResourceAllocation.allocating(allocations).withResources(resources)
                .untilAllocating(36);
        ResourceAllocation<?> first = allocations.get(0)
                .getResourceAllocation();
        ResourceAllocation<?> second = allocations.get(1)
                .getResourceAllocation();
        assertThat(first.getAssignments(), haveHours(8, 8, 8));
        assertThat(second.getAssignments(), haveHours(4, 4, 4));
    }

    @Test
    public void withGenericAllocationAlsoWorks() {
        givenWorkers(1);
        givenGenericAllocation(ResourcesPerDay.amount(2));
        givenSpecificAllocations(ResourcesPerDay.amount(1), ResourcesPerDay
                .amount(1));
        ResourceAllocation.allocating(allocations).withResources(resources)
                .untilAllocating(64);
        ResourceAllocation<?> generic = allocations.get(0)
                .getResourceAllocation();
        ResourceAllocation<?> firstSpecific = allocations.get(1)
                .getResourceAllocation();
        ResourceAllocation<?> secondSpecific = allocations.get(2)
                .getResourceAllocation();
        assertThat(generic.getAssignments(), haveHours(16, 16));
        assertThat(firstSpecific.getAssignments(), haveHours(8, 8));
        assertThat(secondSpecific.getAssignments(), haveHours(8, 8));
    }

    private void givenGenericAllocation(ResourcesPerDay resourcesPerDay) {
        createTaskIfNotCreatedYet();
        allocations.add(new ResourceAllocationWithDesiredResourcesPerDay(
                GenericResourceAllocation.create(task), resourcesPerDay));
    }

    private void givenSpecificAllocations(
            ResourcesPerDay... specifiedResourcesPerDay) {
        createTaskIfNotCreatedYet();
        Worker worker = createWorker();
        for (ResourcesPerDay resourcesPerDay : specifiedResourcesPerDay) {
            SpecificResourceAllocation allocation = createSpecificResourceAllocationFor(
                    task, worker);
            allocations.add(new ResourceAllocationWithDesiredResourcesPerDay(
                    allocation, resourcesPerDay));
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
        replay(task);
    }

    private void givenAllocationsWithoutTask() {
        allocations
                .add(new ResourceAllocationWithDesiredResourcesPerDay(
                        createStubAllocationReturning(null), ResourcesPerDay
                                .amount(2)));
        allocations
                .add(new ResourceAllocationWithDesiredResourcesPerDay(
                        createStubAllocationReturning(null), ResourcesPerDay
                                .amount(2)));
    }

    private void givenAllocationsBelongingToDifferentTasks() {
        Task task = createStubTask();
        allocations
                .add(new ResourceAllocationWithDesiredResourcesPerDay(
                        createStubAllocationReturning(task), ResourcesPerDay
                                .amount(2)));
        allocations
                .add(new ResourceAllocationWithDesiredResourcesPerDay(
                        createStubAllocationReturning(task), ResourcesPerDay
                                .amount(2)));
        Task other = createStubTask();
        allocations
                .add(new ResourceAllocationWithDesiredResourcesPerDay(
                        createStubAllocationReturning(other), ResourcesPerDay
                                .amount(2)));
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

    private ResourceAllocation<?> createStubAllocationReturning(Task task) {
        ResourceAllocation<?> resourceAllocation = createNiceMock(ResourceAllocation.class);
        expect(resourceAllocation.getTask()).andReturn(task).anyTimes();
        replay(resourceAllocation);
        return resourceAllocation;
    }

}
