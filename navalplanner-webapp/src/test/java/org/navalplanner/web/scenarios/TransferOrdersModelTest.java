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

package org.navalplanner.web.scenarios;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

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
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.scenarios.bootstrap.PredefinedScenarios;
import org.navalplanner.business.scenarios.daos.IScenarioDAO;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link TransferOrdersModel}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class TransferOrdersModelTest {

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
    private ITransferOrdersModel transferOrdersModel;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IAdHocTransactionService transactionService;

    private Order givenStoredOrderInScenario() {
        Scenario defaultScenario = transactionService
                .runOnAnotherReadOnlyTransaction(new IOnTransaction<Scenario>() {

                    @Override
                    public Scenario execute() {
                        return PredefinedScenarios.MASTER.getScenario();
                    }
                });
        return givenStoredOrderInScenario(defaultScenario);
    }

    private Order givenStoredOrderInScenario(Scenario scenario) {
        return ScenarioModelTest.givenStoredOrderInScenario(scenario,
                configurationDAO, orderDAO, sessionFactory);
    }

    private Scenario givenStoredScenario() {
        Scenario defaultScenario = PredefinedScenarios.MASTER.getScenario();
        return givenStoredScenario(defaultScenario);
    }

    private Scenario givenStoredScenario(Scenario predecessor) {
        return ScenarioModelTest.givenStoredScenario(predecessor, scenarioDAO,
                sessionFactory);
    }

    @Test
    @NotTransactional
    public void testBasicTransferOrder() {
        final int numOrders = transactionService
                .runOnReadOnlyTransaction(new IOnTransaction<Integer>() {
                    @Override
                    public Integer execute() {
                        return orderDAO.getOrders().size();
                    }
                });

        final Object[] objects = transactionService
                .runOnTransaction(new IOnTransaction<Object[]>() {
                    @Override
                    public Object[] execute() {
                        Scenario source = givenStoredScenario();
                        Scenario destination = givenStoredScenario();

                        Order order = givenStoredOrderInScenario(source);
                        return new Object[] { source.getId(),
                                destination.getId(), order };
                    }
                });
        final Order orderAtSource = (Order) objects[2];
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    Order order = orderDAO.findExistingEntity(orderAtSource
                            .getId());
                    Scenario source = scenarioDAO.find((Long) objects[0]);
                    Scenario destination = scenarioDAO.find((Long) objects[1]);

                    assertThat(source.getOrders().size(),
                            equalTo(numOrders + 1));
                    assertThat(destination.getOrders().size(),
                            equalTo(numOrders));

                    transferOrdersModel.getScenarios();

                    transferOrdersModel.setSourceScenario(source);
                    transferOrdersModel.setDestinationScenario(destination);

                    transferOrdersModel.transfer(order);

                    return null;
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    Scenario source = scenarioDAO.find((Long) objects[0]);
                    Scenario destination = scenarioDAO.find((Long) objects[1]);

                    assertThat(source.getOrders().size(),
                            equalTo(numOrders + 1));
                    assertThat(destination.getOrders().size(),
                            equalTo(numOrders + 1));
                    assertNotNull(destination
                            .getOrderVersion((Order) objects[2]));

                    return null;
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Test(expected = ValidationException.class)
    @NotTransactional
    public void testTransferOrderWithTheSameVersion() {
        final Order order = transactionService
                .runOnTransaction(new IOnTransaction<Order>() {
                    @Override
                    public Order execute() {
                        return givenStoredOrderInScenario();
                    }
                });

        final Scenario source = transactionService
                .runOnReadOnlyTransaction(new IOnTransaction<Scenario>() {
                    @Override
                    public Scenario execute() {
                        return PredefinedScenarios.MASTER.getScenario();
                    }
                });

        final Scenario destination = transactionService
                .runOnTransaction(new IOnTransaction<Scenario>() {
                    @Override
                    public Scenario execute() {
                        return givenStoredScenario();
                    }
                });

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                // reload order so it has the relationship with destination
                Order orderReloaded = orderDAO.findExistingEntity(order.getId());
                transferOrdersModel.setSourceScenario(source);
                transferOrdersModel.setDestinationScenario(destination);

                transferOrdersModel.transfer(orderReloaded);
                return null;
            }
        });
    }

}
