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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.test.planner.entities.DayAssignmentMatchers.from;
import static org.navalplanner.business.test.planner.entities.DayAssignmentMatchers.haveHours;
import static org.navalplanner.business.test.planner.entities.DayAssignmentMatchers.haveResourceAllocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.junit.Test;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.SameWorkHoursEveryDay;
import org.navalplanner.business.planner.entities.GenericDayAssignment;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

public class GenericResourceAllocationTest {

    private GenericResourceAllocation genericResourceAllocation;
    private Set<Criterion> criterions;

    private List<Worker> workers;
    private Worker worker1;
    private Worker worker2;
    private Worker worker3;

    private BaseCalendar baseCalendar;
    private Task task;

    private void givenGenericResourceAllocation() {
        task = givenTaskWithCriterions();
        givenGenericResourceAllocationForTask(task);
    }

    private Task givenTaskWithStartAndEnd(Interval interval) {
        Task task = createNiceMock(Task.class);
        setupCriterions(task);

        expect(task.getStartDate()).andReturn(interval.getStart().toDate())
                .anyTimes();
        expect(task.getEndDate()).andReturn(interval.getEnd().toDate())
                .anyTimes();
        expect(task.getCalendar()).andReturn(baseCalendar).anyTimes();
        replay(task);
        return this.task = task;
    }

    private Task givenTaskWithCriterions() {
        Task task = createNiceMock(Task.class);
        setupCriterions(task);
        expect(task.getCalendar()).andReturn(baseCalendar).anyTimes();
        replay(task);
        return this.task = task;
    }

    private void setupCriterions(Task task) {
        expect(task.getCriterions()).andReturn(givenPredefinedCriterions())
                .anyTimes();
    }

