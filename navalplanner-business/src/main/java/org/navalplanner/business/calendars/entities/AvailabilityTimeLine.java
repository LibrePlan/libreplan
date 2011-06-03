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
package org.navalplanner.business.calendars.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class AvailabilityTimeLine {

    public static abstract class DatePoint implements Comparable<DatePoint> {

        protected abstract int compareTo(FixedPoint fixedPoint);

        protected abstract int compareTo(EndOfTime endOfTime);

        protected abstract int compareTo(StartOfTime startOfTime);

        protected abstract boolean equalTo(FixedPoint fixedPoint);

        protected abstract boolean equalTo(EndOfTime endOfTime);

        protected abstract boolean equalTo(StartOfTime startOfTime);

        @Override
        public final int compareTo(DatePoint obj) {
            Validate.notNull(obj);
            if (obj instanceof FixedPoint) {
                return compareTo((FixedPoint) obj);
            } else if (obj instanceof EndOfTime) {
                return compareTo((EndOfTime) obj);
            } else if (obj instanceof StartOfTime) {
                return compareTo((StartOfTime) obj);
            } else {
                throw new RuntimeException("unknown subclass for " + obj);
            }
        }

        @Override
        public abstract int hashCode();

        @Override
        public final boolean equals(Object obj) {
            if (!(obj instanceof DatePoint)) {
                return false;
            }
            if (obj instanceof FixedPoint) {
                return equalTo((FixedPoint) obj);
            } else if (obj instanceof EndOfTime) {
                return equalTo((EndOfTime) obj);
            } else if (obj instanceof StartOfTime) {
                return equalTo((StartOfTime) obj);
            } else {
                throw new RuntimeException("unknown subclass for " + obj);
            }
        }

        @Override
        public abstract String toString();

    }

    public static class FixedPoint extends DatePoint {
        private final LocalDate date;

        public FixedPoint(LocalDate date) {
            Validate.notNull(date);
            this.date = date;
        }

        public LocalDate getDate() {
            return date;
        }

        @Override
        protected int compareTo(FixedPoint fixedPoint) {
            return this.date.compareTo(fixedPoint.date);
        }

        @Override
        protected int compareTo(EndOfTime endOfTime) {
            return -1;
        }

        @Override
        protected int compareTo(StartOfTime startOfTime) {
            return 1;
        }

        @Override
        protected boolean equalTo(FixedPoint fixedPoint) {
            return date.equals(fixedPoint.date);
        }

        @Override
        protected boolean equalTo(EndOfTime endOfTime) {
            return false;
        }

        @Override
        protected boolean equalTo(StartOfTime startOfTime) {
            return false;
        }

        @Override
        public int hashCode() {
            return date.hashCode();
        }

        @Override
        public String toString() {
            return date.toString();
        }

        public static LocalDate tryExtract(DatePoint start) {
            FixedPoint point = (FixedPoint) start;
            return point.getDate();
        }
    }

    public static class EndOfTime extends DatePoint {
        private static final EndOfTime INSTANCE = new EndOfTime();

        public static EndOfTime create() {
            return INSTANCE;
        }

        @Override
        protected int compareTo(FixedPoint fixedPoint) {
            return 1;
        }

        @Override
        protected int compareTo(EndOfTime endOfTime) {
            return 0;
        }

        @Override
        protected int compareTo(StartOfTime startOfTime) {
            return 1;
        }

        @Override
        protected boolean equalTo(FixedPoint fixedPoint) {
            return false;
        }

        @Override
        protected boolean equalTo(EndOfTime endOfTime) {
            return true;
        }

        @Override
        protected boolean equalTo(StartOfTime startOfTime) {
            return false;
        }

        @Override
        public int hashCode() {
            return EndOfTime.class.hashCode();
        }

        @Override
        public String toString() {
            return EndOfTime.class.getSimpleName();
        }

    }

    public static class StartOfTime extends DatePoint {
        private static final StartOfTime INSTANCE = new StartOfTime();

        public static StartOfTime create() {
            return INSTANCE;
        }

        @Override
        protected int compareTo(FixedPoint fixedPoint) {
            return -1;
        }

        @Override
        protected int compareTo(EndOfTime endOfTime) {
            return -1;
        }

        @Override
        protected int compareTo(StartOfTime startOfTime) {
            return 0;
        }

        @Override
        protected boolean equalTo(FixedPoint fixedPoint) {
            return false;
        }

        @Override
        protected boolean equalTo(EndOfTime endOfTime) {
            return false;
        }

        @Override
        protected boolean equalTo(StartOfTime startOfTime) {
            return true;
        }

        @Override
        public int hashCode() {
            return StartOfTime.class.hashCode();
        }

        @Override
        public String toString() {
            return StartOfTime.class.getSimpleName();
        }
    }

    public static class Interval implements
            Comparable<Interval> {

        /**
         * Creates an interval. Null values can be provided.
         *
         * @param start
         *            if <code>null</code> is interpreted as start of time.
         * @param end
         *            if <code>null</code> is interpreted as end of time
         * @return an interval from start to end
         */
        public static Interval create(LocalDate start, LocalDate end) {
            DatePoint startPoint = start == null ? new StartOfTime()
                    : new FixedPoint(start);
            DatePoint endPoint = end == null ? new EndOfTime()
                    : new FixedPoint(end);
            return new Interval(startPoint, endPoint);
        }

        static Interval all() {
            return new Interval(StartOfTime.create(), EndOfTime.create());
        }

        static Interval from(LocalDate date) {
            return new Interval(new FixedPoint(date), EndOfTime.create());
        }

        public static Interval to(LocalDate date) {
            return new Interval(StartOfTime.create(), new FixedPoint(
                    date));
        }

        static Interval point(LocalDate start) {
            return new Interval(new FixedPoint(start), new FixedPoint(start
                    .plusDays(1)));
        }

        private final DatePoint start;

        private final DatePoint end;

        private Interval(DatePoint start, DatePoint end) {
            this.start = start;
            this.end = end;
        }

        public DatePoint getStart() {
            return start;
        }

        public DatePoint getEnd() {
            return end;
        }

        @Override
        public int compareTo(Interval other) {
            return this.start.compareTo(other.start) * 2
                    - this.end.compareTo(other.end);
        }

        public boolean includes(LocalDate date) {
            return includes(new FixedPoint(date));
        }

        private boolean includes(FixedPoint point) {
            return start.equals(point) || start.compareTo(point) <= 0
                    && point.compareTo(end) < 0;
        }

        public boolean overlaps(Interval other) {
            return start.compareTo(other.end) <= 0
                    && end.compareTo(other.start) >= 0;
        }

        public Interval intersect(Interval other) {
            Validate.isTrue(overlaps(other));
            return new Interval(max(start, other.start), min(end, other.end));
        }

        public Interval coalesce(Interval other) {
            if (!overlaps(other)) {
                throw new IllegalArgumentException(
                        "in order to coalesce two intervals must overlap");
            }
            return new Interval(min(start, other.start), max(end,
                    other.end));
        }

        private DatePoint min(DatePoint... values) {
            return (DatePoint) Collections.min(Arrays.asList(values));
        }

        private DatePoint max(DatePoint... values) {
            return (DatePoint) Collections.max(Arrays.asList(values));
        }

        @Override
        public String toString() {
            return String.format("[%s, %s]", start, end);
        }
    }

    public interface IVetoer {
        public boolean isValid(LocalDate date);
    }

    public static AvailabilityTimeLine allValid() {
        return new AvailabilityTimeLine();
    }

    public static AvailabilityTimeLine createAllInvalid() {
        AvailabilityTimeLine result = new AvailabilityTimeLine();
        result.allInvalid();
        return result;
    }

    private static IVetoer NO_VETOER = new IVetoer() {

        @Override
        public boolean isValid(LocalDate date) {
            return true;
        }
    };

    private IVetoer vetoer = NO_VETOER;

    private List<Interval> invalids = new ArrayList<Interval>();

    private AvailabilityTimeLine() {
    }

    public boolean isValid(LocalDate date) {
        return isValidBasedOnInvaidIntervals(date) && vetoer.isValid(date);
    }

    private boolean isValidBasedOnInvaidIntervals(LocalDate date) {
        if (invalids.isEmpty()) {
            return true;
        }
        Interval possibleInterval = findPossibleIntervalFor(date);
        return possibleInterval == null || !possibleInterval.includes(date);
    }

    private Interval findPossibleIntervalFor(LocalDate date) {
        Interval point = Interval.point(date);
        int binarySearch = Collections.binarySearch(invalids, point);
        if (binarySearch >= 0) {
            return invalids.get(binarySearch);
        } else {
            int insertionPoint = insertionPoint(binarySearch);
            if (insertionPoint == 0) {
                return null;
            }
            return invalids.get(insertionPoint - 1);
        }
    }

    public void allInvalid() {
        insert(Interval.all());
    }

    public void invalidAt(LocalDate date) {
        Interval point = Interval.point(date);
        insert(point);
    }

    /**
     * There are some invalid dates that cannot or are not suitable to be
     * represented as belonging to invalid intervals. For example if the invalid
     * dates are an infinite set.
     *
     * @param vetoer
     *            the vetoer to use
     */
    public void setVetoer(IVetoer vetoer) {
        Validate.notNull(vetoer);
        this.vetoer = vetoer;
    }

    private void insert(Interval toBeInserted) {
        if (invalids.isEmpty()) {
            invalids.add(toBeInserted);
            return;
        }
        toBeInserted = coalesceWithAdjacent(toBeInserted);
        int insertionPoint = insertBeforeAllAdjacent(toBeInserted);
        removeAdjacent(insertionPoint, toBeInserted);
    }

    /**
     * Returns the insertion position for the interval. Inserting the interval
     * at that position guarantees that interval start is posterior or equal to
     * any previous interval start. If the next interval start is equal to the
     * interval, the length of the former is less than the latter
     */
    private int findInsertionPosition(Interval interval) {
        int binarySearch = Collections.binarySearch(invalids, interval);
        return insertionPoint(binarySearch);
    }

    private int insertBeforeAllAdjacent(Interval toBeInserted) {
        int insertionPoint = findInsertionPosition(toBeInserted);
        invalids.add(insertionPoint, toBeInserted);
        return insertionPoint;
    }

    private Interval coalesceWithAdjacent(Interval toBeInserted) {
        Interval result = toBeInserted;
        List<Interval> adjacent = getAdjacent(toBeInserted);
        for (Interval each : adjacent) {
            result = result.coalesce(each);
        }
        return result;
    }

    private List<Interval> getAdjacent(Interval toBeInserted) {
        final int insertionPoint = findInsertionPosition(toBeInserted);
        List<Interval> result = new ArrayList<Interval>();
        assert insertionPoint <= invalids.size();
        for (int i = insertionPoint - 1; i >= 0 && at(i).overlaps(toBeInserted); i--) {
            result.add(at(i));
        }
        for (int i = insertionPoint; i < invalids.size()
                && at(i).overlaps(toBeInserted); i++) {
            result.add(at(i));
        }
        return result;
    }

    private List<Interval> intersectWithAdjacent(Interval interval) {
        List<Interval> result = new ArrayList<Interval>();
        List<Interval> adjacent = getAdjacent(interval);
        for (Interval each : adjacent) {
            assert interval.overlaps(each);
            result.add(interval.intersect(each));
        }
        return result;
    }

    private void removeAdjacent(int insertionPoint, Interval inserted) {
        ListIterator<Interval> listIterator = invalids
                .listIterator(insertionPoint + 1);
        while (listIterator.hasNext()) {
            Interval next = listIterator.next();
            if (!next.overlaps(inserted)) {
                break;
            }
            listIterator.remove();
        }
    }

    private Interval at(int i) {
        return i >= 0 && i < invalids.size() ? invalids.get(i) : null;
    }

    private int insertionPoint(int binarySearchResult) {
        return binarySearchResult < 0 ? (-binarySearchResult) - 1
                : binarySearchResult;
    }

    public void invalidAt(LocalDate intervalStart, LocalDate intervalEnd) {
        if (intervalStart.isAfter(intervalEnd)) {
            throw new IllegalArgumentException(
                    "end must be equal or after start");
        }
        insert(Interval.create(intervalStart, intervalEnd));
    }

    public void invalidFrom(LocalDate date) {
        insert(Interval.from(date));
    }

    public void invalidUntil(LocalDate date) {
        insert(Interval.to(date));
    }

    public AvailabilityTimeLine and(AvailabilityTimeLine another) {
        AvailabilityTimeLine result = AvailabilityTimeLine.allValid();
        inserting(result, invalids);
        inserting(result, another.invalids);
        result.setVetoer(and(this.vetoer, another.vetoer));
        return result;
    }

    private static IVetoer and(final IVetoer a,
            final IVetoer b) {
        return new IVetoer() {
            @Override
            public boolean isValid(LocalDate date) {
                return a.isValid(date) && b.isValid(date);
            }
        };
    }

    public AvailabilityTimeLine or(AvailabilityTimeLine another) {
        List<Interval> intersections = doIntersections(this, another);
        AvailabilityTimeLine result = AvailabilityTimeLine.allValid();
        for (Interval each : intersections) {
            boolean fromStartOfTime = each.getStart().equals(
                    StartOfTime.create());
            boolean untilEndOfTime = each.getEnd().equals(EndOfTime.create());
            if (fromStartOfTime && untilEndOfTime) {
                result.allInvalid();
            } else if (fromStartOfTime) {
                result.invalidUntil(FixedPoint.tryExtract(each.getEnd()));
            } else if (untilEndOfTime) {
                result.invalidFrom(FixedPoint.tryExtract(each.getStart()));
            } else {
                result.invalidAt(FixedPoint.tryExtract(each.getStart()),
                        FixedPoint.tryExtract(each.getEnd()));
            }
        }
        result.setVetoer(or(this.vetoer, another.vetoer));
        return result;
    }

    private static IVetoer or(final IVetoer a,
            final IVetoer b) {
        return new IVetoer() {
            @Override
            public boolean isValid(LocalDate date) {
                return a.isValid(date) || b.isValid(date);
            }
        };
    }

    private static List<Interval> doIntersections(AvailabilityTimeLine one,
            AvailabilityTimeLine another) {
        List<Interval> result = new ArrayList<Interval>();
        for (Interval each : one.invalids) {
            result.addAll(another.intersectWithAdjacent(each));
        }
        return result;
    }

    private void inserting(AvailabilityTimeLine result, List<Interval> invalid) {
        for (Interval each : invalid) {
            result.insert(each);
        }
    }

    public List<Interval> getValidPeriods() {
        List<Interval> result = new ArrayList<Interval>();
        DatePoint previous = StartOfTime.create();
        for (Interval each : invalids) {
            DatePoint invalidStart = each.start;
            if (!invalidStart.equals(StartOfTime.create())
                    && !invalidStart.equals(EndOfTime.create())) {
                result.add(new Interval(previous, invalidStart));
            }
            previous = each.getEnd();
        }
        if (!previous.equals(EndOfTime.create())) {
            result.add(new Interval(previous, EndOfTime.create()));
        }
        return result;
    }

}
