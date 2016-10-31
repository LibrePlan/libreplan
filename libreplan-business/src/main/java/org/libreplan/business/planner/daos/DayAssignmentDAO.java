/*
 * This file is part of LibrePlan
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

package org.libreplan.business.planner.daos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.planner.entities.DayAssignment;
import org.libreplan.business.planner.entities.DerivedDayAssignment;
import org.libreplan.business.planner.entities.GenericDayAssignment;
import org.libreplan.business.planner.entities.SpecificDayAssignment;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.scenarios.entities.Scenario;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@link DayAssignment}.
 *
 * @author Diego Pino García <dpino@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class DayAssignmentDAO extends GenericDAOHibernate<DayAssignment, Long> implements IDayAssignmentDAO {

    private final String SCENARIO = "scenario";

    @Override
    public void removeDerived(Collection<? extends DerivedDayAssignment> assignments) {
        for (DerivedDayAssignment each : assignments) {
            getSession().delete(each);
        }
    }

    @Override
    public List<DayAssignment> getAllFor(Scenario scenario) {
        List<DayAssignment> result = new ArrayList<>();
        result.addAll(getSpecific(scenario, null, null, null));
        result.addAll(getGeneric(scenario, null, null, null));
        result.addAll(getDerived(scenario, null, null, null));

        return result;
    }

    @Override
    public List<DayAssignment> getAllFor(Scenario scenario, LocalDate init, LocalDate end) {
        List<DayAssignment> result = new ArrayList<>();
        result.addAll(getSpecific(scenario, init, end, null));
        result.addAll(getGeneric(scenario, init, end, null));
        result.addAll(getDerived(scenario, init, end, null));

        return result;
    }

    @Override
    public List<DayAssignment> getAllFor(
            Scenario scenario, LocalDate startDateInclusive, LocalDate endDateInclusive, Resource resource) {

        List<DayAssignment> result = new ArrayList<>();
        result.addAll(getSpecific(scenario, startDateInclusive, endDateInclusive, resource));
        result.addAll(getGeneric(scenario, startDateInclusive, endDateInclusive, resource));
        result.addAll(getDerived(scenario, startDateInclusive, endDateInclusive, resource));

        return result;
    }

    private List<DerivedDayAssignment> getDerived(
            Scenario scenario, LocalDate initInclusive, LocalDate endInclusive, Resource resource) {

        String queryString = "select d from DerivedDayAssignmentsContainer c " +
                "JOIN c.dayAssignments d where c.scenario = :scenario" +
                addQueryConditionForInitAndEndDate(initInclusive, endInclusive) + addQueryConditionsForResource(resource);

        Query query = getSession().createQuery(queryString);
        query = query.setParameter(SCENARIO, scenario);
        addInitAndEndParameters(query, initInclusive, endInclusive);
        addResourceParameter(query, resource);

        return query.list();
    }

    private String addQueryConditionForInitAndEndDate(LocalDate initInclusive, LocalDate endInclusive) {
        String initCondition = initInclusive != null ? " and d.day >= :init" : "";
        String endCondition = endInclusive != null ? " and d.day <= :end" : "";
        return initCondition + endCondition;
    }

    private String addQueryConditionsForResource(Resource resource) {
        return resource != null ? " and d.resource = :resource " : "";
    }

    private Query addInitAndEndParameters(Query query, LocalDate initInclusive, LocalDate endInclusive) {
        if (initInclusive != null) {
            query.setParameter("init", initInclusive);
        }

        if (endInclusive != null) {
            query.setParameter("end", endInclusive);
        }
        return query;
    }

    private Query addResourceParameter(Query query, Resource resource) {
        return resource != null ? query.setParameter("resource", resource) : query;
    }

    private List<GenericDayAssignment> getGeneric(
            Scenario scenario, LocalDate initInclusive, LocalDate endInclusive, Resource resource) {

        String queryString = "select d from GenericDayAssignmentsContainer c " +
                "JOIN c.dayAssignments d where c.scenario = :scenario" +
                addQueryConditionForInitAndEndDate(initInclusive, endInclusive) + addQueryConditionsForResource(resource);

        Query query = getSession().createQuery(queryString).setParameter(SCENARIO, scenario);
        addInitAndEndParameters(query, initInclusive, endInclusive);
        addResourceParameter(query, resource);

        return query.list();
    }

    private List<SpecificDayAssignment> getSpecific(
            Scenario scenario, LocalDate initInclusive, LocalDate endInclusive, Resource resource) {

        String queryString = "select d from SpecificDayAssignmentsContainer c " +
                "JOIN c.dayAssignments d where c.scenario = :scenario" +
                addQueryConditionForInitAndEndDate(initInclusive, endInclusive) + addQueryConditionsForResource(resource);

        Query query = getSession().createQuery(queryString).setParameter(SCENARIO, scenario);
        addInitAndEndParameters(query, initInclusive, endInclusive);
        addResourceParameter(query, resource);

        return query.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DayAssignment> listFilteredByDate(LocalDate init, LocalDate end) {
        Criteria criteria  = getSession().createCriteria(DayAssignment.class);
        addDateRestrictionsToDayAssignmentQuery(criteria, init, end);
        return criteria.list();
    }

    private void addDateRestrictionsToDayAssignmentQuery(Criteria criteria, LocalDate init, LocalDate end) {
        if (init != null) {
            criteria.add(Restrictions.ge("day", init));
        }
        if (end != null) {
            criteria.add(Restrictions.le("day", end));
        }
    }

    @Override
    public List<DayAssignment> findByResources(Scenario scenario, List<Resource> resources) {
        // TODO incorporate scenario filtering to the query instead of doing it in memory
        return DayAssignment.withScenario(scenario, findByResources(resources));
    }

    @Override
    public List<DayAssignment> findByResources(List<Resource> resources) {
        return resources.isEmpty()
                ? Collections.emptyList()
                : getSession()
                    .createCriteria(DayAssignment.class)
                    .add(Restrictions.in("resource", resources))
                    .list();
    }

}
