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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TaskGroupTest {
    private TaskGroup taskGroup = TaskGroup.create();

    @Test
    public void taskGroupIsAnInstanceOfTaskElement() {
        assertTrue(taskGroup instanceof TaskElement);
    }

    @Test
    public void taskGroupHasManyTaskElements() {
        List<TaskElement> tasks = taskGroup.getChildren();
        assertTrue("a task group has no task elements initially", tasks
                .isEmpty());
        TaskElement child1 = new Task();
        taskGroup.addTaskElement(child1);
        TaskGroup child2 = TaskGroup.create();
        taskGroup.addTaskElement(child2);
        List<TaskElement> taskElements = taskGroup.getChildren();
        assertThat(taskElements.size(), equalTo(2));
        assertThat(taskGroup.getChildren(), equalTo(Arrays.asList(child1,
                child2)));
    }

    @Test
    public void addingTaskElementToTaskGroupSetsTheParentProperty() {
        Task child = Task.createTask(new HoursGroup());
        taskGroup.addTaskElement(child);
        assertThat(child.getParent(), equalTo(taskGroup));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantAddNullTaskElement() {
        taskGroup.addTaskElement(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void taskElementsCollectionCannotBeModified() {
        taskGroup.getChildren().set(0, null);
    }
}
