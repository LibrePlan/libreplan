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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Months;

/**
 * Zoom level for months and years and weeks in the second level
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class DetailFourTimeTrackerState extends TimeTrackerState {

    private static final int NUMBER_OF_WEEKS_MINIMUM = 40;

    DetailFourTimeTrackerState(IDetailItemModificator firstLevelModificator,
            IDetailItemModificator secondLevelModificator) {
        super(firstLevelModificator, secondLevelModificator);
    }

    private static final int SECOND_LEVEL_SIZE = 56;

    public final double pixelPerDay() {
        return (SECOND_LEVEL_SIZE / (double) 7);
    }

    public final double daysPerPixel() {
        return ((double) 7 / SECOND_LEVEL_SIZE);
    }


    private IDetailItemCreator firstLevelCreator;

    @Override
    protected IDetailItemCreator getDetailItemCreatorFirstLevel() {
        firstLevelCreator = new IDetailItemCreator() {

            @Override
            public DetailItem create(DateTime dateTime) {
                return new DetailItem(getSizeMonth(dateTime), dateTime
                        .toString("MMMM,YYYY"), dateTime, dateTime
                        .plusMonths(1));
            }
        };
        return firstLevelCreator;
    }

    @Override
    protected IDetailItemCreator getDetailItemCreatorSecondLevel() {
        return new IDetailItemCreator() {

            @Override
            public DetailItem create(DateTime dateTime) {
                int daysUntilFirstDayNextWeek = getDaysUntilFirstDayNextWeek(dateTime
                        .toLocalDate());
                int sizeWeek = new BigDecimal(pixelPerDay()
                        * daysUntilFirstDayNextWeek).intValue();

                return new DetailItem(sizeWeek, dateTime.getWeekOfWeekyear()
                        + "", dateTime,
                        dateTime.plusDays(daysUntilFirstDayNextWeek));
            }
        };
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

    private int getSizeMonth(DateTime dateTime) {
        Calendar cal = new GregorianCalendar(dateTime.getYear(), dateTime
                .getMonthOfYear() - 1, dateTime.getDayOfMonth());
        // Get the number of days in that month
        int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        return new BigDecimal(pixelPerDay() * days).intValue();
    }

    @Override
    protected Iterator<LocalDate> getPeriodsFirstLevelGenerator(LocalDate start) {
        return new LazyGenerator<LocalDate>(start) {

            @Override
            protected LocalDate next(LocalDate last) {
                return last.plus(Months.ONE);
            }
        };
    }

    @Override
    protected Iterator<LocalDate> getPeriodsSecondLevelGenerator(LocalDate start) {
        return new LazyGenerator<LocalDate>(start) {

            @Override
            protected LocalDate next(LocalDate last) {
                if (last.getDayOfWeek() != 1) {
                    return last.plusDays(getDaysUntilFirstDayNextWeek(last));
                } else {
                    return last.plusWeeks(1);
                }
            }
        };
    }

    private int getDaysUntilFirstDayNextWeek(LocalDate date) {
        return 8 - date.getDayOfWeek();
    }
}
