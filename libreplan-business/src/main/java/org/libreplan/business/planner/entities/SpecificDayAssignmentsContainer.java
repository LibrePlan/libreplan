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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.util.deepcopy.OnCopy;
import org.libreplan.business.util.deepcopy.Strategy;
import org.libreplan.business.workingday.IntraDayDate;

/**
 * Object containing the {@link SpecificDayAssignment specific day assignments}
 * for a {@link ResourceAllocation} at a {@link Scenario} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class SpecificDayAssignmentsContainer extends BaseEntity implements
        IDayAssignmentsContainer<SpecificDayAssignment> {

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
    private IntraDayDate intraDayStart;

    /**
     * It can be <code>null</code>
     */
    private IntraDayDate intraDayEnd;

    @Valid
    @Override
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
    @Override
    public Scenario getScenario() {
        return scenario;
    }

    @Override
    public void addAll(Collection<? extends SpecificDayAssignment> assignments) {
        dayAssignments.addAll(copyToThisContainer(assignments));
    }

    @Override
    public void removeAll(Collection<? extends DayAssignment> assignments) {
        dayAssignments.removeAll(assignments);
    }

    @Override
    public void resetTo(Collection<SpecificDayAssignment> assignments) {
        dayAssignments.clear();
        dayAssignments.addAll(copyToThisContainer(assignments));
    }

    private Set<SpecificDayAssignment> copyToThisContainer(
            Collection<? extends SpecificDayAssignment> assignments) {
        return SpecificDayAssignment.copy(this, assignments);
    }

    @Override
    public IntraDayDate getIntraDayStart() {
        return intraDayStart;
    }

    @Override
    public void setIntraDayStart(IntraDayDate intraDayStart) {
        this.intraDayStart = intraDayStart;
    }

    @Override
    public IntraDayDate getIntraDayEnd() {
        return intraDayEnd;
    }

    @Override
    public void setIntraDayEnd(IntraDayDate intraDayEnd) {
        this.intraDayEnd = intraDayEnd;
    }

}
