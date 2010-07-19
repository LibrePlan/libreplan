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

package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.common.ProportionalDistributor;

public class ResourcesPerDay {

    public static class ResourcesPerDayDistributor {

        private final ProportionalDistributor distributor;

        private ResourcesPerDayDistributor(ProportionalDistributor distributor) {
            this.distributor = distributor;
        }

        public ResourcesPerDay[] distribute(ResourcesPerDay total) {
            int[] shares = distributor.distribute(toIntFormat(total));
            ResourcesPerDay[] result = new ResourcesPerDay[shares.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = backToResourcePerDay(shares[i]);
            }
            return result;
        }

    }

    public static ResourcesPerDayDistributor distributor(
            ResourcesPerDay... resourcesPerDay) {
        ProportionalDistributor distributor = ProportionalDistributor
                .create(asInts(resourcesPerDay));
        return new ResourcesPerDayDistributor(distributor);
    }

    public static ResourcesPerDayDistributor distributor(
            ProportionalDistributor distributor) {
        return new ResourcesPerDayDistributor(distributor);
    }

    private static int[] asInts(ResourcesPerDay[] resourcesPerDay) {
        int[] result = new int[resourcesPerDay.length];
        for (int i = 0; i < resourcesPerDay.length; i++) {
            result[i] = toIntFormat(resourcesPerDay[i]);
        }
        return result;
    }

    private static int toIntFormat(ResourcesPerDay each) {
        return each.amount.unscaledValue().intValue();
    }

    private static ResourcesPerDay backToResourcePerDay(int integerFormat) {
        return amount(new BigDecimal(integerFormat).movePointLeft(2));
    }

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
        BigDecimal multiply = getAmount().multiply(
                new BigDecimal(resourceWorkingDayHours));
        if(multiply.compareTo(BigDecimal.ZERO)>0){
            return Math.max(1, multiply.setScale(0, RoundingMode.HALF_UP)
                    .intValue());
        } else {
            return 0;
        }
    }

    @Override
    public int hashCode() {
        return amount.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
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
