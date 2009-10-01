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

import org.joda.time.LocalDate;
import org.navalplanner.business.resources.entities.Resource;


/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class SpecificDayAssignment extends DayAssignment {

    private SpecificResourceAllocation specificResourceAllocation;

    public static SpecificDayAssignment create(LocalDate day, int hours,
            Resource resource) {
        return (SpecificDayAssignment) create(new SpecificDayAssignment(day,
                hours, resource));
    }

    public SpecificDayAssignment(LocalDate day, int hours, Resource resource) {
        super(day, hours, resource);
    }

    /**
     * Constructor for hibernate. DO NOT USE!
     */
    public SpecificDayAssignment() {

    }

    public SpecificResourceAllocation getSpecificResourceAllocation() {
        return specificResourceAllocation;
    }

    public void setSpecificResourceAllocation(
            SpecificResourceAllocation specificResourceAllocation) {
        if (this.specificResourceAllocation != null)
            throw new IllegalStateException(
                    "the allocation cannot be changed once it has been set");
        this.specificResourceAllocation = specificResourceAllocation;
    }
}
