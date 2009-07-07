package org.navalplanner.business.test.planner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TaskTest {

    private Task task;
    private HoursGroup hoursGroup;

    public TaskTest() {
        hoursGroup = new HoursGroup();
        hoursGroup.setWorkingHours(3);
        task = Task.createTask(hoursGroup);
    }

    @Test
    public void taskIsASubclassOfTaskElement() {
        assertTrue(task instanceof TaskElement);
    }

    @Test
    public void taskHasNumberOfHours() {
        assertThat(task.getHours(), equalTo(hoursGroup.getWorkingHours()));
    }

    @Test
    public void taskMustHaveOneHoursGroup() {
        HoursGroup hoursGroup = task.getHoursGroup();
        assertNotNull(hoursGroup);
    }

    public static TaskElement createValidTask() {
        HoursGroup hours = new HoursGroup();
        hours.setWorkingHours(20);
        return Task.createTask(hours);
    }

}
