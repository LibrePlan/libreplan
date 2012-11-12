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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.verify;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.test.planner.entities.DayAssignmentMatchers.consecutiveDays;
import static org.libreplan.business.test.planner.entities.DayAssignmentMatchers.from;
import static org.libreplan.business.test.planner.entities.DayAssignmentMatchers.haveHours;
import static org.libreplan.business.test.planner.entities.DayAssignmentMatchers.haveResourceAllocation;
import static org.libreplan.business.workingday.EffortDuration.hours;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.calendars.entities.Capacity;
import org.libreplan.business.calendars.entities.ResourceCalendar;
import org.libreplan.business.planner.entities.DayAssignment;
import org.libreplan.business.planner.entities.ResourceAllocation.DetachDayAssignmentOnRemoval;
import org.libreplan.business.planner.entities.ResourceAllocation.IOnDayAssignmentRemoval;
import org.libreplan.business.planner.entities.SpecificDayAssignment;
import org.libreplan.business.planner.entities.SpecificResourceAllocation;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;
import org.libreplan.business.workingday.ResourcesPerDay;

public class SpecificResourceAllocationTest {

    public static IntraDayDate date(int year, int monthOfYear, int dayOfMonth) {
        return IntraDayDate.startOfDay(new LocalDate(year, monthOfYear,
                dayOfMonth));
    }

    public static IntraDayDate plusDays(IntraDayDate date, int days) {
        return IntraDayDate.create(date.getDate().plusDays(days),
                date.getEffortDuration());
    }

    public static IntraDayDate minusDays(IntraDayDate date, int days) {
        return IntraDayDate.create(date.getDate().minusDays(days),
                date.getEffortDuration());
    }

    public static IntraDayDate plusDaysAndEffort(IntraDayDate date, int days,
            EffortDuration effort) {
        return IntraDayDate.create(date.getDate().plusDays(days), date
                .getEffortDuration().plus(effort));
    }

    private BaseCalendar baseCalendar;

    private Task task;

    private SpecificResourceAllocation specificResourceAllocation;

    private Worker worker;

    private ResourceCalendar calendar;

    private void givenResourceCalendarAlwaysReturning(final int hours) {
        this.calendar = createResourceCalendarAlwaysReturning(hours);
    }

    public static ResourceCalendar createResourceCalendarAlwaysReturning(final int hours) {
        ResourceCalendar workerCalendar;
        workerCalendar = createNiceMock(ResourceCalendar.class);
        expect(workerCalendar.getCapacityOn(isA(PartialDay.class)))
        .andReturn(EffortDuration.hours(hours)).anyTimes();
        IAnswer<? extends EffortDuration> asDurationAnswer = asDurationOnAnswer(hours(hours));
        expect(
                workerCalendar.asDurationOn(isA(PartialDay.class),
                        isA(ResourcesPerDay.class)))
                        .andAnswer(asDurationAnswer).anyTimes();
        expect(workerCalendar.getCapacityWithOvertime(isA(LocalDate.class)))
        .andReturn(
                Capacity.create(hours(hours))
                .overAssignableWithoutLimit()).anyTimes();
        expect(workerCalendar.getAvailability()).andReturn(
                AvailabilityTimeLine.allValid()).anyTimes();
        replay(workerCalendar);
        return workerCalendar;
    }

    private static IAnswer<? extends EffortDuration> asDurationOnAnswer(
            final EffortDuration duration) {
        return new IAnswer<EffortDuration>() {

            @Override
            public EffortDuration answer() throws Throwable {
                ResourcesPerDay perDay = (ResourcesPerDay) EasyMock
                        .getCurrentArguments()[1];
                return perDay.asDurationGivenWorkingDayOf(duration);
            }
        };
    }

