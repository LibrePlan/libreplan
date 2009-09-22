package org.navalplanner.business.test.planner.entities;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.ResourceAllocation;

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
