/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 ComtecSF, S.L.
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
package org.navalplanner.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.web.security.SecurityUtils;
import org.navalplanner.web.users.services.CustomUser;

/**
 * Class used to implement some utilities about users which could be useful in
 * different web layer classes
 *
 * @author Cristina Alavarino Perez <cristina.alvarino@comtecsf.es>
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 */
public class UserUtil {

    private UserUtil() {
    }

    private static final Log LOG = LogFactory.getLog(UserUtil.class);

    public static User getUserFromSession() {
        CustomUser loggedUser = SecurityUtils.getLoggedUser();
        if (loggedUser != null) {
            String username = loggedUser.getUsername();
            try {
                return Registry.getUserDAO().findByLoginName(username);
            } catch (InstanceNotFoundException e) {
                LOG.info("User " + username + " not found in database.");
                return null;
            }
        }
        return null;
    }

}
