package org.navalplanner.business.calendars.daos;

import java.util.List;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@link BaseCalendar}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class BaseCalendarDAO extends GenericDAOHibernate<BaseCalendar, Long>
        implements IBaseCalendarDAO {

    @Override
    public List<BaseCalendar> getBaseCalendars() {
        return list(BaseCalendar.class);
    }

}
