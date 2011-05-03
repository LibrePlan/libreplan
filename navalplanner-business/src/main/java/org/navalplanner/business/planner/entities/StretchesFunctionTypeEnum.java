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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.SplineInterpolator;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.ProportionalDistributor;
import org.navalplanner.business.planner.entities.StretchesFunction.Interval;

/**
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public enum StretchesFunctionTypeEnum {

    STRETCHES {

        @Override
        public void apply(ResourceAllocation<?> allocation,
                List<Interval> intervalsDefinedByStreches,
                LocalDate startInclusive, LocalDate endExclusive,
                int totalHours) {
            Interval.apply(allocation, intervalsDefinedByStreches,
                    startInclusive, endExclusive, totalHours);

        }
    },
    INTERPOLATED {

        @Override
        public void apply(ResourceAllocation<?> allocation,
                List<Interval> intervalsDefinedByStreches,
                LocalDate startInclusive, LocalDate endExclusive,
                int totalHours) {

            double[] x = Interval.getDayPointsFor(startInclusive,
                    intervalsDefinedByStreches);
            assert x.length == 1 + intervalsDefinedByStreches.size();
            double[] y = Interval.getHoursPointsFor(totalHours,
                    intervalsDefinedByStreches);
            assert y.length == 1 + intervalsDefinedByStreches.size();
            int[] hoursForEachDay = hoursForEachDayUsingSplines(x, y,
                    startInclusive, endExclusive);
            int[] reallyAssigned = getReallyAssigned(allocation,
                    startInclusive, hoursForEachDay);
            // Because of calendars, really assigned hours can be less than
            // the hours for each day specified by the interpolation. The
            // remainder must be distributed.
            distributeRemainder(allocation, startInclusive, totalHours,
                    reallyAssigned);

        }

        private int[] getReallyAssigned(ResourceAllocation<?> allocation,
                LocalDate startInclusive, int[] hoursForEachDay) {
            int[] reallyAssigned = new int[hoursForEachDay.length];
            for (int i = 0; i < hoursForEachDay.length; i++) {
                LocalDate day = startInclusive.plusDays(i);
                LocalDate nextDay = day.plusDays(1);
                allocation.withPreviousAssociatedResources()
                        .onIntervalWithinTask(day, nextDay)
                        .allocateHours(hoursForEachDay[i]);
                reallyAssigned[i] = allocation.getAssignedHours(day,
                        nextDay);
            }
            return reallyAssigned;
        }

        private void distributeRemainder(ResourceAllocation<?> allocation,
                LocalDate startInclusive, int totalHours,
                int[] reallyAssigned) {
            final int remainder = totalHours - sum(reallyAssigned);
            if (remainder == 0) {
                return;
            }
            int[] perDay = distributeRemainder(reallyAssigned, remainder);
            for (int i = 0; i < perDay.length; i++) {
                if (perDay[i] == 0) {
                    continue;
                }
                final int newHours = perDay[i] + reallyAssigned[i];
                LocalDate day = startInclusive.plusDays(i);
                LocalDate nextDay = day.plusDays(1);
                allocation.withPreviousAssociatedResources()
                        .onIntervalWithinTask(day, nextDay)
                        .allocateHours(newHours);
            }
        }

        private int sum(int[] array) {
            int result = 0;
            for (int each : array) {
                result += each;
            }
            return result;
        }

        private int[] distributeRemainder(int[] hoursForEachDay,
                int remainder) {
            ProportionalDistributor remainderDistributor = ProportionalDistributor
                    .create(hoursForEachDay);
            return remainderDistributor.distribute(remainder);
        }

    };

    public static int[] hoursForEachDayUsingSplines(double[] x, double[] y,
            LocalDate startInclusive, LocalDate endExclusive) {
        UnivariateRealFunction accumulatingFunction = new SplineInterpolator()
                .interpolate(x, y);
        int[] extractAccumulated = extractAccumulated(accumulatingFunction,
                startInclusive, endExclusive);
        return extractHoursShouldAssignForEachDay(ValleyFiller
                .fillValley(extractAccumulated));
    }

    private static int[] extractAccumulated(
            UnivariateRealFunction accumulatedFunction,
            LocalDate startInclusive, LocalDate endExclusive) {
        int[] result = new int[Days.daysBetween(startInclusive,
                endExclusive).getDays()];
        for (int i = 0; i < result.length; i++) {
            result[i] = evaluate(accumulatedFunction, i + 1);
        }
        return result;
    }

    private static int[] extractHoursShouldAssignForEachDay(
            int[] accumulated) {
        int[] result = new int[accumulated.length];
        int previous = 0;
        for (int i = 0; i < result.length; i++) {
            final int current = accumulated[i];
            result[i] = current - previous;
            previous = current;
        }
        return result;
    }

    private static int evaluate(UnivariateRealFunction accumulatedFunction,
            int x) {
        try {
            return (int) accumulatedFunction.value(x);
        } catch (FunctionEvaluationException e) {
            throw new RuntimeException(e);
        }
    }

    public void applyTo(ResourceAllocation<?> resourceAllocation,
            StretchesFunction stretchesFunction) {

        List<Interval> intervals = new ArrayList<Interval>();
        intervals.addAll(stretchesFunction.getIntervalsDefinedByStreches());

        LocalDate startInclusive = resourceAllocation.getFirstNonConsolidatedDate();
        LocalDate endExclusive = resourceAllocation.getTask().getEndAsLocalDate();
        int totalHours = resourceAllocation.getNonConsolidatedHours();
        apply(resourceAllocation, intervals, startInclusive, endExclusive, totalHours);
    }

    protected abstract void apply(ResourceAllocation<?> allocation,
            List<Interval> intervalsDefinedByStreches,
            LocalDate startInclusive, LocalDate endExclusive, int totalHours);

}
