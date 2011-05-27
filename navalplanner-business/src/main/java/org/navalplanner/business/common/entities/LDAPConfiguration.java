/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 Comtecsf
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

import org.navalplanner.business.common.BaseEntity;

/**
 *
 * This entity will be used to store the LDAP connection properties for
 * authentication
 *
 * @author Cristina Alvarino <cristina.alvarino@comtecsf.es>
 *
 */
public class LDAPConfiguration extends BaseEntity {

    public static LDAPConfiguration create() {
        return create(new LDAPConfiguration());
    }

    // Place in LDAP where username is
    private String ldapUserId;

    private String ldapHost;

    private String ldapPort;

    private String ldapBase;

    private String ldapUserDn;

    private String ldapPassword;

    // TODO Almacena si se guardarán los passwords del ldap en la bd
    private Boolean ldapSavePasswordsDB;

    // TODO Guarda si se va a usar la autenticación con el ldap o no(la de
    // navalplan)
    private Boolean ldapAuthEnabled;

    public String getLdapUserId() {
        return ldapUserId;
    }

    public void setLdapUserId(String ldapUserId) {
        this.ldapUserId = ldapUserId;
    }

    public String getLdapHost() {
        return ldapHost;
    }

    public void setLdapHost(String ldapHost) {
        this.ldapHost = ldapHost;
    }

    public String getLdapPort() {
        return ldapPort;
    }

    public void setLdapPort(String ldapPort) {
        this.ldapPort = ldapPort;
    }

    public String getLdapBase() {
        return ldapBase;
    }

    public void setLdapBase(String ldapBase) {
        this.ldapBase = ldapBase;
    }

    public String getLdapUserDn() {
        return ldapUserDn;
    }

    public void setLdapUserDn(String ldapUserDn) {
        this.ldapUserDn = ldapUserDn;
    }

    public String getLdapPassword() {
        return ldapPassword;
    }

    public void setLdapPassword(String ldapPassword) {
        this.ldapPassword = ldapPassword;
    }

    public Boolean getLdapSavePasswordsDB() {
        return ldapSavePasswordsDB;
    }

    public void setLdapSavePasswordsDB(Boolean ldapSavePasswordsDB) {
        this.ldapSavePasswordsDB = ldapSavePasswordsDB;
    }

    public Boolean getLdapAuthEnabled() {
        return ldapAuthEnabled;
    }

    public void setLdapAuthEnabled(Boolean ldapAuthEnabled) {
        this.ldapAuthEnabled = ldapAuthEnabled;
    }

}
