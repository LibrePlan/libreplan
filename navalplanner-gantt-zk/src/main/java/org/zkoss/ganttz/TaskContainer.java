package org.zkoss.ganttz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.zkoss.ganttz.util.TaskBean;
import org.zkoss.ganttz.util.TaskContainerBean;
import org.zkoss.ganttz.util.TaskContainerBean.IExpandListener;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.ext.AfterCompose;

/**
 * This class contains the information of a task container. It can be modified
 * and notifies of the changes to the interested parties. <br/>
 * Created at Jul 1, 2009
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class TaskContainer extends Task implements AfterCompose {

    public static TaskContainer asTask(TaskContainerBean taskContainerBean,
            TaskList taskList) {
        return new TaskContainer(taskContainerBean, taskList);
    }

    private List<Task> subtasks = new ArrayList<Task>();
    final TaskList taskList;

    private IExpandListener expandListener;

    public TaskContainer(TaskContainerBean taskContainerBean, TaskList taskList) {
        super(taskContainerBean);
        this.expandListener = new IExpandListener() {

            @Override
            public void expandStateChanged(boolean isNowExpanded) {
                if (isNowExpanded) {
                    open();
                } else {
                    close();
                }
            }
        };
        taskContainerBean.addExpandListener(expandListener);
        this.taskList = taskList;
        for (TaskBean taskBean : taskContainerBean.getTasks()) {
            subtasks.add(Task.asTask(taskBean, taskList));
        }
    }

    @Override
    protected void publishDescendants(Map<TaskBean, Task> resultAccumulated) {
        for (Task task : subtasks) {
            task.publishTasks(resultAccumulated);
        }
    }

    public void open() {
        Component previous = this;
        for (Task subtask : subtasks) {
            taskList.addTask(previous, subtask, true);
            previous = subtask;
        }
    }

    public boolean isExpanded() {
        return getTaskContainerBean().isExpanded();
    }

    private TaskContainerBean getTaskContainerBean() {
        return (TaskContainerBean) getTaskBean();
    }

    private void close() {
        for (Task subtask : subtasks) {
            if (subtask instanceof TaskContainer) {
                TaskContainer container = (TaskContainer) subtask;
                container.close();
            }
            taskList.hideTask(subtask);
            taskList.redrawDependencies();
        }
    }
}