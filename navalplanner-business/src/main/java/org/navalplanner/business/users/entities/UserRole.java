/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.users.entities;

import org.navalplanner.business.i18n.I18nHelper;

/**
 * Available user roles.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public enum UserRole {

    ROLE_ADMINISTRATION(_("Administration")),
    ROLE_WS_READER(_("Web service reader")),
    ROLE_WS_WRITER(_("Web service writer")),
    ROLE_READ_ALL_ORDERS(_("All orders read allowed")),
    ROLE_EDIT_ALL_ORDERS(_("All orders edition allowed")),
    ROLE_CREATE_ORDER(_("Order creation allowed"));

    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    private final String displayName;

    private UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return I18nHelper._(displayName);
    }

}
