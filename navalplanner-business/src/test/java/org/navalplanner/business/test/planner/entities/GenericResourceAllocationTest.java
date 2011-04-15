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

package org.navalplanner.business.test.planner.entities;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
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
import static org.navalplanner.business.workingday.EffortDuration.hours;
import static org.navalplanner.business.workingday.EffortDuration.zero;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.junit.Test;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.Capacity;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.calendars.entities.SameWorkHoursEveryDay;
import org.navalplanner.business.planner.entities.GenericDayAssignment;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.VirtualWorker;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.IntraDayDate;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;
import org.navalplanner.business.workingday.ResourcesPerDay;

public class GenericResourceAllocationTest {

    private GenericResourceAllocation genericResourceAllocation;
    private Set<Criterion> criterions;

    private List<Worker> workers;
    private List<ResourceCalendar> workerCalendars = null;
    private Worker worker1;
    private Worker worker2;
    private Worker worker3;

    private BaseCalendar baseCalendar;
    private Task task;

    private static Scenario mockScenario() {
        Scenario result = createNiceMock(Scenario.class);
        replay(result);
        return result;
    }

    private void givenGenericResourceAllocation() {
        task = givenTaskWithCriterions();
        givenGenericResourceAllocationForTask(task);
    }

    private Task givenTaskWithStartAndEnd(Interval interval) {
        Task task = createNiceMock(Task.class);
        setupCriterions(task);
        IntraDayDate start = IntraDayDate.startOfDay(interval.getStart()
                .toLocalDate());
        IntraDayDate end = IntraDayDate.startOfDay(interval.getEnd()
                .toLocalDate());
        expect(task.getStartDate()).andReturn(interval.getStart().toDate())
                .anyTimes();
        expect(task.getIntraDayStartDate()).andReturn(start).anyTimes();
        expect(task.getEndDate()).andReturn(interval.getEnd().toDate())
                .anyTimes();
        expect(task.getIntraDayEndDate()).andReturn(end).anyTimes();
        expect(task.getFirstDayNotConsolidated()).andReturn(start)
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
        setupIsSatisfiedByAll(criterion1);
        Criterion criterion2 = createNiceMock(Criterion.class);
        setupIsSatisfiedByAll(criterion2);
        replay(criterion1, criterion2);
        result.add(criterion1);
        result.add(criterion2);
        this.criterions = result;
        return result;
    }

    private void setupIsSatisfiedByAll(Criterion criterion) {
        expect(
                criterion.isSatisfiedBy(isA(Resource.class),
                        isA(LocalDate.class))).andReturn(true).anyTimes();
    }

    private void givenWorkersWithoutLoadAndWithoutCalendar() {
        worker1 = createNiceMock(Worker.class);
        worker2 = createNiceMock(Worker.class);
        worker3 = createNiceMock(Worker.class);
        mockZeroLoad(worker1, worker2, worker3);
        buildWorkersList();
        replay(worker1, worker2, worker3);
    }

    public static void mockZeroLoad(Resource... resources) {
        for (Resource each : resources) {
            expect(
                    each.getAssignedDurationDiscounting(isA(Map.class),
                            isA(LocalDate.class))).andReturn(zero()).anyTimes();
        }
    }

    private void buildWorkersList() {
        workers = new ArrayList<Worker>();
        workers.add(worker1);
        workers.add(worker2);
        workers.add(worker3);
    }

    private static class LoadSpec {

        public static LoadSpec withHours(int hours) {
            return new LoadSpec(hours(hours));
        }

        private final EffortDuration defaultLoad;

        private LoadSpec(EffortDuration defaultLoad) {
            Validate.notNull(defaultLoad);
            this.defaultLoad = defaultLoad;
        }

        private Map<LocalDate, EffortDuration> exceptions = new HashMap<LocalDate, EffortDuration>();

        LoadSpec withException(LocalDate date, EffortDuration loadAtThatDate) {
            Validate.notNull(date);
            Validate.notNull(loadAtThatDate);
            exceptions.put(date, loadAtThatDate);
            return this;
        }

