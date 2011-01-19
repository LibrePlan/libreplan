/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

import org.zkoss.ganttz.data.GanttDiagramGraph.IAdapter;
import org.zkoss.ganttz.data.constraint.Constraint;

/**
 * This enum tells the type of a depepdency. Each instance contanins the correct
 * behaviour for that type of dependency . <br/>
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public enum DependencyType {

    VOID(Point.VOID, Point.VOID) {

        @Override
        public DependencyType inverse() {
            return VOID;
        }
    },

    END_START(Point.END, Point.START) {

        @Override
        public DependencyType inverse() {
            return START_END;
        }
    },

    START_END(Point.START, Point.END) {

        @Override
        public DependencyType inverse() {
            return END_START;
        }
    },

    START_START(Point.START, Point.START) {

        @Override
        public DependencyType inverse() {
            return START_START;
        }
    },

    END_END(Point.END, Point.END) {

        @Override
        public DependencyType inverse() {
            return END_END;
        }
    };

    public enum Point {
        VOID, START, END;
    }

    private final Point source;

    private final Point destination;

    private DependencyType(Point source, Point destination) {
        this.source = source;
        this.destination = destination;
    }

    protected <V> Constraint<GanttDate> biggerThanTaskEndDate(
            IAdapter<V, ?> adapter, V source) {
        return biggerOrEqualThan(adapter.getEndDateFor(source));
    }

    protected <V> Constraint<GanttDate> biggerThanTaskStartDate(
            IAdapter<V, ?> adapter, V source) {
        return biggerOrEqualThan(adapter.getStartDate(source));
    }

    public Point[] getSourceAndDestination() {
        return new Point[] { getSource(), getDestination() };
    }

    public Point getSource() {
        return this.source;
    }

    public Point getDestination() {
        return this.destination;
    }

    public abstract DependencyType inverse();
}
