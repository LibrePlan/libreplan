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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.entities.ConfigurationRolesLDAP;
import org.navalplanner.business.common.entities.LDAPConfiguration;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
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

    private static final String USER_ID_SUBSTITUTION = "[USER_ID]";

    private static final Log LOG = LogFactory
            .getLog(LDAPCustomAuthenticationProvider.class);

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

        User user = getUserFromDB(usernameInserted);

        // If user != null then exists in NavalPlan
        if (null != user && user.isNavalplanUser()) {
            // is a NavalPlan user, then we must authenticate against DB
            if (authenticateInDatabase(authentication, username, user)) {
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
            configuration = loadLDAPConfiguration();

            if (configuration.getLdapAuthEnabled()) {
                // Sets the new context to ldapTemplate
                ldapTemplate.setContextSource(loadLDAPContext());

                try {
                    // Test authentication for user against LDAP
                    if (authenticateAgainstLDAP(usernameInserted,
                            authentication.getCredentials().toString())) {
                        // Authentication against LDAP was ok
                        if (null == user) {
                            // user does not exist in NavalPlan must be imported
                            final User userNavalplan = User.create();
                            userNavalplan.setLoginName(username);
                            // we must check if it is needed to save LDAP
                            // passwords in DB
                            encodedPassword = null;
                            if (configuration.isLdapSavePasswordsDB())
                                encodedPassword = passwordEncoderService
                                        .encodePassword(authentication
                                                .getCredentials().toString(),
                                                username);
                            userNavalplan.setPassword(encodedPassword);
                            userNavalplan.setNavalplanUser(false);
                            userNavalplan.setDisabled(false);
                            if (configuration.getLdapSaveRolesDB()) {
                                List<String> roles = getMatchedRoles(
                                        configuration, ldapTemplate, username);
                                for (String role : roles) {
                                    userNavalplan.addRole(UserRole.valueOf(
                                            UserRole.class, role));
                                }
                            }
                            saveUserOnTransaction(userNavalplan);
                        } else {
                            // user exists in NavalPlan
                            // We must match the LDAPRoles with
                            // LibrePlanRoles
                            // The user must have the roles from LDAP
                            if (configuration.getLdapSaveRolesDB()) {
                                List<String> roles = getMatchedRoles(
                                        configuration, ldapTemplate, username);
                                for (String role : roles) {
                                    if (!user.getRoles().contains(
                                            UserRole.valueOf(UserRole.class,
                                                    role))) {
                                        user.addRole(UserRole.valueOf(
                                                UserRole.class, role));
                                    }
                                }
                                Set<UserRole> oldRoles = new HashSet<UserRole>();
                                oldRoles.addAll(user.getRoles());
                                for (UserRole role : oldRoles) {
                                    if (!roles.contains(role.name())) {
                                        user.removeRole(role);
                                    }
                                }
                            }
                            if (configuration.isLdapSavePasswordsDB()) {
                                // We must test if user had password in
                                // database, because the configuration
                                // of importing passwords could be changed after
                                // the import of the user so the password could
                                // be null in database.

                                if (null == user.getPassword()
                                        || !(user.getPassword()
                                                .equals(encodedPassword))) {
                                    user.setPassword(encodedPassword);
                                }
                            }
                            final User userNavalplan = user;
                            saveUserOnTransaction(userNavalplan);
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
                } catch (Exception e) {
                    // This exception captures when LDAP authentication is not
                    // possible
                    // We must in this case try to authenticate against DB.
                    LOG.info("LDAP not reachable. Trying to authenticate against database.");
                    if (authenticateInDatabase(authentication, username, user)) {
                        // user credentials are ok
                        return getUserDetailsService().loadUserByUsername(
                                username);
                    } else {
                        throw new BadCredentialsException(e.getMessage());
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

    private LDAPConfiguration loadLDAPConfiguration() {
        return transactionService
                .runOnReadOnlyTransaction(new IOnTransaction<LDAPConfiguration>() {

                    @Override
                    public LDAPConfiguration execute() {
                        return configurationDAO.getConfiguration()
                                .getLdapConfiguration();
                    }
                });
    }

    private User getUserFromDB(String username) {
        final String usernameInserted = username;
        return transactionService
                .runOnReadOnlyTransaction(new IOnTransaction<User>() {

                    @Override
                    public User execute() {
                        try {
                            return userDAO.findByLoginName(usernameInserted);
                        } catch (InstanceNotFoundException e) {
                            LOG.info("User " + usernameInserted
                                    + " not found in database.");
                            return null;
                        }
                    }
                });

    }

    private LDAPCustomContextSource loadLDAPContext() {
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
            LOG.error("There is a problem in LDAP connection: ", e);
        }
        return context;
    }

    private boolean authenticateAgainstLDAP(String username, String password) {
        return ldapTemplate.authenticate(DistinguishedName.EMPTY_PATH,
                new EqualsFilter(configuration.getLdapUserId(), username)
                        .toString(), password);
    }

    private void saveUserOnTransaction(User user) {
        final User userLibrePlan = user;
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                userDAO.save(userLibrePlan);
                return null;
            }
        });
    }

    private boolean authenticateInDatabase(Authentication authentication,
            String username, User user) {
        String encodedPassword = passwordEncoderService.encodePassword(
                authentication.getCredentials().toString(), username);
        return (null != user && null != user.getPassword() && encodedPassword
                .equals(user
                .getPassword()));
    }

    private List<String> getMatchedRoles(LDAPConfiguration configuration,
            LdapTemplate ldapTemplate, String username) {

        String queryRoles = configuration.getLdapSearchQuery().replace(
                USER_ID_SUBSTITUTION, username);
        final LDAPConfiguration ldapConfig = configuration;
        String groupsPath = configuration.getLdapGroupPath();
        String roleProperty = configuration.getLdapRoleProperty();
        List<ConfigurationRolesLDAP> rolesLdap = configuration
                .getConfigurationRolesLdap();

        List<String> rolesReturn = new ArrayList<String>();

        try {

            if (null == groupsPath || groupsPath.isEmpty()) {
                // The LDAP has a node strategy for groups,
                // we must check the roleProperty in user node.
                for (ConfigurationRolesLDAP roleLDAP : rolesLdap) {
                    // We must make a search for each role-matching in nodes
                    List<String> rolesToCheck = Arrays.asList(StringUtils
                            .split(roleLDAP.getRoleLdap(), ";"));
                    List resultsSearch = new ArrayList();
                    for (String role : rolesToCheck) {
                        resultsSearch.addAll(ldapTemplate.search(
                                DistinguishedName.EMPTY_PATH, new EqualsFilter(
                                        roleProperty, roleLDAP.getRoleLdap())
                                        .toString(), new AttributesMapper() {

                                    @Override
                                    public Object mapFromAttributes(
                                            Attributes attributes)
                                            throws NamingException {
                                        return attributes.get(ldapConfig
                                                .getLdapUserId());
                                    }
                                }));
                    }
                    for (Object resultsIter : resultsSearch) {
                        Attribute atrib = (Attribute) resultsIter;
                        if (atrib.contains(queryRoles)) {
                            rolesReturn.add(roleLDAP.getRoleLibreplan());
                        }
                    }
                }
            } else {
                // The LDAP has a branch strategy for groups
                // we must check if the user is in one of the groups.

                for (ConfigurationRolesLDAP roleLdap : rolesLdap) {
                    // We must make a search for each role matching
                    List<String> rolesToCheck = Arrays.asList(StringUtils
                            .split(roleLdap.getRoleLdap(), ";"));
                    for (String role : rolesToCheck) {
                        DirContextAdapter adapter = (DirContextAdapter) ldapTemplate
                                .lookup(role + "," + groupsPath);
                        if (adapter.attributeExists(roleProperty)) {
                            Attributes atrs = adapter.getAttributes();
                            if (atrs.get(roleProperty).contains(queryRoles)) {
                                rolesReturn.add(roleLdap.getRoleLibreplan());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(
                    "Configuration of LDAP role-matching is wrong. Please check it.",
                    e);
            rolesReturn.clear();
        }
        return rolesReturn;
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
