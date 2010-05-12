package org.navalplanner.business.planner.entities;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.resources.entities.Resource;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class LimitingResourceQueueElementGap {

    private DateAndHour startTime;

    private DateAndHour endTime;

    private Integer hoursInGap;

    public LimitingResourceQueueElementGap(Resource resource, DateAndHour startTime,
            DateAndHour endTime) {
        this(resource, startTime.getDate(), startTime.getHour(), endTime.getDate(), endTime.getHour());
    }

    public LimitingResourceQueueElementGap(Resource resource, LocalDate startDate,
            int startHour, LocalDate endDate, int endHour) {

        final ResourceCalendar calendar = resource.getCalendar();

        // Calculate hours in range of dates
        if (startDate.equals(endDate)) {
            hoursInGap = endHour - startHour;
        } else {
            int hoursAtStart = calendar.getCapacityAt(startDate)
                    - startHour;
            int hoursInBetween = calendar.getWorkableHours(startDate
                    .plusDays(1), endDate.minusDays(1));
            hoursInGap = hoursAtStart + hoursInBetween + endHour;
        }

        // Set start and end time for gap
        startTime = new DateAndHour(startDate, startHour);
        endTime = new DateAndHour(endDate, endHour);
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
        final LocalDate earlierStartDateBecauseOfGantt = new LocalDate(
                candidate.getEarlierStartDateBecauseOfGantt());
        final LocalDate startDate = startTime.getDate();
        if (earlierStartDateBecauseOfGantt.isBefore(startDate)
                || earlierStartDateBecauseOfGantt.isEqual(startDate)) {
            return hoursInGap - candidate.getIntentedTotalHours() >= 0;
        }
        return false;
    }

    public String toString() {
        return startTime.getDate() + " - " + startTime.getHour() + "; "
                + endTime.getDate() + " - " + endTime.getHour();
    }

}
