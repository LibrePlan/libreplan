package org.navalplanner.business.workorders.entities;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link TaskWork}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TaskWorkListManipulator implements ITaskWorkContainer {

    private final List<TaskWork> taskWorks;

    public TaskWorkListManipulator(List<TaskWork> taskWorks) {
        this.taskWorks = taskWorks;

    }

    @Override
    public void add(TaskWork task) {
        taskWorks.add(task);
    }

    @Override
    public void remove(TaskWork task) {
        taskWorks.remove(task);
    }

    @Override
    public void replace(TaskWork oldTask, TaskWork newTask) {
        Collections.replaceAll(taskWorks, oldTask, newTask);
    }

    @Override
    public void up(TaskWork task) {
        int position = taskWorks.indexOf(task);
        if (position < taskWorks.size() - 1) {
            taskWorks.remove(position);
            taskWorks.add(position + 1, task);
        }
    }

    @Override
    public void down(TaskWork task) {
        int position = taskWorks.indexOf(task);
        if (position > 0) {
            taskWorks.remove(position);
            taskWorks.add(position - 1, task);
        }
    }

    @Override
    public void add(int position, TaskWork task) {
        taskWorks.add(position, task);
    }

}
