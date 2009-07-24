package org.navalplanner.business.test.planner.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.Dependency.Type;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TaskElementTest {

    private TaskElement task = new Task();

    private TaskElement taskWithOrderLine;

    private Dependency exampleDependency;

    public TaskElementTest() {
        this.taskWithOrderLine = new Task();
        this.taskWithOrderLine.setOrderElement(new OrderLine());
        this.exampleDependency = Dependency.createDependency(new Task(),
                new Task(), Type.END_START);
    }

    @Test
    public void taskElementHasAOneToOneRelationshipWithOrderElement() {
        OrderLine order = new OrderLine();
        task.setOrderElement(order);
        assertSame(order, task.getOrderElement());
    }

    @Test(expected = IllegalArgumentException.class)
    public void orderElementCannotBeSetToNull() {
        task.setOrderElement(null);
    }

    @Test(expected = IllegalStateException.class)
    public void onceSetOrderElementCannotBeChanged() {
        taskWithOrderLine.setOrderElement(new OrderLine());
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
        Dependency dependency = Dependency.createDependency(origin,
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
        OrderLine orderLine = new OrderLine();
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
        //TODO specify which will be the value for the end date
    }

    private void checkPopertiesAreKept(Task taskBeingSplitted,
            TaskElement oneOfTheResult) {
        assertThat(oneOfTheResult.getName(), equalTo(taskBeingSplitted
                .getName()));
        assertThat(oneOfTheResult.getNotes(), equalTo(taskBeingSplitted
                .getNotes()));
        assertThat(oneOfTheResult.getStartDate(), equalTo(taskBeingSplitted
                .getStartDate()));
        assertThat(oneOfTheResult.getOrderElement(), equalTo(taskBeingSplitted
                .getOrderElement()));
    }

    @Test
    public void splittingATaskIntoSeveralKeepsDependencies() {
        HoursGroup hoursGroup = new HoursGroup();
        TaskGroup root = new TaskGroup();
        Task taskBeingSplitted = Task.createTask(hoursGroup);
        root.addTaskElement(taskBeingSplitted);
        Task sourceDependencyTask = Task.createTask(new HoursGroup());
        Task destinationDependencyTask = Task.createTask(new HoursGroup());
        taskBeingSplitted.setName("prueba");
        taskBeingSplitted.setNotes("blabla");
        taskBeingSplitted.setStartDate(new Date());
        Dependency.createDependency(sourceDependencyTask, taskBeingSplitted,
                Type.END_START);
        Dependency.createDependency(taskBeingSplitted,
                destinationDependencyTask, Type.END_START);
        OrderLine orderLine = new OrderLine();
        hoursGroup.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);
        taskBeingSplitted.setOrderElement(orderLine);

        int[] shares = { 50, 50 };
        TaskGroup taskResultOfSplit = taskBeingSplitted.split(shares);
        assertThat(taskResultOfSplit.getParent(), equalTo(root));
        assertThat(taskResultOfSplit.getDependenciesWithThisDestination()
                .size(), equalTo(1));
        Dependency withTaskResultOfSplitDestination = taskResultOfSplit
                .getDependenciesWithThisDestination().iterator().next();
        assertThat(withTaskResultOfSplitDestination.getDestination(),
                equalTo((TaskElement) taskResultOfSplit));
        assertThat(withTaskResultOfSplitDestination.getOrigin(),
                equalTo((TaskElement) sourceDependencyTask));

        assertThat(taskResultOfSplit.getDependenciesWithThisOrigin().size(),
                equalTo(1));
        Dependency withTaskResultOfSplitSource = taskResultOfSplit
                .getDependenciesWithThisOrigin().iterator().next();
        assertThat(withTaskResultOfSplitSource.getDestination(),
                equalTo((TaskElement) destinationDependencyTask));
        assertThat(withTaskResultOfSplitSource.getOrigin(),
                equalTo((TaskElement) taskResultOfSplit));
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
    public void detachRemovesDependenciesFromRelatedTasks() {
        HoursGroup hoursGroup = new HoursGroup();
        Task taskToDetach = Task.createTask(hoursGroup);
        Task sourceDependencyTask = Task.createTask(new HoursGroup());
        Task destinationDependencyTask = Task.createTask(new HoursGroup());
        taskToDetach.setName("prueba");
        taskToDetach.setNotes("blabla");
        taskToDetach.setStartDate(new Date());
        Dependency.createDependency(sourceDependencyTask, taskToDetach,
                Type.END_START);
        Dependency.createDependency(taskToDetach,
                destinationDependencyTask, Type.END_START);
        taskToDetach.detach();
        assertThat(sourceDependencyTask.getDependenciesWithThisOrigin().size(),
                equalTo(0));
        assertThat(destinationDependencyTask
                .getDependenciesWithThisDestination().size(), equalTo(0));
    }

    @Test
    public void detachRemovesTaskFromParent() {
        TaskGroup parent = new TaskGroup();
        HoursGroup hoursGroup = new HoursGroup();
        Task child = Task.createTask(hoursGroup);
        Task anotherChild = Task.createTask(hoursGroup);
        parent.addTaskElement(child);
        parent.addTaskElement(anotherChild);
        child.detach();
        assertThat(parent.getChildren().size(), equalTo(1));
    }
}
