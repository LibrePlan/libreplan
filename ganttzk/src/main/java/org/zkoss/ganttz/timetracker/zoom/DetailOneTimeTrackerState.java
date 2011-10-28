/*
 * This file is part of LibrePlan
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

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.joda.time.Years;


/**
 * Zoom level with years in the first level and semesters in the second level
 * @author Francisco Javier Moran Rúa
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class DetailOneTimeTrackerState extends
        TimeTrackerStateWithSubintervalsFitting {

    public static final Period MINIMUN_PERIOD = PeriodType.YEARS.amount(4);

    private static final int FIRST_LEVEL_ITEM_SIZE = 200;
    private static final int SECOND_LEVEL_ITEM_SIZE = 100;

    public final double daysPerPixel() {
        return ((double) 365 / FIRST_LEVEL_ITEM_SIZE);
    }

    DetailOneTimeTrackerState(IDetailItemModificator firstLevelModificator,
            IDetailItemModificator secondLevelModificator) {
        super(firstLevelModificator, secondLevelModificator);
    }

    @Override
    protected ZoomLevel getZoomLevel() {
        return ZoomLevel.DETAIL_ONE;
    }

    @Override
    public int getSecondLevelSize() {
        return SECOND_LEVEL_ITEM_SIZE;
    }

    @Override
    protected IDetailItemCreator getDetailItemCreatorFirstLevel() {
        return new IDetailItemCreator() {
            @Override
            public DetailItem create(DateTime start) {
                int year = start.getYear();
                DateTime end = new LocalDate(year + 1, 1, 1)
                        .toDateTimeAtStartOfDay();
                return new DetailItem(FIRST_LEVEL_ITEM_SIZE, start.getYear()
                        + "", start, end);
            }
        };
    }

    @Override
    protected ReadablePeriod getPeriodFirstLevel() {
        return Years.ONE;
    }

    @Override
    protected IDetailItemCreator getDetailItemCreatorSecondLevel() {
        return new IDetailItemCreator() {

            @Override
            public DetailItem create(DateTime dateTime) {
                return new DetailItem(SECOND_LEVEL_ITEM_SIZE,
                        dateTime.getMonthOfYear() == 1 ? "H1" : "H2", dateTime,
                        dateTime.plusMonths(6));
            }
        };
    }

    @Override
    protected ReadablePeriod getPeriodSecondLevel() {
        return Months.SIX;
    }

    @Override
    protected LocalDate round(LocalDate date, boolean down) {
        return doYearRound(date, down);
    }

    public static LocalDate doYearRound(LocalDate date, boolean down) {
        return new LocalDate(date.getYear() + (down?0:1), 1, 1);
    }

    @Override
    protected Period getMinimumPeriod() {
        return MINIMUN_PERIOD;
    }

}
