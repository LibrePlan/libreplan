package org.zkoss.ganttz.util;

import java.util.Date;

/**
 * This enum tells the type of a depepdency. Each instance contanins the correct
 * behaviour for that type of dependency . <br/>
 * Created at Apr 24, 2009
 * 
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * 
 */
public enum DependencyType {

    VOID {
        @Override
        public Date calculateEndDestinyTask(TaskBean originalTask, Date current) {
            return current;
        }

        @Override
        public Date calculateStartDestinyTask(TaskBean originalTask,
                Date current) {
            return current;
        }
    },
    END_START {
        @Override
        public Date calculateEndDestinyTask(TaskBean originalTask, Date current) {
            return current;
        }

        @Override
        public Date calculateStartDestinyTask(TaskBean originalTask,
                Date current) {
            return getBigger(originalTask.getEndDate(), current);
        }
    };

    private static Date getBigger(Date date1, Date date2) {
        if (date1.before(date2))
            return date2;
        return date1;
    }

    public abstract Date calculateEndDestinyTask(TaskBean originTask,
            Date current);

    public abstract Date calculateStartDestinyTask(TaskBean originTask,
            Date current);
}
