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

import static org.libreplan.business.i18n.I18nHelper._;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Valid;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * @author Diego Pino García <dpino@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 *
 *         Assignment function by stretches.
 *
 */
public class StretchesFunction extends AssignmentFunction {

    public static class Interval {

        private LocalDate start;

        private LocalDate end;

        private final BigDecimal loadProportion;

        private boolean consolidated = false;

        public static Interval create(BigDecimal loadProportion, LocalDate start,
                LocalDate end, boolean consolidated) {
            Interval result = create(loadProportion, start, end);
            result.consolidated(consolidated);
            return result;
        }

        public static Interval create(BigDecimal loadProportion, LocalDate start,
                LocalDate end) {
            return new Interval(loadProportion, start, end);
        }

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
            double[] result = new double[intervalsDefinedByStreches.size()];
            int i = 0;
            int accumulated = 0;
            for (Interval each : intervalsDefinedByStreches) {
                accumulated += each.getHoursFor(totalHours);
                result[i++] = accumulated;
            }
            return result;
        }

        public static double[] getDayPointsFor(LocalDate start,
                List<Interval> intervalsDefinedByStreches) {
            double[] result = new double[intervalsDefinedByStreches.size()];
            int i = 0;
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
            Validate.isTrue(!isConsolidated());

            // End has to be exclusive on last Stretch
            LocalDate endDate = getEnd();
            if (endDate.equals(taskEnd)) {
                endDate = endDate.plusDays(1);
            }
            resourceAllocation.withPreviousAssociatedResources()
                    .onInterval(getStartFor(startInclusive), endDate)
                    .allocateHours(intervalHours);
        }

        public static void apply(ResourceAllocation<?> allocation,
                List<Interval> intervalsDefinedByStreches,
                LocalDate allocationStart, LocalDate allocationEnd,
                int totalHours) {
            if (intervalsDefinedByStreches.isEmpty()) {
                return;
            }
            Validate.isTrue(totalHours == allocation.getNonConsolidatedHours());

            int[] hoursPerInterval = getHoursPerInterval(
                    intervalsDefinedByStreches, totalHours);
            int remainder = totalHours - sum(hoursPerInterval);
            hoursPerInterval[0] += remainder;
            int i = 0;
            for (Interval interval : intervalsDefinedByStreches) {
                interval.apply(allocation, allocationStart, allocationEnd,
                        hoursPerInterval[i++]);
            }
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

        public String toString() {
            return String.format("[%s, %s]: %s ", start, end, loadProportion);
        }

        public void consolidated(boolean value) {
            consolidated = value;
        }

        public boolean isConsolidated() {
            return consolidated;
        }

    }

    private List<Stretch> stretches = new ArrayList<Stretch>();

    private StretchesFunctionTypeEnum type;

    // Transient field. Not stored
    private StretchesFunctionTypeEnum desiredType;

    // Transient. Calculated from resourceAllocation
    private Stretch consolidatedStretch;

    // Transient. Used to calculate stretches dates
    private ResourceAllocation<?> resourceAllocation;

