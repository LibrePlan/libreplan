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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Date;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderLine;
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

    private void checkPopertiesAreKept(TaskElement original, TaskElement result) {
        assertThat(result.getName(), equalTo(original.getName()));
        assertThat(result.getNotes(), equalTo(original.getNotes()));
        assertThat(result.getStartDate(), equalTo(original.getStartDate()));
        assertThat(result.getOrderElement(),
                equalTo(original.getOrderElement()));
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
