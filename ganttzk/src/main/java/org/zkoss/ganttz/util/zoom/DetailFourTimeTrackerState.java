package org.zkoss.ganttz.util.zoom;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.joda.time.Weeks;

/**
 * Zoom level for months and years and weeks in the second level
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class DetailFourTimeTrackerState extends TimeTrackerStateUsingJodaTime {

    public static final DetailFourTimeTrackerState INSTANCE = new DetailFourTimeTrackerState();

    private static final int FIRST_LEVEL_SIZE = 200;
    private static final int SECOND_LEVEL_SIZE = 50;

    public final double daysPerPixel() {
        return ((double) 7 / SECOND_LEVEL_SIZE);
    }

    @Override
    protected IDetailItemCreator getDetailItemCreatorFirstLevel() {
        return new IDetailItemCreator() {

            @Override
            public DetailItem create(DateTime dateTime) {
                return new DetailItem(FIRST_LEVEL_SIZE, dateTime
                        .toString("MMMM,YYYY"));
            }
        };
    }

    @Override
    protected IDetailItemCreator getDetailItemCreatorSecondLevel() {
        return new IDetailItemCreator() {

            @Override
            public DetailItem create(DateTime dateTime) {
                return new DetailItem(SECOND_LEVEL_SIZE, dateTime
                        .getWeekOfWeekyear()
                        + "",dateTime,dateTime.plusWeeks(1));
            }
        };
    }

    @Override
    protected ReadablePeriod getPeriodFirstLevel() {
        return Months.months(1);
    }

    @Override
    protected ReadablePeriod getPeriodSecondLevel() {
        return Weeks.weeks(1);
    }

    @Override
    protected LocalDate round(LocalDate date, boolean down) {
        if (date.getDayOfMonth() == 1) {
            return date;
        }
        return down ? date.withDayOfMonth(1) : date.plusMonths(1)
                .withDayOfMonth(1);
    }
}
