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

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePeriod;

/**
 * Zoom level for weeks in the first level and days in the second level
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class DetailFiveTimeTrackerState extends
        TimeTrackerStateWithSubintervalsFitting {

    private static final int NUMBER_OF_DAYS_MINIMUM = 50;
    public static final int FIRST_LEVEL_SIZE = 210;
    public static final int SECOND_LEVEL_SIZE = 30;

    DetailFiveTimeTrackerState(IDetailItemModificator firstLevelModificator,
            IDetailItemModificator secondLevelModificator) {
        super(firstLevelModificator, secondLevelModificator);
    }

    public final double daysPerPixel() {
        return ((double) 1 / SECOND_LEVEL_SIZE);
    }

    @Override
    protected IDetailItemCreator getDetailItemCreatorFirstLevel() {
        return new IDetailItemCreator() {

            @Override
            public DetailItem create(DateTime dateTime) {
                return new DetailItem(FIRST_LEVEL_SIZE, dateTime
                        .getWeekOfWeekyear()
                        + dateTime.toString(", MMM YYYY"), dateTime, dateTime
                        .plusDays(7));
            }
        };
    }

    @Override
    protected IDetailItemCreator getDetailItemCreatorSecondLevel() {
        return new IDetailItemCreator() {

            @Override
            public DetailItem create(DateTime dateTime) {
                return new DetailItem(SECOND_LEVEL_SIZE, dateTime
                        .getDayOfMonth()
                        + "", dateTime, dateTime.plusDays(1));
            }
        };
    }

    @Override
    protected ReadablePeriod getPeriodFirstLevel() {
        return Days.days(7);
    }

    @Override
    protected ReadablePeriod getPeriodSecondLevel() {
        return Days.days(1);
    }

    @Override
    protected LocalDate round(LocalDate date, boolean down) {
        int dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == 1) {
            return date;
        }
        return down ? date.withDayOfWeek(1) : date.withDayOfWeek(1)
                .plusWeeks(1);
    }

    @Override
    protected Period getMinimumPeriod() {
        return PeriodType.DAYS.amount(NUMBER_OF_DAYS_MINIMUM);
    }

    @Override
    protected ZoomLevel getZoomLevel() {
        return ZoomLevel.DETAIL_FIVE;
    }

    @Override
    public int getSecondLevelSize() {
        return SECOND_LEVEL_SIZE;
    }

}