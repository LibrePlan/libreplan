package org.navalplanner.business.test.planner.chart;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.planner.chart.ContiguousDaysLine;
import org.navalplanner.business.planner.chart.ContiguousDaysLine.IValueTransformer;
import org.navalplanner.business.planner.chart.ContiguousDaysLine.ONDay;

public class ContiguousDaysLineTest {

    private static final LocalDate someDate = new LocalDate(2002, 2, 10);

    @Test(expected = IllegalArgumentException.class)
    public void aLineCannotBeCreatedWithAStartBeforeTheEnd() {
        ContiguousDaysLine.create(someDate, someDate.minusDays(1));
    }

    @Test
    public void aLineWithTheSameStartAndEndIsEmpty() {
        ContiguousDaysLine<?> line = ContiguousDaysLine.create(
                someDate, someDate);
        assertTrue(line.isEmpty());
        assertThat(line.size(), equalTo(0));
    }

    @Test
    public void theSizeIsEqualToTheDaysBetweenTheDates() {
        for (int i = 0; i < 10; i++) {
            LocalDate end = someDate.plusDays(i);
            ContiguousDaysLine<?> line = ContiguousDaysLine.create(
                    someDate, end);
            assertThat(line.getEndExclusive(), equalTo(end));
            assertThat(line.size(), equalTo(Days.daysBetween(someDate, end)
                    .getDays()));
        }
    }

    @Test
    public void initiallyTheValuesAreNull() {
        for (ONDay<String> onDay : ContiguousDaysLine.create(someDate,
                someDate.plusDays(2), String.class)) {
            assertThat(onDay.getValue(), nullValue());
        }
    }

    @Test
    public void theInitialValueCanBeChanged() {
        ContiguousDaysLine<String> line = ContiguousDaysLine.create(
                someDate, someDate.plusDays(2), String.class);
        line.setValueForAll("foo");
        assertThat(line, allValuesEqualTo("foo"));
    }

    private <T> Matcher<ContiguousDaysLine<T>> allValuesEqualTo(final T value) {
        return new BaseMatcher<ContiguousDaysLine<T>>() {

            @Override
            public boolean matches(Object object) {
                if (object instanceof ContiguousDaysLine) {
                    ContiguousDaysLine<?> line = (ContiguousDaysLine) object;
                    for (ONDay<?> each : line) {
                        if(! ObjectUtils.equals(value, each.getValue())){
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("all the values of the line has as value: "
                                + value);

            }
        };
    }

    @Test
    public void linesCanBeIndexedByLocalDates() {
        ContiguousDaysLine<String> line = ContiguousDaysLine.create(
                someDate, someDate.plusDays(2), String.class);
        assertThat(line.get(someDate), nullValue());
        assertThat(line.get(someDate.plusDays(1)), nullValue());
        line.setValueForAll("foo");
        assertThat(line.get(someDate), equalTo("foo"));
        assertThat(line.get(someDate.plusDays(1)), equalTo("foo"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void aLocalDateIndexBeyondTheLineCausesException() {
        ContiguousDaysLine<String> line = ContiguousDaysLine.create(
                someDate, someDate.plusDays(2), String.class);
        line.get(someDate.plusDays(2));
    }

    @Test
    public void aLineCanBeTransformedInSitu() {
        ContiguousDaysLine<String> line = ContiguousDaysLine.create(
                someDate, someDate.plusDays(2), String.class);
        line.setValueForAll("foo");
        line.transformInSitu(doubleTransformer());
        assertThat(line, allValuesEqualTo("foofoo"));
    }

    @Test
    public void aLineCanBeTransformedIntoAnotherOne() {
        ContiguousDaysLine<String> line = ContiguousDaysLine.create(
                someDate, someDate.plusDays(2), String.class);
        line.setValueForAll("foo");
        ContiguousDaysLine<String> another = line
                .transform(doubleTransformer());
        assertThat("the original line remains the same", line,
                allValuesEqualTo("foo"));
        assertThat(another, allValuesEqualTo("foofoo"));
    }

    @Test
    public void aSubIntervalOfAnInvalidLineIsInvalid() {
        ContiguousDaysLine<Object> invalid = ContiguousDaysLine.invalid();
        assertTrue(invalid.subInterval(someDate, someDate.plusDays(2))
                .isNotValid());
    }

    @Test
    public void ifTheRangeIsOutsideInvalidIsReturned() {
        ContiguousDaysLine<String> line = ContiguousDaysLine.create(someDate,
                someDate.plusDays(2), String.class);
        line.setValueForAll("foo");
        assertTrue(line.subInterval(someDate.minusDays(2),
                someDate.minusDays(1)).isNotValid());
    }

    @Test
    public void ifTheRangeIsCompletelyInsideThatPartIsReturned() {
        ContiguousDaysLine<String> line = ContiguousDaysLine.create(someDate,
                someDate.plusDays(4), String.class);
        line.setValueForAll("foo");
        ContiguousDaysLine<String> newLine = line.subInterval(
                someDate.plusDays(1), someDate.plusDays(3));
        assertThat(newLine.getStart(), equalTo(someDate.plusDays(1)));
        assertThat(newLine.getEndExclusive(), equalTo(someDate.plusDays(3)));
    }

    @Test
    public void ifTheRangeIsPartOutsideAndPartInsideTheIntersectionIsReturned() {
        ContiguousDaysLine<String> line = ContiguousDaysLine.create(someDate,
                someDate.plusDays(4), String.class);
        line.setValueForAll("foo");
        ContiguousDaysLine<String> newLine = line.subInterval(
                someDate.minusDays(1), someDate.plusDays(7));
        assertThat(newLine.getStart(), equalTo(someDate));
        assertThat(newLine, hasSameValuesAs(line));
        assertThat(newLine.getEndExclusive(), equalTo(someDate.plusDays(4)));
    }


    private Matcher<ContiguousDaysLine<?>> hasSameValuesAs(
            final ContiguousDaysLine<?> line) {
        return new BaseMatcher<ContiguousDaysLine<?>>() {

            @Override
            public boolean matches(Object object) {
                if (object instanceof ContiguousDaysLine) {
                    ContiguousDaysLine<?> another = (ContiguousDaysLine<?>) object;
                    for (ONDay<?> each : line) {
                        if (!ObjectUtils.equals(each.getValue(),
                                another.get(each.getDay()))) {
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                List<Object> values = new ArrayList<Object>();
                for (ONDay<?> each : line) {
                    values.add(each.getValue());
                }
                description.appendText("the line has values: " + values);
            }
        };
    }

    private IValueTransformer<String, String> doubleTransformer() {
        return new IValueTransformer<String, String>() {

            @Override
            public String transform(LocalDate day, String previousValue) {
                return previousValue + previousValue;
            }
        };
    }

}
