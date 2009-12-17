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

package org.navalplanner.business.resources.daos;

import java.util.Collection;
import java.util.List;

import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

/**
 * DAO interface for the <code>Resource</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface IResourceDAO extends IGenericDAO<Resource, Long> {

    public List<Worker> getWorkers();

    public List<Worker> getRealWorkers();

    public List<Worker> getVirtualWorkers();

    /**
     * Returns all {@link Resource} which satisfy a set of {@link Criterion}
     */
    List<Resource> findAllSatisfyingCriterions(Collection<? extends Criterion> criterions);

    List<Resource> findResourcesRelatedTo(List<Task> tasks);

    List<Resource> getResources();
}
