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

package org.navalplanner.business.workingday;

import java.util.EnumMap;

import org.apache.commons.lang.Validate;

public class EffortDuration implements Comparable<EffortDuration> {


    public enum Granularity {
        HOURS(3600), MINUTES(60), SECONDS(1);

        static Granularity[] fromMoreCoarseToLessCoarse() {
            return Granularity.values();
        }

        private final int secondsPerUnit;

        private Granularity(int secondsPerUnit) {
            this.secondsPerUnit = secondsPerUnit;
        }

        public int toSeconds(int amount) {
            return secondsPerUnit * amount;
        }

        public int convertFromSeconds(int seconds) {
            return seconds / secondsPerUnit;
        }
    }

    public static EffortDuration zero() {
        return elapsing(0, Granularity.SECONDS);
    }

    public static EffortDuration elapsing(int amount, Granularity granularity) {
        return new EffortDuration(granularity.toSeconds(amount));
    }

    public static EffortDuration hours(int amount) {
        return elapsing(amount, Granularity.HOURS);
    }

    public static EffortDuration minutes(int amount) {
        return elapsing(amount, Granularity.MINUTES);
    }

    public static EffortDuration seconds(int amount) {
        return elapsing(amount, Granularity.SECONDS);
    }

    private final int seconds;

    private EffortDuration(int seconds) {
        Validate.isTrue(seconds >= 0, "seconds cannot be negative");
        this.seconds = seconds;
    }

    public int getHours() {
        return convertTo(Granularity.HOURS);
    }

    public int getMinutes() {
        return convertTo(Granularity.MINUTES);
    }

    public int getSeconds() {
        return convertTo(Granularity.SECONDS);
    }

    public int convertTo(Granularity granularity) {
        return granularity.convertFromSeconds(seconds);
    }

    public EffortDuration and(int amount, Granularity granularity) {
        return new EffortDuration(seconds + granularity.toSeconds(amount));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EffortDuration) {
            EffortDuration other = (EffortDuration) obj;
            return getSeconds() == other.getSeconds();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getSeconds();
    }

    public EnumMap<Granularity, Integer> decompose() {
        EnumMap<Granularity, Integer> result = new EnumMap<EffortDuration.Granularity, Integer>(
                Granularity.class);
        int remainder = seconds;
        for (Granularity each : Granularity.fromMoreCoarseToLessCoarse()) {
            int value = each.convertFromSeconds(remainder);
            remainder -= value * each.toSeconds(1);
            result.put(each, value);
        }
        assert remainder == 0;
        return result;
    }

    @Override
    public int compareTo(EffortDuration other) {
        Validate.notNull(other);
        return seconds - other.seconds;
    }

}
