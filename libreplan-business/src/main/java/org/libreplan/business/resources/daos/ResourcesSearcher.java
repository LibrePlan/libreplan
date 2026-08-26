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

package org.libreplan.business.resources.daos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.resources.entities.Machine;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.ResourceEnum;
import org.libreplan.business.resources.entities.ResourceType;
import org.libreplan.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public class ResourcesSearcher implements IResourcesSearcher {

    private static final Log LOG = LogFactory.getLog(ResourcesSearcher.class);

    @Autowired
    private IAdHocTransactionService adHocTransactionService;

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public IResourcesQuery<Machine> searchMachines() {
        return new Query<>(Machine.class);
    }

    @Override
    public IResourcesQuery<Worker> searchWorkers() {
        return new Query<>(Worker.class);
    }

    class Query<T extends Resource> implements IResourcesQuery<T> {

        private final Class<T> klass;

        private String name = null;

        private List<Criterion> criteria = null;

        private ResourceType type = ResourceType.NON_LIMITING_RESOURCE;

        public Query(Class<T> klass) {
            this.klass = klass;
        }

        @Override
        public IResourcesQuery<T> byName(String name) {
            this.name = name;

            return this;
        }

        @Override
        public IResourcesQuery<T> byCriteria(Collection<? extends Criterion> criteria) {
            Validate.noNullElements(criteria);
            this.criteria = new ArrayList<>(criteria);
            return this;
        }

        @Override
        public IResourcesQuery<T> byResourceType(ResourceType type) {
            this.type = type;
            return this;
        }

        @Override
        public List<T> execute() {
            return adHocTransactionService.runOnReadOnlyTransaction(() -> {
                Session session = sessionFactory.getCurrentSession();
                List<T> resources = runQuery(session);

                return restrictToSatisfyAllCriteria(resources);
            });
        }

        private List<T> runQuery(Session session) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(klass);
            Root<T> root = cq.from(klass);
            cq.distinct(true);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("resourceType"), type));
            addQueryByName(cb, root, predicates);
            addFindRelatedWithSomeOfTheCriterions(cb, root, predicates);
            cq.where(predicates.toArray(new Predicate[0]));

            return session.createQuery(cq).getResultList();
        }

        private void addFindRelatedWithSomeOfTheCriterions(
                CriteriaBuilder cb, Root<T> root, List<Predicate> predicates) {

            if ( !criteriaSpecified() ) {
                return;
            }
            Join<T, ?> satisfactions = root.join("criterionSatisfactions");
            predicates.add(satisfactions.get("criterion").in(Criterion.withAllDescendants(this.criteria)));
        }

        private boolean criteriaSpecified() {
            return this.criteria != null && !this.criteria.isEmpty();
        }

        private void addQueryByName(CriteriaBuilder cb, Root<T> root, List<Predicate> predicates) {
            if ( name == null ) {
                return;
            }

            final String nameWithWildcards = "%" + name.toLowerCase() + "%";
            if ( klass.equals(Worker.class) ) {

                predicates.add(cb.or(
                        cb.or(
                                cb.like(cb.lower(root.get("firstName")), nameWithWildcards),
                                cb.like(cb.lower(root.get("surname")), nameWithWildcards)),
                        cb.like(cb.lower(root.get("nif")), nameWithWildcards)));

            } else if ( klass.equals(Machine.class) ) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), nameWithWildcards),
                        cb.like(cb.lower(root.get("code")), nameWithWildcards)));
            } else {
                LOG.warn("can't handle " + klass);
            }
        }

        private List<T> restrictToSatisfyAllCriteria(List<T> resources) {
            if ( !criteriaSpecified() ) {
                return resources;
            }
            List<T> result = new ArrayList<>();
            for (T each : resources) {
                if ( each.satisfiesCriterionsAtSomePoint(criteria) ) {
                    result.add(each);
                }
            }

            return result;
        }

        @Override
        public Map<CriterionType, Set<Criterion>> getCriteria() {
            return adHocTransactionService.runOnReadOnlyTransaction(getCriterionsTree(klass));
        }
    }

    @Override
    public IResourcesQuery<?> searchBy(ResourceEnum resourceType) {
        Validate.notNull(resourceType);

        switch (resourceType) {
            case MACHINE:
                return searchMachines();

            case WORKER:
                return searchWorkers();

            default:
                throw new RuntimeException("can't handle " + resourceType);
        }
    }

    @Override
    public IResourcesQuery<Resource> searchBoth() {
        final IResourcesQuery<Worker> searchWorkers = searchWorkers();
        final IResourcesQuery<Machine> searchMachines = searchMachines();

        return new IResourcesQuery<Resource>() {

            @Override
            public IResourcesQuery<Resource> byName(String name) {
                searchWorkers.byName(name);
                searchMachines.byName(name);
                return this;
            }

            @Override
            public IResourcesQuery<Resource> byCriteria(Collection<? extends Criterion> criteria) {
                searchWorkers.byCriteria(criteria);
                searchMachines.byCriteria(criteria);
                return this;
            }

            @Override
            public IResourcesQuery<Resource> byResourceType(ResourceType type) {
                searchWorkers.byResourceType(type);
                searchMachines.byResourceType(type);
                return this;
            }

            @Override
            public List<Resource> execute() {
                List<Resource> result = new ArrayList<>();
                List<Worker> workers = searchWorkers.execute();
                result.addAll(workers);
                List<Machine> machines = searchMachines.execute();
                result.addAll(machines);

                return result;
            }

            @Override
            public Map<CriterionType, Set<Criterion>> getCriteria() {
                return adHocTransactionService.runOnReadOnlyTransaction(getCriterionsTree(Resource.class));
            }
        };
    }

    @Autowired
    private ICriterionDAO criterionDAO;

    private IOnTransaction<Map<CriterionType, Set<Criterion>>> getCriterionsTree(
            final Class<? extends Resource> klassTheCriterionTypeMustBeRelatedWith) {

        return () -> {
            Map<CriterionType, Set<Criterion>> result = new LinkedHashMap<>();
            for (Criterion criterion : criterionDAO.getAllSortedByTypeAndName()) {
                CriterionType key = criterion.getType();

                if ( klassTheCriterionTypeMustBeRelatedWith.isAssignableFrom(key.getResource().asClass()) ) {
                    if ( !result.containsKey(key) ) {
                        result.put(key, new LinkedHashSet<>());
                    }
                    result.get(key).add(criterion);
                }
            }

            return result;
        };
    }

}
