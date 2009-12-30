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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.users.daos.IOrderAuthorizationDAO;
import org.navalplanner.business.users.entities.OrderAuthorization;
import org.navalplanner.business.users.entities.OrderAuthorizationType;
import org.navalplanner.business.users.entities.ProfileOrderAuthorization;
import org.navalplanner.business.users.entities.UserOrderAuthorization;
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

    private UserOrderAuthorization createValidUserOrderAuthorization() {
        return UserOrderAuthorization.create(OrderAuthorizationType.READ_AUTHORIZATION);
    }

    private ProfileOrderAuthorization createValidProfileOrderAuthorization() {
        return ProfileOrderAuthorization.create(OrderAuthorizationType.READ_AUTHORIZATION);
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
}
