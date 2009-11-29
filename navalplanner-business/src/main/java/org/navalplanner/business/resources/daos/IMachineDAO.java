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
import org.navalplanner.business.resources.entities.Machine;

/**
 * DAO interface for the <code>Machine</code> entity.
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 */
public interface IMachineDAO extends IGenericDAO<Machine, Long> {

    /**
     * Returns machines which name/NIF partially matches with name, and complies
     * all of the given criterions
     *
     * @param name
     *            search machine by name/NIF
     * @param criterions
     *            search machine that matches with criterions
     * @return
     */
    @SuppressWarnings("unchecked")
    List<Machine> findByNameAndCriterions(String name, List<Criterion> criterions);

    /**
     * Returns machines which name/NIF partially matches with name
     *
     * @param name
     *            search machine by name/Code
     *
     */
    List<Machine> findByNameOrCode(String name);

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
