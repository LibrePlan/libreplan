/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.test.orders.daos;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.daos.OrderDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.orders.entities.TaskSource.TaskSourceSynchronization;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test for {@link OrderDAO}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class OrderDAOTest {

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Before
    public void loadRequiredaData() {
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
        configurationBootstrap.loadRequiredData();
    }

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private SessionFactory sessionFactory;

    private Order order;

    private Task createValidTask() {
        HoursGroup associatedHoursGroup = new HoursGroup();
        associatedHoursGroup.setCode("hours-group-code-" + UUID.randomUUID());
        OrderLine orderLine = createOrderLine();
        orderLine.addHoursGroup(associatedHoursGroup);
        TaskSource taskSource = TaskSource.create(orderLine, Arrays
                .asList(associatedHoursGroup));
        TaskSourceSynchronization mustAdd = TaskSource.mustAdd(taskSource);
        mustAdd.apply(taskSourceDAO);
        Task task = (Task) taskSource.getTask();
        return task;
    }

    private OrderLine createOrderLine() {
        OrderLine orderLine = OrderLine.create();
        orderLine.setName("bla");
        orderLine.setCode("code-" + UUID.randomUUID());
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setCode("hours-group-code-" + UUID.randomUUID());
        orderLine.addHoursGroup(hoursGroup);
        order = Order.create();
        order.setName("bla");
        order.setInitDate(new Date());
        order.setCode("code-" + UUID.randomUUID());
        order.add(orderLine);
        order.setCalendar(configurationDAO.getConfiguration()
                .getDefaultCalendar());
        try {
            orderDAO.save(order);
            sessionFactory.getCurrentSession().flush();
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        return orderLine;
    }

    @Test
    public void testGetTasksByOrder() {
        Task task = createValidTask();
        List<Task> tasks = orderDAO.getTasksByOrder(order);

        assertThat(tasks.size(), equalTo(1));
        assertThat(tasks.get(0).getId(), equalTo(task.getId()));
    }

}
