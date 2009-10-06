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

import java.util.List;

import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Worker;

/**
 * DAO interface for the <code>Worker</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public interface IWorkerDAO extends IGenericDAO<Worker, Long> {

    /**
     * Returns workers which name/NIF partially matches with name, and complies
     * all of the given criterions
     *
     * @param name
     *            search worker by name/NIF
     * @param criterions
     *            search worker that matches with criterions
     * @return
     */
    @SuppressWarnings("unchecked")
    List<Worker> findByNameAndCriterions(String name, List<Criterion> criterions);

    /**
     * Returns workers which name/NIF partially matches with name
     *
     * @param name
     *            search worker by name(firstname or surname)/NIF
     *
     */
    List<Worker> findByNameOrNif(String name);

    /**
     * Finds a {@link Worker} with the NIF param that should be unique.
     *
     * @param nif
     *            The NIF to search the {@link Worker}
     * @return The {@link Worker} with this NIF
     * @throws InstanceNotFoundException
     *             If there're more than one {@link Worker} with this NIF or
     *             there isn't any {@link Worker} with this NIF
     */
    Worker findUniqueByNif(String nif) throws InstanceNotFoundException;

    /**
     * Return list of workers
     *
     * @return
     */
    List<Worker> getWorkers();
}
