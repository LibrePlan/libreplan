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

package org.navalplanner.business.test.workreports.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.UUID;

import org.joda.time.LocalTime;
import org.junit.Test;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workreports.entities.HoursManagementEnum;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.business.workreports.entities.WorkReportType;

/**
 * @author Diego Pino García <dpino@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class WorkReportTest {

    private WorkReportType givenBasicWorkReportType() {
        WorkReportType workReportType = WorkReportType.create();
        workReportType.setCode("type-of-work-hours-code-" + UUID.randomUUID());
        workReportType.setName("type-of-work-hours-name");

        return workReportType;
    }

    private OrderLine givenOrderLine() {
        OrderLine orderLine = OrderLine
                .createOrderLineWithUnfixedPercentage(100);
        orderLine.setCode("order-line-code-" + UUID.randomUUID());
        orderLine.setName("order-line-name");

        return orderLine;
    }

    private Worker givenWorker() {
        return Worker
                .create("Firstname", "Surname", "NIF-" + UUID.randomUUID());
    }

    private WorkReportLine givenBasicWorkReportLine(WorkReport workReport) {
        WorkReportLine workReportLine = WorkReportLine.create(workReport);
        workReportLine.setCode("work-report-line-code-" + UUID.randomUUID());

        return workReportLine;
    }

    private WorkReport givenBasicWorkReport() {
        WorkReport workReport = WorkReport.create();
        workReport.setCode("work-report-code-" + UUID.randomUUID());

        return workReport;
    }

    @Test
    public void checkImpossibleSetFieldsAtWorkReportLevel() {
        WorkReport workReport = WorkReport.create(givenBasicWorkReportType());

        assertNull(workReport.getDate());
        assertNull(workReport.getOrderElement());
        assertNull(workReport.getResource());

        workReport.setDate(new Date());
        workReport.setOrderElement(givenOrderLine());
        workReport.setResource(givenWorker());

        assertNull(workReport.getDate());
        assertNull(workReport.getOrderElement());
        assertNull(workReport.getResource());
    }

    @Test
    public void checkSetDateAtWorkReportLevel() {
        WorkReportType workReportType = givenBasicWorkReportType();
        workReportType.setDateIsSharedByLines(true);

        WorkReport workReport = WorkReport.create(workReportType);

        assertNull(workReport.getDate());
        assertNull(workReport.getOrderElement());
        assertNull(workReport.getResource());

        workReport.setDate(new Date());
        workReport.setOrderElement(givenOrderLine());
        workReport.setResource(givenWorker());

        assertNotNull(workReport.getDate());
        assertNull(workReport.getOrderElement());
        assertNull(workReport.getResource());
    }

    @Test
    public void checkSetOrderElementAtWorkReportLevel() {
        WorkReportType workReportType = givenBasicWorkReportType();
        workReportType.setOrderElementIsSharedInLines(true);

        WorkReport workReport = WorkReport.create(workReportType);

        assertNull(workReport.getDate());
        assertNull(workReport.getOrderElement());
        assertNull(workReport.getResource());

        workReport.setDate(new Date());
        workReport.setOrderElement(givenOrderLine());
        workReport.setResource(givenWorker());

        assertNull(workReport.getDate());
        assertNotNull(workReport.getOrderElement());
        assertNull(workReport.getResource());
    }

    @Test
    public void checkSetResourceAtWorkReportLevel() {
        WorkReportType workReportType = givenBasicWorkReportType();
        workReportType.setResourceIsSharedInLines(true);

        WorkReport workReport = WorkReport.create(workReportType);

        assertNull(workReport.getDate());
        assertNull(workReport.getOrderElement());
        assertNull(workReport.getResource());

        workReport.setDate(new Date());
        workReport.setOrderElement(givenOrderLine());
        workReport.setResource(givenWorker());

        assertNull(workReport.getDate());
        assertNull(workReport.getOrderElement());
        assertNotNull(workReport.getResource());
    }

    @Test
    public void checkDataKeptAtAllWorkReportLines() {
        WorkReportType workReportType = givenBasicWorkReportType();
        workReportType.setDateIsSharedByLines(true);
        workReportType.setOrderElementIsSharedInLines(true);
        workReportType.setResourceIsSharedInLines(true);

        WorkReport workReport = givenBasicWorkReport();
        workReport.setWorkReportType(workReportType);

        WorkReportLine workReportLine1 = givenBasicWorkReportLine(workReport);
        workReport.addWorkReportLine(workReportLine1);

        workReport.setDate(new Date());
        workReport.setOrderElement(givenOrderLine());
        workReport.setResource(givenWorker());

        assertNotNull(workReportLine1.getDate());
        assertNotNull(workReportLine1.getOrderElement());
        assertNotNull(workReportLine1.getResource());

        WorkReportLine workReportLine2 = givenBasicWorkReportLine(workReport);
        workReport.addWorkReportLine(workReportLine2);

        assertNotNull(workReportLine2.getDate());
        assertNotNull(workReportLine2.getOrderElement());
        assertNotNull(workReportLine2.getResource());

        workReportLine2.setDate(null);
        workReportLine2.setOrderElement(null);
        workReportLine2.setResource(null);

        assertNotNull(workReportLine2.getDate());
        assertNotNull(workReportLine2.getOrderElement());
        assertNotNull(workReportLine2.getResource());

    }

    @Test
    public void checkHoursCalculatedByClock() {
        WorkReportType workReportType = givenBasicWorkReportType();
        workReportType
                .setHoursManagement(HoursManagementEnum.HOURS_CALCULATED_BY_CLOCK);

        WorkReport workReport = WorkReport.create(workReportType);
        WorkReportLine workReportLine = givenBasicWorkReportLine(workReport);

        workReport.addWorkReportLine(workReportLine);

        workReportLine.setEffort(EffortDuration.hours(10));
        assertNull(workReportLine.getEffort());

        LocalTime start = new LocalTime(8, 0);
        LocalTime end = start.plusHours(8);

        workReportLine.setClockStart(start);
        workReportLine.setClockFinish(end);

        assertThat(workReportLine.getEffort(), equalTo(EffortDuration.hours(8)));

        workReportLine.setEffort(EffortDuration.hours(10));

        assertThat(workReportLine.getEffort(), equalTo(EffortDuration.hours(8)));
    }

    @Test
    public void checkHoursCalculatedByClock2() {

        WorkReportType workReportType = givenBasicWorkReportType();
        workReportType
                .setHoursManagement(HoursManagementEnum.HOURS_CALCULATED_BY_CLOCK);

        WorkReport workReport = WorkReport.create(workReportType);

        WorkReportLine workReportLine = givenBasicWorkReportLine(workReport);
        workReport.addWorkReportLine(workReportLine);

        workReportLine.setEffort(EffortDuration.hours(10));
        LocalTime start = new LocalTime(8, 0);
        LocalTime end = start.plusHours(8);

        workReportLine.setClockStart(start);
        workReportLine.setClockFinish(end);

        assertThat(workReportLine.getEffort(), equalTo(EffortDuration.hours(8)));
    }

}
