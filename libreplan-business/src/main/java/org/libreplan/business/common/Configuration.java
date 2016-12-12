/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2010-2011 Wireless Galicia, S.L.
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

package org.libreplan.business.common;

import org.apache.commons.lang3.BooleanUtils;


/**
 * This is a singleton that contains the compilation options passed from Maven.
 *
 * Currently we have four options:
 * <ul>
 *     <li>Enable/Disable the warning changing default password</li>
 *     <li>
 *         Enable/Disable default users
 *         (such as wsreader, wswriter, wssubcontracting, manager, hresources, outsourcing and reports)
 *     </li>
 *     <li>Enable/Disable E-mail sending functionality</li>
 *     <li>Enable/Disable "Delete all projects" button</li>
 * </ul>
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */

public class Configuration {

    private static final Configuration singleton = new Configuration();

    private Boolean defaultPasswordsControl;

    private boolean exampleUsersDisabled;

    private boolean emailSendingEnabled;

    private boolean deleteAllProjectsButtonDisabled;

    private Configuration() {
    }

    public static Configuration getInstance() {
        return singleton;
    }

    /**
     * It returns the current state of the default passwords control in order to show or not warnings.
     */
    public static Boolean isDefaultPasswordsControl() {
        return singleton.getDefaultPasswordsControl() != null ? singleton.getDefaultPasswordsControl() : true;
    }

    public Boolean getDefaultPasswordsControl() {
        return defaultPasswordsControl;
    }

    public void setDefaultPasswordsControl(Boolean defaultPasswordsControl) {
        this.defaultPasswordsControl = defaultPasswordsControl;
    }

    public static boolean isExampleUsersDisabled() {
        return BooleanUtils.isNotFalse(singleton.getExampleUsersDisabled());
    }

    public boolean getExampleUsersDisabled() {
        return exampleUsersDisabled;
    }

    public void setExampleUsersDisabled(boolean exampleUsersDisabled) {
        this.exampleUsersDisabled = exampleUsersDisabled;
    }

    public static boolean isEmailSendingEnabled() {
        return BooleanUtils.isNotFalse(singleton.getEmailSendingEnabled());
    }

    public boolean getEmailSendingEnabled() {
        return emailSendingEnabled;
    }

    public void setEmailSendingEnabled(boolean emailSendingEnabled) {
        this.emailSendingEnabled = emailSendingEnabled;
    }

    public boolean isDeleteAllProjectsButtonDisabled() {
        return deleteAllProjectsButtonDisabled;
    }

    public void setDeleteAllProjectsButtonDisabled(boolean deleteAllProjectsButtonDisabled) {
        this.deleteAllProjectsButtonDisabled = deleteAllProjectsButtonDisabled;
    }
}
