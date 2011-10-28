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
package org.libreplan.business.test.workingday;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.workingday.EffortDuration.hours;
import static org.libreplan.business.workingday.EffortDuration.minutes;
import static org.libreplan.business.workingday.EffortDuration.zero;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.EffortDuration.Granularity;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;
import org.libreplan.business.workingday.IntraDayDate.UntilEnd;
import org.libreplan.business.workingday.ResourcesPerDay;

/**
 * @author Óscar González Fernández
 *
 */
public class IntraDayDateTest {

    private LocalDate today = new LocalDate();
    private LocalDate tomorrow = today.plusDays(1);
    private EffortDuration oneHour = EffortDuration.hours(1);
    private EffortDuration halfHour = EffortDuration.minutes(30);

    @Test(expected = IllegalArgumentException.class)
    public void needsANotNullDuration() {
        IntraDayDate.create(new LocalDate(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void needsANotNullDate() {
        IntraDayDate.create(null, EffortDuration.elapsing(1, Granularity.HOURS));
    }

    @Test
    public void equalsAndHashCodeWorkOk() {
        LocalDate today = new LocalDate();
        LocalDate tomorrow = today.plusDays(1);
        EffortDuration oneHour = EffortDuration.hours(1);
        EffortDuration halfHour = EffortDuration.minutes(30);
        assertEquals(IntraDayDate.create(today, halfHour),
                IntraDayDate.create(tomorrow.minusDays(1), halfHour));
        assertEquals(IntraDayDate.create(today, halfHour).hashCode(), IntraDayDate
                .create(tomorrow.minusDays(1), halfHour).hashCode());
        assertThat(IntraDayDate.create(today, halfHour),
                not(equalTo(IntraDayDate.create(today, oneHour))));
    }

    @Test
    public void canKnowIfAreSameDay() {
        assertTrue(IntraDayDate.create(today, halfHour).areSameDay(today));
        assertTrue(IntraDayDate.create(today, oneHour).areSameDay(today));
        assertFalse(IntraDayDate.create(today, halfHour).areSameDay(tomorrow));
        assertFalse(IntraDayDate.create(tomorrow, halfHour).areSameDay(today));
    }

    @Test
    public void canGetDateTimeAtStartOfDay() {
        DateTime dateTime = IntraDayDate.create(today, halfHour)
                .toDateTimeAtStartOfDay();
        assertThat(dateTime, equalTo(today.toDateTimeAtStartOfDay()));
    }

    @Test
    public void implementsComparable() {
        assertTrue(Comparable.class.isAssignableFrom(IntraDayDate.class));
        assertTrue(IntraDayDate.create(today, halfHour).compareTo(
                IntraDayDate.create(today, oneHour)) < 0);
        assertTrue(IntraDayDate.create(today, oneHour).compareTo(
                IntraDayDate.create(tomorrow, halfHour)) < 0);
    }

    @Test
    public void hasMaxAndMinMethods() {
        IntraDayDate a = IntraDayDate.create(today, halfHour);
        IntraDayDate b = IntraDayDate.create(today, oneHour);
        assertThat(IntraDayDate.min(a, b), equalTo(a));
        assertThat(IntraDayDate.max(a, b), equalTo(b));
    }

    @Test
    public void untilTheSameDayReturnsZeroPartialDays() {
        Iterable<PartialDay> days = IntraDayDate.create(today, zero())
                .daysUntil(IntraDayDate.create(today, zero()));
        assertFalse(days.iterator().hasNext());
    }

    @Test
    public void untilTheNextDayReturnsOnePartialDay() {
        IntraDayDate start = IntraDayDate.create(today, zero());
        IntraDayDate end = IntraDayDate.create(tomorrow, zero());
        Iterable<PartialDay> days = start.daysUntil(end);
        assertThat(IntraDayDate.toList(days), delimitedBy(start, end));
    }

    @Test(expected = IllegalArgumentException.class)
    public void untilADayAgo() {
        IntraDayDate start = IntraDayDate.create(today, zero());
        IntraDayDate end = IntraDayDate.create(tomorrow, zero());
        end.daysUntil(start);
    }

    @Test
    public void untilTwoDaysLaterReturnsTwoPartialDays() {
        IntraDayDate start = IntraDayDate.create(today, zero());
        IntraDayDate middle = IntraDayDate.create(today.plusDays(1), zero());
        IntraDayDate end = IntraDayDate.create(today.plusDays(2), zero());
        Iterable<PartialDay> days = start.daysUntil(end);
        assertThat(IntraDayDate.toList(days), delimitedBy(start, middle, end));
    }

    @Test
    public void canNowTheNumberOfDaysBetweenTwoDates() {
        IntraDayDate start = IntraDayDate.create(today, zero());
        IntraDayDate end = IntraDayDate.create(today.plusDays(1), zero());
        assertThat(start.numberOfDaysUntil(end), equalTo(1));
        assertThat(
                start.numberOfDaysUntil(IntraDayDate.create(today, hours(8))),
                equalTo(0));
        assertThat(
                IntraDayDate.create(today, hours(4)).numberOfDaysUntil(
                        IntraDayDate.create(tomorrow, hours(3))), equalTo(0));
        assertThat(
                IntraDayDate.create(today, hours(4)).numberOfDaysUntil(
                        IntraDayDate.create(tomorrow, hours(4))), equalTo(1));
        assertThat(
                IntraDayDate.create(today, hours(4)).numberOfDaysUntil(
                        IntraDayDate.create(tomorrow, hours(5))), equalTo(1));
        assertThat(
                IntraDayDate.create(today, hours(4)).numberOfDaysUntil(
                        IntraDayDate.create(tomorrow.plusDays(1), hours(3))),
                equalTo(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void theEndMustBeEqualOrBiggerThanTheStart() {
        IntraDayDate start = IntraDayDate.create(today, zero());
        IntraDayDate end = IntraDayDate.create(today.plusDays(1), zero());
        assertThat(end.numberOfDaysUntil(start), equalTo(1));
    }

    public void theEndCanBeTheSameAsTheStart() {
        IntraDayDate start = IntraDayDate.create(today, zero());
        assertThat(start.numberOfDaysUntil(start), equalTo(0));
    }

    @Test
    public void worksWellWithEffortDurationsNotZero() {
        IntraDayDate start = IntraDayDate.create(today, halfHour);
        IntraDayDate end = IntraDayDate.create(today, oneHour);
        IntraDayDate anotherEnd = IntraDayDate.create(tomorrow, halfHour);
        assertThat(IntraDayDate.toList(start.daysUntil(end)), delimitedBy(start, end));
        assertThat(IntraDayDate.toList(start.daysUntil(anotherEnd)),
                delimitedBy(start, IntraDayDate.create(tomorrow, zero()),
                        anotherEnd));
    }

    @Test
    public void canHaveAnExtraCondition() {
        IntraDayDate start = IntraDayDate.create(today, zero());
        IntraDayDate end = IntraDayDate.create(today.plusDays(10), zero());
        final List<PartialDay> generated = new ArrayList<IntraDayDate.PartialDay>();
        Iterable<PartialDay> days = start.daysUntil(new UntilEnd(end) {
            @Override
            protected boolean hasNext(boolean lessThanEnd) {
                return lessThanEnd && generated.size() < 2;
            }
        });
        for (PartialDay each : days) {
            generated.add(each);
        }
        assertThat(generated.size(), equalTo(2));
    }

    @Test
    public void aPartialDayCanLimitAWorkingDayDuration() {
        PartialDay day = new PartialDay(IntraDayDate.create(today,
                halfHour), IntraDayDate.create(today, oneHour));
        assertThat(day.limitWorkingDay(hours(10)), equalTo(minutes(30)));
        assertThat(day.limitWorkingDay(minutes(40)), equalTo(minutes(10)));
        PartialDay completeDay = new PartialDay(IntraDayDate.startOfDay(today),
                IntraDayDate.startOfDay(tomorrow));
        assertThat(completeDay.limitWorkingDay(hours(10)), equalTo(hours(10)));
        PartialDay startsInTheMiddle = new PartialDay(IntraDayDate.create(
                today, EffortDuration.hours(3)),
                IntraDayDate.startOfDay(tomorrow));
        assertThat(startsInTheMiddle.limitWorkingDay(hours(10)),
                equalTo(hours(7)));
        assertThat(startsInTheMiddle.limitWorkingDay(hours(3)), equalTo(zero()));
        assertThat(startsInTheMiddle.limitWorkingDay(hours(2)), equalTo(zero()));
        PartialDay startAndEndInSameDay = new PartialDay(IntraDayDate.create(
                today, EffortDuration.hours(3)), IntraDayDate.create(today,
                EffortDuration.hours(6)));
        assertThat(startAndEndInSameDay.limitWorkingDay(hours(4)),
                equalTo(hours(1)));
        assertThat(startAndEndInSameDay.limitWorkingDay(hours(5)),
                equalTo(hours(2)));
        assertThat(startAndEndInSameDay.limitWorkingDay(hours(6)),
                equalTo(hours(3)));
        assertThat(startAndEndInSameDay.limitWorkingDay(hours(10)),
                equalTo(hours(3)));
    }

    private Matcher<Iterable<PartialDay>> delimitedBy(
            final IntraDayDate... dates) {
        return new BaseMatcher<Iterable<PartialDay>>() {

            @Override
            public boolean matches(Object param) {
                List<IntraDayDate> list = toPoints(param);
                return list.equals(Arrays.asList(dates));
            }

            @SuppressWarnings("unchecked")
            private List<IntraDayDate> toPoints(Object param) {
                return toPoints((Iterable<PartialDay>) param);
            }

            private List<IntraDayDate> toPoints(Iterable<PartialDay> param) {
                List<IntraDayDate> result = new ArrayList<IntraDayDate>();
                PartialDay last = null;
                for (PartialDay each : param) {
                    result.add(each.getStart());
                    last = each;
                }
                if (last != null) {
                    result.add(last.getEnd());
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(Arrays.asList(group(dates))
                        .toString());
            }

            private Object group(IntraDayDate[] dates) {
                List<Object> result = new ArrayList<Object>();
                for (int i = 0; i < dates.length - 1; i++) {
                    result.add(Arrays.asList(dates[i], dates[i + 1]));
                }
                return result;
            }
        };
    }

    @Test
    public void roundsUpGoesToTheNextLocalDateOrKeepsTheSame() {
        assertThat(IntraDayDate.create(today, halfHour).roundUp(),
                equalTo(today.plusDays(1)));

        assertThat(IntraDayDate.startOfDay(today).roundUp(), equalTo(today));
    }

    @Test
    public void roundDownGoesToPreviousLocalDateOrKeepsTheSame() {
        assertThat(IntraDayDate.create(today, halfHour).roundDown(),
                equalTo(today));

        assertThat(IntraDayDate.startOfDay(today).roundDown(), equalTo(today));
    }

    @Test
    public void canIncreaseAnIntraDayDayBySomeEffort() {
        LocalDate today = new LocalDate();
        IntraDayDate intraDate = IntraDayDate.create(today, hours(2));
        EffortDuration[] efforts = { hours(4), hours(8), hours(12), hours(0),
                hours(16) };
        for (EffortDuration effort : efforts) {
            IntraDayDate newDay = intraDate.increaseBy(
                    ResourcesPerDay.amount(1), effort);

            assertThat(newDay.getDate(), equalTo(today));
            assertThat(newDay.getEffortDuration(), equalTo(hours(2)
                    .plus(effort)));
        }
    }

    @Test
    public void theAmountOfTheIncreaseDependsOnTheAmountOfResourcesPerDay() {
        LocalDate today = new LocalDate();
        IntraDayDate intraDate = IntraDayDate.startOfDay(today);

        EffortDuration effort = hours(6);

        ResourcesPerDay half = ResourcesPerDay.amount(new BigDecimal(BigInteger
                .valueOf(5), 1));
        ResourcesPerDay oneQuarter = ResourcesPerDay.amount(new BigDecimal(
                BigInteger.valueOf(25), 2));
        ResourcesPerDay[] resourcesPerDays = { ResourcesPerDay.amount(2),
                ResourcesPerDay.amount(3), half, oneQuarter,
                ResourcesPerDay.amount(4) };

        EffortDuration[] ends = { hours(3), hours(2), hours(12), hours(24),
                hours(1).and(30, Granularity.MINUTES) };

        for (int i = 0; i < resourcesPerDays.length; i++) {
            ResourcesPerDay r = resourcesPerDays[i];
            EffortDuration end = ends[i];
            IntraDayDate newDay = intraDate.increaseBy(r, effort);

            assertThat(newDay.getDate(), equalTo(today));
            assertThat(newDay.getEffortDuration(), equalTo(end));
        }
    }

    @Test
    public void canDecreaseAnIntraDayBySomeEffort() {
        LocalDate today = new LocalDate();
        IntraDayDate intraDate = IntraDayDate.create(today, hours(10));
        EffortDuration[] efforts = { hours(4), hours(8), hours(5), hours(10),
                hours(1) };
        for (EffortDuration effort : efforts) {
            IntraDayDate newDay = intraDate.decreaseBy(
                    ResourcesPerDay.amount(1), effort);

            assertThat(newDay.getDate(), equalTo(today));
            assertThat(newDay.getEffortDuration(),
                    equalTo(hours(10).minus(effort)));
        }
    }

    @Test
    public void theDateAlwaysKeepsBeingTheSameWhenDecreasing() {
        LocalDate today = new LocalDate();
        IntraDayDate intraDate = IntraDayDate.create(today, hours(10));
        assertThat(intraDate.decreaseBy(ResourcesPerDay.amount(1), hours(12))
                .getDate(), equalTo(today));
    }

}
