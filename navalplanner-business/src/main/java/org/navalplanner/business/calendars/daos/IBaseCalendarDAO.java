package org.navalplanner.business.calendars.daos;

import java.util.List;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.daos.IGenericDAO;

/**
 * Contract for {@link BaseCalendarDAO}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IBaseCalendarDAO extends IGenericDAO<BaseCalendar, Long> {

    List<BaseCalendar> getBaseCalendars();

    List<BaseCalendar> findByParent(BaseCalendar baseCalendar);

    List<BaseCalendar> findByName(BaseCalendar baseCalendar);

    boolean thereIsOtherWithSameName(BaseCalendar baseCalendar);

}
