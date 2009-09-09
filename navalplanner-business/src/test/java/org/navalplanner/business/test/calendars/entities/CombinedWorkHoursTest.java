package org.navalplanner.business.test.calendars.entities;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.calendars.entities.CombinedWorkHours;
import org.navalplanner.business.calendars.entities.IWorkHours;

public class CombinedWorkHoursTest {

    @Test(expected = IllegalArgumentException.class)
    public void cannotAcceptNull() {
        IWorkHours[] nullWorkHours = null;
        CombinedWorkHours.minOf(nullWorkHours);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noElementIsNull() {
        CombinedWorkHours.minOf(null, createNiceMock(IWorkHours.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustHaveatLeastOne() {
        IWorkHours[] emptyArray = {};
        CombinedWorkHours.minOf(emptyArray);
    }

    @Test
    public void returnsTheMinOfCalendars() {
        IWorkHours minOf = CombinedWorkHours
                .minOf(hours(4), hours(2), hours(7));
        Integer hours = minOf.getWorkableHours(new LocalDate(2000, 3, 3));
        assertThat(hours, equalTo(2));
    }

    private IWorkHours hours(int hours) {
        IWorkHours result = createNiceMock(IWorkHours.class);
        expect(result.getWorkableHours(isA(LocalDate.class))).andReturn(hours);
        replay(result);
        return result;
    }

}
