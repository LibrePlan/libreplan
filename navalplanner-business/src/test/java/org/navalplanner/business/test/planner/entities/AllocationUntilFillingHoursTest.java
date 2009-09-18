package org.navalplanner.business.test.planner.entities;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.ResourceAllocation.ResourceAllocationWithDesiredResourcesPerDay;

public class AllocationUntilFillingHoursTest {

    private List<ResourceAllocationWithDesiredResourcesPerDay> allocations = new ArrayList<ResourceAllocationWithDesiredResourcesPerDay>();

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
        allocations.add(new ResourceAllocationWithDesiredResourcesPerDay(
                createStubAllocationReturning(task), ResourcesPerDay.amount(2)));
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

    private ResourceAllocation createStubAllocationReturning(Task task) {
        ResourceAllocation resourceAllocation = createNiceMock(ResourceAllocation.class);
        expect(resourceAllocation.getTask()).andReturn(task).anyTimes();
        replay(resourceAllocation);
        return resourceAllocation;
    }

}
