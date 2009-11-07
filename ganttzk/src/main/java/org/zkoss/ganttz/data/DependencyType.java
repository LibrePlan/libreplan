/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.zkoss.ganttz.data;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.zkoss.ganttz.data.constraint.Constraint;
import org.zkoss.ganttz.data.constraint.DateConstraint;

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

        @Override
        public List<Constraint<Date>> getEndConstraints(Task source) {
            return Collections.emptyList();
        }

        @Override
        public List<Constraint<Date>> getStartConstraints(Task source) {
            return Collections.emptyList();
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

        @Override
        public List<Constraint<Date>> getStartConstraints(Task source) {
            return Collections.singletonList(biggerThanTaskEndDate(source));
        }

        @Override
        public List<Constraint<Date>> getEndConstraints(Task source) {
            return Collections.emptyList();
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

        @Override
        public List<Constraint<Date>> getEndConstraints(Task source) {
            return Collections.emptyList();
        }

        @Override
        public List<Constraint<Date>> getStartConstraints(Task source) {
            return Collections.singletonList(biggerThanTaskStartDate(source));
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

        @Override
        public List<Constraint<Date>> getEndConstraints(Task source) {
            return Collections.singletonList(biggerThanTaskEndDate(source));
        }

        @Override
        public List<Constraint<Date>> getStartConstraints(Task source) {
            return Collections.emptyList();
        }
    };

    protected Constraint<Date> biggerThanTaskEndDate(Task source) {
        return DateConstraint.biggerOrEqualThan(source.getEndDate());
    }

    protected Constraint<Date> biggerThanTaskStartDate(Task source) {
        return DateConstraint
                .biggerOrEqualThan(source.getBeginDate());
    }

    private static Date getBigger(Date date1, Date date2) {
        if (date1.before(date2)) {
            return date2;
        }
        return date1;
    }

    public abstract Date calculateEndDestinyTask(Task originTask,
            Date current);

    public abstract Date calculateStartDestinyTask(Task originTask,
            Date current);

    public abstract List<Constraint<Date>> getStartConstraints(Task source);

    public abstract List<Constraint<Date>> getEndConstraints(Task source);
}
