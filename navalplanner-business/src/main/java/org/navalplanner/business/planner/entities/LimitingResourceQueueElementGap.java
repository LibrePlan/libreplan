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

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.resources.entities.Resource;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class LimitingResourceQueueElementGap implements Comparable<LimitingResourceQueueElementGap> {

    private DateAndHour startTime;

    private DateAndHour endTime;

    private Integer hoursInGap;

    public LimitingResourceQueueElementGap(Resource resource, DateAndHour startTime,
            DateAndHour endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        hoursInGap = calculateHoursInGap(resource, startTime, endTime);
    }

    private Integer calculateHoursInGap(Resource resource, DateAndHour startTime, DateAndHour endTime) {
        return (endTime == null) ? Integer.MAX_VALUE : calculateHoursInGap(
                resource, startTime.getDate(), startTime.getHour(), endTime
                        .getDate(), endTime.getHour());
    }

    public int getHoursInGap() {
        return hoursInGap;
    }

    private Integer calculateHoursInGap(Resource resource, LocalDate startDate,
            int startHour, LocalDate endDate, int endHour) {

        final ResourceCalendar calendar = resource.getCalendar();

        if (startDate.equals(endDate)) {
            return calendar.getCapacityAt(startDate) - Math.max(startHour, endHour);
        } else {
            int hoursAtStart = calendar.getCapacityAt(startDate) - startHour;
            int hoursInBetween = calendar.getWorkableHours(startDate
                    .plusDays(1), endDate.minusDays(1));
            return hoursAtStart + hoursInBetween + endHour;
        }
    }

    public static LimitingResourceQueueElementGap create(Resource resource, DateAndHour startTime,
            DateAndHour endTime) {
        return new LimitingResourceQueueElementGap(resource, startTime, endTime);
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
                .getEarlierStartDateBecauseOfGantt());
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
        String result = startTime.getDate() + " - " + startTime.getHour();
        if (endTime != null) {
            result += "; " + endTime.getDate() + " - " + endTime.getHour();
        }
        return result;
    }

    @Override
    public int compareTo(LimitingResourceQueueElementGap o) {
        if (o == null) {
            return 1;
        }
        return this.getStartTime().compareTo(o.getStartTime());
    }

    public boolean isBefore(LimitingResourceQueueElementGap gap) {
        return (compareTo(gap) < 0);
    }

}
