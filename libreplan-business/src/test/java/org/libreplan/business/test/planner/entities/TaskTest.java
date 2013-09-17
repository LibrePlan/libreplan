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

import static java.util.Arrays.asList;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.resetToNice;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.planner.entities.allocationalgorithms.AllocationModification.ofType;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.business.test.planner.entities.DayAssignmentMatchers.haveHours;
import static org.libreplan.business.workingday.EffortDuration.hours;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.Validate;
import org.easymock.IAnswer;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.calendars.entities.ResourceCalendar;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.orders.entities.SchedulingDataForVersion;
import org.libreplan.business.orders.entities.SumChargedEffort;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.planner.entities.AggregateOfDayAssignments;
import org.libreplan.business.planner.entities.AggregateOfResourceAllocations;
import org.libreplan.business.planner.entities.CalculatedValue;
import org.libreplan.business.planner.entities.DayAssignment.FilterType;
import org.libreplan.business.planner.entities.Dependency;
import org.libreplan.business.planner.entities.Dependency.Type;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.ResourceAllocation.Direction;
import org.libreplan.business.planner.entities.SpecificResourceAllocation;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.Task.ManualRecurrencesModification;
import org.libreplan.business.planner.entities.Task.ModifiedAllocation;
import org.libreplan.business.planner.entities.Task.RecurrencesModification;
import org.libreplan.business.planner.entities.TaskDeadlineViolationStatusEnum;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskElement.IDatesHandler;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.planner.entities.TaskStatusEnum;
import org.libreplan.business.planner.entities.allocationalgorithms.AllocationModification;
import org.libreplan.business.planner.entities.allocationalgorithms.EffortModification;
import org.libreplan.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.libreplan.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.libreplan.business.recurring.Recurrence;
import org.libreplan.business.recurring.RecurrenceInformation;
import org.libreplan.business.recurring.RecurrencePeriodicity;
import org.libreplan.business.resources.daos.IResourcesSearcher;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.scenarios.bootstrap.IScenariosBootstrap;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;
import org.libreplan.business.workingday.ResourcesPerDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class TaskTest {

    private static final OrderVersion mockedOrderVersion = mockOrderVersion();

    public static final OrderVersion mockOrderVersion() {
        OrderVersion result = createNiceMock(OrderVersion.class);
        replay(result);
        return result;
    }

    private BaseCalendar taskCalendar;

    private Task task;

    private HoursGroup hoursGroup;

    private ResourceCalendar workerCalendar;

    private Worker worker;

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    private Scenario mainScenario;

    @Before
    public void loadRequiredaData() {
        scenariosBootstrap.loadRequiredData();

        mainScenario = scenariosBootstrap.getMain();
        // Load data
        defaultAdvanceTypesBootstrapListener.loadRequiredData();

        // Create basic data
        hoursGroup = new HoursGroup();
        hoursGroup.setWorkingHours(3);
        Order order = new Order();
        order.useSchedulingDataFor(mockedOrderVersion);
        order.setInitDate(new Date());
        OrderLine orderLine = OrderLine.create();
        order.add(orderLine);
        order.setCalendar(stubCalendar());
        SchedulingDataForVersion version = TaskElementTest
                .mockSchedulingDataForVersion(orderLine);
        TaskSource taskSource = TaskSource.create(version, Arrays
                .asList(hoursGroup));
        task = Task.createTask(taskSource);
    }

    private BaseCalendar stubCalendar() {
        taskCalendar = createNiceMock(BaseCalendar.class);
        expect(taskCalendar.getCapacityOn(isA(PartialDay.class)))
                .andReturn(hours(8)).anyTimes();
        expect(taskCalendar.getAvailability()).andAnswer(
                new IAnswer<AvailabilityTimeLine>() {

                    @Override
                    public AvailabilityTimeLine answer() throws Throwable {
                        return AvailabilityTimeLine.allValid();
                    }
                }).anyTimes();
        replay(taskCalendar);
        return taskCalendar;
    }

    @Test
    public void taskIsASubclassOfTaskElement() {
        assertTrue(task instanceof TaskElement);
    }

    @Test
    public void taskHasHoursSpecifiedAtOrderComingFromItsHoursGroup() {
        assertThat(task.getHoursSpecifiedAtOrder(), equalTo(hoursGroup.getWorkingHours()));
    }

    @Test
    public void taskMustHaveOneHoursGroup() {
        HoursGroup hoursGroup = task.getHoursGroup();
        assertNotNull(hoursGroup);
    }

    public static Task createValidTask() {
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setWorkingHours(3);
        OrderLine orderLine = OrderLine.create();
        Order order = new Order();
        order.useSchedulingDataFor(mockedOrderVersion);
        order.setInitDate(new Date());
        order.add(orderLine);
        SchedulingDataForVersion version = TaskElementTest
                .mockSchedulingDataForVersion(orderLine);
        TaskSource taskSource = TaskSource.create(version, Arrays
                .asList(hoursGroup));
        return Task.createTask(taskSource);
    }

    public static Task createValidTaskWithFullProgress(){
        Task task = createValidTask();
        task.setAdvancePercentage(BigDecimal.ONE);
        return task;
    }

    @Test
    public void getResourceAllocationsDoesntRetrieveUnsatisfiedAllocations() {
        assertThat(task.getSatisfiedResourceAllocations().size(), equalTo(0));

        SpecificResourceAllocation unsatisfied = SpecificResourceAllocation
                .create(task);
        assertTrue("in order to be meaningful this test needs an unsatisfied "
                + "allocation", unsatisfied.isUnsatisfied());
        task.addResourceAllocation(unsatisfied);

        assertThat(task.getSatisfiedResourceAllocations().size(), equalTo(0));
        assertThat(task.getAllResourceAllocations().size(), equalTo(1));
    }

    @Test
    public void addingNoEmptyResourceAllocationAddsIt() {
        assertThat(task.getSatisfiedResourceAllocations().size(), equalTo(0));

        SpecificResourceAllocation resourceAllocation = stubResourceAllocationWithAssignedHours(
                task, 500);
        task.addResourceAllocation(resourceAllocation);
        assertThat(task.getSatisfiedResourceAllocations().size(), equalTo(1));
    }

    @Test
    public void taskRemoveResourceAllocation() {
        assertThat(task.getSatisfiedResourceAllocations().size(), equalTo(0));

        SpecificResourceAllocation resourceAllocation = stubResourceAllocationWithAssignedHours(
                task, 500);
        task.addResourceAllocation(resourceAllocation);

        assertThat(task.getSatisfiedResourceAllocations().size(), equalTo(1));

        task.removeResourceAllocation(resourceAllocation);
        assertThat(task.getSatisfiedResourceAllocations().size(), equalTo(0));
    }

    @Test
    public void aTaskWithoutAllocationsHasZeroAssignedHours() {
        assertThat(task.getAssignedHours(), equalTo(0));
    }

    @Test
    public void aTaskWithAllocationsReturnsTheSumOfItsAllocations() {
        task.addResourceAllocation(stubResourceAllocationWithAssignedHours(
                task, 5));
        task.addResourceAllocation(stubResourceAllocationWithAssignedHours(
                task, 3));
        assertThat(task.getAssignedHours(), equalTo(8));
    }

    @Test
    public void theWorkableDaysAreCalculatedBasedOnlyOnDatesNotHours() {
        task.setIntraDayStartDate(IntraDayDate.create(
                new LocalDate(2010, 1, 13), EffortDuration.hours(3)));
        task.setIntraDayEndDate(IntraDayDate.startOfDay(new LocalDate(2010, 1,
                14)));
        assertThat(task.getWorkableDays(), equalTo(1));
    }

    @Test
    public void atLeastOneWorkableDayEvenIfStartAndEndDatesAreAtTheSameDay() {
        LocalDate day = new LocalDate(2010, 1, 13);
        task.setIntraDayStartDate(IntraDayDate.create(day,
                EffortDuration.hours(3)));
        task.setIntraDayEndDate(IntraDayDate.create(day,
                EffortDuration.hours(4)));
        assertThat(task.getWorkableDays(), equalTo(1));
    }

    @Test
    public void ifTheEndIsInTheMiddleOfADayTheWholeDayIsCounted() {
        LocalDate start = new LocalDate(2010, 1, 13);
        task.setIntraDayStartDate(IntraDayDate.create(start,
                EffortDuration.hours(3)));
        task.setIntraDayEndDate(IntraDayDate.create(start.plusDays(1),
                EffortDuration.minutes(1)));
        assertThat(task.getWorkableDays(), equalTo(2));
    }

    @Test
    public void ifSomeDayIsNotWorkableIsNotCounted() {
        final LocalDate start = new LocalDate(2010, 1, 13);

        resetToNice(taskCalendar);
        expect(taskCalendar.getCapacityOn(isA(PartialDay.class))).andAnswer(
                new IAnswer<EffortDuration>() {
                    @Override
                    public EffortDuration answer() throws Throwable {
                        Object[] args = getCurrentArguments();
                        PartialDay day = (PartialDay) args[0];
                        return day.getDate().equals(start.plusDays(1)) ? hours(0)
                                : hours(8);
                    }
                }).anyTimes();
        replay(taskCalendar);

        task.setIntraDayStartDate(IntraDayDate.create(start,
                EffortDuration.hours(3)));
        task.setIntraDayEndDate(IntraDayDate.create(start.plusDays(1),
                EffortDuration.minutes(1)));
        assertThat(task.getWorkableDays(), equalTo(1));
    }

    /**
     * @param task
     * @param hours
     * @return
     */
    private SpecificResourceAllocation stubResourceAllocationWithAssignedHours(
            Task task,
            int hours) {
        SpecificResourceAllocation resourceAllocation = createNiceMock(SpecificResourceAllocation.class);
        expect(resourceAllocation.getAssignedHours()).andReturn(hours)
                .anyTimes();
        expect(resourceAllocation.getTask()).andReturn(task).anyTimes();
        expect(resourceAllocation.hasAssignments()).andReturn(true).anyTimes();
        expect(resourceAllocation.isSatisfied()).andReturn(true).anyTimes();
        resourceAllocation.detach();
        expectLastCall().anyTimes();
        replay(resourceAllocation);
        return resourceAllocation;
    }

    @Test
    public void testIsLimiting() {
        LimitingResourceQueueElement element = LimitingResourceQueueElement.create();
        Task task = createValidTask();
        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation.create(task);
        resourceAllocation.setLimitingResourceQueueElement(element);
        task.addResourceAllocation(resourceAllocation);
        assertTrue(task.getLimitingResourceAllocations().size() == 1);
    }

    @Test
    public void testIsNonLimiting() {
        Task task = createValidTask();
        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation.create(task);
        task.addResourceAllocation(resourceAllocation);
        assertTrue(task.getNonLimitingAndNotRecurrentResourceAllocations().size() == 1);
    }

    @Test
    public void theoreticalHoursIsZeroIfNoResourcesAreAllocated() {
        assertThat(task.getTheoreticalCompletedTimeUntilDate(new Date()), equalTo(EffortDuration.zero()));
    }

    @Test
    public void theoreticalHoursIsTotalIfDateIsLaterThanEndDate() {
        prepareTaskForTheoreticalAdvanceTesting();
        EffortDuration totalAllocatedTime = AggregateOfDayAssignments.create(
                task.getDayAssignments(FilterType.KEEP_ALL)).getTotalTime();
        assertThat(task.getTheoreticalCompletedTimeUntilDate(task.getEndDate()), equalTo(totalAllocatedTime));

    }

    @Test
    public void theoreticalHoursIsZeroIfDateIsEarlierThanStartDate() {
        prepareTaskForTheoreticalAdvanceTesting();
        assertThat(task.getTheoreticalCompletedTimeUntilDate(task.getStartDate()), equalTo(EffortDuration.zero()));

    }

    @Test
    public void theoreticalHoursWithADateWithinStartAndEndDateHead() {
        prepareTaskForTheoreticalAdvanceTesting();
        LocalDate limit = task.getStartAsLocalDate().plusDays(1);
        EffortDuration expected = EffortDuration.hours(8);
        assertThat(task.getTheoreticalCompletedTimeUntilDate(limit.toDateTimeAtStartOfDay().toDate()),
                equalTo(expected));
    }

    @Test
    public void theoreticalHoursWithADateWithinStartAndEndDateTail() {
        prepareTaskForTheoreticalAdvanceTesting();
        LocalDate limit = task.getEndAsLocalDate().minusDays(1);
        EffortDuration expected = EffortDuration.hours(32);
        assertThat(task.getTheoreticalCompletedTimeUntilDate(limit.toDateTimeAtStartOfDay().toDate()),
                equalTo(expected));
    }

    @Test
    public void theoreticalAdvancePercentageIsZeroIfNoResourcesAreAllocated() {
        assertThat(task.getTheoreticalAdvancePercentageUntilDate(new Date()), equalTo(new BigDecimal(0)));
    }

    @Test
    public void theoreticalPercentageIsOneIfDateIsLaterThanEndDate() {
        prepareTaskForTheoreticalAdvanceTesting();
        assertThat(task.getTheoreticalAdvancePercentageUntilDate(task.getEndDate()),
                equalTo(new BigDecimal("1.00000000")));

    }

    @Test
    public void theoreticalPercentageWithADateWithinStartAndEndDateHead() {
        prepareTaskForTheoreticalAdvanceTesting();
        LocalDate limit = task.getStartAsLocalDate().plusDays(1);
        assertThat(task.getTheoreticalAdvancePercentageUntilDate(limit.toDateTimeAtStartOfDay().toDate()),
                equalTo(new BigDecimal("0.20000000")));
    }

    @Test
    public void taskIsFinishedIfAdvancePertentageIsOne() {
        task.setAdvancePercentage(BigDecimal.ONE);
        assertTrue(task.isFinished());
        assertFalse(task.isInProgress());
        assertTrue(task.getTaskStatus() == TaskStatusEnum.FINISHED);
    }

    @Test
    public void taskIsProgressIfAdvancePercentageIsLessThanOne() {
        task.setAdvancePercentage(new BigDecimal("0.9999", new MathContext(4)));
        assertFalse(task.isFinished());
        assertTrue(task.isInProgress());
        assertTrue(task.getTaskStatus() == TaskStatusEnum.IN_PROGRESS);
    }

    @Test
    public void taskIsProgressIfAdvancePercentageIsGreaterThanZero() {
        task.setAdvancePercentage(new BigDecimal("0.0001", new MathContext(4)));
        assertFalse(task.isFinished());
        assertTrue(task.isInProgress());
        assertTrue(task.getTaskStatus() == TaskStatusEnum.IN_PROGRESS);
    }

    @Test
    public void taskIsNotInProgressIfAdvancePercentageIsZeroAndNoWorkReportsAttached() {
        task.setAdvancePercentage(BigDecimal.ZERO);
        SumChargedEffort sumChargedEffort = task.getOrderElement().getSumChargedEffort();
        assertTrue(sumChargedEffort == null || sumChargedEffort.isZero());
        assertFalse(task.isFinished());
        assertFalse(task.isInProgress());
    }

    @Test
    public void taskIsInProgressIfAdvancePercentageIsZeroButWorkReportsAttached() {
        SumChargedEffort sumChargedEffort = SumChargedEffort.create(task
                .getOrderElement());
        sumChargedEffort.addDirectChargedEffort(EffortDuration.hours(1));
        task.getOrderElement().setSumChargedEffort(sumChargedEffort);
        assertFalse(task.isFinished());
        assertTrue(task.isInProgress());
        assertTrue(task.getTaskStatus() == TaskStatusEnum.IN_PROGRESS);
    }

    @Test
    public void taskIsReadyToStartIfAllEndStartDepsAreFinished() {
        Dependency dependency = mockDependency(Type.END_START);
        dependency.getOrigin().setAdvancePercentage(BigDecimal.ONE);
        assertFalse(task.isFinished());
        assertFalse(task.isInProgress());
        assertTrue(task.getTaskStatus() == TaskStatusEnum.READY_TO_START);
    }

    @Test
    public void taskIsReadyToStartIfAllStartStartDepsAreInProgressOrFinished() {
        Dependency dependency1 = mockDependency(Type.START_START);
        dependency1.getOrigin().setAdvancePercentage(BigDecimal.ONE);
        Dependency dependency2 = mockDependency(Type.START_START);
        dependency2.getOrigin().setAdvancePercentage(new BigDecimal("0.0001", new MathContext(4)));
        assertFalse(task.isFinished());
        assertFalse(task.isInProgress());
        assertTrue(task.getTaskStatus() == TaskStatusEnum.READY_TO_START);
    }

    @Test
    public void taskIsBlockedIfHasAnUnfinishedEndStartDependency() {
        Dependency dependency = mockDependency(Type.END_START);
        dependency.getOrigin().setAdvancePercentage(new BigDecimal("0.0001", new MathContext(4)));
        assertFalse(task.isFinished());
        assertFalse(task.isInProgress());
        assertTrue(task.getTaskStatus() == TaskStatusEnum.BLOCKED);
    }

    @Test
    public void taskIsBlockedIfHasANotStartedStartStartDependency() {
        Dependency dependency = mockDependency(Type.START_START);
        dependency.getOrigin().setAdvancePercentage(BigDecimal.ZERO);
        assertFalse(task.isFinished());
        assertFalse(task.isInProgress());
        assertTrue(task.getTaskStatus() == TaskStatusEnum.BLOCKED);
    }

    @Test
    public void taskStatusCalculationTakesIntoAccountDifferentDepType() {
        Dependency dependency1 = mockDependency(Type.END_START);
        dependency1.getOrigin().setAdvancePercentage(BigDecimal.ONE);
        Dependency dependency2 = mockDependency(Type.START_START);
        dependency2.getOrigin().setAdvancePercentage(new BigDecimal("0.0001", new MathContext(4)));
        assertFalse(task.isFinished());
        assertFalse(task.isInProgress());
        assertTrue(task.getTaskStatus() == TaskStatusEnum.READY_TO_START);
        dependency2.getOrigin().setAdvancePercentage(BigDecimal.ZERO);
        assertTrue(task.getTaskStatus() == TaskStatusEnum.BLOCKED);
    }

    @Test
    public void taskIsBlockedIfHasAnUnfinishedEndStartDependencyUsingGroup() {
         Task task1 = createValidTaskWithFullProgress();
         Task task2 = createValidTask();
         task2.setAdvancePercentage(new BigDecimal("0.0001", new MathContext(4)));
         TaskGroup taskGroup = new TaskGroup();
         taskGroup.addTaskElement(task1);
         taskGroup.addTaskElement(task2);
         mockDependency(taskGroup, this.task, Type.END_START);
         assertFalse(task.isFinished());
         assertFalse(task.isInProgress());
         assertTrue(task.getTaskStatus() == TaskStatusEnum.BLOCKED);
    }

    @Test
    public void taskDependenciesDontMatterIfProgressIsNotZero() {
         Task task1 = createValidTaskWithFullProgress();
         Task task2 = createValidTask();
         task2.setAdvancePercentage(new BigDecimal("0.0001", new MathContext(4)));
         TaskGroup taskGroup = new TaskGroup();
         taskGroup.addTaskElement(task1);
         taskGroup.addTaskElement(task2);
         mockDependency(taskGroup, this.task, Type.END_START);
         task.setAdvancePercentage(new BigDecimal("0.0001", new MathContext(4)));
         assertFalse(task.isFinished());
         assertTrue(task.getTaskStatus() == TaskStatusEnum.IN_PROGRESS);
         task.setAdvancePercentage(BigDecimal.ONE);
         assertTrue(task.getTaskStatus() == TaskStatusEnum.FINISHED);
    }

    @Test
    public void taskStatusNotAffectedByEndEndDeps() {
        Dependency dependency = mockDependency(Type.END_END);
        dependency.getOrigin().setAdvancePercentage(BigDecimal.ZERO);
        assertTrue(task.getTaskStatus() == TaskStatusEnum.READY_TO_START);
    }

    @Test
    public void taskWithNoDeadlineHasCorrectDeadlineViolationStatus() {
        task.setDeadline(null);
        assertTrue(task.getDeadlineViolationStatus() ==
                TaskDeadlineViolationStatusEnum.NO_DEADLINE);
    }

    @Test
    public void taskWithViolatedDeadlineHasCorrectDeadlineViolationStatus() {
        task.setDeadline(new LocalDate());
        LocalDate tomorrow = new LocalDate().plusDays(1);
        task.setEndDate(tomorrow.toDateTimeAtStartOfDay().toDate());
        assertTrue(task.getDeadlineViolationStatus() ==
                TaskDeadlineViolationStatusEnum.DEADLINE_VIOLATED);
    }

    @Test
    public void taskWithUnviolatedDeadlineHasCorrectDeadlineViolationStatusJustInTime() {
        LocalDate now = new LocalDate();
        task.setDeadline(now);
        task.setEndDate(now.toDateTimeAtStartOfDay().toDate());
        assertTrue(task.getDeadlineViolationStatus() ==
                TaskDeadlineViolationStatusEnum.ON_SCHEDULE);
    }

    @Test
    public void taskWithUnviolatedDeadlineHasCorrectDeadlineViolationStatusMargin() {
        LocalDate now = new LocalDate();
        task.setDeadline(now);
        task.setEndDate(now.minusDays(1).toDateTimeAtStartOfDay().toDate());
        assertTrue(task.getDeadlineViolationStatus() ==
                TaskDeadlineViolationStatusEnum.ON_SCHEDULE);
    }

    private abstract class AllocationConfiguration {

        public Direction getDirection() {
            return task.getAllocationDirection();
        }

        public IntraDayDate getAllocationStart() {
            if (getCustomStart() == null) {
                return task.getIntraDayStartDate();
            }
            return getCustomStart();
        }

        public IntraDayDate getAllocationEnd() {
            if (getCustomEnd() == null) {
                return task.getIntraDayEndDate();
            }
            return getCustomEnd();
        }

        public abstract CalculatedValue getCalculatedValue();

        public EffortDuration getEffortToAllocate() {
            throw new IllegalStateException("with calculated value: "
                    + getCalculatedValue()
                    + " getEffortToAllocate() is called.");
        }

        IntraDayDate getCustomStart() {
            return null;
        }

        IntraDayDate getCustomEnd() {
            return null;
        }

        RecurrencesModification getCustomRecurrence() {
            return Task.changeRecurrenceInformation(RecurrenceInformation
                    .noRecurrence());
        }

        public void allocateAndMerge(
                List<? extends AllocationModification> newAllocations) {
            allocateAndMerge(Collections.<ResourceAllocation<?>> emptyList(),
                    newAllocations);
        }

        public void allocateAndMerge(List<? extends ResourceAllocation<?>> originals,
                List<? extends AllocationModification> modificationsAndNew) {
            allocateAndMerge(originals, modificationsAndNew,
                    Collections.<ResourceAllocation<?>> emptyList());
        }

        public void allocateAndMerge(
                List<? extends ResourceAllocation<?>> originals,
                List<? extends AllocationModification> modificationsAndNew,
                Collection<? extends ResourceAllocation<?>> toRemove) {
            List<? extends AllocationModification> allocations = modificationsAndNew;

            IntraDayDate start = getAllocationStart();
            IntraDayDate end = getAllocationEnd();

            Integer workableDays = null;

            if (!allocations.isEmpty()) {
                switch (getCalculatedValue()) {
                case NUMBER_OF_HOURS:
                    ResourceAllocation.allocating(
                            ofType(ResourcesPerDayModification.class,
                                    allocations)).allocateOn(start, end);
                    workableDays = task.getWorkableDaysFrom(start.getDate(),
                            end.asExclusiveEnd());
                    break;
                case END_DATE:
                    IntraDayDate date = ResourceAllocation.allocating(
                            ofType(ResourcesPerDayModification.class,
                                    allocations)).untilAllocating(
                            getDirection() == Direction.FORWARD ? start : end,
                            getDirection(), getEffortToAllocate());
                    if (getDirection() == Direction.FORWARD) {
                        end = date;
                    } else {
                        start = date;
                    }
                    break;
                case RESOURCES_PER_DAY:
                    ResourceAllocation.allocatingHours(
                            ofType(EffortModification.class, allocations))
                            .forWholeAllocationOn(start, end);
                    workableDays = task.getWorkableDaysFrom(start.getDate(),
                            end.asExclusiveEnd());
                    break;
                default:
                    throw new RuntimeException("cant handle: "
                            + getCalculatedValue());
                }
            }
            List<ResourceAllocation<?>> newAllocations = AllocationModification
                    .getBeingModified(modificationsAndNew.subList(
                            originals.size(), modificationsAndNew.size()));

            List<ResourceAllocation<?>> updates = AllocationModification
                    .getBeingModified(modificationsAndNew.subList(0,
                            originals.size()));

            List<ModifiedAllocation> modifications = new ArrayList<Task.ModifiedAllocation>();
            for (int i = 0; i < originals.size(); i++) {
                modifications.add(new ModifiedAllocation(originals.get(i),
                        updates.get(i)));
            }

            task.mergeAllocation(createNiceMock(IResourcesSearcher.class),
                    mainScenario, getCustomRecurrence(),
                    start,
                    end, workableDays, getCalculatedValue(), newAllocations,
                    modifications, toRemove);
        }

    }

    public AllocationConfiguration untilAllocating(final int hours) {
        return untilAllocating(hours, RecurrenceInformation.noRecurrence());
    }

    public AllocationConfiguration untilAllocating(final int hours,
            final RecurrenceInformation recurrence) {
        return untilAllocating(hours,
                Task.changeRecurrenceInformation(recurrence));
    }

    public AllocationConfiguration untilAllocating(final int hours,
            final RecurrencesModification recurrenceModification) {
        return new AllocationConfiguration() {

            @Override
            public CalculatedValue getCalculatedValue() {
                return CalculatedValue.END_DATE;
            }

            @Override
            public EffortDuration getEffortToAllocate() {
                return EffortDuration.hours(hours);
            }

            @Override
            RecurrencesModification getCustomRecurrence() {
                return recurrenceModification;
            }
        };
    }

    private AllocationConfiguration calculateHoursOn(final int days) {
        return calculateHoursOn(days, Direction.FORWARD,
                RecurrenceInformation.noRecurrence());
    }

    private AllocationConfiguration calculateHoursOn(final int days,
            final Direction direction,
            final RecurrenceInformation recurrenceInformation) {
        return new AllocationConfiguration() {

            @Override
            public CalculatedValue getCalculatedValue() {
                return CalculatedValue.NUMBER_OF_HOURS;
            }

            @Override
            IntraDayDate getCustomEnd() {
                if (direction != Direction.FORWARD) {
                    return super.getCustomEnd();
                }
                return IntraDayDate.startOfDay(getAllocationStart().getDate()
                        .plusDays(days));
            }

            @Override
            IntraDayDate getCustomStart() {
                if (direction != Direction.BACKWARD) {
                    return super.getCustomStart();
                }
                return IntraDayDate.startOfDay(getAllocationEnd().getDate()
                        .minusDays(days));
            }

            @Override
            RecurrencesModification getCustomRecurrence() {
                return Task.changeRecurrenceInformation(recurrenceInformation);
            }
        };
    }


    @Test
    public void withCalculatingTheEndStrategyTheEndOfTheTaskChanges() {
        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation
                .create(task);
        givenWorker(8);
        resourceAllocation.setResource(this.worker);

        ResourcesPerDayModification resourcePerDayModification = ResourcesPerDayModification
                .create(resourceAllocation, ResourcesPerDay.amount(1));

        untilAllocating(40)
                .allocateAndMerge(asList(resourcePerDayModification));
        assertThat(task, allAllocationsSatisfied());

        assertThat("40 hours at one resource per day must take 5 days",
                task.getEndAsLocalDate(), equalTo(task.getStartAsLocalDate()
                .plusDays(5)));

        SpecificResourceAllocation copy = (SpecificResourceAllocation) resourceAllocation
                .copy(mainScenario);
        ResourcesPerDayModification modification = ResourcesPerDayModification
                .create(copy, ResourcesPerDay.amount(2));

        untilAllocating(40).allocateAndMerge(asList(resourceAllocation),
                asList(modification));
        assertThat(task, allAllocationsSatisfied());

        assertThat("40 hours at two resource per day must take 2.5 days", task
                .getIntraDayEndDate().getDate(), equalTo(task
                .getStartAsLocalDate().plusDays(2)));
    }

    @Test
    public void withCalculatingTheEndStrategyIfAllocatingBackwardsTheStartOfTheTaskChanges() {
        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation
                .create(task);
        givenWorker(8);
        resourceAllocation.setResource(this.worker);

        ResourcesPerDayModification resourcePerDayModification = ResourcesPerDayModification
                .create(resourceAllocation, ResourcesPerDay.amount(1));

        untilAllocating(40)
                .allocateAndMerge(asList(resourcePerDayModification));
        assertThat(task, allAllocationsSatisfied());

        LocalDate previousStart = task.getStartAsLocalDate();
        IntraDayDate newEnd = task.getIntraDayEndDate();

        IDatesHandler datesHandler = task.getDatesHandler(mainScenario,
                createNiceMock(IResourcesSearcher.class));
        datesHandler.moveEndTo(newEnd.previousDayAtStart());
        assertThat(task, allAllocationsSatisfied());

        assertThat(task.getStartAsLocalDate(),
                equalTo(previousStart.minusDays(1)));
        assertThat(task.getAllocationDirection(), equalTo(Direction.BACKWARD));
    }

    @Test
    public void anAllocationCanBeRemoved() {
        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation
                .create(task);
        givenWorker(8);
        resourceAllocation.setResource(this.worker);

        ResourcesPerDayModification resourcePerDayModification = ResourcesPerDayModification
                .create(resourceAllocation, ResourcesPerDay.amount(1));

        assertTrue(task.getAllResourceAllocations().isEmpty());

        untilAllocating(40)
                .allocateAndMerge(asList(resourcePerDayModification));
        assertThat(task, allAllocationsSatisfied());

        assertThat(task.getAllResourceAllocations().size(), equalTo(1));

        untilAllocating(40).allocateAndMerge(
                Collections.<ResourceAllocation<?>> emptyList(),
                Collections.<AllocationModification> emptyList(),
                asList(resourceAllocation));
        assertThat(task, allAllocationsSatisfied());

        assertTrue(task.getAllResourceAllocations().isEmpty());
    }

    @Test
    public void canAllocateSomeHoursOnARange() {
        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation
                .create(task);

        assertTrue(task.getRecurrences().isEmpty());
        assertTrue(task.getNotRecurrentResourceAllocations().isEmpty());

        givenWorker(8);
        resourceAllocation.setResource(this.worker);

        ResourcesPerDayModification resourcePerDayModification = ResourcesPerDayModification
                .create(resourceAllocation, ResourcesPerDay.amount(1));

        AllocationConfiguration hoursOn = calculateHoursOn(5);
        hoursOn.allocateAndMerge(asList(resourcePerDayModification));
        assertThat(task, allAllocationsSatisfied());

        assertThat(
                "8 hours for 5 days, 40 hours",
                AggregateOfResourceAllocations.createFromSatisfied(
                        task.getAllResourceAllocations()).getTotalEffort(),
                equalTo(hours(40)));
    }

    @Test
    public void canCalculateTheResourcesPerDayToAllocateSomeEffortOnATask() {
        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation
                .create(task);

        assertTrue(task.getRecurrences().isEmpty());
        assertTrue(task.getNotRecurrentResourceAllocations().isEmpty());

        givenWorker(8);
        resourceAllocation.setResource(this.worker);

        task.setIntraDayEndDate(IntraDayDate.startOfDay(task
                .getStartAsLocalDate().plusDays(5)));

        AllocationConfiguration allocationConfiguration = new AllocationConfiguration() {

            @Override
            public CalculatedValue getCalculatedValue() {
                return CalculatedValue.RESOURCES_PER_DAY;
            }
        };

        allocationConfiguration.allocateAndMerge(asList(EffortModification
                .create(resourceAllocation, hours(40))));
        assertThat(task, allAllocationsSatisfied());

        assertThat(task.getAllResourceAllocations().size(), equalTo(1));
        assertThat(resourceAllocation.getResourcesPerDay(),
                equalTo(ResourcesPerDay.amount(1)));
    }

    @Test
    public void ifNoRecurrenceIsUsedTheAllocationItHasIsNotRecurrent() {
        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation
                .create(task);

        assertTrue(task.getRecurrences().isEmpty());
        assertTrue(task.getNotRecurrentResourceAllocations().isEmpty());

        givenWorker(8);
        resourceAllocation.setResource(this.worker);

        ResourcesPerDayModification resourcePerDayModification = ResourcesPerDayModification
                .create(resourceAllocation, ResourcesPerDay.amount(1));

        untilAllocating(40)
                .allocateAndMerge(asList(resourcePerDayModification));
        assertThat(task, allAllocationsSatisfied());

        assertTrue(task.getRecurrences().isEmpty());
        assertThat(task.getNotRecurrentResourceAllocations().size(), equalTo(1));
        assertTrue(task.getNotRecurrentResourceAllocations().contains(
                resourceAllocation));
    }

    @Test
    public void ifRecurrenceInformationProvidedRecurrentAllocationsAreCreated() {
        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation
                .create(task);

        givenWorker(8);
        resourceAllocation.setResource(this.worker);

        ResourcesPerDayModification resourcePerDayModification = ResourcesPerDayModification
                .create(resourceAllocation, ResourcesPerDay.amount(1));

        untilAllocating(40,
                RecurrenceInformation.endAtNumberOfRepetitions(1,
                        RecurrencePeriodicity.WEEKLY, 1))
                .allocateAndMerge(asList(resourcePerDayModification));

        assertThat(task.getRecurrences().size(), equalTo(1));

        LocalDate start = task.getStartAsLocalDate();
        assertThat(
                task.getRecurrences(),
                matchesDates(start.plusDays(7), start.plusDays(7).plusDays(5)));

        assertThat(task.getAssignedEffort(), equalTo(hours(80)));

        ManualRecurrencesModification recurrencesModification = task
                .copyRecurrencesToModify(mainScenario);

        Map<Recurrence, List<ModifiedAllocation>> allocationsPerRecurrence = recurrencesModification
                .getAllocationsPerRecurrence();
        assertThat(allocationsPerRecurrence.size(), equalTo(1));
        List<ModifiedAllocation> modifiedAllocations = allocationsPerRecurrence
                .values().iterator().next();
        assertThat(modifiedAllocations.size(), equalTo(1));
        ModifiedAllocation modified = modifiedAllocations.get(0);

        modified.getModification()
                .withPreviousAssociatedResources()
                .onIntervalWithinTask(start.plusDays(8), start.plusDays(9))
                .allocate(hours(10));

        assertThat(
                "Changing the modification doesn't modify the original allocation",
                task.getAssignedEffort(), equalTo(hours(80)));

        untilAllocating(40, recurrencesModification)
                .allocateAndMerge(asList(resourcePerDayModification));

        assertThat(
                "Once the merging is done, the original allocations are modified",
                task.getAssignedEffort(), equalTo(hours(82)));
    }

    @Test
    public void manualChangesOnTheRecurrenceCanModifyTheTaskDates() {
        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation
                .create(task);

        givenWorker(8);
        resourceAllocation.setResource(this.worker);

        ResourcesPerDayModification resourcePerDayModification = ResourcesPerDayModification
                .create(resourceAllocation, ResourcesPerDay.amount(1));

        untilAllocating(40,
                RecurrenceInformation.endAtNumberOfRepetitions(1,
                        RecurrencePeriodicity.WEEKLY, 1))
                .allocateAndMerge(asList(resourcePerDayModification));

        assertThat(task.getRecurrences().size(), equalTo(1));

        LocalDate start = task.getStartAsLocalDate();
        assertThat(task.getRecurrences(),
                matchesDates(start.plusDays(7), start.plusDays(7).plusDays(5)));

        assertThat(task.getIntraDayEndDate(),
                equalTo(IntraDayDate.startOfDay(start.plusDays(7).plusDays(5))));

        ManualRecurrencesModification manualRecurrencesModification = task
                .copyRecurrencesToModify(mainScenario);

        Map<Recurrence, List<ModifiedAllocation>> allocationsPerRecurrence = manualRecurrencesModification
                .getAllocationsPerRecurrence();
        List<ModifiedAllocation> modifiedAllocations = allocationsPerRecurrence
                .values().iterator().next();

        List<ResourcesPerDayModification> resourcesPerDayModifications = ResourcesPerDayModification
                .fromExistent(ModifiedAllocation.modified(modifiedAllocations),
                        createNiceMock(IResourcesSearcher.class));

        ResourceAllocation.allocating(resourcesPerDayModifications)
                .untilAllocating(IntraDayDate.startOfDay(start.plusDays(7)),
                        Direction.FORWARD, hours(48));

        untilAllocating(40, manualRecurrencesModification).allocateAndMerge(
                asList(resourcePerDayModification));

        assertThat(
                "The end date is changed due to the recurrence taking more time",
                task.getIntraDayEndDate(),
                equalTo(IntraDayDate.startOfDay(start.plusDays(7).plusDays(6))));

    }

    @Test
    public void recurrenceCanBeModifiedManually() {
        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation
                .create(task);

        givenWorker(8);
        resourceAllocation.setResource(this.worker);

        ResourcesPerDayModification resourcePerDayModification = ResourcesPerDayModification
                .create(resourceAllocation, ResourcesPerDay.amount(1));

        untilAllocating(40,
                RecurrenceInformation.endAtNumberOfRepetitions(2,
                        RecurrencePeriodicity.WEEKLY, 1))
                .allocateAndMerge(asList(resourcePerDayModification));

        assertThat(task, allAllocationsSatisfied());
    }

    @Test
    public void ifRecurrenceOverlapsWithPreviousItMustBeOmitted() {
        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation
                .create(task);

        givenWorker(8);
        resourceAllocation.setResource(this.worker);

        ResourcesPerDayModification resourcePerDayModification = ResourcesPerDayModification
                .create(resourceAllocation, ResourcesPerDay.amount(1));

        untilAllocating(80,
                RecurrenceInformation.endAtNumberOfRepetitions(2,
                        RecurrencePeriodicity.WEEKLY, 1))
                .allocateAndMerge(asList(resourcePerDayModification));

        assertThat(task, allAllocationsSatisfied());
        assertThat(task.getRecurrences().size(), equalTo(1));
        LocalDate start = task.getStartAsLocalDate();
        assertThat(
                task.getRecurrences(),
                matchesDates(start.plusDays(14), start.plusDays(14)
                        .plusDays(10)));
    }

    @Test
    public void recurrencesAreCreatedInBackwardAllocations() {
        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation
                .create(task);

        IDatesHandler datesHandler = task.getDatesHandler(mainScenario,
                createNiceMock(IResourcesSearcher.class));
        IntraDayDate taskEnd = IntraDayDate.startOfDay(task
                .getStartAsLocalDate()
                .plusDays(30));
        datesHandler.moveEndTo(taskEnd);
        assertThat(task.getAllocationDirection(), equalTo(Direction.BACKWARD));

        givenWorker(8);
        resourceAllocation.setResource(this.worker);

        ResourcesPerDayModification resourcePerDayModification = ResourcesPerDayModification
                .create(resourceAllocation, ResourcesPerDay.amount(1));

        untilAllocating(40,
                RecurrenceInformation.endAtNumberOfRepetitions(2,
                        RecurrencePeriodicity.WEEKLY, 1))
                .allocateAndMerge(asList(resourcePerDayModification));

        assertThat(task, allAllocationsSatisfied());

        assertThat(task.getRecurrences().size(), equalTo(2));
        assertThat(task.getNotRecurrentResourceAllocations().size(), equalTo(1));
        assertTrue(task.getNotRecurrentResourceAllocations().contains(
                resourceAllocation));
        assertThat(task.getAllResourceAllocations().size(), equalTo(3));

        ResourceAllocation<?> notRecurrent = task
                .getNotRecurrentResourceAllocations().iterator().next();
        assertThat(notRecurrent.getIntraDayEndDate(), equalTo(taskEnd));
        assertThat(notRecurrent.getIntraDayStartDate(),
                equalTo(IntraDayDate.startOfDay(taskEnd.getDate().minusDays(5))));

        LocalDate localEndDate = taskEnd.getDate();
        assertThat(
                task.getRecurrences(),
                matchesDates(Direction.BACKWARD, localEndDate.minusDays(7),
                        localEndDate.minusDays(7).minusDays(5),
                        localEndDate.minusDays(14), localEndDate.minusDays(19)));
    }

    @Test
    public void ifRecurrenceOverlapsWithPreviousInBackwardsDirectionItMustBeOmitted() {
        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation
                .create(task);

        IDatesHandler datesHandler = task.getDatesHandler(mainScenario,
                createNiceMock(IResourcesSearcher.class));
        IntraDayDate taskEnd = IntraDayDate.startOfDay(task
                .getStartAsLocalDate().plusDays(30));
        datesHandler.moveEndTo(taskEnd);
        assertThat(task.getAllocationDirection(), equalTo(Direction.BACKWARD));

        givenWorker(8);
        resourceAllocation.setResource(this.worker);

        ResourcesPerDayModification resourcePerDayModification = ResourcesPerDayModification
                .create(resourceAllocation, ResourcesPerDay.amount(1));

        untilAllocating(80,
                RecurrenceInformation.endAtNumberOfRepetitions(2,
                        RecurrencePeriodicity.WEEKLY, 1))
                .allocateAndMerge(asList(resourcePerDayModification));

        assertThat(task, allAllocationsSatisfied());

        assertThat(task.getRecurrences().size(), equalTo(1));
        assertThat(task.getNotRecurrentResourceAllocations().size(), equalTo(1));
        assertTrue(task.getNotRecurrentResourceAllocations().contains(
                resourceAllocation));
        assertThat(task.getAllResourceAllocations().size(), equalTo(2));

        LocalDate localEndDate = taskEnd.getDate();
        assertThat(
                task.getRecurrences(),
                matchesDates(Direction.BACKWARD, localEndDate.minusDays(14),
                        localEndDate.minusDays(24)));
    }

    @Test
    public void recurrencesAreCreatedInBackwardAllocationsWithCalculatingHours() {
        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation
                .create(task);

        IDatesHandler datesHandler = task.getDatesHandler(mainScenario,
                createNiceMock(IResourcesSearcher.class));
        IntraDayDate taskEnd = IntraDayDate.startOfDay(task
                .getStartAsLocalDate()
                .plusDays(30));
        datesHandler.moveEndTo(taskEnd);
        assertThat(task.getAllocationDirection(), equalTo(Direction.BACKWARD));

        givenWorker(8);
        resourceAllocation.setResource(this.worker);

        ResourcesPerDayModification resourcePerDayModification = ResourcesPerDayModification
                .create(resourceAllocation, ResourcesPerDay.amount(1));
        calculateHoursOn(5, Direction.BACKWARD,
                RecurrenceInformation.endAtNumberOfRepetitions(2,
                        RecurrencePeriodicity.WEEKLY, 1))
                .allocateAndMerge(asList(resourcePerDayModification));

        assertThat(task, allAllocationsSatisfied());

        assertThat(task.getRecurrences().size(), equalTo(2));
        assertThat(task.getNotRecurrentResourceAllocations().size(), equalTo(1));
        assertTrue(task.getNotRecurrentResourceAllocations().contains(
                resourceAllocation));
        assertThat(task.getAllResourceAllocations().size(), equalTo(3));

        ResourceAllocation<?> notRecurrent = task
                .getNotRecurrentResourceAllocations().iterator().next();
        assertThat(notRecurrent.getIntraDayEndDate(), equalTo(taskEnd));
        assertThat(notRecurrent.getIntraDayStartDate(),
                equalTo(IntraDayDate.startOfDay(taskEnd.getDate().minusDays(5))));

        LocalDate localEndDate = taskEnd.getDate();
        assertThat(
                task.getRecurrences(),
                matchesDates(Direction.BACKWARD, localEndDate.minusDays(7),
                        localEndDate.minusDays(7).minusDays(5),
                        localEndDate.minusDays(14), localEndDate.minusDays(19)));
    }

    private Matcher<Task> allAllocationsSatisfied() {
        return new BaseMatcher<Task>() {

            @Override
            public boolean matches(Object arg) {
                if (arg instanceof Task) {
                    Task t = (Task) arg;
                    Set<ResourceAllocation<?>> allResourceAllocations = t
                            .getAllResourceAllocations();
                    for (ResourceAllocation<?> each : allResourceAllocations) {
                        if (!each.isSatisfied()) {
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("All resource allocations must be satisfied");
            }
        };
    }

    private Matcher<List<Recurrence>> matchesDates(final LocalDate... dates) {
        return matchesDates(Direction.FORWARD, dates);
    }

    private Matcher<List<Recurrence>> matchesDates(Direction direction,
            final LocalDate... dates) {
        Validate.isTrue(dates.length % 2 == 0);

        final boolean isForward = direction == Direction.FORWARD;

        return new BaseMatcher<List<Recurrence>>() {

            @Override
            public boolean matches(Object arg) {
                if (arg instanceof List) {
                    List<Recurrence> recurrences = (List<Recurrence>) arg;
                    if (recurrences.size() != dates.length / 2) {
                        return false;
                    }
                    int i = 0;
                    for (Recurrence each : recurrences) {
                        int j = i * 2;
                        LocalDate start = isForward ? dates[j] : dates[j + 1];
                        LocalDate end = isForward ? dates[j + 1] : dates[j];
                        if (!(each.getStart().equals(start) && each.getEnd()
                                .equals(end))) {
                            return false;
                        }
                        i++;
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                for (int i = 0; i < dates.length / 2; i++, i++) {
                    LocalDate start = dates[i];
                    LocalDate end = dates[i + 1];
                    description.appendText("Recurrence number " + (i / 2) + 1
                            + " must go from " + start + " to " + dates[i + 1]);
                }
            }
        };
    }

    private void prepareTaskForTheoreticalAdvanceTesting() {
        task.getHoursGroup().setWorkingHours(40);
        assertThat(task.getTotalHours(), equalTo(40));
        task.setEndDate(task.getStartAsLocalDate().plusDays(5).toDateTimeAtStartOfDay().toDate());

        SpecificResourceAllocation resourceAllocation = SpecificResourceAllocation.create(task);

        givenWorker(8);
        resourceAllocation.setResource(this.worker);
        assertTrue(resourceAllocation.getResource() != null);

        resourceAllocation.allocate(ResourcesPerDay.amount(1));
        assertThat(resourceAllocation.getAssignments().size(), equalTo(5));
        assertThat(resourceAllocation.getAssignments(), haveHours(8, 8, 8, 8, 8));

        assertThat(task.getAssignedHours(), equalTo(0));
        task.addResourceAllocation(resourceAllocation);
        assertTrue(task.getNonLimitingAndNotRecurrentResourceAllocations().size() == 1);
        assertThat(task.getAssignedHours(), equalTo(40));
        assertTrue(task.getDayAssignments(FilterType.KEEP_ALL).size() == 5);
    }

    private void givenWorker(int hoursPerDay) {
        this.worker = createNiceMock(Worker.class);
        givenResourceCalendarAlwaysReturning(hoursPerDay);
        expect(this.worker.getCalendar()).andReturn(this.workerCalendar).anyTimes();
        replay(this.worker);
    }

    private void givenResourceCalendarAlwaysReturning(final int hours) {
        this.workerCalendar = SpecificResourceAllocationTest.
                createResourceCalendarAlwaysReturning(hours);
    }

    private Dependency mockDependency(Type type){
        return mockDependency(createValidTask(), this.task, type);
    }

    private Dependency mockDependency(TaskElement origin, TaskElement destination, Type type) {
        Dependency dependency = createNiceMock(Dependency.class);
        expect(dependency.getOrigin()).andReturn(origin).anyTimes();
        expect(dependency.getDestination()).andReturn(destination).anyTimes();
        expect(dependency.getType()).andReturn(type).anyTimes();
        replay(dependency);
        origin.add(dependency);
        destination.add(dependency);
        return dependency;
    }
}