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

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.orders.entities.SchedulingDataForVersion;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.Dependency.Type;
import org.navalplanner.business.planner.entities.StartConstraintType;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.TaskMilestone;
import org.navalplanner.business.planner.entities.TaskStartConstraint;
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
public class TaskElementTest {

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Before
    public void loadRequiredaData() {
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
    }

    private TaskElement task = new Task();

    private Dependency exampleDependency;

    public TaskElementTest() {
        this.exampleDependency = Dependency.create(new Task(), new Task(),
                Type.END_START);
    }

    @Test
    public void initiallyAssociatedDependenciesAreEmpty() {
        assertTrue(task.getDependenciesWithThisDestination().isEmpty());
        assertTrue(task.getDependenciesWithThisOrigin().isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void dependenciesWithThisOriginCollectionCannotBeModified() {
        task.getDependenciesWithThisOrigin().add(exampleDependency);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void dependenciesWithThisDestinationCollectionCannotBeModified() {
        task.getDependenciesWithThisDestination().add(exampleDependency);
    }

    @Test
    public void taskElementHasStartDatePropertyAndItIsRoundedToTheStartOfTheDay() {
        Date now = new Date();
        task.setStartDate(now);
        assertThat(task.getStartDate(), equalTo(toStartOfDay(now)));
        task.setEndDate(now);
        assertThat(task.getEndDate(), equalTo(toStartOfDay(now)));
    }

    private static Date toStartOfDay(Date date) {
        return LocalDate.fromDateFields(date)
                .toDateTimeAtStartOfDay().toDate();
    }

    @Test
    public void aDependencyWithThisOriginCanBeRemoved() {
        Task origin = new Task();
        Task destination = new Task();
        Type type = Type.START_END;
        Dependency.create(origin, destination, type);
        assertThat(origin.getDependenciesWithThisOrigin().size(), equalTo(1));
        assertThat(destination.getDependenciesWithThisDestination().size(),
                equalTo(1));
        origin.removeDependencyWithDestination(destination, type);
        assertThat(origin.getDependenciesWithThisOrigin().size(), equalTo(0));
        assertThat(destination.getDependenciesWithThisDestination().size(),
                equalTo(0));
    }

    private void addDependenciesForChecking(TaskElement taskBeingTransformed,
            TaskElement sourceDependencyTask,
            TaskElement destinationDependencyTask) {
        Dependency.create(sourceDependencyTask, taskBeingTransformed,
                Type.END_START);
        Dependency.create(taskBeingTransformed, destinationDependencyTask,
                Type.END_START);
    }

    public void detachRemovesDependenciesFromRelatedTasks() {
        Task taskToDetach = (Task) TaskTest.createValidTask();
        Task sourceDependencyTask = (Task) TaskTest.createValidTask();
        Task destinationDependencyTask = (Task) TaskTest.createValidTask();
        taskToDetach.setName("prueba");
        taskToDetach.setNotes("blabla");
        taskToDetach.setStartDate(new Date());
        addDependenciesForChecking(taskToDetach, sourceDependencyTask,
                destinationDependencyTask);
        taskToDetach.detach();
        assertThat(sourceDependencyTask.getDependenciesWithThisOrigin().size(),
                equalTo(0));
        assertThat(destinationDependencyTask
                .getDependenciesWithThisDestination().size(), equalTo(0));
    }

    @Test
    public void detachRemovesTaskFromParent() {
        TaskGroup parent = TaskGroupTest.createValidTaskGroup();
        Task child = (Task) TaskTest.createValidTask();
        Task anotherChild = (Task) TaskTest.createValidTask();
        parent.addTaskElement(child);
        parent.addTaskElement(anotherChild);
        child.detach();
        assertThat(parent.getChildren().size(), equalTo(1));
    }

    @Test
    public void MilestoneOrderElementIsNull() {
        TaskMilestone milestone = TaskMilestone.create(new Date());
        assertThat(milestone.getOrderElement(), nullValue());
    }

    @Test
    public void theDeadlineOfTheOrderElementIsCopied() {
        OrderLine orderLine = OrderLine.create();
        addOrderTo(orderLine);
        LocalDate deadline = new LocalDate(2007, 4, 4);
        orderLine.setDeadline(asDate(deadline));
        TaskSource taskSource = asTaskSource(orderLine);
        Task task = Task.createTask(taskSource);
        assertThat(task.getDeadline(), equalTo(deadline));
    }

    private TaskSource asTaskSource(OrderLine orderLine) {
        List<HoursGroup> hoursGroups = orderLine.getHoursGroups();
        if (hoursGroups.isEmpty()) {
            hoursGroups = Collections.singletonList(createHoursGroup(100));
        }
        return TaskSource.create(mockSchedulingDataForVersion(orderLine),
                hoursGroups);
    }

    public static SchedulingDataForVersion mockSchedulingDataForVersion(
            OrderElement orderElement) {
        SchedulingDataForVersion result = createNiceMock(SchedulingDataForVersion.class);
        TaskSource taskSource = createNiceMock(TaskSource.class);
        expect(result.getOrderElement()).andReturn(orderElement).anyTimes();
        expect(taskSource.getOrderElement()).andReturn(orderElement).anyTimes();
        expect(result.getTaskSource()).andReturn(taskSource).anyTimes();
        replay(result, taskSource);
        return result;
    }

    private static Date asDate(LocalDate localDate) {
        return localDate.toDateTimeAtStartOfDay().toDate();
    }

    private static HoursGroup createHoursGroup(int hours) {
        HoursGroup result = new HoursGroup();
        result.setWorkingHours(hours);
        return result;
    }

    @Test
    public void ifNoParentWithStartDateTheStartConstraintIsSoonAsPossible() {
        OrderLine orderLine = OrderLine.create();
        addOrderTo(orderLine);
        TaskSource taskSource = asTaskSource(orderLine);
        Task task = Task.createTask(taskSource);
        assertThat(task.getStartConstraint(),
                isOfType(StartConstraintType.AS_SOON_AS_POSSIBLE));
    }

    private void addOrderTo(OrderElement orderElement) {
        Order order = new Order();
        order.useSchedulingDataFor(TaskTest.mockOrderVersion());
        order.setInitDate(new Date());
        order.add(orderElement);
    }

    @Test
    public void ifTheOrderLineHasDeadlineTheStartConstraintIsNotLaterThan() {
        OrderLine orderLine = OrderLine.create();
        addOrderTo(orderLine);
        LocalDate deadline = new LocalDate(2007, 4, 4);
        orderLine.setDeadline(asDate(deadline));
        TaskSource taskSource = asTaskSource(orderLine);
        Task task = Task.createTask(taskSource);
        assertThat(task.getStartConstraint(),
                isOfType(StartConstraintType.FINISH_NOT_LATER_THAN));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ifSomeParentHasInitDateTheStartConstraintIsNotEarlierThan() {
        LocalDate initDate = new LocalDate(2005, 10, 5);
        OrderLineGroup group = OrderLineGroup.create();
        addOrderTo(group);
        group.setInitDate(asDate(initDate));
        OrderLine orderLine = OrderLine.create();
        group.add(orderLine);
        TaskSource taskSource = asTaskSource(orderLine);
        Task task = Task.createTask(taskSource);
        assertThat(task.getStartConstraint(), allOf(
                isOfType(StartConstraintType.START_NOT_EARLIER_THAN),
                hasValue(initDate)));
    }

    @Test
    public void unlessTheOnlyParentWithInitDateNotNullIsTheOrder() {
        OrderLine orderLine = OrderLine.create();
        addOrderTo(orderLine);
        Order order = orderLine.getOrder();
        Date initDate = asDate(new LocalDate(2005, 10, 5));
        order.setInitDate(initDate);
        TaskSource taskSource = asTaskSource(orderLine);
        Task task = Task.createTask(taskSource);
        assertThat(task.getStartConstraint(),
                isOfType(StartConstraintType.AS_SOON_AS_POSSIBLE));
    }

    private static Matcher<TaskStartConstraint> isOfType(
            final StartConstraintType type) {
        return new BaseMatcher<TaskStartConstraint>() {

            @Override
            public boolean matches(Object object) {
                if (object instanceof TaskStartConstraint) {
                    TaskStartConstraint startConstraint = (TaskStartConstraint) object;
                    return startConstraint.getStartConstraintType() == type;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("the start constraint must be of type "
                        + type);
            }
        };
    }

    private static Matcher<TaskStartConstraint> hasValue(final LocalDate value) {
        return new BaseMatcher<TaskStartConstraint>() {

            @Override
            public boolean matches(Object object) {
                if (object instanceof TaskStartConstraint) {
                    TaskStartConstraint startConstraint = (TaskStartConstraint) object;
                    LocalDate constraintDate = startConstraint
                            .getConstraintDate();
                    boolean bothNotNull = value != null
                                                && constraintDate != null;
                    return value == constraintDate || bothNotNull
                            && constraintDate.equals(value);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("the start constraint must have date "
                        + value);
            }
        };
    }

}
