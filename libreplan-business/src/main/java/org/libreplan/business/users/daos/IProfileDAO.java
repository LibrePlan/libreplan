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

package org.libreplan.business.users.daos;

import java.util.List;

import org.libreplan.business.common.daos.IGenericDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.users.entities.OrderAuthorization;
import org.libreplan.business.users.entities.Profile;

/**
 * DAO interface for the <code>Profile</code> entity.
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public interface IProfileDAO extends IGenericDAO<Profile, Long>{

    boolean existsByProfileName(String profileName);

    boolean existsByProfileNameAnotherTransaction(String profileName);

    Profile findByProfileName(String profileName)
        throws InstanceNotFoundException;

    Profile findByProfileNameAnotherTransaction(String profileName)
        throws InstanceNotFoundException;

    void checkHasUsers(Profile profile) throws ValidationException;

    List<OrderAuthorization> getOrderAuthorizationsByProfile(Profile profile);

    Profile findByProfileNameLoadingRoles(String profileName)
            throws InstanceNotFoundException;

}
