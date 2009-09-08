package org.navalplanner.business.test.planner.entities;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Criterion;

public class GenericResourceAllocationTest {

    private GenericResourceAllocation genericResourceAllocation;
    private Set<Criterion> criterions;

    private void givenGenericResourceAllocation() {
        Task task = createNiceMock(Task.class);
        expect(task.getCriterions()).andReturn(givenPredefinedCriterions());
        replay(task);
        genericResourceAllocation = GenericResourceAllocation.create(task);
    }

    private Set<Criterion> givenPredefinedCriterions() {
        Set<Criterion> result = new HashSet<Criterion>();
        Criterion criterion1 = createNiceMock(Criterion.class);
        Criterion criterion2 = createNiceMock(Criterion.class);
        replay(criterion1, criterion2);
        result.add(criterion1);
        result.add(criterion2);
        this.criterions = result;
        return result;
    }

    @Test
    public void hasTheCriterionsOfTheTask() {
        givenGenericResourceAllocation();
        assertThat(genericResourceAllocation.getCriterions(),
                equalTo(criterions));
    }

}