        EffortDuration getLoad(LocalDate date) {
            if (exceptions.containsKey(date)) {
                return exceptions.get(date);
            }
            return defaultLoad;
        }

    }

    private Worker createWorkerWithLoad(ResourceCalendar resourceCalendar,
            int hours) {
        return createWorkerWithLoad(resourceCalendar,
                new LoadSpec(hours(hours)));
    }

    private Worker createWorkerWithLoad(ResourceCalendar resourceCalendar,
            final LoadSpec loadSpec) {
        Worker result = createNiceMock(Worker.class);
        expect(result.getCalendar()).andReturn(resourceCalendar).anyTimes();
        expect(
                result.getAssignedDurationDiscounting(isA(Map.class),
                        isA(LocalDate.class))).andAnswer(
                new IAnswer<EffortDuration>() {

                    @Override
                    public EffortDuration answer() throws Throwable {
                        Object[] currentArguments = EasyMock
                                .getCurrentArguments();
                        LocalDate date = (LocalDate) currentArguments[1];
                        return loadSpec.getLoad(date);
                    }
                }).anyTimes();
        expect(result.getSatisfactionsFor(isA(Criterion.class))).andReturn(
                satisfactionsForPredefinedCriterions(result)).anyTimes();
        replay(result);
        return result;
    }

    private List<CriterionSatisfaction> satisfactionsForPredefinedCriterions(
            Resource resource) {
        List<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
        for (Criterion each : criterions) {
            result.add(CriterionSatisfaction.create(each, resource,
                    fromVeryEarlyTime()));
        }
        return result;
    }

    private org.navalplanner.business.resources.entities.Interval fromVeryEarlyTime() {
        return org.navalplanner.business.resources.entities.Interval
                .from(new LocalDate(0, 1, 1));
    }

    private void givenCalendarsForResources(int capacity1, int capacity2,
            int capacity3) {
        givenCalendarsForResources(fromHours(capacity1), fromHours(capacity2),
                fromHours(capacity3));
    }

    private Capacity fromHours(int hours) {
        return Capacity.create(hours(hours)).overAssignableWithoutLimit(true);
    }

    private void givenCalendarsForResources(Capacity capacity1,
            Capacity capacity2, Capacity capacity3) {
        workerCalendars = new ArrayList<ResourceCalendar>();
        workerCalendars.add(createCalendar(ResourceCalendar.class, capacity1));
        workerCalendars.add(createCalendar(ResourceCalendar.class, capacity2));
        workerCalendars.add(createCalendar(ResourceCalendar.class, capacity3));
    }

    private void givenWorkersWithLoads(int hours1, int hours2, int hours3) {
        givenWorkersWithLoads(LoadSpec.withHours(hours1),
                LoadSpec.withHours(hours2), LoadSpec.withHours(hours3));
    }

    private void givenWorkersWithLoads(LoadSpec load1, LoadSpec load2,
            LoadSpec load3) {
        ResourceCalendar[] calendars;
        if (workerCalendars == null) {
            calendars = new ResourceCalendar[] { null, null, null };
        } else {
            calendars = new ResourceCalendar[] { workerCalendars.get(0),
                    workerCalendars.get(1), workerCalendars.get(2) };
        }
        worker1 = createWorkerWithLoad(calendars[0], load1);
        worker2 = createWorkerWithLoad(calendars[1], load2);
        worker3 = createWorkerWithLoad(calendars[2], load3);
        buildWorkersList();
    }

    private void givenBaseCalendarWithoutExceptions(int hoursPerDay) {
        BaseCalendar baseCalendar = createCalendar(BaseCalendar.class, Capacity
                .create(hours(hoursPerDay)).overAssignableWithoutLimit(true));
        this.baseCalendar = baseCalendar;
    }

