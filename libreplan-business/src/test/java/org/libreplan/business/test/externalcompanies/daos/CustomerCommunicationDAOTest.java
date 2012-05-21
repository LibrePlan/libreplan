/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 WirelessGalicia, S.L.
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

package org.libreplan.business.test.externalcompanies.daos;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.externalcompanies.daos.ICustomerCommunicationDAO;
import org.libreplan.business.externalcompanies.entities.CustomerCommunication;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.test.calendars.entities.BaseCalendarTest;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link CustomerCommunication}
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class CustomerCommunicationDAOTest {

    @Autowired
    ICustomerCommunicationDAO customerCommunicationDAO;

    @Autowired
    IOrderDAO orderDAO;

    @Autowired
    private IBaseCalendarDAO calendarDAO;

    public Order createValidOrder(String name) {
        Order order = Order.create();
        order.setName(name);
        order.setCode(UUID.randomUUID().toString());
        order.setInitDate(new Date());
        BaseCalendar basicCalendar = BaseCalendarTest.createBasicCalendar();
        calendarDAO.save(basicCalendar);
        order.setCalendar(basicCalendar);
        orderDAO.save(order);
        return order;
    }

    private Date givenDeadLine(int months) {
        LocalDate date = new LocalDate();
        date.plusMonths(months);
        return date.toDateTimeAtStartOfDay().toDate();
    }

    public CustomerCommunication createValidCustomerCommunication() {
        Order order = createValidOrder("Order A");
        CustomerCommunication customerCommunication = CustomerCommunication
                .createTodayNewProject(givenDeadLine(2));
        customerCommunication.setOrder(order);
        return customerCommunication;
    }

    @Test
    public void testOrderDAOInSpringContainer() {
        assertNotNull(orderDAO);
    }

    @Test
    public void testCustomerCommunicationDAOInSpringContainer() {
        assertNotNull(customerCommunicationDAO);
    }

    @Test
    public void testSaveCustomerCommunication() {
        CustomerCommunication customerCommunication = createValidCustomerCommunication();
        customerCommunicationDAO.save(customerCommunication);
        assertTrue(customerCommunication.getId() != null);
    }

    @Test
    public void testRemoveCustomerCommunication()
            throws InstanceNotFoundException {
        CustomerCommunication customerCommunication = createValidCustomerCommunication();
        customerCommunicationDAO.save(customerCommunication);
        assertTrue(customerCommunication.getId() != null);
        customerCommunicationDAO.remove(customerCommunication.getId());
        assertFalse(customerCommunicationDAO
                .exists(customerCommunication.getId()));
    }

    @Test
    public void testSaveCustomerCommunicationWithoutOrder()
            throws InstanceNotFoundException {
        CustomerCommunication customerCommunication = createValidCustomerCommunication();
        customerCommunication.setOrder(null);
        try {
            customerCommunicationDAO.save(customerCommunication);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // Ok
        }
    }

}
