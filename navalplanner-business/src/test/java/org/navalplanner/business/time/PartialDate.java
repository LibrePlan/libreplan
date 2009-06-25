package org.navalplanner.business.time;

import java.util.Date;

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

public class PartialDate implements ReadablePartial {
    private static final DateTimeFieldType year = DateTimeFieldType.year();

    private static final DateTimeFieldType monthOfYear = DateTimeFieldType
            .monthOfYear();

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

        YEAR(year), MONTH(year, monthOfYear), WEEK(year, weekOfYear), DAY(year,
                monthOfYear, dayOfMonth), HOUR(year, monthOfYear, dayOfMonth,
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
            Granularity[] result = new Granularity[this.ordinal() - 1];
            for (int i = 0; i < result.length; i++) {
                result[i] = values()[i];
            }
            return result;
        }
    }

    public static LocalDate tryToConvertToLocalDate(PartialDate partialDate) {
        if (!partialDate.canBeConvertedToLocalDate())
            throw new IllegalArgumentException("the partialDate " + partialDate
                    + " doesn't support be converted to local date");
        return new LocalDate(partialDate.instant);
    }

    public static PartialDate from(LocalDate date) {
        long millis = date.toDateMidnight().getMillis();
        return from(millis).with(Granularity.DAY);
    }

    public static PartialDate from(DateTime dateTime) {
        return from(dateTime.getMillis());
    }

    public static PartialDate from(Date date) {
        return from(date.getTime());
    }

    public static PartialDate from(ReadableInstant instant) {
        return from(instant.getMillis());
    }

    public static PartialDate from(long timeMilliseconds) {
        return new PartialDate(new Instant(timeMilliseconds),
                Granularity.MILLISECONDS);
    }

    private final Granularity granularity;

    private final Partial partial;

    private final Instant instant;

    private PartialDate(Instant instant, Granularity unit) {
        this.partial = asPartial(instant, unit);
        this.granularity = unit;
        this.instant = instant;
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

    private static DateTime interpretInDefaultTimeZone(ReadableInstant instant) {
        return new DateTime(instant.getMillis(), DateTimeZone.getDefault());
    }

    public PartialDate with(Granularity granularity) {
        if (!canBeConvertedTo(granularity)) {
            throw new IllegalArgumentException("the granularity " + granularity
                    + " is more fine-grained than the current one("
                    + this.granularity + "). Conversion is impossible");
        }
        return new PartialDate(instant, granularity);
    }

    public Granularity getGranularity() {
        return this.granularity;
    }

    public boolean canBeConvertedToLocalDate() {
        return !this.granularity.isMoreCoarseGrainedThan(Granularity.DAY);
    }

    public boolean canBeConvertedTo(Granularity unit) {
        return !this.granularity.isMoreCoarseGrainedThan(unit);
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

}