    private void givenGenericResourceAllocationForTask(Task task) {
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

    private void givenWorkersWithoutLoadAndWithoutCalendar() {
        worker1 = createNiceMock(Worker.class);
        worker2 = createNiceMock(Worker.class);
        worker3 = createNiceMock(Worker.class);
        setupCalendarIsNull(worker1);
        buildWorkersList();
        replay(worker1, worker2, worker3);
    }

    private void buildWorkersList() {
        workers = new ArrayList<Worker>();
        workers.add(worker1);
        workers.add(worker2);
        workers.add(worker3);
    }

    private Worker createWorkerWithLoad(int hours) {
        Worker result = createNiceMock(Worker.class);
        setupCalendarIsNull(result);
        expect(result.getAssignedHours(isA(LocalDate.class))).andReturn(hours)
                .anyTimes();
        replay(result);
        return result;
    }

    private void givenWorkersWithLoads(int hours1, int hours2, int hours3) {
        worker1 = createWorkerWithLoad(hours1);
        worker2 = createWorkerWithLoad(hours2);
        worker3 = createWorkerWithLoad(hours3);
        buildWorkersList();
    }

    private void setupCalendarIsNull(Resource resource) {
        expect(resource.getCalendar()).andReturn(null).anyTimes();
    }

    private void givenBaseCalendarWithoutExceptions(int hoursPerDay) {
        BaseCalendar baseCalendar = createNiceMock(BaseCalendar.class);
        expect(baseCalendar.getWorkableHours(isA(Date.class))).andReturn(
                hoursPerDay).anyTimes();
        expect(baseCalendar.getWorkableHours(isA(LocalDate.class))).andReturn(
                hoursPerDay).anyTimes();
        replay(baseCalendar);
        this.baseCalendar = baseCalendar;
    }

    @Test
    public void hasTheCriterionsOfTheTask() {
        givenGenericResourceAllocation();
        assertThat(genericResourceAllocation.getCriterions(),
                equalTo(criterions));
    }

    @Test
    public void getOrderedAssignmentsReturnsEmptyListIfNotExistsWorker() {
        givenWorkersWithoutLoadAndWithoutCalendar();
        givenGenericResourceAllocation();
        List<GenericDayAssignment> assignments = genericResourceAllocation
                .getOrderedAssignmentsFor(worker1);
        assertNotNull(assignments);
        assertTrue(assignments.isEmpty());
    }

    @Test
    public void theGeneratedDayAssignmentsAreRelatedWithTheAllocation() {
        LocalDate start = new LocalDate(2006, 10, 5);
        givenTaskWithStartAndEnd(toInterval(start, Period.days(2)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithoutLoadAndWithoutCalendar();

        genericResourceAllocation.forResources(Arrays.asList(worker1))
                .allocate(ResourcesPerDay.amount(1));

        List<GenericDayAssignment> assignments = genericResourceAllocation
                .getAssignments();
        assertThat(assignments,
                haveResourceAllocation(genericResourceAllocation));
    }

    @Test
    public void allocatingGeneratesDayAssignmentsForEachDay() {
        final int TASK_DURATION_DAYS = 4;
        givenBaseCalendarWithoutExceptions(8);
        LocalDate start = new LocalDate(2006, 10, 5);
        givenTaskWithStartAndEnd(toInterval(start, Period
                .days(TASK_DURATION_DAYS)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithoutLoadAndWithoutCalendar();

        genericResourceAllocation.forResources(Arrays.asList(worker1))
                .allocate(ResourcesPerDay.amount(1));

        List<GenericDayAssignment> orderedAssignmentsFor = genericResourceAllocation
                .getOrderedAssignmentsFor(worker1);
        assertThat(orderedAssignmentsFor, from(start).consecutiveDays(
                TASK_DURATION_DAYS));
    }

    @Test
    public void theResourcesPerDayAreChangedWhenTheAllocationIsDone() {
        givenTaskWithStartAndEnd(toInterval(new LocalDate(2006, 10, 5), Period
                .days(2)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithoutLoadAndWithoutCalendar();
        ResourcesPerDay assignedResourcesPerDay = ResourcesPerDay.amount(5);
        genericResourceAllocation.forResources(workers).allocate(
                assignedResourcesPerDay);
        assertThat(genericResourceAllocation.getResourcesPerDay(),
                equalTo(assignedResourcesPerDay));
    }

    @Test
    public void allocatingSeveralResourcesPerDayHavingJustOneResourceProducesOvertime() {
        LocalDate start = new LocalDate(2006, 10, 5);
        final Integer standardHoursPerDay = SameWorkHoursEveryDay
                .getDefaultWorkingDay().getWorkableHours(start);
        final int TASK_DURATION_DAYS = 4;
        givenBaseCalendarWithoutExceptions(standardHoursPerDay);
        givenTaskWithStartAndEnd(toInterval(start, Period
                .days(TASK_DURATION_DAYS)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithoutLoadAndWithoutCalendar();

        genericResourceAllocation.forResources(Arrays.asList(worker1))
                .allocate(ResourcesPerDay.amount(2));

        List<GenericDayAssignment> orderedAssignmentsFor = genericResourceAllocation
                .getOrderedAssignmentsFor(worker1);
        assertThat(orderedAssignmentsFor.get(0).getHours(),
                equalTo(standardHoursPerDay * 2));
    }

    @Test
    public void theHoursAreGivenBasedOnTheWorkingHoursSpecifiedByTheCalendar() {
        LocalDate start = new LocalDate(2006, 10, 5);
        final int TASK_DURATION_DAYS = 1;
        final int halfWorkingDay = 4;
        givenBaseCalendarWithoutExceptions(halfWorkingDay);
        givenTaskWithStartAndEnd(toInterval(start, Period
                .days(TASK_DURATION_DAYS)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithoutLoadAndWithoutCalendar();

        genericResourceAllocation.forResources(Arrays.asList(worker1))
                .allocate(ResourcesPerDay.amount(1));

        List<GenericDayAssignment> assigmments = genericResourceAllocation
                .getOrderedAssignmentsFor(worker1);
        assertThat(assigmments, haveHours(halfWorkingDay));
    }

    @Test
    public void ifThereisNoTaskCalendarTheWorkingHoursAreSpecifiedbyTheDefaultWorkingDay() {
        LocalDate start = new LocalDate(2006, 10, 5);
        final int TASK_DURATION_DAYS = 1;
        final Integer defaultWorkableHours = SameWorkHoursEveryDay
                .getDefaultWorkingDay().getWorkableHours(start);
        givenBaseCalendarWithoutExceptions(defaultWorkableHours);
        givenTaskWithStartAndEnd(toInterval(start, Period
                .days(TASK_DURATION_DAYS)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithoutLoadAndWithoutCalendar();

        genericResourceAllocation.forResources(Arrays.asList(worker1))
                .allocate(ResourcesPerDay.amount(1));

        List<GenericDayAssignment> assigmments = genericResourceAllocation
                .getOrderedAssignmentsFor(worker1);
        assertThat(assigmments.get(0).getHours(), equalTo(defaultWorkableHours));
    }

    @Test
    public void moreBusyResourcesAreGivenLessLoad() {
        final int TASK_DURATION_DAYS = 4;
        givenBaseCalendarWithoutExceptions(8);
        LocalDate start = new LocalDate(2006, 10, 5);
        givenTaskWithStartAndEnd(toInterval(start, Period
                .days(TASK_DURATION_DAYS)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithLoads(3, 12, 1);

        genericResourceAllocation.forResources(workers).allocate(
                ResourcesPerDay.amount(1));

        List<GenericDayAssignment> assignmentsWorker1 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker1);
        assertThat(assignmentsWorker1, haveHours(3, 3, 3, 3));
        List<GenericDayAssignment> assignmentsWorker2 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker2);
        assertThat(assignmentsWorker2, haveHours(0, 0, 0, 0));
        List<GenericDayAssignment> assignmentsWorker3 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker3);
        assertThat(assignmentsWorker3, haveHours(5, 5, 5, 5));
    }

    private static Interval toInterval(LocalDate start, Period period) {
        return new Interval(start.toDateTimeAtStartOfDay(), start.plus(period)
                .toDateTimeAtStartOfDay());
    }

    @Test
    public void canAllocateHoursOnInterval() {
        final int workableHoursDay = 8;
        givenBaseCalendarWithoutExceptions(workableHoursDay);
        LocalDate start = new LocalDate(2006, 10, 5);
        final int days = 4;
        givenTaskWithStartAndEnd(toInterval(start, Period.days(days)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithLoads(3, 12, 1);

        genericResourceAllocation.forResources(workers).allocate(
                ResourcesPerDay.amount(1));

        assertThat(genericResourceAllocation.getAssignedHours(),
                equalTo(workableHoursDay * days));

        final int hoursOnSubinterval = 3;
        int daysSubinterval = 2;
        genericResourceAllocation.forResources(workers).onInterval(start,
                start.plusDays(daysSubinterval)).allocateHours(
                hoursOnSubinterval);
        assertThat(genericResourceAllocation.getAssignedHours(),
                equalTo(hoursOnSubinterval + (days - daysSubinterval)
                        * workableHoursDay));
    }

    @Test
    public void theRelatedResourcesCanBeRetrieved() {
        givenTaskWithStartAndEnd(toInterval(new LocalDate(2006, 10, 5), Period
                .days(4)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithoutLoadAndWithoutCalendar();

        List<Resource> resourcesGiven = Arrays.<Resource> asList(worker1,
                worker2);
        genericResourceAllocation.forResources(resourcesGiven)
                .allocate(ResourcesPerDay.amount(1));
        assertThat(asSet(genericResourceAllocation.getAssociatedResources()),
                equalTo(asSet(genericResourceAllocation
                        .getAssociatedResources())));
    }

    private Set<Resource> asSet(Collection<Resource> associatedResources) {
        return new HashSet<Resource>(associatedResources);
    }

}
