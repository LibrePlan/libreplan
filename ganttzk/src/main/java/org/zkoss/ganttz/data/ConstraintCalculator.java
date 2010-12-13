/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2010 Igalia S.L.
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

import static java.util.Collections.singletonList;
import static org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues.biggerOrEqualThan;
import static org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues.lessOrEqualThan;

import java.util.Collections;
import java.util.List;

import org.zkoss.ganttz.data.DependencyType.Point;
import org.zkoss.ganttz.data.constraint.Constraint;

/**
 * Calculates the {@link GanttDate} constraints for a given {@link IDependency}
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class ConstraintCalculator<T> {

    private final boolean reversed;

    protected ConstraintCalculator(boolean reversed) {
        this.reversed = reversed;
    }

    public List<Constraint<GanttDate>> getConstraints(
            IDependency<T> dependency, Point pointToBeModified) {
        T effectiveSource = getEffectiveSource(dependency);
        DependencyType effectiveType = getEffectiveType(dependency.getType());
        if (!effectiveType.getDestination().equals(pointToBeModified)) {
            return Collections.emptyList();
        }
        return singletonList(constraintGiven(getReferenceDate(effectiveSource,
                effectiveType)));
    }

    private Constraint<GanttDate> constraintGiven(GanttDate referenceDate) {
        if (!reversed) {
            return biggerOrEqualThan(referenceDate);
        } else {
            return lessOrEqualThan(referenceDate);
        }
    }

    private GanttDate getReferenceDate(T effectiveSource,
            DependencyType effectiveType) {
        if (effectiveType.getSource().equals(Point.START)) {
            return getStartDate(effectiveSource);
        } else {
            return getEndDate(effectiveSource);
        }
    }

    private T getEffectiveSource(IDependency<T> dependency) {
        return !reversed ? dependency.getSource() : dependency.getDestination();
    }

    private DependencyType getEffectiveType(DependencyType type) {
        return !reversed ? type : type.inverse();
    }

    protected abstract GanttDate getStartDate(T vertex);

    protected abstract GanttDate getEndDate(T vertex);

}
