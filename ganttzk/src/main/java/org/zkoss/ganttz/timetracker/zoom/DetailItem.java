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

    private String bankHolidayWeek;

    public String getBankHolidayWeek() {
        return bankHolidayWeek;
    }

    public void setBankHolidayWeek(String bankHolidayWeek) {
        this.bankHolidayWeek = bankHolidayWeek;
    }

    private boolean currentPeriod;
    private int currentDayOffset;

    private boolean deadlinePeriod;
    private int deadlineOffset;

    private DateTime startDate;
    private DateTime endDate;

    public DetailItem(int size, String name, DateTime startDate,
            DateTime endDate) {
        this(size, name, false);
        this.startDate = startDate;
        this.endDate = endDate;
        this.markCurrentDay();
    }

    public DetailItem(int size, String name) {
        this(size, name, false);
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

    public DetailItem(int size, String name, int currentdayoffset,
            int deadlineoffset) {
        this(size, name, currentdayoffset);
        this.deadlinePeriod = true;
        this.deadlineOffset = deadlineoffset;
    }

    public void markCurrentDay() {
        if (this.startDate.isBeforeNow() && this.endDate.isAfterNow()) {
            int offsetInPx = Math.round((((float) Days.daysBetween(
                    this.startDate, new DateTime()).getDays()) / ((float) Days
                    .daysBetween(this.startDate, this.endDate).getDays()))
                    * this.size);
            this.markCurrentDay(offsetInPx);
        }
    }

    public void markDeadlineDay(DateTime deadline) {
        if (!this.startDate.isAfter(deadline) && deadline.isBefore(endDate)) {
            int offsetInPx = Math.round((((float) Days.daysBetween(
                    this.startDate, deadline).getDays()) / ((float) Days
                    .daysBetween(this.startDate, this.endDate).getDays()))
                    * this.size);
            // Management of left border case for current line format
            this.markDeadlineDay(Math.min(this.size - 1, offsetInPx));
        }
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

    public void markDeadlineDay(int offset) {
        this.deadlinePeriod = true;
        this.deadlineOffset = offset;
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

    public boolean isDeadlinePeriod() {
        return deadlinePeriod;
    }

    public int getDeadlineOffset() {
        return deadlineOffset;
    }

    public String getBackgroundOffset() {
        String offset = "0px";
        if (getCurrentDayOffset() != 0) {
            if (getDeadlineOffset() != 0) {
                offset = getDeadlineOffset() + "px";
            } else {
                offset = getCurrentDayOffset() + "px";
            }
        } else if (getDeadlineOffset() != 0) {
            offset = getDeadlineOffset() + "px";
        }
        return offset;
    }

    public void markBankHolidayWeek(String result) {
        setBankHolidayWeek(result);
    }

    public void markBankHoliday() {
        setBankHoliday(true);
    }

}