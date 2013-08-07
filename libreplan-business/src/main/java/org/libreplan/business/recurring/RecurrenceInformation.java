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

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 *
 */
public class RecurrenceInformation {

    public static RecurrenceInformation noRecurrence() {
        return new RecurrenceInformation(0, RecurrencePeriodicity.NO_PERIODICTY);
    }

    private int repetitions;
    private RecurrencePeriodicity recurrencePeriodicity;

    public RecurrenceInformation() {
        this.repetitions = 0;
        this.recurrencePeriodicity = RecurrencePeriodicity.NO_PERIODICTY;
    }

    public RecurrenceInformation(int numberRepetitions,
            RecurrencePeriodicity recurrencePeriodicity) {
        Validate.notNull(recurrencePeriodicity);
        Validate.isTrue(numberRepetitions >= 0,
                "the number of repetitions cannot be negative. It is: "
                        + numberRepetitions);
        this.recurrencePeriodicity = recurrencePeriodicity;
        this.repetitions = recurrencePeriodicity
                .limitRepetitions(numberRepetitions);
    }

    public int getRepetitions() {
        return repetitions;
    }

    public RecurrencePeriodicity getPeriodicity() {
        return recurrencePeriodicity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RecurrenceInformation) {
            RecurrenceInformation other = (RecurrenceInformation) obj;
            return new EqualsBuilder().append(repetitions, other.repetitions)
                    .append(recurrencePeriodicity, other.recurrencePeriodicity)
                    .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(repetitions)
                .append(recurrencePeriodicity)
                .toHashCode();
    }

}