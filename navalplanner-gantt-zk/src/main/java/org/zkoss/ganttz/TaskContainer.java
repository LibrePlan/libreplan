package org.zkoss.ganttz;

import org.zkoss.ganttz.util.TaskContainerBean;
import org.zkoss.zk.ui.ext.AfterCompose;

/**
 * This class contains the information of a task container. It can be modified and
 * notifies of the changes to the interested parties. <br/>
 * Created at Jul 1, 2009
 *
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 *
 */
public class TaskContainer extends Task implements AfterCompose {

    public static TaskContainer asTask(TaskContainerBean taskContainerBean) {
        return new TaskContainer(taskContainerBean);
    }

    public TaskContainer(TaskContainerBean taskContainerBean) {
        super(taskContainerBean);
    }
}