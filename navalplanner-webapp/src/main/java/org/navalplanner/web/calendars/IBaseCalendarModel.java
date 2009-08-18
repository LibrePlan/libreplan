package org.navalplanner.web.calendars;

import java.util.List;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.exceptions.ValidationException;

/**
 * Contract for {@link BaseCalendarModel}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IBaseCalendarModel {

    List<BaseCalendar> getBaseCalendars();

    BaseCalendar getBaseCalendar();

    void save() throws ValidationException;

    void remove(BaseCalendar BaseCalendar);

    void prepareForCreate();

    void initEdit(BaseCalendar BaseCalendar);

    void prepareForRemove(BaseCalendar BaseCalendar);

    boolean isEditing();

}
