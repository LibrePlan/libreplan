package org.navalplanner.web.calendars;

import java.util.ArrayList;
import java.util.List;

import org.navalplanner.business.calendars.entities.BaseCalendar;

/**
 * Class that represents a root node for the {@link BaseCalendar} tree.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class BaseCalendarTreeRoot {

    private List<BaseCalendar> rootCalendars = new ArrayList<BaseCalendar>();
    private List<BaseCalendar> derivedCalendars = new ArrayList<BaseCalendar>();

    /**
     * Creates a {@link BaseCalendarTreeRoot} using the list of {@link BaseCalendar}
     * passed as argument.
     *
     * @param baseCalendars
     *            All the {@link BaseCalendar} that will be shown in the tree.
     */
    public BaseCalendarTreeRoot(List<BaseCalendar> baseCalendars) {
        for (BaseCalendar baseCalendar : baseCalendars) {
            if (baseCalendar.isDerived()) {
                getDerivedCalendars().add(baseCalendar);
            } else {
                getRootCalendars().add(baseCalendar);
            }
        }
    }

    /**
     * Returns the {@link BaseCalendar} that has no parent.
     */
    public List<BaseCalendar> getRootCalendars() {
        return rootCalendars;
    }

    /**
     * Returns all the {@link BaseCalendar} that has a parent.
     */
    public List<BaseCalendar> getDerivedCalendars() {
        return derivedCalendars;
    }

}
