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

package org.navalplanner.business.workingday;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.Fraction;

/**
 * <p>
 * It represents some amount of effort. It's composed by some hours, minutes and
 * seconds. Less granularity than a second can't be specified.
 * </p>
 * <p>
 * This object can represent the predicted amount of work that a task takes, the
 * scheduled amount of work for a working day, the amount of effort that a
 * worker can work in a given day, etc.
 * </p>
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
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

    private static final Pattern lenientEffortDurationSpecification = Pattern
            .compile("(\\d+)(\\s*:\\s*\\d+\\s*)*");

    private static final Pattern contiguousDigitsPattern = Pattern
            .compile("\\d+");

    /**
     * If an {@link EffortDuration} can't be parsed <code>null</code> is
     * returned. The hours field at least is required, the next fields are the
     * minutes and seconds. If there is more than one field, they are separated
     * by colons.
     *
     * @param string
     * @return
     */
    public static EffortDuration parseFromFormattedString(String string) {
        Matcher matcher = lenientEffortDurationSpecification.matcher(string);
        if (matcher.find()) {
            List<String> parts = scan(contiguousDigitsPattern, string);
            assert parts.size() >= 1;
            return EffortDuration.hours(retrieveNumber(0, parts))
                    .and(retrieveNumber(1, parts), Granularity.MINUTES)
                    .and(retrieveNumber(2, parts), Granularity.SECONDS);
        }
        return null;
    }

    private static List<String> scan(Pattern pattern, String text) {
        List<String> result = new ArrayList<String>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    private static int retrieveNumber(int i, List<String> parts) {
        if (i >= parts.size()) {
            return 0;
        }
        return Integer.parseInt(parts.get(i));
    }

    public interface IEffortFrom<T> {

        public EffortDuration from(T each);
    }

    public static <T> EffortDuration sum(Iterable<? extends T> collection,
            IEffortFrom<T> effortFrom) {
        EffortDuration result = zero();
        for (T each : collection) {
            result = result.plus(effortFrom.from(each));
        }
        return result;
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

    /**
     * Multiplies this duration by a scalar <br />
     * <b>Warning:<b /> This method can cause an integer overflow and the result
     * would be incorrect.
     * @param n
     * @return a duration that is the multiply of n and <code>this</code>
     */
    public EffortDuration multiplyBy(int n) {
        return EffortDuration.seconds(this.seconds * n);
    }

    /**
     * Divides this duration by a scalar
     * @param n
     *            a number greater than zero
     * @return a new duration that is the result of dividing <code>this</code>
     *         by n
     */
    public EffortDuration divideBy(int n) {
        Validate.isTrue(n > 0);
        return new EffortDuration(seconds / n);
    }

    /**
     * <p>
     * Divides this duration by other returning the quotient.
     * </p>
     * There can be a remainder left.
     * @see #remainderFor(EffortDuration)
     * @param other
     * @return
     */
    public int divideBy(EffortDuration other) {
        return seconds / other.seconds;
    }

    public Fraction divivedBy(EffortDuration effortAssigned) {
        return Fraction.getFraction(this.seconds, effortAssigned.seconds);
    }

    /**
     * Calculates the remainder resulting of doing the integer division of both
     * durations
     *
     * @see #divideBy(EffortDuration)
     * @param other
     * @return the remainder
     */
    public EffortDuration remainderFor(EffortDuration other) {
        int dividend = divideBy(other);
        return this.minus(other.multiplyBy(dividend));
    }

    /**
     * Pluses two {@link EffortDuration}. <br />
     * <b>Warning:<b /> This method can cause an integer overflow and the result
     * would be incorrect.
     * @param other
     * @return a duration that is the sum of <code>this</code>
     *         {@link EffortDuration} and the other duration
     */
    public EffortDuration plus(EffortDuration other) {
        return new EffortDuration(seconds + other.seconds);
    }

    public boolean isZero() {
        return seconds == 0;
    }

    /**
     * Substracts two {@link EffortDuration}. Because {@link EffortDuration
     * durations} cannot be negative <code>this</code> must be bigger than the
     * parameter or the same
     *
     * @param duration
     * @return the result of substracting the two durations
     * @throws IllegalArgumentException
     *             if the parameter is bigger than <code>this</code>
     */
    public EffortDuration minus(EffortDuration duration) {
        Validate.isTrue(this.compareTo(duration) >= 0,
                "minued must not be smaller than subtrahend");
        return new EffortDuration(seconds - duration.seconds);
    }

    public BigDecimal toHoursAsDecimalWithScale(int scale) {
        BigDecimal result = BigDecimal.ZERO;
        final BigDecimal secondsPerHour = new BigDecimal(3600);
        for (Entry<Granularity, Integer> each : decompose().entrySet()) {
            BigDecimal seconds = new BigDecimal(each.getKey().toSeconds(
                    each.getValue()));
            result = result.add(seconds.divide(secondsPerHour, scale,
                    BigDecimal.ROUND_HALF_UP));
        }
        return result;
    }

    /**
     * <p>
     * Converts this duration in a number of hours. Uses a typical half up
     * round, so for example one hour and half is converted to two hours. There
     * is an exception though, when the duration is less than one hour and is
     * not zero it's returned one. This is handy for avoiding infinite loops in
     * some algorithms; when all code is converted to use {@link EffortDuration
     * Effort Durations} this will no longer be necessary.
     * </p>
     * So there are three cases:
     * <ul>
     * <li>the duration is zero, 0 is returned</li>
     * <li>if duration > 0 and duration < 1, 1 is returned</li>
     * <li>if duration >= 1, typical half up round is done. For example 1 hour
     * and 20 minutes returns 1 hour, 1 hour and 30 minutes 2 hours</li>
     * </ul>
     *
     * @return an integer number of hours
     */
    public int roundToHours() {
        if (this.isZero()) {
            return 0;
        }
        return Math.max(1, roundHalfUpToHours(this.decompose()));
    }

    public static EffortDuration min(EffortDuration... durations) {
        return Collections.min(Arrays.asList(durations));
    }

    public static EffortDuration max(EffortDuration... durations) {
        return Collections.max(Arrays.asList(durations));
    }

    private static int roundHalfUpToHours(
            EnumMap<Granularity, Integer> components) {
        int seconds = components.get(Granularity.SECONDS);
        int minutes = components.get(Granularity.MINUTES)
                + (seconds < 30 ? 0 : 1);
        int hours = components.get(Granularity.HOURS) + (minutes < 30 ? 0 : 1);
        return hours;
    }

    public String toString() {
        EnumMap<Granularity, Integer> valuesForEachUnit = decompose();
        Integer hours = valuesForEachUnit.get(Granularity.HOURS);
        Integer minutes = valuesForEachUnit.get(Granularity.MINUTES);
        Integer seconds = valuesForEachUnit.get(Granularity.SECONDS);
        return hours + ":" + minutes + ":" + seconds;
    }

    public String toFormattedString() {
        EnumMap<Granularity, Integer> byGranularity = this.decompose();
        int hours = byGranularity.get(Granularity.HOURS);
        int minutes = byGranularity.get(Granularity.MINUTES);
        int seconds = byGranularity.get(Granularity.SECONDS);
        if (minutes == 0 && seconds == 0) {
            return String.format("%s", hours);
        } else if (seconds == 0) {
            return String.format("%s:%s", hours, minutes);
        } else {
            return String.format("%s:%s:%s", hours, minutes, seconds);
        }
    }

}
