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
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.util.deepcopy.OnCopy;
import org.navalplanner.business.util.deepcopy.Strategy;
import org.navalplanner.business.workingday.EffortDuration;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class GenericDayAssignment extends DayAssignment {

    private abstract class ParentState {
        abstract GenericResourceAllocation getResourceAllocation();

        abstract ParentState setParent(
                GenericResourceAllocation genericResourceAllocation);

        abstract ParentState setParent(GenericDayAssignmentsContainer container);

        abstract Scenario getScenario();
    }

    private class ContainerNotSpecified extends ParentState {

        private GenericResourceAllocation parent;

        @Override
        GenericResourceAllocation getResourceAllocation() {
            return parent;
        }

        @Override
        ParentState setParent(
                GenericResourceAllocation genericResourceAllocation) {
            if (parent != null) {
                throw new IllegalStateException(
                        "the allocation cannot be changed once it has been set");
            }
            this.parent = genericResourceAllocation;
            return this;
        }

        @Override
        ParentState setParent(GenericDayAssignmentsContainer container) {
            return new OnContainer(container);
        }

        @Override
        Scenario getScenario() {
            return null;
        }

    }

    private class OnContainer extends ParentState {

        OnContainer(GenericDayAssignmentsContainer container) {
            Validate.notNull(container);
            GenericDayAssignment.this.container = container;
        }

        public OnContainer() {
        }

        @Override
        GenericResourceAllocation getResourceAllocation() {
            return container.getResourceAllocation();
        }

        @Override
        ParentState setParent(
                GenericResourceAllocation genericResourceAllocation) {
            throw new IllegalStateException("parent already set");
        }

        @Override
        ParentState setParent(GenericDayAssignmentsContainer container) {
            throw new IllegalStateException("parent already set");
        }

        @Override
        Scenario getScenario() {
            return container.getScenario();
        }
    }

    public static GenericDayAssignment create(LocalDate day,
            EffortDuration duration, Resource resource) {
        return create(new GenericDayAssignment(day, duration, resource));
    }

    public static Set<GenericDayAssignment> copy(
            GenericDayAssignmentsContainer newParent,
            Collection<? extends GenericDayAssignment> assignemnts) {
        Set<GenericDayAssignment> result = new HashSet<GenericDayAssignment>();
        for (GenericDayAssignment a : assignemnts) {
            GenericDayAssignment created = copy(newParent, a);
            created.associateToResource();
            result.add(created);
        }
        return result;
    }

    private static GenericDayAssignment copy(
            GenericDayAssignmentsContainer newParent,
            GenericDayAssignment toBeCopied) {
        GenericDayAssignment result = copyFromWithoutParent(toBeCopied);
        result.setConsolidated(toBeCopied.isConsolidated());
        result.parentState = result.parentState.setParent(newParent);
        result.associateToResource();
        return result;
    }

    private static GenericDayAssignment copyFromWithoutParent(
            GenericDayAssignment toBeCopied) {
        GenericDayAssignment copy = create(toBeCopied.getDay(),
                toBeCopied.getDuration(), toBeCopied.getResource());
        copy.setConsolidated(toBeCopied.isConsolidated());
        return copy;
    }

    public static List<GenericDayAssignment> copyToAssignmentsWithoutParent(
            Collection<? extends GenericDayAssignment> assignments) {
        List<GenericDayAssignment> result = new ArrayList<GenericDayAssignment>();
        for (GenericDayAssignment each : assignments) {
            result.add(copyFromWithoutParent(each));
        }
        return result;
    }

    private GenericDayAssignment(LocalDate day, EffortDuration duration,
            Resource resource) {
        super(day, duration, resource);
        parentState = new ContainerNotSpecified();
    }

    /**
     * Constructor for hibernate. DO NOT USE!
     */
    public GenericDayAssignment() {
        parentState = new OnContainer();
    }

    @NotNull
    private GenericDayAssignmentsContainer container;

    @OnCopy(Strategy.IGNORE)
    private ParentState parentState;

    public GenericResourceAllocation getGenericResourceAllocation() {
        return parentState.getResourceAllocation();
    }

    protected void setGenericResourceAllocation(
            GenericResourceAllocation genericResourceAllocation) {
        parentState = parentState.setParent(genericResourceAllocation);
    }

    protected void detachFromAllocation() {
        this.parentState = new ContainerNotSpecified();
    }

    @Override
    protected BaseEntity getParent() {
        return getGenericResourceAllocation();
    }

    @Override
    public String toString() {
        return super.toString() + " duration: " + getDuration();
    }

    @Override
    public Scenario getScenario() {
        return parentState.getScenario();
    }

    public DayAssignment withDuration(EffortDuration newDuration) {
        GenericDayAssignment result = create(getDay(), newDuration,
                getResource());
        if (container != null) {
            result.parentState.setParent(container);
        } else if (this.getGenericResourceAllocation() != null) {
            result.parentState.setParent(this.getGenericResourceAllocation());
        }
        return result;
    }

}
