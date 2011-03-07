/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.SplineInterpolator;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Valid;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.ProportionalDistributor;

/**
 * Assignment function by stretches.
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class StretchesFunction extends AssignmentFunction {

    public enum Type {
        DEFAULT {
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
            List<Interval> intervalsDefinedByStreches = stretchesFunction
                    .getIntervalsDefinedByStreches();
            int totalHours = resourceAllocation.getAssignedHours();
            Task task = resourceAllocation.getTask();
            LocalDate start = LocalDate.fromDateFields(task.getStartDate());
            LocalDate end = LocalDate.fromDateFields(task.getEndDate());
            apply(resourceAllocation, intervalsDefinedByStreches, start, end,
                    totalHours);
        }

        protected abstract void apply(ResourceAllocation<?> allocation,
                List<Interval> intervalsDefinedByStreches,
                LocalDate startInclusive, LocalDate endExclusive, int totalHours);
    }

    public static class Interval {

        private LocalDate start;

        private LocalDate end;

        private final BigDecimal loadProportion;

        public Interval(BigDecimal loadProportion, LocalDate start,
                LocalDate end) {
            Validate.notNull(loadProportion);
            Validate.isTrue(loadProportion.signum() >= 0);
            Validate.notNull(end);
            this.loadProportion = loadProportion.setScale(2,
                    RoundingMode.HALF_UP);
            this.start = start;
            this.end = end;
        }

        public static double[] getHoursPointsFor(int totalHours,
                List<Interval> intervalsDefinedByStreches) {
            double[] result = new double[intervalsDefinedByStreches.size() + 1];
            int i = 1;
            result[0] = 0;
            int accumulated = 0;
            for (Interval each : intervalsDefinedByStreches) {
                accumulated += each.getHoursFor(totalHours);
                result[i++] = accumulated;
            }
            return result;
        }

        public static double[] getDayPointsFor(LocalDate start,
                List<Interval> intervalsDefinedByStreches) {
            double[] result = new double[intervalsDefinedByStreches.size() + 1];
            result[0] = 0;
            int i = 1;
            for (Interval each : intervalsDefinedByStreches) {
                result[i++] = Days.daysBetween(start, each.getEnd()).getDays();
            }
            return result;
        }

        public LocalDate getEnd() {
            return end;
        }

        public BigDecimal getLoadProportion() {
            return loadProportion;
        }

        public boolean hasNoStart() {
            return start == null;
        }

        public LocalDate getStart() {
            return start;
        }

        public int getHoursFor(int totalHours) {
            return loadProportion.multiply(new BigDecimal(totalHours))
                    .intValue();
        }

        public LocalDate getStartFor(LocalDate allocationStart) {
            return hasNoStart() ? allocationStart : start;
        }

        private void apply(ResourceAllocation<?> resourceAllocation,
                LocalDate startInclusive, LocalDate taskEnd,
                int intervalHours) {
            // End has to be exclusive on last Stretch
            LocalDate endDate = getEnd();
            if (endDate.equals(taskEnd)) {
                endDate = endDate.plusDays(1);
            }
            resourceAllocation.withPreviousAssociatedResources()
                    .onIntervalWithinTask(getStartFor(startInclusive), endDate)
                    .allocateHours(intervalHours);
        }

        public static void apply(ResourceAllocation<?> allocation,
                List<Interval> intervalsDefinedByStreches,
                LocalDate allocationStart, LocalDate allocationEnd,
                int totalHours) {
            if (intervalsDefinedByStreches.isEmpty()) {
                return;
            }
            int[] hoursPerInterval = getHoursPerInterval(
                    intervalsDefinedByStreches, totalHours);
            int remainder = totalHours - sum(hoursPerInterval);
            hoursPerInterval[0] += remainder;
            int i = 0;
            for (Interval interval : intervalsDefinedByStreches) {
                interval.apply(allocation, allocationStart, allocationEnd,
                        hoursPerInterval[i++]);
            }
            Validate.isTrue(totalHours == allocation.getAssignedHours());
        }

        private static int[] getHoursPerInterval(
                List<Interval> intervalsDefinedByStreches, int totalHours) {
            int[] hoursPerInterval = new int[intervalsDefinedByStreches.size()];
            int i = 0;
            for (Interval each : intervalsDefinedByStreches) {
                hoursPerInterval[i++] = each.getHoursFor(totalHours);
            }
            return hoursPerInterval;
        }

        private static int sum(int[] hoursPerInterval) {
            int result = 0;
            for (int each : hoursPerInterval) {
                result += each;
            }
            return result;
        }

    }

    private static int sum(int[] array) {
        int result = 0;
        for (int each : array) {
            result += each;
        }
        return result;
    }

    public static StretchesFunction create() {
        return (StretchesFunction) create(new StretchesFunction());
    }

    public static List<Interval> intervalsFor(
            Collection<? extends Stretch> streches) {
        ArrayList<Interval> result = new ArrayList<Interval>();
        LocalDate previous = null;
        BigDecimal sumOfProportions = new BigDecimal(0);
        for (Stretch each : streches) {
            LocalDate strechDate = each.getDate();
            result.add(new Interval(each.getAmountWorkPercentage().subtract(
                    sumOfProportions), previous,
                    strechDate));
            sumOfProportions = each.getAmountWorkPercentage();
            previous = strechDate;
        }
        return result;
    }

    private static <T> T last(List<? extends T> list) {
        return list.get(list.size() - 1);
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public StretchesFunction() {
    }

    private List<Stretch> stretches = new ArrayList<Stretch>();

    private Type type;

    /**
     * This is a transient field. Not stored
     */
    private Type desiredType;

    public StretchesFunction copy() {
        StretchesFunction result = StretchesFunction.create();
        result.resetToStrechesFrom(this);
        result.type = type;
        result.desiredType = desiredType;
        return result;
    }

    public void resetToStrechesFrom(StretchesFunction from) {
        this.removeAllStretches();
        for (Stretch each : from.getStretches()) {
            Stretch newStretch = new Stretch();
            newStretch.setDate(each.getDate());
            newStretch.setLengthPercentage(each.getLengthPercentage());
            newStretch.setAmountWorkPercentage(each
                    .getAmountWorkPercentage());
            this.addStretch(newStretch);
        }
    }

    public void setStretches(List<Stretch> stretches) {
        this.stretches = stretches;
    }

    private void sortStretches() {
        Collections.sort(stretches, new Comparator<Stretch>() {
            @Override
            public int compare(Stretch o1, Stretch o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
    }

    @Valid
    public List<Stretch> getStretches() {
        sortStretches();
        return Collections.unmodifiableList(stretches);
    }

    public Type getType() {
        return type == null ? Type.DEFAULT : type;
    }

    public Type getDesiredType() {
        return desiredType == null ? getType() : desiredType;
    }

    public void changeTypeTo(Type type) {
        desiredType = type;
    }

    public void addStretch(Stretch stretch) {
        stretches.add(stretch);
    }

    public void removeStretch(Stretch stretch) {
        stretches.remove(stretch);
    }

    public void removeAllStretches() {
        stretches.clear();
    }

    @AssertTrue(message = "At least one stretch is needed")
    public boolean checkNoEmpty() {
        return !stretches.isEmpty();
    }

    @AssertTrue(message = "Some stretch has higher or equal values than the "
            + "previous stretch")
    public boolean checkStretchesOrder() {
        if (stretches.isEmpty()) {
            return false;
        }

        sortStretches();

        Iterator<Stretch> iterator = stretches.iterator();
        Stretch previous = iterator.next();
        while (iterator.hasNext()) {
            Stretch current = iterator.next();
            if (current.getDate().compareTo(previous.getDate()) <= 0) {
                return false;
            }
            if (current.getLengthPercentage().compareTo(
                    previous.getLengthPercentage()) <= 0) {
                return false;
            }
            if (current.getAmountWorkPercentage().compareTo(
                    previous.getAmountWorkPercentage()) <= 0) {
                return false;
            }
            previous = current;
        }
        return true;
    }

    @AssertTrue(message = "Last stretch should have one hundred percent for "
            + "length and amount of work percentage")
    public boolean checkOneHundredPercent() {
        if (stretches.isEmpty()) {
            return false;
        }
        sortStretches();

        Stretch lastStretch = stretches.get(stretches.size() - 1);
        if (lastStretch.getLengthPercentage().compareTo(BigDecimal.ONE) != 0) {
            return false;
        }
        if (lastStretch.getAmountWorkPercentage().compareTo(BigDecimal.ONE) != 0) {
            return false;
        }

        return true;
    }

    @Override
    public void applyTo(ResourceAllocation<?> resourceAllocation) {
        if (!resourceAllocation.hasAssignments()) {
            return;
        }
        getDesiredType().applyTo(resourceAllocation, this);
        type = getDesiredType();
    }

    @Override
    public String getName() {
        if (StretchesFunction.Type.INTERPOLATED.equals(type)) {
            return ASSIGNMENT_FUNCTION_NAME.INTERPOLATION.toString();
        } else {
            return ASSIGNMENT_FUNCTION_NAME.STRETCHES.toString();
        }
    }

    public List<Interval> getIntervalsDefinedByStreches() {
        if (stretches.isEmpty()) {
            return Collections.emptyList();
        }
        List<Interval> result = intervalsFor(stretches);
        BigDecimal sumOfProportions = stretches.isEmpty() ? BigDecimal.ZERO
                : last(stretches).getAmountWorkPercentage();
        BigDecimal left = calculateLeftFor(sumOfProportions);
        if (!left.equals(BigDecimal.ZERO)) {
            throw new IllegalStateException("the streches must sum the 100%");
        }
        return result;
    }

    private BigDecimal calculateLeftFor(BigDecimal sumOfProportions) {
        BigDecimal left = BigDecimal.ONE.subtract(sumOfProportions);
        left = left.signum() <= 0 ? BigDecimal.ZERO : left;
        return left;
    }

    public boolean ifInterpolatedMustHaveAtLeastTwoStreches() {
        return getDesiredType() != Type.INTERPOLATED || stretches.size() >= 2;
    }

}
