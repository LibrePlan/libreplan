/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

package org.libreplan.importers;

import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.entities.ConnectorException;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.workreports.entities.IWorkReportTypeBootstrap;
import org.libreplan.importers.jira.IssueDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test for {@link JiraTimesheetSynchronizer}.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE, WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
public class JiraTimesheetSynchronizerTest {

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Resource
    private IDataBootstrap scenariosBootstrap;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Resource
    private IWorkReportTypeBootstrap workReportTypeBootstrap;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    private List<IssueDTO> issues;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IJiraOrderElementSynchronizer jiraOrderElementSynchronizer;

    @Autowired
    private IJiraTimesheetSynchronizer jiraTimesheetSynchronizer;

    @Before
    public void loadRequiredData() {

        IOnTransaction<Void> load = new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                defaultAdvanceTypesBootstrapListener.loadRequiredData();
                configurationBootstrap.loadRequiredData();
                scenariosBootstrap.loadRequiredData();
                workReportTypeBootstrap.loadRequiredData();
                issues = getJiraIssues();
                return null;
            }
        };

        transactionService.runOnAnotherTransaction(load);
    }

    private List<IssueDTO> getJiraIssues() {
        List<IssueDTO> issues;
        try {
            Properties properties = loadProperties();

            issues = JiraRESTClient.getIssues(
                    properties.getProperty("url"),
                    properties.getProperty("username"),
                    properties.getProperty("password"),
                    JiraRESTClient.PATH_SEARCH,
                    getJiraLabel(properties.getProperty("label")));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return issues;
    }

    private Properties loadProperties() throws IOException {

        String filename = System.getProperty("user.dir") + "/../scripts/jira-connector/jira-conn.properties";

        Properties properties = new Properties();
        properties.load(new FileInputStream(filename));
        return properties;
    }

    private String getJiraLabel(String label) {
        return "labels=" + label;
    }

    private Order givenOrder() {
        return transactionService.runOnAnotherTransaction(new IOnTransaction<Order>() {
            @Override
            public Order execute() {
                return givenValidOrderAlreadyStored();
            }
        });
    }

    private Order givenValidOrderAlreadyStored() {
        Order order = Order.create();
        order.setCode(UUID.randomUUID().toString());
        order.setName("Order name " + UUID.randomUUID());
        order.setInitDate(new Date());
        order.setCalendar(configurationDAO.getConfiguration().getDefaultCalendar());
        OrderVersion version = setupVersionUsing(scenarioManager, order);
        order.useSchedulingDataFor(version);

        orderDAO.save(order);
        orderDAO.flush();
        try {
            return orderDAO.find(order.getId());
        } catch (InstanceNotFoundException e) {
            return null;
        }
    }

    private Order givenOrderWithValidOrderLines() {
        return transactionService.runOnAnotherTransaction(new IOnTransaction<Order>() {
            @Override
            public Order execute() {
                return givenValidOrderWithValidOrderLinesAlreadyStored();
            }
        });
    }


    private Order givenValidOrderWithValidOrderLinesAlreadyStored() {
        Order order = givenOrder();
        jiraOrderElementSynchronizer.syncOrderElementsWithJiraIssues(issues, order);
        order.dontPoseAsTransientObjectAnymore();
        orderDAO.saveWithoutValidating(order);
        orderDAO.flush();
        try {
            return orderDAO.find(order.getId());
        } catch (InstanceNotFoundException e) {
            return null;
        }
    }

    private OrderVersion setupVersionUsing(IScenarioManager scenarioManager, Order order) {
        Scenario current = scenarioManager.getCurrent();
        OrderVersion result = OrderVersion.createInitialVersion(current);
        order.setVersionForScenario(current, result);
        return result;
    }


    @Test
    @Transactional
    @Ignore("Only working if you have a JIRA server configured")
    public void testSyncJiraTimesheet() throws ConnectorException {

        Order order = givenOrderWithValidOrderLines();
        jiraTimesheetSynchronizer.syncJiraTimesheetWithJiraIssues(issues, order);
        assertTrue(order.getWorkReportLines(false).size() > 0);
    }

}
