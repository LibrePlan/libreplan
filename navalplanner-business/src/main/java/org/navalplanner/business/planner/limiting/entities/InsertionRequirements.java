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
package org.navalplanner.business.planner.limiting.entities;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.limiting.entities.Gap.GapOnQueue;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.workingday.IntraDayDate;


/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class InsertionRequirements {

    private final LimitingResourceQueueElement element;

    private final DateAndHour earliestPossibleStart;

    private final DateAndHour earliestPossibleEnd;

    private final DateAndHour latestPossibleEnd;

    public static InsertionRequirements forElement(
            LimitingResourceQueueElement element,
            List<LimitingResourceQueueDependency> dependenciesAffectingStart,
            List<LimitingResourceQueueDependency> dependenciesAffectingEnd) {

        return new InsertionRequirements(element, calculateEarliestPossibleStart(
                element, dependenciesAffectingStart),
                calculateEarliestPossibleEnd(element, dependenciesAffectingEnd));
    }

    /**
     * Specifies a minimum startTime, earliestStart should be lower than this value
     *
     * @param element
     * @param dependenciesAffectingStart
     * @param dependenciesAffectingEnd
     * @param startAt
     * @return
     */
    public static InsertionRequirements forElement(
            LimitingResourceQueueElement element,
            List<LimitingResourceQueueDependency> dependenciesAffectingStart,
            List<LimitingResourceQueueDependency> dependenciesAffectingEnd,
            DateAndHour startAt) {

        DateAndHour earliesPossibleStart = calculateEarliestPossibleStart(
                element, dependenciesAffectingStart);
        return new InsertionRequirements(element, DateAndHour.max(
                earliesPossibleStart, startAt), calculateEarliestPossibleEnd(
                element, dependenciesAffectingEnd));
    }

    private static DateAndHour calculateEarliestPossibleEnd(
            LimitingResourceQueueElement element,
            List<LimitingResourceQueueDependency> dependenciesAffectingEnd) {
        return DateAndHour.max(asDateAndHour(element
                .getEarliestEndDateBecauseOfGantt()),
                max(dependenciesAffectingEnd));
    }

    private static DateAndHour calculateEarliestPossibleStart(
            LimitingResourceQueueElement element,
            List<LimitingResourceQueueDependency> dependenciesAffectingStart) {
        return DateAndHour.max(asDateAndHour(element
                .getEarlierStartDateBecauseOfGantt()),
                max(dependenciesAffectingStart));
    }

    private static DateAndHour max(
            List<LimitingResourceQueueDependency> dependencies) {
        DateAndHour result = null;
        for (LimitingResourceQueueDependency each : dependencies) {
            assert !each.getHasAsOrigin().isDetached();
            result = DateAndHour.max(result, each.getDateFromOrigin());
        }
        return result;
    }

    private static DateAndHour asDateAndHour(Date date) {
        return DateAndHour.from(LocalDate.fromDateFields(date));
    }

    public static InsertionRequirements create(
            LimitingResourceQueueElement element,
            DateAndHour start, DateAndHour end) {
        return new InsertionRequirements(element, start, end);
    }

    private InsertionRequirements(LimitingResourceQueueElement element,
            DateAndHour earliestPossibleStart,
            DateAndHour earliestPossibleEnd) {
        Validate.notNull(element);
        Validate.notNull(earliestPossibleStart);
        Validate.notNull(earliestPossibleEnd);
        this.element = element;
        this.earliestPossibleStart = earliestPossibleStart;
        this.earliestPossibleEnd = earliestPossibleEnd;
        this.latestPossibleEnd = calculateLatestPlanningDate(element);
    }

    /**
     * Returns the earliest date from all the outgoing tasks from element
     *
     * @param element
     * @return
     */
    private DateAndHour calculateLatestPlanningDate(LimitingResourceQueueElement element) {
        IntraDayDate result = null;
        Task task = element.getTask();

        for (Dependency each : task.getDependenciesWithThisOrigin()) {
            if (each.getType().modifiesDestinationEnd()) {
                TaskElement destination = each.getDestination();
                result = (result == null) ? destination.getIntraDayStartDate()
                        : IntraDayDate.min(result,
                                destination.getIntraDayStartDate());
            }
        }
        return (result != null) ? DateAndHour.from(result) : DateAndHour.from(new LocalDate(
                element.getEarliestEndDateBecauseOfGantt()));
    }

    public boolean isPotentiallyValid(Gap gap) {
        DateAndHour gapEnd = gap.getEndTime();
        return gapEnd == null
                || (earliestPossibleStart.isBefore(gapEnd) && !earliestPossibleEnd
                        .isAfter(gapEnd));
    }

    public AllocationSpec guessValidity(GapOnQueue gapOnQueue) {
        Gap gap = gapOnQueue.getGap();
        if (!isPotentiallyValid(gap)) {
            return AllocationSpec.invalidOn(gapOnQueue);
        }
        DateAndHour realStart = DateAndHour.max(earliestPossibleStart, gap
                .getStartTime());
        Resource resource = gapOnQueue.getOriginQueue().getResource();
        List<Integer> hours = gap.getHoursInGapUntilAllocatingAndGoingToTheEnd(
                resource.getCalendar(), realStart,
                earliestPossibleEnd, element.getIntentedTotalHours());
        int total = sum(hours);
        if (total < element.getIntentedTotalHours()) {
            return AllocationSpec.invalidOn(gapOnQueue);
        } else if (total == element.getIntentedTotalHours()) {
            return validAllocation(gapOnQueue, realStart, hours);
        } else {
            assert total > element.getIntentedTotalHours();
            int hoursSurplus = total - element.getIntentedTotalHours();
            StartRemoval result = StartRemoval.removeStartSurplus(realStart,
                    hours, hoursSurplus);
            return validAllocation(gapOnQueue, result.newStart, result.hours);
        }

    }

    private AllocationSpec validAllocation(GapOnQueue gap,
            DateAndHour realStart,
            List<Integer> hours) {
        return AllocationSpec.validOn(element, gap, realStart, calculateEnd(
                realStart, hours), asArray(hours));
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

    public boolean isAppropiativeAllocation(AllocationSpec allocation) {
        Gap gap = allocation.getGap();
        DateAndHour realStart = DateAndHour.max(earliestPossibleStart,
                gap.getStartTime());
        return latestPossibleEnd != null
                && latestPossibleEnd.compareTo(realStart) < 0;
    }

    public DateAndHour getEarliestPossibleStart() {
        return earliestPossibleStart;
    }

}