    private void givenResourceCalendar(final Capacity defaultAnswer,
            final Map<LocalDate, Capacity> answersForDates) {
        this.calendar = createNiceMock(ResourceCalendar.class);
        expect(this.calendar.getCapacityOn(isA(PartialDay.class)))
                .andAnswer(new IAnswer<EffortDuration>() {

                    @Override
                    public EffortDuration answer() throws Throwable {
                        PartialDay day = (PartialDay) EasyMock
                                .getCurrentArguments()[0];
                        LocalDate date = day.getDate();
                        if (answersForDates.containsKey(date)) {
                            return day.limitWorkingDay(answersForDates
                                    .get(date).getStandardEffort());
                        }
                        return day.limitWorkingDay(defaultAnswer
                                .getStandardEffort());
                    }
                }).anyTimes();

        expect(this.calendar.getCapacityWithOvertime(isA(LocalDate.class)))
                .andAnswer(new IAnswer<Capacity>() {

                    @Override
                    public Capacity answer() throws Throwable {
                        LocalDate date = (LocalDate) EasyMock
                                .getCurrentArguments()[0];
                        if (answersForDates.containsKey(date)) {
                            return answersForDates.get(date);
                        }
                        return defaultAnswer;
                    }
                }).anyTimes();
        final IAnswer<EffortDuration> effortAnswer = new IAnswer<EffortDuration>() {

            @Override
            public EffortDuration answer() throws Throwable {
                PartialDay day = (PartialDay) EasyMock
                        .getCurrentArguments()[0];
                ResourcesPerDay resourcesPerDay = (ResourcesPerDay) EasyMock
                        .getCurrentArguments()[1];

                LocalDate date = day.getDate();
                Capacity capacity = answersForDates.containsKey(date) ? answersForDates
                        .get(date) : defaultAnswer;

                EffortDuration oneResourcePerDayWorkingDuration = day
                        .limitWorkingDay(capacity.getStandardEffort());
                EffortDuration amountRequestedDuration = resourcesPerDay
                        .asDurationGivenWorkingDayOf(oneResourcePerDayWorkingDuration);
                return capacity.limitDuration(amountRequestedDuration);
            }
        };
        expect(
                this.calendar.asDurationOn(isA(PartialDay.class),
                        isA(ResourcesPerDay.class))).andAnswer(effortAnswer)
                .anyTimes();
        expect(this.calendar.getAvailability()).andReturn(
                AvailabilityTimeLine.allValid()).anyTimes();
        replay(this.calendar);
    }

    private void givenWorker() {
        this.worker = createNiceMock(Worker.class);
        expect(this.worker.getCalendar()).andReturn(calendar).anyTimes();
        replay(this.worker);
    }

    private void givenTask(IntraDayDate start, IntraDayDate end) {
        task = createNiceMock(Task.class);
        expect(task.getCalendar()).andReturn(baseCalendar).anyTimes();
        expect(task.getStartDate()).andReturn(
                start.toDateTimeAtStartOfDay().toDate()).anyTimes();
        expect(task.getIntraDayStartDate()).andReturn(start).anyTimes();
        expect(task.getEndDate()).andReturn(
                end.toDateTimeAtStartOfDay().toDate()).anyTimes();
        expect(task.getIntraDayEndDate()).andReturn(end).anyTimes();
        expect(task.getFirstDayNotConsolidated()).andReturn(start).anyTimes();
        replay(task);
    }

    private void givenSpecificResourceAllocation(IntraDayDate start,
            IntraDayDate end) {
        givenWorker();
        givenTask(start, end);
        specificResourceAllocation = SpecificResourceAllocation.create(task);
        specificResourceAllocation.setResource(worker);
    }

    private void givenSpecificResourceAllocation(IntraDayDate start, int days) {
        givenSpecificResourceAllocation(start, plusDays(start, days));
    }

    @Test
    public void theAllocationsDoneAreOrderedByDay() {
        givenSpecificResourceAllocation(date(2000, 2, 4), 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(),
                consecutiveDays(2));
    }

