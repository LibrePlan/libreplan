/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.web.test.users.services;

import static org.junit.Assert.assertEquals;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.scenarios.bootstrap.IScenariosBootstrap;
import org.libreplan.business.users.daos.IProfileDAO;
import org.libreplan.business.users.entities.Profile;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.users.bootstrap.IUsersBootstrapInDB;
import org.libreplan.web.users.bootstrap.PredefinedUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for <code>DBUserDetailsService</code>.
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class DBUserDetailsServiceTest {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private IUsersBootstrapInDB usersBootstrap;

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IProfileDAO profileDAO;

    @Before
    public void loadScenariosBootsrap() {
        /*
         * the required data is loaded in another transaction because if it's
         * loaded on the same transaction the added scenario could not be
         * retrieved from PredefinedScenario. This happened when executing all
         * tests. If you execute this test in isolation this problem doesn't
         * happen
         */
        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                scenariosBootstrap.loadRequiredData();
                return null;
            }
        });
    }

    @Test
    public void testLoadUserByUsername() {
        usersBootstrap.loadRequiredData();

        for (PredefinedUsers u : PredefinedUsers.values()) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(u
                    .getLoginName());
            assertEquals(u.getLoginName(), userDetails.getUsername());

            assertEquals(getUserRoles(u), getUserRoles(userDetails));

        }

    }

    private Object getUserRoles(PredefinedUsers u) {
        Set<UserRole> userRoles = new HashSet<UserRole>();

        userRoles.addAll(u.getInitialRoles());

        Set<Profile> initialProfiles = u.getInitialProfiles();
        for (Profile profile : initialProfiles) {
            userRoles.addAll(profile.getRoles());
        }

        return userRoles;
    }

    private Set<UserRole> getUserRoles(UserDetails userDetails) {

        Set<UserRole> userRoles = new HashSet<UserRole>();

        for (GrantedAuthority a : userDetails.getAuthorities()) {
            userRoles.add(UserRole.valueOf(a.getAuthority()));
        }

        return userRoles;

    }

}
