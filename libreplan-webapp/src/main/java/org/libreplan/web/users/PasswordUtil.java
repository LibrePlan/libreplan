/*
 * This file is part of LibrePlan
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

package org.libreplan.web.users;

import org.libreplan.business.common.Registry;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.users.bootstrap.PredefinedUsers;
import org.zkoss.zk.ui.util.Clients;

/**
 * A class which is used to encapsulate some common behaviour of passwords.
 *
 * @author Cristina Alvarino Perez <cristina.alvarino@comtecsf.es>
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 */
public class PasswordUtil {

    public static void checkIfChangeDefaultPasswd(User user,
            String clearPassword) {
        if (user.getLoginName().equalsIgnoreCase(
                PredefinedUsers.ADMIN.getLoginName())) {
            checkIfChangeDefaultPasswd(PredefinedUsers.ADMIN, clearPassword);
            return;
        }
        if (user.getLoginName().equalsIgnoreCase(
                PredefinedUsers.WSREADER.getLoginName())) {
            checkIfChangeDefaultPasswd(PredefinedUsers.WSREADER, clearPassword);
            return;
        }
        if (user.getLoginName().equalsIgnoreCase(
                PredefinedUsers.WSWRITER.getLoginName())) {
            checkIfChangeDefaultPasswd(PredefinedUsers.WSWRITER, clearPassword);
            return;
        }
        if (user.getLoginName().equalsIgnoreCase(
                PredefinedUsers.WSSUBCONTRACTING.getLoginName())) {
            checkIfChangeDefaultPasswd(PredefinedUsers.WSSUBCONTRACTING,
                    clearPassword);
            return;
        }
        if (user.getLoginName().equalsIgnoreCase(
                PredefinedUsers.MANAGER.getLoginName())) {
            checkIfChangeDefaultPasswd(PredefinedUsers.MANAGER, clearPassword);
            return;
        }
        if (user.getLoginName().equalsIgnoreCase(
                PredefinedUsers.HRESOURCES.getLoginName())) {
            checkIfChangeDefaultPasswd(PredefinedUsers.HRESOURCES, clearPassword);
            return;
        }
        if (user.getLoginName().equalsIgnoreCase(
                PredefinedUsers.OUTSOURCING.getLoginName())) {
            checkIfChangeDefaultPasswd(PredefinedUsers.OUTSOURCING, clearPassword);
            return;
        }
        if (user.getLoginName().equalsIgnoreCase(
                PredefinedUsers.REPORTS.getLoginName())) {
            checkIfChangeDefaultPasswd(PredefinedUsers.REPORTS, clearPassword);
            return;
        }
    }

    private static void checkIfChangeDefaultPasswd(PredefinedUsers user,
            String clearPassword) {
        boolean changedPasswd = true;
        if (clearPassword.isEmpty()
                || clearPassword.equals(user.getClearPassword())) {
            changedPasswd = false;
        }
        // save the field changedDefaultAdminPassword in configuration.
        Registry.getConfigurationDAO().saveChangedDefaultPassword(
                user.getLoginName(), changedPasswd);
    }

    /**
     * It calls a JavaScript method called
     * <b>showOrHideDefaultPasswordWarnings</b> defined in
     * "/libreplan-webapp/js/defaultPasswordWarnings.js" to show or hide the
     * default password warnings if the user has changed the password or has
     * been disabled
     */
    public static void showOrHideDefaultPasswordWarnings() {
        boolean adminNotDefaultPassword = PredefinedUsers.ADMIN
                .hasChangedDefaultPasswordOrDisabled();
        boolean wsreaderNotDefaultPassword = PredefinedUsers.WSREADER
                .hasChangedDefaultPasswordOrDisabled();
        boolean wswriterNotDefaultPassword = PredefinedUsers.WSWRITER
                .hasChangedDefaultPasswordOrDisabled();
        boolean wssubcontractingNotDefaultPassword = PredefinedUsers.WSSUBCONTRACTING
                .hasChangedDefaultPasswordOrDisabled();
        boolean managerNotDefaultPassword = PredefinedUsers.MANAGER
                .hasChangedDefaultPasswordOrDisabled();
        boolean hresourcesNotDefaultPassword = PredefinedUsers.HRESOURCES
                .hasChangedDefaultPasswordOrDisabled();
        boolean outsourcingNotDefaultPassword = PredefinedUsers.OUTSOURCING
                .hasChangedDefaultPasswordOrDisabled();
        boolean reportsNotDefaultPassword = PredefinedUsers.REPORTS
                .hasChangedDefaultPasswordOrDisabled();

        Clients.evalJavaScript("showOrHideDefaultPasswordWarnings("
                + adminNotDefaultPassword + ", "
                + wsreaderNotDefaultPassword + ", "
                + wswriterNotDefaultPassword + ", "
                + wssubcontractingNotDefaultPassword + ", "
                + managerNotDefaultPassword + ", "
                + hresourcesNotDefaultPassword + ", "
                + outsourcingNotDefaultPassword + ", "
                + reportsNotDefaultPassword + ");");
    }

}
