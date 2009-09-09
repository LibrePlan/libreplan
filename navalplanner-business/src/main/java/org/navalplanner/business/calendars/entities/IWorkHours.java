package org.navalplanner.business.calendars.entities;

import org.joda.time.LocalDate;

public interface IWorkHours {

    public Integer getWorkableHours(LocalDate date);

}
