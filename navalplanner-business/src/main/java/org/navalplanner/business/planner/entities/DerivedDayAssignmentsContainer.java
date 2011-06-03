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
package org.navalplanner.business.planner.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.util.deepcopy.OnCopy;
import org.navalplanner.business.util.deepcopy.Strategy;

/**
 * Object containing the {@link DerivedDayAssignment derived day assignments}
 * for a {@link DerivedAllocation} at a {@link Scenario} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DerivedDayAssignmentsContainer extends BaseEntity {

    public static DerivedDayAssignmentsContainer create(
            DerivedAllocation derivedAllocation, Scenario scenario) {
        return create(new DerivedDayAssignmentsContainer(derivedAllocation,
                scenario));
    }

    private DerivedAllocation resourceAllocation;

    @OnCopy(Strategy.SHARE)
    private Scenario scenario;

    private Set<DerivedDayAssignment> dayAssignments = new HashSet<DerivedDayAssignment>();

    private DerivedDayAssignmentsContainer(
            DerivedAllocation resourceAllocation, Scenario scenario) {
        Validate.notNull(resourceAllocation);
        Validate.notNull(scenario);
        this.resourceAllocation = resourceAllocation;
        this.scenario = scenario;
    }

    /**
     * Constructor for HIBERNATE. DO NOT USE!
     */
    public DerivedDayAssignmentsContainer() {
    }

    Set<DerivedDayAssignment> getDayAssignments() {
        return new HashSet<DerivedDayAssignment>(dayAssignments);
    }

    public DerivedAllocation getResourceAllocation() {
        return resourceAllocation;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public void resetAssignmentsTo(
            Collection<? extends DerivedDayAssignment> newAssignments) {
        dayAssignments.clear();
        dayAssignments.addAll(copyToThisAllocation(newAssignments));
    }

    public void resetAssignmentsTo(LocalDate startInclusive,
            LocalDate endExclusive, List<DerivedDayAssignment> newAssignments) {
        checkAreValid(newAssignments);
        List<DerivedDayAssignment> toBeRemoved = DayAssignment.getAtInterval(
                DayAssignment.orderedByDay(getDayAssignments()),
                startInclusive, endExclusive);
        dayAssignments.removeAll(toBeRemoved);
        dayAssignments.addAll(copyToThisAllocation(DayAssignment.getAtInterval(
                newAssignments, startInclusive, endExclusive)));
    }

    private List<DerivedDayAssignment> copyToThisAllocation(
            Collection<? extends DerivedDayAssignment> newAssignments) {
        List<DerivedDayAssignment> result = new ArrayList<DerivedDayAssignment>();
        for (DerivedDayAssignment each : newAssignments) {
            result.add(each.copyAsChildOf(this));
        }
        return result;
    }

    private void checkAreValid(List<DerivedDayAssignment> newAssignments) {
        String errorMessage = "the new assignments added must have"
                + " the allocation that this container is associated with";
        for (DerivedDayAssignment each : newAssignments) {
            Validate.isTrue(each.getAllocation().equals(resourceAllocation),
                    errorMessage);
        }
    }
}
