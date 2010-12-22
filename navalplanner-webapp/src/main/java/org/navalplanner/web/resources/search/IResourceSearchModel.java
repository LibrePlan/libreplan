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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.business.resources.entities.ResourceType;
import org.navalplanner.business.resources.entities.Worker;

/**
 * Conversation for worker search
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface IResourceSearchModel {

    public interface IResourcesQuery<T extends Resource> {

        /**
         * Restrict the result to resources that have name as a substring. The
         * match is case insensitive.
         *
         * @param name
         * @return this same object in order to cascade calls
         */
        IResourcesQuery<T> byName(String name);

        /**
         * Restrict the result to a list of {@link Resource} satisfying all
         * criteria at some point in time
         *
         * @param criteria
         * @return this same object in order to cascade calls
         */
        IResourcesQuery<T> byCriteria(Collection<? extends Criterion> criteria);

        /**
         * Restrict resources to the ones having the provided type. By default
         * if this method is not called, the resources are restricted to the
         * type NON_LIMITING_RESOURCE.
         * @param type
         * @return this same object in order to cascade calls
         */
        IResourcesQuery<T> byResourceType(ResourceType type);

        /**
         * Retrieve the list of resources that match the restrictions specified.
         *
         * @return
         */
        List<T> execute();

        /**
         * <p>
         * Gets all {@link Criterion} and groups then by {@link CriterionType}
         * with the condition that the {@link CriterionType#getResource()} is of
         * a type compatible for this query.
         * </p>
         * For example if this query has been created by
         * {@link IResourceSearchModel#searchWorkers()} only the criteria with
         * criterion type such its resource is {@link ResourceEnum.WORKER}
         * @return HashMap<CriterionType, Set<Criterion>>
         */
        Map<CriterionType, Set<Criterion>> getCriteria();
    }

    /**
     * Do the search limited to workers
     *
     * @return
     */
    public IResourcesQuery<Worker> searchWorkers();

    /**
     * Do the search limited to machines
     * @return
     */
    public IResourcesQuery<Machine> searchMachines();

    /**
     * Search machines or workers based on the value of resourceType
     * @param resourceType
     * @return
     */
    public IResourcesQuery<?> searchBy(ResourceEnum resourceType);

    /**
     * Search both resources and machines
     * @return
     */
    public IResourcesQuery<Resource> searchBoth();

}
