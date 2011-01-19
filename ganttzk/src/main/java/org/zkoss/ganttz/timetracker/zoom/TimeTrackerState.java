/*
 * This file is part of NavalPlan
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

package org.zkoss.ganttz.timetracker.zoom;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
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
 * @author Francisco Javier Moran Rúa <jmoran@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public abstract class TimeTrackerState {

    public static Date year(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    public static abstract class LazyGenerator<T> implements Iterator<T> {

        private T current;

        protected LazyGenerator(T first) {
            this.current = first;
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public T next() {
            return this.current = next(this.current);
        }

        protected abstract T next(T last);

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    protected static final long MILLSECONDS_IN_DAY = 1000 * 60 * 60 * 24;

    // Pending to calculate interval dinamically
    protected static final int NUMBER_OF_ITEMS_MINIMUM = 4;

    private final IDetailItemModificator firstLevelModificator;

    private final IDetailItemModificator secondLevelModificator;

    protected TimeTrackerState(IDetailItemModificator firstLevelModificator,
            IDetailItemModificator secondLevelModificator) {
        this.firstLevelModificator = firstLevelModificator;
        this.secondLevelModificator = secondLevelModificator;
    }

    // When applied after setting current day, removes extra data as current day
    // or bank holidays, and must proccess the array twice. May be refactorized
    private static List<DetailItem> markEvens(
            Collection<? extends DetailItem> items) {
        boolean even = false;
        ArrayList<DetailItem> result = new ArrayList<DetailItem>();

        for (DetailItem detailItem : items) {
            detailItem.setEven(even);
            result.add(detailItem);
            even = !even;
        }
        return result;
    }

    protected static LocalDate asLocalDate(Date date) {
        return new LocalDate(date);
    }

    public interface IDetailItemCreator {
        DetailItem create(DateTime dateTime);
    }

    public Collection<DetailItem> getSecondLevelDetails(Interval interval) {
        if (getZoomLevel() == ZoomLevel.DETAIL_FIVE) {
            // Evens are not highlighted in day view
            return applyConfiguredModifications(
                    secondLevelModificator,
                    createDetailsForSecondLevel(interval), getZoomLevel());
        } else {
            return markEvens(applyConfiguredModifications(
                    secondLevelModificator,
                    createDetailsForSecondLevel(interval), getZoomLevel()));
        }
    }

    public Collection<DetailItem> getFirstLevelDetails(Interval interval) {
        return applyConfiguredModifications(firstLevelModificator,
                createDetailsForFirstLevel(interval), getZoomLevel());
    }

    private static List<DetailItem> applyConfiguredModifications(
            IDetailItemModificator modificator,
            Collection<? extends DetailItem> detailsItems, ZoomLevel zoomlevel) {
        List<DetailItem> result = new ArrayList<DetailItem>(detailsItems.size());
        for (DetailItem each : detailsItems) {
            result.add(modificator.applyModificationsTo(each, zoomlevel));
        }
        return result;
    }

    private Collection<DetailItem> createDetails(Interval interval,
            Iterator<LocalDate> datesGenerator,
            IDetailItemCreator detailItemCreator) {
        LocalDate current = interval.getStart();
        LocalDate end = interval.getFinish();
        List<DetailItem> result = new ArrayList<DetailItem>();
        while (current.isBefore(end)) {
            result.add(detailItemCreator.create(current
                    .toDateTimeAtStartOfDay()));
            assert datesGenerator.hasNext();
            current = datesGenerator.next();
        }
        return result;
    }

    private final Collection<DetailItem> createDetailsForFirstLevel(
            Interval interval) {
        Interval realInterval = getRealIntervalFor(interval);
        return createDetails(realInterval,
                getPeriodsFirstLevelGenerator(realInterval.getStart()),
                getDetailItemCreatorFirstLevel());
    }

    protected abstract Iterator<LocalDate> getPeriodsFirstLevelGenerator(
            LocalDate start);

    private final Collection<DetailItem> createDetailsForSecondLevel(
            Interval interval) {
        Interval realInterval = getRealIntervalFor(interval);
        return createDetails(realInterval,
                getPeriodsSecondLevelGenerator(realInterval.getStart()),
                getDetailItemCreatorSecondLevel());
    }

    protected abstract Iterator<LocalDate> getPeriodsSecondLevelGenerator(
            LocalDate start);

    protected abstract IDetailItemCreator getDetailItemCreatorFirstLevel();

    protected abstract IDetailItemCreator getDetailItemCreatorSecondLevel();

    protected abstract LocalDate round(LocalDate date, boolean down);

    public enum PeriodType {
        YEARS {
            @Override
            public ReadablePeriod toPeriod(int amount) {
                return Years.years(amount);
            }

            @Override
            public Years differenceBetween(LocalDate start, LocalDate end) {
                return Years.yearsBetween(start, end);
            }
        },
        MONTHS {
            @Override
            public ReadablePeriod toPeriod(int amount) {
                return Months.months(amount);
            }

            @Override
            public Months differenceBetween(LocalDate start, LocalDate end) {
                return Months.monthsBetween(start, end);
            }
        },
        WEEKS {
            @Override
            public ReadablePeriod toPeriod(int amount) {
                return Weeks.weeks(amount);
            }

            @Override
            public Weeks differenceBetween(LocalDate start, LocalDate end) {
                return Weeks.weeksBetween(start, end);
            }
        },
        DAYS {
            @Override
            public ReadablePeriod toPeriod(int amount) {
                return Days.days(amount);
            }

            @Override
            public Days differenceBetween(LocalDate start, LocalDate end) {
                return Days.daysBetween(start, end);
            }
        };

        public abstract ReadablePeriod toPeriod(int amount);

        public abstract BaseSingleFieldPeriod differenceBetween(
                LocalDate start, LocalDate end);

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
            return type.differenceBetween(interval.getStart(),
                    interval.getFinish());
        }
    }

    protected abstract Period getMinimumPeriod();

    private Interval calculateIntervalWithMinimum(Interval interval) {
        Period minimumPeriod = getMinimumPeriod();
        BaseSingleFieldPeriod intervalAsPeriod = minimumPeriod
                .asPeriod(interval);
        if (intervalAsPeriod.compareTo(minimumPeriod.toPeriod()) >= 0) {
            return interval;
        }
        LocalDate newEnd = new LocalDate(interval.getStart())
                .plus(minimumPeriod.toPeriod());
        return new Interval(interval.getStart(), newEnd);
    }

    public Interval getRealIntervalFor(Interval testInterval) {
        return calculateForAtLeastMinimum(calculateIntervalWithMinimum(testInterval));
    }

    private Interval calculateForAtLeastMinimum(Interval atLeastMinimum) {
        LocalDate start = round(atLeastMinimum.getStart(), true);
        LocalDate finish = round(atLeastMinimum.getFinish(), false);
        Interval result = new Interval(start.toDateTimeAtStartOfDay().toDate(),
                finish.toDateTimeAtStartOfDay().toDate());
        return result;
    }

    public abstract double daysPerPixel();

    protected abstract ZoomLevel getZoomLevel();

    public abstract int getSecondLevelSize();

}
