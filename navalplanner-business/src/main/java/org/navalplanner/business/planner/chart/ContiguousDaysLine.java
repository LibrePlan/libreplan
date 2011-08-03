/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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
package org.navalplanner.business.planner.chart;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.chart.ContiguousDaysLine.ONDay;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.workingday.EffortDuration;

/**
 * It represents some contiguous days from a start date to a not included end
 * date. Each of these {@link LocalDate} has an associated value that can be
 * <code>null</code>.
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class ContiguousDaysLine<T> implements Iterable<ONDay<T>> {

    public static class ONDay<T> {

        private final LocalDate day;

        private final T value;

        private ONDay(LocalDate day, T value) {
            Validate.notNull(day);
            this.day = day;
            this.value = value;
        }

        public LocalDate getDay() {
            return day;
        }

        public T getValue() {
            return value;
        }
    }

    public static <T> ContiguousDaysLine<T> create(LocalDate fromInclusive,
            LocalDate endExclusive, Class<T> klass) {
        return create(fromInclusive, endExclusive);
    }

    public static <T> ContiguousDaysLine<T> create(LocalDate fromInclusive,
            LocalDate endExclusive) {
        if (fromInclusive.isAfter(endExclusive)) {
            throw new IllegalArgumentException("fromInclusive ("
                    + fromInclusive + ") is after endExclusive ("
                    + endExclusive + ")");
        }
        Days daysBetween = Days.daysBetween(fromInclusive, endExclusive);
        return new ContiguousDaysLine<T>(fromInclusive, daysBetween.getDays());
    }

    public static ContiguousDaysLine<List<DayAssignment>> byDay(
            Collection<? extends DayAssignment> assignments) {
        if (assignments.isEmpty()) {
            return invalid();
        }
        DayAssignment min = Collections.min(assignments,
                DayAssignment.byDayComparator());
        DayAssignment max = Collections.max(assignments,
                DayAssignment.byDayComparator());
        ContiguousDaysLine<List<DayAssignment>> result = create(min.getDay(),
                max.getDay().plusDays(1));
        result.transformInSitu(new IValueTransformer<List<DayAssignment>, List<DayAssignment>>() {

            @Override
            public List<DayAssignment> transform(LocalDate day,
                    List<DayAssignment> previousValue) {
                return new LinkedList<DayAssignment>();
            }
        });
        for (DayAssignment each : assignments) {
            result.get(each.getDay()).add(each);
        }
        return result;
    }

    public static <T> ContiguousDaysLine<T> invalid() {
        return new ContiguousDaysLine<T>(null, 0);
    }

    @SuppressWarnings("unchecked")
    public static ContiguousDaysLine<EffortDuration> min(
            ContiguousDaysLine<EffortDuration> a,
            ContiguousDaysLine<EffortDuration> b) {
        return join(EffortDuration.class, minTransformer(), a, b);
    }

    public static IValueTransformer<EffortDuration[], EffortDuration> minTransformer() {
        return new IValueTransformer<EffortDuration[], EffortDuration>() {

            @Override
            public EffortDuration transform(LocalDate day,
                    EffortDuration[] previousValue) {
                return EffortDuration.min(previousValue);
            }
        };
    }

    /**
     * Joins both {@link ContiguousDaysLine} substracting from minuend line. An
     * effortDuration can't be negative so, if subtrahend line is at some point
     * bigger than minuend, zero is returned at that point.
     *
     * @param minuend
     * @param subtrahend
     * @return
     */
    @SuppressWarnings("unchecked")
    public static ContiguousDaysLine<EffortDuration> substract(
            ContiguousDaysLine<EffortDuration> minuend,
            ContiguousDaysLine<EffortDuration> subtrahend) {
        return join(EffortDuration.class, substractTransformer(), minuend,
                subtrahend);
    }

    private static IValueTransformer<EffortDuration[], EffortDuration> substractTransformer() {
        return new IValueTransformer<EffortDuration[], EffortDuration>() {

            @Override
            public EffortDuration transform(LocalDate day,
                    EffortDuration[] previousValue) {
                EffortDuration result = previousValue[0];
                for (int i = 1; i < previousValue.length; i++) {
                    result = result.minus(EffortDuration.min(previousValue[i],
                            result));
                }
                return result;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static ContiguousDaysLine<EffortDuration> sum(
            ContiguousDaysLine<EffortDuration> summandA,
            ContiguousDaysLine<EffortDuration> summandB) {
        return join(EffortDuration.class, additionTransformer(), summandA,
                summandB);
    }

    public static SortedMap<LocalDate, EffortDuration> toSortedMap(
            ContiguousDaysLine<EffortDuration> line) {
        SortedMap<LocalDate, EffortDuration> result = new TreeMap<LocalDate, EffortDuration>();
        for (ONDay<EffortDuration> each : line) {
            result.put(each.getDay(), each.getValue());
        }
        return result;
    }

    private static IValueTransformer<EffortDuration[], EffortDuration> additionTransformer() {
        return new IValueTransformer<EffortDuration[], EffortDuration>() {

            @Override
            public EffortDuration transform(LocalDate day,
                    EffortDuration[] previousValue) {
                return EffortDuration.sum(previousValue);
            }
        };
    }

    public static <T, R> ContiguousDaysLine<R> join(final Class<T> klass,
            final IValueTransformer<T[], R> transformer,
            final ContiguousDaysLine<T>... lines) {
        if (lines[0].isNotValid()) {
            return invalid();
        }
        LocalDate start = lines[0].getStart();
        LocalDate endExclusive = lines[0].getEndExclusive();
        for (ContiguousDaysLine<T> each : lines) {
            Validate.isTrue(each.getStart().equals(start),
                    "the start of all lines must be same date");
            Validate.isTrue(each.getEndExclusive().equals(endExclusive),
                    "the start of all lines must be same date");
        }
        ContiguousDaysLine<R> result = ContiguousDaysLine.create(start,
                endExclusive);
        result.transformInSitu(new IValueTransformer<R, R>() {

            @Override
            public R transform(LocalDate day, R previousValue) {
                return transformer.transform(day, getValues(day, lines));
            }

            private T[] getValues(LocalDate day, ContiguousDaysLine<T>... lines) {
                @SuppressWarnings("unchecked")
                T[] result = (T[]) Array.newInstance(klass, lines.length);
                for (int i = 0; i < result.length; i++) {
                    result[i] = lines[i].get(day);
                }
                return result;
            }
        });
        return result;
    }

    private final LocalDate startInclusive;

    private final List<T> values;

    private ContiguousDaysLine(LocalDate start, int size) {
        this.startInclusive = start;
        this.values = new ArrayList<T>(size);
        for (int i = 0; i < size; i++) {
            values.add(null);
        }
    }

    public boolean isNotValid() {
        return startInclusive == null;
    }

    public LocalDate getStart() {
        mustBeValid();
        return startInclusive;
    }

    private void mustBeValid() {
        if (isNotValid()) {
            throw new IllegalStateException("this line is invalid");
        }
    }

    public LocalDate getEndExclusive() {
        return getStart().plusDays(values.size());
    }

    public T get(LocalDate day) throws IndexOutOfBoundsException {
        Validate.notNull(day);
        Days days = Days.daysBetween(startInclusive, day);
        return values.get(days.getDays());
    }

    public void set(LocalDate day, T value) throws IndexOutOfBoundsException {
        Validate.notNull(day);
        Days days = Days.daysBetween(startInclusive, day);
        values.set(days.getDays(), value);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public void setValueForAll(T value) {
        ListIterator<T> listIterator = values.listIterator();
        while (listIterator.hasNext()) {
            listIterator.next();
            listIterator.set(value);
        }
    }

    public interface IValueTransformer<T, R> {

        R transform(LocalDate day, T previousValue);
    }

    public void transformInSitu(IValueTransformer<T, T> transformer) {
        LocalDate current = startInclusive;
        ListIterator<T> listIterator = values.listIterator();
        while (listIterator.hasNext()) {
            T previousValue = listIterator.next();
            listIterator.set(transformer.transform(current, previousValue));
            current = current.plusDays(1);
        }
    }

    public <R> ContiguousDaysLine<R> transform(
            IValueTransformer<T, R> doubleTransformer) {
        if (isNotValid()) {
            return invalid();
        }
        ContiguousDaysLine<R> result = ContiguousDaysLine.create(
                startInclusive, getEndExclusive());
        for (ONDay<T> onDay : this) {
            LocalDate day = onDay.getDay();
            result.set(day, doubleTransformer.transform(day, onDay.getValue()));
        }
        return result;
    }

    public ContiguousDaysLine<T> copy() {
        return transform(ContiguousDaysLine.<T> identity());
    }

    private static <T> IValueTransformer<T, T> identity() {
        return new IValueTransformer<T, T>() {

            @Override
            public T transform(LocalDate day, T previousValue) {
                return previousValue;
            }
        };
    }

    public static <T, S, R> IValueTransformer<T, R> compound(
            final IValueTransformer<T, S> first,
            final IValueTransformer<S, R> last) {

        return new IValueTransformer<T, R>() {

            @Override
            public R transform(LocalDate day, T previousValue) {
                S intermediateValue = first.transform(day, previousValue);
                return last.transform(day, intermediateValue);
            }
        };
    }

    @Override
    public Iterator<ONDay<T>> iterator() {
        final Iterator<T> iterator = values.iterator();
        return new Iterator<ONDay<T>>() {

            private LocalDate current = startInclusive;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public ONDay<T> next() {
                T next = iterator.next();
                ONDay<T> result = new ONDay<T>(current, next);
                current = current.plusDays(1);
                return result;
            }

            @Override
            public void remove() throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }
        };
    }

    public int size() {
        return values.size();
    }

}
