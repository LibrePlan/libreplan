package org.navalplanner.business.workorders.entities;

import java.util.ArrayList;
import java.util.List;

public class TaskWorkContainer extends TaskWork implements ITaskWorkContainer {

    private List<TaskWork> children = new ArrayList<TaskWork>();

    @Override
    public List<TaskWork> getChildren() {
        return new ArrayList<TaskWork>(children);
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public void remove(TaskWork child) {
        getManipulator().remove(child);
    }

    @Override
    public void replace(TaskWork oldTask, TaskWork newTask) {
        getManipulator().replace(oldTask, newTask);
    }

    @Override
    public void add(TaskWork task) {
        getManipulator().add(task);
    }

    @Override
    public void up(TaskWork task) {
        getManipulator().up(task);
    }

    private TaskWorkListManipulator getManipulator() {
        return new TaskWorkListManipulator(children);
    }

    @Override
    public TaskWorkContainer asContainer() {
        return this;
    }

    @Override
    public void down(TaskWork task) {
        getManipulator().down(task);
    }

    @Override
    public void add(int position, TaskWork task) {
        children.add(position, task);
    }

    @Override
    public Integer getWorkHours() {
        int result = 0;
        List<TaskWork> children = getChildren();
        for (TaskWork taskWork : children) {
            result += taskWork.getWorkHours();
        }
        return result;
    }

}
