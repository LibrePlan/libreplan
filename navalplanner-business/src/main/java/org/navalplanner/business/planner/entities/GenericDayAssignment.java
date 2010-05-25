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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.LocalDate;
import org.navalplanner.business.resources.entities.Resource;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class GenericDayAssignment extends DayAssignment {

    private GenericResourceAllocation genericResourceAllocation;

    public static GenericDayAssignment create(LocalDate day, int hours,
            Resource resource) {
        return create(new GenericDayAssignment(day, hours,
                resource));
    }

    public static Set<GenericDayAssignment> copy(
            GenericResourceAllocation newAllocation,
            Collection<GenericDayAssignment> assignemnts) {
        Set<GenericDayAssignment> result = new HashSet<GenericDayAssignment>();
        for (GenericDayAssignment a : assignemnts) {
            GenericDayAssignment created = create(a.getDay(), a.getHours(), a
                    .getResource());
            created.setConsolidated(a.isConsolidated());
            created.setGenericResourceAllocation(newAllocation);
            created.associateToResource();
            result.add(created);
        }
        return result;
    }

    private GenericDayAssignment(LocalDate day, int hours, Resource resource) {
        super(day, hours, resource);
    }

    /**
     * Constructor for hibernate. DO NOT USE!
     */
    public GenericDayAssignment() {

    }

    public GenericResourceAllocation getGenericResourceAllocation() {
        return genericResourceAllocation;
    }

    protected void setGenericResourceAllocation(
            GenericResourceAllocation genericResourceAllocation) {
        if (this.genericResourceAllocation != null) {
            throw new IllegalStateException(
                    "the allocation cannot be changed once it has been set");
        }
        this.genericResourceAllocation = genericResourceAllocation;
    }

    protected void detachFromAllocation() {
        genericResourceAllocation = null;
    }

    @Override
    public boolean belongsTo(Object allocation) {
        return allocation != null
                && genericResourceAllocation.equals(allocation);
    }

    @Override
    public String toString() {
        return super.toString() + " hours: " + getHours();
    }

}
