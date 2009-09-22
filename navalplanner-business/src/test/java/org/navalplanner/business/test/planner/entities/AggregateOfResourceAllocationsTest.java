package org.navalplanner.business.test.planner.entities;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;

public class AggregateOfResourceAllocationsTest {

    private AggregateOfResourceAllocations aggregate;

    @Test(expected = IllegalArgumentException.class)
    public void doesntAcceptNullResourceAllocations() {
        new AggregateOfResourceAllocations(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noNullElements() {
        List<ResourceAllocation<?>> list = new ArrayList<ResourceAllocation<?>>();
        list.add(null);
        new AggregateOfResourceAllocations(list);
    }

    @Test
    public void aggregateWithNoResourceAllocationsHasZeroTotalHours() {
        givenAggregateOfResourceAllocationsWithAssignedHours();
        assertThat(aggregate.getTotalHours(), equalTo(0));
    }

    @Test
    public void calculatesTheTotalHours() {
        givenAggregateOfResourceAllocationsWithAssignedHours(4, 5, 6);
        assertThat(aggregate.getTotalHours(), equalTo(15));
    }

    @Test
    public void canCalculateTheResourcesPerDay() {
        givenAggregateOfResourceAllocationsWithResourcesPerDay(ResourcesPerDay
                .amount(2), ResourcesPerDay.amount(3));
        Map<ResourceAllocation<?>, ResourcesPerDay> resourcesPerDay = aggregate
                .getResourcesPerDay();
        assertThat(resourcesPerDay.size(), equalTo(2));
        assertThat(resourcesPerDay.values(), hasItem(equalTo(ResourcesPerDay
                .amount(2))));
        assertThat(resourcesPerDay.values(), hasItem(equalTo(ResourcesPerDay
                .amount(3))));
    }

    private void givenAggregateOfResourceAllocationsWithResourcesPerDay(
            ResourcesPerDay... resourcesPerDay) {
        Collection<ResourceAllocation<?>> list = new ArrayList<ResourceAllocation<?>>();
        for (ResourcesPerDay r : resourcesPerDay) {
            ResourceAllocation<?> resourceAllocation = createMock(ResourceAllocation.class);
            expect(resourceAllocation.getResourcesPerDay()).andReturn(r)
                    .anyTimes();
            replay(resourceAllocation);
            list.add(resourceAllocation);
        }
        aggregate = new AggregateOfResourceAllocations(list);
    }

    private void givenAggregateOfResourceAllocationsWithAssignedHours(
            int... hours) {
        ArrayList<ResourceAllocation<?>> list = new ArrayList<ResourceAllocation<?>>();
        for (int h : hours) {
            ResourceAllocation<?> resourceAllocation = createMock(ResourceAllocation.class);
            expect(resourceAllocation.getAssignedHours()).andReturn(h)
                    .anyTimes();
            replay(resourceAllocation);
            list.add(resourceAllocation);
        }
        aggregate = new AggregateOfResourceAllocations(list);
    }

}
