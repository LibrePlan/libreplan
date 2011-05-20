/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 ComtecSF S.L.
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
package org.navalplanner.web.users.services;

import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.security.AuthenticationException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.providers.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;

/**
 * An extending from AbstractUserDetailsAuthenticationProvider class which is
 * used to implement the authentication against LDAP.
 *
 * In the future this provider will implement all the process explained in
 * <https
 * ://wiki.navalplan.org/twiki/bin/view/NavalPlan/AnA04S06LdapAuthentication>
 *
 * At this time it authenticates user against LDAP and then searches it in BD to
 * use the BD user in application.
 *
 * @author Ignacio Diaz <ignacio.diaz@comtecsf.es>
 * @author Cristina Alvarino <cristina.alvarino@comtecsf.es>
 *
 */
public class LDAPCustomAuthenticationProvider extends
        AbstractUserDetailsAuthenticationProvider {

    // Template to search in LDAP
    private LdapTemplate ldapTemplate;

    // Place in LDAP where username is
    private String userId;

    private UserDetailsService userDetailsService;

    @Override
    protected void additionalAuthenticationChecks(UserDetails arg0,
            UsernamePasswordAuthenticationToken arg1)
            throws AuthenticationException {
        // No needed at this time
    }

    @Override
    public UserDetails retrieveUser(String username,
            UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {

        // Tests if the user is in LDAP, if not, throws a new
        // AuthenticationException
        if (!(ldapTemplate.authenticate(DistinguishedName.EMPTY_PATH,
                new EqualsFilter(userId, username).toString(), authentication
                        .getCredentials().toString()))) {

            throw new BadCredentialsException("User is not in LDAP.");

        } else {
            // Gets and returns user from DB once authenticated against LDAP
            return getUserDetailsService().loadUserByUsername(username);
        }
    }

    // Getters and setters
    public LdapTemplate getLdapTemplate() {
        return ldapTemplate;
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

}
