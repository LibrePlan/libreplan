/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 Igalia, S.L.
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
package org.libreplan.business.recurring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePeriod;
import org.libreplan.business.planner.entities.ResourceAllocation.Direction;

/**
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 *
 */
public class RecurrenceInformation {

    public static RecurrenceInformation noRecurrence() {
        return endAtNumberOfRepetitions(0, RecurrencePeriodicity.NO_PERIODICTY, 0);
    }

    public static RecurrenceInformation endAtNumberOfRepetitions(
            int numberRepetitions, RecurrencePeriodicity recurrencePeriodicity,
            int amountOfPeriodsPerRepetition) {
        return new RecurrenceInformation(numberRepetitions, null,
                recurrencePeriodicity, amountOfPeriodsPerRepetition, null);
    }

    public static RecurrenceInformation endBy(LocalDate endBy,
            RecurrencePeriodicity recurrencePeriodicity,
            int amountOfPeriodsPerRepetition) {
        Validate.notNull(endBy);
        return new RecurrenceInformation(0, endBy, recurrencePeriodicity,
                amountOfPeriodsPerRepetition, null);
    }

    private int repetitions;

    /**
     * It can be <code>null</code>. If not <code>null</code>, repetitions must
     * have value zero and recurrences must be generated as long as they start
     * before this value; or after if the direction of allocation is backwards.
     */
    private LocalDate endBy;

    private RecurrencePeriodicity recurrencePeriodicity;

    private int amountOfPeriodsPerRepetition;

    /**
     * Only applicable to {@link RecurrencePeriodicity#WEEKLY} and
     * {@link RecurrencePeriodicity#MONTHLY} . It's <code>null</code> if it
     * doesn't have to repeat on a specific day.
     *
     * <p>
     * If the recurrence is weekly {@link #repeatOnDay} can take values from 1
     * to 7. If the recurrence is monthly it can take values from 1 to 31. If
     * the month doesn't have that day it will repeat on the topmost day of that
     * month.
     * </p>
     */
    private Integer repeatOnDay;

    public int getAmountOfPeriodsPerRepetition() {
        return amountOfPeriodsPerRepetition;
    }

    public RecurrenceInformation() {
        this.repetitions = 0;
        this.recurrencePeriodicity = RecurrencePeriodicity.NO_PERIODICTY;
        this.amountOfPeriodsPerRepetition = 0;
    }

    private RecurrenceInformation(int numberRepetitions, LocalDate endBy,
            RecurrencePeriodicity recurrencePeriodicity,
            int amountOfPeriodsPerRepetition, Integer repeatOnDay) {
        Validate.notNull(recurrencePeriodicity);
        Validate.isTrue(numberRepetitions >= 0,
                "the number of repetitions cannot be negative. It is: "
                        + numberRepetitions);
        Validate.isTrue(amountOfPeriodsPerRepetition >= 0,
                "the amountOfPeriodsPerRepetition cannot be negative");
        Validate.isTrue(recurrencePeriodicity.isNoPeriodicity()
                        || amountOfPeriodsPerRepetition >= 1,
                "if there are repetitions, the amount of periods per repetition must be greater than zero");
        if (numberRepetitions > 0) {
            Validate.isTrue(endBy == null,
                    "If number repetitions greater than zero, endBy must be null.");
        }
        if (endBy != null) {
            Validate.isTrue(numberRepetitions == 0,
                    "If endBy specified, the number of repetitions must be zero.");
        }
        this.recurrencePeriodicity = recurrencePeriodicity;
        this.repetitions = recurrencePeriodicity
                .limitRepetitions(numberRepetitions);
        this.amountOfPeriodsPerRepetition = recurrencePeriodicity
                .limitAmountOfPeriods(amountOfPeriodsPerRepetition);
        this.repeatOnDay = repeatOnDay;
        this.endBy = endBy;
    }

    public RecurrenceInformation repeatOnDay(int day) {
        recurrencePeriodicity.checkRepeatOnDay(day);
        return new RecurrenceInformation(repetitions, endBy,
                recurrencePeriodicity, amountOfPeriodsPerRepetition, day);
    }

    public int getRepetitions() {
        return repetitions;
    }

    public RecurrencePeriodicity getPeriodicity() {
        return recurrencePeriodicity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RecurrenceInformation) {
            RecurrenceInformation other = (RecurrenceInformation) obj;
            return new EqualsBuilder().append(repetitions, other.repetitions)
                    .append(endBy, other.endBy)
                    .append(recurrencePeriodicity, other.recurrencePeriodicity)
                    .append(amountOfPeriodsPerRepetition,
                            other.amountOfPeriodsPerRepetition)
                    .append(repeatOnDay, other.repeatOnDay)
                    .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(repetitions).append(endBy)
                .append(recurrencePeriodicity)
                .append(amountOfPeriodsPerRepetition).append(repeatOnDay)
                .toHashCode();
    }

    public List<LocalDate> getRecurrences(Direction direction, LocalDate start) {
        Validate.notNull(direction);
        Validate.notNull(start);

        if (recurrencePeriodicity.isNoPeriodicity()) {
            return Collections.emptyList();
        }

        ReadablePeriod period = recurrencePeriodicity
                .buildPeriod(amountOfPeriodsPerRepetition);
        LocalDate current = start;
        List<LocalDate> result = new ArrayList<LocalDate>();

        for (int i = 0; i < repetitions || repetitions == 0 && endBy != null; i++) {

            current = direction == Direction.FORWARD ? current.plus(period)
                    : current.minus(period);

            if (repeatOnDay != null) {
                current = recurrencePeriodicity.adjustToDay(current,
                        repeatOnDay);
            }
            if (endBy != null && surpassedEndBy(direction, current)) {
                break;
            }
            result.add(current);
        }
        return result;
    }

    private boolean surpassedEndBy(Direction direction, LocalDate current) {
        if (direction == Direction.FORWARD) {
            return current.compareTo(endBy) > 0;
        } else {
            return current.compareTo(endBy) < 0;
        }
    }

    public Integer getRepeatOnDay() {
        return repeatOnDay;
    }

}