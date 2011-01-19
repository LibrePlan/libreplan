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

package org.navalplanner.web.resources.search;

import java.util.ArrayList;
import java.util.List;

import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.web.common.components.ResourceAllocationBehaviour;
import org.navalplanner.web.common.components.bandboxsearch.BandboxMultipleSearch;
import org.navalplanner.web.common.components.finders.FilterPair;
import org.navalplanner.web.common.components.finders.ResourceAllocationFilterEnum;
import org.navalplanner.web.planner.allocation.INewAllocationsAdder;
import org.navalplanner.web.resources.search.IResourceSearchModel.IResourcesQuery;
import org.zkoss.zk.ui.Component;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 *
 *         Controller for searching for {@link Resource}
 */
public class NewAllocationSelectorComboController extends
        AllocationSelectorController {

    private ResourceAllocationBehaviour behaviour;

    private BandboxMultipleSearch bbMultipleSearch;

    public NewAllocationSelectorComboController(ResourceAllocationBehaviour behaviour) {
        this.behaviour = behaviour;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        bbMultipleSearch.setFinder(behaviour.getFinder());
    }

    /**
     * Does the actual search for workers
     *
     * @param criteria
     */
    private List<? extends Resource> searchResources(List<Criterion> criteria) {
        return query(inferType(criteria)).byCriteria(criteria)
                .byResourceType(behaviour.getType()).execute();
    }

    private static ResourceEnum inferType(List<Criterion> criteria) {
        if (criteria.isEmpty()) {
            // FIXME resolve the ambiguity. One option is asking the user
            return ResourceEnum.WORKER;
        }
        return first(criteria).getType().getResource();
    }

    private static Criterion first(List<Criterion> list) {
        return list.iterator().next();
    }

    private IResourcesQuery<?> query(ResourceEnum resourceEnum) {
        return resourceSearchModel.searchBy(resourceEnum);
    }

    /**
     * Returns list of selected {@link Criterion}, selects only those which are
     * leaf nodes
     * @return
     */
    public List<Criterion> getSelectedCriterions() {
        List<Criterion> criteria = new ArrayList<Criterion>();
        for (FilterPair pair : getSelectedItems()) {
            if (pair.getType().equals(ResourceAllocationFilterEnum.Criterion)) {
                criteria.add((Criterion) pair.getValue());
            }
        }
        return criteria;
    }

    private List<FilterPair> getSelectedItems() {
        return ((List<FilterPair>) bbMultipleSearch
                .getSelectedElements());
    }

    private boolean isGeneric() {
        return ((FilterPair) getSelectedItems().get(0)).getType().equals(
                ResourceAllocationFilterEnum.Criterion);
    }

    public void onClose() {
        clearAll();
    }

    public void clearAll() {
        bbMultipleSearch.clear();
    }

    public List<Resource> getSelectedResources() {
        List<Resource> resources = new ArrayList<Resource>();
        for (FilterPair pair : getSelectedItems()) {
            if (pair.getType().equals(ResourceAllocationFilterEnum.Resource)) {
                resources.add((Resource) pair.getValue());
            }
        }
        return resources;
    }

    public void addTo(INewAllocationsAdder allocationsAdder) {
        if (!getSelectedItems().isEmpty()) {
            if (isGeneric()) {
                List<Criterion> criteria = getSelectedCriterions();
                List<? extends Resource> resources = searchResources(criteria);
                ResourceEnum type = inferType(criteria);
                allocationsAdder.addGeneric(type, criteria, resources);
            } else {
                allocationsAdder.addSpecific(getSelectedResources());
            }
        }
    }

    public void setDisabled(boolean disabled) {
        bbMultipleSearch.clear();
        bbMultipleSearch.setDisabled(disabled);
    }

}
