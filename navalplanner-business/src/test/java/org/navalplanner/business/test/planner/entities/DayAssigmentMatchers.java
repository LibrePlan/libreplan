package org.navalplanner.business.test.planner.entities;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.joda.time.LocalDate;
import org.junit.matchers.JUnitMatchers;
import org.navalplanner.business.planner.entities.DayAssigment;
import org.navalplanner.business.planner.entities.GenericDayAssigment;

/**
 * Some {@link Matcher} that work against dayAssigments
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DayAssigmentMatchers {

    public static final class FromMatcher extends
            BaseMatcher<List<? extends DayAssigment>> {
        private final LocalDate start;

        private FromMatcher(LocalDate start) {
            this.start = start;
        }

        @Override
        public boolean matches(Object value) {
            if (value instanceof List) {
                List<? extends DayAssigment> assigments = (List<? extends GenericDayAssigment>) value;
                return !assigments.isEmpty()
                        && assigments.get(0).getDay().equals(start);
            }
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("the first assignment must be at date"
                    + start);
        }

        public Matcher<List<? extends DayAssigment>> consecutiveDays(int days) {
            return JUnitMatchers.both(this).and(
                    DayAssigmentMatchers.consecutiveDays(days));
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

    public static Matcher<? extends List<? extends DayAssigment>> consecutiveDays(
            final int days) {
        return new BaseMatcher<List<? extends DayAssigment>>() {

            @Override
            public boolean matches(Object value) {
                if (value instanceof List) {
                    List<? extends DayAssigment> assignments = (List) value;
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
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("it must have " + days
                        + " days consecutive ");
            }
        };
    }

    public static final FromMatcher from(
            final LocalDate start) {
        return new FromMatcher(start);
    }

}
