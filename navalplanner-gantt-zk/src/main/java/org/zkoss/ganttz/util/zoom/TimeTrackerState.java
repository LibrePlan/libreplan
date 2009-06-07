/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zkoss.ganttz.util.zoom;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.zkoss.ganttz.util.Interval;

/**
 *
 * @author Francisco Javier Moran Rúa
 *
 */
public abstract class TimeTrackerState {

    protected static final long MILLSECONDS_IN_DAY = 1000 * 60 * 60 * 24;
    protected static final int NUMBER_OF_ITEMS_MINIMUM = 10;

    /**
     * This class is conceived as an immutable class.
     *
     * @author Francisco Javier Moran Rúa
     *
     */
    public final static class DetailItem {

        private int size;
        private String name;

        private final boolean even;

        public DetailItem(int size, String name) {
            this(size, name, false);
        }

        public DetailItem(int size, String name, boolean even) {
            this.size = size;
            this.name = name;
            this.even = even;
        }

        /**
         * @return the size
         */
        public int getSize() {
            return size;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        public DetailItem markEven(boolean even) {
            return new DetailItem(size, name, even);
        }

        public boolean isEven() {
            return even;
        }

    }

    public Collection<DetailItem> getFirstLevelDetails(Interval interval) {
        return markEvens(createDetailsForFirstLevel(interval));
    }

    private static List<DetailItem> markEvens(
            Collection<? extends DetailItem> items) {
        boolean even = false;
        ArrayList<DetailItem> result = new ArrayList<DetailItem>();
        for (DetailItem detailItem : items) {
            result.add(detailItem.markEven(even));
            even = !even;
        }
        return result;

    }

    protected abstract Collection<DetailItem> createDetailsForFirstLevel(
            Interval interval);

    protected abstract Collection<DetailItem> createDetailsForSecondLevel(
            Interval interval);

    public Collection<DetailItem> getSecondLevelDetails(Interval interval) {
        return markEvens(createDetailsForSecondLevel(interval));
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

        System.out.println("Initial date:" + initialDate);
        System.out.println("End date:" + endDate);
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

}
