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

package org.navalplanner.business.test.planner.entities;

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
import static org.navalplanner.business.test.planner.entities.DayAssignmentMatchers.consecutiveDays;
import static org.navalplanner.business.test.planner.entities.DayAssignmentMatchers.from;
import static org.navalplanner.business.test.planner.entities.DayAssignmentMatchers.haveHours;
import static org.navalplanner.business.test.planner.entities.DayAssignmentMatchers.haveResourceAllocation;
import static org.navalplanner.business.workingday.EffortDuration.hours;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.planner.entities.ResourceAllocation.DetachDayAssignmentOnRemoval;
import org.navalplanner.business.planner.entities.ResourceAllocation.IOnDayAssignmentRemoval;
import org.navalplanner.business.planner.entities.SpecificDayAssignment;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.IntraDayDate;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;
import org.navalplanner.business.workingday.ResourcesPerDay;

public class SpecificResourceAllocationTest {

    private BaseCalendar baseCalendar;

    private Task task;

    private SpecificResourceAllocation specificResourceAllocation;

    private Worker worker;

    private ResourceCalendar calendar;

    private int assignedHours = 0;

    private void givenAssignedHours(int assignedHours) {
        this.assignedHours = assignedHours;
    }

    private void givenResourceCalendarAlwaysReturning(final int hours) {
        this.calendar = createNiceMock(ResourceCalendar.class);
        expect(this.calendar.getCapacityOn(isA(PartialDay.class)))
                .andReturn(EffortDuration.hours(hours)).anyTimes();
        IAnswer<? extends EffortDuration> asDurationAnswer = asDurationOnAnswer(hours(hours));
        expect(
                this.calendar.asDurationOn(isA(PartialDay.class),
                        isA(ResourcesPerDay.class)))
                .andAnswer(asDurationAnswer).anyTimes();
        expect(this.calendar.getAvailability()).andReturn(
                AvailabilityTimeLine.allValid()).anyTimes();
        replay(this.calendar);
    }

    private IAnswer<Integer> toHoursAnswer(final int hours) {
        final IAnswer<? extends EffortDuration> durationOnAnswer = asDurationOnAnswer(hours(hours));
        return new IAnswer<Integer>() {

            @Override
            public Integer answer() throws Throwable {
                return durationOnAnswer.answer().roundToHours();
            }
        };
    }

