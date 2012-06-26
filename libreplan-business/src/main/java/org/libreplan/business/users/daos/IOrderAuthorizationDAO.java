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
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.users.entities.OrderAuthorization;
import org.libreplan.business.users.entities.Profile;
import org.libreplan.business.users.entities.User;

/**
 * DAO interface for the {@link OrderAuthorization} entity.
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public interface IOrderAuthorizationDAO extends IGenericDAO<OrderAuthorization, Long> {

    /**
     * Retrieves the list of {@link OrderAuthorization} objects related with
     * the specified {@link Order} object.
     * @param order {@link Order} object
     * @return list of {@link OrderAuthorization} objects
     */
    List<OrderAuthorization> listByOrder(Order order);

    /**
     * Retrieves the list of {@link OrderAuthorization} objects related with
     * the specified {@link User} object.
     * @param user {@link User} object
     * @return list of {@link OrderAuthorization} objects
     */
    List<OrderAuthorization> listByUser(User user);

    /**
     * Retrieves the list of {@link OrderAuthorization} objects related with
     * the specified {@link Profile} object.
     * @param profile {@link Profile} object
     * @return list of {@link OrderAuthorization} objects
     */
    List<OrderAuthorization> listByProfile(Profile profile);

    /**
     * Retrieves the list of {@link OrderAuthorization} objects related with
     * the specified {@link User} object or with the {@link Profile} objects
     * contained by the user.
     * @param user {@link User} object
     * @return list of {@link OrderAuthorization} objects
     */
    List<OrderAuthorization> listByUserAndItsProfiles(User user);

    /**
     * Returns <code>true</code> if the user or its profile have any
     * {@link OrderAuthorization}. That means that the user should have access
     * to the proper pages (company view and projects list).
     *
     * @param user
     *            {@link User} object
     * @return <code>true</code> if the user or its profile have any
     *         authorization
     */
    boolean userOrItsProfilesHaveAnyAuthorization(User user);

    /**
     * Retrieves the list of {@link OrderAuthorization} objects related with
     * the specified {@link Order} and {@link User} objects.
     * @param order {@link Order} object
     * @param user {@link User} object
     * @return list of {@link OrderAuthorization} objects
     */
    List<OrderAuthorization> listByOrderAndUser(Order order, User user);

    /**
     * Retrieves the list of {@link OrderAuthorization} objects related with
     * the specified {@link Order} and {@link Profile} objects.
     * @param order {@link Order} object
     * @param profile {@link Profile} object
     * @return list of {@link OrderAuthorization} objects
     */
    List<OrderAuthorization> listByOrderAndProfile(Order order, Profile profile);

    /**
     * Retrieves the list of {@link OrderAuthorization} objects related with
     * the specified {@link Order} and {@link User} object or with the
     * {@link Profile} objects contained by the user.
     * @param order {@link Order} object
     * @param user user {@link User} object
     * @return list of {@link OrderAuthorization} objects
     */
    List<OrderAuthorization> listByOrderUserAndItsProfiles(Order order,
            User user);

}
