package org.navalplanner.business.workorders.entities;

import java.util.ArrayList;
import java.util.List;

public class TaskWorkLeaf extends TaskWork {

    @Override
    public List<TaskWork> getChildren() {
        return new ArrayList<TaskWork>();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    public void remove(TaskWork taskWork) {

    }

    @Override
    public TaskWorkContainer asContainer() {
        TaskWorkContainer result = new TaskWorkContainer();
        result.setName(getName());
        result.setInitDate(getInitDate());
        result.setEndDate(getEndDate());
        result.setActivities(getActivities());
        return result;
    }

    @Override
    public void replace(TaskWork old, TaskWork newTask) {
        throw new UnsupportedOperationException();
    }
}
