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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.validator.NotNull;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * @author Diego Pino García <dpino@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 *
 *         Stretch for the assignment function.
 *
 */
public class Stretch {

    public static Stretch create(LocalDate date, BigDecimal datePercent, BigDecimal workPercent) {
        return new Stretch(date, datePercent, workPercent);
    }

    /**
     * Infers the datePercent based on duration of task and the date of the
     * Stretch
     *
     * @param date
     * @param task
     * @param workPercent
     * @return
     */
    public static Stretch create(LocalDate date, Task task,
            BigDecimal workPercent) {
        LocalDate start = task.getStartAsLocalDate();
        LocalDate end = task.getEndAsLocalDate();

        Days taskDuration = Days.daysBetween(start, end);
        Days daysDuration = Days.daysBetween(start, date);
        BigDecimal daysPercent = daysPercent(daysDuration, taskDuration);
        return new Stretch(date, daysPercent, workPercent);
    }

    protected static BigDecimal daysPercent(Days daysPartial, Days daysTotal) {
        return percentWorked(daysPartial.getDays(), daysTotal.getDays());
    }

    private static BigDecimal percentWorked(int daysPartial, int daysTotal) {
        return divide(BigDecimal.valueOf(daysPartial), Integer.valueOf(daysTotal));
    }

    private static BigDecimal divide(BigDecimal numerator, Integer denominator) {
        if (Integer.valueOf(0).equals(denominator)) {
            return BigDecimal.ZERO;
        }
        return numerator.divide(BigDecimal.valueOf(denominator), 8,
                BigDecimal.ROUND_HALF_EVEN);
    }

    public static Stretch copy(Stretch stretch) {
        Stretch result = new Stretch();
        result.date = stretch.date;
        result.lengthPercentage = stretch.lengthPercentage;
        result.amountWorkPercentage = stretch.amountWorkPercentage;
        result.consolidated = stretch.consolidated;
        result.readOnly = stretch.readOnly;
        return result;
    }

    public static Stretch buildFromConsolidatedProgress(ResourceAllocation<? extends DayAssignment> resourceAllocation) {
        return ConsolidatedStretch.fromConsolidatedProgress(resourceAllocation);
    }

    public static List<Stretch> sortByDate(
            List<Stretch> stretches) {
        Collections.sort(stretches, new Comparator<Stretch>() {
            @Override
            public int compare(Stretch o1, Stretch o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        return stretches;
    }

    @NotNull
    private LocalDate date = new LocalDate();

    @NotNull
    private BigDecimal lengthPercentage = BigDecimal.ZERO;

    @NotNull
    private BigDecimal amountWorkPercentage = BigDecimal.ZERO;

    // Transient value, a stretch is readOnly if it is a stretch user cannot edit
    private boolean readOnly = false;

    // Transient value, a stretch is consolidated if it's a consolidated stretch
    private boolean consolidated = false;

    private Stretch(LocalDate date, BigDecimal lengthPercent, BigDecimal progressPercent) {
        this.date = date;
        this.lengthPercentage = lengthPercent;
        this.amountWorkPercentage = progressPercent;
    }

    public Stretch() {

    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    /**
     * @param lengthPercentage
     *            It's one based, instead of one hundred based.
     * @throws IllegalArgumentException
     *             If the percentages is not between 0 and 1.
     */
    public void setLengthPercentage(BigDecimal lengthPercentage)
            throws IllegalArgumentException {
        if ((lengthPercentage.compareTo(BigDecimal.ZERO) < 0)
                || (lengthPercentage.compareTo(BigDecimal.ONE) > 0)) {
            throw new IllegalArgumentException(
                    "lengthPercentage should be between 0 and 1");
        }
        this.lengthPercentage = lengthPercentage;
    }

    public BigDecimal getLengthPercentage() {
        return lengthPercentage;
    }

    /**
     * @param amountWorkPercentage
     *            It's one based, instead of one hundred based.
     * @throws IllegalArgumentException
     *             If the percentages is not between 0 and 1.
     */
    public void setAmountWorkPercentage(BigDecimal amountWorkPercentage)
            throws IllegalArgumentException {
        if ((amountWorkPercentage.compareTo(BigDecimal.ZERO) < 0)
                || (amountWorkPercentage.compareTo(BigDecimal.ONE) > 0)) {
            throw new IllegalArgumentException(
                    "amountWorkPercentage should be between 0 and 1");
        }
        this.amountWorkPercentage = amountWorkPercentage;
    }

    public BigDecimal getAmountWorkPercentage() {
        return amountWorkPercentage;
    }

    public String toString() {
        return String.format("(%s, %s, %s, readOnly: %s) ", date, lengthPercentage, amountWorkPercentage, readOnly);
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void readOnly(boolean value) {
        readOnly = value;
    }

    public boolean isConsolidated() {
        return consolidated;
    }

    public void consolidated(boolean value) {
        consolidated = value;
    }

}

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         Builds Stretch from consolidated progress in resource allocation.
 *
 */
class ConsolidatedStretch {

    protected static Stretch fromConsolidatedProgress(
            ResourceAllocation<?> resourceAllocation) {

        List<? extends DayAssignment> consolidated = resourceAllocation.getConsolidatedAssignments();
        if (consolidated.isEmpty()) {
            return null;
        }

        final Task task = resourceAllocation.getTask();
        final LocalDate consolidatedEnd = lastDay(consolidated);

        return create(consolidatedEnd.plusDays(1), task.getAdvancePercentage(), task);
    }

    private static Stretch create(LocalDate date, BigDecimal advancePercentage,
            Task task) {
        Stretch result = Stretch.create(date, task, advancePercentage);
        result.readOnly(true);
        result.consolidated(true);
        return result;
    }

    private ConsolidatedStretch() {

    }

    private static LocalDate lastDay(List<? extends DayAssignment> days) {
        return days.get(days.size() - 1).getDay();
    }

}