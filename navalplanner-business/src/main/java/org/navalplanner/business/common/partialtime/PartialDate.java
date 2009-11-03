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

package org.navalplanner.business.common.partialtime;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.Partial;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.chrono.ISOChronology;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class PartialDate implements ReadablePartial {
    private static final DateTimeFieldType year = DateTimeFieldType.year();

    private static final DateTimeFieldType monthOfYear = DateTimeFieldType
            .monthOfYear();

    private static final DateTimeFieldType weekyear = DateTimeFieldType
            .weekyear();

    private static final DateTimeFieldType weekOfYear = DateTimeFieldType
            .weekOfWeekyear();

    private static final DateTimeFieldType dayOfMonth = DateTimeFieldType
            .dayOfMonth();

    private static final DateTimeFieldType hourOfDay = DateTimeFieldType
            .hourOfDay();

    private static final DateTimeFieldType minuteOfHour = DateTimeFieldType
            .minuteOfHour();

    private static final DateTimeFieldType secondOfMinute = DateTimeFieldType
            .secondOfMinute();

    private static final DateTimeFieldType millisOfSecond = DateTimeFieldType
            .millisOfSecond();

    public enum Granularity {

        YEAR(year), MONTH(year, monthOfYear), WEEK(weekyear, weekOfYear) {
            TimeQuantity asQuantity(TimeQuantity acc, List<Integer> values) {
                return acc.plus(values.get(0), Granularity.YEAR).plus(
                        values.get(1), Granularity.WEEK);
            }
        },
        DAY(year, monthOfYear, dayOfMonth), HOUR(year, monthOfYear, dayOfMonth,
                hourOfDay), MINUTE(year, monthOfYear, dayOfMonth, hourOfDay,
                minuteOfHour), SECOND(year, monthOfYear, dayOfMonth, hourOfDay,
                minuteOfHour, secondOfMinute), MILLISECONDS(year, monthOfYear,
                dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute,
                millisOfSecond);

        private DateTimeFieldType[] types;

        private Granularity(DateTimeFieldType... types) {
            this.types = types;
        }

        public DateTimeFieldType[] getDateTimeTypes() {
            return types.clone();
        }

        public TimeQuantity asQuantity(int[] values) {
            return asQuantity(TimeQuantity.empty(), asIntegersList(values));
        }

        TimeQuantity asQuantity(TimeQuantity acc, List<Integer> values) {
            Integer current = values.remove(values.size() - 1);
            TimeQuantity result = acc.plus(current, this);
            if (hasPrevious()) {
                result = getPrevious().asQuantity(result, values);
            }
            return result;
        }

        private boolean hasPrevious() {
            return ordinal() > 0;
        }

        private Granularity getPrevious() {
            Granularity result = Granularity.values()[ordinal() - 1];
            // we bypass week, as it isn't part of the rest of hierarchy
            if (result == WEEK) {
                return WEEK.getPrevious();
            }
            return result;
        }

        private List<Integer> asIntegersList(int[] values) {
            List<Integer> result = new ArrayList<Integer>();
            for (int value : values) {
                result.add(value);
            }
            return result;
        }

        boolean isMoreCoarseGrainedThan(Granularity other) {
            return this.ordinal() < other.ordinal();
        }

        public Granularity[] thisAndMoreFineGrained() {
            Granularity[] result = new Granularity[values().length
                    - this.ordinal()];
            for (int i = 0; i < result.length; i++) {
                result[i] = values()[i + this.ordinal()];
            }
            return result;
        }

        public Granularity[] moreCoarseGrained() {
            Granularity[] result = new Granularity[this.ordinal()];
            for (int i = 0; i < result.length; i++) {
                result[i] = values()[i];
            }
            return result;
        }

        private static Granularity forType(DateTimeFieldType fieldType) {
            for (Granularity granularity : Granularity.values()) {
                if (granularity.specifies(fieldType)) {
                    return granularity;
                }
            }
            throw new RuntimeException("not found granularity for " + fieldType);
        }

        private boolean specifies(DateTimeFieldType fieldType) {
            return getMostSpecific().equals(fieldType);
        }

        private DateTimeFieldType getMostSpecific() {
            return getDateTimeTypes()[getDateTimeTypes().length - 1];
        }
    }

    public static PartialDate createFrom(LocalDate date) {
        long millis = date.toDateTimeAtStartOfDay().getMillis();
        return createFrom(millis).with(Granularity.DAY);
    }

    public static PartialDate createFrom(DateTime dateTime) {
        return createFrom(dateTime.getMillis());
    }

    public static PartialDate createFrom(Date date) {
        return createFrom(date.getTime());
    }

    public static PartialDate createFrom(ReadableInstant instant) {
        return createFrom(instant.getMillis());
    }

    public static PartialDate createFrom(long timeMilliseconds) {
        return new PartialDate(new Instant(timeMilliseconds),
                Granularity.MILLISECONDS);
    }

    private static List<Integer> getValues(Partial partial,
            Granularity granularity) {
        List<Integer> values = new ArrayList<Integer>();
        for (DateTimeFieldType fieldType : granularity.getDateTimeTypes()) {
            values.add(partial.get(fieldType));
        }
        return values;
    }

    private static Instant toNormalizedInstant(Granularity granularity,
            List<Integer> values) {
        return new Partial(granularity.getDateTimeTypes(), asIntArray(values),
                ISOChronology.getInstance(DateTimeZone.getDefault()))
                .toDateTime(new DateTime(0, DateTimeZone.getDefault()))
                .toInstant();
    }

    private static int[] asIntArray(List<Integer> values) {
        int[] result = new int[values.size()];
        int i = 0;
        for (Integer integer : values) {
            result[i++] = integer;
        }
        return result;
    }

    private static Partial asPartial(ReadableInstant instant, Granularity unit) {
        DateTime dateTime = interpretInDefaultTimeZone(instant);
        DateTimeFieldType[] dateTimeTypes = unit.getDateTimeTypes();
        int[] values = new int[dateTimeTypes.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = dateTime.get(dateTimeTypes[i]);
        }
        return new Partial(dateTimeTypes, values);
    }

    private final Granularity granularity;

    private final Partial partial;

    private final Instant normalizedInstant;

    private List<Integer> values;

    private PartialDate(Instant instant, Granularity granularityUnit) {
        this.partial = asPartial(instant, granularityUnit);
        this.granularity = granularityUnit;
        this.values = Collections.unmodifiableList(getValues(partial,
                granularityUnit));
        this.normalizedInstant = toNormalizedInstant(granularityUnit, values);
        assert this.partial.equals(asPartial(this.normalizedInstant,
                granularityUnit));
    }

    private static DateTime interpretInDefaultTimeZone(ReadableInstant instant) {
        return new DateTime(instant.getMillis(), DateTimeZone.getDefault());
    }

    public PartialDate with(Granularity granularity) {
        if (!canBeConvertedTo(granularity)) {
            throw new IllegalArgumentException("the granularity " + granularity
                    + " is more fine-grained than the current one("
                    + this.granularity + "). Conversion is impossible");
        }
        return new PartialDate(normalizedInstant, granularity);
    }

    public Granularity getGranularity() {
        return this.granularity;
    }

    public boolean canBeConvertedToLocalDate() {
        return !this.granularity.isMoreCoarseGrainedThan(Granularity.DAY);
    }

    public boolean canBeConvertedTo(Granularity unit) {
        Validate.notNull(unit);
        return !this.granularity.isMoreCoarseGrainedThan(unit);
    }

    public boolean before(PartialDate other) throws IllegalArgumentException {
        if (!getGranularity().equals(other.getGranularity())) {
            throw new IllegalArgumentException(
                    "It's required that the two PartialDates have the same granularity");
        }
        assert this.values.size() == other.values.size();
        Iterator<Integer> iterator = this.values.iterator();
        Iterator<Integer> otherIterator = other.values.iterator();
        while (iterator.hasNext()) {
            int diff = iterator.next() - otherIterator.next();
            if (diff > 0) {
                return false;
            }
            if (diff < 0) {
                return true;
            }
        }
        return false;
    }

    public boolean after(PartialDate other) throws IllegalArgumentException {
        return !before(other) && !equals(other);
    }

    public LocalDate tryToConvertToLocalDate() throws IllegalArgumentException {
        if (!this.canBeConvertedToLocalDate()) {
            throw new IllegalArgumentException("the partialDate " + this
                    + " can't support be converted to local date");
        }
        return new LocalDate(this.normalizedInstant, ISOChronology
                .getInstance(DateTimeZone.getDefault()));
    }

    public IntervalOfPartialDates to(PartialDate to) {
        return new IntervalOfPartialDates(this, to);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getGranularity()).append(values)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PartialDate) {
            PartialDate other = (PartialDate) obj;
            if (!getGranularity().equals(other.getGranularity())) {
                return false;
            }
            return this.values.equals(other.values);
        }
        return false;
    }

    @Override
    public int get(DateTimeFieldType field) {
        return partial.get(field);
    }

    @Override
    public Chronology getChronology() {
        return partial.getChronology();
    }

    @Override
    public DateTimeField getField(int index) {
        return partial.getField(index);
    }

    @Override
    public DateTimeFieldType getFieldType(int index) {
        return granularity.types[index];
    }

    @Override
    public int getValue(int index) {
        return partial.getValue(index);
    }

    @Override
    public boolean isSupported(DateTimeFieldType field) {
        return partial.isSupported(field);
    }

    @Override
    public int size() {
        return partial.size();
    }

    @Override
    public DateTime toDateTime(ReadableInstant baseInstant) {
        return partial.toDateTime(baseInstant);
    }

    public IntervalOfPartialDates to(TimeQuantity timeQuantity) {
        return to(this.plus(timeQuantity));
    }

    public PartialDate plus(TimeQuantity timeQuantity) {
        if (granularity.isMoreCoarseGrainedThan(timeQuantity
                .getGreatestGranularitySpecified())) {
            throw new IllegalArgumentException("");
        }
        DateTimeFieldType[] dateTimeTypes = granularity.getDateTimeTypes();
        long result = normalizedInstant.getMillis();
        for (int i = 0; i < dateTimeTypes.length; i++) {
            DateTimeField field = dateTimeTypes[i].getField(getChronology());
            result = field.add(result, timeQuantity.valueFor(Granularity
                    .forType(dateTimeTypes[i])));
        }
        return new PartialDate(new Instant(result), granularity);
    }

    public TimeQuantity quantityFrom(PartialDate start) {
        Validate.isTrue(this.granularity.equals(start.getGranularity()),
                "must have the same granularity");
        Validate.isTrue(this.after(start));
        int[] substractedValues = substract(this.values, start.values);
        return this.granularity.asQuantity(substractedValues);
    }

    private static int[] substract(List<Integer> bigger, List<Integer> other) {
        assert bigger.size() == other.size();
        int[] result = new int[bigger.size()];
        Iterator<Integer> iterator = bigger.iterator();
        Iterator<Integer> otherIterator = other.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            result[i] = iterator.next() - otherIterator.next();
            i++;
        }
        return result;
    }

    public Integer valueFor(Granularity granularity) {
        if (this.granularity.isMoreCoarseGrainedThan(granularity)) {
            throw new IllegalArgumentException(granularity
                    + " is more specific than this instance granularity: "
                    + this.granularity);
        }
        return get(granularity.getMostSpecific());
    }

    public Serializable[] getDataForPersistence() {
        return new Serializable[] {
                new Timestamp(normalizedInstant.getMillis()),
                granularity.name() };
    }

    public static PartialDate createFromDataForPersistence(Object... values) {
        Timestamp instant = (Timestamp) values[0];
        Granularity granularity = Granularity.valueOf((String) values[1]);
        return PartialDate.createFrom(instant).with(granularity);
    }
}
