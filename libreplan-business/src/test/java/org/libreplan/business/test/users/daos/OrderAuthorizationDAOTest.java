/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

package org.libreplan.business.test.users.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.users.daos.IOrderAuthorizationDAO;
import org.libreplan.business.users.daos.IProfileDAO;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.OrderAuthorization;
import org.libreplan.business.users.entities.OrderAuthorizationType;
import org.libreplan.business.users.entities.Profile;
import org.libreplan.business.users.entities.ProfileOrderAuthorization;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserOrderAuthorization;
import org.libreplan.business.users.entities.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE, BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Test for {@link org.libreplan.business.users.daos.OrderAuthorizationDAO}.
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 *
 */
public class OrderAuthorizationDAOTest {

    @Autowired
    IOrderAuthorizationDAO orderAuthorizationDAO;

    @Autowired
    IUserDAO userDAO;

    @Autowired
    IProfileDAO profileDAO;

    @Autowired
    IOrderDAO orderDAO;

    @Autowired
    IBaseCalendarDAO baseCalendarDAO;

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    private UserOrderAuthorization createValidUserOrderAuthorization() {
        return UserOrderAuthorization.create(OrderAuthorizationType.READ_AUTHORIZATION);
    }

    private ProfileOrderAuthorization createValidProfileOrderAuthorization() {
        return ProfileOrderAuthorization.create(OrderAuthorizationType.READ_AUTHORIZATION);
    }

    private User createValidUser() {
        String loginName = UUID.randomUUID().toString();
        return User.create(loginName, loginName, new HashSet<>());
    }

    private Profile createValidProfile() {
        Set<UserRole> roles = new HashSet<>();
        return Profile.create(UUID.randomUUID().toString(), roles);
    }

    private Order createValidOrder() {
        Order order = Order.create();
        BaseCalendar baseCalendar = BaseCalendar.createBasicCalendar();
        baseCalendar.setName(UUID.randomUUID().toString());
        baseCalendarDAO.save(baseCalendar);
        order.setCalendar(baseCalendar);
        order.setInitDate(new Date());
        order.setName(UUID.randomUUID().toString());
        order.setCode(UUID.randomUUID().toString());

        return order;
    }

    @Before
    public void loadRequiredData() {
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
    }

    @Test
    @Transactional
    public void testInSpringContainer() {
        assertNotNull(orderAuthorizationDAO);
    }

    @Test
    @Transactional
    public void testSaveOrderAuthorization() {
        UserOrderAuthorization userOrderAuthorization = createValidUserOrderAuthorization();
        orderAuthorizationDAO.save(userOrderAuthorization);
        assertNotNull(userOrderAuthorization.getId());

        ProfileOrderAuthorization profileOrderAuthorization = createValidProfileOrderAuthorization();
        orderAuthorizationDAO.save(profileOrderAuthorization);
        assertNotNull(profileOrderAuthorization.getId());
    }

    @Test
    @Transactional
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
    @Transactional
    public void testListOrderAuthorizations() {
        int previous = orderAuthorizationDAO.list(OrderAuthorization.class).size();
        UserOrderAuthorization userOrderAuthorization = createValidUserOrderAuthorization();
        orderAuthorizationDAO.save(userOrderAuthorization);
        ProfileOrderAuthorization profileOrderAuthorization = createValidProfileOrderAuthorization();
        orderAuthorizationDAO.save(profileOrderAuthorization);
        assertEquals(previous + 2, orderAuthorizationDAO.list(OrderAuthorization.class).size());
    }

    @Test
    @Transactional
    public void testListOrderAuthorizationsByOrder() {
        int previous = orderAuthorizationDAO.list(OrderAuthorization.class).size();

        Order order = createValidOrder();
        orderDAO.save(order);

        UserOrderAuthorization userOrderAuthorization1 = createValidUserOrderAuthorization();
        userOrderAuthorization1.setOrder(order);
        orderAuthorizationDAO.save(userOrderAuthorization1);

        UserOrderAuthorization userOrderAuthorization2 = createValidUserOrderAuthorization();
        userOrderAuthorization2.setOrder(order);
        orderAuthorizationDAO.save(userOrderAuthorization2);

        assertEquals(previous + 2, orderAuthorizationDAO.listByOrder(order).size());

        userOrderAuthorization2.setOrder(null);
        orderAuthorizationDAO.save(userOrderAuthorization2);
        assertEquals(previous + 1, orderAuthorizationDAO.listByOrder(order).size());
   }

    @Test
    @Transactional
    public void testNavigateFromOrderAuthorizationToUser() {
        User user = createValidUser();
        userDAO.save(user);
        UserOrderAuthorization userOrderAuthorization = createValidUserOrderAuthorization();
        userOrderAuthorization.setUser(user);
        orderAuthorizationDAO.save(userOrderAuthorization);
        assertEquals(user.getId(), userOrderAuthorization.getUser().getId());
    }

    @Test
    @Transactional
    public void testNavigateFromOrderAuthorizationToProfile() {
        Profile profile = createValidProfile();
        profileDAO.save(profile);
        ProfileOrderAuthorization profileOrderAuthorization = createValidProfileOrderAuthorization();
        profileOrderAuthorization.setProfile(profile);
        orderAuthorizationDAO.save(profileOrderAuthorization);
        assertEquals(profile.getId(), profileOrderAuthorization.getProfile().getId());
    }

    @Test
    @Transactional
    public void testNavigateFromOrderAuthorizationToOrder() {
        Order order = createValidOrder();
        orderDAO.save(order);

        UserOrderAuthorization userOrderAuthorization = createValidUserOrderAuthorization();
        userOrderAuthorization.setOrder(order);
        orderAuthorizationDAO.save(userOrderAuthorization);
        assertEquals(order.getId(),userOrderAuthorization.getOrder().getId());
    }

