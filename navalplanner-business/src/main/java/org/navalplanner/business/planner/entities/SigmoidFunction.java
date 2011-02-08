package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;

import org.joda.time.Days;
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
        LocalDate start = LocalDate.fromDateFields(task.getStartDate());
        LocalDate end = LocalDate.fromDateFields(task.getEndDate());
        int totalHours = resourceAllocation.getAssignedHours();

        apply(resourceAllocation, start, end, totalHours);
    }

    private void apply(ResourceAllocation<?> resourceAllocation,
            LocalDate start, LocalDate end, int totalHours) {

        System.out.println("### SigmoidFunction.apply: " + start + "; " + end + "; " + totalHours);

        final int daysDuration = Days.daysBetween(start, end).getDays();
        EffortDuration capacity;

        // Calculate hours per day and round values (take only integer part)
        BigDecimal[] hoursToAllocatePerDay = generateHoursToAllocateFor(daysDuration, totalHours);
        hoursToAllocatePerDay = roundValues(hoursToAllocatePerDay, HOUR_FRAGMENTATION);

        // Calculate reminder (difference between totalHours and sum of hours calculated)
        BigDecimal totalHoursToAllocate = sumHoursPerDay(hoursToAllocatePerDay);
        assert(totalHoursToAllocate.compareTo(BigDecimal.valueOf(totalHours)) <= 0);
        BigDecimal remindingHours = BigDecimal.valueOf(totalHours).subtract(totalHoursToAllocate);
        allocateRemindingHours(hoursToAllocatePerDay, remindingHours);

        // Starting from startDate do allocation, one slot of hours per day in resource
        BaseCalendar calendar = resourceAllocation.getTask().getCalendar();
        LocalDate day = new LocalDate(start);
        int hours = 0, i = 0;
        for (; i < hoursToAllocatePerDay.length;) {
            hours = hoursToAllocatePerDay[i].intValue();
            capacity = calendar.getCapacityOn(PartialDay.wholeDay(day));
            if (!EffortDuration.zero().equals(capacity)) {
                allocate(resourceAllocation, day, hours);
                i++;
            }
            day = day.plusDays(1);
        }

        clearPreviouslyAllocatedDays(resourceAllocation, day, end);
    }

    private void clearPreviouslyAllocatedDays(
            ResourceAllocation<?> resourceAllocation, LocalDate start,
            LocalDate end) {

        while (start.isBefore(end) || start.isEqual(end)) {
            allocate(resourceAllocation, start, 0);
            start = start.plusDays(1);
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
                .onIntervalWithinTask(day, nextDay).allocateHours(hours);
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

    private BigDecimal[] generatePointValuesForDays(int days) {
        final BigDecimal dayIntervalConstant = getDayIntervalConstant(days);

        BigDecimal[] result = new BigDecimal[days];
        for (int i = 0; i < days; i++) {
            result[i] = BigDecimal.valueOf(-6)
                .add(dayIntervalConstant.multiply(BigDecimal.valueOf(i)));
        }
        return result;
    }

    // 12 divide by days
    private BigDecimal getDayIntervalConstant(int days) {
        return BigDecimal.valueOf(12).divide(BigDecimal.valueOf(days),
                PRECISSION, ROUND_MODE);
    }

}
