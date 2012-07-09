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

package org.libreplan.web.security;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.users.entities.OrderAuthorization;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.users.services.CustomUser;
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

    /**
     * Returns <code>true</code> if current user:
     *
     * <ul>
     * <li>Has role {@link UserRole#ROLE_SUPERUSER}</li>
     * <li>Or has at least one of the <code>roles</code> provided as parameters.
     * </ul>
     */
    public final static boolean isSuperuserOrUserInRoles(UserRole... roles) {
        if (isUserInRole(UserRole.ROLE_SUPERUSER)) {
            return true;
        }
        for (UserRole role : roles) {
            if (isUserInRole(role)) {
                return true;
            }
        }
        return false;
    }

    public final static String getSessionUserLoginName() {
        HttpServletRequest request = (HttpServletRequest)Executions
            .getCurrent().getNativeRequest();
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return null;
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

    /**
     * Returns <code>true</code> if current user:
     *
     * <ul>
     * <li>Has role {@link UserRole#ROLE_SUPERUSER}</li>
     * <li>Or has role {@link UserRole#ROLE_PLANNING}</li>
     * <li>Or has role {@link UserRole#ROLE_READ_ALL_PROJECTS}</li>
     * <li>Or has role {@link UserRole#ROLE_READ_EDIT_PROJECTS}</li>
     * <li>Or has role {@link UserRole#ROLE_CREATE_PROJECTS}</li>
     * <li>Or has any {@link OrderAuthorization} over any project</li>
     * </ul>
     */
    public final static boolean isSuperuserOrRolePlanningOrHasAnyAuthorization() {
        if (isSuperuserOrUserInRoles(UserRole.ROLE_PLANNING,
                UserRole.ROLE_READ_ALL_PROJECTS,
                UserRole.ROLE_EDIT_ALL_PROJECTS,
                UserRole.ROLE_CREATE_PROJECTS)) {
            return true;
        }

        return Registry.getTransactionService().runOnReadOnlyTransaction(
                new IOnTransaction<Boolean>() {
                    @Override
                    public Boolean execute() {
                        try {
                            String username = getLoggedUser().getUsername();
                            return Registry
                                    .getOrderAuthorizationDAO()
                                    .userOrItsProfilesHaveAnyAuthorization(
                                            Registry.getUserDAO()
                                                    .findByLoginName(
                                                            username));
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

}
