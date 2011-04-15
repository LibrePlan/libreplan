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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.workingday.EffortDuration;

public class AssignedEffortDiscounting implements
        IAssignedEffortForResource {

    private final Map<Long, Set<BaseEntity>> allocations;

    public AssignedEffortDiscounting(BaseEntity allocationToDiscountFrom) {
        this(Collections.singleton(allocationToDiscountFrom));
    }

    AssignedEffortDiscounting(Collection<? extends BaseEntity> discountFrom) {
        this.allocations = BaseEntity.byId(discountFrom);
    }

    public EffortDuration getAssignedDurationAt(Resource resource, LocalDate day) {
        return resource.getAssignedDurationDiscounting(allocations, day);
    }

}