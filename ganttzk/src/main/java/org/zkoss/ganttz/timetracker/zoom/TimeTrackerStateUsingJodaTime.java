/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.joda.time.base.BaseSingleFieldPeriod;
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

    public Collection<DetailItem> createDetails(Interval interval,
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

    public enum PeriodType {
        YEARS {
            @Override
            public ReadablePeriod toPeriod(int amount) {
                return Years.years(amount);
            }

            @Override
            public Years differenceBetween(LocalDate start,
                    LocalDate end) {
                return Years.yearsBetween(start, end);
            }
        },
        MONTHS {
            @Override
            public ReadablePeriod toPeriod(int amount) {
                return Months.months(amount);
            }

            @Override
            public Months differenceBetween(LocalDate start,
                    LocalDate end) {
                return Months.monthsBetween(start, end);
            }
        },
        WEEKS {
            @Override
            public ReadablePeriod toPeriod(int amount) {
                return Weeks.weeks(amount);
            }

            @Override
            public Weeks differenceBetween(LocalDate start,
                    LocalDate end) {
                return Weeks.weeksBetween(start, end);
            }
        },
        DAYS {
            @Override
            public ReadablePeriod toPeriod(int amount) {
                return Days.days(amount);
            }

            @Override
            public Days differenceBetween(LocalDate start,
                    LocalDate end) {
                return Days.daysBetween(start, end);
            }
        };

        public abstract ReadablePeriod toPeriod(int amount);

        public abstract BaseSingleFieldPeriod differenceBetween(LocalDate start,
                LocalDate end);

        public Period amount(int amount) {
            return new Period(this, amount);
        }

    }

    static class Period {

        private final PeriodType type;

        private final int amount;

        private Period(PeriodType type, int amount) {
            this.type = type;
            this.amount = amount;
        }

        ReadablePeriod toPeriod() {
            return this.type.toPeriod(amount);
        }

        BaseSingleFieldPeriod asPeriod(Interval interval) {
            LocalDate start = LocalDate.fromDateFields(interval.getStart());
            LocalDate end = LocalDate.fromDateFields(interval.getFinish());
            return type.differenceBetween(start, end);
        }
    }

    protected abstract Period getMinimumPeriod();

    private Interval calculateIntervalWithMinimum(Interval interval) {
        Period minimumPeriod = getMinimumPeriod();
        BaseSingleFieldPeriod intervalAsPeriod = minimumPeriod
                .asPeriod(interval);
        if (intervalAsPeriod
                .compareTo(minimumPeriod.toPeriod()) >= 0) {
            return interval;
        }
        LocalDate newEnd = new LocalDate(interval.getStart())
                .plus(minimumPeriod.toPeriod());
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
