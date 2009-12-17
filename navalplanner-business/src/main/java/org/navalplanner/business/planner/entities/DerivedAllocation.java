/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.MachineWorkersConfigurationUnit;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class DerivedAllocation extends BaseEntity {

    private static boolean isIfGenericContainsMachine(
            ResourceAllocation<?> derivedFrom,
            MachineWorkersConfigurationUnit configurationUnit) {
        if (derivedFrom instanceof GenericResourceAllocation) {
            GenericResourceAllocation generic = (GenericResourceAllocation) derivedFrom;
            return generic.getAssociatedResources().contains(
                    configurationUnit.getMachine());
        }
        return true;
    }

    private static boolean isIfSpecificSameMachine(
            ResourceAllocation<?> derivedFrom,
            MachineWorkersConfigurationUnit configurationUnit) {
        if (derivedFrom instanceof SpecificResourceAllocation) {
            SpecificResourceAllocation specific = (SpecificResourceAllocation) derivedFrom;
            return specific.getResource()
                    .equals(configurationUnit.getMachine());
        }
        return true;
    }

    @NotNull
    private ResourceAllocation<?> derivedFrom;

    @NotNull
    private MachineWorkersConfigurationUnit configurationUnit;

    private HashSet<DerivedDayAssignment> assignments;

    public static DerivedAllocation create(ResourceAllocation<?> derivedFrom,
            MachineWorkersConfigurationUnit configurationUnit) {
        return create(new DerivedAllocation(derivedFrom, configurationUnit));
    }

    /**
     * Constructor for hibernate. DO NOT USE!
     */
    public DerivedAllocation() {

    }

    public DerivedAllocation(ResourceAllocation<?> derivedFrom,
            MachineWorkersConfigurationUnit configurationUnit) {
        Validate.notNull(derivedFrom);
        Validate.notNull(configurationUnit);
        Validate
                .isTrue(isIfSpecificSameMachine(derivedFrom, configurationUnit));
        Validate.isTrue(isIfGenericContainsMachine(derivedFrom,
                configurationUnit));
        this.derivedFrom = derivedFrom;
        this.configurationUnit = configurationUnit;
    }

    public MachineWorkersConfigurationUnit getConfigurationUnit() {
        return configurationUnit;
    }

    public ResourceAllocation<?> getDerivedFrom() {
        return derivedFrom;
    }

    public void resetAssignmentsTo(List<DerivedDayAssignment> dayAssignments) {
        checkAreValid(dayAssignments);
        this.assignments = new HashSet<DerivedDayAssignment>(dayAssignments);
    }

    private void checkAreValid(List<DerivedDayAssignment> dayAssignments) {
        for (DerivedDayAssignment each : dayAssignments) {
            checkIsValid(each);
        }
    }

    private void checkIsValid(DerivedDayAssignment dayAssingment) {
        Machine machine = configurationUnit.getMachine();
        if (!dayAssingment.getResource().equals(machine)) {
            throw new IllegalArgumentException(dayAssingment
                    + " has the resource: " + dayAssingment.getResource()
                    + " but this derived allocation has the resource: "
                    + machine);
        }
        if (!dayAssingment.getAllocation().equals(this)) {
            throw new IllegalArgumentException(dayAssingment
                    + " is related to " + dayAssingment.getAllocation()
                    + " instead of this: " + this);
        }
    }

    public void resetAssignmentsTo(LocalDate startInclusive,
            LocalDate endExclusive, List<DerivedDayAssignment> newAssignments) {
        List<DerivedDayAssignment> toBeRemoved = DayAssignment.getAtInterval(
                getAssignments(), startInclusive, endExclusive);
        assignments.removeAll(toBeRemoved);
        checkAreValid(newAssignments);
        assignments.addAll(DayAssignment.getAtInterval(newAssignments,
                startInclusive, endExclusive));
    }

    public List<DerivedDayAssignment> getAssignments() {
        return DayAssignment.orderedByDay(new ArrayList<DerivedDayAssignment>(
                assignments));
    }


}
