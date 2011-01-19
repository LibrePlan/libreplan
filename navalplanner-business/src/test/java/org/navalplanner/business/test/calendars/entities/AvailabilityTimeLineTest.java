/*
 * This file is part of NavalPlan
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
package org.navalplanner.business.test.calendars.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.DatePoint;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.EndOfTime;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.FixedPoint;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.Interval;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.StartOfTime;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
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

    @Test
    public void pointsAreMergedCorrectly() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();

        timeLine.invalidAt(new LocalDate(2010, 4, 7));
        timeLine.invalidAt(new LocalDate(2010, 4, 11));
        timeLine.invalidAt(new LocalDate(2010, 4, 8));
        timeLine.invalidAt(new LocalDate(2010, 4, 6));

        assertFalse(timeLine.isValid(new LocalDate(2010, 4, 8)));
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
    public void addingAllInvalidMakesAllInvalid() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        timeLine.invalidFrom(contemporaryExample);
        timeLine.invalidUntil(contemporaryExample.minusDays(10));

        assertFalse(timeLine.isValid(earlyExample));
        assertTrue(timeLine.isValid(contemporaryExample.minusDays(10)));

        assertFalse(timeLine.isValid(contemporaryExample));
        assertFalse(timeLine.isValid(lateExample));

        timeLine.allInvalid();
        assertFalse(timeLine.isValid(earlyExample));
        assertFalse(timeLine.isValid(contemporaryExample.minusDays(10)));
        assertFalse(timeLine.isValid(contemporaryExample));
        assertFalse(timeLine.isValid(lateExample));
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

    @Test
    public void doingOROnTwoTimeLinesResultingOnAnAllValidTimeLine() {
        AvailabilityTimeLine one = AvailabilityTimeLine.allValid();
        one.invalidAt(contemporaryExample.minusDays(40), contemporaryExample
                .minusDays(20));

        AvailabilityTimeLine another = AvailabilityTimeLine.allValid();
        another.invalidAt(contemporaryExample.minusDays(10),
                contemporaryExample.plusDays(10));

        AvailabilityTimeLine result = one.or(another);

        assertThat(result.getValidPeriods(), definedBy(StartOfTime.create(),
                EndOfTime.create()));
    }

    @Test
    public void doingORonTwoTimeLinesWithSeveralIntersectingInvalidPeriods() {
        AvailabilityTimeLine one = AvailabilityTimeLine.allValid();
        one.invalidAt(contemporaryExample.minusDays(40), contemporaryExample
                .minusDays(20));
        one.invalidAt(contemporaryExample.plusDays(35), contemporaryExample
                .plusDays(50));

        AvailabilityTimeLine another = AvailabilityTimeLine.allValid();
        another.invalidAt(contemporaryExample.minusDays(25),
                contemporaryExample.plusDays(10));
        another.invalidAt(contemporaryExample.plusDays(30), contemporaryExample
                .plusDays(40));

        AvailabilityTimeLine result = one.or(another);

        assertThat(result.getValidPeriods(), definedBy(StartOfTime.create(),
                point(contemporaryExample.minusDays(25)),
                point(contemporaryExample.minusDays(20)),
                point(contemporaryExample.plusDays(35)),
                point(contemporaryExample.plusDays(40)), EndOfTime.create()));
    }

    @Test
    public void doingOROnTheSameTimeLineResultsInTheSameTimeLine() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        timeLine.invalidAt(earlyExample, contemporaryExample);
        timeLine.invalidAt(lateExample, lateExample.plusDays(20));

        AvailabilityTimeLine result = timeLine.or(timeLine);

        assertThat(result.getValidPeriods(), definedBy(StartOfTime.create(),
                point(earlyExample), point(contemporaryExample),
                point(lateExample), point(lateExample
                        .plusDays(20)), EndOfTime.create()));
    }

    @Test
    public void doingAnAndWithAnAllValidTimeLineProducesTheSameTimeLine() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        timeLine.invalidAt(earlyExample, contemporaryExample);
        timeLine.invalidAt(lateExample, lateExample.plusDays(20));

        AvailabilityTimeLine result = timeLine.and(AvailabilityTimeLine
                .allValid());

        assertThat(result.getValidPeriods(), definedBy(StartOfTime.create(),
                point(earlyExample), point(contemporaryExample),
                point(lateExample), point(lateExample
                        .plusDays(20)), EndOfTime.create()));
    }

    @Test
    public void doingAnOrWithANeverValidTimeLineProducesTheSameTimeLine() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        timeLine.invalidAt(earlyExample, contemporaryExample);
        timeLine.invalidAt(lateExample, lateExample.plusDays(20));
        AvailabilityTimeLine another = AvailabilityTimeLine.allValid();
        another.allInvalid();

        AvailabilityTimeLine result = timeLine.and(another);

        assertThat(result.getValidPeriods(), definedBy(StartOfTime.create(),
                point(earlyExample), point(contemporaryExample),
                point(lateExample), point(lateExample.plusDays(20)), EndOfTime
                        .create()));
    }

    @Test
    public void anAllValidPeriodsGeneratesAnAllEncompassingInterval() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        List<Interval> validPeriods = timeLine.getValidPeriods();

        assertThat(validPeriods, definedBy(StartOfTime.create(), EndOfTime
                .create()));
    }

    @Test
    public void anInvalidPeriodUntilGeneratesAValidIntervalAfterwards() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        timeLine.invalidUntil(contemporaryExample);
        List<Interval> validPeriods = timeLine.getValidPeriods();

        assertThat(validPeriods, definedBy(point(contemporaryExample),
                EndOfTime.create()));
    }

    @Test
    public void anInvalidFromPeriodGeneratesAValidIntervalBefore() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        timeLine.invalidFrom(contemporaryExample);
        List<Interval> validPeriods = timeLine.getValidPeriods();

        assertThat(validPeriods, definedBy(StartOfTime.create(),
                new FixedPoint(contemporaryExample)));
    }

    @Test
    public void anInvalidityPeriodGeneratesTwoValidIntervals() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        timeLine.invalidAt(contemporaryExample, lateExample);
        List<Interval> validPeriods = timeLine.getValidPeriods();

        assertThat(validPeriods, definedBy(StartOfTime.create(),
                point(contemporaryExample), point(lateExample), EndOfTime
                        .create()));
    }

    @Test
    public void anAllInvalidTimelineGeneratesZeroValidIntervals() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        timeLine.allInvalid();
        assertTrue(timeLine.getValidPeriods().isEmpty());
    }

    @Test
    public void anInvalidPointGeneratesTwoValidPeriods() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        timeLine.invalidAt(contemporaryExample);
        assertThat(timeLine.getValidPeriods(), definedBy(StartOfTime.create(),
                point(contemporaryExample), point(contemporaryExample
                        .plusDays(1)), EndOfTime.create()));
    }

    private static FixedPoint point(LocalDate param) {
        return new FixedPoint(param);
    }

    @Test
    public void aCombinationOfSeveralInvalidPeriods() {
        AvailabilityTimeLine timeLine = AvailabilityTimeLine.allValid();
        timeLine.invalidUntil(earlyExample);
        timeLine.invalidAt(contemporaryExample, lateExample);
        timeLine.invalidFrom(lateExample.plusDays(10));
        assertThat(timeLine.getValidPeriods(), definedBy(point(earlyExample),
                point(contemporaryExample), point(lateExample),
                point(lateExample.plusDays(10))));
    }

    private static Matcher<List<Interval>> definedBy(final DatePoint... points) {
        Validate.isTrue(points.length % 2 == 0,
                "number of points provided must be even");
        return new BaseMatcher<List<Interval>>() {

            @SuppressWarnings("unchecked")
            @Override
            public boolean matches(Object object) {
                if (object instanceof List) {
                    List<Interval> intervals = (List<Interval>) object;
                    List<DatePoint[]> pairsOfPoints = pointsAsPairs();
                    for (int i = 0; i < intervals.size(); i++) {
                        Interval interval = intervals.get(i);
                        DatePoint[] pair = pairsOfPoints.get(i);
                        if (!(pair[0].equals(interval.getStart()) && pair[1]
                                .equals(interval.getEnd()))) {
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }

            private List<DatePoint[]> pointsAsPairs() {
                List<DatePoint[]> result = new ArrayList<DatePoint[]>();
                for (int i = 0; i < points.length / 2; i++) {
                    DatePoint[] pair = { points[i * 2], points[i * 2 + 1] };
                    result.add(pair);
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                StringBuilder text = new StringBuilder();
                boolean first = true;
                for (DatePoint[] each : pointsAsPairs()) {
                    DatePoint start = each[0];
                    DatePoint end = each[1];
                    if (!first) {
                        text.append(", ");
                    }
                    text.append(String.format("[%s, %s]", start, end));
                    first = false;
                }
                description.appendText(text.toString());
            }
        };
    }

}
