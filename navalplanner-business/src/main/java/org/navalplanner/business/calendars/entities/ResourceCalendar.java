package org.navalplanner.business.calendars.entities;

import org.navalplanner.business.resources.entities.Resource;

/**
 * Calendar for a {@link Resource}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class ResourceCalendar extends BaseCalendar {

    public static ResourceCalendar create() {
        ResourceCalendar resourceCalendar = new ResourceCalendar(CalendarData
                .create());
        resourceCalendar.setNewObject(true);
        return resourceCalendar;
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public ResourceCalendar() {
    }

    private ResourceCalendar(CalendarData calendarData) {
        super(calendarData);
    }

}
