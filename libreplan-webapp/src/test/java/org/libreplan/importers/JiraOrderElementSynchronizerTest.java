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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.Resource;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.advance.entities.AdvanceType;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.libreplan.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.entities.JiraConfiguration;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.importers.jira.Issue;
import org.libreplan.importers.jira.TimeTracking;
import org.libreplan.importers.jira.WorkLog;
import org.libreplan.importers.jira.WorkLogItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test for {@link JiraOrderElementSynchronizer }
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class JiraOrderElementSynchronizerTest {

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Resource
    private IDataBootstrap scenariosBootstrap;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    private static final String LABEL = "labels=epd_12a_ZorgActiviteiten";

    private List<Issue> issues;

    @Autowired
    private IOrderDAO orderDAO;


    @Before
    public void loadRequiredaData() {

        IOnTransaction<Void> load = new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                defaultAdvanceTypesBootstrapListener.loadRequiredData();
                configurationBootstrap.loadRequiredData();
                scenariosBootstrap.loadRequiredData();
                issues = getJiraIssues();
                return null;
            }
        };

        transactionService.runOnAnotherTransaction(load);
    }

    private List<Issue> getJiraIssues() {
        List<Issue> issues = new ArrayList<Issue>();
        try {
            Properties properties = loadProperties();
            issues = JiraRESTClient.getIssues(properties.getProperty("url"),
                    properties.getProperty("username"),
                    properties.getProperty("password"),
                    JiraRESTClient.PATH_SEARCH, LABEL);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return issues;
    }

    private Properties loadProperties() throws FileNotFoundException,
            IOException {

        String filename = System.getProperty("user.dir")
                + "/../scripts/jira-connector/jira-conn.properties";

        Properties properties = new Properties();
        properties.load(new FileInputStream(filename));
        return properties;

    }

    private Order givenOrder() {
        return transactionService
                .runOnAnotherTransaction(new IOnTransaction<Order>() {
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
        order.setCalendar(configurationDAO.getConfiguration()
                .getDefaultCalendar());
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
        return transactionService
                .runOnAnotherTransaction(new IOnTransaction<Order>() {
                    @Override
                    public Order execute() {
                        return givenValidOrderWithValidOrderLinesAlreadyStored();
                    }
                });
    }

    private Order givenValidOrderWithValidOrderLinesAlreadyStored() {
        Order order = Order.create();
        order.setCode(UUID.randomUUID().toString());
        order.setName("Order name " + UUID.randomUUID());
        order.setInitDate(new Date());
        order.setCalendar(configurationDAO.getConfiguration()
                .getDefaultCalendar());
        OrderVersion version = setupVersionUsing(scenarioManager, order);
        order.useSchedulingDataFor(version);
        for (Issue issue : issues) {
            String code = JiraConfiguration.CODE_PREFIX + order.getCode() + "-"
                    + issue.getKey();
            String name = issue.getFields().getSummary();

            syncOrderLine(order, code, name);

            int estimatedHours = getEstimatedHours(
                    issue.getFields().getTimetracking()).intValue();

            syncHoursGroup((OrderLine) order.getOrderElement(code), code,
                    estimatedHours);

            syncPorgressMeasurement(order.getOrderElement(code), issue);

        }
        orderDAO.save(order);
        orderDAO.flush();
        try {
            return orderDAO.find(order.getId());
        } catch (InstanceNotFoundException e) {
            return null;
        }
    }

    private OrderVersion setupVersionUsing(IScenarioManager scenarioManager,
            Order order) {
        Scenario current = scenarioManager.getCurrent();
        OrderVersion result = OrderVersion.createInitialVersion(current);
        order.setVersionForScenario(current, result);
        return result;
    }



    private void syncOrderLine(Order order, String code, String name) {
        OrderLine orderLine = (OrderLine) order.getOrderElement(code);
        if (orderLine == null) {
            orderLine = OrderLine.createOrderLineWithUnfixedPercentage(1000);
            order.add(orderLine);
            orderLine.setCode(code);
        }
        orderLine.setName(name);

    }

    private void syncHoursGroup(OrderLine orderLine, String code,
            Integer workingHours) {
        HoursGroup hoursGroup = orderLine.getHoursGroup(code);
        if (hoursGroup == null) {
            hoursGroup = HoursGroup.create(orderLine);
            hoursGroup.setCode(code);
            orderLine.addHoursGroup(hoursGroup);
        }

        hoursGroup.setWorkingHours(workingHours);

    }

    private void syncPorgressMeasurement(OrderElement orderElement, Issue issue) {

        WorkLog workLog = issue.getFields().getWorklog();

        if (workLog == null) {
            return;
        }
        if (orderElement == null) {
            return;
        }

        List<WorkLogItem> workLogItems = workLog.getWorklogs();
        if (workLogItems.isEmpty()) {
            return;
        }

        Integer estimatedHours = getEstimatedHours(issue.getFields()
                .getTimetracking());

        if (estimatedHours == 0) {
            return;
        }

        Integer loggedHours = getLoggedHours(issue.getFields()
                .getTimetracking());

        BigDecimal percentage = new BigDecimal((loggedHours * 100)
                / (loggedHours + estimatedHours));

        LocalDate latestWorkLogDate = new LocalDate();

        updateOrCreateProgressAssignmentAndMeasurement(orderElement,
                percentage, latestWorkLogDate);

    }

    private void updateOrCreateProgressAssignmentAndMeasurement(
            OrderElement orderElement, BigDecimal percentage,
            LocalDate latestWorkLogDate) {

        AdvanceType advanceType = PredefinedAdvancedTypes.PERCENTAGE.getType();

        DirectAdvanceAssignment directAdvanceAssignment = orderElement
                .getDirectAdvanceAssignmentByType(advanceType);
        if (directAdvanceAssignment == null) {
            directAdvanceAssignment = DirectAdvanceAssignment.create(false,
                    new BigDecimal(100));
            directAdvanceAssignment.setAdvanceType(advanceType);
        }
        directAdvanceAssignment.setOrderElement(orderElement);

        AdvanceMeasurement advanceMeasurement = directAdvanceAssignment
                .getAdvanceMeasurementAtExactDate(latestWorkLogDate);
        if (advanceMeasurement == null) {
            advanceMeasurement = AdvanceMeasurement.create();
        }

        advanceMeasurement.setValue(percentage);
        advanceMeasurement.setDate(latestWorkLogDate);

        directAdvanceAssignment.addAdvanceMeasurements(advanceMeasurement);

        advanceMeasurement.setAdvanceAssignment(directAdvanceAssignment);

        if (directAdvanceAssignment.isNewObject()) {
            try {
                directAdvanceAssignment.getOrderElement().addAdvanceAssignment(
                        directAdvanceAssignment);
            } catch (DuplicateValueTrueReportGlobalAdvanceException e) {
            } catch (DuplicateAdvanceAssignmentForOrderElementException e) {
            }
        }

    }

    private Integer getEstimatedHours(TimeTracking timeTracking) {
        if (timeTracking == null) {
            return 0;
        }

        Integer timeestimate = timeTracking.getRemainingEstimateSeconds();
        if (timeestimate != null && timeestimate > 0) {
            return timeestimate / 3600;
        }

        Integer timeoriginalestimate = timeTracking
                .getOriginalEstimateSeconds();
        if (timeoriginalestimate != null) {
            return timeoriginalestimate / 3600;
        }
        return 0;
    }

    private Integer getLoggedHours(TimeTracking timeTracking) {
        if (timeTracking == null) {
            return 0;
        }

        Integer timespentInSec = timeTracking.getTimeSpentSeconds();
        if (timespentInSec != null && timespentInSec > 0) {
            return timespentInSec / 3600;
        }

        return 0;
    }

    @Test
    @Ignore("Only working if you have a JIRA server configured")
    public void testSyncOrderElementsOfAnExistingOrderWithNoOrderLines() {
        Order order = givenOrder();
        for (Issue issue : issues) {
            String code = JiraConfiguration.CODE_PREFIX + order.getCode() + "-"
                    + issue.getKey();
            String name = issue.getFields().getSummary();

            syncOrderLine(order, code, name);

            syncHoursGroup((OrderLine) order.getOrderElement(code), code,
                    getEstimatedHours(issue.getFields().getTimetracking()));

            syncPorgressMeasurement(order.getOrderElement(code), issue);

        }
        assertEquals(order.getOrderElements().size(), issues.size());
        assertTrue(order.getOrderElements().get(0).getHoursGroups().size() > 0);
        assertTrue(!order.getAdvancePercentage().equals(BigDecimal.ZERO));
    }


    @Test
    @Ignore("Only working if you have a JIRA server configured")
    public void testReSyncOrderElementsOfAnExistingOrderWithOrderLines() {
        Order order = givenOrderWithValidOrderLines();
        Integer workingHours = order.getWorkHours();
        for (Issue issue : issues) {
            String code = JiraConfiguration.CODE_PREFIX + order.getCode() + "-"
                    + issue.getKey();
            String name = issue.getFields().getSummary();

            syncOrderLine(order, code, name);

            Integer estimatedHours = getEstimatedHours(issue.getFields()
                    .getTimetracking()) * 10;

            syncHoursGroup((OrderLine) order.getOrderElement(code), code,
                    estimatedHours);

            syncPorgressMeasurement(order.getOrderElement(code), issue);

        }
        assertEquals(order.getOrderElements().size(), issues.size());
        assertEquals(workingHours.intValue(),
                (order.getWorkHours().intValue() / 10));
    }

}
