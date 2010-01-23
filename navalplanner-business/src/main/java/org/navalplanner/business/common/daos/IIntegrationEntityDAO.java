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

package org.navalplanner.business.common.daos;

import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;

/**
 * Common DAO interface for all entities used in application integration. All
 * DAO interfaces of entities used in application integration must extend from
 * this interface.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public interface IIntegrationEntityDAO<E extends IntegrationEntity>
    extends IGenericDAO<E, Long> {

    public boolean existsByCode(String code);

    public boolean existsByCodeAnotherTransaction(String code);

    public E findByCode(String code) throws InstanceNotFoundException;

    public E findByCodeAnotherTransaction(String code)
        throws InstanceNotFoundException;

    public E findExistingEntityByCode(String code);

}
