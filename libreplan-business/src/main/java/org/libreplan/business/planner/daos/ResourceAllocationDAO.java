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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.joda.time.LocalDate;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.planner.entities.GenericResourceAllocation;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.SpecificResourceAllocation;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.scenarios.entities.Scenario;
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
            Scenario onScenario, List<Resource> resources) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        result.addAll(findSpecificAllocationsRelatedTo(onScenario, resources,
                null, null));
        result.addAll(findGenericAllocationsFor(onScenario, resources, null,
                null));
        return result;
    }

    @Override
    public List<ResourceAllocation<?>> findAllocationsRelatedToAnyOf(
            Scenario onScenario,
            List<Resource> resources, LocalDate intervalFilterStartDate,
            LocalDate intervalFilterEndDate) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        result.addAll(findSpecificAllocationsRelatedTo(onScenario, resources,
                intervalFilterStartDate, intervalFilterEndDate));
        result.addAll(findGenericAllocationsFor(onScenario, resources,
                intervalFilterStartDate, intervalFilterEndDate));
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<GenericResourceAllocation> findGenericAllocationsFor(
            final Scenario onScenario,
            final List<Resource> resources,
            final LocalDate intervalFilterStartDate,
            final LocalDate intervalFilterEndDate) {

        if (resources.isEmpty()) {
            return new ArrayList<GenericResourceAllocation>();
        }
        QueryBuilder queryBuilder = new QueryBuilder() {

            @Override
            protected String getBaseQuery() {
                return "select distinct generic from GenericResourceAllocation generic "
                        + "join generic.task task "
                        + "join generic.genericDayAssignmentsContainers container "
                        + "join container.dayAssignments dayAssignment";
            }

            @Override
            protected String getBaseConditions() {
                return "where dayAssignment.resource in (:resources)";
            }

            @Override
            protected void setBaseParameters(Query query) {
                query.setParameterList("resources", resources);
            }

            @Override
            protected IQueryPart[] getExtraParts() {
                return new IQueryPart[] {
                        new DatesInterval("task", intervalFilterStartDate,
                                intervalFilterEndDate),
                        new OnScenario("task", onScenario) };
            }

        };
        Query query = queryBuilder.build(getSession());
        return query.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SpecificResourceAllocation> findSpecificAllocationsRelatedTo(
            final Scenario onScenario,
            final List<Resource> resources,
            final LocalDate intervalFilterStartDate,
            final LocalDate intervalFilterEndDate) {

        if (resources.isEmpty()) {
            return new ArrayList<SpecificResourceAllocation>();
        }
        QueryBuilder queryBuilder = new QueryBuilder() {

            @Override
            protected String getBaseQuery() {
                return "select distinct specific from "
                        + "SpecificResourceAllocation specific "
                        + "join specific.task task";
            }

            @Override
            protected String getBaseConditions() {
                return "where specific.resource in (:resources)";
            }

            @Override
            protected void setBaseParameters(Query query) {
                query.setParameterList("resources", resources);
            }

            @Override
            protected IQueryPart[] getExtraParts() {
                return new IQueryPart[] {
                        new DatesInterval("task", intervalFilterStartDate,
                                intervalFilterEndDate),
                        new OnScenario("task", onScenario) };
            }
        };
        return (List<SpecificResourceAllocation>) queryBuilder.build(
                getSession()).list();
    }

    @Override
    public List<ResourceAllocation<?>> findAllocationsRelatedTo(
            Scenario onScenario,
            Resource resource, LocalDate intervalFilterStartDate,
            LocalDate intervalFilterEndDate) {
        return stripAllocationsWithoutAssignations(findAllocationsRelatedToAnyOf(
                onScenario, Arrays.asList(resource), intervalFilterStartDate,
                intervalFilterEndDate));
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

    @Override
    public Map<Criterion, List<GenericResourceAllocation>> findGenericAllocationsByCriterion(
            final Scenario onScenario,
            final Date intervalFilterStartDate, final Date intervalFilterEndDate) {

        QueryBuilder queryBuilder = new QueryBuilder() {

            @Override
            protected String getBaseQuery() {
                return "select generic, criterion "
                        + "from GenericResourceAllocation as generic "
                        + "join generic.criterions as criterion "
                        + "join generic.task as task";
            }

            @Override
            protected String getBaseConditions() {
                return "";
            }

            @Override
            protected void setBaseParameters(Query query) {
            }

            @Override
            protected IQueryPart[] getExtraParts() {
                return new IQueryPart[] {
                        new DatesInterval("task", intervalFilterStartDate,
                                intervalFilterEndDate),
                        new OnScenario("task", onScenario) };
            }

        };
        Query query = queryBuilder.build(getSession());

        return toCriterionMapFrom(query);
    }

    @Override
    public Map<Criterion, List<GenericResourceAllocation>> findGenericAllocationsBySomeCriterion(
            final Scenario onScenario,
            final List<Criterion> criterions,
            final Date intervalFilterStartDate, final Date intervalFilterEndDate) {

        if (criterions.isEmpty()) {
            return new HashMap<Criterion, List<GenericResourceAllocation>>();
        }

        QueryBuilder queryBuilder = new QueryBuilder() {

            @Override
            protected String getBaseQuery() {
                return "select generic, criterion "
                        + "from GenericResourceAllocation as generic "
                        + "join generic.task as task "
                        + "join generic.criterions as criterion ";
            }

            @Override
            protected String getBaseConditions() {
                return "where criterion in(:criterions) ";
            }

            @Override
            protected void setBaseParameters(Query query) {
                query.setParameterList("criterions", criterions);
            }

            @Override
            protected IQueryPart[] getExtraParts() {
                return new IQueryPart[] {
                        new DatesInterval("task", intervalFilterStartDate,
                                intervalFilterEndDate),
                        new OnScenario("task", onScenario) };
            }
        };
        Query q = queryBuilder.build(getSession());
        return toCriterionMapFrom(q);
    }

    @SuppressWarnings("unchecked")
    private Map<Criterion, List<GenericResourceAllocation>> toCriterionMapFrom(Query query){
        return addParents(stripAllocationsWithoutAssignations(byCriterion(query
                .list())));
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

    private Map<Criterion, List<GenericResourceAllocation>> addParents(
            Map<Criterion, List<GenericResourceAllocation>> byCriterion) {
        Map<Criterion, List<GenericResourceAllocation>> toBeMerged = new HashMap<Criterion, List<GenericResourceAllocation>>();

        for (Entry<Criterion, List<GenericResourceAllocation>> each : byCriterion
                .entrySet()) {
            Criterion criterion = each.getKey();
            for (Criterion parent : getParentsFrom(criterion)) {
                List<GenericResourceAllocation> childAllocations = each
                        .getValue();
                addToCriterion(toBeMerged, parent, childAllocations);
            }
        }
        return mergeTo(byCriterion, toBeMerged);
    }

    private void addToCriterion(
            Map<Criterion, List<GenericResourceAllocation>> map,
            Criterion criterion, List<GenericResourceAllocation> toAdd) {
        if (!map.containsKey(criterion)) {
            map.put(criterion,
                    new ArrayList<GenericResourceAllocation>());
        }
        map.get(criterion).addAll(toAdd);
    }

    private Map<Criterion, List<GenericResourceAllocation>> mergeTo(
            Map<Criterion, List<GenericResourceAllocation>> byCriterion,
            Map<Criterion, List<GenericResourceAllocation>> toMerge) {
        for (Entry<Criterion, List<GenericResourceAllocation>> each : toMerge
                .entrySet()) {
            addToCriterion(byCriterion, each.getKey(), each.getValue());
        }
        return byCriterion;
    }

    private List<Criterion> getParentsFrom(Criterion criterion) {
        List<Criterion> result = new ArrayList<Criterion>();
        Criterion current = criterion.getParent();
        while (current != null) {
            result.add(current);
            current = current.getParent();
        }
        return result;
    }

    @Override
    public List<SpecificResourceAllocation> findSpecificAllocationsRelatedTo(
            final Scenario onScenario,
            final Criterion criterion,
            final Date intervalFilterStartDate, final Date intervalFilterEndDate) {

        QueryBuilder builder = new QueryBuilder() {

            @Override
            protected String getBaseQuery() {
                return "select distinct s from SpecificResourceAllocation s "
                        + "join s.resource r "
                        + "join r.criterionSatisfactions satisfaction "
                        + "join satisfaction.criterion c join s.task t";
            }

            @Override
            protected String getBaseConditions() {
                return " where c = :criterion";
            }

            @Override
            protected void setBaseParameters(Query query) {
                query.setParameter("criterion", criterion);
            }

            @Override
            protected IQueryPart[] getExtraParts() {
                return new IQueryPart[] {
                        new DatesInterval("t", intervalFilterStartDate,
                                intervalFilterEndDate),
                        new OnScenario("t", onScenario) };
            }
        };

        Query query = builder.build(getSession());

        @SuppressWarnings("unchecked")
        List<SpecificResourceAllocation> result = query.list();
        return onlyAllocationsWithActiveCriterion(criterion, result,
                asLocalDate(intervalFilterStartDate),
                asLocalDate(intervalFilterEndDate));
    }

    private static LocalDate asLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDate.fromDateFields(date);
    }

    private List<SpecificResourceAllocation> onlyAllocationsWithActiveCriterion(
            Criterion criterion, List<SpecificResourceAllocation> allocations,
            LocalDate startInclusive, LocalDate endExclusive) {
        List<SpecificResourceAllocation> result = new ArrayList<SpecificResourceAllocation>();
        for (SpecificResourceAllocation each : allocations) {
            if (each.interferesWith(criterion, startInclusive, endExclusive)) {
                result.add(each);
            }
        }
        return result;
    }

    public static abstract class QueryBuilder {

        private static Pattern wherePattern = Pattern.compile("WHERE",
                Pattern.CASE_INSENSITIVE);

        protected abstract String getBaseQuery();

        protected abstract String getBaseConditions();

        protected abstract void setBaseParameters(Query query);

        protected abstract IQueryPart[] getExtraParts();

        public Query build(Session session) {
            final List<IQueryPart> extraParts = Arrays.asList(getExtraParts());

            String queryString = getBaseQuery();
            queryString = withExtraQueryParts(queryString, extraParts);

            queryString += " " + getBaseConditions();
            queryString = withExtraWhereConditions(queryString, extraParts);

            Query query = session.createQuery(queryString);
            setBaseParameters(query);
            injectParameters(query, extraParts);
            return query;
        }

        private String withExtraQueryParts(String initialQuery,
                List<IQueryPart> extraParts) {
            StringBuilder result = new StringBuilder(initialQuery);
            for (IQueryPart each : extraParts) {
                result.append(" ").append(each.queryPart());
            }
            return result.toString();
        }

        private String withExtraWhereConditions(String initialQuery,
                List<IQueryPart> extraParts) {
            StringBuilder result = new StringBuilder(initialQuery);
            boolean alreadyHasWhere = hasWhere(initialQuery);
            if (!alreadyHasWhere) {
                result.append(" where ");
            }
            List<String> conditionsToAdd = notEmptyConditions(extraParts);
            for (int i = 0; i < conditionsToAdd.size(); i++) {
                if (alreadyHasWhere || i != 0) {
                    result.append(" and ");
                }
                result.append(conditionsToAdd.get(i));
            }
            return result.toString();
        }

        private List<String> notEmptyConditions(List<IQueryPart> extraParts) {
            List<String> result = new ArrayList<String>();
            for (IQueryPart each : extraParts) {
                String toAdd = each.wherePart();
                if (!StringUtils.isEmpty(toAdd)) {
                    result.add(toAdd);
                }
            }
            return result;
        }

        private static boolean hasWhere(String initialQuery) {
            return wherePattern.matcher(initialQuery).find();
        }

        private void injectParameters(Query query, List<IQueryPart> extraParts) {
            for (IQueryPart each : extraParts) {
                each.injectParameters(query);
            }
        }

    }

    public interface IQueryPart {

        public abstract String queryPart();

        public abstract String wherePart();

        public abstract void injectParameters(Query query);

    }

    public static class DatesInterval implements IQueryPart {

        private final LocalDate startInclusive;
        private final LocalDate endInclusive;
        private final String baseAlias;

        public DatesInterval(String baseAlias, Date startInclusive,
                Date endInclusive) {
            this(baseAlias, asLocal(startInclusive), asLocal(endInclusive));
        }

        private static LocalDate asLocal(Date date) {
            if (date == null) {
                return null;
            }
            return LocalDate.fromDateFields(date);
        }

        public DatesInterval(String baseAlias, LocalDate startInclusive,
                LocalDate endInclusive) {
            this.baseAlias = baseAlias;
            this.startInclusive = startInclusive;
            this.endInclusive = endInclusive;
        }

        @Override
        public String queryPart() {
            return "";
        }

        @Override
        public String wherePart() {
            String result = "";
            if (startInclusive != null) {
                result += baseAlias + ".endDate.date >= :startInclusive";
            }
            if (!result.isEmpty() && endInclusive != null) {
                result += " and ";
            }
            if (endInclusive != null) {
                result += baseAlias + ".startDate.date <= :endInclusive";
            }
            return result;
        }

        @Override
        public void injectParameters(Query query) {
            if (startInclusive != null) {
                query.setParameter("startInclusive", startInclusive);
            }
            if (endInclusive != null) {
                query.setParameter("endInclusive", endInclusive);
            }
        }

    }

    public class OnScenario implements IQueryPart {

        private final Scenario onScenario;
        private final String taskAlias;

        private OnScenario(String taskAlias, Scenario onScenario) {
            Validate.notNull(taskAlias);
            Validate.notNull(onScenario);
            this.taskAlias = taskAlias;
            this.onScenario = onScenario;
        }

        @Override
        public String queryPart() {
            return "join " + taskAlias
                    + ".taskSource.schedulingData as schedulingData "
                    + "join schedulingData.orderElement as orderElement "
                    + ", OrderVersion as version ";
        }

        @Override
        public String wherePart() {
            return "orderElement.schedulingDatasForVersion[version] = schedulingData "
                    + "and version.ownerScenario = :scenario";
        }

        @Override
        public void injectParameters(Query query) {
            query.setParameter("scenario", onScenario);
        }
    }

}