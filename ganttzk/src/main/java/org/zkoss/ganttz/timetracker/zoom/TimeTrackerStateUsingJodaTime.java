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

package org.zkoss.ganttz.timetracker.zoom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePeriod;
import org.zkoss.ganttz.util.Interval;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class TimeTrackerStateUsingJodaTime extends TimeTrackerState {

    TimeTrackerStateUsingJodaTime(
            IDetailItemModificator firstLevelModificator,
            IDetailItemModificator secondLevelModificator) {
        super(firstLevelModificator, secondLevelModificator);
    }

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

    protected abstract Days getMinimumPeriod();

    private Interval calculateIntervalWithMinimum(Interval interval) {
        ReadablePeriod minimumPeriod = getMinimumPeriod();
        Days intervalDays = asPeriod(interval);
        if (intervalDays.compareTo(minimumPeriod) >= 0) {
            return interval;
        }
        LocalDate newEnd = new LocalDate(interval.getStart())
                .plus(minimumPeriod);
        return new Interval(interval.getStart(), newEnd
                .toDateTimeAtStartOfDay().toDate());
    }

    private Days asPeriod(Interval interval) {
        DateTime start = new DateTime(interval.getStart());
        DateTime finish = new DateTime(interval.getFinish());
        return Days.daysBetween(start, finish);
    }

    @Override
    public Interval getRealIntervalFor(Interval testInterval) {
        return calculateForAtLeastMinimum(calculateIntervalWithMinimum(testInterval));
    }

    private Interval calculateForAtLeastMinimum(Interval atLeastMinimum) {
        LocalDate start = round(asLocalDate(atLeastMinimum.getStart()), true);
        LocalDate finish = round(asLocalDate(atLeastMinimum.getFinish()), false);
        Interval result = new Interval(start.toDateTimeAtStartOfDay().toDate(),
                finish
                .toDateTimeAtStartOfDay().toDate());
        return result;
    }
}
