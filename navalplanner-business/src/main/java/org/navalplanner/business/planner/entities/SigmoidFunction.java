/*
 * This file is part of NavalPlan
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
package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;

/**
 *
 * @author Diego Pino Garcia<dpino@igalia.com>
 *
 */
public class SigmoidFunction extends AssignmentFunction {

    private static final int PRECISSION = 6;

    private static final int ROUND_MODE = BigDecimal.ROUND_HALF_EVEN;

    // Fragmentation of hours (0.25, 0.50, 0.75, 1). 1 indicates no fragmentation
    private static final BigDecimal HOUR_FRAGMENTATION = BigDecimal.valueOf(1);

    public static SigmoidFunction create() {
        return create(new SigmoidFunction());
    }

    protected SigmoidFunction() {

    }

    @Override
    public String getName() {
        return ASSIGNMENT_FUNCTION_NAME.SIGMOID.toString();
    }

    @Override
    public void applyTo(ResourceAllocation<?> resourceAllocation) {
        final Task task = resourceAllocation.getTask();
        final int totalHours = resourceAllocation.getAssignedHours();
        apply(resourceAllocation, task.getStartAsLocalDate(), task.getEndAsLocalDate(), totalHours);
    }

    private void apply(ResourceAllocation<?> resourceAllocation,
            LocalDate start, LocalDate end, int totalHours) {

        final LocalDate previousEndDate = resourceAllocation.getEndDate();

        EffortDuration capacity;
        BaseCalendar calendar = resourceAllocation.getTask().getCalendar();
        int daysDuration = daysWithAllocatedHours(resourceAllocation).size();

        // Calculate hours per day and round values (take only integer part)
        BigDecimal[] hoursToAllocatePerDay = generateHoursToAllocateFor(daysDuration, totalHours);
        hoursToAllocatePerDay = roundValues(hoursToAllocatePerDay, HOUR_FRAGMENTATION);

        // Calculate reminder (difference between totalHours and sum of hours calculated)
        BigDecimal totalHoursToAllocate = sumHoursPerDay(hoursToAllocatePerDay);
        assert(totalHoursToAllocate.compareTo(BigDecimal.valueOf(totalHours)) <= 0);
        BigDecimal remindingHours = BigDecimal.valueOf(totalHours).subtract(totalHoursToAllocate);
        allocateRemindingHours(hoursToAllocatePerDay, remindingHours);
        avoidZeroHoursInDays(hoursToAllocatePerDay);

        assert(hoursToAllocatePerDay.length == daysDuration);

        // Starting from startDate do allocation, one slot of hours per day in resource
        LocalDate day = new LocalDate(start);
        int hours = 0, i = 0;
        while (i < hoursToAllocatePerDay.length) {
            hours = hoursToAllocatePerDay[i].intValue();
            capacity = calendar.getCapacityOn(PartialDay.wholeDay(day));
            if (!EffortDuration.zero().equals(capacity)) {
                allocate(resourceAllocation, day, hours);
                i++;
            }
            day = day.plusDays(1);
        }
        Validate.isTrue(resourceAllocation.getEndDate().equals(previousEndDate));
    }

    private List<BigDecimal> daysWithAllocatedHours(
            ResourceAllocation<?> resourceAllocation) {

        List<BigDecimal> result = new ArrayList<BigDecimal>();
        LocalDate day = new LocalDate(resourceAllocation.getStartDate());
        final LocalDate end = resourceAllocation.getEndDate();
        int i = 0;

        while (day.isBefore(end)) {
            int hoursAllocated = resourceAllocation.getAssignedHours(day, day.plusDays(1));
            if (hoursAllocated != 0) {
                result.add(new BigDecimal(hoursAllocated));
                i++;
            }
            day = day.plusDays(1);
        }
        return result;
    }

    /**
     * Days with zero hours can occur at the beginning days.
     *
     * To avoid allocating days with zero hours, we iterate through the days and
     * subtract a day from the next day to the current day, until we come up
     * with a day which is no zero
     *
     * @param hoursToAllocatePerDay
     */
    private void avoidZeroHoursInDays(BigDecimal[] hoursToAllocatePerDay) {
        int length = hoursToAllocatePerDay.length;
        for (int i = 0; i < length; i++) {
            BigDecimal hours = hoursToAllocatePerDay[i];
            if (hours.doubleValue() != 0) {
                return;
            }
            if (i + 1 <= length) {
                BigDecimal next = hoursToAllocatePerDay[i + 1];
                hoursToAllocatePerDay[i + 1] = next.subtract(BigDecimal.ONE);
                hoursToAllocatePerDay[i] = hours.add(BigDecimal.ONE);
            }
        }
    }

