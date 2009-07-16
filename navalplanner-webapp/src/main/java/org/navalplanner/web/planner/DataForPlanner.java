package org.navalplanner.web.planner;

import java.util.Calendar;
import java.util.Date;

import org.zkoss.ganttz.util.DependencyBean;
import org.zkoss.ganttz.util.DependencyRegistry;
import org.zkoss.ganttz.util.DependencyType;
import org.zkoss.ganttz.util.TaskBean;
import org.zkoss.ganttz.util.TaskContainerBean;
import org.zkoss.ganttz.util.TaskLeafBean;

/**
 * Some test data for planner <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DataForPlanner {

    public DataForPlanner() {
    }

    public DependencyRegistry getEmpty() {
        return new DependencyRegistry();
    }

    public DependencyRegistry getLightLoad() {
        return getModelWith(20);
    }

    public DependencyRegistry getMediumLoad() {
        return getModelWith(300);
    }

    public DependencyRegistry getHighLoad() {
        return getModelWith(500);
    }

    private DependencyRegistry getModelWith(int tasksToCreate) {
        DependencyRegistry dependencyRegistry = new DependencyRegistry();
        Date now = new Date();
        Date end = twoMonthsLater(now);
        TaskContainerBean container = createContainer("container", now, end);
        TaskBean child1 = createTaskBean("child 1", now, end);
        container.add(child1);
        TaskContainerBean containerChild = createContainer("child 2", now, end);
        containerChild.setExpanded(true);
        container.add(containerChild);
        containerChild.add(createTaskBean("another", now, end));
        dependencyRegistry.addTopLevel(container);
        TaskBean first = null;
        TaskBean second = null;
        for (int i = 0; i < tasksToCreate - 3; i++) {
            String name = "tarefa " + (i + 1);
            TaskBean taskBean = createTaskBean(name, now, end);
            if (i == 0)
                first = taskBean;
            if (i == 1)
                second = taskBean;
            dependencyRegistry.addTopLevel(taskBean);
        }
        dependencyRegistry.add(new DependencyBean(child1, containerChild,
                DependencyType.END_START));
        dependencyRegistry.add(new DependencyBean(first, second,
                DependencyType.END_START));
        dependencyRegistry.applyAllRestrictions();
        return dependencyRegistry;
    }

    private TaskContainerBean createContainer(String name, Date start, Date end) {
        TaskContainerBean container = new TaskContainerBean();
        container.setBeginDate(start);
        container.setEndDate(end);
        container.setName(name);
        return container;
    }

    private TaskBean createTaskBean(String name, Date now, Date end) {
        TaskBean taskBean = new TaskLeafBean();
        taskBean.setName(name);
        taskBean.setBeginDate(now);
        taskBean.setEndDate(end);
        return taskBean;
    }

    private static Date twoMonthsLater(Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 2);
        return calendar.getTime();
    }
}
