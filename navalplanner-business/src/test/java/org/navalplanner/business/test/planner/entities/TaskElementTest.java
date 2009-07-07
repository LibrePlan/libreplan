package org.navalplanner.business.test.planner.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
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
}
