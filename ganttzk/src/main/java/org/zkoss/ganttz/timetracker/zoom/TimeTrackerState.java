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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.zkoss.ganttz.util.Interval;

/**
 * @author Francisco Javier Moran Rúa <jmoran@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public abstract class TimeTrackerState {

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

    protected abstract Collection<DetailItem> createDetailsForFirstLevel(
            Interval interval);

    protected abstract Collection<DetailItem> createDetailsForSecondLevel(
            Interval interval);

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

    protected static int[] calculateInitialEndYear(Date initialDate,
            Date endDate) {

        int[] pairYears = new int[2];

        long yearsInBetween = calculateYearsBetween(initialDate, endDate);
        Calendar cal = new GregorianCalendar();
        cal.setTime(initialDate);
        int initialYear = cal.get(Calendar.YEAR);
        int endYear;

        if (yearsInBetween >= NUMBER_OF_ITEMS_MINIMUM) {
            cal.setTime(endDate);
            endYear = cal.get(Calendar.YEAR);
        } else {
            endYear = initialYear + NUMBER_OF_ITEMS_MINIMUM;
        }

        pairYears[0] = initialYear;
        pairYears[1] = endYear;

        return pairYears;
    }

    protected static long calculateYearsBetween(Date initialDate, Date endDate) {

        long milsecondsDiff = endDate.getTime() - initialDate.getTime();

        // To chech later: If you put MILLSECONDS_IN_YEAR the
        // division is made wrongly.

        long days = milsecondsDiff / MILLSECONDS_IN_DAY;
        return (days / 365);
    }

    public static Date year(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    public abstract Interval getRealIntervalFor(Interval testInterval);

    public abstract double daysPerPixel();

    protected abstract ZoomLevel getZoomLevel();

    public abstract int getSecondLevelSize();

}
