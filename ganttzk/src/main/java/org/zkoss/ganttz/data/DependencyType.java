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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues.biggerOrEqualThan;

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

    VOID(Point.VOID, Point.VOID),

    END_START(Point.END, Point.START),

    START_END(Point.START, Point.END),

    START_START(Point.START, Point.START),

    END_END(Point.END, Point.END);

    private enum Point {
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

    public final List<Constraint<GanttDate>> getStartConstraints(Task source) {
        return getStartConstraints(source, GanttDiagramGraph.taskAdapter());
    }

    public final <V> List<Constraint<GanttDate>> getStartConstraints(V source,
            IAdapter<V, ?> adapter) {
        if (getDestination() != Point.START) {
            return emptyList();
        }
        return constraintsFromReferenceDate(getReferenceDate(source, adapter));
    }

    public final List<Constraint<GanttDate>> getEndConstraints(Task source) {
        return getEndConstraints(source, GanttDiagramGraph.taskAdapter());
    }

    public final <V> List<Constraint<GanttDate>> getEndConstraints(V source,
            IAdapter<V, ?> adapter) {
        if (getDestination() != Point.END) {
            return emptyList();
        }
        return constraintsFromReferenceDate(getReferenceDate(source, adapter));
    }

    private <V> GanttDate getReferenceDate(V source, IAdapter<V, ?> adapter) {
        if (getSource() == Point.START) {
            return adapter.getStartDate(source);
        } else {
            return adapter.getEndDateFor(source);
        }
    }

    private List<Constraint<GanttDate>> constraintsFromReferenceDate(
            GanttDate referenceDate) {
        return singletonList(biggerOrEqualThan(referenceDate));
    }

    public final PointType getPointModified() {
        Point destination = getDestination();
        switch (destination) {
        case VOID:
            return PointType.NONE;
        case START:
            return PointType.BOTH;
        case END:
            return PointType.END;
        default:
            throw new RuntimeException("couldn't handle " + destination);
        }
    }

    private final Point getSource() {
        return this.source;
    }

    private final Point getDestination() {
        return this.destination;
    }
}
