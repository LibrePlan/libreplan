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

package org.navalplanner.web.users;

import java.util.List;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.users.entities.OrderAuthorization;
import org.navalplanner.business.users.entities.OrderAuthorizationType;
import org.navalplanner.business.users.entities.Profile;
import org.navalplanner.business.users.entities.ProfileOrderAuthorization;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserOrderAuthorization;

/**
 * Model for UI operations related to {@link OrderAuthorization}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public interface IOrderAuthorizationModel {

    void initCreate(Order order);

    void initEdit(Order order);

    void confirmSave();

    List<ProfileOrderAuthorization> getProfileOrderAuthorizations();

    List<UserOrderAuthorization> getUserOrderAuthorizations();

    /**
     * Adds {@link UserOrderAuthorization} objects in the model.
     *
     * @param user User object to receive the authorization
     * @param authorizations list of AuthorizationTypes
     * @return A list of the AuthorizationTypes which failed,
     * or null if all AuthorizationTypes were added successfully.
     */
    List<OrderAuthorizationType> addUserOrderAuthorization(
            User user, List<OrderAuthorizationType> authorizations);

    /**
     * Adds {@link ProfileOrderAuthorization} objects in the model.
     *
     * @param profile Profile object to receive the authorization
     * @param authorizations list of AuthorizationTypes
     * @return A list of the AuthorizationTypes which failed,
     * or null if all AuthorizationTypes were added successfully.
     */
    List<OrderAuthorizationType> addProfileOrderAuthorization(
            Profile profile, List<OrderAuthorizationType> authorizations);

    void removeOrderAuthorization(OrderAuthorization orderAuthorization);

}