    public static StretchesFunction create() {
        return (StretchesFunction) create(new StretchesFunction());
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    protected StretchesFunction() {

    }

    public static List<Interval> intervalsFor(ResourceAllocation<?> allocation,
            Collection<? extends Stretch> streches) {
        ArrayList<Interval> result = new ArrayList<Interval>();
        LocalDate previous = null, stretchDate = null;
        BigDecimal sumOfProportions = BigDecimal.ZERO, loadedProportion = BigDecimal.ZERO;

        for (Stretch each : streches) {
            stretchDate = each.getDateIn(allocation);
            loadedProportion = each.getAmountWorkPercentage().subtract(
                    sumOfProportions);
            if (loadedProportion.signum() < 0) {
                loadedProportion = BigDecimal.ZERO;
            }
            result.add(Interval.create(loadedProportion, previous,
                    stretchDate, each.isConsolidated()));
            sumOfProportions = each.getAmountWorkPercentage();
            previous = stretchDate;
        }
        return result;
    }

    private static <T> T last(List<? extends T> list) {
        return list.get(list.size() - 1);
    }

    public StretchesFunction copy() {
        StretchesFunction result = StretchesFunction.create();
        result.resetToStrechesFrom(this);
        result.type = type;
        result.desiredType = desiredType;
        result.consolidatedStretch = consolidatedStretch;
        result.resourceAllocation = resourceAllocation;
        return result;
    }

    public void resetToStrechesFrom(StretchesFunction from) {
        this.removeAllStretches();
        for (Stretch each : from.getStretchesDefinedByUser()) {
            this.addStretch(Stretch.copy(each));
        }
        this.consolidatedStretch = from.consolidatedStretch;
    }

    public List<Stretch> getStretchesDefinedByUser() {
        return Collections.unmodifiableList(Stretch
                .sortByLengthPercentage(stretches));
    }

    @Valid
    public List<Stretch> getStretches() {
        List<Stretch> result = new ArrayList<Stretch>();
        result.add(getFirstStretch());
        result.addAll(stretches);
        result.add(getLastStretch());
        return Collections.unmodifiableList(Stretch
                .sortByLengthPercentage(result));
    }

    private Stretch getLastStretch() {
        Stretch result = Stretch.create(BigDecimal.ONE, BigDecimal.ONE);
        result.readOnly(true);
        return result;
    }

    private Stretch getFirstStretch() {
        Stretch result = Stretch.create(BigDecimal.ZERO, BigDecimal.ZERO);
        result.readOnly(true);
        return result;
    }

    public StretchesFunctionTypeEnum getType() {
        return type == null ? StretchesFunctionTypeEnum.STRETCHES : type;
    }

    public StretchesFunctionTypeEnum getDesiredType() {
        return desiredType == null ? getType() : desiredType;
    }

    public void changeTypeTo(StretchesFunctionTypeEnum type) {
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
        // first 0%-0% and last 100%-100% stretches are added automatically
        return getStretchesPlusConsolidated().size() > 2;
    }

    @AssertTrue(message = "Some stretch has lower or equal values than the "
            + "previous stretch")
    public boolean checkStretchesOrder() {
        List<Stretch> stretchesPlusConsolidated = getStretchesPlusConsolidated();
        if (stretchesPlusConsolidated.isEmpty()) {
            return false;
        }

        Iterator<Stretch> iterator = stretchesPlusConsolidated.iterator();
        Stretch previous = iterator.next();
        while (iterator.hasNext()) {
            Stretch current = iterator.next();
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

    public List<Stretch> getStretchesPlusConsolidated() {
        List<Stretch> result = new ArrayList<Stretch>();
        result.addAll(getStretches());
        if (consolidatedStretch != null) {
            result.add(consolidatedStretch);
        }
        return Collections.unmodifiableList(Stretch
                .sortByLengthPercentage(result));
    }

    @AssertTrue(message = "Last stretch should have one hundred percent for "
            + "length and amount of work percentage")
    public boolean checkOneHundredPercent() {
        List<Stretch> stretches = getStretchesPlusConsolidated();
        if (stretches.isEmpty()) {
            return false;
        }
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
        // Is 100% consolidated
        if (resourceAllocation.getFirstNonConsolidatedDate() == null) {
            return;
        }
        this.resourceAllocation = resourceAllocation;
        getDesiredType().applyTo(resourceAllocation, this);
        type = getDesiredType();
    }

    @Override
    public String getName() {
        if (StretchesFunctionTypeEnum.INTERPOLATED.equals(type)) {
            return AssignmentFunctionName.INTERPOLATION.toString();
        } else {
            return AssignmentFunctionName.STRETCHES.toString();
        }
    }

    public List<Interval> getIntervalsDefinedByStreches() {
        List<Stretch> stretches = stretchesFor(type);
        if (stretches.isEmpty()) {
            return Collections.emptyList();
        }
        checkStretchesSumOneHundredPercent();
        return intervalsFor(resourceAllocation, stretches);
    }

    private List<Stretch> stretchesFor(StretchesFunctionTypeEnum type) {
        return (getDesiredType().equals(StretchesFunctionTypeEnum.INTERPOLATED)) ? getStretchesPlusConsolidated()
                : getStretches();
    }

    private void checkStretchesSumOneHundredPercent() {
        List<Stretch> stretches = getStretchesPlusConsolidated();
        BigDecimal sumOfProportions = stretches.isEmpty() ? BigDecimal.ZERO
                : last(stretches).getAmountWorkPercentage();
        BigDecimal left = calculateLeftFor(sumOfProportions);
        if (!left.equals(BigDecimal.ZERO)) {
            throw new IllegalStateException(_("Stretches must sum 100%"));
        }
    }

    private BigDecimal calculateLeftFor(BigDecimal sumOfProportions) {
        BigDecimal left = BigDecimal.ONE.subtract(sumOfProportions);
        left = left.signum() <= 0 ? BigDecimal.ZERO : left;
        return left;
    }

    public boolean checkHasAtLeastTwoStretches() {
        return getStretchesPlusConsolidated().size() >= 2;
    }

    public boolean isInterpolated() {
        return getDesiredType().equals(StretchesFunctionTypeEnum.INTERPOLATED);
    }

    public void setConsolidatedStretch(Stretch stretch) {
        consolidatedStretch = stretch;
    }

    public Stretch getConsolidatedStretch() {
        return consolidatedStretch;
    }

    public void setResourceAllocation(ResourceAllocation<?> resourceAllocation) {
        this.resourceAllocation = resourceAllocation;
    }

    @Override
    public boolean isManual() {
        return false;
    }

}
