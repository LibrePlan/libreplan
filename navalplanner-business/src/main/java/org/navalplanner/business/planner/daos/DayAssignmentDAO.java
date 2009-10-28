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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;
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
        SortedMap<LocalDate, Integer> result = new TreeMap<LocalDate, Integer>();

        Set<ResourceAllocation<?>> resourceAllocations = taskElement
                .getResourceAllocations();

        Set<GenericResourceAllocation> genericResourceAllocations = new HashSet<GenericResourceAllocation>();
        Set<SpecificResourceAllocation> specificResourceAllocations = new HashSet<SpecificResourceAllocation>();

        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof GenericResourceAllocation) {
                Long id = resourceAllocation.getId();
                if (id != null) {
                    genericResourceAllocations
                            .add((GenericResourceAllocation) resourceAllocation);
                }
            } else if (resourceAllocation instanceof SpecificResourceAllocation) {
                Long id = resourceAllocation.getId();
                if (id != null) {
                    specificResourceAllocations
                            .add((SpecificResourceAllocation) resourceAllocation);
                }
            }
        }

        if (!genericResourceAllocations.isEmpty()) {
            Criteria criteria = getSession().createCriteria(
                    GenericDayAssignment.class);
            criteria.add(Restrictions.in("genericResourceAllocation",
                    genericResourceAllocations));

            criteria.setProjection(Projections.projectionList().add(
                    Property.forName("day").group()).add(
                    Projections.sum("hours")));

            List<Object[]> list = criteria.list();

            for (Object[] object : list) {
                LocalDate date = (LocalDate) object[0];
                Integer hours = (Integer) object[1];
                result.put(date, hours.intValue());
            }
        }

        if (!specificResourceAllocations.isEmpty()) {
            Criteria criteria = getSession().createCriteria(
                    SpecificDayAssignment.class);
            criteria.add(Restrictions.in("specificResourceAllocation",
                    specificResourceAllocations));

            criteria.setProjection(Projections.projectionList().add(
                    Property.forName("day").group()).add(
                    Projections.sum("hours")));

            List<Object[]> list = criteria.list();

            for (Object[] object : list) {
                LocalDate date = (LocalDate) object[0];
                Integer hours = (Integer) object[1];

                if (result.get(date) == null) {
                    result.put(date, hours.intValue());
                } else {
                    result.put(date, result.get(date) + hours.intValue());
                }
            }
        }

        return result;
    }

}
