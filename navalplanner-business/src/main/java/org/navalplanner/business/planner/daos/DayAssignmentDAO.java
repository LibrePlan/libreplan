/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.planner.daos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.GenericDayAssignment;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificDayAssignment;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.TaskElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for {@DayAssignment}
 *
 * @author Diego Pino García <dpino@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class DayAssignmentDAO extends GenericDAOHibernate<DayAssignment, Long>
        implements IDayAssignmentDAO {

    @Override
    @Transactional(readOnly = true)
    public SortedMap<LocalDate, Integer> getHoursAssignedByDayFor(
            TaskElement taskElement) {
        return calculateHoursAssignedByDayFor(taskElement
                .getResourceAllocations());
    }

    private SortedMap<LocalDate, Integer> calculateHoursAssignedByDayFor(
            Collection<ResourceAllocation<?>> resourceAllocations) {
        SortedMap<LocalDate, Integer> result = new TreeMap<LocalDate, Integer>();
        addResultsFromGeneric(result, resourceAllocations);
        addResultsFromSpecific(result, resourceAllocations);
        return result;
    }

    private void addResultsFromGeneric(
            SortedMap<LocalDate, Integer> result,
            Collection<ResourceAllocation<?>> resourceAllocations) {
        List<GenericResourceAllocation> genericResourceAllocations = withId(getOfType(
                GenericResourceAllocation.class, resourceAllocations));
        addToResult(result, queryHoursByDay(GenericDayAssignment.class,
                "genericResourceAllocation", genericResourceAllocations));
    }

    private void addResultsFromSpecific(SortedMap<LocalDate, Integer> result,
            Collection<ResourceAllocation<?>> resourceAllocations) {
        List<SpecificResourceAllocation> specificResourceAllocations = withId(getOfType(
                SpecificResourceAllocation.class, resourceAllocations));
        addToResult(result, queryHoursByDay(SpecificDayAssignment.class,
                "specificResourceAllocation", specificResourceAllocations));
    }

    private <T extends ResourceAllocation<?>> List<T> getOfType(Class<T> type,
            Collection<? extends ResourceAllocation<?>> resourceAllocations) {
        List<T> result = new ArrayList<T>();
        for (ResourceAllocation<?> allocation : resourceAllocations) {
            if (type.isInstance(allocation)) {
                result.add(type.cast(allocation));
            }
        }
        return result;
    }

    private <T extends BaseEntity> List<T> withId(List<T> elements) {
        List<T> result = new ArrayList<T>();
        for (T element : elements) {
            if (element.getId() != null) {
                result.add(element);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T extends DayAssignment, R extends ResourceAllocation<T>> List<Object[]> queryHoursByDay(
            Class<T> classBeingSearched, String allocationRelationship,
            List<R> allocations) {
        if (allocations.isEmpty()) {
            return Collections.emptyList();
        }
        Criteria criteria = getSession().createCriteria(classBeingSearched);
        criteria.add(Restrictions.in(allocationRelationship, allocations));
        criteria.setProjection(Projections.projectionList().add(
                Property.forName("day").group()).add(Projections.sum("hours")));
        List<Object[]> list = criteria.list();
        return list;
    }

    private void addToResult(SortedMap<LocalDate, Integer> result,
            List<Object[]> list) {
        for (Object[] object : list) {
            LocalDate date = (LocalDate) object[0];
            Integer hours = (Integer) object[1];
            int current = result.get(date) != null ? result.get(date) : 0;
            result.put(date, current + hours);
        }
    }

}
