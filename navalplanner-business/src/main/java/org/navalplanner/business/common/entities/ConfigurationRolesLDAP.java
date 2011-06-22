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

package org.navalplanner.business.common.entities;

import org.hibernate.validator.NotEmpty;

/**
 * A class which is used to store the configuration of the matching between the
 * LDAP roles and LibrePlan roles this will be used in LDAP configuration tab of
 * the Configuration screen.
 *
 * This class is a component of LdapConfiguration class
 *
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 * @author Cristina Alvarino Perez <cristina.alvarino@comtecsf.es>
 */
public class ConfigurationRolesLDAP {

    private String roleLdap;

    private String roleLibreplan;

    @NotEmpty(message = "role ldap not specified")
    public String getRoleLdap() {
        return roleLdap;
    }

    public void setRoleLdap(String roleLdap) {
        this.roleLdap = roleLdap;
    }

    @NotEmpty(message = "role libreplan not specified")
    public String getRoleLibreplan() {
        return roleLibreplan;
    }

    public void setRoleLibreplan(String roleLibreplan) {
        this.roleLibreplan = roleLibreplan;
    }

}