    private <T extends BaseCalendar> T createCalendar(Class<T> klass,
            final Capacity capacity) {
        return createCalendar(klass, capacity, 1);
    }

    private <T extends BaseCalendar> T createCalendar(Class<T> klass,
            final Capacity capacity, int units) {
        final Capacity capacityMultipliedByUnits = capacity.multiplyBy(units);
        BaseCalendar baseCalendar = createNiceMock(klass);
        expect(baseCalendar.getCapacityOn(isA(PartialDay.class))).andAnswer(
                new IAnswer<EffortDuration>() {

                    @Override
                    public EffortDuration answer() throws Throwable {
                        PartialDay day = (PartialDay) getCurrentArguments()[0];
                        return day.limitDuration(capacityMultipliedByUnits
                                .getStandardEffort());
                    }
                }).anyTimes();
        expect(baseCalendar.isActive(isA(LocalDate.class))).andReturn(true)
                .anyTimes();
        expect(baseCalendar.canWorkOn(isA(LocalDate.class))).andReturn(true)
                .anyTimes();
        expect(baseCalendar.getAvailability()).andReturn(
                AvailabilityTimeLine.allValid()).anyTimes();
        IAnswer<EffortDuration> durationAnswer = new IAnswer<EffortDuration>() {
            @Override
            public EffortDuration answer() throws Throwable {
                PartialDay day = (PartialDay) getCurrentArguments()[0];
                ResourcesPerDay resourcesPerDay = (ResourcesPerDay) getCurrentArguments()[1];
                return capacityMultipliedByUnits.limitDuration(resourcesPerDay
                        .asDurationGivenWorkingDayOf(day.limitDuration(capacity
                                .getStandardEffort())));
            }
        };
        expect(
                baseCalendar.asDurationOn(isA(PartialDay.class),
                        isA(ResourcesPerDay.class))).andAnswer(durationAnswer)
                .anyTimes();
        expect(baseCalendar.getCapacityWithOvertime(isA(LocalDate.class)))
                .andReturn(capacityMultipliedByUnits).anyTimes();

        if (baseCalendar instanceof ResourceCalendar) {
            ResourceCalendar resourceCalendar = (ResourceCalendar) baseCalendar;
            expect(resourceCalendar.getCapacity()).andReturn(units).anyTimes();
        }
        replay(baseCalendar);
        return klass.cast(baseCalendar);
    }

