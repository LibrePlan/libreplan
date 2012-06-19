/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.web.users.services;

import javax.servlet.http.HttpServletRequest;

import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.users.daos.IOrderAuthorizationDAO;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.ui.TargetUrlResolverImpl;
import org.springframework.security.ui.savedrequest.SavedRequest;
import org.springframework.security.userdetails.UserDetails;

/**
 * Determines the URL for authenticated users depending on if user is bound or
 * not to any resource.<br />
 *
 * If the user is bound to a resource then the target URL will be the user
 * dashboard.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class CustomTargetUrlResolver extends TargetUrlResolverImpl {

    public final static String USER_DASHBOARD_URL = "/myaccount/userDashboard.zul";

    private static final String PLANNING_URL = "/planner/index.zul";

    private static final String SETTINGS_URL = "/myaccount/settings.zul";

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IOrderAuthorizationDAO orderAuthorizationDAO;

    @Autowired
    private IAdHocTransactionService transactionServiceDAO;

    @Override
    public String determineTargetUrl(SavedRequest savedRequest,
            HttpServletRequest currentRequest, final Authentication auth) {
        if (isUserInRole(auth, UserRole.ROLE_SUPERUSER.name())) {
            return super.determineTargetUrl(savedRequest, currentRequest, auth);
        }

        if (isUserInRole(auth, UserRole.ROLE_BOUND_USER.name())) {
            return USER_DASHBOARD_URL;
        }

        if (isUserInRole(auth, UserRole.ROLE_PLANNING.name())) {
            return PLANNING_URL;
        }

        boolean userOrItsProfilesHaveAnyAuthorization = transactionServiceDAO
                .runOnReadOnlyTransaction(new IOnTransaction<Boolean>() {
            @Override
            public Boolean execute() {
                try {
                    UserDetails userDetails = (UserDetails) auth.getPrincipal();
                    User user = userDAO.findByLoginName(userDetails.getUsername());
                    user.getProfiles().size();
                            return orderAuthorizationDAO
                                    .userOrItsProfilesHaveAnyAuthorization(user);
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }

            }
        });
        if (userOrItsProfilesHaveAnyAuthorization) {
            return PLANNING_URL;
        }

        return SETTINGS_URL;
    }

    private boolean isUserInRole(Authentication auth, String role) {
        if ((auth == null) || (auth.getPrincipal() == null)
                || (auth.getAuthorities() == null)) {
            return false;
        }

        for (int i = 0; i < auth.getAuthorities().length; i++) {
            if (role.equals(auth.getAuthorities()[i].getAuthority())) {
                return true;
            }
        }

        return false;
    }
}
