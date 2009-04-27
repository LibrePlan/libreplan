package org.zkoss.ganttz;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;

public class ListDetails extends HtmlMacroComponent {

    private static Log LOG = LogFactory.getLog(ListDetails.class);

    public ListDetails() {
        LOG.info("constructing list details");
    }

    Planner getPlanner() {
        return (Planner) getParent();
    }

    public void addTask() {
        TaskDetail taskDetail = new TaskDetail();
        String newId = UUID.randomUUID().toString();
        taskDetail.setTaskId(newId);
        taskDetail.setDynamicProperty("start", TaskDetail.format(new Date()));
        taskDetail.setDynamicProperty("length", "30 days");
        taskDetail.setDynamicProperty("taskName", Labels
                .getLabel("task.new_task_name"));
        Component insertionPoint = getFellow("insertionPoint");
        taskDetail.setParent(insertionPoint);
        taskDetail.afterCompose();
        Task task = new Task();
        getPlanner().publishTask(task);
        task.setColor("yellow");
        task.setId(newId);
    }

}
