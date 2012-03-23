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
package org.libreplan.business.test.planner.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.costcategories.daos.ICostCategoryDAO;
import org.libreplan.business.costcategories.daos.ITypeOfWorkHoursDAO;
import org.libreplan.business.costcategories.entities.CostCategory;
import org.libreplan.business.costcategories.entities.HourCost;
import org.libreplan.business.costcategories.entities.ResourcesCostCategoryAssignment;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.orders.entities.OrderLineGroup;
import org.libreplan.business.planner.entities.IMoneyCostCalculator;
import org.libreplan.business.planner.entities.MoneyCostCalculator;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.daos.IOrderVersionDAO;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workreports.daos.IWorkReportDAO;
import org.libreplan.business.workreports.daos.IWorkReportTypeDAO;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.business.workreports.entities.WorkReportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test for {@link MoneyCostCalculator}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class MoneyCostCalculatorTest {

    @javax.annotation.Resource
    private IDataBootstrap scenariosBootstrap;

    @Before
    public void loadRequiredaData() {
        scenariosBootstrap.loadRequiredData();
    }

    @Autowired
    private IMoneyCostCalculator moneyCostCalculator;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IWorkReportDAO workReportDAO;

    @Autowired
    private IWorkReportTypeDAO workReportTypeDAO;

    @Autowired
    private ICostCategoryDAO costCategoryDAO;

    @Autowired
    private ITypeOfWorkHoursDAO typeOfWorkHoursDAO;

    @Autowired
    private IOrderVersionDAO orderVersionDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    private TypeOfWorkHours typeOfWorkHours;
    private CostCategory costCategory;
    private Resource resource;
    private List<OrderElement> orderElements = new ArrayList<OrderElement>();
    private WorkReportType workReportType;
    private WorkReport workReport;

    private void givenTypeOfWorkHours() {
        typeOfWorkHours = TypeOfWorkHours.createUnvalidated(
                "default-type-of-work-hours", "default-type-of-work-hours",
                true, new BigDecimal(30));
        typeOfWorkHoursDAO.save(typeOfWorkHours);
    }

    private void givenCostCategory() {
        costCategory = CostCategory.createUnvalidated("default-cost-category",
                "default-cost-category", true);
        HourCost hourCost = HourCost.createUnvalidated(
                "default-hour-cost", new BigDecimal(50), new LocalDate());
        hourCost.setType(typeOfWorkHours);
        costCategory.addHourCost(hourCost);
        costCategoryDAO.save(costCategory);
    }

    private void givenResource(boolean relatedWithCostCategory) {
        resource = Worker.createUnvalidated("default-resource",
                "default-resource", "default-resource", "default-resource");

        if (relatedWithCostCategory) {
            ResourcesCostCategoryAssignment resourcesCostCategoryAssignment = ResourcesCostCategoryAssignment
                    .create();
            resourcesCostCategoryAssignment
                    .setCode("resources-cost-category-assignment");
            resourcesCostCategoryAssignment.setCostCategory(costCategory);
            resourcesCostCategoryAssignment.setInitDate(new LocalDate());

            resource.addResourcesCostCategoryAssignment(resourcesCostCategoryAssignment);
        }
        resourceDAO.save(resource);
    }

    private void givenOrderElement() {
        OrderElement orderElement = OrderLine.createOrderLineWithUnfixedPercentage(100);
        orderElement.setCode("default-order-element-" + UUID.randomUUID());
        orderElement.setName("default-order-element-" + UUID.randomUUID());
        orderElement.getHoursGroups().get(0)
                .setCode("default-hours-group-" + UUID.randomUUID());
        orderElementDAO.save(orderElement);

        orderElements.add(orderElement);
    }

    private void givenOrderLineGroupWithTwoLines() {
        OrderLineGroup orderLineGroup = OrderLineGroup.create();
        orderLineGroup.setCode("default-order-line-group-" + UUID.randomUUID());
        orderLineGroup.setName("default-order-line-group-" + UUID.randomUUID());

        OrderVersion orderVersion = OrderVersion
                .createInitialVersion(scenarioManager.getCurrent());
        orderVersionDAO.save(orderVersion);
        orderLineGroup.useSchedulingDataFor(orderVersion);

        OrderLine orderLine1 = OrderLine
                .createOrderLineWithUnfixedPercentage(100);
        orderLine1.setCode("order-line-1-" + UUID.randomUUID());
        orderLine1.setName("order-line-1-" + UUID.randomUUID());
        orderLine1.getHoursGroups().get(0)
                .setCode("hours-group-1-" + UUID.randomUUID());
        orderLineGroup.add(orderLine1);

        OrderLine orderLine2 = OrderLine
                .createOrderLineWithUnfixedPercentage(100);
        orderLine2.setCode("order-line-2-" + UUID.randomUUID());
        orderLine2.setName("order-line-2-" + UUID.randomUUID());
        orderLine2.getHoursGroups().get(0)
                .setCode("hours-group-2-" + UUID.randomUUID());
        orderLineGroup.add(orderLine2);

        orderElementDAO.save(orderLineGroup);

        orderElements.add(orderLineGroup);
        orderElements.add(orderLine1);
        orderElements.add(orderLine2);
    }

    private void giveWorkReportType() {
        workReportType = WorkReportType.create("default-work-report-type",
                "default-work-report-type");
        workReportTypeDAO.save(workReportType);
    }

    private void givenWorkReport() {
        givenWorkReport(null);
    }

    private void givenWorkReport(List<Integer> hoursList) {
        workReport = WorkReport.create(workReportType);
        workReport.setCode("default-work-report");

        for (OrderElement each : orderElements) {
            int hours = 10;
            if (hoursList != null) {
                hours = hoursList.get(orderElements.indexOf(each));
            }
            workReport.addWorkReportLine(createWorkReportLine(each, hours));
        }

        workReportDAO.save(workReport);
    }

    private WorkReportLine createWorkReportLine(OrderElement orderElement,
            Integer hours) {
        WorkReportLine workReportLine = WorkReportLine.create(workReport);
        workReportLine.setCode("default-work-report-line-" + UUID.randomUUID());
        workReportLine.setDate(new Date());
        workReportLine.setResource(resource);
        workReportLine.setOrderElement(orderElement);
        workReportLine.setTypeOfWorkHours(typeOfWorkHours);
        workReportLine.setEffort(EffortDuration.hours(hours));
        return workReportLine;
    }

    private void givenBasicExample() {
        givenTypeOfWorkHours();
        givenCostCategory();
        givenResource(true);
        givenOrderElement();
        giveWorkReportType();
        givenWorkReport();
    }

    private void givenBasicExampleWithoutCostCategoryRelationship() {
        givenTypeOfWorkHours();
        givenResource(false);
        givenOrderElement();
        giveWorkReportType();
        givenWorkReport();
    }

    private void givenExampleOrderLineGroup() {
        givenTypeOfWorkHours();
        givenCostCategory();
        givenResource(true);
        givenOrderLineGroupWithTwoLines();
        giveWorkReportType();
        givenWorkReport();
    }

    private void givenExampleOrderLineGroupWithDifferentHours(
            List<Integer> hoursList) {
        givenTypeOfWorkHours();
        givenCostCategory();
        givenResource(true);
        givenOrderLineGroupWithTwoLines();
        giveWorkReportType();
        givenWorkReport(hoursList);
    }

    @Test
    public void basicTest() throws InstanceNotFoundException {
        givenBasicExample();
        assertThat(moneyCostCalculator.getMoneyCost(orderElements.get(0)),
                equalTo(new BigDecimal(500).setScale(2)));
    }

    @Test
    public void basicTestWithoutCostCategoryRelationship()
            throws InstanceNotFoundException {
        givenBasicExampleWithoutCostCategoryRelationship();
        assertThat(moneyCostCalculator.getMoneyCost(orderElements.get(0)),
                equalTo(new BigDecimal(300).setScale(2)));
    }

    @Test
    public void exampleOrderLineGroup() throws InstanceNotFoundException {
        givenExampleOrderLineGroup();
        assertThat(moneyCostCalculator.getMoneyCost(orderElements.get(0)),
                equalTo(new BigDecimal(1500).setScale(2)));
        assertThat(moneyCostCalculator.getMoneyCost(orderElements.get(1)),
                equalTo(new BigDecimal(500).setScale(2)));
        assertThat(moneyCostCalculator.getMoneyCost(orderElements.get(2)),
                equalTo(new BigDecimal(500).setScale(2)));
    }

    @Test
    public void exampleOrderLineGroupWithDifferentHours1()
            throws InstanceNotFoundException {
        givenExampleOrderLineGroupWithDifferentHours(Arrays.asList(0, 10, 5));
        assertThat(moneyCostCalculator.getMoneyCost(orderElements.get(0)),
                equalTo(new BigDecimal(750).setScale(2)));
        assertThat(moneyCostCalculator.getMoneyCost(orderElements.get(1)),
                equalTo(new BigDecimal(500).setScale(2)));
        assertThat(moneyCostCalculator.getMoneyCost(orderElements.get(2)),
                equalTo(new BigDecimal(250).setScale(2)));
    }

    @Test
    public void exampleOrderLineGroupWithDifferentHours2()
            throws InstanceNotFoundException {
        givenExampleOrderLineGroupWithDifferentHours(Arrays.asList(6, 0, 0));
        assertThat(moneyCostCalculator.getMoneyCost(orderElements.get(0)),
                equalTo(new BigDecimal(300).setScale(2)));
        assertThat(moneyCostCalculator.getMoneyCost(orderElements.get(1)),
                equalTo(new BigDecimal(0).setScale(2)));
        assertThat(moneyCostCalculator.getMoneyCost(orderElements.get(2)),
                equalTo(new BigDecimal(0).setScale(2)));
    }

    @Test
    public void exampleOrderLineGroupWithDifferentHours3()
            throws InstanceNotFoundException {
        givenExampleOrderLineGroupWithDifferentHours(Arrays.asList(6, 5, 10));
        assertThat(moneyCostCalculator.getMoneyCost(orderElements.get(0)),
                equalTo(new BigDecimal(1050).setScale(2)));
        assertThat(moneyCostCalculator.getMoneyCost(orderElements.get(1)),
                equalTo(new BigDecimal(250).setScale(2)));
        assertThat(moneyCostCalculator.getMoneyCost(orderElements.get(2)),
                equalTo(new BigDecimal(500).setScale(2)));
    }

}
