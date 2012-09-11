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

package org.libreplan.business.reports.dtos;

import org.apache.commons.lang.StringUtils;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.SumChargedEffort;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.workingday.EffortDuration;

/**
 * DTO to represent each row in the Project Status report.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class ProjectStatusReportDTO {

    private String code;

    private String name;

    private EffortDuration estimatedHours;

    private EffortDuration plannedHours;

    private EffortDuration imputedHours;

    public ProjectStatusReportDTO(OrderElement orderElement) {
        code = orderElement.getCode();
        name = orderElement.getName();

        Integer estimatedHours = orderElement.getWorkHours();
        this.estimatedHours = estimatedHours != null ? EffortDuration
                .hours(estimatedHours) : null;

        TaskSource taskSource = orderElement.getTaskSource();
        if (taskSource != null) {
            plannedHours = taskSource.getTask().getSumOfAssignedEffort();
        }

        SumChargedEffort sumChargedEffort = orderElement.getSumChargedEffort();
        if (sumChargedEffort != null) {
            imputedHours = sumChargedEffort.getTotalChargedEffort();
        }

        appendPrefixSpacesDependingOnDepth(orderElement);
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getEstimatedHours() {
        return toString(estimatedHours);
    }

    public String getPlannedHours() {
        return toString(plannedHours);
    }

    public String getImputedHours() {
        return toString(imputedHours);
    }

    private String toString(EffortDuration effortDuration) {
        if (effortDuration == null) {
            return null;
        }
        return effortDuration.toFormattedString();
    }

    private void appendPrefixSpacesDependingOnDepth(OrderElement orderElement) {
        int depth = 0;
        while (!orderElement.getParent().isOrder()) {
            depth++;
            orderElement = orderElement.getParent();
        }

        name = StringUtils.repeat("   ", depth) + name;
    }

}