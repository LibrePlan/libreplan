package org.zkoss.ganttz.util;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the information of a task container. It can be modified
 * and notifies of the changes to the interested parties. <br/>
 * Created at Jul 1, 2009
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class TaskContainerBean extends TaskBean {

    private List<TaskBean> tasks = new ArrayList<TaskBean>();

    public void add(TaskBean task) {
        tasks.add(task);
    }

    public List<TaskBean> getTasks() {
        return tasks;
    }

}