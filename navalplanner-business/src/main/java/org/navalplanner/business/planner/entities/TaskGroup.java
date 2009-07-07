package org.navalplanner.business.planner.entities;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TaskGroup extends TaskElement {

    private List<TaskElement> taskElements = new ArrayList<TaskElement>();

    public List<TaskElement> getTaskElements() {
        return taskElements;
    }

    public void addTaskElement(TaskElement task) {
        Validate.notNull(task);
        taskElements.add(task);
    }

}
