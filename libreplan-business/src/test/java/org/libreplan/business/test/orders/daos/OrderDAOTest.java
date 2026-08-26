/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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

import static org.junit.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Date;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;

import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.externalcompanies.entities.DeadlineCommunication;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderStatusEnum;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.bootstrap.IScenariosBootstrap;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.test.calendars.entities.BaseCalendarTest;
import org.libreplan.business.test.planner.daos.ResourceAllocationDAOTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test for {@link IOrderDAO}.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE, BUSINESS_SPRING_CONFIG_TEST_FILE })
public class OrderDAOTest {

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
    @Transactional
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
        OrderVersion orderVersion = ResourceAllocationDAOTest.setupVersionUsing(scenarioManager, order);
        order.useSchedulingDataFor(orderVersion);
        return order;
    }

    private Order createValidOrderWithDeadlineCommunications(String name) {
        Order order = createValidOrder(name);

        // Create two deadline communications
        Date date1 = (new Date());
        Date date2 = (new LocalDate(date1).plusDays(3)).toDateTimeAtStartOfDay().toDate();

        DeadlineCommunication deadlineCommunication1 = DeadlineCommunication.create(date1, null);
        DeadlineCommunication deadlineCommunication2 = DeadlineCommunication.create(date2, null);

        order.getDeliveringDates().add(deadlineCommunication1);
        order.getDeliveringDates().add(deadlineCommunication2);

        return order;
    }

    @Test
    @Transactional
    public void testSaveOrdersWithDeliveringDates() {
        Order order = createValidOrderWithDeadlineCommunications("test");
        orderDAO.save(order);
        orderDAO.flush();

        assertThat(order.getDeliveringDates().size(), equalTo(2));

        DeadlineCommunication dcFirst = order.getDeliveringDates().first();
        DeadlineCommunication dcLast = order.getDeliveringDates().last();

        assertTrue(dcFirst.getSaveDate().after(dcLast.getSaveDate()));


        // A new DeadlineCommunication is placed between the existing communications
        Date date = (new LocalDate(dcLast.getSaveDate()).plusDays(2)).toDateTimeAtStartOfDay().toDate();
        DeadlineCommunication deadlineCommunication = DeadlineCommunication.create(date, null);
        order.getDeliveringDates().add(deadlineCommunication);

        orderDAO.save(order);
        orderDAO.flush();

        assertThat(order.getDeliveringDates().size(), equalTo(3));

        dcFirst = order.getDeliveringDates().first();
        dcLast =  order.getDeliveringDates().last();
        DeadlineCommunication new_dc = (DeadlineCommunication) order.getDeliveringDates().toArray()[1];

        assertTrue(dcFirst.getSaveDate().after(dcLast.getSaveDate()));
        assertTrue(dcFirst.getSaveDate().after(new_dc.getSaveDate()));
        assertFalse(dcLast.equals(new_dc));
        assertTrue(dcLast.getSaveDate().before(new_dc.getSaveDate()));
    }

    @Test
    @Transactional
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
    @Transactional
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

    /**
     * Regression test for a production bug (found on the v1.6.1 line, see the
     * fix-tasksource-1.6.1 branch) where saving a project failed on the very first Save click,
     * with no user edits, throwing:
     * <pre>
     * org.hibernate.NonUniqueObjectException: A different object with the same identifier value
     * was already associated with the session : [org.libreplan.business.orders.entities.Order#...]
     * </pre>
     *
     * Root cause: {@code SaveCommandBuilder.doTheSaving()} (libreplan-webapp) receives a detached
     * {@code Order} (carried over from an earlier, already-closed request/session - the normal way
     * ZK's long-lived planning conversation works) and calls {@code state.synchronizeTrees()} before
     * ever reattaching it. That triggers a lazy/eager load (via
     * {@code SchedulingDataForVersion.orderElement}, mapped {@code fetch="join"}) that resolves to a
     * FRESH, separate {@code Order} instance for the same id, since the original detached instance
     * was never registered in the new transaction's session. The later
     * {@code orderDAO.save(order)} call then fails, because Hibernate finds two different Java
     * objects claiming the same identity in one session.
     *
     * The fix ({@code orderDAO.reattach(order)} at the very start of {@code doTheSaving()}) makes
     * the detached instance the session's canonical managed object first, so any later load of the
     * same id resolves to it instead of creating a conflicting duplicate.
     *
     * This test reproduces the exact Hibernate mechanism directly at the DAO layer - deterministic
     * and independent of the specific scheduling-tree shape that triggered it in production. Ported
     * here defensively: the original crash did NOT reproduce under this branch's Hibernate 6 (which
     * appears to treat the same fetch="join" association as genuinely lazy), but the underlying
     * detached-order-reattachment gap is identical, so this guards against it regressing here too.
     */
    @Test
    @Transactional
    public void savingADetachedOrderAfterAnotherInstanceIsAlreadyInTheSessionThrowsNonUniqueObjectException() {
        final Long orderId = transactionService.runOnAnotherTransaction(new IOnTransaction<Long>() {
            @Override
            public Long execute() {
                Order order = createValidOrder("reattach-regression-" + UUID.randomUUID());
                orderDAO.save(order);
                orderDAO.flush();
                return order.getId();
            }
        });

        // A genuinely detached Java object: loaded in its own transaction, whose session has since
        // closed - exactly like `state.getOrder()` in SaveCommandBuilder, carried over from an
        // earlier ZK request.
        final Order detachedOrder = transactionService.runOnAnotherTransaction(new IOnTransaction<Order>() {
            @Override
            public Order execute() {
                try {
                    return orderDAO.find(orderId);
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Without reattaching first: once something else has loaded a DIFFERENT instance for the
        // same id into the session, saving the original detached reference must fail - this is the
        // original bug, reproduced directly.
        try {
            transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
                @Override
                public Void execute() {
                    try {
                        orderDAO.find(orderId); // loads a fresh, different Order instance
                    } catch (InstanceNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    orderDAO.save(detachedOrder); // same id, different instance -> must fail
                    return null;
                }
            });
            fail("Expected saving a stale detached Order after a different instance for the same id "
                    + "was already loaded in the session to throw a NonUniqueObjectException, "
                    + "reproducing the original v1.6.1 production bug.");
        } catch (RuntimeException expected) {
            assertNonUniqueObjectExceptionSomewhereInCauseChain(expected);
        }

        // The fix: reattaching first makes the detached instance the session's managed instance, so
        // a later load of the same id resolves to it instead of conflicting.
        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                orderDAO.reattach(detachedOrder);
                try {
                    orderDAO.find(orderId);
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
                orderDAO.save(detachedOrder);
                orderDAO.flush();
                return null;
            }
        });
    }

    private static void assertNonUniqueObjectExceptionSomewhereInCauseChain(Throwable t) {
        Throwable current = t;
        while (current != null) {
            if (current instanceof org.hibernate.NonUniqueObjectException) {
                return;
            }
            current = current.getCause();
        }
        throw new AssertionError(
                "Expected a NonUniqueObjectException somewhere in the cause chain of: " + t, t);
    }

    /*
     * Characterization tests added for the Hibernate Criteria -> JPA Criteria API migration
     * (Jakarta EE / Hibernate 6). findAll/findByCode/getActiveOrders/
     * getOrdersWithNotEmptyCustomersReferences/existsByNameAnotherTransaction/
     * findByNameAnotherTransaction had no test coverage before.
     */

    @Test
    @Transactional
    public void testFindAllIncludesSaved() {
        Order order = createValidOrder("test-" + UUID.randomUUID());
        orderDAO.save(order);

        boolean found = false;
        for (Order o : orderDAO.findAll()) {
            if (o.getId().equals(order.getId())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    @Transactional
    public void testFindByCodeIsCaseInsensitiveAndTrims() throws InstanceNotFoundException {
        Order order = createValidOrder("test-" + UUID.randomUUID());
        String mixedCaseCode = "MiXeD-" + UUID.randomUUID();
        order.setCode(mixedCaseCode);
        orderDAO.save(order);

        assertEquals(order.getId(), orderDAO.findByCode(mixedCaseCode).getId());
        assertEquals(order.getId(), orderDAO.findByCode(mixedCaseCode.toLowerCase()).getId());
        assertEquals(order.getId(), orderDAO.findByCode("  " + mixedCaseCode + "  ").getId());
    }

    @Test(expected = InstanceNotFoundException.class)
    @Transactional
    public void testFindByCodeThrowsWhenNotFound() throws InstanceNotFoundException {
        orderDAO.findByCode("does-not-exist-" + UUID.randomUUID());
    }

    @Test
    @Transactional
    public void testGetActiveOrdersExcludesCancelledAndStored() {
        Order active = createValidOrder("test-" + UUID.randomUUID());
        orderDAO.save(active);

        Order cancelled = createValidOrder("test-" + UUID.randomUUID());
        cancelled.setState(OrderStatusEnum.CANCELLED);
        orderDAO.save(cancelled);

        Order stored = createValidOrder("test-" + UUID.randomUUID());
        stored.setState(OrderStatusEnum.STORED);
        orderDAO.save(stored);

        boolean activeFound = false;
        for (Order o : orderDAO.getActiveOrders()) {
            if (o.getId().equals(active.getId())) {
                activeFound = true;
            }
            assertFalse(o.getId().equals(cancelled.getId()));
            assertFalse(o.getId().equals(stored.getId()));
        }
        assertTrue(activeFound);
    }

    @Test
    @Transactional
    public void testGetOrdersWithNotEmptyCustomersReferencesExcludesNullAndEmpty() {
        Order withReference = createValidOrder("test-" + UUID.randomUUID());
        withReference.setCustomerReference("ref-" + UUID.randomUUID());
        orderDAO.save(withReference);

        Order withEmptyReference = createValidOrder("test-" + UUID.randomUUID());
        withEmptyReference.setCustomerReference("");
        orderDAO.save(withEmptyReference);

        Order withNullReference = createValidOrder("test-" + UUID.randomUUID());
        orderDAO.save(withNullReference);

        List<Order> result = orderDAO.getOrdersWithNotEmptyCustomersReferences();
        boolean found = false;
        for (Order o : result) {
            if (o.getId().equals(withReference.getId())) {
                found = true;
            }
            assertFalse(o.getId().equals(withEmptyReference.getId()));
            assertFalse(o.getId().equals(withNullReference.getId()));
        }
        assertTrue(found);
    }

    @Test
    public void testFindByNameAnotherTransactionReturnsMatch() throws InstanceNotFoundException {
        final String name = "test-" + UUID.randomUUID();

        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                Order order = createValidOrder(name);
                orderDAO.save(order);
                orderDAO.flush();
                return null;
            }
        });

        assertEquals(name, orderDAO.findByNameAnotherTransaction(name).getName());
    }

    @Test(expected = InstanceNotFoundException.class)
    public void testFindByNameAnotherTransactionThrowsWhenNotFound() throws InstanceNotFoundException {
        orderDAO.findByNameAnotherTransaction("does-not-exist-" + UUID.randomUUID());
    }

    @Test
    public void testExistsByNameAnotherTransaction() {
        final String name = "test-" + UUID.randomUUID();

        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                Order order = createValidOrder(name);
                orderDAO.save(order);
                orderDAO.flush();
                return null;
            }
        });

        assertTrue(orderDAO.existsByNameAnotherTransaction(name));
        assertFalse(orderDAO.existsByNameAnotherTransaction("does-not-exist-" + UUID.randomUUID()));
    }

}
