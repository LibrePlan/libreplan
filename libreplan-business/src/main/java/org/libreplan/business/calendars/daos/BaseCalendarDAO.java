/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

package org.libreplan.business.calendars.daos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.calendars.entities.CalendarData;
import org.libreplan.business.calendars.entities.ResourceCalendar;
import org.libreplan.business.common.daos.IntegrationEntityDAO;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.templates.entities.OrderTemplate;
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
public class BaseCalendarDAO extends IntegrationEntityDAO<BaseCalendar> implements IBaseCalendarDAO {

    @Override
    public List<BaseCalendar> getBaseCalendars() {
        List<BaseCalendar> list = list(BaseCalendar.class);
        removeResourceCalendarInstances(list);
        Collections.sort(list);
        return list;
    }

    private void removeResourceCalendarInstances(List<BaseCalendar> list) {
        for (Iterator<BaseCalendar> iterator = list.iterator(); iterator.hasNext();) {
            BaseCalendar baseCalendar = iterator.next();
            if ( baseCalendar instanceof ResourceCalendar ) {
                iterator.remove();
            }
        }
    }

    @Override
    public List<BaseCalendar> findByParent(BaseCalendar baseCalendar) {
        if (baseCalendar == null) {
            return new ArrayList<BaseCalendar>();
        }

        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<BaseCalendar> cq = cb.createQuery(BaseCalendar.class);
        Root<BaseCalendar> root = cq.from(BaseCalendar.class);
        Join<BaseCalendar, CalendarData> v = root.join("calendarDataVersions");
        cq.where(cb.equal(v.get("parent"), baseCalendar));

        List<BaseCalendar> list = getSession().createQuery(cq).getResultList();
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

        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<BaseCalendar> cq = cb.createQuery(BaseCalendar.class);
        Root<BaseCalendar> root = cq.from(BaseCalendar.class);
        cq.where(cb.equal(cb.lower(root.get("name")), name.toLowerCase()));

        List<BaseCalendar> list = getSession().createQuery(cq).getResultList();
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
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<ResourceCalendar> cq = cb.createQuery(ResourceCalendar.class);
        Root<ResourceCalendar> root = cq.from(ResourceCalendar.class);
        Join<ResourceCalendar, CalendarData> calendarData = root.join("calendarDataVersions");
        cq.where(cb.equal(calendarData.get("parent"), calendar));
        List<ResourceCalendar> calendars = getSession().createQuery(cq).getResultList();
        if (!calendars.isEmpty()) {
            throw ValidationException
                    .invalidValueException(
                            "Cannot delete calendar. It is being used at this moment by some resources.",
                            calendar);
        }
    }

    private void checkHasOrders(BaseCalendar calendar) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> root = cq.from(Order.class);
        cq.where(cb.equal(root.get("calendar"), calendar));
        List<Order> orders = getSession().createQuery(cq).getResultList();
        if (!orders.isEmpty()) {
            throw ValidationException
                    .invalidValueException(
                            "Cannot delete calendar. It is being used at this moment by some orders.",
                            calendar);
        }
    }

    private void checkHasTasks(BaseCalendar calendar) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<TaskElement> cq = cb.createQuery(TaskElement.class);
        Root<TaskElement> root = cq.from(TaskElement.class);
        cq.where(cb.equal(root.get("calendar"), calendar));
        List<TaskElement> tasks = getSession().createQuery(cq).getResultList();
        if (!tasks.isEmpty()) {
            throw ValidationException
                    .invalidValueException(
                            "Cannot delete calendar. It is being used at this moment by some tasks.",
                            calendar);
        }
    }

    private void checkHasTemplates(BaseCalendar calendar) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<OrderTemplate> cq = cb.createQuery(OrderTemplate.class);
        Root<OrderTemplate> root = cq.from(OrderTemplate.class);
        cq.where(cb.equal(root.get("calendar"), calendar));
        List<OrderTemplate> templates = getSession().createQuery(cq).getResultList();
        if (!templates.isEmpty()) {
            throw ValidationException
                    .invalidValueException(
                            "Cannot delete calendar. It is being used at this moment by some templates.",
                            calendar);
        }
    }

}
