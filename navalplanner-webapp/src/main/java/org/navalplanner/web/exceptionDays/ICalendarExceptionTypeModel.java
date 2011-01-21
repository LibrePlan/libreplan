package org.navalplanner.web.exceptionDays;

import java.util.List;

import org.navalplanner.business.calendars.entities.CalendarExceptionType;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.web.common.IIntegrationEntityModel;
import org.zkoss.util.InvalidValueException;

/**
 *
 * @author Diego Pino <dpino@igalia.com>
 *
 */
public interface ICalendarExceptionTypeModel extends IIntegrationEntityModel {

    void initCreate();

    void initEdit(CalendarExceptionType exceptionType);

    List<CalendarExceptionType> getExceptionDayTypes();

    CalendarExceptionType getExceptionDayType();

    void confirmSave();

    void confirmDelete(CalendarExceptionType exceptionType)
            throws InstanceNotFoundException, InvalidValueException;

}
