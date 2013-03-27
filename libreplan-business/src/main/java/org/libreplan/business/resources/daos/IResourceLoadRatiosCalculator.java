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

package org.libreplan.business.resources.daos;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.workingday.EffortDuration;

/**
 * Specifies the method to calculate the load ratios of a resource and the data
 * type which represents the output
 *
 * @author Javier Moran Rua <jmoran@igalia.com>
 *
 */
public interface IResourceLoadRatiosCalculator {

    public interface ILoadRatiosDataType {

        EffortDuration getLoad();

        EffortDuration getOverload();

        EffortDuration getCapacity();

        /**
         * Calculates the overtime ratio. The overtime ratio is defined as
         * overload / (load+overload).
         *
         * @return the overtime ratio represented with a {@link BigDecimal} with
         *         scale of 2. If both load and overload are zero it is returned
         *         zero.
         */
        BigDecimal getOvertimeRatio();

        /**
         * Calculates the availability ratio. The availability ratio is defined
         * as 1 - (load/capacity)*100. It is a percentage.
         *
         * @return the availability ratio represented with a {@link BigDecimal}
         *         with a scale of 2. In the case that the capacity is zero, a
         *         0% of availability is returned.
         */
        BigDecimal getAvailiabilityRatio();
    }

    /**
     * Calculates the load ratios of a resource between two dates in the
     * escenario specified.
     *
     * @param resource
     * @param startDate
     * @param endDate
     * @return the load ratios calculated.
     */
    ILoadRatiosDataType calculateLoadRatios(Resource resource,
            LocalDate startDate,
            LocalDate endDate, Scenario scenario);

}