    /*
     * Characterization tests added for the Hibernate Criteria -> JPA Criteria API migration
     * (Jakarta EE / Hibernate 6). listByUser/listByProfile/listByOrderAndUser/listByOrderAndProfile
     * had no direct test coverage before; each test below creates multiple records that differ in
     * exactly one filtered field, to confirm the query both finds the matching record AND excludes
     * the non-matching ones - this is the part a wrong AND/OR or equality translation would break
     * silently. Run once here (pre-migration, still on the old Criteria API) to confirm they pass
     * against known-correct behavior, then re-run unchanged after the DAO is rewritten.
     */

    @Test
    @Transactional
    public void testListOrderAuthorizationsByUser() {
        User user1 = createValidUser();
        userDAO.save(user1);
        User user2 = createValidUser();
        userDAO.save(user2);

        UserOrderAuthorization auth1 = createValidUserOrderAuthorization();
        auth1.setUser(user1);
        orderAuthorizationDAO.save(auth1);

        UserOrderAuthorization auth2 = createValidUserOrderAuthorization();
        auth2.setUser(user2);
        orderAuthorizationDAO.save(auth2);

        List<OrderAuthorization> resultsForUser1 = orderAuthorizationDAO.listByUser(user1);
        assertEquals(1, resultsForUser1.size());
        assertEquals(auth1.getId(), resultsForUser1.get(0).getId());

        List<OrderAuthorization> resultsForUser2 = orderAuthorizationDAO.listByUser(user2);
        assertEquals(1, resultsForUser2.size());
        assertEquals(auth2.getId(), resultsForUser2.get(0).getId());
    }

    @Test
    @Transactional
    public void testListOrderAuthorizationsByProfile() {
        Profile profile1 = createValidProfile();
        profileDAO.save(profile1);
        Profile profile2 = createValidProfile();
        profileDAO.save(profile2);

        ProfileOrderAuthorization auth1 = createValidProfileOrderAuthorization();
        auth1.setProfile(profile1);
        orderAuthorizationDAO.save(auth1);

        ProfileOrderAuthorization auth2 = createValidProfileOrderAuthorization();
        auth2.setProfile(profile2);
        orderAuthorizationDAO.save(auth2);

        List<OrderAuthorization> resultsForProfile1 = orderAuthorizationDAO.listByProfile(profile1);
        assertEquals(1, resultsForProfile1.size());
        assertEquals(auth1.getId(), resultsForProfile1.get(0).getId());

        List<OrderAuthorization> resultsForProfile2 = orderAuthorizationDAO.listByProfile(profile2);
        assertEquals(1, resultsForProfile2.size());
        assertEquals(auth2.getId(), resultsForProfile2.get(0).getId());
    }

    @Test
    @Transactional
    public void testListOrderAuthorizationsByOrderAndUser() {
        Order order1 = createValidOrder();
        orderDAO.save(order1);
        Order order2 = createValidOrder();
        orderDAO.save(order2);

        User user1 = createValidUser();
        userDAO.save(user1);
        User user2 = createValidUser();
        userDAO.save(user2);

        UserOrderAuthorization matching = createValidUserOrderAuthorization();
        matching.setOrder(order1);
        matching.setUser(user1);
        orderAuthorizationDAO.save(matching);

        // Same order, different user - must NOT match the order1+user1 query
        UserOrderAuthorization sameOrderDifferentUser = createValidUserOrderAuthorization();
        sameOrderDifferentUser.setOrder(order1);
        sameOrderDifferentUser.setUser(user2);
        orderAuthorizationDAO.save(sameOrderDifferentUser);

        // Same user, different order - must NOT match the order1+user1 query
        UserOrderAuthorization sameUserDifferentOrder = createValidUserOrderAuthorization();
        sameUserDifferentOrder.setOrder(order2);
        sameUserDifferentOrder.setUser(user1);
        orderAuthorizationDAO.save(sameUserDifferentOrder);

        List<OrderAuthorization> results = orderAuthorizationDAO.listByOrderAndUser(order1, user1);
        assertEquals(1, results.size());
        assertEquals(matching.getId(), results.get(0).getId());
    }

    @Test
    @Transactional
    public void testListOrderAuthorizationsByOrderAndProfile() {
        Order order1 = createValidOrder();
        orderDAO.save(order1);
        Order order2 = createValidOrder();
        orderDAO.save(order2);

        Profile profile1 = createValidProfile();
        profileDAO.save(profile1);
        Profile profile2 = createValidProfile();
        profileDAO.save(profile2);

        ProfileOrderAuthorization matching = createValidProfileOrderAuthorization();
        matching.setOrder(order1);
        matching.setProfile(profile1);
        orderAuthorizationDAO.save(matching);

        // Same order, different profile - must NOT match the order1+profile1 query
        ProfileOrderAuthorization sameOrderDifferentProfile = createValidProfileOrderAuthorization();
        sameOrderDifferentProfile.setOrder(order1);
        sameOrderDifferentProfile.setProfile(profile2);
        orderAuthorizationDAO.save(sameOrderDifferentProfile);

        // Same profile, different order - must NOT match the order1+profile1 query
        ProfileOrderAuthorization sameProfileDifferentOrder = createValidProfileOrderAuthorization();
        sameProfileDifferentOrder.setOrder(order2);
        sameProfileDifferentOrder.setProfile(profile1);
        orderAuthorizationDAO.save(sameProfileDifferentOrder);

        List<OrderAuthorization> results = orderAuthorizationDAO.listByOrderAndProfile(order1, profile1);
        assertEquals(1, results.size());
        assertEquals(matching.getId(), results.get(0).getId());
    }
}
