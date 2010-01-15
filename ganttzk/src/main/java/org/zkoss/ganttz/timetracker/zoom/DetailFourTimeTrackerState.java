/*
 * This file is part of ###PROJECT_NAME###
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

import org.jfree.data.time.Month;
import org.joda.time.DateTime;
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
    protected Interval calculateIntervalWithMinimum(Interval candidateInterval) {
        Interval resultInterval;
        LocalDate startDate = LocalDate.fromDateFields(candidateInterval.getStart());
        LocalDate endDate = LocalDate.fromDateFields(candidateInterval.getFinish());
        Weeks numberOfWeeks = Weeks.weeksBetween(startDate.toDateTimeAtCurrentTime(),
                endDate.toDateTimeAtCurrentTime());

        if (numberOfWeeks.getWeeks() < this.NUMBER_OF_WEEKS_MINIMUM) {
            LocalDate endIntervalDate = LocalDate.fromDateFields(candidateInterval.
                    getStart()).
                    toDateMidnight().
                    plusWeeks(this.NUMBER_OF_WEEKS_MINIMUM).toLocalDate();
            LocalDate roundedEndIntervalDate = roundToNextYear(endIntervalDate);

            resultInterval = new Interval(candidateInterval.getStart(),
                    roundedEndIntervalDate.toDateMidnight().toDate());
        } else {
            resultInterval = candidateInterval;
        }

        return resultInterval;
    }

}
