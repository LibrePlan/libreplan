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

package org.navalplanner.business.planner.entities.consolidations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.ResourceAllocation;

/**
 * Represents the number of hours per {@link ResourceAllocation} that are not
 * consolidated.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class PendingConsolidatedHoursPerResourceAllocation extends BaseEntity {

    private Integer pendingConsolidatedHours;

    private ResourceAllocation<?> resourceAllocation;

    public static PendingConsolidatedHoursPerResourceAllocation create(
            LocalDate consolidatedDate, BigDecimal consolidatedValue,
            ResourceAllocation<?> resourceAllocation) {
        return create(new PendingConsolidatedHoursPerResourceAllocation(
                consolidatedDate, consolidatedValue, resourceAllocation));
    }

    public static PendingConsolidatedHoursPerResourceAllocation create(
            Integer pendingConsolidatedHours,
            ResourceAllocation<?> resourceAllocation) {
        return create(new PendingConsolidatedHoursPerResourceAllocation(
                pendingConsolidatedHours, resourceAllocation));
    }

    protected PendingConsolidatedHoursPerResourceAllocation(
            LocalDate consolidatedDate, BigDecimal consolidatedValue,
            ResourceAllocation<?> resourceAllocation) {
        this.setResourceAllocation(resourceAllocation);
        this.setPendingConsolidatedHours(calculatePendingConsolidatedHours(
                consolidatedDate, consolidatedValue, resourceAllocation
                        .getAssignments()));
    }

    protected PendingConsolidatedHoursPerResourceAllocation(
            Integer pendingConsolidatedHours,
            ResourceAllocation<?> resourceAllocation) {
        this.setResourceAllocation(resourceAllocation);
        this.setPendingConsolidatedHours(pendingConsolidatedHours);
    }

    protected PendingConsolidatedHoursPerResourceAllocation() {

    }

    private Integer calculatePendingConsolidatedHours(
            LocalDate consolidatedDate, BigDecimal consolidatedValue,
            Collection<? extends DayAssignment> assignments) {
        for (DayAssignment dayAssignment : assignments) {
            if ((dayAssignment.getDay().compareTo(consolidatedDate)) <= 0) {
                dayAssignment.setConsolidated(true);
            }
        }

        int originalTotalAssigment = resourceAllocation
                .getOriginalTotalAssigment();
        return BigDecimal.ONE
                .subtract(
                        consolidatedValue.divide(new BigDecimal(100),
                                RoundingMode.DOWN)).multiply(
                        new BigDecimal(originalTotalAssigment)).intValue();
    }

    public void setPendingConsolidatedHours(Integer pendingConsolidatedHours) {
        this.pendingConsolidatedHours = pendingConsolidatedHours;
    }

    @NotNull(message = "pending consolidated hours not specified")
    public Integer getPendingConsolidatedHours() {
        return pendingConsolidatedHours;
    }

    public void setResourceAllocation(ResourceAllocation<?> resourceAllocation) {
        this.resourceAllocation = resourceAllocation;
    }

    @NotNull(message = "resource allocation not specified")
    public ResourceAllocation<?> getResourceAllocation() {
        return resourceAllocation;
    }

}
