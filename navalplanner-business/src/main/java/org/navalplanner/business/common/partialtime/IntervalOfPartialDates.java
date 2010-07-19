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

package org.navalplanner.business.common.partialtime;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class IntervalOfPartialDates {

    private final PartialDate start;

    private final PartialDate end;

    public IntervalOfPartialDates(PartialDate start, PartialDate end) {
        if (!start.getGranularity().equals(end.getGranularity())) {
            throw new IllegalArgumentException(
                    "the from and the to must have the same granularity");
        }
        if (!start.before(end)) {
            throw new IllegalArgumentException(
                    "the start must be before the end");
        }
        this.start = start;
        this.end = end;
    }

    public PartialDate getStart() {
        return this.start;
    }

    public PartialDate getEnd() {
        return this.end;
    }

    public TimeQuantity getDuration() {
        return end.quantityFrom(start);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof IntervalOfPartialDates) {
            IntervalOfPartialDates other = (IntervalOfPartialDates) obj;
            return new EqualsBuilder().append(this.start, other.start).append(
                    this.end, other.end).isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(start).append(end).toHashCode();
    }
}