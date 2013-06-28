/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 Igalia, S.L.
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
package org.libreplan.business.recurring;


/*
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 *
 */
public class RecurrenceInformation {

    private int repetitions = 0;
    private RecurrencePeriodicity recurrencePeriodicity;

    public RecurrenceInformation() {
        this.repetitions = 0;
        this.recurrencePeriodicity = RecurrencePeriodicity.NO_PERIODICTY;
    }

    public RecurrenceInformation(int repetitions,
            RecurrencePeriodicity periodicity) {
        this.repetitions = repetitions;
        this.recurrencePeriodicity = periodicity;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public RecurrencePeriodicity getPeriodicity() {
        return recurrencePeriodicity;
    }

    public void setPeriodicity(RecurrencePeriodicity periodicity) {
        this.recurrencePeriodicity = periodicity;
    }

}