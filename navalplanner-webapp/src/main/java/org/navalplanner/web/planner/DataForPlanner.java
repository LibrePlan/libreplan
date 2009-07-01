package org.navalplanner.web.planner;

import java.util.Calendar;
import java.util.Date;

import org.zkoss.ganttz.util.DependencyBean;
import org.zkoss.ganttz.util.DependencyRegistry;
import org.zkoss.ganttz.util.DependencyType;
import org.zkoss.ganttz.util.TaskBean;

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
        TaskBean first = null;
        TaskBean second = null;
        for (int i = 0; i < tasksToCreate; i++) {
            TaskBean taskBean = new TaskBean();
            if (i == 0)
                first = taskBean;
            if (i == 1)
                second = taskBean;
            taskBean.setName("tarefa " + (i + 1));
            taskBean.setBeginDate(now);
            taskBean.setEndDate(end);
            dependencyRegistry.add(taskBean);
        }
        dependencyRegistry.add(new DependencyBean(first, second,
                DependencyType.END_START));
        return dependencyRegistry;
    }

    private static Date twoMonthsLater(Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 2);
        return calendar.getTime();
    }
}
