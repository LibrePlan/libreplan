package org.navalplanner.business.workorders.entities;

import java.util.ArrayList;
import java.util.List;

public class TaskWorkContainer extends TaskWork {

    private List<TaskWork> children = new ArrayList<TaskWork>();

    public List<TaskWork> getChildren() {
        return new ArrayList<TaskWork>(children);
    }

    public void addTask(TaskWorkLeaf leaf) {
        children.add(leaf);
    }

}
