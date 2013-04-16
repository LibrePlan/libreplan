/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

package org.libreplan.business.common.daos;

import java.util.List;

import org.libreplan.business.common.entities.Connector;

/**
 * Contract for {@link Conn}
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public interface IConnectorDAO extends IGenericDAO<Connector, Long> {

    List<Connector> getAll();

    Connector findUniqueByName(String name);

    boolean existsByNameAnotherTransaction(Connector connector);

    Connector findUniqueByNameAnotherTransaction(String name);

}
