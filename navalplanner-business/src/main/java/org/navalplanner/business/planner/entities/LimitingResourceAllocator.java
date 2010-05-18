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

        // Iterate through queue elements
        int pos = 0;
        do {
            LimitingResourceQueueElementGap gap = getGapInQueueAtPosition(
                    resource, elements, element, pos++);

            // The queue cannot hold this element (queue.resource
            // doesn't meet element.criteria)
            if (gap == null) {
                return null;
            }

            if (canFitIntoGap(element, gap, resource)) {
                return gap;
            }
        } while (pos <= size);

        return null;
    }

    private static boolean canFitIntoGap(LimitingResourceQueueElement element,
            LimitingResourceQueueElementGap gap, final Resource resource) {

        final boolean canfit = gap.canFit(element);
        final ResourceAllocation<?> resourceAllocation = element
                .getResourceAllocation();

        if (resourceAllocation instanceof SpecificResourceAllocation) {
            return canfit;
        } else if (resourceAllocation instanceof GenericResourceAllocation) {
            // Resource must satisfy element.criteria during for the
            // period of time the element will be allocated in the
            // queue
            final GenericResourceAllocation generic = (GenericResourceAllocation) resourceAllocation;
            List<DayAssignment> dayAssignments = generateDayAssignments(
                    resourceAllocation, resource, gap.getStartTime());
            DateAndHour[] startAndEndTime = calculateStartAndEndTime(dayAssignments);
            return canfit
                    && (satisfiesCriteriaDuringInterval(resource, generic
                            .getCriterions(), startAndEndTime));
        }
        return false;
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

    private static boolean satisfiesCriteriaDuringInterval(Resource resource, Set<Criterion> criteria, DateAndHour[] interval) {
        final Date startDate = interval[0].getDate().toDateTimeAtStartOfDay().toDate();
        final Date endDate = interval[1].getDate().toDateTimeAtStartOfDay().toDate();
        return satisfiesCriteriaDuringInterval(resource, criteria, startDate, endDate);
    }

    private static boolean satisfiesCriteriaDuringInterval(Resource resource, Set<Criterion> criteria, Date startDate, Date endDate) {
        ICriterion compositedCriterion = CriterionCompounder.buildAnd(criteria)
                .getResult();
        return compositedCriterion.isSatisfiedBy(resource, startDate, endDate);
    }

    private static LimitingResourceQueueElementGap getGapInQueueAtPosition(
            Resource resource, List<LimitingResourceQueueElement> elements,
            LimitingResourceQueueElement element, int pos) {

        final int size = elements.size();
        final DateAndHour startTimeBecauseOfGantt = getStartTimeBecauseOfGantt(element);

        if (size == 0) {
            return createLastGap(element, null, resource);
        }

        if (pos == size) {
            return createLastGap(element, elements.get(size - 1), resource);
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
            return createLastGap(element, current, resource);
        }

    }

    private static DateAndHour getStartTimeBecauseOfGantt(LimitingResourceQueueElement element) {
        return new DateAndHour(new LocalDate(element.getEarlierStartDateBecauseOfGantt()), 0);
    }

    private static LimitingResourceQueueElementGap createLastGap(
            LimitingResourceQueueElement candidate,
            LimitingResourceQueueElement lastElement, Resource resource) {

        final DateAndHour queueEndTime = (lastElement != null) ? lastElement
                .getEndTime() : null;
        DateAndHour startTime = DateAndHour.Max(
                getStartTimeBecauseOfGantt(candidate), queueEndTime);
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

}
