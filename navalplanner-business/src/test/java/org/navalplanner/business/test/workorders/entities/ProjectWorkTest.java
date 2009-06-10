package org.navalplanner.business.test.workorders.entities;

import org.junit.Test;
import org.navalplanner.business.workorders.entities.ProjectWork;
import org.navalplanner.business.workorders.entities.TaskWork;
import org.navalplanner.business.workorders.entities.TaskWorkContainer;
import org.navalplanner.business.workorders.entities.TaskWorkLeaf;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

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
        container.add(leaf);
        projectWork.add(container);
        assertThat(projectWork.getTaskWorks().size(), equalTo(1));
    }

    @Test
    public void testPreservesOrder() throws Exception {
        TaskWorkContainer container = new TaskWorkContainer();

        TaskWorkLeaf[] created = new TaskWorkLeaf[100];
        for (int i = 0; i < created.length; i++) {
            created[i] = new TaskWorkLeaf();
            container.add(created[i]);
        }
        for (int i = 0; i < created.length; i++) {
            assertThat(container.getChildren().get(i),
                    equalTo((TaskWork) created[i]));
        }
    }
}
