/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.resources.daos;

import java.util.Date;
import java.util.List;

import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.reports.dtos.HoursWorkedPerResourceDTO;
import org.navalplanner.business.reports.dtos.HoursWorkedPerWorkerInAMonthDTO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

/**
 * DAO interface for the <code>Resource</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface IResourceDAO extends IIntegrationEntityDAO<Resource> {

    /**
     * Returns all {@link Resource} which are related with tasks
     *
     * @param tasks
     * @return
     */
    List<Resource> findResourcesRelatedTo(List<Task> tasks);

    /**
     * Returns all {@link Machine}
     *
     * @return
     */
    List<Machine> getMachines();

    /**
     * Returns all real resources ({@link Machine} and {@link Worker})
     *
     * @return
     */
    List<Resource> getRealResources();

    /**
     * Returns all {@link Worker} which are not virtual
     *
     * @return
     */
    List<Worker> getRealWorkers();

    /**
     * Returns all {@link Resource}
     *
     * @return
     */
    List<Resource> getResources();

    /**
     * Returns all {@link Worker} which are virtual
     *
     * @return
     */
    List<Worker> getVirtualWorkers();

    /**
     * Returns all {@link Worker} (including those which are virtual)
     *
     * @return
     */
    List<Worker> getWorkers();

    /**
     *
     * Returns all {@link Resource} which are limiting
     *
     * @return
     */
    List<Resource> getAllLimitingResources();

    /**
     *
     * Returns all {@link Resource} which are not limiting
     *
     * @return
     */
    List<Resource> getAllNonLimitingResources();

    /**
     * Returns all {@link HoursWorkedPerResourceDTO} per {@link Resource} between
     * the specified dates.
     * @return
     */
    List<HoursWorkedPerResourceDTO> getWorkingHoursPerWorker(
            List<Resource> resources, List<Label> labels,
            List<Criterion> criterions, Date startingDate,
            Date endingDate);

    /**
     * Returns all {@link HoursWorkedPerWorkerInAMonthDTO} in year and month
     *
     * @param year
     * @param month
     * @return
     */
    List<HoursWorkedPerWorkerInAMonthDTO> getWorkingHoursPerWorker(Integer year, Integer month);

}
