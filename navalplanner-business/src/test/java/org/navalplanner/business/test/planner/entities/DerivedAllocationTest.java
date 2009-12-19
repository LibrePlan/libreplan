/*x
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
package org.navalplanner.business.test.planner.entities;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.planner.entities.DerivedAllocation;
import org.navalplanner.business.planner.entities.DerivedDayAssignment;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.MachineWorkersConfigurationUnit;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DerivedAllocationTest {

    private Worker worker = Worker.create();

    private Machine machine = Machine.create();

    private MachineWorkersConfigurationUnit configurationUnit;

    private ResourceAllocation<?> derivedFrom;

    private DerivedAllocation derivedAllocation;

    private List<DerivedDayAssignment> dayAssignments;

    private void givenConfigurationUnit(Machine machine) {
        configurationUnit = createNiceMock(MachineWorkersConfigurationUnit.class);
        expect(configurationUnit.getMachine()).andReturn(machine).anyTimes();
        replay(configurationUnit);
    }

    private void givenConfigurationUnit() {
        givenConfigurationUnit(machine);
    }

    private void givenADerivedAllocation() {
        givenDerivedFrom();
        givenConfigurationUnit();
        derivedAllocation = DerivedAllocation.create(derivedFrom,
                configurationUnit);
    }

    private void givenDayAssignments(LocalDate start, Resource resource, int... hours) {
        givenDayAssignments(derivedAllocation, start, resource, hours);
    }

    private void givenDayAssignments(DerivedAllocation derivedAllocation,
            LocalDate start, Resource resource, int... hours) {
        dayAssignments = new ArrayList<DerivedDayAssignment>();
        for (int i = 0; i < hours.length; i++) {
            LocalDate current = start.plusDays(i);
            DerivedDayAssignment d = DerivedDayAssignment.create(current,
                    hours[i], resource, derivedAllocation);
            dayAssignments.add(d);
        }
    }

    private Machine createMachine() {
        Machine result = createNiceMock(Machine.class);
        replay(result);
        return result;
    }

    private void givenSpecificDerivedFrom(Resource resource) {
        derivedFrom = createNiceMock(SpecificResourceAllocation.class);
        SpecificResourceAllocation specific = (SpecificResourceAllocation) derivedFrom;
        expect(specific.getResource()).andReturn(resource).anyTimes();
        replay(derivedFrom);
    }

    private void givenGenericDerivedFrom(Resource... resources) {
        derivedFrom = createNiceMock(GenericResourceAllocation.class);
        GenericResourceAllocation generic = (GenericResourceAllocation) derivedFrom;
        expect(generic.getAssociatedResources()).andReturn(
                Arrays.asList(resources));
        replay(derivedFrom);
    }

    private void givenDerivedFrom() {
        givenSpecificDerivedFrom(machine);
    }

    @Test
    public void aDerivedAllocationHasAMachineWorkerConfigurationUnitAndAResourceAllocation() {
        givenConfigurationUnit();
        givenDerivedFrom();
        DerivedAllocation result = DerivedAllocation.create(derivedFrom,
                configurationUnit);
        assertNotNull(result);
        assertThat(result.getConfigurationUnit(), equalTo(configurationUnit));
        assertEquals(result.getDerivedFrom(), derivedFrom);
    }

    @Test(expected = IllegalArgumentException.class)
    public void theConfigurationUnitMachineMustBeTheSameThanTheAllocationIfItIsSpecific() {
        givenConfigurationUnit(createMachine());
        givenSpecificDerivedFrom(createMachine());
        DerivedAllocation.create(derivedFrom, configurationUnit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void theMachineOfTheConfigurationUnitMustBeInTheResourcesOfTheGenericAlloation() {
        givenConfigurationUnit();
        givenGenericDerivedFrom(Worker.create(), Worker.create());
        DerivedAllocation.create(derivedFrom, configurationUnit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void theDerivedFromMustBeNotNull() {
        givenConfigurationUnit();
        DerivedAllocation.create(derivedFrom, configurationUnit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void theConfigurationUnitMustBeNotNull() {
        givenDerivedFrom();
        DerivedAllocation.create(derivedFrom, configurationUnit);
    }

    @Test
    public void aJustCreatedDerivedAllocationIsANewObject() {
        givenDerivedFrom();
        givenConfigurationUnit();
        DerivedAllocation result = DerivedAllocation.create(derivedFrom,
                configurationUnit);
        assertTrue(result.isNewObject());
    }

    @Test
    public void aDerivedAllocationCanBeResetToSomeDayAssignmentsAndIsOrderedByDay() {
        givenADerivedAllocation();
        givenDayAssignments(new LocalDate(2008, 12, 1), worker, 8, 8, 8, 8);
        derivedAllocation.resetAssignmentsTo(dayAssignments);
        assertThat(derivedAllocation.getAssignments(), equalTo(dayAssignments));
    }

    @Test(expected = IllegalArgumentException.class)
    public void theDerivedDayAssignmentsMustBeForTheSameMachine() {
        givenADerivedAllocation();
        final Machine otherMachine = Machine.create();
        givenDayAssignments(new LocalDate(2008, 12, 1), otherMachine, 8, 8,
                8);
        derivedAllocation.resetAssignmentsTo(dayAssignments);
    }

    @Test(expected = IllegalArgumentException.class)
    public void theDerivedDayAssignmentsMustBeForTheSameDerivedAllocation() {
        givenADerivedAllocation();
        DerivedAllocation another = DerivedAllocation.create(derivedFrom,
                configurationUnit);
        givenDayAssignments(another, new LocalDate(2008, 12, 1), worker, 8, 8,
                8);
        derivedAllocation.resetAssignmentsTo(dayAssignments);
    }

    @Test
    public void theAssignmentsCanBeResetOnAnInterval() {
        givenADerivedAllocation();
        LocalDate start = new LocalDate(2008, 12, 1);
        givenDayAssignments(start, worker, 8, 8, 8, 8);
        derivedAllocation.resetAssignmentsTo(dayAssignments);
        final LocalDate startInterval = start.plusDays(2);
        final LocalDate finishInterval = start.plusDays(4);
        DerivedDayAssignment newAssignment = DerivedDayAssignment.create(
                startInterval, 3, worker, derivedAllocation);
        derivedAllocation.resetAssignmentsTo(startInterval, finishInterval,
                Arrays.asList(newAssignment));
        assertThat(derivedAllocation.getAssignments(), equalTo(Arrays.asList(
                dayAssignments.get(0), dayAssignments.get(1), newAssignment)));
    }

    @Test
    public void whenResettingAssignmentsOnIntervalOnlyTheOnesAtTheIntervalAreAdded() {
        givenADerivedAllocation();
        LocalDate start = new LocalDate(2008, 12, 1);
        givenDayAssignments(start, worker, 8, 8, 8, 8);
        derivedAllocation.resetAssignmentsTo(dayAssignments);
        DerivedDayAssignment newAssignment = DerivedDayAssignment.create(start
                .minusDays(1), 3, worker, derivedAllocation);
        derivedAllocation.resetAssignmentsTo(start, start.plusDays(4), Arrays
                .asList(newAssignment));
        assertTrue(derivedAllocation.getAssignments().isEmpty());
    }

}
