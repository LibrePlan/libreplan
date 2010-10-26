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

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.zkoss.util.Locales;

/**
 * Zoom level with semesters in the first level and months in the second level
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class DetailThreeTimeTrackerState extends
        TimeTrackerStateWithSubintervalsFitting {

    private static final int NUMBER_OF_MONTHS_MINIMUM = 20;

    DetailThreeTimeTrackerState(IDetailItemModificator firstLevelModificator,
            IDetailItemModificator secondLevelModificator) {
        super(firstLevelModificator, secondLevelModificator);
    }

    private static final int FIRST_LEVEL_SIZE = 300;
    protected static final int SECOND_LEVEL_SIZE = 50;

    public final double daysPerPixel() {
        return ((double) 182.5 / FIRST_LEVEL_SIZE);
    }

    @Override
    protected ReadablePeriod getPeriodFirstLevel() {
        return Months.months(6);
    }

    @Override
    protected ReadablePeriod getPeriodSecondLevel() {
        return Months.months(1);
    }

    @Override
    protected IDetailItemCreator getDetailItemCreatorSecondLevel() {
        return new IDetailItemCreator() {

            @Override
            public DetailItem create(DateTime dateTime) {
                return new DetailItem(SECOND_LEVEL_SIZE,
                        getMonthString(dateTime),
                        dateTime, dateTime.plusMonths(1));
            }
        };
    }

    @Override
    protected LocalDate round(LocalDate date, boolean down) {
        if (date.getMonthOfYear() == 1 && date.getDayOfMonth() == 1) {
            return date;
        }
        if (date.getMonthOfYear() == 7 && date.getDayOfMonth() == 1) {
            return date;
        }
        date = date.withDayOfMonth(1);
        if (date.getMonthOfYear() < 7) {
            return down ? date.withMonthOfYear(1) : date.withMonthOfYear(7);
        } else {
            return down ? date.withMonthOfYear(7) : date.plusYears(1)
                    .withMonthOfYear(1);
        }
    }

    private String getMonthString(DateTime dateTime) {
        return dateTime.toString("MMM", Locales.getCurrent());
    }

    private String getYearWithSemesterString(DateTime dateTime) {
        return dateTime.getYear() + ","
                + (dateTime.getMonthOfYear() < 6 ? "H1" : "H2");
    }

    @Override
    protected IDetailItemCreator getDetailItemCreatorFirstLevel() {
        return new IDetailItemCreator() {
            @Override
            public DetailItem create(DateTime dateTime) {
                return new DetailItem(FIRST_LEVEL_SIZE,
                        getYearWithSemesterString(dateTime), dateTime, dateTime
                                .plusMonths(6));
            }
        };
    }

    @Override
    protected Period getMinimumPeriod() {
        return PeriodType.MONTHS.amount(NUMBER_OF_MONTHS_MINIMUM);
    }

    @Override
    protected ZoomLevel getZoomLevel() {
        return ZoomLevel.DETAIL_FOUR;
    }

    @Override
    public int getSecondLevelSize() {
        return SECOND_LEVEL_SIZE;
    }

}