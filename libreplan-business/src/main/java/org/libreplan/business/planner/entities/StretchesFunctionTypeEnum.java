/*
 * This file is part of LibrePlan
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

package org.libreplan.business.planner.entities;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.SplineInterpolator;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.libreplan.business.common.ProportionalDistributor;
import org.libreplan.business.planner.entities.StretchesFunction.Interval;
import org.libreplan.business.workingday.EffortDuration;

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

            final Task task = allocation.getTask();

            double[] x = Interval.getDayPointsFor(task.getStartAsLocalDate(),
                    intervalsDefinedByStreches);
            assert x.length == 1 + intervalsDefinedByStreches.size();
            double[] y = Interval.getHoursPointsFor(totalHours,
                    intervalsDefinedByStreches);
            assert y.length == 1 + intervalsDefinedByStreches.size();
            int[] hoursForEachDay = hoursForEachDayUsingSplines(x, y,
                    startInclusive, endExclusive);

            Days daysBetween = Days.daysBetween(startInclusive, endExclusive);
            assert hoursForEachDay.length == daysBetween.getDays();

            allocateDaysFrom(allocation, asEffortDuration(hoursForEachDay), startInclusive);
            LocalDate newEndDate = lastDayAssignment(allocation).plusDays(1);

            // Because of calendars, really assigned hours can be less than the
            // hours for each day specified by the interpolation. The remainder
            // must be distributed.
            int[] assignedHours = getAssignedHours(allocation, startInclusive, newEndDate);
            int[] remindingHours = distributeRemainder(allocation, startInclusive, totalHours, assignedHours);
            int[] hoursToAllocate = sum(assignedHours, remindingHours);
            allocateDaysFrom(allocation, asEffortDuration(hoursToAllocate),
                    startInclusive);

            assignedHours = getAssignedHours(allocation, startInclusive, newEndDate);
            Validate.isTrue(sum(assignedHours) == totalHours);
        }

        private int[] sum(int[] assignedHours, int[] remindingHours) {
            Validate.isTrue(assignedHours.length == remindingHours.length);
            for (int i = 0; i < assignedHours.length; i++) {
                assignedHours[i] += remindingHours[i];
            }
            return assignedHours;
        }

        private int[] getAssignedHours(ResourceAllocation<?> allocation,
                LocalDate startInclusive, LocalDate endExclusive) {

            final Days daysBetween = Days.daysBetween(startInclusive, endExclusive);
            int[] result = new int[daysBetween.getDays()];

            LocalDate day = new LocalDate(startInclusive); int i = 0;
            while (day.isBefore(endExclusive)) {
                result[i++] = allocation.getAssignedHours(day, day.plusDays(1));
                day = day.plusDays(1);
            }
            return result;
        }

        private void allocateDaysFrom(ResourceAllocation<?> allocation,
                List<EffortDuration> hoursToAllocate, LocalDate startInclusive) {
            final LocalDate endExclusive = startInclusive.plusDays(hoursToAllocate.size());
            LOG.debug(String.format("allocate on interval (%s, %s): %s", startInclusive, endExclusive, hoursToAllocate));
            allocation.withPreviousAssociatedResources().onInterval(
                    startInclusive, endExclusive).allocate(hoursToAllocate);
        }

        private List<EffortDuration> asEffortDuration(int[] hoursPerDay) {
            List<EffortDuration> result = new ArrayList<EffortDuration>();
            for (int hours: hoursPerDay) {
                result.add(EffortDuration.hours(hours));
            }
            return result;
        }

        private int[] distributeRemainder(ResourceAllocation<?> allocation,
                LocalDate startInclusive, int totalHours,
                int[] reallyAssigned) {
            final int remainder = totalHours - sum(reallyAssigned);
            if (remainder == 0) {
                return new int[reallyAssigned.length];
            }
            return distributeRemainder(reallyAssigned, remainder);
        }

        private int[] distributeRemainder(int[] hoursForEachDay,
                int remainder) {
            ProportionalDistributor remainderDistributor = ProportionalDistributor
                    .create(hoursForEachDay);
            return remainderDistributor.distribute(remainder);
        }

        private int sum(int[] array) {
            int result = 0;
            for (int each : array) {
                result += each;
            }
            return result;
        }

        private LocalDate lastDayAssignment(ResourceAllocation<?> allocation) {
            List<DayAssignment> assignments = (List<DayAssignment>) allocation.getAssignments();
            DayAssignment last = assignments.get(assignments.size() - 1);
            return last.getDay();
        }

    };

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(StretchesFunctionTypeEnum.class);

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
        LocalDate endExclusive = resourceAllocation.getIntraDayEndDate()
                .asExclusiveEnd();
        int totalHours = resourceAllocation.getNonConsolidatedHours();
        apply(resourceAllocation, intervals, startInclusive, endExclusive, totalHours);
    }

    protected abstract void apply(ResourceAllocation<?> allocation,
            List<Interval> intervalsDefinedByStreches,
            LocalDate startInclusive, LocalDate endExclusive, int totalHours);

}
