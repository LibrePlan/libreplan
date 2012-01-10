/*
 * This file is part of LibrePlan
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

package org.libreplan.business.costcategories.daos;

import java.util.List;

import org.libreplan.business.common.daos.IIntegrationEntityDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;

/**
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public interface ITypeOfWorkHoursDAO extends
        IIntegrationEntityDAO<TypeOfWorkHours> {

    /**
     * Checks if type is referenced by other entities examining the entities
     * that are related with {@link TypeOfWorkHours} via a foreign key
     *
     * @param type
     * @throws ValidationException
     */
    void checkIsReferencedByOtherEntities(TypeOfWorkHours type) throws ValidationException;

    boolean existsByCode(TypeOfWorkHours typeOfWorkHours);

    boolean existsByName(TypeOfWorkHours typeOfWorkHours);

    boolean existsTypeWithCodeInAnotherTransaction(String code);

    List<TypeOfWorkHours> findActive();

    TypeOfWorkHours findUniqueByCode(String code)
        throws InstanceNotFoundException;

    TypeOfWorkHours findUniqueByCode(TypeOfWorkHours typeOfWorkHours)
        throws InstanceNotFoundException;

    TypeOfWorkHours findUniqueByCodeInAnotherTransaction(String code)
            throws InstanceNotFoundException;

    TypeOfWorkHours findUniqueByName(String name)
            throws InstanceNotFoundException;

    TypeOfWorkHours findUniqueByNameInAnotherTransaction(String name)
            throws InstanceNotFoundException;

    List<TypeOfWorkHours> hoursTypeByNameAsc();

}