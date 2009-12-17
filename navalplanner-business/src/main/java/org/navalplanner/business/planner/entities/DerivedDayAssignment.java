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

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.Resource;


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

    @NotNull
    private DerivedAllocation allocation;

    private DerivedDayAssignment(LocalDate day, int hours, Resource resource,
            DerivedAllocation derivedAllocation) {
        super(day, hours, resource);
        Validate.notNull(derivedAllocation);
        Validate.isTrue(resource instanceof Machine);
        this.allocation = derivedAllocation;
    }

    @Override
    protected void detachFromAllocation() {
        this.allocation = null;
    }

    public DerivedAllocation getAllocation() {
        return allocation;
    }

}
