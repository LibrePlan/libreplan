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

package org.navalplanner.business.test.calendars.entities;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.calendars.entities.CombinedWorkHours;
import org.navalplanner.business.calendars.entities.IWorkHours;
import org.navalplanner.business.workingday.EffortDuration;

public class CombinedWorkHoursTest {

    @Test(expected = IllegalArgumentException.class)
    public void cannotAcceptOnlyNullElements() {
        IWorkHours[] nullWorkHours = null;
        CombinedWorkHours.minOf(nullWorkHours);
    }

    public void someElementsCanBeNull() {
        CombinedWorkHours minOf = CombinedWorkHours.minOf(null,
                createNiceMock(IWorkHours.class));
        assertNotNull(minOf);
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
        EffortDuration duration = minOf.getCapacityDurationAt(new LocalDate(
                2000, 3, 3));
        assertThat(duration, equalTo(EffortDuration.hours(2)));
    }

    private IWorkHours hours(int hours) {
        IWorkHours result = createNiceMock(IWorkHours.class);
        expect(result.getCapacityDurationAt(isA(LocalDate.class))).andReturn(
                EffortDuration.hours(hours));
        replay(result);
        return result;
    }

}
