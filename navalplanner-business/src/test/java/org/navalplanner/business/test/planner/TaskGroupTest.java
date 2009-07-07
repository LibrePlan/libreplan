package org.navalplanner.business.test.planner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TaskGroupTest {
    private TaskGroup taskGroup = new TaskGroup();

    @Test
    public void taskGroupIsAnInstanceOfTaskElement() {
        assertTrue(taskGroup instanceof TaskElement);
    }

    @Test
    public void taskGroupHasManyTaskElements() {
        List<TaskElement> tasks = taskGroup.getTaskElements();
        assertTrue("a task group has no task elements initially", tasks
                .isEmpty());
        TaskElement child1 = new Task();
        taskGroup.addTaskElement(child1);
        TaskGroup child2 = new TaskGroup();
        taskGroup.addTaskElement(child2);
        List<TaskElement> taskElements = taskGroup.getTaskElements();
        assertThat(taskElements.size(), equalTo(2));
        assertThat(taskGroup.getTaskElements(), equalTo(Arrays.asList(child1,
                child2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantAddNullTaskElement() {
        taskGroup.addTaskElement(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void taskElementsCollectionCannotBeModified() {
        taskGroup.getTaskElements().set(0, null);
    }
}
