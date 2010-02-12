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

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.joda.time.Weeks;

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

    private static final int FIRST_LEVEL_SIZE = 200;
    private static final int SECOND_LEVEL_SIZE = 50;

    public final double daysPerPixel() {
        return ((double) 7 / SECOND_LEVEL_SIZE);
    }


    @Override
    protected IDetailItemCreator getDetailItemCreatorFirstLevel() {
        return new IDetailItemCreator() {

            @Override
            public DetailItem create(DateTime dateTime) {
                return new DetailItem(FIRST_LEVEL_SIZE, dateTime
                        .toString("MMMM,YYYY"));
            }
        };
    }

    @Override
    protected IDetailItemCreator getDetailItemCreatorSecondLevel() {
        return new IDetailItemCreator() {

            @Override
            public DetailItem create(DateTime dateTime) {
                return new DetailItem(SECOND_LEVEL_SIZE, dateTime
                        .getWeekOfWeekyear()
                        + "",dateTime,dateTime.plusWeeks(1));
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
    protected Days getMinimumPeriod() {
        return Days.days(7 * NUMBER_OF_WEEKS_MINIMUM);
    }

    @Override
    protected ZoomLevel getZoomLevel() {
        return ZoomLevel.DETAIL_THREE;
    }

    @Override
    public int getSecondLevelSize() {
        return SECOND_LEVEL_SIZE;
    }

}
