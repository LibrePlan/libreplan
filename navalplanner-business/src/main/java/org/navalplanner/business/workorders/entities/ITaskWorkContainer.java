package org.navalplanner.business.workorders.entities;

/**
 * Container of TaskWorks. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ITaskWorkContainer {

    public void add(TaskWork task);

    public void remove(TaskWork task);

    public void replace(TaskWork oldTask, TaskWork newTask);

    public void up(TaskWork task);

    public void down(TaskWork task);

    public void add(int position, TaskWork task);

}