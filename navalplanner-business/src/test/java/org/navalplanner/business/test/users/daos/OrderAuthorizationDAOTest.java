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

package org.navalplanner.business.test.users.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.users.daos.IOrderAuthorizationDAO;
import org.navalplanner.business.users.daos.IProfileDAO;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.OrderAuthorization;
import org.navalplanner.business.users.entities.OrderAuthorizationType;
import org.navalplanner.business.users.entities.Profile;
import org.navalplanner.business.users.entities.ProfileOrderAuthorization;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserOrderAuthorization;
import org.navalplanner.business.users.entities.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Test for {@link OrderAuthorizationDAO}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 *
 */
@Transactional
public class OrderAuthorizationDAOTest {

    @Autowired
    IOrderAuthorizationDAO orderAuthorizationDAO;

    @Autowired
    IUserDAO userDAO;

    @Autowired
    IProfileDAO profileDAO;

    private UserOrderAuthorization createValidUserOrderAuthorization() {
        return UserOrderAuthorization.create(OrderAuthorizationType.READ_AUTHORIZATION);
    }

    private ProfileOrderAuthorization createValidProfileOrderAuthorization() {
        return ProfileOrderAuthorization.create(OrderAuthorizationType.READ_AUTHORIZATION);
    }

    private User createValidUser() {
        String loginName = UUID.randomUUID().toString();
        return User.create(loginName, loginName, new HashSet<UserRole>());
    }

    private Profile createValidProfile() {
        Set<UserRole> roles = new HashSet<UserRole>();
        roles.add(UserRole.ROLE_BASIC_USER);
        return Profile.create(UUID.randomUUID().toString(), roles);
    }

    @Test
    public void testInSpringContainer() {
        assertNotNull(orderAuthorizationDAO);
    }

    @Test
    public void testSaveOrderAuthorization() {
        UserOrderAuthorization userOrderAuthorization = createValidUserOrderAuthorization();
        orderAuthorizationDAO.save(userOrderAuthorization);
        assertNotNull(userOrderAuthorization.getId());

        ProfileOrderAuthorization profileOrderAuthorization = createValidProfileOrderAuthorization();
        orderAuthorizationDAO.save(profileOrderAuthorization);
        assertNotNull(profileOrderAuthorization.getId());
    }

    @Test
    public void testRemoveOrderAuthorization() throws InstanceNotFoundException {
        UserOrderAuthorization userOrderAuthorization = createValidUserOrderAuthorization();
        orderAuthorizationDAO.save(userOrderAuthorization);
        orderAuthorizationDAO.remove(userOrderAuthorization.getId());
        assertFalse(orderAuthorizationDAO.exists(userOrderAuthorization.getId()));

        ProfileOrderAuthorization profileOrderAuthorization = createValidProfileOrderAuthorization();
        orderAuthorizationDAO.save(profileOrderAuthorization);
        orderAuthorizationDAO.remove(profileOrderAuthorization.getId());
        assertFalse(orderAuthorizationDAO.exists(profileOrderAuthorization.getId()));
    }

    @Test
    public void testListOrderAuthorizations() {
        int previous = orderAuthorizationDAO.list(OrderAuthorization.class).size();
        UserOrderAuthorization userOrderAuthorization = createValidUserOrderAuthorization();
        orderAuthorizationDAO.save(userOrderAuthorization);
        ProfileOrderAuthorization profileOrderAuthorization = createValidProfileOrderAuthorization();
        orderAuthorizationDAO.save(profileOrderAuthorization);
        assertEquals(previous + 2, orderAuthorizationDAO.list(OrderAuthorization.class).size());
    }

    @Test
    public void testNavigateFromOrderAuthorizationToUser() {
        User user = createValidUser();
        userDAO.save(user);
        UserOrderAuthorization userOrderAuthorization = createValidUserOrderAuthorization();
        userOrderAuthorization.setUser(user);
        orderAuthorizationDAO.save(userOrderAuthorization);
        assertEquals(user.getId(), userOrderAuthorization.getUser().getId());
    }

    @Test
    public void testNavigateFromOrderAuthorizationToProfile() {
        Profile profile = createValidProfile();
        profileDAO.save(profile);
        ProfileOrderAuthorization profileOrderAuthorization = createValidProfileOrderAuthorization();
        profileOrderAuthorization.setProfile(profile);
        orderAuthorizationDAO.save(profileOrderAuthorization);
        assertEquals(profile.getId(), profileOrderAuthorization.getProfile().getId());
    }
}
