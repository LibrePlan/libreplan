package org.zkoss.ganttz;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;

public class ListDetails extends HtmlMacroComponent {

    private static Log LOG = LogFactory.getLog(ListDetails.class);
    private TaskRemovedListener taskRemovedListener;

    public ListDetails() {
        LOG.info("constructing list details");
    }

    Planner getPlanner() {
        return (Planner) getParent();
    }

    private List<TaskDetail> getTaskDetails() {
        List<Object> children = getInsertionPoint().getChildren();
        return Planner.findComponentsOfType(TaskDetail.class, children);
    }

    public void taskRemoved(Task taskRemoved) {
        List<TaskDetail> taskDetails = getTaskDetails();
        for (TaskDetail taskDetail : taskDetails) {
            if (taskDetail.getTaskId().equals(taskRemoved.getId())) {
                removeDetail(taskDetail);
                return;
            }
        }
        throw new RuntimeException("not found taskDetail for " + taskRemoved);
    }

    private void removeDetail(TaskDetail taskDetail) {
        getInsertionPoint().getChildren().remove(taskDetail);
    }

    public void addTask() {
        TaskDetail taskDetail = new TaskDetail();
        String newId = UUID.randomUUID().toString();
        taskDetail.setTaskId(newId);
        taskDetail.setDynamicProperty("start", TaskDetail.format(new Date()));
        taskDetail.setDynamicProperty("length", "30 days");
        taskDetail.setDynamicProperty("taskName", Labels
                .getLabel("task.new_task_name"));
        Component insertionPoint = getInsertionPoint();
        taskDetail.setParent(insertionPoint);
        taskDetail.afterCompose();
        Task task = new Task();
        getPlanner().addTask(task);
        task.setColor("#007bbe");
        task.setId(newId);
    }

    private Component getInsertionPoint() {
        return getFellow("insertionPoint");
    }

}