    @Test
    public void theCriterionsAreCopied() {
        givenGenericResourceAllocation();
        GenericResourceAllocation copied = (GenericResourceAllocation) genericResourceAllocation
                .copy(mockScenario());
        assertThat(copied.getCriterions(), equalTo(criterions));
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
    public void canAllocateSomeResourcesPerDayUntilSomeEndDate() {
        final int TASK_DURATION_DAYS = 4;
        givenBaseCalendarWithoutExceptions(8);
        LocalDate start = new LocalDate(2006, 10, 5);
        givenTaskWithStartAndEnd(toInterval(start, Period
                .days(TASK_DURATION_DAYS)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithoutLoadAndWithoutCalendar();
        ResourcesPerDay resourcesPerDay = ResourcesPerDay.amount(1);

        genericResourceAllocation.forResources(Arrays.asList(worker1))
                                 .resourcesPerDayUntil(start.plusDays(2))
                                 .allocate(resourcesPerDay);

        List<GenericDayAssignment> orderedAssignmentsFor = genericResourceAllocation
                .getOrderedAssignmentsFor(worker1);
        int hoursPerDay = resourcesPerDay.asDurationGivenWorkingDayOf(
                EffortDuration.hours(8)).getHours();
        assertThat(orderedAssignmentsFor, haveHours(hoursPerDay, hoursPerDay));
    }

    @Test
    public void allocatingUntilSomeEndDateDeletesAssignmentsAfterThatDate() {
        final int TASK_DURATION_DAYS = 4;
        givenBaseCalendarWithoutExceptions(8);
        LocalDate start = new LocalDate(2006, 10, 5);
        givenTaskWithStartAndEnd(toInterval(start, Period
                .days(TASK_DURATION_DAYS)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithoutLoadAndWithoutCalendar();
        ResourcesPerDay resourcesPerDay = ResourcesPerDay.amount(1);

        genericResourceAllocation.forResources(Arrays.asList(worker1))
                .allocate(resourcesPerDay);
        genericResourceAllocation.forResources(Arrays.asList(worker1))
                .resourcesPerDayUntil(start.plusDays(2))
                .allocate(resourcesPerDay);

        List<GenericDayAssignment> orderedAssignmentsFor = genericResourceAllocation
                .getOrderedAssignmentsFor(worker1);
        int hoursPerDay = resourcesPerDay.asDurationGivenWorkingDayOf(
                EffortDuration.hours(8)).getHours();
        assertThat(orderedAssignmentsFor, haveHours(hoursPerDay, hoursPerDay));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAllocatingUntilSomeEndDateTheEndDateMustNotBeBeforeTaskStart() {
        LocalDate start = new LocalDate(2006, 10, 5);
        givenTaskWithStartAndEnd(toInterval(start, Period.days(4)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithoutLoadAndWithoutCalendar();
        ResourcesPerDay resourcesPerDay = ResourcesPerDay.amount(1);

        genericResourceAllocation.forResources(Arrays.asList(worker1))
                .resourcesPerDayUntil(start.minusDays(1))
                .allocate(resourcesPerDay);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAllocatingUntilSomeEndDateTheEndDateMustNotBeNull() {
        LocalDate start = new LocalDate(2006, 10, 5);
        givenTaskWithStartAndEnd(toInterval(start, Period.days(4)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithoutLoadAndWithoutCalendar();
        ResourcesPerDay resourcesPerDay = ResourcesPerDay.amount(1);

        genericResourceAllocation.forResources(Arrays.asList(worker1))
                .resourcesPerDayUntil(null).allocate(resourcesPerDay);
    }

    @Test
    public void allocatingUntilEndDateEqualToStartImpliesNoAssignmentsAndZeroResourcesPerDay() {
        LocalDate start = new LocalDate(2006, 10, 5);
        givenTaskWithStartAndEnd(toInterval(start, Period.days(4)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithoutLoadAndWithoutCalendar();
        ResourcesPerDay resourcesPerDay = ResourcesPerDay.amount(1);

        genericResourceAllocation.forResources(Arrays.asList(worker1))
                .resourcesPerDayUntil(start).allocate(resourcesPerDay);
        assertThat(genericResourceAllocation.getResourcesPerDay(),
                equalTo(ResourcesPerDay.amount(0)));
        assertTrue(genericResourceAllocation.getOrderedAssignmentsFor(worker1)
                .isEmpty());
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
                .getDefaultWorkingDay()
                .getCapacityOn(PartialDay.wholeDay(start)).getHours();
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
                .getDefaultWorkingDay()
                .getCapacityOn(PartialDay.wholeDay(start)).getHours();
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
    public void itGivesAllTheLoadItCanToTheLessLoadedResource() {
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
        assertThat(assignmentsWorker1, haveHours(1, 1, 1, 1));
        List<GenericDayAssignment> assignmentsWorker2 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker2);
        assertThat(assignmentsWorker2, haveHours());
        List<GenericDayAssignment> assignmentsWorker3 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker3);
        assertThat(assignmentsWorker3, haveHours(7, 7, 7, 7));
    }

    @Test
    public void itTakesIntoAccountTheLoadForEachDay() {
        final int TASK_DURATION_DAYS = 4;
        givenBaseCalendarWithoutExceptions(8);
        LocalDate start = new LocalDate(2006, 10, 5);
        givenTaskWithStartAndEnd(toInterval(start,
                Period.days(TASK_DURATION_DAYS)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithLoads(
                LoadSpec.withHours(3)
                        .withException(start.plusDays(1), hours(1))
                        .withException(start.plusDays(3), hours(8)),
                LoadSpec.withHours(12).withException(start.plusDays(3), zero()),
                LoadSpec.withHours(1)
                        .withException(start.plusDays(1), hours(3))
                        .withException(start.plusDays(3), hours(8)));

        genericResourceAllocation.forResources(workers).allocate(
                ResourcesPerDay.amount(1));

        List<GenericDayAssignment> assignmentsWorker1 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker1);
        assertThat(assignmentsWorker1, haveHours(1, 7, 1));
        List<GenericDayAssignment> assignmentsWorker2 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker2);
        assertThat(assignmentsWorker2, haveHours(8));
        List<GenericDayAssignment> assignmentsWorker3 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker3);
        assertThat(assignmentsWorker3, haveHours(7, 1, 7));
    }

    @Test
    public void previouslyPickedResourcesHaveMorePriority() {
        final int TASK_DURATION_DAYS = 4;
        givenBaseCalendarWithoutExceptions(8);
        LocalDate start = new LocalDate(2006, 10, 5);
        givenTaskWithStartAndEnd(toInterval(start,
                Period.days(TASK_DURATION_DAYS)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithLoads(
                LoadSpec.withHours(0)
                        .withException(start.plusDays(3), hours(4)),
                LoadSpec.withHours(12),
                LoadSpec.withHours(1)
                        .withException(start.plusDays(3), hours(0)));

        genericResourceAllocation.forResources(workers).allocate(
                ResourcesPerDay.amount(1));

        List<GenericDayAssignment> assignmentsWorker3 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker3);
        assertThat(assignmentsWorker3, haveHours(4));
        List<GenericDayAssignment> assignmentsWorker1 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker1);
        assertThat(assignmentsWorker1, haveHours(8, 8, 8, 4));
        List<GenericDayAssignment> assignmentsWorker2 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker2);
        assertThat(assignmentsWorker2, haveHours());
    }

    @Test
    public void doesntSurpassTheExtraHours() {
        final int TASK_DURATION_DAYS = 4;
        givenBaseCalendarWithoutExceptions(8);
        LocalDate start = new LocalDate(2006, 10, 5);
        givenTaskWithStartAndEnd(toInterval(start,
                Period.days(TASK_DURATION_DAYS)));
        givenGenericResourceAllocationForTask(task);

        Capacity workingDay = Capacity.create(hours(8));
        Capacity with2ExtraHours = workingDay
                .withAllowedExtraEffort(hours(2));
        givenCalendarsForResources(with2ExtraHours, with2ExtraHours,
                workingDay.overAssignableWithoutLimit(true));
        givenWorkersWithLoads(0, 0, 0);

        genericResourceAllocation.forResources(workers).allocate(
                ResourcesPerDay.amount(4));

        List<GenericDayAssignment> assignmentsWorker1 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker1);
        assertThat(assignmentsWorker1, haveHours(10, 10, 10, 10));
        List<GenericDayAssignment> assignmentsWorker2 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker2);
        assertThat(assignmentsWorker2, haveHours(10, 10, 10, 10));
        List<GenericDayAssignment> assignmentsWorker3 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker3);
        assertThat(assignmentsWorker3, haveHours(12, 12, 12, 12));

    }

    @Test
    public void itGivesAllTheLoadItCanToTheLessLoadedResourceAndThenToTheNextOne() {
        final int TASK_DURATION_DAYS = 4;
        givenBaseCalendarWithoutExceptions(8);
        LocalDate start = new LocalDate(2006, 10, 5);
        givenTaskWithStartAndEnd(toInterval(start,
                Period.days(TASK_DURATION_DAYS)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithLoads(0, 0, 0);

        genericResourceAllocation.forResources(asList(worker1, worker2))
                .allocate(ResourcesPerDay.amount(2));

        List<GenericDayAssignment> assignmentsWorker1 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker1);
        assertThat(assignmentsWorker1, haveHours(8, 8, 8, 8));
        List<GenericDayAssignment> assignmentsWorker2 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker2);
        assertThat(assignmentsWorker2, haveHours(8, 8, 8, 8));
    }

    @Test
    public void virtualWorkersAreGivenMoreLoad() {
        final int TASK_DURATION_DAYS = 4;
        givenBaseCalendarWithoutExceptions(8);
        LocalDate start = new LocalDate(2006, 10, 5);
        givenTaskWithStartAndEnd(toInterval(start, Period
                .days(TASK_DURATION_DAYS)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithLoads(8, 8, 8);
        givenVirtualWorkerWithCapacityAndLoad(Capacity.create(hours(8))
                .overAssignableWithoutLimit(true), 5, hours(40));

        genericResourceAllocation.forResources(workers).allocate(
                ResourcesPerDay.amount(1));
        List<GenericDayAssignment> assignmentsWorker3 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker3);
        assertThat(assignmentsWorker3, haveHours(1, 1, 1, 1));

        List<GenericDayAssignment> assignmentsWorker1 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker1);
        assertThat(assignmentsWorker1, haveHours(1, 1, 1, 1));

        List<GenericDayAssignment> virtualWorkerAssignments = genericResourceAllocation
                .getOrderedAssignmentsFor(workers.get(workers.size() - 1));
        assertThat(virtualWorkerAssignments, haveHours(5, 5, 5, 5));

        List<GenericDayAssignment> assignmentsWorker2 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker2);
        assertThat(assignmentsWorker2, haveHours(1, 1, 1, 1));
    }

    @Test
    public void itWorksWithCalendarsReturningZeroHours() {
        final int TASK_DURATION_DAYS = 4;
        givenBaseCalendarWithoutExceptions(8);
        givenCalendarsForResources(4, 4, 0);
        LocalDate start = new LocalDate(2006, 10, 5);
        givenTaskWithStartAndEnd(toInterval(start, Period
                .days(TASK_DURATION_DAYS)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithLoads(4, 4, 4);

        genericResourceAllocation.forResources(workers).allocate(
                ResourcesPerDay.amount(1));

        List<GenericDayAssignment> assignmentsWorker1 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker1);
        assertThat(assignmentsWorker1, haveHours(4, 4, 4, 4));
        List<GenericDayAssignment> assignmentsWorker2 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker2);
        assertThat(assignmentsWorker2, haveHours(4, 4, 4, 4));
        List<GenericDayAssignment> assignmentsWorker3 = genericResourceAllocation
                .getOrderedAssignmentsFor(worker3);
        assertThat(assignmentsWorker3, haveHours());
    }

    @Test
    public void theEndHourOfTheAllocationIsTheBiggestAllocationDoneIfThereIsSpareSpaceLeft() {
        final int TASK_DURATION_DAYS = 4;
        givenBaseCalendarWithoutExceptions(8);
        givenCalendarsForResources(8, 8, 8);
        LocalDate start = new LocalDate(2006, 10, 5);
        givenTaskWithStartAndEnd(toInterval(start,
                Period.days(TASK_DURATION_DAYS)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithLoads(8, 6, 2);
        IntraDayDate end = ResourceAllocation.allocating(
                singletonList(ResourcesPerDayModification.create(
                        genericResourceAllocation,
                        ResourcesPerDay.amount(new BigDecimal(1)), workers)))
                .untilAllocating(12);
        assertThat(end.getDate(), equalTo(start.plusDays(1)));
        EffortDuration biggestLastAssignment = hours(4);
        assertThat(end.getEffortDuration(), equalTo(biggestLastAssignment));
    }

    @Test
    public void theEndOfTheAllocationIsTheNextDayIfThereIsNoSpareSpaceLeft() {
        final int TASK_DURATION_DAYS = 4;
        givenBaseCalendarWithoutExceptions(8);
        givenCalendarsForResources(8, 8, 8);
        LocalDate start = new LocalDate(2006, 10, 5);
        givenTaskWithStartAndEnd(toInterval(start,
                Period.days(TASK_DURATION_DAYS)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithLoads(8, 2, 6);
        IntraDayDate end = ResourceAllocation.allocating(
                singletonList(ResourcesPerDayModification.create(
                        genericResourceAllocation, ResourcesPerDay.amount(1),
                        workers))).untilAllocating(16);
        assertThat(end.getDate(), equalTo(start.plusDays(2)));
    }

    private void givenVirtualWorkerWithCapacityAndLoad(
            Capacity capacityPerDayAndUnit,
            int capacityUnits,
            EffortDuration load) {
        VirtualWorker worker = createNiceMock(VirtualWorker.class);
        expect(
                worker.getAssignedDurationDiscounting(isA(Map.class),
                        isA(LocalDate.class))).andReturn(load)
                .anyTimes();
        expect(worker.getCalendar()).andReturn(
                createCalendar(ResourceCalendar.class, capacityPerDayAndUnit,
                        capacityUnits)).anyTimes();
        replay(worker);
        workers.add(worker);
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
        genericResourceAllocation.forResources(workers)
                .onIntervalWithinTask(start, start.plusDays(daysSubinterval))
                .allocateHours(hoursOnSubinterval);
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

    @Test
    public void canAllocateHoursOnIntervalUsingPreviousResources() {
        final int workableHoursDay = 8;
        givenBaseCalendarWithoutExceptions(workableHoursDay);
        LocalDate start = new LocalDate(2006, 10, 5);
        final int days = 4;
        givenTaskWithStartAndEnd(toInterval(start, Period.days(days)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithLoads(3, 12, 1);

        genericResourceAllocation.forResources(workers).allocate(
                ResourcesPerDay.amount(1));

        final int hoursOnSubinterval = 3;
        int daysSubinterval = 2;
        genericResourceAllocation.withPreviousAssociatedResources().onIntervalWithinTask(
                start,
                start.plusDays(daysSubinterval)).allocateHours(
                hoursOnSubinterval);
        assertThat(genericResourceAllocation.getAssignedHours(),
                equalTo(hoursOnSubinterval + (days - daysSubinterval)
                        * workableHoursDay));
    }

    @Test
    public void allocatingWithPreviousAssociatedResourcesCanBeUsedSafelyWhenNoAllocationHasBeenDone() {
        final int workableHoursDay = 8;
        givenBaseCalendarWithoutExceptions(workableHoursDay);
        LocalDate start = new LocalDate(2006, 10, 5);
        final int days = 4;
        givenTaskWithStartAndEnd(toInterval(start, Period.days(days)));
        givenGenericResourceAllocationForTask(task);

        genericResourceAllocation.withPreviousAssociatedResources().allocate(
                ResourcesPerDay.amount(1));

        assertThat(genericResourceAllocation.getAssignedHours(), equalTo(0));
    }

    @Test
    public void afterAllocatingMoreHoursOnIntervalTheResourcesPerDayAreIncreased() {
        final int workableHoursDay = 8;
        givenBaseCalendarWithoutExceptions(workableHoursDay);
        LocalDate start = new LocalDate(2006, 10, 5);
        final int days = 4;
        givenTaskWithStartAndEnd(toInterval(start, Period.days(days)));
        givenGenericResourceAllocationForTask(task);
        givenWorkersWithLoads(8, 8, 8);

        genericResourceAllocation.forResources(workers).allocate(
                ResourcesPerDay.amount(3));
        ResourcesPerDay original = genericResourceAllocation
                .getResourcesPerDay();
        genericResourceAllocation.forResources(workers).onIntervalWithinTask(start,
                start.plusDays(2)).allocateHours(60);
        ResourcesPerDay current = genericResourceAllocation
                .getResourcesPerDay();
        assertTrue(current.getAmount()
                .compareTo(original.getAmount()) > 0);
    }

}
