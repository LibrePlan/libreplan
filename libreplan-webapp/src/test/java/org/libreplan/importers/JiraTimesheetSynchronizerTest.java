/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workreports.daos.IWorkReportDAO;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.business.workreports.entities.WorkReportType;
import org.libreplan.business.workreports.valueobjects.DescriptionField;
import org.libreplan.business.workreports.valueobjects.DescriptionValue;
import org.libreplan.importers.jira.Issue;
import org.libreplan.importers.jira.WorkLog;
import org.libreplan.importers.jira.WorkLogItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test for {@link JiraTimesheetSynchronizer}
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class JiraTimesheetSynchronizerTest {

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

    private static final String PATH = "rest/api/latest/search";
    private static final String LABEL = "labels=epd_12a_ZorgActiviteiten";

    private List<Issue> issues;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IWorkReportDAO workReportDAO;

    private TypeOfWorkHours typeOfWorkHours;

    private WorkReportType workReportType;


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
                    properties.getProperty("password"), PATH, LABEL);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
            String code = "JIRA-" + order.getCode() + "-" + issue.getKey();
            String name = issue.getFields().getSummary();

            OrderLine orderLine = OrderLine
                    .createOrderLineWithUnfixedPercentage(1000);
            orderLine.useSchedulingDataFor(version);
            order.add(orderLine);
            orderLine.setCode(code);
            orderLine.setName(name);

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

    private WorkReport getOrCreateWorkReport(String code) {
        WorkReport workReport = findWorkReport(code);
        if (workReport == null) {
            workReport = WorkReport.create(workReportType);
            workReport.setCode(code);
        }
        return workReport;
    }


    private void updateOrCreateWorkReportLineAndAddToWorkReport(
            WorkReport workReport, OrderElement orderElement,
            List<WorkLogItem> workLogItems) {

        for (WorkLogItem workLogItem : workLogItems) {
            WorkReportLine workReportLine;
            try {
                workReportLine = workReport
                        .getWorkReportLineByCode(orderElement.getCode() + "-"
                                + workLogItem.getId());
            } catch (InstanceNotFoundException e) {
                workReportLine = WorkReportLine.create(workReport);
            }

            org.libreplan.business.resources.entities.Resource resource = createAndGetWorker(workLogItem
                    .getAuthor().getName());
            if (resource != null) {

                updateWorkReportLine(workReportLine, orderElement, workLogItem,
                        resource);
                if (workReportLine.isNewObject()) {
                    workReport.addWorkReportLine(workReportLine);
                }
            }
        }

    }

    private void updateWorkReportLine(WorkReportLine workReportLine,
            OrderElement orderElement, WorkLogItem workLogItem,
            org.libreplan.business.resources.entities.Resource resource) {

        String code = orderElement.getCode() + "-" + workLogItem.getId();
        int timeSpent = workLogItem.getTimeSpentSeconds().intValue();

        workReportLine.setCode(code);
        workReportLine.setDate(workLogItem.getStarted());
        workReportLine.setResource(resource);
        workReportLine.setOrderElement(orderElement);
        workReportLine.setEffort(EffortDuration
                .hours(EffortDuration.Granularity.HOURS
                        .convertFromSeconds(timeSpent)));
        workReportLine.setTypeOfWorkHours(typeOfWorkHours);

        updateOrCreateDescriptionValuesAndAddToWorkReportLine(workReportLine,
                workLogItem.getComment());
    }

    private void updateOrCreateDescriptionValuesAndAddToWorkReportLine(
            WorkReportLine workReportLine, String comment) {
        Set<DescriptionValue> descriptionValues = new HashSet<DescriptionValue>();
        for (DescriptionField descriptionField : workReportType.getLineFields()) {
            DescriptionValue descriptionValue;
            try {
                descriptionValue = workReportLine
                        .getDescriptionValueByFieldName(descriptionField
                                .getFieldName());
                descriptionValue.setValue(comment.substring(0,
                        Math.min(comment.length(), 254)));
            } catch (InstanceNotFoundException e) {
                descriptionValue = DescriptionValue.create(
                        descriptionField.getFieldName(), comment);
            }
            descriptionValues.add(descriptionValue);
        }
        workReportLine.setDescriptionValues(descriptionValues);
    }

    private WorkReport findWorkReport(String code) {
        try {
            return workReportDAO.findByCodeAnotherTransaction(code);
        } catch (InstanceNotFoundException e) {
        }
        return null;
    }

    private WorkReportType createWorkReportType(String name) {
        WorkReportType workReportType = WorkReportType.create();
        workReportType.setName("Jira-connector");
        workReportType.setCodeAutogenerated(true);
        return workReportType;
    }

    private TypeOfWorkHours createTypeOfWorkHours(String name) {
        typeOfWorkHours = TypeOfWorkHours.create();
        typeOfWorkHours.setName("Default");
        typeOfWorkHours.setCodeAutogenerated(true);
        return typeOfWorkHours;
    }

    private Worker createAndGetWorker(String nif) {
        Worker worker = Worker.create(nif, nif, nif);
        return worker;
    }

    @Test
    public void testSyncJiraTimesheet() {

        workReportType = createWorkReportType("Jira-connector");
        typeOfWorkHours = createTypeOfWorkHours("Default");

        Order order = givenOrder();

        String code = order.getCode();

        WorkReport workReport = getOrCreateWorkReport(code);

        for (Issue issue : issues) {
            WorkLog worklog = issue.getFields().getWorklog();
            if (worklog != null) {
                List<WorkLogItem> workLogItems = worklog.getWorklogs();
                if (workLogItems != null && !workLogItems.isEmpty()) {

                    String code1 = "JIRA-" + order.getCode() + "-"
                            + issue.getKey();

                    OrderElement orderElement = order.getOrderElement(code1);

                    if (orderElement != null) {
                        updateOrCreateWorkReportLineAndAddToWorkReport(
                                workReport, orderElement, workLogItems);

                    }
                }
            }
        }
        assertTrue(workReport.getWorkReportLines().size() > 0);
    }

}
