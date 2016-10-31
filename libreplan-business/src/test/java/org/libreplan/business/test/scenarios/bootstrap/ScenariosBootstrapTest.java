/*
 * This file is part of LibrePlan
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

package org.libreplan.business.test.scenarios.bootstrap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.planner.daos.ITaskSourceDAO;
import org.libreplan.business.scenarios.bootstrap.PredefinedScenarios;
import org.libreplan.business.scenarios.daos.IOrderVersionDAO;
import org.libreplan.business.scenarios.daos.IScenarioDAO;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.scenarios.entities.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE, BUSINESS_SPRING_CONFIG_TEST_FILE })

// Needed to clear context after testClass
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)

public class ScenariosBootstrapTest {

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IOrderVersionDAO orderVersionDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Autowired
    SessionFactory sessionFactory;

    public void loadRequiredData() {

        defaultAdvanceTypesBootstrapListener.loadRequiredData();
        configurationBootstrap.loadRequiredData();

        try {

            for (TaskSource source: taskSourceDAO.list(TaskSource.class)) {
                taskSourceDAO.remove(source.getId());
            }

            Session session = sessionFactory.getCurrentSession();

            Query deleteSchedulingStatesByOrderVersion =
                    session.createSQLQuery("DELETE FROM scheduling_states_by_order_version");

            deleteSchedulingStatesByOrderVersion.executeUpdate();
            session.flush();

            Query deleteSchedulingDataForVersion = session.createSQLQuery("DELETE FROM scheduling_data_for_version");

            deleteSchedulingDataForVersion.executeUpdate();
            session.flush();

            for (Order order : orderDAO.findAll()) {
                orderDAO.remove(order.getId());
            }

            Scenario masterScenario = null;
            try {
                masterScenario = PredefinedScenarios.MASTER.getScenario();
            } catch (RuntimeException ignored) {
            }

            if ( masterScenario != null ) {
                for (Scenario scenario : scenarioDAO.getAllExcept(masterScenario)) {
                    scenarioDAO.remove(scenario.getId());
                }

                for (OrderVersion orderVersion : orderVersionDAO.list(OrderVersion.class)) {
                    masterScenario.removeVersion(orderVersion);
                }
                session.flush();
            }

            for (OrderVersion orderVersion : orderVersionDAO.list(OrderVersion.class)) {
                orderVersionDAO.remove(orderVersion.getId());
            }
            session.flush();

        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private Order givenOrderStored() {
        return createOrderStored(orderDAO, configurationDAO);
    }

    public Order createOrderStored(IOrderDAO orderDAO, IConfigurationDAO configurationDAO) {
        Order order = Order.create();
        order.setInitDate(new Date());
        order.setName("name-" + UUID.randomUUID().toString());
        order.setCode("code-" + UUID.randomUUID().toString());
        order.setCalendar(configurationDAO.getConfiguration().getDefaultCalendar());
        orderDAO.save(order);

        return order;
    }

    @Test
    @Transactional
    public void loadBasicData() throws InstanceNotFoundException {
        assertFalse(scenarioDAO.getAll().isEmpty());
        assertNotNull(scenarioDAO.findByName(PredefinedScenarios.MASTER.getName()));
    }

    @Test
    @Transactional
    public void loadBasicDataAssociatedWithCurrentOrders() throws InstanceNotFoundException {
        loadRequiredData();
        assertFalse(scenarioDAO.getAll().isEmpty());
        Scenario scenario = PredefinedScenarios.MASTER.getScenario();
        assertNotNull(scenario);

        Order orderAssociated = givenOrderStored();
        scenario.addOrder(orderAssociated);
        scenarioDAO.save(scenario);

        assertTrue(isAt(orderAssociated, scenario.getTrackedOrders()));
    }

    private boolean isAt(Order orderAssociated, Set<Order> trackedOrders) {
        for (Order each : trackedOrders) {
            if (each.getId().equals(orderAssociated.getId())) {
                return true;
            }
        }
        return false;
    }

}
