/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.business.test.planner.entities;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.libreplan.business.planner.entities.AggregateOfResourceAllocations;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.workingday.ResourcesPerDay;

public class AggregateOfResourceAllocationsTest {

    private AggregateOfResourceAllocations aggregate;

    @Test(expected = IllegalArgumentException.class)
    public void doesntAcceptNullResourceAllocations() {
        AggregateOfResourceAllocations.createFromSatisfied(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noNullElements() {
        List<ResourceAllocation<?>> list = new ArrayList<ResourceAllocation<?>>();
        list.add(null);
        AggregateOfResourceAllocations.createFromSatisfied(list);
    }

    @Test
    public void aggregateWithNoResourceAllocationsHasZeroTotalHours() {
        givenAggregateOfResourceAllocationsWithAssignedHours();
        assertThat(aggregate.getTotalHours(), equalTo(0));
    }

    @Test
    public void ifNoAllocationsIsEmpty() {
        givenAggregateOfResourceAllocationsWithAssignedHours();
        assertTrue(aggregate.isEmpty());
    }

    @Test
    public void unsatisfiedAllocationsAreIgnored() {
        List<ResourceAllocation<?>> allocationsList = Collections
                .<ResourceAllocation<?>> singletonList(givenUnsatisfiedResourceAllocation());
        AggregateOfResourceAllocations aggregate = AggregateOfResourceAllocations.createFromSatisfied(allocationsList);
        assertTrue(aggregate.isEmpty());
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
            expect(resourceAllocation.isSatisfied()).andReturn(true).anyTimes();
            replay(resourceAllocation);
            list.add(resourceAllocation);
        }
        aggregate = AggregateOfResourceAllocations.createFromSatisfied(list);
    }

    private ResourceAllocation<?> givenUnsatisfiedResourceAllocation() {
        ResourceAllocation<?> result = createMock(ResourceAllocation.class);
        expect(result.isSatisfied()).andReturn(false).anyTimes();
        replay(result);
        return result;
    }

    private void givenAggregateOfResourceAllocationsWithAssignedHours(
            int... hours) {
        ArrayList<ResourceAllocation<?>> list = new ArrayList<ResourceAllocation<?>>();
        for (int h : hours) {
            ResourceAllocation<?> resourceAllocation = createMock(ResourceAllocation.class);
            expect(resourceAllocation.getAssignedHours()).andReturn(h)
                    .anyTimes();
            expect(resourceAllocation.isSatisfied()).andReturn(true)
                    .anyTimes();
            replay(resourceAllocation);
            list.add(resourceAllocation);
        }
        aggregate = AggregateOfResourceAllocations.createFromSatisfied(list);
    }

}
