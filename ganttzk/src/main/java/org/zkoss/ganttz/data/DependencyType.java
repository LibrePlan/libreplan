/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

import static org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues.biggerOrEqualThan;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.zkoss.ganttz.data.GanttDiagramGraph.IAdapter;
import org.zkoss.ganttz.data.GanttDiagramGraph.PointType;
import org.zkoss.ganttz.data.constraint.Constraint;

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

        public <V> List<Constraint<GanttDate>> getStartConstraints(V source,
                IAdapter<V, ?> adapter) {
            return Collections.emptyList();
        }

        public <V> List<Constraint<GanttDate>> getEndConstraints(V source,
                IAdapter<V, ?> adapter) {
            return Collections.emptyList();
        }

        @Override
        public PointType getPointModified() {
            return PointType.NONE;
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
            return getBigger(originalTask.getEndDate().toDayRoundedDate(),
                    current);
        }

        public <V> List<Constraint<GanttDate>> getStartConstraints(V source,
                IAdapter<V, ?> adapter) {
            return Collections.singletonList(biggerThanTaskEndDate(adapter,
                    source));
        }

        public <V> List<Constraint<GanttDate>> getEndConstraints(V source,
                GanttDiagramGraph.IAdapter<V, ?> adapter) {
            return Collections.emptyList();
        }

        @Override
        public PointType getPointModified() {
            return PointType.BOTH;
        }

    },
    START_START {

        @Override
        public Date calculateEndDestinyTask(Task originTask, Date current) {
            return current;
        }

        @Override
        public Date calculateStartDestinyTask(Task originTask, Date current) {
            return getBigger(originTask.getBeginDate().toDayRoundedDate(),
                    current);
        }

        public <V> List<Constraint<GanttDate>> getStartConstraints(V source,
                GanttDiagramGraph.IAdapter<V, ?> adapter) {
            return Collections.singletonList(biggerThanTaskStartDate(adapter,
                    source));
        }

        public <V> List<Constraint<GanttDate>> getEndConstraints(V source,
                GanttDiagramGraph.IAdapter<V, ?> adapter) {
            return Collections.emptyList();
        }

        @Override
        public PointType getPointModified() {
            return PointType.BOTH;
        }
    },
    END_END {

        @Override
        public Date calculateEndDestinyTask(Task originTask, Date current) {
            return getBigger(originTask.getEndDate().toDayRoundedDate(),
                    current);
        }

        @Override
        public Date calculateStartDestinyTask(Task originTask, Date current) {
            return current;
        }

        @Override
        public PointType getPointModified() {
            return PointType.END;
        }

        @Override
        public <V> List<Constraint<GanttDate>> getEndConstraints(V source,
                IAdapter<V, ?> adapter) {
            return Collections.singletonList(biggerThanTaskEndDate(adapter,
                    source));
        }

        @Override
        public <V> List<Constraint<GanttDate>> getStartConstraints(V source,
                IAdapter<V, ?> adapter) {
            return Collections.emptyList();
        }
    };

    protected <V> Constraint<GanttDate> biggerThanTaskEndDate(
            IAdapter<V, ?> adapter, V source) {
        return biggerOrEqualThan(adapter.getEndDateFor(source));
    }

    protected <V> Constraint<GanttDate> biggerThanTaskStartDate(
            IAdapter<V, ?> adapter, V source) {
        return biggerOrEqualThan(adapter.getStartDate(source));
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

    public final List<Constraint<GanttDate>> getStartConstraints(Task source) {
        return getStartConstraints(source, GanttDiagramGraph.taskAdapter());
    }

    public abstract <V> List<Constraint<GanttDate>> getStartConstraints(
            V source, IAdapter<V, ?> adapter);

    public final List<Constraint<GanttDate>> getEndConstraints(Task source) {
        return getEndConstraints(source, GanttDiagramGraph.taskAdapter());
    }

    public abstract <V> List<Constraint<GanttDate>> getEndConstraints(V source,
            IAdapter<V, ?> adapter);

    public abstract PointType getPointModified();
}
