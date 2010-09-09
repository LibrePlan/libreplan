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

package org.navalplanner.web.common.components.finders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.hibernate.notification.IAutoUpdatedSnapshot;
import org.navalplanner.business.hibernate.notification.PredefinedDatabaseSnapshots;
import org.navalplanner.business.hibernate.notification.ReloadOn;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.VirtualWorker;
import org.navalplanner.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements all the methods needed to search the criterion and resources to
 * allocate to the tasks. Provides multiples searches to allocate several
 * {@link Criterion} or an especific {@link Resource}.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class ResourceAllocationMultipleFiltersFinder extends
        MultipleFiltersFinder {

    @Autowired
    private IResourceDAO resourceDAO;

    private IFilterEnum mode = FilterEnumNone.None;

    private boolean isLimitingResourceAllocation = false;

    private IAutoUpdatedSnapshot<Map<Class<?>, List<Resource>>> mapResources;

    @Autowired
    private PredefinedDatabaseSnapshots databaseSnapshots;

    protected ResourceAllocationMultipleFiltersFinder() {

    }

    protected ResourceAllocationMultipleFiltersFinder(
            boolean isLimitingResourceAllocation) {
        this.isLimitingResourceAllocation = isLimitingResourceAllocation;
    }

    @Transactional(readOnly = true)
    public void init() {
        mapResources = getSnapshotRefresher()
                .takeSnapshot(
                        onTransaction(getResourcesMapCallable()),
                        ReloadOn.onChangeOf(Resource.class, Worker.class,
                        Machine.class, VirtualWorker.class));
    }

    private Callable<Map<Class<?>, List<Resource>>> getResourcesMapCallable() {
        return new Callable<Map<Class<?>, List<Resource>>>() {

            @Override
            public Map<Class<?>, List<Resource>> call() throws Exception {
                Map<Class<?>, List<Resource>> result = new HashMap<Class<?>, List<Resource>>();
                result.put(Worker.class,
                        new ArrayList<Resource>(resourceDAO.getRealWorkers()));
                result.put(Machine.class,
                        new ArrayList<Resource>(resourceDAO.getMachines()));
                result.put(VirtualWorker.class, new ArrayList<Resource>(
                        resourceDAO.getVirtualWorkers()));
                return result;
            }
        };
    }

    public List<FilterPair> getFirstTenFilters() {
        getListMatching().clear();
        if (!isModeResource()) {
            fillWithFirstTenFiltersCriterions();
        }
        if (isModeNone()) {
            fillWithFirstTenFiltersResources();
        }
        addNoneFilter();
        return getListMatching();
    }

    private List<FilterPair> fillWithFirstTenFiltersResources() {
        Map<Class<?>, List<Resource>> mapResources = this.mapResources
                .getValue();
        Iterator<Class<?>> iteratorClass = mapResources.keySet().iterator();
        while (iteratorClass.hasNext() && getListMatching().size() < 10) {
            Class<?> className = iteratorClass.next();
            for (int i = 0; getListMatching().size() < 10
                    && i < mapResources.get(className).size(); i++) {
                Resource resource = mapResources.get(className).get(i);

                if (isLimitingResourceAllocation != resource
                        .isLimitingResource()) {
                    continue;
                }

                String pattern = className.getSimpleName() + " :: "
                        + resource.getName();
                getListMatching().add(
                        new FilterPair(ResourceAllocationFilterEnum.Resource,
                                pattern, resource));
            }
        }
        return getListMatching();
    }

    private List<FilterPair> fillWithFirstTenFiltersCriterions() {
        Map<CriterionType, List<Criterion>> mapCriterions = getCriterionsMap();
        Iterator<CriterionType> iteratorCriterionType = mapCriterions.keySet()
                .iterator();
        while (iteratorCriterionType.hasNext() && getListMatching().size() < 10) {
            CriterionType type = iteratorCriterionType.next();
            for (int i = 0; getListMatching().size() < 10
                    && i < mapCriterions.get(type).size(); i++) {
                Criterion criterion = mapCriterions.get(type).get(i);
                String pattern = type.getName() + " :: " + criterion.getName();
                getListMatching().add(
                        new FilterPair(ResourceAllocationFilterEnum.Criterion,
                                pattern, criterion));
            }
        }
        return getListMatching();
    }

    private Map<CriterionType, List<Criterion>> getCriterionsMap() {
        return this.databaseSnapshots.snapshotCriterionsMap();
    }

    public List<FilterPair> getMatching(String filter) {
        getListMatching().clear();
        if ((filter != null) && (!filter.isEmpty())) {
            filter = StringUtils.deleteWhitespace(filter.toLowerCase());
            searchInCriterionTypes(filter);
            searchInResources(filter);
        }

        addNoneFilter();
        return getListMatching();
    }

    private void searchInCriterionTypes(String filter) {
        Map<CriterionType, List<Criterion>> mapCriterions = getCriterionsMap();
        boolean limited = (filter.length() < 3);
        for (CriterionType type : mapCriterions.keySet()) {
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
        Map<CriterionType, List<Criterion>> mapCriterions = getCriterionsMap();
        for (Criterion criterion : mapCriterions.get(type)) {
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
        Map<CriterionType, List<Criterion>> mapCriterions = getCriterionsMap();
        for (Criterion criterion : mapCriterions.get(type)) {
            addCriterion(type, criterion);
            if ((limited) && (getListMatching().size() > 9)) {
                return;
            }
        }
    }

    private void searchInResources(String filter) {
        Map<Class<?>, List<Resource>> mapResources = this.mapResources
                .getValue();
        boolean limited = (filter.length() < 3);
        for (Class<?> className : mapResources.keySet()) {
            for (Resource resource : mapResources.get(className)) {

                if (isLimitingResourceAllocation != resource
                        .isLimitingResource()) {
                    continue;
                }

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

    private void setFilterPairResource(Class className, boolean limited) {
        Map<Class<?>, List<Resource>> mapResources = this.mapResources
                .getValue();
        for (Resource resource : mapResources.get(className)) {
            addResource(className, resource);
            if ((limited) && (getListMatching().size() > 9)) {
                return;
            }
        }
    }

    private void addCriterion(CriterionType type, Criterion criterion) {
        String pattern = type.getName() + " :: " + criterion.getName();
        getListMatching().add(
                new FilterPair(ResourceAllocationFilterEnum.Criterion, pattern,
                        criterion));
    }

    private void addResource(Class className, Resource resource) {
        String pattern = className.getSimpleName() + " :: "
                + resource.getName();
        getListMatching().add(
                new FilterPair(ResourceAllocationFilterEnum.Resource, pattern,
                        resource));
    }

    public boolean isValidNewFilter(List filterValues, Object obj) {
        if (!super.isValidNewFilter(filterValues, obj)) {
            return false;
        }
        FilterPair filter = (FilterPair) obj;
        currentMode(filterValues);

        if (filterValues.isEmpty()) {
            mode = ((FilterPair) obj).getType();
            return true;
        }
        if (isModeResource()) {
            return false;
        }
        if (!isModeResource()
                && filter.getType().equals(
                        ResourceAllocationFilterEnum.Resource)) {
            return false;
        }
        return true;
    }

    private boolean isModeResource() {
        return mode.equals(ResourceAllocationFilterEnum.Resource);
    }

    private boolean isModeCriterion() {
        return mode.equals(ResourceAllocationFilterEnum.Criterion);
    }

    private boolean isModeNone() {
        return mode.equals(FilterEnumNone.None);
    }

    private void currentMode(List filterValues) {
        for (FilterPair filter : (List<FilterPair>) filterValues) {
            if (filter.getType().equals(ResourceAllocationFilterEnum.Resource)) {
                mode = ResourceAllocationFilterEnum.Resource;
            }else{
                mode = ResourceAllocationFilterEnum.Criterion;
            }
        }
        mode = FilterEnumNone.None;
    }

    public String objectToString(Object obj) {
        FilterPair filterPair = (FilterPair) obj;
        String text;
        if (filterPair.getType().equals(ResourceAllocationFilterEnum.Criterion)) {
            text = filterPair.getType() + "(" + filterPair.getPattern() + "); ";
        } else {
            text = filterPair.getType() + "(" + filterPair.getPattern() + ") ";
        }
        return text;
    }

    public boolean updateDeletedFilters(List filterValues, String value) {
        boolean result = false;
        if (isModeResource()) {
            result = updateDeletedFiltersInModeResource(filterValues, value);
        } else {
            result = updateDeletedFiltersInModeCriterion(filterValues, value);
        }
        currentMode(filterValues);
        return result;
    }

    public boolean updateDeletedFiltersInModeResource(List filterValues,
            String value) {
        List<FilterPair> list = new ArrayList<FilterPair>();
        list.addAll(filterValues);
        if (filterValues.size() == 1) {
            FilterPair filterPair = list.get(0);
            String filter = filterPair.getType() + "("
                    + filterPair.getPattern() + ")";
            if (!isFilterEquals(value, filter)) {
                filterValues.remove(filterPair);
                return true;
            }
        }
        return false;
    }

    public boolean updateDeletedFiltersInModeCriterion(List filterValues,
            String value) {
        return super.updateDeletedFilters(filterValues, value);
    }

    public void setLimintingResourceAllocation(boolean val) {
        isLimitingResourceAllocation = val;
    }
}
