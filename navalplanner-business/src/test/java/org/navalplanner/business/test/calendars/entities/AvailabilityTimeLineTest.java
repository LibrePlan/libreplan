/*
 * This file is part of ###PROJECT_NAME###
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
package org.navalplanner.business.test.calendars.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class AvailabilityTimeLineTest {

    private LocalDate earlyExample = new LocalDate(1000, 10, 6);

    private LocalDate contemporaryExample = new LocalDate(2010, 10, 6);

    private LocalDate lateExample = new LocalDate(3000, 10, 6);

    @Test
    public void anAllValidTimeLineIsValidForAllDates() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        assertTrue(timeLine.isValid(earlyExample));
        assertTrue(timeLine.isValid(contemporaryExample));
        assertTrue(timeLine.isValid(lateExample));
    }

    @Test
    public void canBeAddedInvalidDates() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();

        timeLine.invalidAt(earlyExample);

        assertFalse(timeLine.isValid(earlyExample));
        assertTrue(timeLine.isValid(contemporaryExample));
        assertTrue(timeLine.isValid(lateExample));
    }

    @Test
    public void canBeAddedInvalidIntervals() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();

        LocalDate intervalStart = contemporaryExample.minusDays(10);
        LocalDate intervalEnd = contemporaryExample.plusDays(5);
        timeLine.invalidAt(intervalStart, intervalEnd);

        assertFalse("the start is inclusive", timeLine.isValid(intervalStart));
        assertFalse(timeLine.isValid(contemporaryExample.minusDays(1)));
        assertFalse(timeLine.isValid(contemporaryExample));
        assertFalse(timeLine.isValid(contemporaryExample.plusDays(1)));
        assertTrue("the end is exclusive", timeLine.isValid(intervalEnd));
    }

    @Test
    public void addingAnIntervalThatIsCompletelyInvalidIsIgnored() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        LocalDate intervalStart = contemporaryExample.minusDays(10);
        LocalDate intervalEnd = contemporaryExample.plusDays(5);

        timeLine.invalidAt(intervalStart, intervalEnd);
        timeLine.invalidAt(intervalStart.plusDays(2), intervalEnd.minusDays(2));

        assertFalse(timeLine.isValid(intervalEnd.minusDays(1)));
    }

    @Test
    public void addingAnIntervalThatItsNotCompletelyInvalid() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        LocalDate intervalStart = contemporaryExample.minusDays(10);
        LocalDate intervalEnd = contemporaryExample.plusDays(5);

        timeLine.invalidAt(intervalStart, intervalEnd);
        timeLine.invalidAt(intervalStart.minusDays(3), intervalEnd.plusDays(4));

        assertFalse(timeLine.isValid(intervalStart.minusDays(3)));
        assertFalse(timeLine.isValid(intervalEnd));
        assertFalse(timeLine.isValid(intervalEnd.plusDays(3)));
        assertTrue(timeLine.isValid(intervalEnd.plusDays(4)));
    }

    @Test
    public void addingAnIntervalThatJoinsTwoInvalidIntervals() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        LocalDate intervalStart = contemporaryExample.minusDays(10);
        LocalDate intervalEnd = contemporaryExample.plusDays(5);

        timeLine.invalidAt(intervalStart, intervalEnd);
        timeLine.invalidAt(intervalStart.minusDays(20), intervalStart
                .minusDays(10));
        timeLine.invalidAt(intervalStart.minusDays(10), intervalStart);
        LocalDate current = intervalStart.minusDays(20);
        while (current.isBefore(intervalEnd)) {
            assertFalse(timeLine.isValid(current));
            current = current.plusDays(1);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void endMustBeAfterStart() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        LocalDate intervalStart = contemporaryExample.minusDays(10);
        LocalDate intervalEnd = contemporaryExample.plusDays(5);
        timeLine.invalidAt(intervalEnd, intervalStart);
    }

    @Test
    public void addingFromInterval() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        timeLine.invalidFrom(contemporaryExample);

        assertFalse(timeLine.isValid(contemporaryExample));
        assertFalse(timeLine.isValid(contemporaryExample.plusDays(10)));
        assertFalse(timeLine.isValid(lateExample));
        assertTrue(timeLine.isValid(contemporaryExample.minusDays(1)));
    }

    @Test
    public void addingToInterval() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        timeLine.invalidUntil(contemporaryExample);

        assertTrue(timeLine.isValid(contemporaryExample));
        assertTrue(timeLine.isValid(contemporaryExample.plusDays(1)));
        assertFalse(timeLine.isValid(contemporaryExample.minusDays(1)));
        assertFalse(timeLine.isValid(earlyExample));
    }

    @Test
    public void addingAndAlreadyIncludedIntervalToAFromIntervalDoesNothing() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        timeLine.invalidFrom(contemporaryExample);
        timeLine.invalidAt(contemporaryExample.plusDays(30),
                contemporaryExample.plusDays(100));

        assertFalse(timeLine.isValid(contemporaryExample));
        assertFalse(timeLine.isValid(contemporaryExample.plusDays(10)));
        assertFalse(timeLine.isValid(lateExample));
        assertTrue(timeLine.isValid(contemporaryExample.minusDays(1)));
    }

    @Test
    public void addingSeveralTypesOfIntervals() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        timeLine.invalidFrom(contemporaryExample);
        timeLine.invalidUntil(contemporaryExample.minusDays(10));

        assertFalse(timeLine.isValid(earlyExample));
        assertTrue(timeLine.isValid(contemporaryExample.minusDays(10)));

        assertFalse(timeLine.isValid(contemporaryExample));
        assertFalse(timeLine.isValid(lateExample));

        timeLine.invalidAt(contemporaryExample.minusDays(10),
                contemporaryExample);

        assertFalse(timeLine.isValid(contemporaryExample));
        assertFalse(timeLine.isValid(contemporaryExample.plusDays(1)));
        assertFalse(timeLine.isValid(contemporaryExample.minusDays(1)));
        assertFalse(timeLine.isValid(earlyExample));
    }

    @Test
    public void addingTwoAvailabilityTimeLines() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        timeLine.invalidAt(contemporaryExample.minusDays(10),
                contemporaryExample.plusDays(10));

        AvailabilityTimeLine another = AvailabilityTimeLine.allValid();
        another.invalidAt(contemporaryExample.minusDays(40),
                contemporaryExample.minusDays(20));

        AvailabilityTimeLine result = timeLine.and(another);

        LocalDate current = contemporaryExample.minusDays(40);
        LocalDate end = contemporaryExample.plusDays(20);
        while (current.compareTo(end) < 0) {
            assertEquals("must give the same value for: " + current, timeLine
                    .isValid(current)
                    && another.isValid(current), result.isValid(current));
            current = current.plusDays(1);
        }
    }

}
