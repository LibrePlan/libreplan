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
import java.util.Date;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.libreplan.business.planner.entities.IMoneyCostCalculator;
import org.libreplan.business.planner.entities.MoneyCostCalculator;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
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

    private TypeOfWorkHours typeOfWorkHours;
    private CostCategory costCategory;
    private Resource resource;
    private OrderElement orderElement;
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
        orderElement = OrderLine
                .createOrderLineWithUnfixedPercentage(100);
        orderElement.setCode("default-order-element");
        orderElement.setName("default-order-element");
        orderElement.getHoursGroups().get(0).setCode("default-hours-group");
        orderElementDAO.save(orderElement);
    }

    private void giveWorkReportType() {
        workReportType = WorkReportType.create("default-work-report-type",
                "default-work-report-type");
        workReportTypeDAO.save(workReportType);
    }

    private void givenWorkReport() {
        workReport = WorkReport.create(workReportType);
        workReport.setCode("default-work-report");

        WorkReportLine workReportLine = WorkReportLine.create(workReport);
        workReportLine.setCode("default-work-report-line");
        workReportLine.setDate(new Date());
        workReportLine.setResource(resource);
        workReportLine.setOrderElement(orderElement);
        workReportLine.setTypeOfWorkHours(typeOfWorkHours);
        workReportLine.setEffort(EffortDuration.hours(10));

        workReport.addWorkReportLine(workReportLine);
        workReportDAO.save(workReport);
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

    @Test
    public void basicTest() throws InstanceNotFoundException {
        givenBasicExample();
        assertThat(moneyCostCalculator.getMoneyCost(orderElement),
                equalTo(new BigDecimal(500).setScale(2)));
    }

    @Test
    public void basicTestWithoutCostCategoryRelationship()
            throws InstanceNotFoundException {
        givenBasicExampleWithoutCostCategoryRelationship();
        assertThat(moneyCostCalculator.getMoneyCost(orderElement),
                equalTo(new BigDecimal(300).setScale(2)));
    }

}
