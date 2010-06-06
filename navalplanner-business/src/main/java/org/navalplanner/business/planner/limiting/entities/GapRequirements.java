/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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
package org.navalplanner.business.planner.limiting.entities;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;


/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class GapRequirements {

    private final LimitingResourceQueueElement element;

    private final DateAndHour earliestPossibleStart;

    private final DateAndHour earliestPossibleEnd;

    public static GapRequirements forElement(
            LimitingResourceQueueElement element) {
        DateAndHour end = element.getEarliestEndDateBecauseOfDependencies();
        return new GapRequirements(element, calculateEarliestPossibleStart(element),
                calculateEarliestPossibleEnd(element, end));
    }

    private static DateAndHour calculateEarliestPossibleEnd(
            LimitingResourceQueueElement element, DateAndHour end) {
        return DateAndHour.Max(asDateAndHour(element
                .getEarliestEndDateBecauseOfGantt()), end);
    }

    private static DateAndHour calculateEarliestPossibleStart(
            LimitingResourceQueueElement element) {
        return DateAndHour.Max(asDateAndHour(element
                .getEarlierStartDateBecauseOfGantt()),
                element.getEarliestStartDateBecauseOfDependencies());
    }

    private static DateAndHour asDateAndHour(Date date) {
        return DateAndHour.from(LocalDate.fromDateFields(date));
    }

    private GapRequirements(LimitingResourceQueueElement element,
            DateAndHour earliestPossibleStart, DateAndHour earliestPossibleEnd) {
        Validate.notNull(element);
        Validate.notNull(earliestPossibleStart);
        Validate.notNull(earliestPossibleEnd);
        this.element = element;
        this.earliestPossibleStart = earliestPossibleStart;
        this.earliestPossibleEnd = earliestPossibleEnd;
    }

    public boolean isPotentiallyValid(Gap gap) {
        DateAndHour gapEnd = gap.getEndTime();
        return gapEnd == null
                || (earliestPossibleStart.isBefore(gapEnd) && !earliestPossibleEnd
                        .isAfter(gapEnd));
    }

    public AllocationOnGap guessValidity(Gap gap) {
        if (!isPotentiallyValid(gap)) {
            return AllocationOnGap.invalidOn(gap);
        }
        DateAndHour realStart = DateAndHour.Max(earliestPossibleStart, gap
                .getStartTime());
        List<Integer> hours = gap.getHoursInGapUntilAllocatingAndGoingToTheEnd(
                element.getResource().getCalendar(), realStart,
                earliestPossibleEnd, element.getIntentedTotalHours());
        int total = sum(hours);
        if (total < element.getIntentedTotalHours()) {
            return AllocationOnGap.invalidOn(gap);
        } else if (total == element.getIntentedTotalHours()) {
            return validAllocation(gap, realStart, hours);
        } else {
            assert total > element.getIntentedTotalHours();
            int hoursSurplus = total - element.getIntentedTotalHours();
            StartRemoval result = StartRemoval.removeStartSurplus(realStart,
                    hours, hoursSurplus);
            return validAllocation(gap, result.newStart, result.hours);
        }

    }

    private AllocationOnGap validAllocation(Gap gap, DateAndHour realStart,
            List<Integer> hours) {
        return AllocationOnGap.validOn(gap, realStart, calculateEnd(realStart,
                hours), asArray(hours));
    }

    private DateAndHour calculateEnd(DateAndHour realStart, List<Integer> hours) {
        if (hours.size() == 1) {
            return new DateAndHour(realStart.getDate(), hours.get(0)
                    + realStart.getHour());
        }
        return new DateAndHour(realStart.getDate().plusDays(hours.size() - 1),
                getLast(hours));
    }

    private int getLast(List<Integer> hours) {
        return hours.get(hours.size() - 1);
    }

    private static class StartRemoval {

        /**
         * removes the initial assignments so the resulting list has
         * <code>hoursSurplus</code> less hours
         */
        static StartRemoval removeStartSurplus(DateAndHour start,
                List<Integer> hours, int hoursSurplus) {
            int previousSize = hours.size();
            int hoursRemovedAtFirstDayOfNewHours = stripStartAssignments(hours,
                    hoursSurplus);
            int currentSize = hours.size();
            int daysRemoved = previousSize - currentSize;
            LocalDate newStartDay = start.getDate().plusDays(daysRemoved);
            return new StartRemoval(new DateAndHour(newStartDay,
                    hoursRemovedAtFirstDayOfNewHours), hours);
        }

        /**
         * @return the hours reduced in the resulting first assignment
         */
        private static int stripStartAssignments(List<Integer> hours,
                int hoursSurplus) {
            ListIterator<Integer> listIterator = hours.listIterator();
            while (listIterator.hasNext() && hoursSurplus > 0) {
                Integer current = listIterator.next();
                int hoursTaken = Math.min(hoursSurplus, current);
                hoursSurplus -= hoursTaken;
                if (hoursTaken == current) {
                    listIterator.remove();
                } else {
                    listIterator.set(hoursTaken);
                    return current - hoursTaken;
                }
            }
            return 0;
        }

        private final DateAndHour newStart;

        private final List<Integer> hours;

        private StartRemoval(DateAndHour newStart, List<Integer> hours) {
            this.newStart = newStart;
            this.hours = hours;
        }
    }

    private static int[] asArray(Collection<Integer> integers) {
        int[] result = new int[integers.size()];
        int i = 0;
        for (Integer each : integers) {
            result[i++] = each;
        }
        return result;
    }

    private static int sum(List<Integer> hours) {
        int result = 0;
        for (int each : hours) {
            result += each;
        }
        return result;
    }

    public LimitingResourceQueueElement getElement() {
        return element;
    }

}
