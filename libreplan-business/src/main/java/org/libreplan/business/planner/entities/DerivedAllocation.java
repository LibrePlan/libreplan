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
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.common.Registry;
import org.libreplan.business.resources.entities.MachineWorkersConfigurationUnit;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.util.deepcopy.OnCopy;
import org.libreplan.business.util.deepcopy.Strategy;

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

    @OnCopy(Strategy.IGNORE)
    private Set<DerivedDayAssignmentsContainer> derivedDayAssignmentsContainers = new HashSet<DerivedDayAssignmentsContainer>();

    private Map<Scenario, DerivedDayAssignmentsContainer> byScenario() {
        Map<Scenario, DerivedDayAssignmentsContainer> result = new HashMap<Scenario, DerivedDayAssignmentsContainer>();
        for (DerivedDayAssignmentsContainer each : derivedDayAssignmentsContainers) {
            result.put(each.getScenario(), each);
        }
        return result;
    }

    private DerivedDayAssignmentsContainer retrieveOrCreate(
            Scenario scenario) {
        DerivedDayAssignmentsContainer result = byScenario().get(scenario);
        if (result == null) {
            result = DerivedDayAssignmentsContainer.create(this, scenario);
            derivedDayAssignmentsContainers.add(result);
        }
        return result;
    }

    @OnCopy(Strategy.IGNORE)
    private DayAssignmentsState dayAssignmentsState;

    private abstract class DayAssignmentsState {
        List<DerivedDayAssignment> getAssignments() {
            return DayAssignment.orderedByDay(getUnorderedAssignments());
        }

        int getHours() {
            return DayAssignment.sum(getUnorderedAssignments()).roundToHours();
        }

        abstract void resetAssignmentsTo(
                List<DerivedDayAssignment> dayAssignments);

        abstract void resetAssignmentsTo(LocalDate startInclusive,
                LocalDate endExclusive,
                List<DerivedDayAssignment> newAssignments);

        protected abstract Collection<DerivedDayAssignment> getUnorderedAssignments();

        abstract DayAssignmentsState useScenario(Scenario scenario);

    }

    private class NotSpecifiedScenarioState extends DayAssignmentsState {

        @Override
        protected Collection<DerivedDayAssignment> getUnorderedAssignments() {
            Scenario current = Registry.getScenarioManager().getCurrent();
            DerivedDayAssignmentsContainer container = byScenario()
                    .get(current);
            if (container == null) {
                return new ArrayList<DerivedDayAssignment>();
            }
            return container.getDayAssignments();
        }

        @Override
        void resetAssignmentsTo(List<DerivedDayAssignment> dayAssignments) {
            throwNotModifiable();
        }

        @Override
        void resetAssignmentsTo(LocalDate startInclusive,
                LocalDate endExclusive,
                List<DerivedDayAssignment> newAssignments) {
            throwNotModifiable();
        }

        private void throwNotModifiable() {
            throw new IllegalStateException(
                    "the scenario has not been specified");
        }

        @Override
        DayAssignmentsState useScenario(Scenario scenario) {
            return new SpecifiedScenarioState(scenario);
        }
    }

    private class TransientState extends DayAssignmentsState {

        private List<DerivedDayAssignment> assignments = new ArrayList<DerivedDayAssignment>();

        @Override
        public Collection<DerivedDayAssignment> getUnorderedAssignments() {
            return assignments;
        }

        @Override
        void resetAssignmentsTo(List<DerivedDayAssignment> dayAssignments) {
            assignments = copyAssignmentsAsChildrenOf(dayAssignments);
        }

        private List<DerivedDayAssignment> copyAssignmentsAsChildrenOf(
                List<DerivedDayAssignment> dayAssignments) {
            List<DerivedDayAssignment> result = new ArrayList<DerivedDayAssignment>();
            for (DerivedDayAssignment each : dayAssignments) {
                result.add(each.copyAsChildOf(DerivedAllocation.this));
            }
            return result;
        }

        @Override
        void resetAssignmentsTo(LocalDate startInclusive,
                LocalDate endExclusive,
                List<DerivedDayAssignment> newAssignments) {
            checkAreValid(newAssignments);
            List<DerivedDayAssignment> toBeRemoved = DayAssignment
                    .getAtInterval(getAssignments(), startInclusive,
                            endExclusive);
            assignments.removeAll(toBeRemoved);
            detachAssignments(toBeRemoved);
            assignments.addAll(DayAssignment.getAtInterval(newAssignments,
                    startInclusive, endExclusive));
        }

        @Override
        DayAssignmentsState useScenario(Scenario scenario) {
            return new SpecifiedScenarioState(scenario, assignments);
        }
    }

    private class SpecifiedScenarioState extends DayAssignmentsState {
        private final Scenario scenario;

        private SpecifiedScenarioState(Scenario scenario) {
            Validate.notNull(scenario);
            this.scenario = scenario;
        }

        public SpecifiedScenarioState(Scenario scenario,
                List<DerivedDayAssignment> assignments) {
            this(scenario);
            resetAssignmentsTo(assignments);
        }

        @Override
        protected Collection<DerivedDayAssignment> getUnorderedAssignments() {
            DerivedDayAssignmentsContainer container = retrieveOrCreate(scenario);
            return container.getDayAssignments();
        }


        @Override
        void resetAssignmentsTo(List<DerivedDayAssignment> dayAssignments) {
            DerivedDayAssignmentsContainer container = retrieveOrCreate(scenario);
            container.resetAssignmentsTo(dayAssignments);
        }

        @Override
        void resetAssignmentsTo(LocalDate startInclusive,
                LocalDate endExclusive,
                List<DerivedDayAssignment> newAssignments) {
            DerivedDayAssignmentsContainer container = retrieveOrCreate(scenario);
            container.resetAssignmentsTo(startInclusive, endExclusive,
                    newAssignments);
        }

        @Override
        DayAssignmentsState useScenario(Scenario scenario) {
            return new SpecifiedScenarioState(scenario);
        }

    }

    public BigDecimal getAlpha() {
        return configurationUnit.getAlpha();
    }

    public List<Resource> getResources() {
        Set<Resource> result = DayAssignment
                .getAllResources(dayAssignmentsState.getUnorderedAssignments());
        return new ArrayList<Resource>(result);
    }

    public int getHours() {
        return dayAssignmentsState.getHours();
    }

    public String getName() {
        return configurationUnit.getName();
    }

    /**
     * Constructor for hibernate. DO NOT USE!
     */
    public DerivedAllocation() {
        this.dayAssignmentsState = new NotSpecifiedScenarioState();
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
        this.dayAssignmentsState = new TransientState();
    }

    public MachineWorkersConfigurationUnit getConfigurationUnit() {
        return configurationUnit;
    }

    public ResourceAllocation<?> getDerivedFrom() {
        return derivedFrom;
    }

    public void resetAssignmentsTo(List<DerivedDayAssignment> dayAssignments) {
        dayAssignmentsState.resetAssignmentsTo(dayAssignments);
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
        dayAssignmentsState.resetAssignmentsTo(startInclusive, endExclusive,
                newAssignments);
    }

    private void detachAssignments(Collection<DerivedDayAssignment> toBeRemoved) {
        for (DerivedDayAssignment each : toBeRemoved) {
            each.detach();
        }
    }

    public List<DerivedDayAssignment> getAssignments() {
        return dayAssignmentsState.getAssignments();
    }

    public void useScenario(Scenario scenario) {
        this.dayAssignmentsState = dayAssignmentsState.useScenario(scenario);
    }

    public Set<DerivedDayAssignmentsContainer> getContainers() {
        return new HashSet<DerivedDayAssignmentsContainer>(
                derivedDayAssignmentsContainers);
    }

    public void copyAssignments(Scenario from, Scenario to) {
        DerivedDayAssignmentsContainer fromContainer = retrieveOrCreate(from);
        DerivedDayAssignmentsContainer toContainer = retrieveOrCreate(to);
        toContainer.resetAssignmentsTo(fromContainer.getDayAssignments());
    }

    public void removePredecessorContainersFor(Scenario scenario) {
        Map<Scenario, DerivedDayAssignmentsContainer> byScenario = byScenario();
        for (Scenario each : scenario.getPredecessors()) {
            DerivedDayAssignmentsContainer container = byScenario.get(each);
            if (container != null) {
                derivedDayAssignmentsContainers.remove(container);
            }
        }
    }

    public void removeContainersFor(Scenario scenario) {
        DerivedDayAssignmentsContainer container = byScenario().get(scenario);
        if (container != null) {
            derivedDayAssignmentsContainers.remove(container);
        }
    }

}
