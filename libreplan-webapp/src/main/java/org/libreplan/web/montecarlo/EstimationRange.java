/*
 * This file is part of LibrePlan
 *
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
package org.libreplan.web.montecarlo;

import java.math.BigDecimal;

/**
 *
 * @author Diego Pino Garcia<dpino@igalia.com>
 *
 */
public class EstimationRange {

    BigDecimal min;
    BigDecimal max;
    ESTIMATION_TYPE estimationType;

    public static EstimationRange optimisticRangeFor(MonteCarloTask task) {
        return new EstimationRange(
                task.getOptimisticDurationPercentageLowerLimit(),
                task.getOptimisticDurationPercentageUpperLimit(),
                ESTIMATION_TYPE.OPTIMISTIC);
    }

    public static EstimationRange normalRangeFor(MonteCarloTask task) {
        return new EstimationRange(
                task.getNormalDurationPercentageLowerLimit(),
                task.getNormalDurationPercentageUpperLimit(),
                ESTIMATION_TYPE.NORMAL);
    }

    public static EstimationRange pessimisticRangeFor(MonteCarloTask task) {
        return new EstimationRange(
                task.getPessimisticDurationPercentageLowerLimit(),
                task.getPessimisticDurationPercentageUpperLimit(),
                ESTIMATION_TYPE.PESSIMISTIC);
    }

    public EstimationRange(BigDecimal min, BigDecimal max,
            ESTIMATION_TYPE estimationType) {
        this.min = min;
        this.max = max;
        this.estimationType = estimationType;
    }

    public boolean contains(BigDecimal value) {
        return (value.compareTo(min) >= 0 && value.compareTo(max) <= 0);
    }

    public ESTIMATION_TYPE getEstimationType() {
        return estimationType;
    }

}

enum ESTIMATION_TYPE {
    PESSIMISTIC, NORMAL, OPTIMISTIC;
}
