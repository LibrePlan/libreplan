package org.navalplanner.web.montecarlo;

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
