package org.navalplanner.business.test.workorders.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.navalplanner.business.workorders.entities.ProjectWork;
import org.navalplanner.business.workorders.entities.TaskWorkContainer;
import org.navalplanner.business.workorders.entities.TaskWorkLeaf;

/**
 * Tests for {@link ProjectWork}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ProjectWorkTest {

    @Test
    public void testAddingTaskWork() throws Exception {
        ProjectWork projectWork = new ProjectWork();
        TaskWorkContainer container = new TaskWorkContainer();
        TaskWorkLeaf leaf = new TaskWorkLeaf();
        container.addTask(leaf);
        projectWork.add(container);
        assertThat(projectWork.getTaskWorks().size(), equalTo(1));
    }
}
