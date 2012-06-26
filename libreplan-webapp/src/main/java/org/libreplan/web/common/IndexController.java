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

package org.libreplan.web.common;

import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.security.SecurityUtils;
import org.libreplan.web.users.services.CustomTargetUrlResolver;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;

/**
 * Controller to redirect user to proper initial page depending or roles.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@SuppressWarnings("serial")
public class IndexController extends GenericForwardComposer {

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        String url = getInitialPageURL();
        Executions.sendRedirect(url);
    }

    private String getInitialPageURL() {
        if (SecurityUtils.isUserInRole(UserRole.ROLE_SUPERUSER)) {
            return CustomTargetUrlResolver.PLANNING_URL;
        }

        if (SecurityUtils.isUserInRole(UserRole.ROLE_BOUND_USER)) {
            return CustomTargetUrlResolver.USER_DASHBOARD_URL;
        }

        if (SecurityUtils.isSuperuserOrRolePlanningOrHasAnyAuthorization()) {
            return CustomTargetUrlResolver.PLANNING_URL;
        }

        return CustomTargetUrlResolver.SETTINGS_URL;
    }

}
