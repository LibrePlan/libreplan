/*
 * This file is part of NavalPlan
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
package org.navalplanner.business.test.scenarios.daos;

import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.scenarios.daos.IScenarioDAO;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class ScenarioDAOTest {

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Resource
    private IDataBootstrap configurationBootstrap;

    public static Order createOrderStored(IOrderDAO orderDAO,
            IConfigurationDAO configurationDAO) {
        Order order = Order.create();
        order.setInitDate(new Date());
        order.setName("name-" + UUID.randomUUID().toString());
        order.setCode("code-" + UUID.randomUUID().toString());
        order.setCalendar(configurationDAO.getConfiguration()
                .getDefaultCalendar());
        orderDAO.save(order);
        return order;
    }

    private Order givenOrderStored() {
        return ScenarioDAOTest.createOrderStored(orderDAO, configurationDAO);
    }

    private Scenario createNewScenario() {
        return Scenario.create(UUID.randomUUID().toString());
    }

    @Before
    public void loadRequiredaData() {
        configurationBootstrap.loadRequiredData();
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
    }

    @Test
    @NotTransactional
    public void afterSavingScenarioWithOrderNewlyRetrievedOrderHasScenariosInfo() {
        final Scenario scenario = createNewScenario();
        final Long orderId = transactionService
                .runOnTransaction(new IOnTransaction<Long>() {

            @Override
            public Long execute() {
                Order order = givenOrderStored();
                scenario.addOrder(order);
                scenarioDAO.save(scenario);
                return order.getId();
            }
        });

        transactionService.runOnTransaction(new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                Order order = orderDAO.findExistingEntity(orderId);
                Map<Scenario, OrderVersion> scenarios = order.getScenarios();
                assertTrue(isAt(scenario, scenarios.keySet()));
                return null;
            }

            private boolean isAt(Scenario scenario, Set<Scenario> scenarios) {
                for (Scenario each : scenarios) {
                    if (scenario.getId().equals(each.getId())) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

}
