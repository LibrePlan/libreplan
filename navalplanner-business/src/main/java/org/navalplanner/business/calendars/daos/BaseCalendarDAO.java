/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.CalendarData;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.common.daos.IntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.templates.entities.OrderTemplate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for {@link BaseCalendar}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Diego Pino García <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class BaseCalendarDAO extends IntegrationEntityDAO<BaseCalendar>
        implements IBaseCalendarDAO {

    @Override
    public List<BaseCalendar> getBaseCalendars() {
        List<BaseCalendar> list = list(BaseCalendar.class);
        removeResourceCalendarInstances(list);
        return list;
    }

    private void removeResourceCalendarInstances(List<BaseCalendar> list) {
        for (Iterator<BaseCalendar> iterator = list.iterator(); iterator
                .hasNext();) {
            BaseCalendar baseCalendar = iterator.next();
            if (baseCalendar instanceof ResourceCalendar) {
                iterator.remove();
            }
        }
    }

    @Override
    public List<BaseCalendar> findByParent(BaseCalendar baseCalendar) {
        if (baseCalendar == null) {
            return new ArrayList<BaseCalendar>();
        }

        Criteria c = getSession().createCriteria(BaseCalendar.class)
                .createCriteria("calendarDataVersions", "v");
        c.add(Restrictions.eq("v.parent", baseCalendar));

        List<BaseCalendar> list = (List<BaseCalendar>) c.list();
        removeResourceCalendarInstances(list);
        return list;
    }

    @Override
    public List<BaseCalendar> findByName(BaseCalendar baseCalendar) {
        if (baseCalendar == null) {
            return new ArrayList<BaseCalendar>();
        }

        return findByName(baseCalendar.getName());
    }

    @Override
    public List<BaseCalendar> findByName(String name) {

        if (StringUtils.isBlank(name)) {
            return new ArrayList<BaseCalendar>();
        }

        Criteria c = getSession().createCriteria(BaseCalendar.class);
        c.add(Restrictions.eq("name", name).ignoreCase());

        List<BaseCalendar> list = (List<BaseCalendar>) c.list();
        removeResourceCalendarInstances(list);
        return list;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @Override
    public boolean thereIsOtherWithSameName(BaseCalendar baseCalendar) {
        List<BaseCalendar> withSameName = findByName(baseCalendar);
        if (withSameName.isEmpty()) {
            return false;
        }
        if (withSameName.size() > 1) {
            return true;
        }
        return areDifferentInDB(withSameName.get(0), baseCalendar);
    }

    private boolean areDifferentInDB(BaseCalendar one, BaseCalendar other) {
        if ((one.getId() == null) || (other.getId() == null)) {
            return true;
        }
        return !one.getId().equals(other.getId());
    }

    @Override
    public void checkIsReferencedByOtherEntities(BaseCalendar calendar) {
        checkHasResources(calendar);
        checkHasOrders(calendar);
        checkHasTasks(calendar);
        checkHasTemplates(calendar);
    }

    /**
     * A {@link BaseCalendar} is being used by a {@link Resource} if there is
     * some {@link CalendarData} which belongs to a {@link ResourceCalendar} and
     * has as a parent the parameter calendar
     *
     * @param calendar
     */
    private void checkHasResources(BaseCalendar calendar) {
        Query query = getSession().createQuery(
                "FROM CalendarData "
                        + "WHERE calendar IN (FROM ResourceCalendar) "
                        + "AND parent = :parent");
        query.setParameter("parent", calendar);
        if (!query.list().isEmpty()) {
            throw ValidationException
                    .invalidValue(
                            "Cannot delete calendar. It is being used at this moment by some resources.",
                            calendar);
        }
    }

    private void checkHasOrders(BaseCalendar calendar) {
        List orders = getSession().createCriteria(Order.class)
                .add(Restrictions.eq("calendar", calendar)).list();
        if (!orders.isEmpty()) {
            throw ValidationException
                    .invalidValue(
                            "Cannot delete calendar. It is being used at this moment by some orders.",
                            calendar);
        }
    }

    private void checkHasTasks(BaseCalendar calendar) {
        List tasks = getSession().createCriteria(TaskElement.class)
                .add(Restrictions.eq("calendar", calendar)).list();
        if (!tasks.isEmpty()) {
            throw ValidationException
                    .invalidValue(
                            "Cannot delete calendar. It is being used at this moment by some tasks.",
                            calendar);
        }
    }

    private void checkHasTemplates(BaseCalendar calendar) {
        List templates = getSession().createCriteria(OrderTemplate.class)
                .add(Restrictions.eq("calendar", calendar)).list();
        if (!templates.isEmpty()) {
            throw ValidationException
                    .invalidValue(
                            "Cannot delete calendar. It is being used at this moment by some templates.",
                            calendar);
        }
    }

}
