package org.zkoss.ganttz.util.zoom;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePeriod;

/**
 * Zoom level for weeks in the first level and days in the second level
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DetailFiveTimeTrackerState extends TimeTrackerStateUsingJodaTime {

    public static final int FIRST_LEVEL_SIZE = 140;

    public static final int SECOND_LEVEL_SIZE = 20;

    public static final DetailFiveTimeTrackerState INSTANCE = new DetailFiveTimeTrackerState();

    private DetailFiveTimeTrackerState() {

    }

    @Override
    protected IDetailItemCreator getDetailItemCreatorFirstLevel() {
        return new IDetailItemCreator() {

            @Override
            public DetailItem create(DateTime dateTime) {
                return new DetailItem(FIRST_LEVEL_SIZE, dateTime
                        .getWeekOfWeekyear()
                        + dateTime.toString(", MMM YYYY"));
            }
        };
    }

    @Override
    protected IDetailItemCreator getDetailItemCreatorSecondLevel() {
        return new IDetailItemCreator() {

            @Override
            public DetailItem create(DateTime dateTime) {
                return new DetailItem(SECOND_LEVEL_SIZE, dateTime
                        .getDayOfMonth()
                        + "", dateTime, dateTime.plusDays(1));
            }
        };
    }

    @Override
    protected ReadablePeriod getPeriodFirstLevel() {
        return Days.days(7);
    }

    @Override
    protected ReadablePeriod getPeriodSecondLevel() {
        return Days.days(1);
    }

    @Override
    protected LocalDate round(LocalDate date, boolean down) {
        int dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == 1)
            return date;
        return down ? date.withDayOfWeek(1) : date.withDayOfWeek(1)
                .plusWeeks(1);
    }
}