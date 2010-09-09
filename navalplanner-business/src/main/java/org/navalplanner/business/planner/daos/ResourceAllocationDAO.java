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

package org.navalplanner.business.planner.daos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificDayAssignment;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@ResourceAllocation}
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ResourceAllocationDAO extends
        GenericDAOHibernate<ResourceAllocation, Long> implements
        IResourceAllocationDAO {

    @Override
    public List<ResourceAllocation<?>> findAllocationsRelatedToAnyOf(
            List<Resource> resources) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        result.addAll(findSpecificAllocationsRelatedTo(resources, null, null));
        result.addAll(findGenericAllocationsFor(resources, null, null));
        return result;
    }

    @Override
    public List<ResourceAllocation<?>> findAllocationsRelatedToAnyOf(
            List<Resource> resources, LocalDate intervalFilterStartDate,
            LocalDate intervalFilterEndDate) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        result.addAll(findSpecificAllocationsRelatedTo(resources, intervalFilterStartDate, intervalFilterEndDate));
        result.addAll(findGenericAllocationsFor(resources, intervalFilterStartDate, intervalFilterEndDate));
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<GenericResourceAllocation> findGenericAllocationsFor(
            List<Resource> resources, LocalDate intervalFilterStartDate,
            LocalDate intervalFilterEndDate) {
        if(resources.isEmpty()) {
            return new ArrayList<GenericResourceAllocation>();
        }
        Criteria criteria  = getSession().createCriteria(GenericResourceAllocation.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
            .createCriteria(
                "genericDayAssignmentsContainers").createCriteria(
                "dayAssignments").add(
                Restrictions.in("resource", resources));

        filterByDatesIfApplyable(criteria, intervalFilterStartDate,
                intervalFilterEndDate);
        return (List<GenericResourceAllocation>) criteria.list();
    }

    @SuppressWarnings("unchecked")
    private List<SpecificResourceAllocation> findSpecificAllocationsRelatedTo(
            List<Resource> resources, LocalDate intervalFilterStartDate,
            LocalDate intervalFilterEndDate) {
        if(resources.isEmpty()) {
            return new ArrayList<SpecificResourceAllocation>();
        }
        Criteria criteria  = getSession().createCriteria(
                SpecificResourceAllocation.class);
        criteria.add(Restrictions.in("resource", resources));

        filterByDatesIfApplyable(criteria, intervalFilterStartDate,
                intervalFilterEndDate);
        return (List<SpecificResourceAllocation>) criteria.list();
    }

    private void filterByDatesIfApplyable(Criteria criteria,
            LocalDate intervalFilterStartDate, LocalDate intervalFilterEndDate) {
        if(intervalFilterStartDate != null || intervalFilterEndDate != null) {
            Criteria dateCriteria = criteria.createCriteria("task");
            if(intervalFilterEndDate != null) {
                dateCriteria.add(Restrictions.le("startDate.date",
                        intervalFilterEndDate));
            }
            if(intervalFilterStartDate != null) {
                dateCriteria.add(Restrictions.ge("endDate.date",
                        intervalFilterStartDate));
            }
        }
    }

    @Override
    public List<ResourceAllocation<?>> findAllocationsRelatedTo(
            Resource resource) {
        return stripAllocationsWithoutAssignations(findAllocationsRelatedToAnyOf(Arrays
                .asList(resource)));
    }

    @Override
    public List<ResourceAllocation<?>> findAllocationsRelatedTo(
            Resource resource, LocalDate intervalFilterStartDate,
            LocalDate intervalFilterEndDate) {
        return stripAllocationsWithoutAssignations(findAllocationsRelatedToAnyOf(Arrays
                .asList(resource), intervalFilterStartDate, intervalFilterEndDate));
    }

    private <R extends ResourceAllocation<?>> List<R> stripAllocationsWithoutAssignations(
            List<R> allocations) {
        List<R> result = new ArrayList<R>();
        for (R eachAllocation : allocations) {
            if (eachAllocation.hasAssignments()) {
                result.add(eachAllocation);
            }
        }
        return result;
    }

    private Map<Criterion, List<GenericResourceAllocation>> stripAllocationsWithoutAssignations(
            Map<Criterion, List<GenericResourceAllocation>> map) {
        Map<Criterion, List<GenericResourceAllocation>> result = new HashMap<Criterion, List<GenericResourceAllocation>>();
        for (Entry<Criterion, List<GenericResourceAllocation>> entry : map
                .entrySet()) {
            List<GenericResourceAllocation> valid = stripAllocationsWithoutAssignations(entry.getValue());
            if (!valid.isEmpty()) {
                result.put(entry.getKey(), valid);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Criterion, List<GenericResourceAllocation>> findGenericAllocationsByCriterion() {
        List<Object> results = getSession()
                .createQuery(
                "select generic, criterion "
                        + "from GenericResourceAllocation as generic "
                        + "join generic.criterions as criterion")
                .list();
        return stripAllocationsWithoutAssignations(byCriterion(results));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Criterion, List<GenericResourceAllocation>> findGenericAllocationsByCriterion(
            Date intervalFilterStartDate, Date intervalFilterEndDate) {
        String query = "select generic, criterion "
            + "from GenericResourceAllocation as generic "
            + "join generic.criterions as criterion ";
        if(intervalFilterStartDate != null || intervalFilterEndDate != null) {
            query += "inner join generic.task as task ";
            if(intervalFilterEndDate != null) {
                query += "where task.startDate.date <= :intervalFilterEndDate ";
            }
            if(intervalFilterStartDate != null) {
                if(intervalFilterEndDate != null) {
                    query += "and ";
                }
                else {
                    query += "where ";
                }
                query += "task.endDate.date >= :intervalFilterStartDate ";
            }
        }
        Query q = getSession().createQuery(query);
        if(intervalFilterStartDate != null) {
            q.setParameter("intervalFilterStartDate",
                    LocalDate.fromDateFields(intervalFilterStartDate));
        }
        if(intervalFilterEndDate != null) {
            q.setParameter("intervalFilterEndDate",
                    LocalDate.fromDateFields(intervalFilterEndDate));
        }
        return stripAllocationsWithoutAssignations(byCriterion(q.list()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Criterion, List<GenericResourceAllocation>> findGenericAllocationsByCriterionFor(
            List<Task> tasks) {
        if (tasks.isEmpty()) {
            return new HashMap<Criterion, List<GenericResourceAllocation>>();
        }
        List<Object> list = getSession().createQuery(
                "select generic, criterion "
                        + "from GenericResourceAllocation as generic "
                        + "join generic.criterions as criterion "
                        + "join generic.task task where task in(:tasks)")
                .setParameterList("tasks", tasks).list();
        return stripAllocationsWithoutAssignations(byCriterion(list));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Criterion, List<GenericResourceAllocation>> findGenericAllocationsBySomeCriterion(
            List<Criterion> criterions) {
        if (criterions.isEmpty()) {
            return new HashMap<Criterion, List<GenericResourceAllocation>>();
        }
        List<Object> list = getSession().createQuery(
                "select generic, criterion "
                        + "from GenericResourceAllocation as generic "
                        + "join generic.criterions as criterion "
                        + "where criterion in(:criterions)").setParameterList(
                "criterions", criterions).list();
        return stripAllocationsWithoutAssignations(byCriterion(list));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Criterion, List<GenericResourceAllocation>> findGenericAllocationsBySomeCriterion(
            List<Criterion> criterions, Date intervalFilterStartDate, Date intervalFilterEndDate) {
        if (criterions.isEmpty()) {
            return new HashMap<Criterion, List<GenericResourceAllocation>>();
        }
        String query = "select generic, criterion "
            + "from GenericResourceAllocation as generic "
            + "join generic.criterions as criterion ";
        if(intervalFilterStartDate != null || intervalFilterEndDate != null) {
            query += "inner join generic.task as task ";
        }
        query += "where criterion in(:criterions) ";
        if(intervalFilterEndDate != null) {
            query += "and task.startDate.date <= :intervalFilterEndDate ";
        }
        if(intervalFilterStartDate != null) {
            query += "and task.endDate.date >= :intervalFilterStartDate ";
        }

        Query q = getSession().createQuery(query);
        q.setParameterList("criterions", criterions);
        if(intervalFilterStartDate != null) {
            q.setParameter("intervalFilterStartDate",
                    LocalDate.fromDateFields(intervalFilterStartDate));
        }
        if(intervalFilterEndDate != null) {
            q.setParameter("intervalFilterEndDate",
                    LocalDate.fromDateFields(intervalFilterEndDate));
        }
        return stripAllocationsWithoutAssignations(byCriterion(q.list()));
    }

    private Map<Criterion, List<GenericResourceAllocation>> byCriterion(
            List<Object> results) {

        Map<Criterion, List<GenericResourceAllocation>> result = new HashMap<Criterion, List<GenericResourceAllocation>>();
        for (Object row : results) {
            GenericResourceAllocation allocation = getAllocation(row);
            Criterion criterion = getCriterion(row);
            if (!result.containsKey(criterion)) {
                result.put(criterion,
                        new ArrayList<GenericResourceAllocation>());
            }
            result.get(criterion).add(allocation);
        }
        return result;
    }

    private GenericResourceAllocation getAllocation(Object row) {
        Object[] elements = (Object[]) row;
        return (GenericResourceAllocation) elements[0];
    }

    private Criterion getCriterion(Object row) {
        Object[] elements = (Object[]) row;
        return (Criterion) elements[1];
    }

    @Override
    public List<SpecificDayAssignment> getSpecificAssignmentsBetween(
            Collection<Resource> relatedToOne, LocalDate start, LocalDate end) {
        return getSession().createCriteria(SpecificDayAssignment.class).add(
                Restrictions.and(Restrictions.in("resource", relatedToOne),
                        Restrictions.between("day", start, end))).list();
    }

}