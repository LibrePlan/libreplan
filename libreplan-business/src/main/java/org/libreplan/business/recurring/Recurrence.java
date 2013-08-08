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

import java.util.Set;

import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.planner.entities.ResourceAllocation;

/**
 *
 */
public class Recurrence extends BaseEntity {


    private LocalDate date;

    private Set<ResourceAllocation<?>> resourceAllocations;

    public Recurrence() {
    }

    @NotNull
    private LocalDate getDate() {
        return date;
    }

}
