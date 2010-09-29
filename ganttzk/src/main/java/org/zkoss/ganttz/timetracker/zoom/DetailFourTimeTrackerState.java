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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.joda.time.Weeks;
import org.zkoss.ganttz.util.Interval;

/**
 * Zoom level for months and years and weeks in the second level
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class DetailFourTimeTrackerState extends TimeTrackerStateUsingJodaTime {

    private static final int NUMBER_OF_WEEKS_MINIMUM = 40;

    DetailFourTimeTrackerState(IDetailItemModificator firstLevelModificator,
            IDetailItemModificator secondLevelModificator) {
        super(firstLevelModificator, secondLevelModificator);
    }

    private static final int FIRST_LEVEL_SIZE = 210;
    private static final int SECOND_LEVEL_SIZE = 56;
    private static final int MAX_DAYS = 8;

    private int daysWeek = 7;

    public final double pixelPerDay() {
        return (SECOND_LEVEL_SIZE / (double) 7);
    }

    public final double daysPerPixel() {
        return ((double) 7 / SECOND_LEVEL_SIZE);
    }


    @Override
    protected IDetailItemCreator getDetailItemCreatorFirstLevel() {
        return new IDetailItemCreator() {

            @Override
            public DetailItem create(DateTime dateTime) {
                return new DetailItem(getSizeMonth(dateTime), dateTime
                        .toString("MMMM,YYYY"), dateTime, dateTime
                        .plusMonths(1));
            }
        };
    }

    @Override
    protected IDetailItemCreator getDetailItemCreatorSecondLevel() {
        return new IDetailItemCreator() {

            @Override
            public DetailItem create(DateTime dateTime) {
                daysWeek = MAX_DAYS - dateTime.getDayOfWeek();
                int sizeWeek = (new BigDecimal(pixelPerDay() * daysWeek))
                        .intValue();

                return new DetailItem(sizeWeek, dateTime
                        .getWeekOfWeekyear()
                        + "", dateTime, dateTime.plusDays(daysWeek));
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

    @Override
    protected Period getMinimumPeriod() {
        return PeriodType.WEEKS.amount(NUMBER_OF_WEEKS_MINIMUM);
    }

    @Override
    protected ZoomLevel getZoomLevel() {
        return ZoomLevel.DETAIL_THREE;
    }

    @Override
    public int getSecondLevelSize() {
        return SECOND_LEVEL_SIZE;
    }

    @Override
    public Collection<DetailItem> createDetails(Interval interval,
            ReadablePeriod period, IDetailItemCreator detailItemCreator) {
        if (period.equals(getPeriodFirstLevel())) {
            return super.createDetails(interval, period, detailItemCreator);
        } else {
            return createDetails(interval, detailItemCreator);
        }
    }

    private Collection<DetailItem> createDetails(Interval interval,
            IDetailItemCreator detailItemCreator) {
        DateTime current = asLocalDate(interval.getStart())
                .toDateTimeAtStartOfDay();
        DateTime end = asLocalDate(interval.getFinish())
                .toDateTimeAtStartOfDay();
        List<DetailItem> result = new ArrayList<DetailItem>();
        while (current.isBefore(end)) {
            result.add(detailItemCreator.create(current));
            current = current.plus(Days.days(daysWeek));
        }
        return result;
    }

    private int getSizeMonth(DateTime dateTime) {
        Calendar cal = new GregorianCalendar(dateTime.getYear(), dateTime
                .getMonthOfYear() - 1, dateTime.getDayOfMonth());
        // Get the number of days in that month
        int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        return new BigDecimal(pixelPerDay() * days).intValue();
    }
}
