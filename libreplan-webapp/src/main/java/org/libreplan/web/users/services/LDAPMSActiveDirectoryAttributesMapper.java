/*
 * This file is part of LibrePlan
 * 
 * Copyright (C) YEAR Copyright Holder (OOPS!)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.libreplan.web.users.services;

import org.libreplan.business.users.entities.User;
import org.springframework.ldap.core.AttributesMapper;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

/**
 * @author William Lee <eeleer@gmail.com>
 *
 * LDAP attributes mapping for Microsoft active directory.
 */
public class LDAPMSActiveDirectoryAttributesMapper implements AttributesMapper {

    public static final String ATTR_MAP_NAME = "Microsoft Active Directory";

    public Object mapFromAttributes(Attributes attributes) throws NamingException {
        User ldapUsr = User.create();
        ldapUsr.setEmail((String) attributes.get("userPrincipalName").get());
        ldapUsr.setLastName((String) attributes.get("sn").get());
        ldapUsr.setFirstName((String) attributes.get("givenName").get());
        ldapUsr.setLoginName((String) attributes.get("sAMAccountName").get());
        return ldapUsr;
    }

}
