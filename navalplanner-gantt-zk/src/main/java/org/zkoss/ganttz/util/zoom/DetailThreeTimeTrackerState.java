package org.zkoss.ganttz.util.zoom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.util.Locales;

/**
 * Zoom level with semesters in the first level and months in the second level
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DetailThreeTimeTrackerState extends TimeTrackerState {

    public static final DetailThreeTimeTrackerState INSTANCE = new DetailThreeTimeTrackerState();

    private static final int FIRST_LEVEL_SIZE = 300;
    protected static final int SECOND_LEVEL_SIZE = 50;

    private static LocalDate asLocalDate(Date date) {
        return new LocalDate(date);
    }

    public interface IDetailItemCreator {
        DetailItem create(DateTime dateTime);
    }

    public static Collection<DetailItem> createDetails(Interval interval,
            ReadablePeriod period, IDetailItemCreator detailItemCreator) {
        DateTime current = asLocalDate(interval.getStart())
                .toDateTimeAtStartOfDay();
        DateTime end = asLocalDate(interval.getFinish())
                .toDateTimeAtStartOfDay();
        List<DetailItem> result = new ArrayList<DetailItem>();
        while (current.isBefore(end)) {
            result.add(detailItemCreator.create(current));
            current = current.plus(period);
        }
        return result;
    }

    private DetailThreeTimeTrackerState() {
    }

    @Override
    protected Collection<DetailItem> createDetailsForFirstLevel(
            Interval interval) {
        return createDetails(getRealIntervalFor(interval), Months.months(6),
                new IDetailItemCreator() {

                    @Override
                    public DetailItem create(DateTime dateTime) {
                        return new DetailItem(FIRST_LEVEL_SIZE,
                                getYearWithSemesterString(dateTime));
                    }
                });
    }

    private String getYearWithSemesterString(DateTime dateTime) {
        return dateTime.getYear() + ","
                + (dateTime.getMonthOfYear() < 6 ? "H1" : "H2");
    }

    @Override
    protected Collection<DetailItem> createDetailsForSecondLevel(
            Interval interval) {
        return createDetails(getRealIntervalFor(interval), Months.months(1),
                new IDetailItemCreator() {

                    @Override
                    public DetailItem create(DateTime dateTime) {
                        return new DetailItem(SECOND_LEVEL_SIZE,
                                getMonthString(dateTime));
                    }
                });
    }

    public LocalDate round(LocalDate date, boolean down) {
        if (date.getMonthOfYear() == 1 && date.getDayOfMonth() == 1)
            return date;
        if (date.getMonthOfYear() == 7 && date.getDayOfMonth() == 1)
            return date;
        date = date.withDayOfMonth(1);
        if (date.getMonthOfYear() < 7) {
            return down ? date.withMonthOfYear(1) : date.withMonthOfYear(7);
        } else {
            return down ? date.withMonthOfYear(7) : date.plusYears(1)
                    .withMonthOfYear(1);
        }
    }

    @Override
    public Interval getRealIntervalFor(Interval testInterval) {
        LocalDate start = round(asLocalDate(testInterval.getStart()), true);
        LocalDate finish = round(asLocalDate(testInterval.getFinish()), false);
        return new Interval(start.toDateTimeAtStartOfDay().toDate(), finish
                .toDateTimeAtStartOfDay().toDate());
    }

    private String getMonthString(DateTime dateTime) {
        return dateTime.toString("MMM", Locales.getCurrent());
    }
}
