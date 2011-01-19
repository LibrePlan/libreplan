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

package org.navalplanner.web.users.bootstrap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.navalplanner.business.users.entities.UserRole;

/**
 * It enumerates the mandatory users (login names) for running the application.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public enum MandatoryUser {

    USER(new ArrayList<UserRole>()),
    ADMIN(Arrays.asList(UserRole.ROLE_ADMINISTRATION,
        UserRole.ROLE_READ_ALL_ORDERS,
        UserRole.ROLE_EDIT_ALL_ORDERS,
        UserRole.ROLE_CREATE_ORDER)),
    WSREADER(Arrays.asList(UserRole.ROLE_WS_READER)),
    WSWRITER(Arrays.asList(UserRole.ROLE_WS_READER,
        UserRole.ROLE_WS_WRITER));

    private Set<UserRole> initialRoles;

    private MandatoryUser(Collection<UserRole> initialUserRoles) {
        this.initialRoles = new HashSet<UserRole>(initialUserRoles);
    }

    public String getLoginName() {
        return this.name().toLowerCase();
    }

    public String getClearPassword() {
        return getLoginName();
    }

    public Set<UserRole> getInitialRoles() {
        return initialRoles;
    }

}
