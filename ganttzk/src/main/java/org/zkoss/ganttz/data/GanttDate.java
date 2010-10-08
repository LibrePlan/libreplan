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
package org.zkoss.ganttz.data;

import java.util.Date;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.zkoss.ganttz.IDatesMapper;

/**
 * @author Óscar González Fernández
 *
 */
public abstract class GanttDate implements Comparable<GanttDate> {

    public static LocalDateBased createFrom(Date date) {
        if (date == null) {
            return null;
        }
        return createFrom(LocalDate.fromDateFields(date));
    }

    public static LocalDateBased createFrom(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return new LocalDateBased(localDate);
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof GanttDate) {
            GanttDate other = (GanttDate) obj;
            return isEqualsTo(other);
        }
        return false;
    }

    protected abstract boolean isEqualsTo(GanttDate other);

    @Override
    public abstract int hashCode();

    public interface ICases<R> {
        public R on(LocalDateBased localDateBased);

        public R on(CustomDate customType);
    }

    public static abstract class Cases<T extends CustomDate, R> implements
            ICases<R> {

        private final Class<T> klass;

        public Cases(Class<T> klass) {
            Validate.notNull(klass);
            this.klass = klass;
        }

        @Override
        public R on(CustomDate customType) {
            return onCustom(klass.cast(customType));
        }

        protected abstract R onCustom(T customType);
    }

    public abstract <R> R byCases(ICases<R> cases);

    /**
     * Converts this {@link GanttDate} to a date that is the start of the day
     * represented by this {@link GanttDate}
     */
    public abstract Date toDayRoundedDate();

    public abstract int toPixels(IDatesMapper datesMapper);

    public static class LocalDateBased extends GanttDate {

        private final LocalDate localDate;

        public LocalDateBased(LocalDate localDate) {
            Validate.notNull(localDate);
            this.localDate = localDate;
        }

        public LocalDate getLocalDate() {
            return localDate;
        }

        public <R> R byCases(ICases<R> cases) {
            return cases.on(this);
        }

        @Override
        public int compareTo(GanttDate o) {
            return o.byCases(new ICases<Integer>() {

                @Override
                public Integer on(LocalDateBased localDateBased) {
                    return localDate.compareTo(localDateBased.localDate);
                }

                @Override
                public Integer on(CustomDate customType) {
                    return -customType.compareToLocalDate(localDate);
                }
            });
        }

        @Override
        public Date toDayRoundedDate() {
            return localDate.toDateTimeAtStartOfDay().toDate();
        }

        @Override
        public boolean isEqualsTo(GanttDate other) {
            return other.byCases(new ICases<Boolean>() {

                @Override
                public Boolean on(LocalDateBased localDateBased) {
                    return localDate.equals(localDateBased.localDate);
                }

                @Override
                public Boolean on(CustomDate customType) {
                    return false;
                }
            });
        }

        @Override
        public int hashCode() {
            return localDate.hashCode();
        }

        @Override
        public int toPixels(IDatesMapper datesMapper) {
            return datesMapper.toPixels(localDate);
        }

    }

    public static abstract class CustomDate extends GanttDate {

        public CustomDate() {
        }

        protected abstract boolean isEqualsToCustom(CustomDate customType);

        @Override
        protected boolean isEqualsTo(GanttDate other) {
            return other.byCases(new ICases<Boolean>() {

                @Override
                public Boolean on(LocalDateBased localDateBased) {
                    return false;
                }

                @Override
                public Boolean on(CustomDate customType) {
                    return isEqualsToCustom(customType);
                }
            });
        }

        @Override
        public <R> R byCases(ICases<R> cases) {
            return cases.on(this);
        }

        @Override
        public int compareTo(GanttDate o) {
            return o.byCases(new ICases<Integer>() {
                @Override
                public Integer on(LocalDateBased localDateBased) {
                    return compareToLocalDate(localDateBased.localDate);
                }

                @Override
                public Integer on(CustomDate customType) {
                    return compareToCustom(customType);
                }
            });
        }

        protected abstract int compareToCustom(CustomDate customType);

        protected abstract int compareToLocalDate(LocalDate localDate);
    }

    public boolean after(GanttDate other) {
        return compareTo(other) > 0;
    }

}
