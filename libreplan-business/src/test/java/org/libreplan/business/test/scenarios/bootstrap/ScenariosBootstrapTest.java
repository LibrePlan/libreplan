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

import java.util.Set;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.scenarios.bootstrap.IScenariosBootstrap;
import org.libreplan.business.scenarios.bootstrap.PredefinedScenarios;
import org.libreplan.business.scenarios.daos.IScenarioDAO;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.test.scenarios.daos.ScenarioDAOTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class ScenariosBootstrapTest {

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Before
    public void loadRequiredaData() {
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
        configurationBootstrap.loadRequiredData();
    }

    private void removeCurrentScenarios() {
        for (Scenario scenario : scenarioDAO.getAll()) {
            try {
                scenarioDAO.remove(scenario.getId());
            } catch (InstanceNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Order givenOrderStored() {
        return ScenarioDAOTest.createOrderStored(orderDAO, configurationDAO);
    }

    @Test
    @Ignore("FIXME pending review after rename to libreplan")
    public void loadBasicData() throws InstanceNotFoundException {
        removeCurrentScenarios();
        scenariosBootstrap.loadRequiredData();

        assertFalse(scenarioDAO.getAll().isEmpty());
        assertNotNull(scenarioDAO.findByName(PredefinedScenarios.MASTER
                .getName()));
    }

    @Test
    @NotTransactional
    @Ignore("FIXME pending review after rename to libreplan")
    public void loadBasicDataAssociatedWithCurrentOrders()
            throws InstanceNotFoundException {
        final Order orderAssociated = transactionService
                .runOnAnotherTransaction(new IOnTransaction<Order>() {

            @Override
            public Order execute() {
                removeCurrentScenarios();
                Order order = givenOrderStored();
                scenariosBootstrap.loadRequiredData();
                return order;
            }
        });
        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                assertFalse(scenarioDAO.getAll().isEmpty());
                Scenario scenario = PredefinedScenarios.MASTER.getScenario();
                assertNotNull(scenario);
                assertTrue(isAt(orderAssociated, scenario.getTrackedOrders()));
                return null;
            }

            private boolean isAt(Order orderAssociated, Set<Order> trackedOrders) {
                for (Order each : trackedOrders) {
                    if (each.getId().equals(orderAssociated.getId())) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

}
