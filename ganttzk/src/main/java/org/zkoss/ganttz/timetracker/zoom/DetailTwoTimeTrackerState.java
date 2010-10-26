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
import org.joda.time.Years;

/**
 * Zoom level with years in the first level and quarters in the second level
 * @author Francisco Javier Moran Rúa
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class DetailTwoTimeTrackerState extends
        TimeTrackerStateWithSubintervalsFitting {

    private static final int FIRST_LEVEL_ITEM_SIZE = 400;
    private static final int SECOND_LEVEL_ITEM_SIZE = 100;

    protected DetailTwoTimeTrackerState(
            IDetailItemModificator firstLevelModificator,
            IDetailItemModificator secondLevelModificator) {
        super(firstLevelModificator, secondLevelModificator);
    }

    @Override
    protected IDetailItemCreator getDetailItemCreatorFirstLevel() {
        return new IDetailItemCreator() {
            @Override
            public DetailItem create(DateTime dateTime) {
                return new DetailItem(FIRST_LEVEL_ITEM_SIZE, dateTime.getYear()
                        + "", dateTime, dateTime);
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
                int quarterNumber = dateTime.getMonthOfYear() / 3 + 1;
                String quarterCaption = "Q" + quarterNumber;
                return new DetailItem(SECOND_LEVEL_ITEM_SIZE, quarterCaption,
                        dateTime, dateTime.plusMonths(3));
            }
        };
    }

    @Override
    protected ReadablePeriod getPeriodSecondLevel() {
        return Months.THREE;
    }

    @Override
    protected LocalDate round(LocalDate date, boolean down) {
        return DetailOneTimeTrackerState.doYearRound(date, down);
    }

    @Override
    protected Period getMinimumPeriod() {
        return DetailOneTimeTrackerState.MINIMUN_PERIOD;
    }

    @Override
    public double daysPerPixel() {
        return ((double) 365 / FIRST_LEVEL_ITEM_SIZE);
    }

    @Override
    protected ZoomLevel getZoomLevel() {
        return ZoomLevel.DETAIL_TWO;
    }

    @Override
    public int getSecondLevelSize() {
        return SECOND_LEVEL_ITEM_SIZE;
    }

}
