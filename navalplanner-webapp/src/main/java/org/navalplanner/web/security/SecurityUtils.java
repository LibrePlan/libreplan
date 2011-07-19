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

package org.navalplanner.web.security;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.navalplanner.business.users.entities.UserRole;
import org.navalplanner.web.users.bootstrap.MandatoryUser;
import org.navalplanner.web.users.services.CustomUser;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.zkoss.zk.ui.Executions;

/**
 * Utility methods for security tasks.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Cristina Alvarino Perez <cristina.alvarino@comtecsf.es>
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public final static boolean isUserInRole(UserRole role) {
        return Executions.getCurrent().isUserInRole(role.name());
    }

    public final static String getSessionUserLoginName() {
        HttpServletRequest request = (HttpServletRequest)Executions
            .getCurrent().getNativeRequest();
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return MandatoryUser.USER.getLoginName();
        }
        return principal.getName();
    }

    /**
     * @return <code>null</code> if not user is logged
     */
    public final static CustomUser getLoggedUser() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            // This happens before processing first element of login page
            return null;
        }
        if (authentication.getPrincipal() instanceof CustomUser) {
            return (CustomUser) authentication.getPrincipal();
        }
        return null;
    }

    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
