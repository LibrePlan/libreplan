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

package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang.Validate;

public class ResourcesPerDay {

    public static ResourcesPerDay calculateFrom(int hoursWorking, int workableHours) {
        return amount(new BigDecimal(hoursWorking).divide(new BigDecimal(
                workableHours), 2, RoundingMode.HALF_UP));
    }

    public static ResourcesPerDay amount(int amount) {
        return new ResourcesPerDay(new BigDecimal(amount));
    }

    public static ResourcesPerDay amount(BigDecimal decimal) {
        return new ResourcesPerDay(decimal);
    }

    private final BigDecimal amount;

    private ResourcesPerDay(BigDecimal amount) {
        Validate.isTrue(amount.intValue() >= 0);
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public int asHoursGivenResourceWorkingDayOf(
            Integer resourceWorkingDayHours) {
        return getAmount().multiply(new BigDecimal(resourceWorkingDayHours))
                .setScale(0, RoundingMode.HALF_UP).intValue();
    }

    @Override
    public int hashCode() {
        return amount.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof ResourcesPerDay) {
            ResourcesPerDay other = (ResourcesPerDay) obj;
            return amount.equals(other.getAmount());
        }
        return false;
    }

    public boolean isZero() {
        BigDecimal withoutDecimalpart = amount.movePointRight(2);
        return withoutDecimalpart.intValue() == 0;
    }

    @Override
    public String toString() {
        return amount.toString();
    }


}
