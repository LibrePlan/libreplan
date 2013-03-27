/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.business.common.entities;

import static org.libreplan.business.i18n.I18nHelper._;

import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Weeks;

/**
 * Different values for personal timesheets periodicity.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public enum PersonalTimesheetsPeriodicityEnum {

    MONTHLY(_("Monthly")) {
        @Override
        public LocalDate getStart(LocalDate date) {
            return date.dayOfMonth().withMinimumValue();
        }

        @Override
        public LocalDate getEnd(LocalDate date) {
            return date.dayOfMonth().withMaximumValue();
        }

        @Override
        public int getItemsBetween(LocalDate start, LocalDate end) {
            return Months.monthsBetween(start, end).getMonths();
        }

        @Override
        public LocalDate getDateForItemFromDate(int item, LocalDate fromDate) {
            return fromDate.plusMonths(item);
        }

        @Override
        public LocalDate previous(LocalDate date) {
            return getStart(date).minusMonths(1);
        }

        @Override
        public LocalDate next(LocalDate date) {
            return getStart(date).plusMonths(1);
        }
    },
    TWICE_MONTHLY(_("Twice-monthly")) {
        @Override
        public LocalDate getStart(LocalDate date) {
            if (date.getDayOfMonth() <= 15) {
                return date.dayOfMonth().withMinimumValue();
            } else {
                return date.dayOfMonth().withMinimumValue().plusDays(15);
            }
        }

        @Override
        public LocalDate getEnd(LocalDate date) {
            if (date.getDayOfMonth() <= 15) {
                return date.dayOfMonth().withMinimumValue().plusDays(14);
            } else {
                return date.dayOfMonth().withMaximumValue();
            }
        }

        @Override
        public int getItemsBetween(LocalDate start, LocalDate end) {
            return Months.monthsBetween(start, end).getMonths() * 2;
        }

        @Override
        public LocalDate getDateForItemFromDate(int item, LocalDate fromDate) {
            int months = (item % 2 == 0) ? (item / 2) : ((item - 1) / 2);
            LocalDate date = fromDate.plusMonths(months);
            if (item % 2 != 0) {
                if (date.getDayOfMonth() <= 15) {
                    date = date.dayOfMonth().withMinimumValue().plusDays(15);
                } else {
                    date = date.plusMonths(1).dayOfMonth().withMinimumValue();
                }
            }
            return date;
        }

        @Override
        public LocalDate previous(LocalDate date) {
            if (date.getDayOfMonth() <= 15) {
                return date.minusMonths(1).dayOfMonth().withMinimumValue()
                        .plusDays(15);
            } else {
                return date.dayOfMonth().withMinimumValue();
            }
        }

        @Override
        public LocalDate next(LocalDate date) {
            if (date.getDayOfMonth() <= 15) {
                return date.dayOfMonth().withMinimumValue().plusDays(15);
            } else {
                return date.plusMonths(1).dayOfMonth().withMinimumValue();
            }
        }
    },
    WEEKLY(_("Weekly")) {
        @Override
        public LocalDate getStart(LocalDate date) {
            return date.dayOfWeek().withMinimumValue();
        }

        @Override
        public LocalDate getEnd(LocalDate date) {
            return date.dayOfWeek().withMaximumValue();
        }

        @Override
        public int getItemsBetween(LocalDate start, LocalDate end) {
            return Weeks.weeksBetween(start, end).getWeeks();
        }

        @Override
        public LocalDate getDateForItemFromDate(int item, LocalDate fromDate) {
            return fromDate.plusWeeks(item);
        }

        @Override
        public LocalDate previous(LocalDate date) {
            return getStart(date).minusWeeks(1);
        }

        @Override
        public LocalDate next(LocalDate date) {
            return getStart(date).plusWeeks(1);
        }
    };

    private String name;

    private PersonalTimesheetsPeriodicityEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the start date of the personal timesheet which includes the
     * specified <code>date</code>.
     */
    public abstract LocalDate getStart(LocalDate date);

    /**
     * Returns the end date of the personal timesheet which includes the
     * specified <code>date</code>.
     */
    public abstract LocalDate getEnd(LocalDate date);

    /**
     * Returns the number of personal timesheets between the specified dates.
     */
    public abstract int getItemsBetween(LocalDate start, LocalDate end);

    /**
     * Returns the date of the personal timesheet in the position specified by
     * <code>item</code> taking into account the <code>fromDate</code>.
     */
    public abstract LocalDate getDateForItemFromDate(int item,
            LocalDate fromDate);

    /**
     * Returns the date of the previous personal timesheet to the one which
     * includes the specified <code>date</code>.
     */
    public abstract LocalDate previous(LocalDate date);

    /**
     * Returns the date of the next personal timesheet to the one which includes
     * the specified <code>date</code>.
     */
    public abstract LocalDate next(LocalDate date);

}