    private void allocateRemindingHours(BigDecimal[] hoursToAllocatePerDay, BigDecimal remindingHours) {
        final int length = hoursToAllocatePerDay.length;

        // Add reminding hours to best fit in a way that the distribution of
        // hours grows continuously
        for (int i = 0; i < length - 1; i++) {
            BigDecimal current = hoursToAllocatePerDay[i];
            BigDecimal next = hoursToAllocatePerDay[i+1];

            if (current.add(remindingHours).compareTo(next) <= 0) {
                hoursToAllocatePerDay[i] = current.add(remindingHours);
                return;
            }
        }

        // Add reminding hours to last day
        BigDecimal lastDay = hoursToAllocatePerDay[length - 1];
        hoursToAllocatePerDay[length - 1] = lastDay.add(remindingHours);
    }

    private void showSamplePointValues(BigDecimal[] samplePointValues) {
        for (int i = 0; i < samplePointValues.length; i++) {
            System.out.println(String.format("%d: %f", i, samplePointValues[i]));
        }
     }

    private void allocate(ResourceAllocation<?> resourceAllocation,
            LocalDate day, int hours) {
        final LocalDate nextDay = day.plusDays(1);
        resourceAllocation.withPreviousAssociatedResources()
                .onInterval(day, nextDay).allocateHours(hours);
    }

    private BigDecimal[] roundValues(BigDecimal[] allocatedHoursPerDay,
            BigDecimal truncateValue) {

        BigDecimal[] result = new BigDecimal[allocatedHoursPerDay.length];
        BigDecimal reminder = BigDecimal.ZERO;

        for (int i = 0; i < result.length; i++) {
            BigDecimal value = allocatedHoursPerDay[i];
            value = value.add(reminder);

            BigDecimal intPart = intPart(value);
            BigDecimal decimalPart = decimalPart(value);
            reminder = calculateReminder(decimalPart, truncateValue);
            decimalPart = decimalPart.subtract(reminder);
            result[i] = intPart.add(decimalPart);
        }
        return result;
    }

    private BigDecimal calculateReminder(BigDecimal decimalPart,
            BigDecimal truncateValue) {
        BigDecimal[] result = decimalPart.divideAndRemainder(truncateValue);
        return result[1];
    }

    private BigDecimal intPart(BigDecimal bd) {
        return BigDecimal.valueOf(bd.intValue());
    }

    private BigDecimal decimalPart(BigDecimal bd) {
        return bd.subtract(intPart(bd));
    }

    private BigDecimal sumHoursPerDay(BigDecimal[] hoursPerDay) {
        BigDecimal result = BigDecimal.ZERO;
        for (int i = 0; i < hoursPerDay.length; i++) {
            result = result.add(hoursPerDay[i]);
        }
        return result;
    }

    private BigDecimal[] generateHoursToAllocateFor(int days, int hours) {
        BigDecimal[] valuesPerDay = generatePointValuesForDays(days);
        BigDecimal[] acummulatedHoursPerDay = calculateNumberOfAccumulatedHoursForDays(valuesPerDay, hours);
        BigDecimal[] allocatedHoursPerDay = calculateNumberOfAllocatedHoursForDays(acummulatedHoursPerDay);
        return allocatedHoursPerDay;
    }

    private BigDecimal[] generatePointValuesForDays(int days) {
        final BigDecimal dayIntervalConstant = getDayIntervalConstant(days);

        BigDecimal[] result = new BigDecimal[days];
        for (int i = 0; i < days; i++) {
            result[i] = BigDecimal.valueOf(-6)
                .add(dayIntervalConstant.multiply(BigDecimal.valueOf(i)));
        }
        return result;
    }

    private BigDecimal[] calculateNumberOfAllocatedHoursForDays(BigDecimal[] acummulatedHoursPerDay) {
        BigDecimal[] result = new BigDecimal[acummulatedHoursPerDay.length];

        result[0] = acummulatedHoursPerDay[0];
        for (int i = 1; i < result.length; i++) {
            result[i] = acummulatedHoursPerDay[i].subtract(acummulatedHoursPerDay[i - 1]);
        }
        return result;
    }

    private BigDecimal[] calculateNumberOfAccumulatedHoursForDays(
            BigDecimal[] dayValues, int totalHours) {
        BigDecimal[] result = new BigDecimal[dayValues.length];
        for (int i = 0; i < dayValues.length; i++) {
            result[i] = calculateNumberOfAccumulatedHoursAtDay(dayValues[i], totalHours);
        }
        return result;
    }

    private BigDecimal calculateNumberOfAccumulatedHoursAtDay(
            BigDecimal valueAtOneDay, int totalHours) {
        BigDecimal epow = BigDecimal.valueOf(Math.pow(Math.E, valueAtOneDay
                .negate().doubleValue()));
        BigDecimal denominator = BigDecimal.valueOf(1).add(epow);
        return BigDecimal.valueOf(totalHours).divide(denominator,
                PRECISSION, ROUND_MODE);
    }

    // 12 divide by days
    private BigDecimal getDayIntervalConstant(int days) {
        return BigDecimal.valueOf(12).divide(BigDecimal.valueOf(days),
                PRECISSION, ROUND_MODE);
    }

}
