/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.scenarios;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.scenarios.bootstrap.PredefinedScenarios;
import org.navalplanner.business.scenarios.daos.IOrderVersionDAO;
import org.navalplanner.business.scenarios.daos.IScenarioDAO;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link ScenarioModel}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class ScenarioModelTest {

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Resource
    private IDataBootstrap scenariosBootstrap;

    @Before
    public void loadRequiredaData() {
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
        configurationBootstrap.loadRequiredData();
        scenariosBootstrap.loadRequiredData();
    }

    @Autowired
    private IScenarioModel scenarioModel;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IOrderVersionDAO orderVersionDAO;

    @Autowired
    private SessionFactory sessionFactory;

    private Order givenStoredOrderInDefaultScenario() {
        Scenario defaultScenario = transactionService
                .runOnAnotherReadOnlyTransaction(new IOnTransaction<Scenario>() {

                    @Override
                    public Scenario execute() {
                        return PredefinedScenarios.MASTER.getScenario();
                    }
                });
        return givenStoredOrderInScenario(defaultScenario);
    }

    public static Order givenStoredOrderInScenario(Scenario scenario,
            IConfigurationDAO configurationDAO, IOrderDAO orderDAO,
            SessionFactory sessionFactory) {
        Order order = Order.create();
        order.setCode(UUID.randomUUID().toString());
        order.setName("order-name");
        order.setInitDate(new Date());
        order.setCalendar(configurationDAO.getConfiguration()
                .getDefaultCalendar());

        OrderVersion orderVersion = scenario.addOrder(order);
        order.setVersionForScenario(scenario, orderVersion);
        order.useSchedulingDataFor(orderVersion);
        OrderLine orderLine = OrderLine
                .createOrderLineWithUnfixedPercentage(1000);
        order.add(orderLine);
        orderLine.setCode(UUID.randomUUID().toString());
        orderLine.setName("order-line-name");
        orderDAO.save(order);
        orderDAO.flush();
        sessionFactory.getCurrentSession().evict(order);
        order.dontPoseAsTransientObjectAnymore();

        return order;
    }

    private Order givenStoredOrderInScenario(Scenario scenario) {
        return givenStoredOrderInScenario(scenario, configurationDAO, orderDAO,
                sessionFactory);
    }

    private Scenario givenStoredScenario() {
        Scenario defaultScenario = PredefinedScenarios.MASTER.getScenario();
        return givenStoredScenario(defaultScenario);
    }

    public static Scenario givenStoredScenario(Scenario predecessor,
            IScenarioDAO scenarioDAO, SessionFactory sessionFactory) {
        Scenario scenario = predecessor.newDerivedScenario();
        scenario.setName("scenario-name-" + UUID.randomUUID());

        scenarioDAO.save(scenario);
        scenarioDAO.flush();
        sessionFactory.getCurrentSession().evict(scenario);
        scenario.dontPoseAsTransientObjectAnymore();

        return scenario;
    }

    private Scenario givenStoredScenario(Scenario predecessor) {
        return givenStoredScenario(predecessor, scenarioDAO, sessionFactory);
    }

    @Test
    @Rollback(false)
    public void testNotRollback() {
        // Just to do not make rollback in order to have the default scenario
    }

    @Test
    public void testCreateAndSaveScenarioWithoutOrders() {
        int previous = scenarioModel.getScenarios().size();

        Scenario defaultScenario = PredefinedScenarios.MASTER.getScenario();
        scenarioModel.initCreateDerived(defaultScenario);

        Scenario newScenario = scenarioModel.getScenario();
        newScenario.setName("scenario-name-" + UUID.randomUUID());

        scenarioModel.confirmSave();
        assertThat(scenarioModel.getScenarios().size(), equalTo(previous + 1));
        assertThat(scenarioModel.getScenarios().get(previous).getId(),
                equalTo(newScenario.getId()));
    }

    @Test
    public void testCreateAndSaveScenarioWithOrders() {
        Order order = givenStoredOrderInDefaultScenario();

        int previous = scenarioModel.getScenarios().size();

        Scenario defaultScenario = PredefinedScenarios.MASTER.getScenario();
        scenarioModel.initCreateDerived(defaultScenario);

        Scenario newScenario = scenarioModel.getScenario();
        newScenario.setName("scenario-name-" + UUID.randomUUID());

        scenarioModel.confirmSave();
        assertThat(scenarioModel.getScenarios().size(), equalTo(previous + 1));
        Scenario newScenarioSaved = scenarioModel.getScenarios().get(previous);
        assertThat(newScenarioSaved.getId(), equalTo(newScenario.getId()));
        assertThat(newScenarioSaved.getOrders().size(), equalTo(defaultScenario
                .getOrders().size()));
        assertNotNull(newScenarioSaved.getOrders().get(order));
        assertThat(newScenarioSaved.getOrders().get(order).getOwnerScenario()
                .getId(), equalTo(defaultScenario.getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveDefaultScenario() {
        Scenario defaultScenario = PredefinedScenarios.MASTER.getScenario();
        scenarioModel.remove(defaultScenario);
    }

    @Test
    public void testRemoveScenarioWithoutOrders() {
        Scenario scenario = givenStoredScenario();

        int previous = scenarioModel.getScenarios().size();

        scenarioModel.remove(scenario, false);
        assertThat(scenarioModel.getScenarios().size(), equalTo(previous - 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveScenarioWithDerivedScenraios() {
        Scenario scenario = givenStoredScenario();
        givenStoredScenario(scenario);
        scenarioModel.remove(scenario);
    }

    @Test
    public void testRemoveScenarioWithOrders() throws InstanceNotFoundException {
        Order order = givenStoredOrderInDefaultScenario();
        Scenario scenario = givenStoredScenario();

        int previous = scenarioModel.getScenarios().size();

        OrderVersion orderVersion = scenario.getOrderVersion(order);

        scenarioModel.remove(scenario, false);
        assertThat(scenarioModel.getScenarios().size(), equalTo(previous - 1));

        assertNotNull(orderDAO.find(order.getId()));
        assertNotNull(orderVersionDAO.find(orderVersion.getId()));
    }

    @Test
    public void testRemoveScenarioWithOrdersJustInThisScenario()
            throws InstanceNotFoundException {
        Scenario scenario = givenStoredScenario();
        Order order = givenStoredOrderInScenario(scenario);

        int previous = scenarioModel.getScenarios().size();

        // Reload scenario information
        sessionFactory.getCurrentSession().evict(scenario);
        scenario = scenarioDAO.find(scenario.getId());

        OrderVersion orderVersion = scenario.getOrderVersion(order);

        scenarioModel.remove(scenario, false);
        assertThat(scenarioModel.getScenarios().size(), equalTo(previous - 1));

        try {
            orderDAO.find(order.getId());
            fail("Order should be removed");
        } catch (InstanceNotFoundException e) {
            // Ok
        }

        try {
            orderVersionDAO.find(orderVersion.getId());
            fail("Order version should be removed");
        } catch (InstanceNotFoundException e) {
            // Ok
        }
    }

}
