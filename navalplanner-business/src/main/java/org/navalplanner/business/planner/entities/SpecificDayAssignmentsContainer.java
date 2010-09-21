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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.util.deepcopy.OnCopy;
import org.navalplanner.business.util.deepcopy.Strategy;
import org.navalplanner.business.workingday.IntraDayDate;

/**
 * Object containing the {@link SpecificDayAssignment specific day assignments}
 * for a {@link ResourceAllocation} at a {@link Scenario} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class SpecificDayAssignmentsContainer extends BaseEntity {

    public static SpecificDayAssignmentsContainer create(
            SpecificResourceAllocation specificResourceAllocation,
            Scenario scenario) {
        return create(new SpecificDayAssignmentsContainer(specificResourceAllocation,
                scenario));
    }

    private SpecificResourceAllocation resourceAllocation;

    @OnCopy(Strategy.SHARE)
    private Scenario scenario;

    private Set<SpecificDayAssignment> dayAssignments = new HashSet<SpecificDayAssignment>();

    /**
     * It can be <code>null</code>
     */
    private IntraDayDate intraDayEnd;

    @Valid
    public Set<SpecificDayAssignment> getDayAssignments() {
        return new HashSet<SpecificDayAssignment>(dayAssignments);
    }

    private SpecificDayAssignmentsContainer(
            SpecificResourceAllocation resourceAllocation, Scenario scenario) {
        Validate.notNull(resourceAllocation);
        Validate.notNull(scenario);
        this.resourceAllocation = resourceAllocation;
        this.scenario = scenario;
    }

    /**
     * default constructor for Hibernate. DO NOT USE!
     */
    public SpecificDayAssignmentsContainer() {
    }

    @NotNull
    public SpecificResourceAllocation getResourceAllocation() {
        return resourceAllocation;
    }

    @NotNull
    public Scenario getScenario() {
        return scenario;
    }

    public void addAll(Collection<? extends SpecificDayAssignment> assignments) {
        dayAssignments.addAll(copyToThisContainer(assignments));
    }

    public void removeAll(List<? extends DayAssignment> assignments) {
        dayAssignments.removeAll(assignments);
    }

    public void resetTo(Collection<SpecificDayAssignment> assignments) {
        dayAssignments.clear();
        dayAssignments.addAll(copyToThisContainer(assignments));
    }

    private Set<SpecificDayAssignment> copyToThisContainer(
            Collection<? extends SpecificDayAssignment> assignments) {
        return SpecificDayAssignment.copy(this, assignments);
    }

    public IntraDayDate getIntraDayEnd() {
        return intraDayEnd;
    }

    public void setIntraDayEnd(IntraDayDate intraDayEnd) {
        this.intraDayEnd = intraDayEnd;
    }

}
