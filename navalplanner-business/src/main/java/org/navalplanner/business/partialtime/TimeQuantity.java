package org.navalplanner.business.partialtime;

import java.util.EnumMap;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.partialtime.PartialDate.Granularity;

/**
 * Represents a quantity of time. It's composed from granularities and integers <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TimeQuantity {

    private static EnumMap<Granularity, Integer> createSumOf(
            EnumMap<Granularity, Integer>... values) {
        EnumMap<Granularity, Integer> result = new EnumMap<Granularity, Integer>(
                Granularity.class);
        for (Granularity granularity : Granularity.values()) {
            Integer acc = sumAll(granularity, values);
            if (acc != null) {
                result.put(granularity, acc);
            }
        }
        return result;
    }

    private static Integer sumAll(Granularity granularity,
            EnumMap<Granularity, Integer>... maps) {
        Integer result = null;
        for (EnumMap<Granularity, Integer> enumMap : maps) {
            if (enumMap.containsKey(granularity)) {
                Integer valueToAdd = enumMap.get(granularity);
                result = result == null ? enumMap.get(granularity) : result
                        + valueToAdd;
            }
        }
        return result;
    }

    private static EnumMap<Granularity, Integer> copyAndAdd(
            EnumMap<Granularity, Integer> existent, int quantity,
            Granularity granularity) {
        EnumMap<Granularity, Integer> result = new EnumMap<Granularity, Integer>(
                existent);
        int newQuantity = quantity;
        if (result.containsKey(granularity)) {
            newQuantity += result.get(granularity);
        }
        result.put(granularity, newQuantity);
        return result;
    }

    private final EnumMap<Granularity, Integer> values;

    public static TimeQuantity empty() {
        return new TimeQuantity();
    }

    public TimeQuantity() {
        this(new EnumMap<Granularity, Integer>(Granularity.class));
    }

    private TimeQuantity(EnumMap<Granularity, Integer> enumMap) {
        Validate.notNull(enumMap);
        this.values = enumMap;
    }

    public Integer valueFor(Granularity granularity) {
        return values.containsKey(granularity) ? values.get(granularity) : 0;
    }

    public TimeQuantity plus(int quantity, Granularity granularity) {
        Validate.notNull(granularity, "granularity must be not null");
        return new TimeQuantity(copyAndAdd(values, quantity, granularity));
    }

    public TimeQuantity plus(TimeQuantity other) {
        return new TimeQuantity(createSumOf(values, other.values));
    }

    public Granularity getGreatestGranularitySpecified() {
        Granularity result = Granularity.YEAR;
        for (Granularity granularity : values.keySet()) {
            if (result == null || result.isMoreCoarseGrainedThan(granularity)) {
                result = granularity;
            }
        }
        return result;
    }
}
