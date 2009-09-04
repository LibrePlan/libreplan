package org.navalplanner.web.planner;

import org.navalplanner.business.planner.entities.TaskElement;
import org.zkoss.ganttz.extensions.ICommandOnTask;

/**
 * Contract for {@link CalendarAllocationCommand}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface ICalendarAllocationCommand extends ICommandOnTask<TaskElement> {

    void setCalendarAllocationController(
            CalendarAllocationController calendarAllocationController);

}
