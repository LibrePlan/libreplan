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
import java.util.HashSet;
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

    public static IAssignedEffortForResource effortDiscounting(
            Collection<? extends BaseEntity> allocations) {
        return new AssignedEffortDiscounting(allocations);
    }

    public static IAssignedEffortForResource sum(
            final IAssignedEffortForResource... assignedEffortForResources) {
        return new IAssignedEffortForResource() {

            @Override
            public EffortDuration getAssignedDurationAt(Resource resource,
                    LocalDate day) {
                EffortDuration result = EffortDuration.zero();
                for (IAssignedEffortForResource each : assignedEffortForResources) {
                    EffortDuration e = each
                            .getAssignedDurationAt(resource, day);
                    if (e != null) {
                        result = result.plus(e);
                    }
                }
                return result;
            }
        };
    }

    public static IAssignedEffortForResource sum(
            Collection<? extends IAssignedEffortForResource> assignedEffortForResources) {
        return sum(assignedEffortForResources
                .toArray(new IAssignedEffortForResource[0]));
    }

    public static WithTheLoadOf withTheLoadOf(
            Collection<? extends ResourceAllocation<?>> allocations) {
        return new WithTheLoadOf(allocations);
    }

    public static class WithTheLoadOf implements IAssignedEffortForResource {

        private final Set<? extends ResourceAllocation<?>> allocations;
        private final IAssignedEffortForResource implementation;

        public WithTheLoadOf(
                Collection<? extends ResourceAllocation<?>> allocations) {
            this.allocations = new HashSet<ResourceAllocation<?>>(allocations);
            this.implementation = sum(this.allocations);
        }

        @Override
        public EffortDuration getAssignedDurationAt(Resource resource,
                LocalDate day) {
            return implementation.getAssignedDurationAt(resource, day);
        }

        public WithTheLoadOf withoutConsidering(ResourceAllocation<?> allocation) {
            Set<ResourceAllocation<?>> copy = new HashSet<ResourceAllocation<?>>(
                    this.allocations);
            copy.remove(allocation);
            return new WithTheLoadOf(copy);
        }
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
