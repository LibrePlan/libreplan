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

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
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

    private final static String USER_DASHBOARD_URL = "/myaccount/userDashboard.zul";

    @Autowired
    private IUserDAO userDAO;

    @Override
    public String determineTargetUrl(SavedRequest savedRequest,
            HttpServletRequest currentRequest, Authentication auth) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        try {
            User user = userDAO.findByLoginName(userDetails.getUsername());
            if (user.isBound()) {
                return USER_DASHBOARD_URL;
            }
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }

        return super.determineTargetUrl(savedRequest, currentRequest, auth);
    }
}
