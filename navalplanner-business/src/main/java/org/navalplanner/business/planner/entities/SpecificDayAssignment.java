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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
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
public class SpecificDayAssignment extends DayAssignment {

    private abstract class ParentState {
        abstract SpecificResourceAllocation getResourceAllocation();

        abstract ParentState setParent(
                SpecificResourceAllocation genericResourceAllocation);

        abstract ParentState setParent(SpecificDayAssignmentsContainer container);

        abstract Scenario getScenario();
    }

    private class ContainerNotSpecified extends ParentState {

        private SpecificResourceAllocation parent;

        @Override
        SpecificResourceAllocation getResourceAllocation() {
            return parent;
        }

        @Override
        ParentState setParent(
                SpecificResourceAllocation genericResourceAllocation) {
            if (parent != null) {
                throw new IllegalStateException(
                        "the allocation cannot be changed once it has been set");
            }
            this.parent = genericResourceAllocation;
            return this;
        }

        @Override
        ParentState setParent(SpecificDayAssignmentsContainer container) {
            return new OnContainer(container);
        }

        @Override
        Scenario getScenario() {
            return null;
        }

    }

    private class OnContainer extends ParentState {

        OnContainer(SpecificDayAssignmentsContainer container) {
            Validate.notNull(container);
            SpecificDayAssignment.this.container = container;
        }

        public OnContainer() {
        }

        @Override
        SpecificResourceAllocation getResourceAllocation() {
            return container.getResourceAllocation();
        }

        @Override
        ParentState setParent(
                SpecificResourceAllocation genericResourceAllocation) {
            throw new IllegalStateException("parent already set");
        }

        @Override
        ParentState setParent(SpecificDayAssignmentsContainer container) {
            throw new IllegalStateException("parent already set");
        }

        @Override
        Scenario getScenario() {
            return container.getScenario();
        }
    }


    public static Set<SpecificDayAssignment> copy(
            SpecificDayAssignmentsContainer container,
            Collection<? extends SpecificDayAssignment> specificDaysAssignment) {
        Set<SpecificDayAssignment> result = new HashSet<SpecificDayAssignment>();
        for (SpecificDayAssignment s : specificDaysAssignment) {
            SpecificDayAssignment created = copyFromWithoutParent(s);
            created.parentState = created.parentState.setParent(container);
            created.setConsolidated(s.isConsolidated());
            created.associateToResource();
            result.add(created);
        }
        return result;
    }

    private static SpecificDayAssignment copyFromWithoutParent(
            SpecificDayAssignment assignment) {
        SpecificDayAssignment copy = create(assignment.getDay(),
                assignment.getDuration(), assignment.getResource());
        copy.setConsolidated(assignment.isConsolidated());
        return copy;
    }

    public static List<SpecificDayAssignment> copyToAssignmentsWithoutParent(
            Collection<? extends SpecificDayAssignment> assignments) {
        List<SpecificDayAssignment> result = new ArrayList<SpecificDayAssignment>();
        for (SpecificDayAssignment each : assignments) {
            result.add(copyFromWithoutParent(each));
        }
        return result;
    }

    @OnCopy(Strategy.IGNORE)
    private ParentState parentState;

    @NotNull
    private SpecificDayAssignmentsContainer container;

    public static SpecificDayAssignment create(LocalDate day,
            EffortDuration duration, Resource resource) {
        return create(new SpecificDayAssignment(day, duration, resource));
    }

    public SpecificDayAssignment(LocalDate day, EffortDuration duration,
            Resource resource) {
        super(day, duration, resource);
        this.parentState = new ContainerNotSpecified();
    }

    /**
     * Constructor for hibernate. DO NOT USE!
     */
    public SpecificDayAssignment() {
        this.parentState = new OnContainer();
    }

    public SpecificResourceAllocation getSpecificResourceAllocation() {
        return parentState.getResourceAllocation();
    }

    public void setSpecificResourceAllocation(
            SpecificResourceAllocation specificResourceAllocation) {
        this.parentState = this.parentState
                .setParent(specificResourceAllocation);
    }

    @Override
    public boolean belongsTo(Object resourceAllocation) {
        return resourceAllocation != null
                && getSpecificResourceAllocation().equals(resourceAllocation);
    }

    @Override
    public Scenario getScenario() {
        return parentState.getScenario();
    }

    @Override
    public DayAssignment withDuration(EffortDuration newDuration) {
        SpecificDayAssignment result = create(getDay(), newDuration,
                getResource());
        if (container != null) {
            result.parentState.setParent(container);
        } else if (this.getSpecificResourceAllocation() != null) {
            result.parentState.setParent(this.getSpecificResourceAllocation());
        }
        return result;
    }

    @Override
    protected void detachFromAllocation() {
        this.parentState = new ContainerNotSpecified();
    }
}
