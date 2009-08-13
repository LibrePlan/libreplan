package org.zkoss.ganttz.data;

import java.util.Date;

/**
 * This enum tells the type of a depepdency. Each instance contanins the correct
 * behaviour for that type of dependency . <br/>
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public enum DependencyType {

    VOID {
        @Override
        public Date calculateEndDestinyTask(Task originalTask, Date current) {
            return current;
        }

        @Override
        public Date calculateStartDestinyTask(Task originalTask,
                Date current) {
            return current;
        }
    },
    END_START {
        @Override
        public Date calculateEndDestinyTask(Task originalTask, Date current) {
            return current;
        }

        @Override
        public Date calculateStartDestinyTask(Task originalTask,
                Date current) {
            return getBigger(originalTask.getEndDate(), current);
        }
    },
    START_START {

        @Override
        public Date calculateEndDestinyTask(Task originTask, Date current) {
            return current;
        }

        @Override
        public Date calculateStartDestinyTask(Task originTask, Date current) {
            return getBigger(originTask.getBeginDate(), current);
        }
    },
    END_END {

        @Override
        public Date calculateEndDestinyTask(Task originTask, Date current) {
            return getBigger(originTask.getEndDate(), current);
        }

        @Override
        public Date calculateStartDestinyTask(Task originTask, Date current) {
            return current;
        }
    };

    private static Date getBigger(Date date1, Date date2) {
        if (date1.before(date2))
            return date2;
        return date1;
    }

    public abstract Date calculateEndDestinyTask(Task originTask,
            Date current);

    public abstract Date calculateStartDestinyTask(Task originTask,
            Date current);
}
