/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.business.calendars.daos;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.calendars.entities.CalendarException;
import org.navalplanner.business.calendars.entities.CalendarExceptionType;
import org.navalplanner.business.common.daos.IntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for {@link CalendarExceptionType}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class CalendarExceptionTypeDAO extends
        IntegrationEntityDAO<CalendarExceptionType> implements
        ICalendarExceptionTypeDAO {

    @Override
    public boolean existsByName(CalendarExceptionType type) {
        Criteria c = getSession().createCriteria(CalendarExceptionType.class);
        c.add(Restrictions.eq("name", type.getName()));

        List list = c.list();
        return (list.size() == 1);
    }

    @Override
    public List<CalendarExceptionType> getAll() {
        return list(CalendarExceptionType.class);
    }

    @Override
    public boolean hasCalendarExceptions(CalendarExceptionType type) {
        return !getCalendarExceptions(type).isEmpty();
    }

    private List<CalendarException> getCalendarExceptions(CalendarExceptionType type) {
        Criteria c = getSession().createCriteria(CalendarException.class);
        c.add(Restrictions.eq("type.id", type.getId()));
        return c.list();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsByNameAnotherTransaction(String name) {
        return existsByName(name);
    }

    @Override
    public boolean existsByName(String name) {
        try {
            findByName(name);
            return true;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CalendarExceptionType findByName(String name) throws InstanceNotFoundException {
        if (StringUtils.isBlank(name)) {
            throw new InstanceNotFoundException(null, CalendarExceptionType.class.getName());
        }

        CalendarExceptionType calendarExceptionType = (CalendarExceptionType) getSession().createCriteria(
                CalendarExceptionType.class).add(
                Restrictions.eq("name", name.trim()).ignoreCase())
                .uniqueResult();

        if (calendarExceptionType == null) {
            throw new InstanceNotFoundException(name, CalendarExceptionType.class.getName());
        } else {
            return calendarExceptionType;
        }

    }

}
