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

package org.libreplan.business.planner.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         Calculates generic 'Earned Value' indicators (those calculated out of
 *         BCWP, ACWP and BCWS
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EarnedValueCalculator implements IEarnedValueCalculator {

    @Override
    public SortedMap<LocalDate, BigDecimal> calculateCostVariance(
            SortedMap<LocalDate, BigDecimal> bcwp,
            SortedMap<LocalDate, BigDecimal> acwp) {
        return substract(bcwp, acwp);
    }

    @Override
    public SortedMap<LocalDate, BigDecimal> calculateScheduleVariance(
            SortedMap<LocalDate, BigDecimal> bcwp,
            SortedMap<LocalDate, BigDecimal> bcws) {
        return substract(bcwp, bcws);
    }

    @Override
    public SortedMap<LocalDate, BigDecimal> calculateBudgetAtCompletion(
            SortedMap<LocalDate, BigDecimal> bcws) {
        SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();
        BigDecimal value = Collections.max(bcws.values());
        for (LocalDate day : bcws.keySet()) {
            result.put(day, value);
        }
        return result;
    }

    @Override
    public SortedMap<LocalDate, BigDecimal> calculateEstimateAtCompletion(
            SortedMap<LocalDate, BigDecimal> acwp,
            SortedMap<LocalDate, BigDecimal> bcwp,
            SortedMap<LocalDate, BigDecimal> bac) {
        return multiply(divide(acwp, bcwp,
                BigDecimal.ZERO), bac);
    }

    @Override
    public SortedMap<LocalDate, BigDecimal> calculateVarianceAtCompletion(
            SortedMap<LocalDate, BigDecimal> bac,
            SortedMap<LocalDate, BigDecimal> eac) {
        return substract(bac, eac);
    }

    @Override
    public SortedMap<LocalDate, BigDecimal> calculateEstimatedToComplete(
            SortedMap<LocalDate, BigDecimal> eac,
            SortedMap<LocalDate, BigDecimal> acwp) {
        return substract(eac, acwp);
    }

    @Override
    public SortedMap<LocalDate, BigDecimal> calculateCostPerformanceIndex(
            SortedMap<LocalDate, BigDecimal> bcwp,
            SortedMap<LocalDate, BigDecimal> acwp) {
        return divide(bcwp, acwp, BigDecimal.ZERO);
    }

    @Override
    public SortedMap<LocalDate, BigDecimal> calculateSchedulePerformanceIndex(
            SortedMap<LocalDate, BigDecimal> bcwp,
            SortedMap<LocalDate, BigDecimal> bcws) {
        return divide(bcwp, bcws, BigDecimal.ZERO);
    }

    private SortedMap<LocalDate, BigDecimal> substract(
            SortedMap<LocalDate, BigDecimal> minuend,
            SortedMap<LocalDate, BigDecimal> subtrahend) {
        final SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();
        forValuesAtSameKey(minuend, subtrahend, substractionOperation(result));
        return result;

    }

    public static <K, V> void forValuesAtSameKey(Map<K, V> a, Map<K, V> b,
            IOperation<K, V> onSameKey) {
        for (Entry<K, V> each : a.entrySet()) {
            V aValue = each.getValue();
            V bValue = b.get(each.getKey());
            onSameKey.operate(each.getKey(), aValue, bValue);
        }
    }

    private static IOperation<LocalDate, BigDecimal> substractionOperation(
            final SortedMap<LocalDate, BigDecimal> result) {
        return notNullOperands(new IOperation<LocalDate, BigDecimal>() {

            @Override
            public void operate(LocalDate key, BigDecimal minuedValue,
                    BigDecimal subtrahendValue) {
                result.put(key, minuedValue.subtract(subtrahendValue));
            }

            @Override
            public void undefinedFor(LocalDate key) {
            }
        });
    }

    public static <K, V> IOperation<K, V> notNullOperands(
            final IOperation<K, V> operation) {
        return new PreconditionChecker<K, V>(operation) {
            @Override
            protected boolean isOperationDefinedFor(K key, V a, V b) {
                return a != null && b != null;
            }
        };
    }

    public interface IOperation<K, V> {

        public void operate(K key, V a, V b);

        public void undefinedFor(K key);
    }

    protected static abstract class PreconditionChecker<K, V> implements
            IOperation<K, V> {

        private final IOperation<K, V> decorated;

        protected PreconditionChecker(IOperation<K, V> decorated) {
            this.decorated = decorated;
        }

        @Override
        public void operate(K key, V a, V b) {
            if (isOperationDefinedFor(key, a, b)) {
                decorated.operate(key, a, b);
            } else {
                decorated.undefinedFor(key);
            }
        }

        protected abstract boolean isOperationDefinedFor(K key, V a, V b);

        @Override
        public void undefinedFor(K key) {
            decorated.undefinedFor(key);
        }

    }

    private static SortedMap<LocalDate, BigDecimal> multiply(
            Map<LocalDate, BigDecimal> firstFactor,
            Map<LocalDate, BigDecimal> secondFactor) {
        final SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();
        forValuesAtSameKey(firstFactor, secondFactor,
                multiplicationOperation(result));
        return result;
    }

    private static IOperation<LocalDate, BigDecimal> multiplicationOperation(
            final SortedMap<LocalDate, BigDecimal> result) {
        return notNullOperands(new IOperation<LocalDate, BigDecimal>() {

            @Override
            public void operate(LocalDate key, BigDecimal a,
                    BigDecimal b) {
                result.put(key, a.multiply(b));
            }

            @Override
            public void undefinedFor(LocalDate key) {
                result.put(key, BigDecimal.ZERO);
            }
        });
    }

    private static SortedMap<LocalDate, BigDecimal> divide(
            Map<LocalDate, BigDecimal> dividend,
            Map<LocalDate, BigDecimal> divisor,
            final BigDecimal defaultIfNotComputable) {
        final TreeMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();
        forValuesAtSameKey(dividend, divisor,
                divisionOperation(result, defaultIfNotComputable));
        return result;
    }

    private static IOperation<LocalDate, BigDecimal> divisionOperation(
            final TreeMap<LocalDate, BigDecimal> result,
            final BigDecimal defaultIfNotComputable) {
        return notNullOperands(secondOperandNotZero(new IOperation<LocalDate, BigDecimal>() {

            @Override
            public void operate(LocalDate key, BigDecimal dividendValue,
                    BigDecimal divisorValue) {
                result.put(key,
                        dividendValue.divide(divisorValue, RoundingMode.DOWN));
            }

            @Override
            public void undefinedFor(LocalDate key) {
                result.put(key, defaultIfNotComputable);
            }
        }));
    }

    public static <K> IOperation<K, BigDecimal> secondOperandNotZero(
            final IOperation<K, BigDecimal> operation) {
        return new PreconditionChecker<K, BigDecimal>(operation) {
            @Override
            protected boolean isOperationDefinedFor(K key, BigDecimal a,
                    BigDecimal b) {
                return b.signum() != 0;
            }
        };
    }

}