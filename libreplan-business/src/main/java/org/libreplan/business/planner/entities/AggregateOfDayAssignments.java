/*
 * This file is part of LibrePlan
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

package org.libreplan.business.planner.entities;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;

import org.libreplan.business.planner.entities.DayAssignment;
import org.libreplan.business.workingday.EffortDuration;

/**
 * Computes aggregate values on a set{@link DayAssignment}.
 * <p>
 * @author Nacho Barrientos <nacho@igalia.com>
 */
public class AggregateOfDayAssignments {

    public static AggregateOfDayAssignments createByDataRange(
            List<DayAssignment> assignments,
            Date start,
            Date end) {

        assignments = DayAssignment.orderedByDay(assignments);

        return new AggregateOfDayAssignments(
                DayAssignment.getAtInterval(assignments,
                        new LocalDate(start), new LocalDate(end)));
    }

    public static AggregateOfDayAssignments create(List<DayAssignment> assignments) {
        return new AggregateOfDayAssignments(assignments);
    }

    private Set<DayAssignment> dayAssignments;

    private AggregateOfDayAssignments(
            Collection<DayAssignment> assignments) {
        Validate.notNull(assignments);
        Validate.noNullElements(assignments);
        this.dayAssignments = new HashSet<DayAssignment>(
                assignments);
    }

    public EffortDuration getTotalTime() {
        EffortDuration sum = EffortDuration.zero();
        for (DayAssignment dayAssignment : dayAssignments) {
            sum = EffortDuration.sum(sum, dayAssignment.getDuration());
        }
        return sum;
    }

}
