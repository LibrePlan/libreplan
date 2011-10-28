/*
 * This file is part of LibrePlan
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

package org.libreplan.business.planner.entities;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Share {

    private final int hours;

    public Share(int hours) {
        this.hours = hours;
    }

    public int getHours() {
        return hours;
    }

    public Share plus(int increment) {
        return new Share(hours + increment);
    }

    @Override
    public String toString() {
        return hours + "";
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hours).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Share) {
            Share otherShare = (Share) obj;
            return new EqualsBuilder().append(hours, otherShare.hours)
                    .isEquals();
        }
        return false;
    }

}
