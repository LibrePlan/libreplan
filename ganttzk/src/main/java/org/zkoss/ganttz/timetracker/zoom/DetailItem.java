package org.zkoss.ganttz.timetracker.zoom;

import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 * One of each of the subintervals a time line is divided into
 * @author Francisco Javier Moran Rúa <jmoran@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public final class DetailItem {

    private int size;
    private String name;

    private boolean even;
    private boolean bankHoliday;

    private boolean currentPeriod;
    private int currentDayOffset;

    private DateTime startDate;

    private DateTime endDate;

    public DetailItem(int size, String name) {
        this(size, name, false);
    }

    public DetailItem(int size, String name, DateTime startDate,
            DateTime endDate) {
        this(size, name, false);
        this.startDate = startDate;
        this.endDate = endDate;
        this.markCurrentDay();
    }

    public void markCurrentDay() {
        if (this.startDate.isBeforeNow() && this.endDate.isAfterNow()) {
            int offsetInPx = Math
                    .round((((float) Days.daysBetween(this.startDate,
                            new DateTime()).getDays()) / ((float) Days
                            .daysBetween(this.startDate, this.endDate)
                            .getDays()))
                            * this.size);
            this.markCurrentDay(offsetInPx);
        }
    }

    public DetailItem(int size, String name, boolean even) {
        this.size = size;
        this.name = name;
        this.even = even;
        this.currentPeriod = false;
        this.currentDayOffset = 0;
    }

    public DetailItem(int size, String name, int currentdayoffset) {
        this.size = size;
        this.name = name;
        this.even = false;
        this.bankHoliday = false;
        this.currentPeriod = true;
        this.currentDayOffset = currentdayoffset;
    }

    public int getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEven(boolean even) {
        this.even = even;
    }

    public void markCurrentDay(int offset) {
        this.currentPeriod = true;
        this.currentDayOffset = offset;
    }

    public boolean isEven() {
        return even;
    }

    public boolean isBankHoliday() {
        return bankHoliday;
    }

    public void setBankHoliday(boolean bankHoliday) {
        this.bankHoliday = bankHoliday;
    }

    public boolean isCurrentPeriod() {
        return currentPeriod;
    }

    public int getCurrentDayOffset() {
        return currentDayOffset;
    }

}