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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import org.joda.time.DateTime;
import org.zkoss.ganttz.util.Interval;

/**
 * Zoom level with years in the first level and quarters in the second level
 * @author Francisco Javier Moran Rúa
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class DetailTwoTimeTrackerState extends TimeTrackerState {

    private static final int FIRST_LEVEL_ITEM_SIZE = 400;
    private static final int SECOND_LEVEL_ITEM_SIZE = 100;

    protected DetailTwoTimeTrackerState(
            IDetailItemModificator firstLevelModificator,
            IDetailItemModificator secondLevelModificator) {
        super(firstLevelModificator, secondLevelModificator);
    }

    public final double daysPerPixel() {
        return ((double) 365 / FIRST_LEVEL_ITEM_SIZE);
    }

    public Interval getRealIntervalFor(Interval interval) {
        int[] pairYears = calculateInitialEndYear(interval.getStart(), interval
                .getFinish());
        int startQuarter = calculateInQuarterPeriodDateInYear(interval
                .getStart(), pairYears[0]);
        int endQuarter = calculateInQuarterPeriodDateInYear(interval
                .getFinish(), pairYears[1]);
        return new Interval(quarterAt(startQuarter - 1, year(pairYears[0])),
                quarterAt(endQuarter, year(pairYears[1])));
    }

    private static Date quarterAt(int quarter, Date date) {
        int year = from(date).get(Calendar.YEAR);
        Calendar calendar = from(year(year));
        calendar.add(Calendar.MONTH, 3 * quarter);
        return calendar.getTime();
    }

    static Calendar from(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    private Collection<DetailItem> buildCollectionDetailsFirstLevel(
            Date initialDate, Date endDate, int initialYear, int endYear) {

        Collection<DetailItem> detailsVector = new Vector<DetailItem>();

        // Calculate the size of the first detail of the first level
        int quarter = calculateInQuarterPeriodDateInYear(initialDate,
                initialYear);
        detailsVector.add(new DetailItem((4 - (quarter - 1))
                * FIRST_LEVEL_ITEM_SIZE / 4, String.valueOf(initialYear)));

        for (int i = (initialYear + 1); i < endYear; i++) {
            DetailItem d = new DetailItem(FIRST_LEVEL_ITEM_SIZE, String
                    .valueOf(i));
            detailsVector.add(d);
        }

        // Calculate the size of the last detail of the first level
        int endQuarter = calculateInQuarterPeriodDateInYear(endDate, endYear);
        detailsVector
                .add(new DetailItem(endQuarter * FIRST_LEVEL_ITEM_SIZE / 4,
                        String.valueOf(endYear)));

        return detailsVector;

    }


    private Collection<DetailItem> buildCollectionDetailsSecondLevel(
            Date initialDate, Date endDate, int initialYear, int endYear) {

        ArrayList<DetailItem> result = new ArrayList<DetailItem>();

        DateTime beginInterval = new DateTime(initialDate);
        DateTime endInterval = beginInterval.plusMonths(3);
        int startDateQuarter = calculateInQuarterPeriodDateInYear(initialDate,
                initialYear);
        int quarterEndDate = calculateInQuarterPeriodDateInYear(endDate,endYear);

        for (int j = initialYear; j <= endYear; j++) {
            final int initialQuarter = (j == initialYear) ? startDateQuarter - 1
                    : 0;
            final int endQuarter = (j == endYear) ? quarterEndDate : 4;
            for (int i = initialQuarter; i < endQuarter; i++) {
                DetailItem quarter = new DetailItem(SECOND_LEVEL_ITEM_SIZE, "Q"
                        + (i + 1),
                            beginInterval, endInterval);
                result.add(quarter);
                beginInterval = beginInterval.plusMonths(3);
                endInterval = endInterval.plusMonths(3);
            }
        }
        return result;
    }

    /**
     * @param date
     * @param year
     * @return a number from 1(quarter until to 1st April) to 4(quarter until
     *         1st January of the next year) showing the quarter in which the
     *         date is for the year
     */
    private int calculateInQuarterPeriodDateInYear(Date date, int year) {
        Date[] quarters = createQuartersForYear(year);
        for (int i = 0; i < quarters.length; i++) {
            if (date.before(quarters[i])) {
                return i + 1;
            }
        }
        throw new IllegalArgumentException("date " + date + " is not in year "
                + year);
    }

    private static Date[] createQuartersForYear(int year) {
        Date yearDate = year(year);
        Date[] result = new Date[4];
        for (int i = 0; i < result.length; i++) {
            result[i] = quarterAt(i + 1, yearDate);
        }
        return result;
    }

    @Override
    protected Collection<DetailItem> createDetailsForFirstLevel(
            Interval interval) {
        int[] pairYears = calculateInitialEndYear(interval.getStart(), interval
                .getFinish());
        return buildCollectionDetailsFirstLevel(interval.getStart(), interval
                .getFinish(), pairYears[0], pairYears[1]);

    }

    @Override
    protected Collection<DetailItem> createDetailsForSecondLevel(
            Interval interval) {
        int[] pairYears = calculateInitialEndYear(interval.getStart(), interval
                .getFinish());
        return buildCollectionDetailsSecondLevel(interval.getStart(), interval
                .getFinish(), pairYears[0], pairYears[1]);

    }

}
