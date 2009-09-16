package org.navalplanner.business.test.planner.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.joda.time.LocalDate;
import org.junit.matchers.CombinableMatcher;
import org.junit.matchers.JUnitMatchers;
import org.navalplanner.business.planner.entities.DayAssigment;
import org.navalplanner.business.planner.entities.GenericDayAssigment;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificDayAssigment;

/**
 * Some {@link Matcher} that work against dayAssigments
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DayAssigmentMatchers {

    public static abstract class ListDayAssigmentsMatcher extends
            BaseMatcher<List<? extends DayAssigment>> {

        @Override
        final public boolean matches(Object value) {
            if (value instanceof List) {
                List<DayAssigment> dayAssigments = new ArrayList<DayAssigment>(
                        (List<DayAssigment>) value);
                return matches(dayAssigments);
            }
            return false;
        }

        protected abstract boolean matches(List<DayAssigment> assignments);

    }

    public static final class FromMatcher extends ListDayAssigmentsMatcher {
        private final LocalDate start;

        private FromMatcher(LocalDate start) {
            this.start = start;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("the first assignment must be at date"
                    + start);
        }

        public CombinableMatcher<List<? extends DayAssigment>> consecutiveDays(
                int days) {
            return JUnitMatchers.both(this).and(
                    DayAssigmentMatchers.consecutiveDays(days));
        }

        @Override
        protected boolean matches(List<DayAssigment> assignments) {
            return !assignments.isEmpty()
                    && assignments.get(0).getDay().equals(start);
        }
    }

    public static final Matcher<List<? extends DayAssigment>> haveHours(
            final int... hours) {
        return new BaseMatcher<List<? extends DayAssigment>>() {

            @Override
            public boolean matches(Object value) {
                if (value instanceof List) {
                    List<? extends DayAssigment> assigments = (List<? extends GenericDayAssigment>) value;
                    if (assigments.size() != hours.length)
                        return false;
                    for (int i = 0; i < hours.length; i++) {
                        if (hours[i] != assigments.get(i).getHours()) {
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

    public static ListDayAssigmentsMatcher consecutiveDays(final int days) {
        return new ListDayAssigmentsMatcher() {

            @Override
            public boolean matches(List<DayAssigment> assignments) {
                if (assignments.size() != days) {
                    return false;
                }
                if (days == 0) {
                    return true;
                }
                LocalDate current = assignments.get(0).getDay();
                for (DayAssigment d : assignments) {
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

    public static final FromMatcher from(final LocalDate start) {
        return new FromMatcher(start);
    }

    public static ListDayAssigmentsMatcher haveResourceAllocation(
            final ResourceAllocation allocation) {
        return new ListDayAssigmentsMatcher() {

            @Override
            protected boolean matches(List<DayAssigment> assignments) {
                for (DayAssigment dayAssigment : assignments) {
                    if (dayAssigment instanceof GenericDayAssigment) {
                        GenericDayAssigment generic = (GenericDayAssigment) dayAssigment;
                        if (!allocation.equals(generic
                                .getGenericResourceAllocation())) {
                            return false;
                        }
                    } else if (dayAssigment instanceof SpecificDayAssigment) {
                        SpecificDayAssigment specific = (SpecificDayAssigment) dayAssigment;
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