    private IAnswer<? extends EffortDuration> asDurationOnAnswer(
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

    private void givenResourceCalendar(final int defaultAnswer, final Map<LocalDate, Integer> answersForDates){
        this.calendar = createNiceMock(ResourceCalendar.class);
        expect(this.calendar.getCapacityOn(isA(PartialDay.class)))
                .andAnswer(new IAnswer<EffortDuration>() {

                    @Override
                    public EffortDuration answer() throws Throwable {
                        PartialDay day = (PartialDay) EasyMock
                                .getCurrentArguments()[0];
                        LocalDate date = day.getDate();
                        if (answersForDates.containsKey(date)) {
                            return EffortDuration.hours(answersForDates
                                    .get(date));
                        }
                        return EffortDuration.hours(defaultAnswer);
                    }
                }).anyTimes();
        final IAnswer<Integer> toHoursAnswer = new IAnswer<Integer>() {

            @Override
            public Integer answer() throws Throwable {
                Object argument = EasyMock
                        .getCurrentArguments()[0];
                LocalDate date;
                if (argument instanceof LocalDate) {
                    date = (LocalDate) argument;
                }else{
                    date = ((PartialDay) argument).getDate();
                }
                int hours;
                if (answersForDates.containsKey(date)) {
                    hours = answersForDates.get(date);
                } else {
                    hours = defaultAnswer;
                }
                return toHoursAnswer(hours).answer();
            }
        };
        IAnswer<EffortDuration> effortAnswer = new IAnswer<EffortDuration>() {
            @Override
            public EffortDuration answer() throws Throwable {
                return hours(toHoursAnswer.answer());
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
        expect(this.worker.getAssignedHours(isA(LocalDate.class))).andReturn(
                assignedHours).anyTimes();
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

    private void givenSpecificResourceAllocation(LocalDate start, LocalDate end) {
        givenSpecificResourceAllocation(IntraDayDate.startOfDay(start),
                IntraDayDate.startOfDay(end));
    }

    private void givenSpecificResourceAllocation(IntraDayDate start,
            IntraDayDate end) {
        givenWorker();
        givenTask(start, end);
        specificResourceAllocation = SpecificResourceAllocation.create(task);
        specificResourceAllocation.setResource(worker);
    }

    private void givenSpecificResourceAllocation(LocalDate start, int days) {
        givenSpecificResourceAllocation(start, start.plusDays(days));
    }

    @Test
    public void theAllocationsDoneAreOrderedByDay() {
        givenSpecificResourceAllocation(new LocalDate(2000, 2, 4), 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(),
                consecutiveDays(2));
    }

    @Test
    public void theAllocationsDoneHaveAsParentTheAllocation() {
        givenSpecificResourceAllocation(new LocalDate(2000, 2, 4), 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(),
                haveResourceAllocation(specificResourceAllocation));
    }

    @Test
    public void theAllocationStartsAtTheStartDate() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(), from(start));
    }

    @Test
    public void theAllocationIsDoneEvenIfThereisOvertime() {
        givenAssignedHours(4);
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(), haveHours(8, 8));
    }

    @Test
    public void canAllocateUntilSomeEndDate() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.resourcesPerDayUntil(start.plusDays(3))
                .allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(), haveHours(8, 8,
                8));
    }

    @Test
    public void canAllocateFromEndUntilSomeStartDate() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.resourcesPerDayFromEndUntil(start).allocate(
                ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(8, 8, 8, 8));
    }

    @Test
    public void canAllocateFromEndInTheMiddleOfTheDayToStartDate() {
        LocalDate start = new LocalDate(2000, 2, 4);
        IntraDayDate end = IntraDayDate.create(start.plusDays(3), hours(3));
        givenSpecificResourceAllocation(IntraDayDate.startOfDay(start), end);
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
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(), haveHours(4, 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantAllocateOnAnWrongInterval() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        LocalDate dayBefore = start.plusDays(-1);
        specificResourceAllocation.onInterval(start, dayBefore).allocateHours(
                10);
    }

    @Test
    public void canAllocateZeroHours() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onInterval(start, start.plusDays(2))
                .allocateHours(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantAllocateNegativeHours() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onInterval(start, start.plusDays(1))
                .allocateHours(-1);
    }

    @Test
    public void someHoursInAnIntervalCanBeAssigned() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onInterval(start, start.plusDays(2))
                .allocateHours(10);
        assertThat(specificResourceAllocation.getAssignments(), haveHours(5, 5));
    }

    @Test
    public void thePartOfTheIntervalUsedIsTheOneOverlapping() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onInterval(start.plusDays(1),
                start.plusDays(6)).allocateHours(12);
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(4, 4, 4));
    }

    @Test
    public void ifTheProvidedIntervalIsAfterTheTaskDoesntAllocateAnything() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onInterval(start.plusDays(5),
                start.plusDays(6)).allocateHours(12);
        assertTrue(specificResourceAllocation.getAssignments().isEmpty());
    }

    @Test
    public void ifTheProvidedIntervalIsBeforeTheTaskDoesntAllocateAnything() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onInterval(start.minusDays(5),
                start.minusDays(2)).allocateHours(12);
        assertTrue(specificResourceAllocation.getAssignments().isEmpty());
    }

    @Test
    public void thePartOfTheIntervalUsedIsTheOneOverlappingWithTheTask() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);

        specificResourceAllocation.fromStartUntil(start.plusDays(2))
                .allocateHours(16);
        specificResourceAllocation.onInterval(start, start.plusDays(6))
                .allocateHours(12);

        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(3, 3, 3, 3));
    }

    @Test
    public void theEndIsNotChangedIfAZeroAllocationIsDoneInTheLastDay() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        LocalDate end = start.plusDays(4);

        specificResourceAllocation.fromStartUntil(end).allocateHours(32);
        specificResourceAllocation.onInterval(start.plusDays(3), end)
                .allocateHours(0);

        assertThat(specificResourceAllocation.getIntraDayEndDate(),
                equalTo(IntraDayDate.startOfDay(end)));
    }

    @Test
    public void theEndCanGrowUntilReachingTheEndOfTheTask() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        LocalDate end = start.plusDays(4);

        specificResourceAllocation.fromStartUntil(end.minusDays(1))
                .allocateHours(24);
        assertThat(specificResourceAllocation.getIntraDayEndDate(),
                equalTo(IntraDayDate.startOfDay(end.minusDays(1))));

        specificResourceAllocation.onInterval(start, end).allocateHours(32);
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(8, 8, 8, 8));
        assertThat(specificResourceAllocation.getIntraDayEndDate(),
                equalTo(IntraDayDate.startOfDay(end)));
    }

    @Test
    public void theStartIsNotChangedIfAZeroAllocationIsDoneInTheFirstDay() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.fromStartUntil(start.plusDays(4))
                .allocateHours(32);
        specificResourceAllocation.onInterval(start, start.plusDays(1))
                .allocateHours(0);
        assertThat(specificResourceAllocation.getIntraDayStartDate(),
                equalTo(IntraDayDate.startOfDay(start)));
    }

    @Test
    public void canAssignFromStartUntilEnd() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.fromStartUntil(start.plusDays(4))
                .allocateHours(32);
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(8, 8, 8, 8));
        assertThat(specificResourceAllocation.getResourcesPerDay(),
                equalTo(ResourcesPerDay.amount(1)));
    }

    @Test
    public void canAssignFromEndUntilStart() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.fromEndUntil(start).allocateHours(32);
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(8, 8, 8, 8));
        assertThat(specificResourceAllocation.getResourcesPerDay(),
                equalTo(ResourcesPerDay.amount(1)));
    }

    @Test
    public void ifEndIsInTheMiddleOfADayFromEndUntilStartCalculatesResourcesPerDayCorrectly() {
        LocalDate start = new LocalDate(2000, 2, 4);
        IntraDayDate end = IntraDayDate.create(start.plusDays(3), hours(4));
        givenSpecificResourceAllocation(IntraDayDate.startOfDay(start), end);
        specificResourceAllocation.fromEndUntil(start).allocateHours(28);
        assertThat(specificResourceAllocation.getAssignments(),
                haveHours(8, 8, 8, 4));
        assertThat(specificResourceAllocation.getResourcesPerDay(),
                equalTo(ResourcesPerDay.amount(1)));
    }

    @Test
    public void canBeNotifiedWhenADayAssignmentIsRemoved() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onInterval(start, start.plusDays(2))
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
        specificResourceAllocation.onInterval(start, start.plusDays(2))
                .allocateHours(10);
        verify(dayAssignmentRemovalMock);
    }

    @Test
    public void canAutomaticallyDetachDayAssignmentsWhenRemoved() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onInterval(start, start.plusDays(2))
                .allocateHours(10);
        List<SpecificDayAssignment> assignments = specificResourceAllocation
                .getAssignments();
        for (SpecificDayAssignment each : assignments) {
            assertThat(each.getSpecificResourceAllocation(), notNullValue());
        }

        specificResourceAllocation
                .setOnDayAssignmentRemoval(new DetachDayAssignmentOnRemoval());
        specificResourceAllocation.onInterval(start, start.plusDays(2))
                .allocateHours(10);

        for (SpecificDayAssignment each : assignments) {
            assertThat(each.getSpecificResourceAllocation(), nullValue());
        }
    }

    @Test
    public void thePreviousAssignmentsAreReplacedWhenAllocationHoursOnInterval() {
        givenResourceCalendarAlwaysReturning(3);
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        specificResourceAllocation.onInterval(start, start.plusDays(2))
                .allocateHours(10);
        assertThat(specificResourceAllocation.getAssignments(), haveHours(5, 5,
                3, 3));
    }

    @Test
    public void allocatingZeroHoursOnIntervalRemovesThem() {
        givenResourceCalendarAlwaysReturning(3);
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        specificResourceAllocation.onInterval(start, start.plusDays(2))
                .allocateHours(0);
        assertThat(specificResourceAllocation.getAssignments(), from(start
                .plusDays(2)));
    }

    @Test
    public void theResourcesPerDayAreRecalculatedWhenAllocationHoursOnInterval() {
        givenResourceCalendarAlwaysReturning(3);
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        ResourcesPerDay original = ResourcesPerDay.amount(1);
        specificResourceAllocation.allocate(original);
        specificResourceAllocation.onInterval(start, start.plusDays(2))
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
        final LocalDate start = new LocalDate(2000, 2, 4);
        givenResourceCalendar(8, new HashMap<LocalDate, Integer>() {
            {
                put(start.plusDays(3), 0);
            }
        });
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.resourcesPerDayUntil(start.plusDays(4))
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
        specificResourceAllocation.resourcesPerDayUntil(end.asExclusiveEnd())
                .allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getResourcesPerDay(),
                equalTo(ResourcesPerDay.amount(1)));
    }

    @Test
    public void theHoursAreDistributedTakingIntoAccountTheWorkableHours() {
        final LocalDate start = new LocalDate(2000, 2, 4);
        givenResourceCalendar(8, new HashMap<LocalDate, Integer>() {
            {
                put(start, 2);
            }
        });
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onInterval(start, start.plusDays(2))
                .allocateHours(10);
        assertThat(specificResourceAllocation.getAssignments(), haveHours(2, 8));
    }

    @SuppressWarnings("serial")
    @Test
    public void youCanAllocateHoursPreservingTheCurrentShape() {
        final LocalDate start = new LocalDate(2000, 2, 4);
        givenResourceCalendar(8, new HashMap<LocalDate, Integer>() {
            {
                put(start, 2);
                put(start.plusDays(1), 4);
                put(start.plusDays(3), 6);
            }
        });
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.onInterval(start, start.plusDays(4))
                .allocateHours(20);
        assertThat(specificResourceAllocation.getAssignments(), haveHours(2, 4,
                8, 6));

        specificResourceAllocation.allocateKeepingProportions(start, start
                .plusDays(2), 9);

        assertThat(specificResourceAllocation.getAssignments(), haveHours(3, 6,
                8, 6));
    }

    @Test
    public void theEndDateOfTheAllocationIsExclusive() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getEndDate(),
                equalTo(new LocalDate(2000, 2, 6)));
    }

    @Test
    public void theAllocationIsFinishedByEndDate() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        assertTrue(specificResourceAllocation
                .isAlreadyFinishedBy(specificResourceAllocation.getEndDate()));
    }

}
