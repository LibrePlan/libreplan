package org.navalplanner.business.planner.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TaskGroup extends TaskElement {

    private List<TaskElement> taskElements = new ArrayList<TaskElement>();

    public void addTaskElement(TaskElement task) {
        Validate.notNull(task);
        task.setParent(this);
        taskElements.add(task);
    }

    @Override
    public List<TaskElement> getChildren() {
        return Collections.unmodifiableList(taskElements);
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public Integer getWorkHours() {
        return getOrderElement().getWorkHours();
    }

    public void remove(TaskElement taskElement) {
        taskElements.remove(taskElement);
    }

}
