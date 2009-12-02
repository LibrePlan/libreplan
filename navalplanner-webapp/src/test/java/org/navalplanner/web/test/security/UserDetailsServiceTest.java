/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.test.security;

import static org.junit.Assert.assertEquals;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.users.bootstrap.IUsersBootstrap;
import org.navalplanner.business.users.bootstrap.MandatoryUser;
import org.navalplanner.business.users.entities.UserRole;
import org.navalplanner.web.security.DefaultUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for implementations of Spring Security's
 * <code>UserDetailsService</code>.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
    WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE })
@Transactional
public class UserDetailsServiceTest {

    @Autowired
    // FIXME private UserDetailsService userDetailsService;
    private DefaultUserDetailsService userDetailsService;

    @Autowired
    private IUsersBootstrap usersBootstrap;

    @Test
    public void testLoadUserByUsername() {

        usersBootstrap.loadRequiredData();

        for (MandatoryUser u : MandatoryUser.values()) {

            UserDetails userDetails =
                userDetailsService.loadUserByUsername(u.name());

            assertEquals(u.name(), userDetails.getUsername());

            assertEquals(u.getInitialRoles(), getUserRoles(userDetails));

        }

    }

    private Set<UserRole> getUserRoles(UserDetails userDetails) {

        Set<UserRole> userRoles = new HashSet<UserRole>();

        for (GrantedAuthority a : userDetails.getAuthorities()) {
            userRoles.add(UserRole.valueOf(a.getAuthority()));
        }

        return userRoles;

    }

}
