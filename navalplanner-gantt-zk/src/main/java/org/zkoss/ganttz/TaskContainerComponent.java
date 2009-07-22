package org.zkoss.ganttz;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.data.TaskContainer;
import org.zkoss.ganttz.data.TaskContainer.IExpandListener;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.ext.AfterCompose;

/**
 * This class contains the information of a task container. It can be modified
 * and notifies of the changes to the interested parties. <br/>
 * Created at Jul 1, 2009
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class TaskContainerComponent extends TaskComponent implements AfterCompose {

    public static TaskContainerComponent asTask(Task taskContainerBean,
            TaskList taskList) {
        return new TaskContainerComponent((TaskContainer) taskContainerBean,
                taskList);
    }

    private List<TaskComponent> subtaskComponents = new ArrayList<TaskComponent>();
    private final TaskList taskList;

    private IExpandListener expandListener;

    public TaskContainerComponent(TaskContainer taskContainer, TaskList taskList) {
        super(taskContainer);
        if (!taskContainer.isContainer())
            throw new IllegalArgumentException();
        this.expandListener = new IExpandListener() {

            @Override
            public void expandStateChanged(boolean isNowExpanded) {
                if (isNowExpanded) {
                    open();
                } else {
                    close();
                }
                updateClass();
            }
        };
        taskContainer.addExpandListener(expandListener);
        this.taskList = taskList;
        for (Task task : taskContainer.getTasks()) {
            getCurrentComponents().add(TaskComponent.asTaskComponent(task, taskList));
        }
    }

    @Override
    protected void publishDescendants(Map<Task, TaskComponent> resultAccumulated) {
        for (TaskComponent taskComponent : getCurrentComponents()) {
            taskComponent.publishTaskComponents(resultAccumulated);
        }
    }

    @Override
    protected void remove() {
        if (isExpanded()) {
            for (TaskComponent subtaskComponent : getCurrentComponents()) {
                subtaskComponent.remove();
            }
        }
        super.remove();
    }

    public void open() {
        Component previous = this;
        for (TaskComponent subtaskComponent : getCurrentComponents()) {
            taskList.addTaskComponent(previous, subtaskComponent, true);
            previous = subtaskComponent;
        }
    }

    private List<TaskComponent> getCurrentComponents() {
        ListIterator<TaskComponent> listIterator = subtaskComponents
                .listIterator();
        // one of the subtask components created previously could have been
        // removed, so we only return the valid ones
        while (listIterator.hasNext()) {
            if (!getTaskContainer().contains(listIterator.next().getTask())) {
                listIterator.remove();
            }
        }
        return subtaskComponents;
    }

    public boolean isExpanded() {
        return getTaskContainer().isExpanded();
    }

    private TaskContainer getTaskContainer() {
        return (TaskContainer) getTask();
    }

    @Override
    protected String calculateClass() {
        return super.calculateClass() +" "+ (getTaskContainer().isExpanded()?
                "expanded":"closed");
    }



    private void close() {
        for (TaskComponent subtaskComponent : getCurrentComponents()) {
            if (subtaskComponent instanceof TaskContainerComponent) {
                TaskContainerComponent container = (TaskContainerComponent) subtaskComponent;
                container.close();
            }
            taskList.hideTaskComponent(subtaskComponent);
            taskList.redrawDependencies();
        }
    }
}