    @Test
    public void theAllocationsDoneHaveAsParentTheAllocation() {
        givenSpecificResourceAllocation(date(2000, 2, 4), 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(),
                haveResourceAllocation(specificResourceAllocation));
    }

    @Test
    public void theAllocationStartsAtTheStartDate() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(), from(start));
    }

    @Test
    public void theAllocationIsDoneEvenIfThereisOvertimeIfCapacitiesAreOverAssignable() {
        givenResourceCalendar(Capacity.create(hours(4))
                .overAssignableWithoutLimit(),
                Collections.<LocalDate, Capacity> emptyMap());
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(2));
        assertThat(specificResourceAllocation.getAssignments(), haveHours(8, 8));
    }

    @Test
    public void ifNotOverassignableItOnlyDoesExtraEffortSpecified() {
        givenResourceCalendar(
                Capacity.create(hours(4)).withAllowedExtraEffort(hours(2)),
                Collections.<LocalDate, Capacity> emptyMap());
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(2));
        assertThat(specificResourceAllocation.getAssignments(), haveHours(6, 6));
    }

    @Test
    public void canAllocateUntilSomeEndDate() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.resourcesPerDayUntil(plusDays(start, 3))
                .allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(), haveHours(8, 8,
                8));
    }

    @Test
    public void initiallyTheIntendedResourcesPerDayAreNull() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        assertThat(specificResourceAllocation.getIntendedResourcesPerDay(),
                nullValue());
    }

    @Test
    public void afterAllocatingSomeResourcesPerDayTheIntendedResourcesPerDayAreNotNull() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        ResourcesPerDay specified = ResourcesPerDay.amount(1);
        specificResourceAllocation.resourcesPerDayUntil(plusDays(start, 3))
                .allocate(specified);
        assertThat(specificResourceAllocation.getIntendedResourcesPerDay(),
                equalTo(specified));
    }

    @Test
    public void theIntendedAndTheRealResourcesPerDayCanBeDifferent() {
        givenResourceCalendar(
                Capacity.create(hours(8)).withAllowedExtraEffort(hours(2)),
                Collections.<LocalDate, Capacity> emptyMap());

        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);

        specificResourceAllocation.resourcesPerDayUntil(plusDays(start, 3))
                .allocate(ResourcesPerDay.amount(2));

        BigDecimal intentededAmount = specificResourceAllocation
                .getIntendedResourcesPerDay().getAmount();
        BigDecimal realAmount = specificResourceAllocation.getResourcesPerDay()
                .getAmount();
        assertTrue(intentededAmount.compareTo(realAmount) > 0);
    }

    @Test
    public void canAllocateFromEndUntilSomeStartDate() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.resourcesPerDayFromEndUntil(start).allocate(
                ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(8, 8, 8, 8));
    }

    @Test
    public void canAllocateFromEndInTheMiddleOfTheDayToStartDate() {
        IntraDayDate start = date(2000, 2, 4);
        IntraDayDate end = plusDaysAndEffort(start, 3, hours(3));
        givenSpecificResourceAllocation(start, end);
        specificResourceAllocation.resourcesPerDayFromEndUntil(start).allocate(
                ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(8, 8, 8, 3));
        assertThat(specificResourceAllocation.getResourcesPerDay(),
                equalTo(ResourcesPerDay.amount(1)));
    }

    @Test
    public void theResourcesPerDayAreConvertedTakingIntoAccountTheWorkerCalendar() {
        givenResourceCalendarAlwaysReturning(4);
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(), haveHours(4, 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantAllocateOnAnWrongInterval() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        IntraDayDate dayBefore = minusDays(start, 1);
        specificResourceAllocation.onIntervalWithinTask(start, dayBefore).allocateHours(
                10);
    }

    @Test
    public void canAllocateZeroHours() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onIntervalWithinTask(start,
                plusDays(start, 2))
                .allocateHours(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantAllocateNegativeHours() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onIntervalWithinTask(start,
                plusDays(start, 1))
                .allocateHours(-1);
    }

    @Test
    public void someHoursInAnIntervalCanBeAssigned() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onIntervalWithinTask(start,
                plusDays(start, 2))
                .allocateHours(10);
        assertThat(specificResourceAllocation.getAssignments(), haveHours(5, 5));
    }

    @Test
    public void theHoursForEachDayCanBeAssigned() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onIntervalWithinTask(start,
                plusDays(start, 4)).allocate(
                Arrays.asList(hours(4), hours(8), hours(4), hours(8)));
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(4, 8, 4, 8));
    }

    @Test
    public void ifLessDaysAreSpecifiedTheInitialDaysAreAllocated() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onIntervalWithinTask(start,
                plusDays(start, 4)).allocate(
                Arrays.asList(hours(4), hours(8), hours(4)));
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(4, 8, 4));
    }

    @Test
    public void ifMoreDaysAreSpecifiedTheInitialDaysAreAllocated() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onIntervalWithinTask(start,
                plusDays(start, 4))
                .allocate(
                        Arrays.asList(hours(4), hours(8), hours(4), hours(4),
                                hours(3)));
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(4, 8, 4, 4));
    }

    @Test
    public void theDaysSpecifiedOutsideBoundsAreDiscarded() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onIntervalWithinTask(minusDays(start, 2),
                plusDays(start, 1)).allocate(
                Arrays.asList(hours(2), hours(3), hours(4)));
        assertThat(specificResourceAllocation.getAssignments(), haveHours(4));
    }

    @Test
    public void combineOutsideBoundsAndZeroPadding() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onIntervalWithinTask(minusDays(start, 2),
                plusDays(start, 1)).allocate(Arrays.asList(hours(2), hours(3)));
        assertThat(specificResourceAllocation.getAssignments(), haveHours());
    }

    @Test
    public void theDaysSpecifiedOutsideTheTaskAreDiscarded() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onIntervalWithinTask(minusDays(start, 1),
                plusDays(start, 4)).allocate(
                Arrays.asList(hours(10), hours(4), hours(8), hours(4),
                        hours(4), hours(3)));
        List<SpecificDayAssignment> assigments = specificResourceAllocation
                .getAssignments();
        assertThat(assigments, haveHours(4, 8, 4, 4));
    }

    @Test
    public void theIntervalWithinTaskCanBeMadeOfIntraDayDates() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        IntraDayDate startInterval = start;
        IntraDayDate endInterval = plusDaysAndEffort(start, 2,
                EffortDuration.hours(4));
        specificResourceAllocation.onIntervalWithinTask(startInterval,
                endInterval).allocateHours(12);
        List<SpecificDayAssignment> assignments = specificResourceAllocation
                .getAssignments();
        assertThat(DayAssignment.sum(assignments), equalTo(hours(12)));
        assertTrue(assignments.get(0).getDuration()
                .compareTo(assignments.get(2).getDuration()) > 0);
        assertTrue(assignments.get(1).getDuration()
                .compareTo(assignments.get(2).getDuration()) > 0);
    }

    @Test
    public void thePartOfTheIntervalUsedIsTheOneOverlapping() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onIntervalWithinTask(plusDays(start, 1),
                plusDays(start, 6)).allocateHours(12);
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(4, 4, 4));
    }

    @Test
    public void ifTheProvidedIntervalIsAfterTheTaskDoesntAllocateAnything() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onIntervalWithinTask(plusDays(start, 5),
                plusDays(start, 6)).allocateHours(12);
        assertTrue(specificResourceAllocation.getAssignments().isEmpty());
    }

    @Test
    public void ifTheProvidedIntervalIsBeforeTheTaskDoesntAllocateAnything() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onIntervalWithinTask(minusDays(start, 5),
                minusDays(start, 2)).allocateHours(12);
        assertTrue(specificResourceAllocation.getAssignments().isEmpty());
    }

    @Test
    public void thePartOfTheIntervalUsedIsTheOneOverlappingWithTheTask() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);

        specificResourceAllocation.fromStartUntil(plusDays(start, 2))
                .allocateHours(16);
        specificResourceAllocation.onIntervalWithinTask(start,
                plusDays(start, 6))
                .allocateHours(12);

        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(3, 3, 3, 3));
    }

    @Test
    public void theEndIsNotChangedIfAZeroAllocationIsDoneInTheLastDay() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        IntraDayDate end = plusDays(start, 4);

        specificResourceAllocation.fromStartUntil(end).allocateHours(32);
        specificResourceAllocation
                .onIntervalWithinTask(plusDays(start, 3), end)
                .allocateHours(0);

        assertThat(specificResourceAllocation.getIntraDayEndDate(),
                equalTo(end));
    }

    @Test
    public void theEndCanGrowUntilReachingTheEndOfTheTask() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        IntraDayDate end = plusDays(start, 4);

        specificResourceAllocation.fromStartUntil(minusDays(end, 1))
                .allocateHours(24);
        assertThat(specificResourceAllocation.getIntraDayEndDate(),
                equalTo(minusDays(end, 1)));

        specificResourceAllocation.onIntervalWithinTask(start, end).allocateHours(32);
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(8, 8, 8, 8));
        assertThat(specificResourceAllocation.getIntraDayEndDate(),
                equalTo(end));
    }

    @Test
    public void theStartIsNotChangedIfAZeroAllocationIsDoneInTheFirstDay() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.fromStartUntil(plusDays(start, 4))
                .allocateHours(32);
        specificResourceAllocation.onIntervalWithinTask(start,
                plusDays(start, 1))
                .allocateHours(0);
        assertThat(specificResourceAllocation.getIntraDayStartDate(),
                equalTo(start));
    }

    @Test
    public void thereIsAWayForAllocatingOutsideTheBoundsOfTheTask() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.fromStartUntil(plusDays(start, 4))
                .allocateHours(32);

        specificResourceAllocation.onInterval(plusDays(start, 5),
                plusDays(start, 6)).allocateHours(8);

        assertThat(specificResourceAllocation.getIntraDayEndDate(),
                equalTo(plusDays(start, 6)));
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(8, 8, 8, 8, 8));
    }

    @Test
    public void canAllocateOutsideTheBoundsUsingAnIntervalMadeOfIntraDayDates() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onInterval(start,
                IntraDayDate.create(start.getDate().plusDays(4), hours(4)))
                .allocateHours(36);
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(8, 8, 8, 8, 4));
    }

    @Test
    public void canAllocateOutsideTheBoundsSpecifyingTheHoursForEachDay() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onInterval(minusDays(start, 1),
                plusDaysAndEffort(start, 4, hours(4)))
                .allocate(
                        Arrays.asList(hours(8), hours(2), hours(8), hours(8),
                                hours(8),
                        hours(4)));
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(8, 2, 8, 8, 8, 4));
    }

    @Test
    public void allocatingZeroHoursAtTheEndShrinksTheAllocation() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.fromStartUntil(plusDays(start, 4))
                .allocateHours(32);

        specificResourceAllocation.onInterval(plusDays(start, 3),
                plusDays(start, 4)).allocateHours(0);

        assertThat(specificResourceAllocation.getIntraDayEndDate(),
                equalTo(plusDays(start, 3)));
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(8, 8, 8));
    }

    @Test
    public void allocatingZeroHoursAtTheStartShrinksTheAllocation() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.fromStartUntil(plusDays(start, 4))
                .allocateHours(32);

        specificResourceAllocation.onInterval(start, plusDays(start, 1))
                .allocateHours(0);

        assertThat(specificResourceAllocation.getIntraDayStartDate(),
                equalTo(plusDays(start, 1)));
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(8, 8, 8));
    }

    @Test
    public void canAssignFromStartUntilEnd() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.fromStartUntil(plusDays(start, 4))
                .allocateHours(32);
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(8, 8, 8, 8));
        assertThat(specificResourceAllocation.getResourcesPerDay(),
                equalTo(ResourcesPerDay.amount(1)));
    }

    @Test
    public void canAssignFromEndUntilStart() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.fromEndUntil(start).allocateHours(32);
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(8, 8, 8, 8));
        assertThat(specificResourceAllocation.getResourcesPerDay(),
                equalTo(ResourcesPerDay.amount(1)));
    }

    @Test
    public void ifEndIsInTheMiddleOfADayFromEndUntilStartCalculatesResourcesPerDayCorrectly() {
        IntraDayDate start = date(2000, 2, 4);
        IntraDayDate end = plusDaysAndEffort(start, 3, hours(4));
        givenSpecificResourceAllocation(start, end);
        specificResourceAllocation.fromEndUntil(start).allocateHours(28);
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(8, 8, 8, 4));
        assertThat(specificResourceAllocation.getResourcesPerDay(),
                equalTo(ResourcesPerDay.amount(1)));
    }

    @Test
    public void canBeNotifiedWhenADayAssignmentIsRemoved() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onIntervalWithinTask(start,
                plusDays(start, 2))
                .allocateHours(10);
        List<SpecificDayAssignment> currentAssignments = specificResourceAllocation
                .getAssignments();
        IOnDayAssignmentRemoval dayAssignmentRemovalMock = createMock(IOnDayAssignmentRemoval.class);
        for (SpecificDayAssignment each : currentAssignments) {
            dayAssignmentRemovalMock
                    .onRemoval(specificResourceAllocation, each);
            expectLastCall().once();
        }
        specificResourceAllocation
                .setOnDayAssignmentRemoval(dayAssignmentRemovalMock);
        replay(dayAssignmentRemovalMock);
        specificResourceAllocation.onIntervalWithinTask(start,
                plusDays(start, 2))
                .allocateHours(10);
        verify(dayAssignmentRemovalMock);
    }

    @Test
    public void canAutomaticallyDetachDayAssignmentsWhenRemoved() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onIntervalWithinTask(start,
                plusDays(start, 2))
                .allocateHours(10);
        List<SpecificDayAssignment> assignments = specificResourceAllocation
                .getAssignments();
        for (SpecificDayAssignment each : assignments) {
            assertThat(each.getSpecificResourceAllocation(), notNullValue());
        }

        specificResourceAllocation
                .setOnDayAssignmentRemoval(new DetachDayAssignmentOnRemoval());
        specificResourceAllocation.onIntervalWithinTask(start,
                plusDays(start, 2))
                .allocateHours(10);

        for (SpecificDayAssignment each : assignments) {
            assertThat(each.getSpecificResourceAllocation(), nullValue());
        }
    }

    @Test
    public void thePreviousAssignmentsAreReplacedWhenAllocationHoursOnInterval() {
        givenResourceCalendarAlwaysReturning(3);
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        specificResourceAllocation.onIntervalWithinTask(start,
                plusDays(start, 2))
                .allocateHours(10);
        assertThat(specificResourceAllocation.getAssignments(), haveHours(5, 5,
                3, 3));
    }

    @Test
    public void allocatingZeroHoursOnIntervalRemovesThem() {
        givenResourceCalendarAlwaysReturning(3);
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        specificResourceAllocation.onIntervalWithinTask(start,
                plusDays(start, 2))
                .allocateHours(0);
        assertThat(specificResourceAllocation.getAssignments(),
                from(plusDays(start, 2)));
    }

    @Test
    public void theResourcesPerDayAreRecalculatedWhenAllocationHoursOnInterval() {
        givenResourceCalendarAlwaysReturning(3);
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        ResourcesPerDay original = ResourcesPerDay.amount(1);
        specificResourceAllocation.allocate(original);
        specificResourceAllocation.onIntervalWithinTask(start,
                plusDays(start, 2))
                .allocateHours(10);
        ResourcesPerDay newResourcesPerDay = specificResourceAllocation
                .getResourcesPerDay();
        assertTrue("Expecting that the resources per day is increased",
                newResourcesPerDay
                .getAmount().compareTo(original.getAmount()) > 0);
    }

    @Test
    @SuppressWarnings("serial")
    public void theResourcesPerDayAreTheOnesSpecifiedEvenIfInTheLastDayNoAllocationCanBeDone() {
        final IntraDayDate start = date(2000, 2, 4);
        givenResourceCalendar(Capacity.create(hours(8))
                .overAssignableWithoutLimit(),
                new HashMap<LocalDate, Capacity>() {
            {
                        put(plusDays(start, 3).getDate(),
                                Capacity.create(hours(8))
                                .notOverAssignableWithoutLimit());
            }
        });
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.resourcesPerDayUntil(plusDays(start, 4))
                .allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getResourcesPerDay(),
                equalTo(ResourcesPerDay.amount(1)));
    }

    @Test
    public void theResourcesPerDayAreTheOnesSpecifiedEvenIfTheStartIsInTheMiddleOfTheDay() {
        final IntraDayDate start = IntraDayDate.create(
                new LocalDate(2000, 2, 4), hours(4));
        givenResourceCalendarAlwaysReturning(8);
        IntraDayDate end = IntraDayDate.startOfDay(start.getDate().plusDays(4));
        givenSpecificResourceAllocation(start, end);
        specificResourceAllocation.resourcesPerDayUntil(end)
                .allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getResourcesPerDay(),
                equalTo(ResourcesPerDay.amount(1)));
    }

    @Test
    public void theHoursAreDistributedTakingIntoAccountTheWorkableHours() {
        final IntraDayDate start = date(2000, 2, 4);
        givenResourceCalendar(Capacity.create(hours(8))
                .overAssignableWithoutLimit(),
                new HashMap<LocalDate, Capacity>() {
            {
                        put(start.getDate(), Capacity.create(hours(2))
                                .notOverAssignableWithoutLimit());
            }
        });
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onIntervalWithinTask(start,
                plusDays(start, 2))
                .allocateHours(10);
        assertThat(specificResourceAllocation.getAssignments(), haveHours(2, 8));
    }

    @SuppressWarnings("serial")
    @Test
    public void youCanAllocateHoursPreservingTheCurrentShape() {
        final IntraDayDate start = date(2000, 2, 4);
        givenResourceCalendar(Capacity.create(hours(8))
                .overAssignableWithoutLimit(),
                new HashMap<LocalDate, Capacity>() {
            {
                        put(start.getDate(), Capacity.create(hours(2))
                                .notOverAssignableWithoutLimit());
                        put(plusDays(start, 1).getDate(),
                                Capacity.create(hours(4))
                                .notOverAssignableWithoutLimit());
                        put(plusDays(start, 3).getDate(),
                                Capacity.create(hours(6))
                                .notOverAssignableWithoutLimit());
            }
        });
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onIntervalWithinTask(start,
                plusDays(start, 4))
                .allocateHours(20);
        assertThat(specificResourceAllocation.getAssignments(), haveHours(2, 4,
                8, 6));

        specificResourceAllocation.allocateWholeAllocationKeepingProportions(
                EffortDuration.hours(18), plusDays(start, 2));

        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(6, 12));
    }

    @Test
    public void theEndDateOfTheAllocationIsExclusive() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getIntraDayEndDate(),
                equalTo(date(2000, 2, 6)));
    }

    @Test
    public void theAllocationIsFinishedByEndDate() {
        IntraDayDate start = date(2000, 2, 4);
        givenSpecificResourceAllocation(start, 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        assertTrue(specificResourceAllocation
                .isAlreadyFinishedBy(specificResourceAllocation.getEndDate()));
    }

}
