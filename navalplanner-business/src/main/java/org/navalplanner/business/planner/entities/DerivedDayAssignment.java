/*
 * This file is part of NavalPlan
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

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.util.deepcopy.OnCopy;
import org.navalplanner.business.util.deepcopy.Strategy;


/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class DerivedDayAssignment extends DayAssignment {

    public static DerivedDayAssignment create(LocalDate day, int hours,
            Resource resource, DerivedAllocation derivedAllocation) {
        return create(new DerivedDayAssignment(day, hours, resource,
                derivedAllocation));
    }

    /**
     * Constructor for Hibernate. DO NOT USE!
     */
    public DerivedDayAssignment() {
    }

    private abstract class ParentState {
        protected abstract DerivedAllocation getAllocation();
    }

    private class TransientParentState extends ParentState {

        private final DerivedAllocation parent;

        private TransientParentState(DerivedAllocation parent) {
            Validate.notNull(parent);
            this.parent = parent;
        }

        @Override
        protected DerivedAllocation getAllocation() {
            return this.parent;
        }
    }

    private class ContainerParentState extends ParentState {

        public ContainerParentState() {
        }

        @Override
        protected DerivedAllocation getAllocation() {
            return container.getResourceAllocation();
        }
    }

    private DerivedDayAssignmentsContainer container;

    @OnCopy(Strategy.IGNORE)
    private ParentState parentState;

    private DerivedDayAssignment(LocalDate day, int hours, Resource resource) {
        super(day, hours, resource);
        Validate.isTrue(resource instanceof Worker);
    }

    private DerivedDayAssignment(LocalDate day, int hours, Resource resource,
            DerivedAllocation derivedAllocation) {
        this(day, hours, resource);
        this.parentState = new TransientParentState(derivedAllocation);
    }

    private DerivedDayAssignment(LocalDate day, int hours, Resource resource,
            DerivedDayAssignmentsContainer container) {
        this(day, hours, resource);
        Validate.notNull(container);
        this.container = container;
        this.parentState = new ContainerParentState();
    }

    public DerivedAllocation getAllocation() {
        return parentState.getAllocation();
    }

    DerivedDayAssignment copyAsChildOf(DerivedDayAssignmentsContainer container) {
        return create(this.getDay(), this.getHours(), this.getResource(),
                container);
    }

    private static DerivedDayAssignment create(LocalDate day, int hours,
            Resource resource, DerivedDayAssignmentsContainer container) {
        return create(new DerivedDayAssignment(day, hours, resource, container));
    }

    DerivedDayAssignment copyAsChildOf(DerivedAllocation derivedAllocation) {
        return create(this.getDay(), this.getHours(), this.getResource(),
                derivedAllocation);
    }

    @Override
    public boolean belongsTo(Object allocation) {
        return allocation != null
                && parentState.getAllocation().equals(allocation);
    }

}
