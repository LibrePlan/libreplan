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
package org.navalplanner.business.planner.entities;

import java.util.Date;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;

/**
 * Component class that encapsulates a {@link PositionConstraintType} and its
 * associated constraint date <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TaskPositionConstraint {

    private PositionConstraintType constraintType = PositionConstraintType.AS_SOON_AS_POSSIBLE;

    private LocalDate constraintDate = null;

    public TaskPositionConstraint() {
    }

    public PositionConstraintType getConstraintType() {
        return constraintType != null ? constraintType
                : PositionConstraintType.AS_SOON_AS_POSSIBLE;
    }

    public boolean isConstraintAppliedToStart() {
        return getConstraintType().appliesToTheStart();
    }

    public boolean isConstraintAppliedToEnd() {
        return !isConstraintAppliedToStart();
    }

    public Date getConstraintDateAsDate() {
        return constraintDate != null ? constraintDate.toDateTimeAtStartOfDay()
                .toDate() : null;
    }

    public void explicityMovedTo(LocalDate date) {
        Validate.notNull(date);
        constraintType = constraintType.newTypeAfterMoved();
        constraintDate = date;
    }

    public LocalDate getConstraintDate() {
        return constraintDate;
    }

    public void notEarlierThan(LocalDate date) {
        Validate.notNull(date);
        this.constraintDate = date;
        this.constraintType = PositionConstraintType.START_NOT_EARLIER_THAN;
    }

    public void finishNotLaterThan(LocalDate date) {
        Validate.notNull(date);
        this.constraintDate = date;
        this.constraintType = PositionConstraintType.FINISH_NOT_LATER_THAN;
    }

    public void asLateAsPossible() {
        this.constraintType = PositionConstraintType.AS_LATE_AS_POSSIBLE;
        this.constraintDate = null;
    }

    public void asSoonAsPossible() {
        this.constraintType = PositionConstraintType.AS_SOON_AS_POSSIBLE;
        this.constraintDate = null;
    }

    public boolean isValid(PositionConstraintType type, LocalDate value) {
        return type != null
                && type.isAssociatedDateRequired() == (value != null);
    }

    public void update(PositionConstraintType type, LocalDate value) {
        Validate.isTrue(isValid(type, value));
        this.constraintType = type;
        this.constraintDate = value;
    }

}
