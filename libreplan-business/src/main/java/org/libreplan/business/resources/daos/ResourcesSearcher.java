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

package org.libreplan.business.resources.daos;

import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.ilike;
import static org.hibernate.criterion.Restrictions.in;
import static org.hibernate.criterion.Restrictions.like;
import static org.hibernate.criterion.Restrictions.or;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
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

    public IResourcesQuery<Machine> searchMachines() {
        return new Query<Machine>(Machine.class);
    }

    @Override
    public IResourcesQuery<Worker> searchWorkers() {
        return new Query<Worker>(Worker.class);
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
        public IResourcesQuery<T> byCriteria(
                Collection<? extends Criterion> criteria) {
            Validate.noNullElements(criteria);
            this.criteria = new ArrayList<Criterion>(criteria);
            return this;
        }

        @Override
        public IResourcesQuery<T> byResourceType(ResourceType type) {
            this.type = type;
            return this;
        }

        @Override
        public List<T> execute() {
            return adHocTransactionService
                    .runOnReadOnlyTransaction(new IOnTransaction<List<T>>() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public List<T> execute() {
                            Session session = sessionFactory
                                    .getCurrentSession();
                            List<T> resources = buildCriteria(session).list();
                            return restrictToSatisfyAllCriteria(resources);
                        }

                    });
        }

        private Criteria buildCriteria(Session session) {
            Criteria result = session.createCriteria(klass);
            result.add(eq("resourceType", type));
            addQueryByName(result);
            addFindRelatedWithSomeOfTheCriterions(result);
            result.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            return result;
        }

        private void addFindRelatedWithSomeOfTheCriterions(Criteria criteria) {
            if (!criteriaSpecified()) {
                return;
            }
            criteria.createCriteria("criterionSatisfactions").add(
                    in("criterion", Criterion.withAllDescendants(this.criteria)));
        }

        private boolean criteriaSpecified() {
            return this.criteria != null && !this.criteria.isEmpty();
        }

        private void addQueryByName(Criteria criteria) {
            if (name == null) {
                return;
            }
            final String nameWithWildcards = "%" + name + "%";
            if (klass.equals(Worker.class)) {
                criteria.add(or(
                        or(ilike("firstName", nameWithWildcards),
                                ilike("surname", nameWithWildcards)),
                        like("nif", nameWithWildcards)));
            } else if (klass.equals(Machine.class)) {
                criteria.add(or(ilike("name", nameWithWildcards),
                        ilike("code", nameWithWildcards)));
            } else {
                LOG.warn("can't handle " + klass);
            }
        }

        private List<T> restrictToSatisfyAllCriteria(List<T> resources) {
            if (!criteriaSpecified()) {
                return resources;
            }
            List<T> result = new ArrayList<T>();
            for (T each : resources) {
                if (each.satisfiesCriterionsAtSomePoint(criteria)) {
                    result.add(each);
                }
            }
            return result;
        }

        @Override
        public Map<CriterionType, Set<Criterion>> getCriteria() {
            return adHocTransactionService
                    .runOnReadOnlyTransaction(getCriterionsTree(klass));
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
            public IResourcesQuery<Resource> byCriteria(
                    Collection<? extends Criterion> criteria) {
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
                List<Resource> result = new ArrayList<Resource>();
                List<Worker> workers = searchWorkers.execute();
                result.addAll(workers);
                List<Machine> machines = searchMachines.execute();
                result.addAll(machines);
                return result;
            }

            @Override
            public Map<CriterionType, Set<Criterion>> getCriteria() {
                return adHocTransactionService
                        .runOnReadOnlyTransaction(getCriterionsTree(Resource.class));
            }
        };
    }

    @Autowired
    private ICriterionDAO criterionDAO;

    private IOnTransaction<Map<CriterionType, Set<Criterion>>> getCriterionsTree(
            final Class<? extends Resource> klassTheCriterionTypeMustBeRelatedWith) {
        return new IOnTransaction<Map<CriterionType, Set<Criterion>>>() {
            @Override
            public Map<CriterionType, Set<Criterion>> execute() {
                Map<CriterionType, Set<Criterion>> result = new LinkedHashMap<CriterionType, Set<Criterion>>();
                for (Criterion criterion : criterionDAO
                        .getAllSortedByTypeAndName()) {
                    CriterionType key = criterion.getType();
                    if (klassTheCriterionTypeMustBeRelatedWith
                            .isAssignableFrom(key.getResource().asClass())) {
                        if (!result.containsKey(key)) {
                            result.put(key, new LinkedHashSet<Criterion>());
                        }
                        result.get(key).add(criterion);
                    }
                }
                return result;
            }
        };
    }

}
