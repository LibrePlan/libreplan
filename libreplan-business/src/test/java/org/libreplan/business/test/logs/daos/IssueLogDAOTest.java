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

package org.libreplan.business.test.logs.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.logs.daos.IIssueLogDAO;
import org.libreplan.business.logs.entities.IssueLog;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.test.calendars.entities.BaseCalendarTest;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link IssueLogDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 */
public class IssueLogDAOTest {

    @Autowired
    private IIssueLogDAO issueLogDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IBaseCalendarDAO calendarDAO;

    @Autowired
    private IUserDAO userDAO;

    private User createValidUser() {
        User user = User.create(UUID.randomUUID().toString(), UUID.randomUUID().toString(), new HashSet<>());
        userDAO.save(user);
        return user;
    }

    private Order createValidOrder(String name) {
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

    @Test
    @Transactional
    public void testGetIssueLogsIncludesSaved() {
        IssueLog issueLog = IssueLog.create();
        issueLog.setOrder(createValidOrder("order-" + UUID.randomUUID()));
        issueLog.setCreatedBy(createValidUser());
        issueLogDAO.save(issueLog);

        boolean found = false;
        for (IssueLog l : issueLogDAO.getIssueLogs()) {
            if (l.getId().equals(issueLog.getId())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    @Transactional
    public void testGetByParentOnlyReturnsMatchingOrder() {
        Order order1 = createValidOrder("order-" + UUID.randomUUID());
        Order order2 = createValidOrder("order-" + UUID.randomUUID());

        IssueLog matching = IssueLog.create();
        matching.setOrder(order1);
        matching.setCreatedBy(createValidUser());
        issueLogDAO.save(matching);

        IssueLog other = IssueLog.create();
        other.setOrder(order2);
        other.setCreatedBy(createValidUser());
        issueLogDAO.save(other);

        List<IssueLog> result = issueLogDAO.getByParent(order1);
        assertEquals(1, result.size());
        assertEquals(matching.getId(), result.get(0).getId());
    }

}
