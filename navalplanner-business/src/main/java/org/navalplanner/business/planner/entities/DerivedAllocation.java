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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.resources.entities.MachineWorkersConfigurationUnit;
import org.navalplanner.business.resources.entities.Resource;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class DerivedAllocation extends BaseEntity {

    public static Map<MachineWorkersConfigurationUnit, DerivedAllocation> byConfigurationUnit(
            Collection<? extends DerivedAllocation> derivedAllocations) {
        Map<MachineWorkersConfigurationUnit, DerivedAllocation> map = new HashMap<MachineWorkersConfigurationUnit, DerivedAllocation>();
        for (DerivedAllocation each : derivedAllocations) {
            map.put(each.getConfigurationUnit(), each);
        }
        return map;
    }

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

    public static DerivedAllocation create(ResourceAllocation<?> derivedFrom,
            MachineWorkersConfigurationUnit configurationUnit) {
        return create(new DerivedAllocation(derivedFrom, configurationUnit));
    }

    @NotNull
    private ResourceAllocation<?> derivedFrom;

    @NotNull
    private MachineWorkersConfigurationUnit configurationUnit;

    private Set<DerivedDayAssignment> assignments = new HashSet<DerivedDayAssignment>();

    public BigDecimal getAlpha() {
        return configurationUnit.getAlpha();
    }

    public List<Resource> getResources() {
        return new ArrayList<Resource>(DayAssignment
                .getAllResources(assignments));
    }

    public int getHours() {
        return DayAssignment.sum(assignments);
    }

    public String getName() {
        return configurationUnit.getName();
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
        this.assignments.clear();
        this.assignments.addAll(dayAssignments);
    }

    public DerivedAllocation asDerivedFrom(ResourceAllocation<?> allocation)
            throws IllegalStateException {
        if (!isNewObject()) {
            throw new IllegalStateException(
                    "a "
                            + DerivedAllocation.class.getSimpleName()
                            + " that already exists can't change its derivedFrom property");
        }
        this.derivedFrom = allocation;
        return this;
    }

    private void checkAreValid(List<DerivedDayAssignment> dayAssignments) {
        for (DerivedDayAssignment each : dayAssignments) {
            checkIsValid(each);
        }
    }

    private void checkIsValid(DerivedDayAssignment dayAssingment) {
        if (!dayAssingment.getAllocation().equals(this)) {
            throw new IllegalArgumentException(dayAssingment
                    + " is related to " + dayAssingment.getAllocation()
                    + " instead of this: " + this);
        }
    }

    public void resetAssignmentsTo(LocalDate startInclusive,
            LocalDate endExclusive, List<DerivedDayAssignment> newAssignments) {
        checkAreValid(newAssignments);
        List<DerivedDayAssignment> toBeRemoved = DayAssignment.getAtInterval(
                getAssignments(), startInclusive, endExclusive);
        assignments.removeAll(toBeRemoved);
        assignments.addAll(DayAssignment.getAtInterval(newAssignments,
                startInclusive, endExclusive));
    }

    public List<DerivedDayAssignment> getAssignments() {
        return DayAssignment.orderedByDay(new ArrayList<DerivedDayAssignment>(
                assignments));
    }

    public List<DerivedDayAssignment> copyAssignmentsAsChildrenOf(
            DerivedAllocation allocation) {
        List<DerivedDayAssignment> result = new ArrayList<DerivedDayAssignment>();
        for (DerivedDayAssignment each : getAssignments()) {
            result.add(each.copyAsChildOf(allocation));
        }
        return result;
    }
}
