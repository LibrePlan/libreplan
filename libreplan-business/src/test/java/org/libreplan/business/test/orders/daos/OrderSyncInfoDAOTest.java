/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

package org.libreplan.business.test.orders.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.daos.IOrderSyncInfoDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderSyncInfo;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.bootstrap.IScenariosBootstrap;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.test.calendars.entities.BaseCalendarTest;
import org.libreplan.business.test.planner.daos.ResourceAllocationDAOTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link OrderSyncInfoDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 *
 * OrderSyncInfo.isUniqueOrderSyncInfoConstraint() (a bean-validation check run on save())
 * calls a REQUIRES_NEW DAO method, which can't see uncommitted data from a normal
 * @Transactional test's own transaction - not even the referenced Order itself. Test methods
 * here run without @Transactional and use IAdHocTransactionService to genuinely commit each
 * step (matching OrderDAOTest/ExternalCompanyDAOTest), and re-fetch the Order by id in each
 * new transaction rather than reusing the Java object across transaction boundaries (a
 * detached entity from a closed session/transaction isn't recognized as persistent by a
 * later, unrelated transaction).
 */
public class OrderSyncInfoDAOTest {

    @Autowired
    private IOrderSyncInfoDAO orderSyncInfoDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IBaseCalendarDAO calendarDAO;

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Before
    public void loadRequiredData() {
        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                scenariosBootstrap.loadRequiredData();
                return null;
            }
        });
    }

    private Long createAndCommitOrder() {
        return transactionService.runOnAnotherTransaction(new IOnTransaction<Long>() {
            @Override
            public Long execute() {
                Order order = Order.create();
                order.setName(UUID.randomUUID().toString());
                order.setCode(UUID.randomUUID().toString());
                order.setInitDate(new Date());
                BaseCalendar basicCalendar = BaseCalendarTest.createBasicCalendar();
                calendarDAO.save(basicCalendar);
                order.setCalendar(basicCalendar);
                OrderVersion orderVersion = ResourceAllocationDAOTest.setupVersionUsing(scenarioManager, order);
                orderElementDAO.save(order);
                orderElementDAO.flush();
                order.useSchedulingDataFor(orderVersion);
                return order.getId();
            }
        });
    }

    private Order findOrder(Long orderId) {
        try {
            return orderDAO.find(orderId);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private OrderSyncInfo createValid(String key, Order order, String connectorName, Date lastSyncDate) {
        OrderSyncInfo info = OrderSyncInfo.create(key, order, connectorName);
        info.setLastSyncDate(lastSyncDate);
        orderSyncInfoDAO.save(info);
        orderSyncInfoDAO.flush();
        return info;
    }

    @Test
    public void testFindLastSynchronizedInfosByOrderAndConnectorNameOrderedDescByDate() {
        final Long orderId = createAndCommitOrder();
        final String connectorName = "connector-" + UUID.randomUUID();

        List<Long> ids = transactionService.runOnAnotherTransaction(new IOnTransaction<List<Long>>() {
            @Override
            public List<Long> execute() {
                Order order = findOrder(orderId);
                OrderSyncInfo older = createValid("key-" + UUID.randomUUID(), order, connectorName, new Date(1000));
                OrderSyncInfo newer = createValid("key-" + UUID.randomUUID(), order, connectorName, new Date(2000));
                return java.util.Arrays.asList(older.getId(), newer.getId());
            }
        });

        List<Long> resultIds = transactionService.runOnAnotherTransaction(new IOnTransaction<List<Long>>() {
            @Override
            public List<Long> execute() {
                Order order = findOrder(orderId);
                List<OrderSyncInfo> result =
                        orderSyncInfoDAO.findLastSynchronizedInfosByOrderAndConnectorName(order, connectorName);
                List<Long> resultIds = new java.util.ArrayList<>();
                for (OrderSyncInfo info : result) {
                    resultIds.add(info.getId());
                }
                return resultIds;
            }
        });

        assertEquals(2, resultIds.size());
        assertEquals(ids.get(1), resultIds.get(0));
        assertEquals(ids.get(0), resultIds.get(1));
    }

    @Test
    public void testFindLastSynchronizedInfoByOrderAndConnectorNameReturnsMostRecent() {
        final Long orderId = createAndCommitOrder();
        final String connectorName = "connector-" + UUID.randomUUID();

        final Long newerId = transactionService.runOnAnotherTransaction(new IOnTransaction<Long>() {
            @Override
            public Long execute() {
                Order order = findOrder(orderId);
                createValid("key-" + UUID.randomUUID(), order, connectorName, new Date(1000));
                return createValid("key-" + UUID.randomUUID(), order, connectorName, new Date(2000)).getId();
            }
        });

        Long resultId = transactionService.runOnAnotherTransaction(new IOnTransaction<Long>() {
            @Override
            public Long execute() {
                Order order = findOrder(orderId);
                return orderSyncInfoDAO.findLastSynchronizedInfoByOrderAndConnectorName(order, connectorName).getId();
            }
        });

        assertEquals(newerId, resultId);
    }

    @Test
    public void testFindLastSynchronizedInfoByOrderAndConnectorNameReturnsNullWhenNone() {
        final Long orderId = createAndCommitOrder();

        OrderSyncInfo result = transactionService.runOnAnotherTransaction(new IOnTransaction<OrderSyncInfo>() {
            @Override
            public OrderSyncInfo execute() {
                Order order = findOrder(orderId);
                return orderSyncInfoDAO.findLastSynchronizedInfoByOrderAndConnectorName(
                        order, "does-not-exist-" + UUID.randomUUID());
            }
        });

        assertNull(result);
    }

    @Test
    public void testFindByKeyOrderAndConnectorNameRequiresAllToMatch() {
        final Long orderId = createAndCommitOrder();
        final String key = "key-" + UUID.randomUUID();
        final String connectorName = "connector-" + UUID.randomUUID();

        final Long infoId = transactionService.runOnAnotherTransaction(new IOnTransaction<Long>() {
            @Override
            public Long execute() {
                Order order = findOrder(orderId);
                return createValid(key, order, connectorName, new Date()).getId();
            }
        });

        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                Order order = findOrder(orderId);
                assertEquals(infoId,
                        orderSyncInfoDAO.findByKeyOrderAndConnectorName(key, order, connectorName).getId());
                assertNull(orderSyncInfoDAO.findByKeyOrderAndConnectorName("different-key", order, connectorName));
                assertNull(orderSyncInfoDAO.findByKeyOrderAndConnectorName(key, order, "different-connector"));
                return null;
            }
        });
    }

    @Test
    public void testFindByConnectorNameOnlyReturnsMatches() {
        final Long orderId = createAndCommitOrder();
        final String connectorName = "connector-" + UUID.randomUUID();

        final Long matchingId = transactionService.runOnAnotherTransaction(new IOnTransaction<Long>() {
            @Override
            public Long execute() {
                Order order = findOrder(orderId);
                Long m = createValid("key-" + UUID.randomUUID(), order, connectorName, new Date()).getId();
                createValid("key-" + UUID.randomUUID(), order, "other-" + UUID.randomUUID(), new Date());
                return m;
            }
        });

        List<Long> resultIds = transactionService.runOnAnotherTransaction(new IOnTransaction<List<Long>>() {
            @Override
            public List<Long> execute() {
                List<OrderSyncInfo> result = orderSyncInfoDAO.findByConnectorName(connectorName);
                List<Long> resultIds = new java.util.ArrayList<>();
                for (OrderSyncInfo info : result) {
                    resultIds.add(info.getId());
                }
                return resultIds;
            }
        });

        assertEquals(1, resultIds.size());
        assertEquals(matchingId, resultIds.get(0));
    }

}
