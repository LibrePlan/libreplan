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

import java.util.List;

import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.Machine;

/**
 * DAO interface for the <code>Machine</code> entity.
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public interface IMachineDAO extends IIntegrationEntityDAO<Machine> {

    /**
     * Returns machines which name/NIF partially matches with name
     *
     * @param name
     *            search machine by name/Code
     *
     */
    List<Machine> findByNameOrCode(String name, boolean limitingResource);

    /**
     * Finds a {@link Machine} with the Code param that should be unique.
     *
     * @param code
     *            The Code to search the {@link Machine}
     * @return The {@link Machine} with this Code
     * @throws InstanceNotFoundException
     *             If there're more than one {@link Machine} with this Code or
     *             there isn't any {@link Machine} with this Code
     */
    Machine findUniqueByCode(String code) throws InstanceNotFoundException;

    /**
     * Finds a {@link Machine} with the Code param that should be unique
     * and opens a new transaction to do it.
     *
     * @param code
     *            The Code to search the {@link Machine}
     * @return The {@link Machine} with this Code
     * @throws InstanceNotFoundException
     *             If there're more than one {@link Machine} with this Code or
     *             there isn't any {@link Machine} with this Code
     */
    Machine findUniqueByCodeInAnotherTransaction(String code) throws InstanceNotFoundException;

    /**
     * Return list of machines
     *
     * @return
     */
    List<Machine> getAll();

    /**
     * Check in a new transaction if there is a machine with the same
     * code as the one passed as parameter
     */
    boolean existsMachineWithCodeInAnotherTransaction(String code);
}
