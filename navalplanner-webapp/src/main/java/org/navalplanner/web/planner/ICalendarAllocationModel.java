package org.navalplanner.web.planner;

import java.util.List;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.planner.entities.Task;

/**
 * Contract for {@link CalendarAllocationModel}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface ICalendarAllocationModel {

    List<BaseCalendar> getBaseCalendars();

    void setTask(Task task);

    void confirmAssignCalendar(BaseCalendar calendar);

    void cancel();

    BaseCalendar getAssignedCalendar();

}
