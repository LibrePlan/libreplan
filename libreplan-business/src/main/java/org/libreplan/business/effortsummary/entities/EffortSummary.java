/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.business.effortsummary.entities;

import org.joda.time.LocalDate;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.resources.entities.Resource;

public class EffortSummary extends BaseEntity {

    private LocalDate startDate;

    private LocalDate endDate;

    private int[] availableEffort;

    private int[] assignedEffort;

    private Resource resource;

    public static EffortSummary create(LocalDate startDate, LocalDate endDate,
            int[] availableEffort, int[] assignedEffort,
            Resource resource) {
        EffortSummary newObject = new EffortSummary();
        newObject.setStartDate(startDate);
        newObject.setEndDate(endDate);
        newObject.setAvailableEffort(availableEffort);
        newObject.setAssignedEffort(assignedEffort);
        newObject.setResource(resource);
        return create(newObject);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public int[] getAvailableEffort() {
        return availableEffort;
    }

    public void setAvailableEffort(int[] availableEffort) {
        this.availableEffort = availableEffort;
    }

    public int[] getAssignedEffort() {
        return assignedEffort;
    }

    public void setAssignedEffort(int[] assignedEffort) {
        this.assignedEffort = assignedEffort;
    }

}
