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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
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
    public SortedMap<LocalDate, Integer> getDayAssignmentsByTaksElementGroupByDay(
            TaskElement taskElement) {
        SortedMap<LocalDate, Integer> result = new TreeMap<LocalDate, Integer>();

        Set<ResourceAllocation<?>> resourceAllocations = taskElement
                .getResourceAllocations();

        Set<Long> genericResourceAllocations = new HashSet<Long>();
        Set<Long> specificResourceAllocations = new HashSet<Long>();

        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof GenericResourceAllocation) {
                Long id = resourceAllocation.getId();
                if (id != null) {
                    genericResourceAllocations.add(id);
                }
            } else if (resourceAllocation instanceof SpecificResourceAllocation) {
                Long id = resourceAllocation.getId();
                if (id != null) {
                    specificResourceAllocations.add(id);
                }
            }
        }

        List<String> queries = new ArrayList<String>();
        if (!genericResourceAllocations.isEmpty()) {
            String join = StringUtils.join(genericResourceAllocations, ",");
            queries.add("generic_resource_allocation_id IN (" + join + ")");
        }
        if (!specificResourceAllocations.isEmpty()) {
            String join = StringUtils.join(specificResourceAllocations, ",");
            queries.add("specific_resource_allocation_id IN (" + join + ")");
        }

        if (queries.isEmpty()) {
            return result;
        }

        String resourceAllocationsFilter = StringUtils.join(queries, " OR ");

        String strQuery = "SELECT day, SUM(hours) " + "FROM day_assignment "
                + "WHERE " + resourceAllocationsFilter + " "
                + "GROUP BY day ORDER BY day";

        Query query = getSession().createSQLQuery(strQuery);
        List<Object[]> list = query.list();

        for (Object[] object : list) {
            Date date = (Date) object[0];
            Number hours = (Number) object[1];
            result.put(new LocalDate(date), hours.intValue());
        }

        return result;
    }

}
