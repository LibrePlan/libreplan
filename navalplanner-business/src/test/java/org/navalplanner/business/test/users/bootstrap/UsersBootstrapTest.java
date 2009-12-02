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

package org.navalplanner.business.test.users.bootstrap;

import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.users.bootstrap.IUsersBootstrap;
import org.navalplanner.business.users.bootstrap.MandatoryUser;
import org.navalplanner.business.users.daos.IUserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for <code>IUsersBootstrap</code>.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
    BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class UsersBootstrapTest {

    @Autowired
    private IUsersBootstrap usersBootstrap;

    @Autowired
    private IUserDAO userDAO;

    @Test
    public void testMandatoryUsersCreated() {

       checkLoadRequiredData();

        /*
         * Load data again to verify that a second load does not cause
         * problems.
         */
       checkLoadRequiredData();

    }

    private void checkLoadRequiredData() {

        usersBootstrap.loadRequiredData();

        for (MandatoryUser u : MandatoryUser.values()) {
            assertTrue(userDAO.existsByLoginName(u.name()));
        }

    }

}
