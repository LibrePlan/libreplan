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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.users.daos.IOrderAuthorizationDAO;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * Determines the URL for authenticated users depending on if user is bound or
 * not to any resource.<br />
 *
 * If the user is bound to a resource then the target URL will be the user
 * dashboard.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class CustomTargetUrlResolver extends
        SavedRequestAwareAuthenticationSuccessHandler {

    public final static String USER_DASHBOARD_URL = "/myaccount/userDashboard.zul";

    public static final String PLANNING_URL = "/planner/index.zul";

    public static final String SETTINGS_URL = "/myaccount/settings.zul";

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IOrderAuthorizationDAO orderAuthorizationDAO;

    @Autowired
    private IAdHocTransactionService transactionServiceDAO;

    private ThreadLocal<Authentication> currentAuth = new ThreadLocal<Authentication>();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response, Authentication auth)
            throws ServletException, IOException {

        try {
            currentAuth.set(auth);
            super.onAuthenticationSuccess(request, response, auth);
        } finally {
            currentAuth.remove();
        }
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request,
            HttpServletResponse response) {

        String targetURL = super.determineTargetUrl(request, response);
        // if using default URL, we may want to use one based on the current
        // user
        if (targetURL.equals(getDefaultTargetUrl())) {
            return calculatePreferedForUser(currentAuth.get());
        }
        return targetURL;
    }

    private String calculatePreferedForUser(final Authentication auth) {
        if (isUserInSomeRole(auth, roles(UserRole.ROLE_BOUND_USER))) {
            return USER_DASHBOARD_URL;
        }

        if (isUserInSomeRole(auth,
                roles(UserRole.ROLE_SUPERUSER, UserRole.ROLE_PLANNING))) {
            return getDefaultTargetUrl();
        }

        if (!hasAnyAuthorization(auth)) {
            return SETTINGS_URL;
        }
        return getDefaultTargetUrl();
    }

    private boolean hasAnyAuthorization(final Authentication auth) {
        return transactionServiceDAO
                .runOnReadOnlyTransaction(new IOnTransaction<Boolean>() {
            @Override
            public Boolean execute() {
                try {
                    UserDetails userDetails = (UserDetails) auth.getPrincipal();
                    User user = userDAO.findByLoginName(userDetails.getUsername());
                    user.getProfiles().size();
                    return orderAuthorizationDAO.userOrItsProfilesHaveAnyAuthorization(user);
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static Set<String> roles(UserRole... roles) {
        Set<String> result = new HashSet<String>();
        for (UserRole each : roles) {
            result.add(each.name());
        }
        return result;
    }

    private boolean isUserInSomeRole(Authentication auth, Set<String> roles) {
        if ((auth == null) || (auth.getPrincipal() == null)
                || (auth.getAuthorities() == null)) {
            return false;
        }

        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (roles.contains(authority.getAuthority())) {
                return true;
            }

        }
        return false;
    }
}
