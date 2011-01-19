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

package org.navalplanner.web.test.users.bootstrap;

import static org.junit.Assert.assertEquals;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.web.users.bootstrap.IUsersBootstrapInDB;
import org.navalplanner.web.users.bootstrap.MandatoryUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for <code>IUsersBootstrapInDB</code>.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class UsersBootstrapInDBTest {

    @Autowired
    private IUsersBootstrapInDB usersBootstrap;

    @Autowired
    private IUserDAO userDAO;

    @Test
    public void testMandatoryUsersCreated() throws InstanceNotFoundException {

       checkLoadRequiredData();

        /*
         * Load data again to verify that a second load does not cause
         * problems.
         */
       checkLoadRequiredData();

    }

    private void checkLoadRequiredData() throws InstanceNotFoundException {

        usersBootstrap.loadRequiredData();

        for (MandatoryUser u : MandatoryUser.values()) {

            User user = userDAO.findByLoginName(u.getLoginName());

            assertEquals(u.getLoginName(), user.getLoginName());
            assertEquals(u.getInitialRoles(), user.getRoles());

        }

    }

}
