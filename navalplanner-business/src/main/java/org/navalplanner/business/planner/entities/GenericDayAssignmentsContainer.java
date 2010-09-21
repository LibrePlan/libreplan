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
 * Object containing the {@link GenericDayAssignment generic day assignments}
 * for a {@link ResourceAllocation} at a {@link Scenario} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class GenericDayAssignmentsContainer extends BaseEntity {

    public static GenericDayAssignmentsContainer create(
            GenericResourceAllocation genericAllocation,
            Scenario scenario) {
        return create(new GenericDayAssignmentsContainer(genericAllocation,
                scenario));
    }

    private GenericResourceAllocation resourceAllocation;

    @OnCopy(Strategy.SHARE)
    private Scenario scenario;

    private Set<GenericDayAssignment> dayAssignments = new HashSet<GenericDayAssignment>();

    /**
     * It can be <code>null</code>
     */
    private IntraDayDate intraDayEnd;

    private GenericDayAssignmentsContainer(GenericResourceAllocation resourceAllocation,
            Scenario scenario) {
        Validate.notNull(resourceAllocation);
        Validate.notNull(scenario);
        this.resourceAllocation = resourceAllocation;
        this.scenario = scenario;
    }

    /**
     * default constructor for Hibernate. DO NOT USE!
     */
    public GenericDayAssignmentsContainer() {
    }

    @NotNull
    public GenericResourceAllocation getResourceAllocation() {
        return resourceAllocation;
    }

    @NotNull
    public Scenario getScenario() {
        return scenario;
    }

    @Valid
    public Set<GenericDayAssignment> getDayAssignments() {
        return new HashSet<GenericDayAssignment>(dayAssignments);
    }

    public void addAll(Collection<? extends GenericDayAssignment> assignments) {
        dayAssignments.addAll(copyToThisContainer(assignments));
    }

    public void removeAll(List<? extends DayAssignment> assignments) {
        dayAssignments.removeAll(assignments);
    }

    public void resetTo(Collection<GenericDayAssignment> assignments) {
        dayAssignments.clear();
        dayAssignments.addAll(copyToThisContainer(assignments));
    }

    private Set<GenericDayAssignment> copyToThisContainer(
            Collection<? extends GenericDayAssignment> assignments) {
        return GenericDayAssignment.copy(this, assignments);
    }

    public IntraDayDate getIntraDayEnd() {
        return intraDayEnd;
    }

    public void setIntraDayEnd(IntraDayDate intraDayEnd) {
        this.intraDayEnd = intraDayEnd;
    }

}
