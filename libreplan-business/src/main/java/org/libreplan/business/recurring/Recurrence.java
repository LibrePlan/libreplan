/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 Igalia, S.L.
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

package org.libreplan.business.recurring;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.planner.entities.AggregateOfResourceAllocations;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.workingday.IntraDayDate;

/**
 *
 */
public class Recurrence extends BaseEntity {


    private LocalDate date;

    private Set<ResourceAllocation<?>> resourceAllocations = new HashSet<ResourceAllocation<?>>();

    private AggregateOfResourceAllocations satisfied;

    public Recurrence() {
    }

    public Recurrence(LocalDate date,
            Collection<? extends ResourceAllocation<?>> allocations) {
        Validate.notNull(date);
        Validate.notNull(allocations);
        this.date = date;
        this.resourceAllocations = new HashSet<ResourceAllocation<?>>(
                allocations);
    }

    private AggregateOfResourceAllocations getSatisfied() {
        if (satisfied == null) {
            satisfied = AggregateOfResourceAllocations
                    .createFromSatisfied(resourceAllocations);
        }
        return satisfied;
    }

    public Set<ResourceAllocation<?>> getResourceAllocations() {
        return new HashSet<ResourceAllocation<?>>(resourceAllocations);
    }

    @SuppressWarnings("unchecked")
    public LocalDate getEnd() {
        if (getSatisfied().isEmpty()) {
            return getDate();
        }
        return Collections.max(asList(getDate(), getSatisfied()
                .getEndAsLocalDate()));
    }

    public IntraDayDate getIntraDayDateEnd() {
        IntraDayDate date = IntraDayDate.startOfDay(getDate());
        if (getSatisfied().isEmpty()) {
            return date;
        }
        return Collections.max(asList(date, getSatisfied().getEnd()));
    }

    @SuppressWarnings("unchecked")
    public LocalDate getStart() {
        if (getSatisfied().isEmpty()) {
            return getDate();
        }
        return Collections.min(asList(getDate(), getSatisfied()
                .getStartAsLocalDate()));
    }

    public IntraDayDate getIntraDayDateStart() {
        IntraDayDate date = IntraDayDate.startOfDay(getDate());
        if (getSatisfied().isEmpty()) {
            return date;
        }
        return Collections.max(asList(date, getSatisfied().getStart()));
    }

    @NotNull
    private LocalDate getDate() {
        return date;
    }

}
