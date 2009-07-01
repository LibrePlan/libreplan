package org.zkoss.ganttz.util.zoom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePeriod;
import org.zkoss.ganttz.util.Interval;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class TimeTrackerStateUsingJodaTime extends TimeTrackerState {

    protected static LocalDate asLocalDate(Date date) {
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

    @Override
    protected Collection<DetailItem> createDetailsForFirstLevel(
            Interval interval) {
        return createDetails(getRealIntervalFor(interval),
                getPeriodFirstLevel(), getDetailItemCreatorFirstLevel());
    }

    @Override
    protected Collection<DetailItem> createDetailsForSecondLevel(
            Interval interval) {
        return createDetails(getRealIntervalFor(interval),
                getPeriodSecondLevel(), getDetailItemCreatorSecondLevel());
    }

    protected abstract IDetailItemCreator getDetailItemCreatorFirstLevel();

    protected abstract ReadablePeriod getPeriodFirstLevel();

    protected abstract IDetailItemCreator getDetailItemCreatorSecondLevel();

    protected abstract ReadablePeriod getPeriodSecondLevel();

    protected abstract LocalDate round(LocalDate date, boolean down);

    @Override
    public Interval getRealIntervalFor(Interval testInterval) {
        LocalDate start = round(asLocalDate(testInterval.getStart()), true);
        LocalDate finish = round(asLocalDate(testInterval.getFinish()), false);
        return new Interval(start.toDateTimeAtStartOfDay().toDate(), finish
                .toDateTimeAtStartOfDay().toDate());
    }
}
