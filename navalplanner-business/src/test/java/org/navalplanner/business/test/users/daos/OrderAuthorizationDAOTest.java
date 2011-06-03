/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.test.users.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
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
        return User.create(loginName, loginName, new HashSet<UserRole>());
    }

    private Profile createValidProfile() {
        Set<UserRole> roles = new HashSet<UserRole>();
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
    public void loadRequiredaData() {
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
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

    @Test
    public void testNavigateFromOrderAuthorizationToOrder() {
        Order order = createValidOrder();
        orderDAO.save(order);

        UserOrderAuthorization userOrderAuthorization = createValidUserOrderAuthorization();
        userOrderAuthorization.setOrder(order);
        orderAuthorizationDAO.save(userOrderAuthorization);
        assertEquals(order.getId(),userOrderAuthorization.getOrder().getId());
    }
}
