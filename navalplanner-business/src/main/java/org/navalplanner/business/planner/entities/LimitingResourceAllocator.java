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

package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionCompounder;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.navalplanner.business.resources.entities.Resource;

/**
 * Handles all the logic related to allocation of
 * {@link LimitingResourceQueueElement} into {@link LimitingResourceQueue}
 *
 * The class does not do the allocation itself but provides methods:
 * <em>getFirstValidGap</em>, <em>calculateStartAndEndTime</em> or
 * <em>generateDayAssignments</em>, needed to do the allocation of
 * {@link LimitingResourceQueueElement}
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class LimitingResourceAllocator {

    private final static ResourcesPerDay ONE_RESOURCE_PER_DAY = ResourcesPerDay
            .amount(new BigDecimal(1));

    /**
     * Returns first valid gap in queue for element
     *
     * Returns null if there is not a valid gap. This case can only happen on
     * trying to allocate an element related to a generic resource allocation.
     * It is possible that queue.resource does not hold element.criteria at any
     * interval of time
     *
     * @param queue search gap inside queue
     * @param element element to fit into queue
     * @return
     */
    public static LimitingResourceQueueElementGap getFirstValidGap(LimitingResourceQueue queue,
            LimitingResourceQueueElement element) {

        final Resource resource = queue.getResource();
        final List<LimitingResourceQueueElement> elements = new LinkedList<LimitingResourceQueueElement>(
                queue.getLimitingResourceQueueElements());
        final int size = elements.size();
        final DateAndHour startTime = getStartTimeBecauseOfGantt(element);

        // Iterate through queue elements
        int pos = 0;
        do {
            LimitingResourceQueueElementGap gap = getGapInQueueAtPosition(
                    resource, elements, startTime, pos++);

            // The queue cannot hold this element (queue.resource
            // doesn't meet element.criteria)
            if (gap == null) {
                return null;
            }

            List<LimitingResourceQueueElementGap> subgaps = getFittingSubgaps(element, gap, resource);
            if (!subgaps.isEmpty()) {
                return subgaps.get(0);
            }

        } while (pos < size);

        return null;
    }


    private static List<LimitingResourceQueueElementGap> getFittingSubgaps(
            LimitingResourceQueueElement element,
            final LimitingResourceQueueElementGap gap, final Resource resource) {

        List<LimitingResourceQueueElementGap> result = new ArrayList<LimitingResourceQueueElementGap>();

        if (isSpecific(element) && gap.canFit(element)) {
            result.add(gap);
        } else if (isGeneric(element)) {
            final List<LimitingResourceQueueElementGap> gaps = splitIntoGapsSatisfyingCriteria(
                    resource, getCriteria(element), gap);
            for (LimitingResourceQueueElementGap subgap : gaps) {
                if (subgap.canFit(element)) {
                    result.add(subgap);
                }
            }
        }
        return result;
    }

    public static List<LimitingResourceQueueElementGap> getValidGapsForElementSince(
            LimitingResourceQueueElement element, LimitingResourceQueue queue,
            DateAndHour since) {

        List<LimitingResourceQueueElementGap> result = new ArrayList<LimitingResourceQueueElementGap>();

        final Resource resource = queue.getResource();
        final List<LimitingResourceQueueElement> elements = new LinkedList<LimitingResourceQueueElement>(
                queue.getLimitingResourceQueueElements());
        final int size = elements.size();

        // Move until startTime
        Integer pos = moveUntil(elements, since);
        if (pos == null) {
            if (size == 0) {
                result.add(createLastGap(since, null, resource));
            } else {
                result.add(createLastGap(since, elements.get(size - 1), resource));
            }
            return result;
        }

        // Iterate through queue elements
        do {
            LimitingResourceQueueElementGap gap = getGapInQueueAtPosition(
                    resource, elements, since, pos++);

            // The queue cannot hold this element (queue.resource
            // doesn't meet element.criteria)
            if (gap == null) {
                return null;
            }

            result.addAll(getFittingSubgaps(element, gap, resource));

        } while (pos < size);

        return result;
    }

    private static Integer moveUntil(List<LimitingResourceQueueElement> elements, DateAndHour until) {
        if (elements.size() > 0) {
            // Space between until and first element start time
            LimitingResourceQueueElement first = elements.get(0);
            if (until.isBefore(first.getStartTime())) {
                return 0;
            }

            for (int pos = 0; pos < elements.size(); pos++) {
                final LimitingResourceQueueElement each = elements.get(pos);
                final DateAndHour startTime = each.getStartTime();
                if (until.isAfter(startTime) || until.isEquals(startTime)) {
                    return pos;
                }
            }
        }
        return null;
    }

    private static boolean isGeneric(LimitingResourceQueueElement element) {
        return element.getResourceAllocation() instanceof GenericResourceAllocation;
    }

    private static boolean isSpecific(LimitingResourceQueueElement element) {
        return element.getResourceAllocation() instanceof SpecificResourceAllocation;
    }

    private static Set<Criterion> getCriteria(LimitingResourceQueueElement element) {
        final ResourceAllocation<?> resourceAllocation = element.getResourceAllocation();
        if (resourceAllocation instanceof GenericResourceAllocation) {
            return ((GenericResourceAllocation) resourceAllocation).getCriterions();
        }
        return null;
    }

    private static Date toDate(LocalDate date) {
        return date != null ? date.toDateTimeAtStartOfDay().toDate() : null;
    }

    private static List<LimitingResourceQueueElementGap> splitIntoGapsSatisfyingCriteria(
            Resource resource, Set<Criterion> criteria, LimitingResourceQueueElementGap gap) {
        return splitIntoGapsSatisfyingCriteria(resource, criteria, gap.getStartTime(), gap.getEndTime());
    }

    /**
     * Returns a set of {@link LimitingResourceQueueElementGap} composed by those gaps
     * which satisfy <em>criteria</em> within the period: <em>gapStartTime</em> till <em>gapEndTime</em>
     *
     * @param resource
     * @param criteria
     *            criteria to be satisfied by resource
     * @param gapStartTime
     *            start time of gap
     * @param gapEndTime
     *            end time of gap
     * @return
     */
    private static List<LimitingResourceQueueElementGap> splitIntoGapsSatisfyingCriteria(
            Resource resource, Set<Criterion> criteria, DateAndHour gapStartTime,
            DateAndHour gapEndTime) {

        final ICriterion compositedCriterion = CriterionCompounder.buildAnd(criteria)
                .getResult();
        final ResourceCalendar calendar = resource.getCalendar();

        // FIXME: If endTime is null (lastGap), set endTime as 100 years ahead startTime
        final LocalDate gapEndDate = gapEndTime != null ? gapEndTime.getDate().plusDays(1)
                : gapStartTime.getDate().plusYears(10);
        final LocalDate gapStartDate = gapStartTime.getDate();

        List<LimitingResourceQueueElementGap> result = new ArrayList<LimitingResourceQueueElementGap>();

        LocalDate date = gapStartDate;
        boolean open = compositedCriterion.isSatisfiedBy(resource, toDate(date));
        DateAndHour startTime = gapStartTime, endTime;
        while (date.isBefore(gapEndDate)) {
            if (calendar.getCapacityAt(date) == 0) {
                date = date.plusDays(1);
                continue;
            }

            if (open == false && compositedCriterion.isSatisfiedBy(resource, toDate(date))) {
                startTime = new DateAndHour(date, 0);
                open = true;
            }
            if (open == true && !compositedCriterion.isSatisfiedBy(resource, toDate(date))) {
                endTime = new DateAndHour(date, 0);
                result.add(LimitingResourceQueueElementGap.create(resource,
                        startTime, endTime));
                open = false;
            }
            date = date.plusDays(1);
        }
        result.add(LimitingResourceQueueElementGap.create(resource, startTime, gapEndTime));

        return result;
    }

    /**
     * Calculates start and end date out of a list of day assignments.
     *
     * The first day is the day were the first day assignment happened.
     * The last day is the day were the last day assignment happened.
     * @param dayAssignments
     * @return
     */
    public static DateAndHour[] calculateStartAndEndTime(List<DayAssignment> dayAssignments) {
        DateAndHour[] result = new DateAndHour[2];

        final DayAssignment start = dayAssignments.get(0);
        final DayAssignment end = dayAssignments.get(dayAssignments.size() - 1);
        result[0] = new DateAndHour(start.getDay(), start.getHours());
        result[1] = new DateAndHour(end.getDay(), end.getHours());

        return result;
    }

    private static LimitingResourceQueueElementGap getGapInQueueAtPosition(
            Resource resource, List<LimitingResourceQueueElement> elements,
            DateAndHour startTimeBecauseOfGantt, int pos) {

        final int size = elements.size();

        if (size == 0) {
            return createLastGap(startTimeBecauseOfGantt, null, resource);
        }

        if (pos == size) {
            return createLastGap(startTimeBecauseOfGantt, elements.get(size - 1), resource);
        }

        LimitingResourceQueueElement current = elements.get(pos);
        // First element
        if (pos == 0
                && startTimeBecauseOfGantt.getDate().isBefore(
                        current.getStartDate())) {
            return LimitingResourceQueueElementGap.create(resource,
                    startTimeBecauseOfGantt, current.getStartTime());
        }

        // Rest of elements
        if (pos + 1 < size) {
            LimitingResourceQueueElement next = elements.get(pos + 1);
            if (startTimeBecauseOfGantt.isBefore(current.getEndTime())) {
                return LimitingResourceQueueElementGap.create(resource, current
                        .getEndTime(), next.getStartTime());
            } else {
                return LimitingResourceQueueElementGap.create(resource,
                        DateAndHour.Max(current.getEndTime(),
                                startTimeBecauseOfGantt), next.getStartTime());
            }
        } else {
            // Current was the last element
            return createLastGap(startTimeBecauseOfGantt, current, resource);
        }

    }

    private static DateAndHour getStartTimeBecauseOfGantt(LimitingResourceQueueElement element) {
        return new DateAndHour(new LocalDate(element.getEarlierStartDateBecauseOfGantt()), 0);
    }

    private static LimitingResourceQueueElementGap createLastGap(
            DateAndHour _startTime, LimitingResourceQueueElement lastElement,
            Resource resource) {

        final DateAndHour queueEndTime = (lastElement != null) ? lastElement
                .getEndTime() : null;
        DateAndHour startTime = DateAndHour.Max(_startTime, queueEndTime);
        return LimitingResourceQueueElementGap
                .create(resource, startTime, null);
    }

    /**
     * Generates a list of {@link DayAssignment} for {@link Resource} starting
     * from startTime
     *
     * The returned list is not associated to resouceAllocation.
     *
     * resourceAllocation is passed to know if the list of day assignments
     * should be {@link GenericDayAssignment} or {@link SpecificDayAssignment}
     *
     * @param resourceAllocation
     * @param resource
     * @param startTime
     * @return
     */
    public static List<DayAssignment> generateDayAssignments(
            ResourceAllocation<?> resourceAllocation,
            Resource resource,
            DateAndHour startTime) {

        List<DayAssignment> assignments = new ArrayList<DayAssignment>();

        LocalDate date = startTime.getDate();
        int totalHours = resourceAllocation.getIntendedTotalHours();

        // Generate first day assignment
        int hoursCanAllocate = hoursCanWorkOnDay(resource, date, startTime.getHour());
        if (hoursCanAllocate > 0) {
            int hoursToAllocate = Math.min(totalHours, hoursCanAllocate);
            DayAssignment dayAssignment = createDayAssignment(resourceAllocation, resource, date, hoursToAllocate);
            totalHours -= addDayAssignment(assignments, dayAssignment);
        }

        // Generate rest of day assignments
        for (date = date.plusDays(1); totalHours > 0; date = date.plusDays(1)) {
            totalHours -= addDayAssignment(assignments, generateDayAssignment(
                    resourceAllocation, resource, date, totalHours));
        }
        return assignments;
    }

    private static List<DayAssignment> generateDayAssignmentsStartingFromEnd(ResourceAllocation<?> resourceAllocation,
            Resource resource,
            DateAndHour endTime) {

        List<DayAssignment> assignments = new ArrayList<DayAssignment>();

        LocalDate date = endTime.getDate();
        int totalHours = resourceAllocation.getIntendedTotalHours();

        // Generate last day assignment
        int hoursCanAllocate = hoursCanWorkOnDay(resource, date, endTime.getHour());
        if (hoursCanAllocate > 0) {
            int hoursToAllocate = Math.min(totalHours, hoursCanAllocate);
            DayAssignment dayAssignment = createDayAssignment(resourceAllocation, resource, date, hoursToAllocate);
            totalHours -= addDayAssignment(assignments, dayAssignment);
        }

        // Generate rest of day assignments
        for (date = date.minusDays(1); totalHours > 0; date = date.minusDays(1)) {
            totalHours -= addDayAssignment(assignments, generateDayAssignment(
                    resourceAllocation, resource, date, totalHours));
        }
        return assignments;
    }

    private static DayAssignment createDayAssignment(ResourceAllocation<?> resourceAllocation,
            Resource resource, LocalDate date, int hoursToAllocate) {
        if (resourceAllocation instanceof SpecificResourceAllocation) {
            return SpecificDayAssignment.create(date, hoursToAllocate, resource);
        } else if (resourceAllocation instanceof GenericResourceAllocation) {
            return GenericDayAssignment.create(date, hoursToAllocate, resource);
        }
        return null;
    }

    private static int addDayAssignment(List<DayAssignment> list, DayAssignment dayAssignment) {
        if (dayAssignment != null) {
            list.add(dayAssignment);
            return dayAssignment.getHours();
        }
        return 0;
    }

    private static int hoursCanWorkOnDay(final Resource resource,
            final LocalDate date, int alreadyWorked) {
        final ResourceCalendar calendar = resource.getCalendar();
        int hoursCanAllocate = calendar.toHours(date, ONE_RESOURCE_PER_DAY);
        return hoursCanAllocate - alreadyWorked;
    }

    private static DayAssignment generateDayAssignment(
            final ResourceAllocation<?> resourceAllocation,
            Resource resource,
            final LocalDate date, int intentedHours) {

        final ResourceCalendar calendar = resource.getCalendar();

        int hoursCanAllocate = calendar.toHours(date, ONE_RESOURCE_PER_DAY);
        if (hoursCanAllocate > 0) {
            int hoursToAllocate = Math.min(intentedHours, hoursCanAllocate);
            return createDayAssignment(resourceAllocation, resource, date, hoursToAllocate);
        }
        return null;
    }

    public static DateAndHour startTimeToAllocateStartingFromEnd(
            ResourceAllocation<?> resourceAllocation, Resource resource,
            LimitingResourceQueueElementGap gap) {

        // Last element, time is end of last element (gap.starttime)
        if (gap.getEndTime() == null) {
            return gap.getStartTime();
        }

        final List<DayAssignment> dayAssignments = LimitingResourceAllocator
                .generateDayAssignmentsStartingFromEnd(resourceAllocation,
                        resource, gap.getEndTime());
        final DateAndHour[] startAndEnd = LimitingResourceAllocator
                .calculateStartAndEndTime(dayAssignments);
        return startAndEnd[1];
    }

}
