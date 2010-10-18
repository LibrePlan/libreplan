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
package org.navalplanner.business.calendars.entities;

import static org.navalplanner.business.workingday.EffortDuration.zero;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.EndOfTime;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.FixedPoint;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.Interval;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.StartOfTime;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.IntraDayDate;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;
import org.navalplanner.business.workingday.ResourcesPerDay;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ThereAreHoursOnWorkHoursCalculator {

    // Not instantiableisCapacityAvailable
    private ThereAreHoursOnWorkHoursCalculator() {
    }

    public static abstract class CapacityResult {
        public final boolean thereIsCapacityAvailable() {
            return match(new IMatcher<Boolean>() {

                @Override
                public Boolean on(CapacityAvailable result) {
                    return true;
                }

                @Override
                public Boolean on(ThereAreNoValidPeriods result) {
                    return false;
                }

                @Override
                public Boolean on(ValidPeriodsDontHaveCapacity result) {
                    return false;
                }

                @Override
                public Boolean on(ResourcesPerDayIsZero result) {
                    return false;
                }
            });
        }

        public interface IMatcher<T> {
            public T on(CapacityAvailable result);

            public T on(ThereAreNoValidPeriods result);

            public T on(ValidPeriodsDontHaveCapacity result);

            public T on(ResourcesPerDayIsZero result);
        }

        public abstract <T> T match(IMatcher<T> matcher);
    }

    public static class CapacityAvailable extends CapacityResult {

        private CapacityAvailable() {
        }

        @Override
        public <T> T match(IMatcher<T> matcher) {
            return matcher.on(this);
        }

    }

    public static class ThereAreNoValidPeriods extends CapacityResult {

        private final ICalendar specifiedCalendar;

        private final AvailabilityTimeLine specifiedAdditionalAvailability;

        private ThereAreNoValidPeriods(ICalendar specifiedCalendar,
                AvailabilityTimeLine specifiedAdditionalAvailability) {
            this.specifiedCalendar = specifiedCalendar;
            this.specifiedAdditionalAvailability = specifiedAdditionalAvailability;
        }

        public ICalendar getSpecifiedCalendar() {
            return specifiedCalendar;
        }

        public AvailabilityTimeLine getSpecifiedAdditionalAvailability() {
            return specifiedAdditionalAvailability;
        }

        @Override
        public <T> T match(IMatcher<T> matcher) {
            return matcher.on(this);
        }

    }

    public static class ValidPeriodsDontHaveCapacity extends CapacityResult {

        private final List<Interval> validPeriods;

        private final EffortDuration sumReached;

        private final EffortDuration effortNeeded;

        private ValidPeriodsDontHaveCapacity(
                Collection<? extends Interval> validPeriods,
                EffortDuration sumReached, EffortDuration effortNeeded) {
            this.validPeriods = Collections
                    .unmodifiableList(new ArrayList<Interval>(validPeriods));
            this.sumReached = sumReached;
            this.effortNeeded = effortNeeded;
        }

        public List<Interval> getValidPeriods() {
            return validPeriods;
        }

        public EffortDuration getSumReached() {
            return sumReached;
        }

        public EffortDuration getEffortNeeded() {
            return effortNeeded;
        }

        @Override
        public <T> T match(IMatcher<T> matcher) {
            return matcher.on(this);
        }
    }

    public static class ResourcesPerDayIsZero extends CapacityResult {

        private ResourcesPerDayIsZero() {
        }

        @Override
        public <T> T match(IMatcher<T> matcher) {
            return matcher.on(this);
        }

    }

    /**
     * Calculates if there are enough hours
     */
    public static CapacityResult thereIsAvailableCapacityFor(
            ICalendar calendar,
            AvailabilityTimeLine availability,
            ResourcesPerDay resourcesPerDay, EffortDuration effortToAllocate) {
        if (effortToAllocate.isZero()) {
            return new CapacityAvailable();
        }
        if (resourcesPerDay.isZero()) {
            return new ResourcesPerDayIsZero();
        }
        AvailabilityTimeLine realAvailability = calendar.getAvailability()
                .and(availability);
        List<Interval> validPeriods = realAvailability.getValidPeriods();
        if (validPeriods.isEmpty()) {
            return new ThereAreNoValidPeriods(calendar, availability);
        }

        Interval last = getLast(validPeriods);
        Interval first = validPeriods.get(0);
        final boolean isOpenEnded = last.getEnd().equals(EndOfTime.create())
                || first.getStart().equals(StartOfTime.create());
        if (isOpenEnded) {
            return new CapacityAvailable();
        }
        return thereIsCapacityOn(calendar, effortToAllocate, resourcesPerDay,
                validPeriods);

    }

    private static Interval getLast(List<Interval> validPeriods) {
        return validPeriods.get(validPeriods.size() - 1);
    }

    private static CapacityResult thereIsCapacityOn(ICalendar calendar,
            EffortDuration effortToAllocate,
            ResourcesPerDay resourcesPerDay, List<Interval> validPeriods) {
        EffortDuration sum = zero();
        for (Interval each : validPeriods) {
            FixedPoint start = (FixedPoint) each.getStart();
            FixedPoint end = (FixedPoint) each.getEnd();
            EffortDuration pending = effortToAllocate.minus(sum);
            sum = sum.plus(sumDurationUntil(calendar, pending,
                    resourcesPerDay, start.getDate(), end.getDate()));
            if (sum.compareTo(effortToAllocate) >= 0) {
                return new CapacityAvailable();
            }
        }
        return new ValidPeriodsDontHaveCapacity(validPeriods, sum,
                effortToAllocate);
    }

    private static EffortDuration sumDurationUntil(ICalendar calendar,
            EffortDuration maximum,
            ResourcesPerDay resourcesPerDay,
            LocalDate start, LocalDate end) {
        return sunDurationUntil(calendar, maximum, resourcesPerDay, IntraDayDate.startOfDay(start),
                IntraDayDate.startOfDay(end));
    }

    private static EffortDuration sunDurationUntil(ICalendar calendar,
            EffortDuration maximum, ResourcesPerDay resourcesPerDay,
            IntraDayDate start, IntraDayDate end) {
        EffortDuration result = zero();
        for (PartialDay current : start.daysUntil(end)) {
            result = result.plus(calendar.asDurationOn(current, resourcesPerDay));
            if (result.compareTo(maximum) >= 0) {
                return maximum;
            }
        }
        return result;
    }

}
