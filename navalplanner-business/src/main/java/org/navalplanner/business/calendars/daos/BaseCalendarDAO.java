package org.navalplanner.business.calendars.daos;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
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

    @Override
    public List<BaseCalendar> findByParent(BaseCalendar baseCalendar) {
        if (baseCalendar == null) {
            return new ArrayList<BaseCalendar>();
        }

        Criteria c = getSession().createCriteria(BaseCalendar.class)
                .createCriteria("calendarDataVersions", "v");
        c.add(Restrictions.eq("v.parent", baseCalendar));

        return (List<BaseCalendar>) c.list();
    }

    @Override
    public List<BaseCalendar> findByName(BaseCalendar baseCalendar) {
        if (baseCalendar == null) {
            return new ArrayList<BaseCalendar>();
        }

        Criteria c = getSession().createCriteria(BaseCalendar.class);
        c.add(Restrictions.eq("name", baseCalendar.getName()));

        return (List<BaseCalendar>) c.list();
    }

}
