/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2015 LibrePlan
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.business.test.orders.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.daos.IOrderFileDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderFile;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.bootstrap.IScenariosBootstrap;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.test.calendars.entities.BaseCalendarTest;
import org.libreplan.business.test.planner.daos.ResourceAllocationDAOTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link OrderFileDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 */
public class OrderFileDAOTest {

    @Autowired
    private IOrderFileDAO orderFileDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IBaseCalendarDAO calendarDAO;

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Autowired
    private IScenarioManager scenarioManager;

    @Before
    public void loadRequiredData() {
        scenariosBootstrap.loadRequiredData();
    }

    private Order createValidOrder() {
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
        return order;
    }

    private OrderFile createValid(Order parent) {
        OrderFile file = new OrderFile();
        file.setName("file-" + UUID.randomUUID());
        file.setType("application/octet-stream");
        file.setParent(parent);
        orderFileDAO.save(file);
        return file;
    }

    @Test
    @Transactional
    public void testGetAllIncludesSaved() {
        Order order = createValidOrder();
        OrderFile file = createValid(order);

        boolean found = false;
        for (OrderFile f : orderFileDAO.getAll()) {
            if (f.getId().equals(file.getId())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    @Transactional
    public void testFindByParentOnlyReturnsMatching() {
        Order order1 = createValidOrder();
        Order order2 = createValidOrder();

        OrderFile matching = createValid(order1);
        OrderFile other = createValid(order2);

        List<OrderFile> result = orderFileDAO.findByParent(order1);
        assertEquals(1, result.size());
        assertEquals(matching.getId(), result.get(0).getId());
    }

    @Test
    @Transactional
    public void testDeleteRemovesEntity() {
        Order order = createValidOrder();
        OrderFile file = createValid(order);
        orderFileDAO.delete(file);
        assertFalse(orderFileDAO.exists(file.getId()));
    }

}
