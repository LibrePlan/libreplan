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

package org.navalplanner.business.users.bootstrap;

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

    USER(Arrays.asList(UserRole.ROLE_BASIC_USER)),
    ADMIN(Arrays.asList(UserRole.ROLE_BASIC_USER,
        UserRole.ROLE_ADMINISTRATION));

    private Set<UserRole> initialRoles;

    private MandatoryUser(Collection<UserRole> initialUserRoles) {
        this.initialRoles = new HashSet<UserRole>(initialUserRoles);
    }

    public Set<UserRole> getInitialRoles() {
        return initialRoles;
    }

}
