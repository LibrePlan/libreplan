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

import org.apache.commons.lang.BooleanUtils;


/**
 * This is a singleton that contains the compilation options passed from Maven.
 *
 * Currently we have two options:
 * <ul>
 * <li>Enable/Disable the warning changing default password</li>
 * <li>Enable/Disable default users (such as user, wsreader and wswriter)</li>
 * </ul>
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class Configuration {

    private static final Configuration singleton = new Configuration();

    private Boolean defaultPasswordsControl;

    private Boolean exampleUsersDisabled;

    private Configuration() {
    }

    public static Configuration getInstance() {
        return singleton;
    }

    /**
     * It returns the current state of the default passwords control in order to
     * show or not warnings.
     */
    public static Boolean isDefaultPasswordsControl() {
        return singleton.getDefaultPasswordsControl() != null ? singleton
                .getDefaultPasswordsControl() : true;
    }

    public void setDefaultPasswordsControl(Boolean defaultPasswordsControl) {
        this.defaultPasswordsControl = defaultPasswordsControl;
    }

    public Boolean getDefaultPasswordsControl() {
        return defaultPasswordsControl;
    }

    public void setExampleUsersDisabled(Boolean exampleUsersDisabled) {
        this.exampleUsersDisabled = exampleUsersDisabled;
    }

    public Boolean getExampleUsersDisabled() {
        return exampleUsersDisabled;
    }

    /**
     * Returns the value of example users disabled compilation option
     */
    public static boolean isExampleUsersDisabled() {
        return BooleanUtils.isNotFalse(singleton.getExampleUsersDisabled());
    }

}
