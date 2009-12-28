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

package org.navalplanner.business.test.planner.entities;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.test.planner.entities.DayAssignmentMatchers.consecutiveDays;
import static org.navalplanner.business.test.planner.entities.DayAssignmentMatchers.from;
import static org.navalplanner.business.test.planner.entities.DayAssignmentMatchers.haveHours;
import static org.navalplanner.business.test.planner.entities.DayAssignmentMatchers.haveResourceAllocation;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Worker;

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

    private void givenResourceCalendarAlwaysReturning(int hours) {
        this.calendar = createNiceMock(ResourceCalendar.class);
        expect(this.calendar.getCapacityAt(isA(LocalDate.class))).andReturn(
                hours).anyTimes();
        expect(this.calendar.getWorkableHours(isA(Date.class)))
                .andReturn(hours).anyTimes();
        replay(this.calendar);
    }

    private void givenResourceCalendar(final int defaultAnswer, final Map<LocalDate, Integer> answersForDates){
        this.calendar = createNiceMock(ResourceCalendar.class);
        expect(this.calendar.getCapacityAt(isA(LocalDate.class))).andAnswer(new IAnswer<Integer>() {

            @Override
            public Integer answer() throws Throwable {
                LocalDate date = (LocalDate) EasyMock.getCurrentArguments()[0];
                if(answersForDates.containsKey(date)){
                    return answersForDates.get(date);
                }
                return defaultAnswer;
            }
        }).anyTimes();
        expect(this.calendar.getWorkableHours(isA(Date.class)))
        .andAnswer(new IAnswer<Integer>() {

            @Override
            public Integer answer() throws Throwable {
                Date date = (Date) EasyMock.getCurrentArguments()[0];
                LocalDate localDate = new LocalDate(date.getTime());
                if(answersForDates.containsKey(localDate)){
                    return answersForDates.get(localDate);
                }
                return defaultAnswer;
            }
        }).anyTimes();
        replay(this.calendar);
    }

    private void givenWorker() {
        this.worker = createNiceMock(Worker.class);
        expect(this.worker.getCalendar()).andReturn(calendar).anyTimes();
        expect(this.worker.getAssignedHours(isA(LocalDate.class))).andReturn(
                assignedHours).anyTimes();
        replay(this.worker);
    }

    private void givenTask(LocalDate start, LocalDate end) {
        task = createNiceMock(Task.class);
        expect(task.getCalendar()).andReturn(baseCalendar).anyTimes();
        expect(task.getStartDate()).andReturn(
                start.toDateTimeAtStartOfDay().toDate()).anyTimes();
        expect(task.getEndDate()).andReturn(
                end.toDateTimeAtStartOfDay().toDate()).anyTimes();
        replay(task);
    }

    private void givenSpecificResourceAllocation(LocalDate start, LocalDate end) {
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
        givenAssignedHours(4);
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 4);
        specificResourceAllocation.until(start.plusDays(3)).allocate(
                ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(), haveHours(8, 8,
                8));
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

    @SuppressWarnings("serial")
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
