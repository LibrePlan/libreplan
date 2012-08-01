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

package org.libreplan.web.users;

import java.util.List;

import org.libreplan.business.users.entities.OrderAuthorization;
import org.libreplan.business.users.entities.OrderAuthorizationType;
import org.libreplan.business.users.entities.Profile;
import org.libreplan.business.users.entities.ProfileOrderAuthorization;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserOrderAuthorization;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;

/**
 * Model for UI operations related to {@link OrderAuthorization}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public interface IOrderAuthorizationModel {

    void initCreate(PlanningState planningState);

    void initEdit(PlanningState planningState);

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
