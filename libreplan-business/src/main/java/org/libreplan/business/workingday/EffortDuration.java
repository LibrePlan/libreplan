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

package org.libreplan.business.workingday;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.Fraction;

/**
 * <p>
 * LP AUDIOVISUAL HACK: We don't need to schedule effort in time any more.
 * Instead, we need to schedule money. To be able to reuse the scheduling
 * algorithm with no changes, we have let it think it's working with time but we
 * modify this class to do the conversion between time and money. The
 * granularity level HOURS now is equivalent to the money measured in *cents*.
 * We do that because OrderElements hierarchy stores effort measured in hours in
 * an integer attribute, so we multiply * 100 to prevent the lose of the decimal
 * part.
 * </p>
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
        /**
         * LP AUDIOVISUAL HACK: granularity for the budget of the task, measured
         * in euros (excluding cents).
         */
        EUROS(100),
        /**
         * LP AUDIOVISUAL HACK: hours granularity is equivalent to the budget of
         * the task, measured in cents
         */
        HOURS(1),
        /**
         * LP AUDIOVISUAL HACK: a granularity smaller than hours make no sense
         * now, but it is kept for compatibility
         */
        MINUTES(1),
        /**
         * LP AUDIOVISUAL HACK: agranularity smaller than hours make no sense
         * now, but it is kept for compatibility
         */
        SECONDS(1);

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

    /**
     * If an {@link EffortDuration} can't be parsed <code>null</code> is
     * returned. The hours field at least is required, the next fields are the
     * minutes and seconds. If there is more than one field, they are separated
     * by colons.
     *
     * LP AUDIOVISUAL HACK: now we just expect this string to be a valid
     * decimal.
     *
     * @param string
     * @return
     */
    public static EffortDuration parseFromFormattedString(String string) {
        try {
            // delegate string conversion to Java libraries
            return fromEurosAsBigDecimal(new BigDecimal(string));
        } catch (NumberFormatException e) {
            return null;
        }
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

    public static EffortDuration sum(EffortDuration... summands) {
        return sum(Arrays.asList(summands), new IEffortFrom<EffortDuration>() {

            @Override
            public EffortDuration from(EffortDuration each) {
                return each;
            }
        });
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

    public static EffortDuration euros(int amount) {
        return elapsing(amount, Granularity.EUROS);
    }

    public static EffortDuration fromHoursAsBigDecimal(BigDecimal hours) {
        // TODO watch out where this method is used and how
        return elapsing(hours.intValue(), Granularity.SECONDS);
    }

    public static EffortDuration fromEurosAsBigDecimal(BigDecimal euros) {
        return elapsing(euros.multiply(new BigDecimal(100)).intValue(),
                Granularity.HOURS);
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
     * <p>
     * Divides this duration by other (using total seconds) returning the
     * quotient as BigDecimal.
     * </p>
     * @param other
     * @return
     */
    public BigDecimal dividedByAndResultAsBigDecimal(EffortDuration other) {
        if (other.isZero()) {
            return BigDecimal.ZERO;
        }
        else {
            return new BigDecimal(this.getSeconds()).divide(
                    new BigDecimal(other.getSeconds()), 8, BigDecimal.ROUND_HALF_EVEN);
        }
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
        // TODO watch out where this method is used and how
        return new BigDecimal(seconds).setScale(scale);
    }

    public BigDecimal toEurosAsDecimal() {
        BigDecimal decimal = new BigDecimal(seconds).setScale(2);
        return decimal.divide(new BigDecimal(100));
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
        // there is no need to round because hours is the minimum granularity we
        // have now
        return seconds;
    }

    public static EffortDuration min(EffortDuration... durations) {
        return Collections.min(Arrays.asList(durations));
    }

    public static EffortDuration max(EffortDuration... durations) {
        return Collections.max(Arrays.asList(durations));
    }

    public static EffortDuration average(EffortDuration total, int items) {
        return EffortDuration.seconds(total.seconds / items);
    }

    public String toString() {
        return toFormattedString();
    }

    /**
     * LP AUDIOVISUAL HACK: string representations of EffortDuration now have
     * EUROS as their base granularity, with the decimal part (cents) becoming
     * HOURS.
     */
    public String toFormattedString() {
        // LP AUDIOVISUAL HACK: now string is EUROS.HOURS
        BigDecimal decimal = new BigDecimal(seconds).setScale(2);
        return decimal.divide(new BigDecimal(100)).toString();
    }

    public EffortDuration atNearestMinute() {
        // LP AUDIOVISUAL HACK: minutes and seconds granularity levels don't
        // exist now so we can't round lower than hours.
        return this;
    }

}
