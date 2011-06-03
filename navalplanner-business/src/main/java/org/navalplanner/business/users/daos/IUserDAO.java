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

package org.navalplanner.business.users.daos;

import java.util.List;

import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.users.entities.User;

/**
 * DAO interface for the <code>User</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public interface IUserDAO extends IGenericDAO<User, Long>{

    /**
     * NOTE: Login name comparison is case-insensitive.
     */
    public User findByLoginName(String loginName)
        throws InstanceNotFoundException;

    /**
     * NOTE: Login name comparison is case-insensitive, and the method is
     * executed in another transaction.
     */
    public User findByLoginNameAnotherTransaction(String loginName)
        throws InstanceNotFoundException;

    /**
     * NOTE: Login name comparison is case-insensitive.
     */
    public boolean existsByLoginName(String loginName);

    /**
     * NOTE: Login name comparison is case-insensitive, and the method is
     * executed in another transaction.
     */
    public boolean existsByLoginNameAnotherTransaction(String loginName);

    /**
     * Finds a User entity by its loginName, among those with the disabled
     * attribute set to false.
     * @param loginName loginName to perform the search. NOTE: Login name
     * comparison is case-insensitive.
     * @return a {@link User} object.
     * @throws InstanceNotFoundException
     */
    User findByLoginNameNotDisabled(String loginName)
            throws InstanceNotFoundException;

    /**
     * Retrieves a list of the User entities which attribute 'disabled' has
     * the value false.
     *
     * @return a list of {@link User} object.
     */
    public List<User> listNotDisabled();

    public List<User> findByLastConnectedScenario(Scenario scenario);

}
