package org.navalplanner.business.workorders.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskWorkContainer extends TaskWork {

    private Set<TaskWork> children = new HashSet<TaskWork>();

    public List<TaskWork> getChildren() {
        return new ArrayList<TaskWork>(children);
    }

    public void addTask(TaskWorkLeaf leaf) {
        children.add(leaf);
    }

}
