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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.Validate;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.chart.ContiguousDaysLine.ONDay;

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

    private final LocalDate startInclusive;

    private final List<T> values;

    private ContiguousDaysLine(LocalDate start, int size) {
        Validate.notNull(start);
        this.startInclusive = start;
        this.values = new ArrayList<T>(size);
        for (int i = 0; i < size; i++) {
            values.add(null);
        }
    }

    public LocalDate getStart() {
        return startInclusive;
    }

    public LocalDate getEndExclusive() {
        return startInclusive.plusDays(values.size());
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
        ContiguousDaysLine<R> result = ContiguousDaysLine.create(
                startInclusive, getEndExclusive());
        for (ONDay<T> onDay : this) {
            LocalDate day = onDay.getDay();
            result.set(day, doubleTransformer.transform(day, onDay.getValue()));
        }
        return result;
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
