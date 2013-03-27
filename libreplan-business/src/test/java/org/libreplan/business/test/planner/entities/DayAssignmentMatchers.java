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

package org.libreplan.business.test.planner.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.joda.time.LocalDate;
import org.junit.matchers.CombinableMatcher;
import org.junit.matchers.JUnitMatchers;
import org.libreplan.business.planner.entities.DayAssignment;
import org.libreplan.business.planner.entities.GenericDayAssignment;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.SpecificDayAssignment;
import org.libreplan.business.workingday.IntraDayDate;

/**
 * Some {@link Matcher} that work against dayAssignments
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DayAssignmentMatchers {

    public static abstract class ListDayAssignmentsMatcher extends
            BaseMatcher<List<? extends DayAssignment>> {

        @Override
        final public boolean matches(Object value) {
            if (value instanceof List) {
                List<DayAssignment> dayAssignments = new ArrayList<DayAssignment>(
                        (List<DayAssignment>) value);
                return matches(dayAssignments);
            }
            return false;
        }

        protected abstract boolean matches(List<DayAssignment> assignments);

    }

    public static final class FromMatcher extends ListDayAssignmentsMatcher {
        private final LocalDate start;

        private FromMatcher(LocalDate start) {
            this.start = start;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("the first assignment must be at date"
                    + start);
        }

        public CombinableMatcher<List<? extends DayAssignment>> consecutiveDays(
                int days) {
            return JUnitMatchers.both(this).and(
                    DayAssignmentMatchers.consecutiveDays(days));
        }

        @Override
        protected boolean matches(List<DayAssignment> assignments) {
            return !assignments.isEmpty()
                    && assignments.get(0).getDay().equals(start);
        }
    }

    public static final Matcher<List<? extends DayAssignment>> haveHours(
            final int... hours) {
        return new BaseMatcher<List<? extends DayAssignment>>() {

            @Override
            public boolean matches(Object value) {
                if (value instanceof List) {
                    List<? extends DayAssignment> assignments = (List<? extends DayAssignment>) value;
                    if (assignments.size() != hours.length) {
                        return false;
                    }
                    for (int i = 0; i < hours.length; i++) {
                        if (hours[i] != assignments.get(i).getHours()) {
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("must have hours: "
                        + Arrays.toString(hours));
            }
        };
    }

    public static ListDayAssignmentsMatcher consecutiveDays(final int days) {
        return new ListDayAssignmentsMatcher() {

            @Override
            public boolean matches(List<DayAssignment> assignments) {
                if (assignments.size() != days) {
                    return false;
                }
                if (days == 0) {
                    return true;
                }
                LocalDate current = assignments.get(0).getDay();
                for (DayAssignment d : assignments) {
                    if (!d.getDay().equals(current)) {
                        return false;
                    }
                    current = current.plusDays(1);
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("it must have " + days
                        + " days consecutive ");
            }
        };
    }

    public static final FromMatcher from(final IntraDayDate start) {
        return new FromMatcher(start.getDate());
    }

    public static final FromMatcher from(final LocalDate start) {
        return new FromMatcher(start);
    }

    public static ListDayAssignmentsMatcher haveResourceAllocation(
            final ResourceAllocation<?> allocation) {
        return new ListDayAssignmentsMatcher() {

            @Override
            protected boolean matches(List<DayAssignment> assignments) {
                for (DayAssignment dayAssignment : assignments) {
                    if (dayAssignment instanceof GenericDayAssignment) {
                        GenericDayAssignment generic = (GenericDayAssignment) dayAssignment;
                        if (!allocation.equals(generic
                                .getGenericResourceAllocation())) {
                            return false;
                        }
                    } else if (dayAssignment instanceof SpecificDayAssignment) {
                        SpecificDayAssignment specific = (SpecificDayAssignment) dayAssignment;
                        if (!allocation.equals(specific
                                .getSpecificResourceAllocation())) {
                            return false;
                        }
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("all must belong to allocation "
                        + allocation);
            }
        };
    }

}
