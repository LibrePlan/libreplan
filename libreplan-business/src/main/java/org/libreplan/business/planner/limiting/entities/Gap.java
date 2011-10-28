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

package org.libreplan.business.planner.limiting.entities;

import static org.libreplan.business.workingday.EffortDuration.hours;
import static org.libreplan.business.workingday.EffortDuration.zero;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine.DatePoint;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine.EndOfTime;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine.FixedPoint;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine.Interval;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine.StartOfTime;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.calendars.entities.ResourceCalendar;
import org.libreplan.business.planner.entities.AvailabilityCalculator;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.LimitingResourceQueue;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class Gap implements Comparable<Gap> {

    public static class GapOnQueue {

        public static List<GapOnQueue> onQueue(LimitingResourceQueue queue,
                Collection<? extends Gap> gaps) {
            List<GapOnQueue> result = new ArrayList<GapOnQueue>();
            for (Gap each : gaps) {
                result.add(each.onQueue(queue));
            }
            return result;
        }

        public static List<GapOnQueue> onQueue(
                LimitingResourceQueue queue, DateAndHour startTime,
                DateAndHour endTime) {

            Gap gap = (endTime == null || endTime.compareTo(startTime) <= 0) ? Gap
                    .untilEnd(queue.getResource(), startTime) : Gap.create(
                    queue.getResource(), startTime, endTime);
            return GapOnQueue.onQueue(queue, Collections.singleton(gap));
        }

        private final LimitingResourceQueue originQueue;

        private final Gap gap;

        GapOnQueue(LimitingResourceQueue originQueue, Gap gap) {
            this.originQueue = originQueue;
            this.gap = gap;
        }

        public LimitingResourceQueue getOriginQueue() {
            return originQueue;
        }

        public Gap getGap() {
            return gap;
        }

        public List<GapOnQueue> splitIntoGapsSatisfyingCriteria(
                Set<Criterion> criteria) {
            return GapOnQueue.onQueue(originQueue, gap
                    .splitIntoGapsSatisfyingCriteria(originQueue.getResource(),
                            criteria));
        }

        public String toString() {
            return "queue: " + originQueue + "; gap: " + gap;
        }

    }

    /**
     * @author Diego Pino García <dpino@igalia.com>
     *
     *         Stores a {@link GapOnQueue} plus its adjacent
     *         {@link LimitingResourceQueueElement}
     */
    public static class GapOnQueueWithQueueElement {

        private final LimitingResourceQueueElement queueElement;

        private final GapOnQueue gapOnQueue;

        public static GapOnQueueWithQueueElement create(GapOnQueue gapOnQueue,
                LimitingResourceQueueElement queueElement) {
            return new GapOnQueueWithQueueElement(gapOnQueue, queueElement);
        }

        GapOnQueueWithQueueElement(GapOnQueue gapOnQueue, LimitingResourceQueueElement queueElement) {
            this.gapOnQueue = gapOnQueue;
            this.queueElement = queueElement;
        }

        public LimitingResourceQueueElement getQueueElement() {
            return queueElement;
        }

        public GapOnQueue getGapOnQueue() {
            return gapOnQueue;
        }

        /**
         * Joins first.gap + second.gap and keeps second.queueElement as
         * {@link LimitingResourceQueueElement}
         *
         * @param first
         * @param second
         * @return
         */
        public static GapOnQueueWithQueueElement coalesce(
                GapOnQueueWithQueueElement first,
                GapOnQueueWithQueueElement second) {

            LimitingResourceQueue queue = first.getGapOnQueue()
                    .getOriginQueue();
            DateAndHour startTime = first.getGapOnQueue().getGap()
                    .getStartTime();
            DateAndHour endTime = second.getGapOnQueue().getGap().getEndTime();
            Gap coalescedGap = Gap.create(queue.getResource(), startTime,
                    endTime);

            return create(coalescedGap.onQueue(queue), second.getQueueElement());
        }

        public String toString() {
            return "gapOnQueue: " + gapOnQueue + "; queueElement: " + queueElement;
        }

    }

    public static Gap untilEnd(LimitingResourceQueueElement current,
            DateAndHour startInclusive) {
        return untilEnd(current.getResource(), startInclusive);
    }

    private static Gap untilEnd(Resource resource, DateAndHour startInclusive) {
        return new Gap(resource, startInclusive, null);
    }

    private DateAndHour startTime;

    private DateAndHour endTime;

    private Integer hoursInGap;

    public Gap(Resource resource, DateAndHour startTime,
            DateAndHour endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        hoursInGap = calculateHoursInGap(resource, startTime, endTime);
    }

    public GapOnQueue onQueue(LimitingResourceQueue queue) {
        return new GapOnQueue(queue, this);
    }

    private Integer calculateHoursInGap(Resource resource, DateAndHour startTime, DateAndHour endTime) {
        // TODO remove this method. Use GapRequirements instead
        if (endTime == null || startTime == null) {
            // startTime is never null when hours in gap is really use
            return Integer.MAX_VALUE;
        } else {
            return calculateHoursInGap(resource, startTime.getDate(), startTime
                    .getHour(), endTime.getDate(), endTime.getHour());
        }
    }

    public int getHoursInGap() {
        return hoursInGap;
    }

    private Integer calculateHoursInGap(Resource resource, LocalDate startDate,
            int startHour, LocalDate endDate, int endHour) {
        IntraDayDate intraStart = IntraDayDate.create(startDate,
                hours(startHour));
        IntraDayDate intraEnd = IntraDayDate.create(endDate, hours(endHour));
        return calculateHoursInGap(resource, intraStart, intraEnd);
    }

    private Integer calculateHoursInGap(Resource resource, IntraDayDate start,
            IntraDayDate end) {
        final ResourceCalendar calendar = resource.getCalendar();
        Iterable<PartialDay> days = start.daysUntil(end);
        EffortDuration result = zero();
        for (PartialDay each : days) {
            result = result.plus(calendar.getCapacityOn(each));
        }
        return result.roundToHours();
    }

    public List<Integer> getHoursInGapUntilAllocatingAndGoingToTheEnd(
            BaseCalendar calendar,
            DateAndHour realStart, DateAndHour allocationEnd, int total) {

        Validate.isTrue(endTime == null || allocationEnd.compareTo(endTime) <= 0);
        Validate.isTrue(startTime == null
                || realStart.compareTo(startTime) >= 0);
        Validate.isTrue(total >= 0);
        List<Integer> result = new ArrayList<Integer>();

        // If endTime is null (last tasks) assume the end is in 10 years from now
        DateAndHour endDate = getEndTime();
        if (endDate == null) {
            endDate = DateAndHour.TEN_YEARS_FROM(realStart);
        }

        Iterator<PartialDay> daysUntilEnd = realStart.toIntraDayDate()
                .daysUntil(endDate.toIntraDayDate()).iterator();
        while (daysUntilEnd.hasNext()) {
            PartialDay each = daysUntilEnd.next();
            int hoursAtDay = calendar.getCapacityOn(each).roundToHours();
            int hours = Math.min(hoursAtDay, total);
            total -= hours;

            // Don't add hours when total and hours are zero (it'd be like
            // adding an extra 0 hour day when total is completed)
            if (total != 0 || hours != 0) {
                result.add(hours);
            }

            if (total == 0
                    && DateAndHour.from(each.getDate())
                            .compareTo(allocationEnd) >= 0) {
                break;
            }
        }
        return result;
    }

    public static Gap create(Resource resource, DateAndHour startTime,
            DateAndHour endTime) {
        return new Gap(resource, startTime, endTime);
    }

    public DateAndHour getStartTime() {
        return startTime;
    }

    public DateAndHour getEndTime() {
        return endTime;
    }

    /**
     * Returns true if the gap starts after earlierStartDateBecauseOfGantt and
     * if it's big enough for fitting candidate
     *
     * @param hours
     * @return
     */
    public boolean canFit(LimitingResourceQueueElement candidate) {
        LocalDate startAfter = LocalDate.fromDateFields(candidate
                .getEarliestStartDateBecauseOfGantt());
        LocalDate endsAfter = LocalDate.fromDateFields(candidate
                .getEarliestEndDateBecauseOfGantt());

        return canSatisfyStartConstraint(startAfter)
                && canSatisfyEndConstraint(endsAfter)
                && hoursInGap >= candidate.getIntentedTotalHours();
    }

    private boolean canSatisfyStartConstraint(final LocalDate startsAfter) {
        return startsAfter.compareTo(startTime.getDate()) <= 0;
    }

    private boolean canSatisfyEndConstraint(LocalDate endsAfter) {
        return endTime == null || endsAfter.compareTo(endTime.getDate()) <= 0;
    }

    public String toString() {
        String result = "";

        if (startTime != null) {
            result = startTime.getDate() + " - " + startTime.getHour();
        }
        if (endTime != null) {
            result += "; " + endTime.getDate() + " - " + endTime.getHour();
        }
        return result;
    }

    @Override
    public int compareTo(Gap other) {
        if (other == null) {
            return 1;
        }
        if (this.getStartTime() == null && other.getStartTime() == null) {
            return 0;
        } else if (this.getStartTime() == null) {
            return -1;
        } else if (other.getStartTime() == null) {
            return 1;
        }
        return this.getStartTime().compareTo(other.getStartTime());
    }

    public boolean isBefore(Gap gap) {
        return (compareTo(gap) < 0);
    }

    public List<Gap> splitIntoGapsSatisfyingCriteria(Resource resource,
            Set<Criterion> criteria) {
        return splitIntoGapsSatisfyingCriteria(resource, criteria,
                getStartTime(), getEndTime());
    }

    /**
     * Returns a set of {@link Gap} composed by those gaps which satisfy
     * <em>criteria</em> within the period: <em>gapStartTime</em> till
     * <em>gapEndTime</em>
     * @param resource
     * @param criteria
     *            criteria to be satisfied by resource
     * @param gapStartTime
     *            start time of gap
     * @param gapEndTime
     *            end time of gap
     * @return
     */
    private static List<Gap> splitIntoGapsSatisfyingCriteria(Resource resource,
            Set<Criterion> criteria, DateAndHour gapStartTime,
            DateAndHour gapEndTime) {
        AvailabilityTimeLine criterionsAvailability = AvailabilityCalculator
                .getCriterionsAvailabilityFor(criteria, resource);
        if (gapStartTime != null) {
            criterionsAvailability.invalidUntil(gapStartTime.getDate());
        }
        if (gapEndTime != null) {
            criterionsAvailability.invalidFrom(gapEndTime.getDate());
        }
        List<Interval> validPeriods = criterionsAvailability.getValidPeriods();
        List<Gap> result = new ArrayList<Gap>();
        for (Interval each : validPeriods) {
            result.add(createGap(resource, each, gapStartTime, gapEndTime));
        }
        return result;
    }

    private static Gap createGap(Resource resource, Interval interval,
            DateAndHour originalGapStartTime, DateAndHour originalGapEndTime) {
        DateAndHour start = convert(originalGapStartTime, interval.getStart());
        DateAndHour end = convert(originalGapEndTime, interval.getEnd());
        return Gap.create(resource, start, end);
    }

    private static DateAndHour convert(DateAndHour possibleMatch,
            DatePoint datePoint) {
        if (datePoint instanceof StartOfTime || datePoint instanceof EndOfTime) {
            return null;
        }
        FixedPoint p = (FixedPoint) datePoint;
        if (possibleMatch != null
                && possibleMatch.getDate().equals(p.getDate())) {
            return possibleMatch;
        }
        return DateAndHour.from(p.getDate());
    }

}
