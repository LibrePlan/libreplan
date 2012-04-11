/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.web.common.components.finders;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.lang.StringUtils;
import org.libreplan.business.hibernate.notification.PredefinedDatabaseSnapshots;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.resources.entities.Resource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements all the methods needed to search the different criteria to filter
 * the {@link Resource}s.<br />
 * It provides the following criteria to filter: {@link Resource} and
 * {@link Criterion}.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class WorkerMultipleFiltersFinder extends MultipleFiltersFinder {

    @Autowired
    private PredefinedDatabaseSnapshots databaseSnapshots;

    protected WorkerMultipleFiltersFinder() {
    }

    @Override
    public List<FilterPair> getFirstTenFilters() {
        getListMatching().clear();
        fillWithFirstTenFiltersResources();
        fillWithFirstTenFiltersCriterions();
        addNoneFilter();
        return getListMatching();
    }

    private List<FilterPair> fillWithFirstTenFiltersResources() {
        Map<Class<?>, List<Resource>> mapResources = getMapResources();
        Iterator<Class<?>> iteratorClass = mapResources.keySet().iterator();
        while (iteratorClass.hasNext() && getListMatching().size() < 10) {
            Class<?> className = iteratorClass.next();
            for (int i = 0; getListMatching().size() < 10
                    && i < mapResources.get(className).size(); i++) {
                Resource resource = mapResources.get(className).get(i);
                addResource(className, resource);
            }
        }
        return getListMatching();
    }

    private Map<Class<?>, List<Resource>> getMapResources() {
        return databaseSnapshots.snapshotMapResources();
    }

    private void addResource(Class<?> className, Resource resource) {
        String pattern = resource.getName();
        getListMatching().add(
                new FilterPair(WorkerFilterEnum.RESOURCE, className
                        .getSimpleName(), pattern, resource));
    }

    private List<FilterPair> fillWithFirstTenFiltersCriterions() {
        SortedMap<CriterionType, List<Criterion>> mapCriterions = getMapCriterions();
        Iterator<CriterionType> iteratorCriterionType = mapCriterions.keySet()
                .iterator();
        while (iteratorCriterionType.hasNext() && getListMatching().size() < 10) {
            CriterionType type = iteratorCriterionType.next();
            for (int i = 0; getListMatching().size() < 10
                    && i < mapCriterions.get(type).size(); i++) {
                Criterion criterion = mapCriterions.get(type).get(i);
                addCriterion(type, criterion);
            }
        }
        return getListMatching();
    }

    private SortedMap<CriterionType, List<Criterion>> getMapCriterions() {
        return databaseSnapshots.snapshotCriterionsMap();
    }

    private void addCriterion(CriterionType type, Criterion criterion) {
        String pattern = criterion.getName() + " ( " + type.getName() + " )";
        getListMatching().add(
                new FilterPair(WorkerFilterEnum.CRITERION, type
                        .getResource().toLowerCase(), pattern, criterion));
    }

    public List<FilterPair> getMatching(String filter) {
        getListMatching().clear();
        if ((filter != null) && (!filter.isEmpty())) {
            filter = StringUtils.deleteWhitespace(filter.toLowerCase());
            searchInResources(filter);
            searchInCriterionTypes(filter);
        }

        addNoneFilter();
        return getListMatching();
    }

    private void searchInResources(String filter) {
        Map<Class<?>, List<Resource>> mapResources = databaseSnapshots
                .snapshotMapResources();
        for (Class<?> className : mapResources.keySet()) {
            for (Resource resource : mapResources.get(className)) {
                String name = StringUtils.deleteWhitespace(resource.getName()
                        .toLowerCase());
                if (name.contains(filter)) {
                    addResource(className, resource);
                    if ((filter.length() < 3) && (getListMatching().size() > 9)) {
                        return;
                    }
                }
            }
        }
    }

    private void searchInCriterionTypes(String filter) {
        boolean limited = (filter.length() < 3);
        for (CriterionType type : getMapCriterions().keySet()) {
            String name = StringUtils.deleteWhitespace(type.getName()
                    .toLowerCase());
            if (name.contains(filter)) {
                setFilterPairCriterionType(type, limited);
            } else {
                searchInCriterions(type, filter);
            }
        }
    }

    private void searchInCriterions(CriterionType type, String filter) {
        List<Criterion> list = getMapCriterions().get(type);
        if (list == null) {
            return;
        }
        for (Criterion criterion : list) {
            String name = StringUtils.deleteWhitespace(criterion.getName()
                    .toLowerCase());
            if (name.contains(filter)) {
                addCriterion(type, criterion);
                if ((filter.length() < 3) && (getListMatching().size() > 9)) {
                    return;
                }
            }
        }
    }

    private void setFilterPairCriterionType(CriterionType type, boolean limited) {
        List<Criterion> list = getMapCriterions().get(type);
        if (list == null) {
            return;
        }
        for (Criterion criterion : list) {
            addCriterion(type, criterion);
            if ((limited) && (getListMatching().size() > 9)) {
                return;
            }
        }
    }

}
