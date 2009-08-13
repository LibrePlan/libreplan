package org.navalplanner.business.test.calendars.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.calendars.entities.ExceptionDay;

/**
 * Tests for {@link ExceptionDay}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class ExceptionDayTest {

    @Test
    public void testCreateExceptionDayWithDate() {
        Date date = new Date();

        ExceptionDay day = ExceptionDay.create(date, 8);

        assertThat(day.getDate(), equalTo(new LocalDate(date)));
        assertThat(day.getHours(), equalTo(8));
    }

}
