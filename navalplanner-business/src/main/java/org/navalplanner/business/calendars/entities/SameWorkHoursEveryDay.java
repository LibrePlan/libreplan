package org.navalplanner.business.calendars.entities;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;

public class SameWorkHoursEveryDay implements IWorkHours {

    private static final SameWorkHoursEveryDay DEFAULT_WORKING_DAY = new SameWorkHoursEveryDay(
            8);

    public static SameWorkHoursEveryDay getDefaultWorkingDay() {
        return DEFAULT_WORKING_DAY;
    }
    private final Integer hours;

    public SameWorkHoursEveryDay(Integer hours) {
        Validate.notNull(hours);
        Validate.isTrue(hours >= 0);
        this.hours = hours;
    }

    @Override
    public Integer getWorkableHours(LocalDate date) {
        return hours;
    }

}
