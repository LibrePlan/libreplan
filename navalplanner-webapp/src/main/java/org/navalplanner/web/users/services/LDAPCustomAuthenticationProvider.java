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

import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.entities.LDAPConfiguration;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.CommunicationException;
import org.springframework.ldap.ServiceUnavailableException;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.providers.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

/**
 * An extending from AbstractUserDetailsAuthenticationProvider class which is
 * used to implement the authentication against LDAP.
 *
 * This provider implements the process explained in <https
 * ://wiki.navalplan.org/twiki/bin/view/NavalPlan/AnA04S06LdapAuthentication>
 *
 * At this time it authenticates user against LDAP and then searches it in BD to
 * use the BD user in application.
 *
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 * @author Cristina Alvarino Perez <cristina.alvarino@comtecsf.es>
 *
 */
public class LDAPCustomAuthenticationProvider extends
        AbstractUserDetailsAuthenticationProvider implements
        AuthenticationProvider {

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IUserDAO userDAO;

    private LDAPConfiguration configuration;

    // Template to search in LDAP
    private LdapTemplate ldapTemplate;

    private UserDetailsService userDetailsService;

    private DBPasswordEncoderService passwordEncoderService;

    private static final String COLON = ":";

    @Override
    protected void additionalAuthenticationChecks(UserDetails arg0,
            UsernamePasswordAuthenticationToken arg1)
            throws AuthenticationException {
        // No needed at this time
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Transactional(readOnly = true)
    @Override
    public UserDetails retrieveUser(String username,
            UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {

        final String usernameInserted = username;
        String encodedPassword = passwordEncoderService.encodePassword(
                authentication.getCredentials().toString(), username);
        User user = null;

        // Gets user from DB if exists
        user = (User) transactionService
                .runOnReadOnlyTransaction(new IOnTransaction() {

                    @Override
                    public Object execute() {
                        try {
                            return userDAO.findByLoginName(usernameInserted);
                        } catch (InstanceNotFoundException e) {
                            return null;
                        }
                    }
                });

        // If user != null then exists in NavalPlan
        if (null != user && user.isNavalplanUser()) {
            // is a NavalPlan user, then we must authenticate against DB
            if (encodedPassword.equals(user.getPassword())) {
                // user credentials are ok
                return getUserDetailsService().loadUserByUsername(username);
            } else {
                throw new BadCredentialsException(
                        "Credentials are not the same as in database.");
            }

        } else {
            // is a LDAP or null user, then we must authenticate against LDAP
            // if LDAP is enabled
            // Gets the LDAPConfiguration properties
            configuration = (LDAPConfiguration) transactionService
                    .runOnReadOnlyTransaction(new IOnTransaction() {

                        @Override
                        public Object execute() {
                            return configurationDAO.getConfiguration()
                                    .getLdapConfiguration();
                        }
                    });

            if (configuration.getLdapAuthEnabled()) {

                // Establishes the context for LDAP connection.
                LDAPCustomContextSource context = (LDAPCustomContextSource) ldapTemplate
                        .getContextSource();
                context.setUrl(configuration.getLdapHost() + COLON
                        + configuration.getLdapPort());
                context.setBase(configuration.getLdapBase());
                context.setUserDn(configuration.getLdapUserDn());
                context.setPassword(configuration.getLdapPassword());
                try {
                    context.afterPropertiesSet();
                } catch (Exception e) {
                    // This exception will be never reached if the LDAP
                    // properties are
                    // well-formed.
                    e.printStackTrace();
                }
                // Sets the new context to ldapTemplate
                ldapTemplate.setContextSource(context);
                try {
                    // Test authentication for user against LDAP
                    if (ldapTemplate.authenticate(DistinguishedName.EMPTY_PATH,
                            new EqualsFilter(configuration.getLdapUserId(),
                                    username).toString(), authentication
                                    .getCredentials().toString())) {
                        // Authentication against LDAP was ok
                        if (null == user) {
                            // user does not exist in NavalPlan must be imported
                            final User userNavalplan = User.create();
                            userNavalplan.setLoginName(username);
                            // we must check if it is needed to save LDAP
                            // passwords
                            // in
                            // DB
                            encodedPassword = null;
                            if (configuration.isLdapSavePasswordsDB())
                                encodedPassword = passwordEncoderService
                                        .encodePassword(authentication
                                                .getCredentials().toString(),
                                                username);
                            userNavalplan.setPassword(encodedPassword);
                            userNavalplan.setNavalplanUser(false);
                            userNavalplan.setDisabled(false);
                            userNavalplan.addRole(UserRole.ROLE_ADMINISTRATION);
                            transactionService
                                    .runOnTransaction(new IOnTransaction() {
                                        @Override
                                        public Object execute() {
                                            userDAO.save(userNavalplan);
                                            return true;
                                        }
                                    });
                        } else {
                            // user exists in NavalPlan
                            if (configuration.isLdapSavePasswordsDB()) {
                                // We must test if user had password in
                                // database,
                                // because the configuration
                                // of importing passwords could be changed after
                                // the
                                // import of the user
                                // so the password could be null in database.
                                if (null == user.getPassword()
                                        || !(user.getPassword()
                                                .equals(encodedPassword))) {
                                    user.setPassword(encodedPassword);
                                    final User userNavalplan = user;
                                    transactionService
                                            .runOnTransaction(new IOnTransaction() {
                                                @Override
                                                public Object execute() {
                                                    userDAO.save(userNavalplan);
                                                    return true;
                                                }
                                            });
                                }
                            }
                        }
                        // Gets and returns user from DB once authenticated
                        // against
                        // LDAP
                        return getUserDetailsService().loadUserByUsername(
                                username);
                    } else {
                        throw new BadCredentialsException(
                                "User is not in LDAP.");
                    }
                } catch (CommunicationException ce) {
                    // This exception captures when LDAP is not reachable.
                    // We must in this case try to authenticate against DB.
                    // LDAP is not enabled we must check if the LDAP user is in
                    // DB
                    if (authenticateInDatabase(authentication, username, user)) {
                        // user credentials are ok
                        return getUserDetailsService().loadUserByUsername(
                                username);
                    } else {
                        throw new BadCredentialsException(
                                "Authenticating LDAP user against LDAP. Maybe LDAP is out of service. "
                                        + "Credentials are not the same as in database.");
                    }
                } catch (UncategorizedLdapException ule) {
                    // This exception captures when LDAP URL is malformed
                    // this should never occur, but we check it to try
                    // database authentication.
                    if (authenticateInDatabase(authentication, username, user)) {
                        // user credentials are ok
                        return getUserDetailsService().loadUserByUsername(
                                username);
                    } else {
                        throw new BadCredentialsException(
                                "LDAP url is malformed. Trying to authenticate against DB. "
                                        + "Credentials are not the same as in database");
                    }
                } catch (ServiceUnavailableException sua) {
                    // This exception captures when LDAP is not available
                    // We try database authentication.
                    if (authenticateInDatabase(authentication, username, user)) {
                        // user credentials are ok
                        return getUserDetailsService().loadUserByUsername(
                                username);
                    } else {
                        throw new BadCredentialsException(
                                "LDAP is out of service. Trying to authenticate against DB. "
                                        + "Credentials are not the same as in database");
                    }
                }
            } else {
                // LDAP is not enabled we must check if the LDAP user is in DB
                if (authenticateInDatabase(authentication, username, user)) {
                    // user credentials are ok
                    return getUserDetailsService().loadUserByUsername(username);
                } else {
                    throw new BadCredentialsException(
                            "Authenticating LDAP user against LDAP was not possible because LDAPAuthentication is not enabled. "
                                    + "Credentials are not the same as in database.");
                }
            }
        }
    }

    private boolean authenticateInDatabase(Authentication authentication,
            String username, User user) {
        String encodedPassword = passwordEncoderService.encodePassword(
                authentication.getCredentials().toString(), username);
        return (null != user && null != user.getPassword() && encodedPassword
                .equals(user
                .getPassword()));
    }

    public DBPasswordEncoderService getPasswordEncoderService() {
        return passwordEncoderService;
    }

    public void setPasswordEncoderService(
            DBPasswordEncoderService passwordEncoderService) {
        this.passwordEncoderService = passwordEncoderService;
    }

    // Getters and setters
    public LdapTemplate getLdapTemplate() {
        return ldapTemplate;
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

}
