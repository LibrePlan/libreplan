/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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

package org.navalplanner.business.test.orders.daos;

import static junit.framework.Assert.assertNotNull;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.bootstrap.IScenariosBootstrap;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.test.calendars.entities.BaseCalendarTest;
import org.navalplanner.business.test.planner.daos.ResourceAllocationDAOTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test for {@link IOrderDAO}
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class OrderDAOTest {

    @Before
    public void loadRequiredaData() {
        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                scenariosBootstrap.loadRequiredData();
                return null;
            }
        });
    }

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IBaseCalendarDAO calendarDAO;

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Test
    public void testInSpringContainer() {
        assertNotNull(orderDAO);
    }

    private Order createValidOrder(String name) {
        Order order = Order.create();
        order.setName(name);
        order.setCode(UUID.randomUUID().toString());
        order.setInitDate(new Date());
        BaseCalendar basicCalendar = BaseCalendarTest.createBasicCalendar();
        calendarDAO.save(basicCalendar);
        order.setCalendar(basicCalendar);
        OrderVersion orderVersion = ResourceAllocationDAOTest
                .setupVersionUsing(scenarioManager, order);
        order.useSchedulingDataFor(orderVersion);
        return order;
    }

    @Test
    public void testSaveTwoOrdersWithDifferentNames() {
        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                Order order = createValidOrder("test");
                orderDAO.save(order);
                orderDAO.flush();
                return null;
            }
        });

        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                Order order = createValidOrder("test2");
                orderDAO.save(order);
                orderDAO.flush();
                return null;
            }
        });
    }

    @Test(expected = ValidationException.class)
    public void testSaveTwoOrdersWithSameNames() {
        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                Order order = createValidOrder("test");
                orderDAO.save(order);
                orderDAO.flush();
                return null;
            }
        });

        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                Order order = createValidOrder("test");
                orderDAO.save(order);
                orderDAO.flush();
                return null;
            }
        });
    }

}
