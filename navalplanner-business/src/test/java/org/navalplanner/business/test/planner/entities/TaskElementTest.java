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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Arrays;
import java.util.Date;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.TaskMilestone;
import org.navalplanner.business.planner.entities.Dependency.Type;
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

    private TaskElement taskWithOrderLine;

    private Dependency exampleDependency;

    public TaskElementTest() {
        this.taskWithOrderLine = new Task();
        this.taskWithOrderLine.setOrderElement(OrderLine.create());
        this.exampleDependency = Dependency.create(new Task(),
                new Task(), Type.END_START);
    }

    @Test
    public void taskElementHasAOneToOneRelationshipWithOrderElement() {
        OrderLine order = OrderLine.create();
        task.setOrderElement(order);
        assertSame(order, task.getOrderElement());
    }

    @Test(expected = IllegalArgumentException.class)
    public void orderElementCannotBeSetToNull() {
        task.setOrderElement(null);
    }

    @Test(expected = IllegalStateException.class)
    public void onceSetOrderElementCannotBeChanged() {
        taskWithOrderLine.setOrderElement(OrderLine.create());
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
    public void taskElementHasStartDateProperty() {
        Date now = new Date();
        task.setStartDate(now);
        assertThat(task.getStartDate(), equalTo(now));
        task.setEndDate(now);
        assertThat(task.getEndDate(), equalTo(now));
    }

    @Test
    public void aDependencyWithThisOriginCanBeRemoved() {
        Task origin = new Task();
        Task destination = new Task();
        Type type = Type.START_END;
        Dependency.create(origin,
                destination, type);
        assertThat(origin.getDependenciesWithThisOrigin().size(), equalTo(1));
        assertThat(destination.getDependenciesWithThisDestination().size(),
                equalTo(1));
        origin.removeDependencyWithDestination(destination, type);
        assertThat(origin.getDependenciesWithThisOrigin().size(), equalTo(0));
        assertThat(destination.getDependenciesWithThisDestination().size(),
                equalTo(0));
    }

    @Test
    public void splittingATaskIntoSeveral() {
        HoursGroup hoursGroup = new HoursGroup();
        Task taskBeingSplitted = Task.createTask(hoursGroup);
        taskBeingSplitted.setName("prueba");
        taskBeingSplitted.setNotes("blabla");
        taskBeingSplitted.setStartDate(new Date());
        OrderLine orderLine = OrderLine.create();
        hoursGroup.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);
        taskBeingSplitted.setOrderElement(orderLine);
        int[] shares = { 20, 30, 50 };
        TaskGroup taskGroup = taskBeingSplitted.split(shares);
        assertThat(taskGroup.getChildren().size(), equalTo(3));

        checkPopertiesAreKept(taskBeingSplitted, taskGroup);
        for (TaskElement taskElement : taskGroup.getChildren()) {
            assertThat(taskElement.getOrderElement(), equalTo(taskBeingSplitted
                    .getOrderElement()));
        }
        TaskElement first = taskGroup.getChildren().get(0);
        checkPopertiesAreKept(taskBeingSplitted, first);
        TaskElement second = taskGroup.getChildren().get(1);
        checkPopertiesAreKept(taskBeingSplitted, second);
        TaskElement third = taskGroup.getChildren().get(2);
        checkPopertiesAreKept(taskBeingSplitted, third);
        assertThat(first.getWorkHours(), equalTo(20));
        assertThat(second.getWorkHours(), equalTo(30));
        assertThat(third.getWorkHours(), equalTo(50));
    }

    @Test
    public void splittingATaskKeepsItsShareOfHoursIfPresent() {
        HoursGroup hoursGroup = new HoursGroup();
        Task initial = Task.createTask(hoursGroup);
        initial.setName("prueba");
        initial.setNotes("blabla");
        initial.setStartDate(new Date());
        OrderLine orderLine = OrderLine.create();
        hoursGroup.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);
        initial.setOrderElement(orderLine);
        int[] shares = { 50, 50 };
        TaskGroup group = initial.split(shares);
        Task t = (Task) group.getChildren().get(0);
        TaskGroup childSplittedGroup = t.split(new int[] { 25, 25 });
        assertThat("the work hours must be the same that it had",
                childSplittedGroup.getWorkHours(), equalTo(50));
    }

    private void checkPopertiesAreKept(TaskElement original, TaskElement result) {
        assertThat(result.getName(), equalTo(original.getName()));
        assertThat(result.getNotes(), equalTo(original.getNotes()));
        assertThat(result.getStartDate(), equalTo(original.getStartDate()));
        assertThat(result.getOrderElement(),
                equalTo(original.getOrderElement()));
    }

    @Test
    public void splittingATaskIntoSeveralKeepsDependencies() {
        HoursGroup hoursGroup = new HoursGroup();
        TaskGroup root = TaskGroup.create();
        Task taskBeingSplitted = Task.createTask(hoursGroup);
        root.addTaskElement(taskBeingSplitted);
        taskBeingSplitted.setName("prueba");
        taskBeingSplitted.setNotes("blabla");
        taskBeingSplitted.setStartDate(new Date());
        Task sourceDependencyTask = Task.createTask(new HoursGroup());
        Task destinationDependencyTask = Task.createTask(new HoursGroup());
        addDependenciesForChecking(taskBeingSplitted, sourceDependencyTask,
                destinationDependencyTask);
        OrderLine orderLine = OrderLine.create();
        hoursGroup.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);
        taskBeingSplitted.setOrderElement(orderLine);

        int[] shares = { 50, 50 };
        TaskGroup taskResultOfSplit = taskBeingSplitted.split(shares);
        assertThat(taskResultOfSplit.getParent(), equalTo(root));

        checkDependenciesAreKept(taskResultOfSplit, sourceDependencyTask,
                destinationDependencyTask);
    }

    private void checkDependenciesAreKept(
            TaskElement taskResultOfTransformation, Task sourceDependencyTask,
            Task destinationDependencyTask) {
        assertThat(taskResultOfTransformation
                .getDependenciesWithThisDestination().size(), equalTo(1));
        Dependency withTaskResultOfSplitDestination = taskResultOfTransformation
                .getDependenciesWithThisDestination().iterator().next();
        assertThat(withTaskResultOfSplitDestination.getDestination(),
                equalTo((TaskElement) taskResultOfTransformation));
        assertThat(withTaskResultOfSplitDestination.getOrigin(),
                equalTo((TaskElement) sourceDependencyTask));

        assertThat(taskResultOfTransformation.getDependenciesWithThisOrigin()
                .size(), equalTo(1));
        Dependency withTaskResultOfSplitSource = taskResultOfTransformation
                .getDependenciesWithThisOrigin().iterator().next();
        assertThat(withTaskResultOfSplitSource.getDestination(),
                equalTo((TaskElement) destinationDependencyTask));
        assertThat(withTaskResultOfSplitSource.getOrigin(),
                equalTo((TaskElement) taskResultOfTransformation));
    }

    private void addDependenciesForChecking(TaskElement taskBeingTransformed,
            TaskElement sourceDependencyTask,
            TaskElement destinationDependencyTask) {
        Dependency.create(sourceDependencyTask, taskBeingTransformed,
                Type.END_START);
        Dependency.create(taskBeingTransformed,
                destinationDependencyTask, Type.END_START);
    }

    @Test
    public void theSplitMustBeEqualToTheWorkingHours() {
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setWorkingHours(10);
        Task taskBeingSplitted = Task.createTask(hoursGroup);
        int[][] listOfWrongShares = { { 20, 10, 3 }, { 50, 80, 10 },
                { 90, 30, 10 }, { 10, 20 }, { 10, 110 }, { 101 }, {} };
        for (int[] shares : listOfWrongShares) {
            try {
                taskBeingSplitted.split(shares);
                fail("it should have sent an IllegalArgumentException for "
                        + Arrays.toString(shares));
            } catch (IllegalArgumentException e) {
                // Ok
            }
        }
    }

    @Test
    public void aTaskGroupThatIsAssociatedToAnOrderLineGroupCannotBeMerged() {
        TaskGroup taskGroup = TaskGroup.create();
        taskGroup.setOrderElement(OrderLineGroup.create());
        assertFalse(taskGroup.canBeMerged());
    }

    @Test
    public void aTaskGroupWithChildrenAssociatedWithDifferentHourGroups() {
        TaskGroup taskGroup = TaskGroup.create();
        taskGroup.setOrderElement(OrderLine.create());
        taskGroup.addTaskElement(Task.createTask(new HoursGroup()));
        taskGroup.addTaskElement(Task.createTask(new HoursGroup()));
        assertFalse(taskGroup.canBeMerged());
    }

    @Test
    public void aTaskGroupWithoutChildrenCannotBeMerged() {
        TaskGroup taskGroup = TaskGroup.create();
        taskGroup.setOrderElement(OrderLine.create());
        assertFalse(taskGroup.canBeMerged());
    }

    @Test
    public void aTaskGroupWithTasksThatExceedHoursCannotBeMerged() {
        TaskGroup taskGroup = TaskGroup.create();
        taskGroup.setOrderElement(OrderLine.create());
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setWorkingHours(10);
        taskGroup.addTaskElement(Task.createTask(hoursGroup));
        taskGroup.addTaskElement(Task.createTask(hoursGroup));
        assertFalse(taskGroup.canBeMerged());
    }

    @Test(expected = IllegalStateException.class)
    public void mergingATaskThatCannotBeMergedFails() {
        TaskGroup taskGroup = TaskGroup.create();
        taskGroup.setOrderElement(OrderLineGroup.create());
        taskGroup.merge();
    }

    @Test
    public void mergingATaskGroupSumsTheHoursOfTheChildren() {
        HoursGroup hoursGroup = new HoursGroup();
        Task taskBeingSplitted = Task.createTask(hoursGroup);
        taskBeingSplitted.setName("prueba");
        taskBeingSplitted.setNotes("blabla");
        taskBeingSplitted.setStartDate(new Date());
        OrderLine orderLine = OrderLine.create();
        hoursGroup.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);
        taskBeingSplitted.setOrderElement(orderLine);
        int[] shares = { 20, 30, 50 };
        TaskGroup taskGroup = taskBeingSplitted.split(shares);
        Task task = taskGroup.merge();
        checkPopertiesAreKept(taskGroup, task);
        assertThat(task.getHoursGroup(), equalTo(hoursGroup));
        assertThat(task.getOrderElement(), equalTo((OrderElement) orderLine));
        assertThat(task.getWorkHours(), equalTo(100));
    }

    @Test
    public void mergingATaskCanResultInATaskWithAShareOfHours() {
        HoursGroup hoursGroup = new HoursGroup();
        Task taskBeingSplitted = Task.createTask(hoursGroup);
        OrderLine orderLine = OrderLine.create();
        hoursGroup.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);
        taskBeingSplitted.setOrderElement(orderLine);
        int[] shares = { 20, 30, 50 };
        TaskGroup taskGroup = taskBeingSplitted.split(shares);
        Task subTask = (Task) taskGroup.getChildren().get(0);
        TaskGroup group = subTask.split(new int[] { 10, 10 });
        Task merged = group.merge();
        assertThat(merged.getWorkHours(), equalTo(20));
    }

    @Test
    public void mergingATaskKeepsDependencies() {
        HoursGroup hoursGroup = new HoursGroup();
        Task taskBeingSplitted = Task.createTask(hoursGroup);
        OrderLine orderLine = OrderLine.create();
        hoursGroup.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);
        taskBeingSplitted.setOrderElement(orderLine);
        int[] shares = { 20, 30, 50 };
        TaskGroup taskGroup = taskBeingSplitted.split(shares);
        Task source = Task.createTask(new HoursGroup());
        Task destination = Task.createTask(new HoursGroup());
        addDependenciesForChecking(taskGroup, source, destination);
        Task transformed = taskGroup.merge();
        checkDependenciesAreKept(transformed, source, destination);
    }

    @Test
    public void theMergedEntityHasTheSameParent() {
        HoursGroup hoursGroup = new HoursGroup();
        Task taskBeingSplitted = Task.createTask(hoursGroup);
        OrderLine orderLine = OrderLine.create();
        hoursGroup.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);
        taskBeingSplitted.setOrderElement(orderLine);
        int[] shares = { 20, 30, 50 };
        TaskGroup parent = taskBeingSplitted.split(shares);
        Task subTask = (Task) parent.getChildren().get(0);
        TaskGroup group = subTask.split(new int[] { 10, 10 });
        Task merged = group.merge();
        assertThat(merged.getParent(), equalTo(parent));
    }

    @Test
    public void detachRemovesDependenciesFromRelatedTasks() {
        HoursGroup hoursGroup = new HoursGroup();
        Task taskToDetach = Task.createTask(hoursGroup);
        Task sourceDependencyTask = Task.createTask(new HoursGroup());
        Task destinationDependencyTask = Task.createTask(new HoursGroup());
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
        TaskGroup parent = TaskGroup.create();
        HoursGroup hoursGroup = new HoursGroup();
        Task child = Task.createTask(hoursGroup);
        Task anotherChild = Task.createTask(hoursGroup);
        parent.addTaskElement(child);
        parent.addTaskElement(anotherChild);
        child.detach();
        assertThat(parent.getChildren().size(), equalTo(1));
    }

    @Test
    public void MilestoneOrderElementIsNull() {
        TaskMilestone milestone = new TaskMilestone();
        OrderLine orderLine = OrderLine.create();
        try {
            milestone.setOrderElement(orderLine);
        } catch (IllegalStateException e) {
            // Ok Exception expected
        } finally {
            assertTrue(milestone.getOrderElement() == null);
        }
    }
}
