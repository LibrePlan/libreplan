/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Óscar González Fernández
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
package org.libreplan.business.planner.entities;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.workingday.EffortDuration;

/**
 * @author Oscar Gonzalez Fernandez <ogfernandez@gmail.com>
 */
public class AssignedEffortForResource {

    public interface IAssignedEffortForResource {

        public EffortDuration getAssignedDurationAt(Resource resource,
                LocalDate day);
    }

    public static IAssignedEffortForResource discount(
            Collection<? extends BaseEntity> allocations) {
        return new AssignedEffortDiscounting(allocations);
    }

    private static class AssignedEffortDiscounting implements
            IAssignedEffortForResource {

        private final Map<Long, Set<BaseEntity>> allocations;

        AssignedEffortDiscounting(Collection<? extends BaseEntity> discountFrom) {
            this.allocations = BaseEntity.byId(discountFrom);
        }

        public EffortDuration getAssignedDurationAt(Resource resource,
                LocalDate day) {
            return resource.getAssignedDurationDiscounting(allocations, day);
        }
    }

}
