/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.test.ws.workreports;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.costcategories.daos.ITypeOfWorkHoursDAO;
import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.resources.daos.IWorkerDAO;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.workreports.daos.IWorkReportDAO;
import org.navalplanner.business.workreports.daos.IWorkReportTypeDAO;
import org.navalplanner.business.workreports.entities.HoursManagementEnum;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.workreports.api.IWorkReportService;
import org.navalplanner.ws.workreports.api.WorkReportDTO;
import org.navalplanner.ws.workreports.api.WorkReportLineDTO;
import org.navalplanner.ws.workreports.api.WorkReportListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link IWorkReportService}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE })
@Transactional
public class WorkReportServiceTest {

    @Autowired
    private IWorkReportService workReportService;

    @Autowired
    private IWorkerDAO workerDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private ITypeOfWorkHoursDAO typeOfWorkHoursDAO;

    @Autowired
    private IWorkReportTypeDAO workReportTypeDAO;

    @Autowired
    private IAdHocTransactionService adHocTransaction;

    @Autowired
    private IWorkReportDAO workReportDAO;

    private final String workReportTypeCode = "TypeCode-A";

    private final String workReportTypeCode2 = "TypeCode-B";

    private final String workReportTypeCode3 = "TypeCode-C";

    private final String workReportTypeCode4 = "TypeCode-C";

    private final String resourceCode = "ResourceCode-A";

    private final String orderElementCode = "OrderElementCode-A";

    private final String typeOfWorkHoursCode = "TypeOfWorkHoursCode-A";

    @Test
    @Rollback(false)
    public void givenWorkerStored() {
        Worker worker = Worker.create("Firstname", "Surname", resourceCode);
        workerDAO.save(worker);
        workerDAO.flush();
        sessionFactory.getCurrentSession().evict(worker);

        worker.dontPoseAsTransientObjectAnymore();
    }

    @Test
    @Rollback(false)
    public void givenOrderLineStored() {
        OrderLine orderLine = OrderLine.create();
        orderLine.setCode(orderElementCode);
        orderLine.setName("order-line-name" + UUID.randomUUID());

        orderElementDAO.save(orderLine);
        orderElementDAO.flush();
        sessionFactory.getCurrentSession().evict(orderLine);

        orderLine.dontPoseAsTransientObjectAnymore();
    }

    @Test
    @Rollback(false)
    public void givenTypeOfWorkHoursStored() {
        TypeOfWorkHours typeOfWorkHours = TypeOfWorkHours.create();
        typeOfWorkHours.setCode(typeOfWorkHoursCode);
        typeOfWorkHours.setName("type-of-work-hours-name-" + UUID.randomUUID());

        typeOfWorkHoursDAO.save(typeOfWorkHours);
        typeOfWorkHoursDAO.flush();
        sessionFactory.getCurrentSession().evict(typeOfWorkHours);

        typeOfWorkHours.dontPoseAsTransientObjectAnymore();
    }

    @Test
    @Rollback(false)
    public void givenWorkReportTypeStored() {
        givenWorkReportTypeStored(false, false, false, null, workReportTypeCode);
    }

    @Test
    @Rollback(false)
    public void givenWorkReportTypeStored2() {
        givenWorkReportTypeStored(true, false, false, null, workReportTypeCode2);
    }

    @Test
    @Rollback(false)
    public void givenWorkReportTypeStored3() {
        givenWorkReportTypeStored(false, false, false,
                HoursManagementEnum.HOURS_CALCULATED_BY_CLOCK,
                workReportTypeCode3);
    }

    private WorkReportType givenWorkReportTypeStored(boolean dateShared,
            boolean orderElementShared, boolean resourceShared,
            HoursManagementEnum hoursManagement, String workReportTypeCode) {
        WorkReportType workReportType = WorkReportType.create();
        workReportType.setCode(workReportTypeCode);
        workReportType.setName(workReportTypeCode);

        workReportType.setDateIsSharedByLines(dateShared);
        workReportType.setOrderElementIsSharedInLines(orderElementShared);
        workReportType.setResourceIsSharedInLines(resourceShared);

        if (hoursManagement != null) {
            workReportType.setHoursManagement(hoursManagement);
        }

        workReportTypeDAO.save(workReportType);
        workReportTypeDAO.flush();
        sessionFactory.getCurrentSession().evict(workReportType);

        workReportType.dontPoseAsTransientObjectAnymore();

        return workReportType;
    }

    private WorkReportLineDTO createWorkReportLineDTO() {
        WorkReportLineDTO workReportLineDTO = new WorkReportLineDTO();

        workReportLineDTO.code = "work-report-line-code-" + UUID.randomUUID();
        workReportLineDTO.resource = resourceCode;
        workReportLineDTO.orderElement = orderElementCode;
        workReportLineDTO.date = new Date();
        workReportLineDTO.typeOfWorkHours = typeOfWorkHoursCode;
        workReportLineDTO.numHours = 8;

        return workReportLineDTO;
    }

    private WorkReportDTO createWorkReportDTO(String type) {
        WorkReportDTO workReportDTO = new WorkReportDTO();
        workReportDTO.code = "work-report-code-" + UUID.randomUUID();
        workReportDTO.workReportType = type;
        workReportDTO.workReportLines.add(createWorkReportLineDTO());
        return workReportDTO;
    }

    @Test
    @Transactional
    public void importValidWorkReport() {
        int previous = workReportDAO.getAll().size();

        WorkReportListDTO workReportListDTO = new WorkReportListDTO(Arrays
                .asList(createWorkReportDTO(workReportTypeCode)));

        InstanceConstraintViolationsListDTO instanceConstraintViolationsListDTO = workReportService
                .addWorkReports(workReportListDTO);
        assertThat(
                instanceConstraintViolationsListDTO.instanceConstraintViolationsList
                        .size(), equalTo(0));
        List<WorkReport> workReports = workReportDAO.getAll();
        assertThat(workReports.size(), equalTo(previous + 1));

        Set<WorkReportLine> workReportLines = workReports.get(previous)
                .getWorkReportLines();
        assertThat(workReportLines.size(), equalTo(1));

        assertThat(workReportLines.iterator().next().getNumHours(), equalTo(8));

    }

    @Test
    public void importInvalidWorkReportWithoutDateAtWorkReportLevel() {
        int previous = workReportDAO.getAll().size();

        WorkReportListDTO workReportListDTO = new WorkReportListDTO(Arrays
                .asList(createWorkReportDTO(workReportTypeCode2)));

        InstanceConstraintViolationsListDTO instanceConstraintViolationsListDTO = workReportService
                .addWorkReports(workReportListDTO);
        assertThat(
                instanceConstraintViolationsListDTO.instanceConstraintViolationsList
                        .size(), equalTo(1));
        List<WorkReport> workReports = workReportDAO.getAll();
        assertThat(workReports.size(), equalTo(previous));
    }

    @Test
    public void importValidWorkReportWithDateAtWorkReportLevel() {
        int previous = workReportDAO.getAll().size();

        WorkReportDTO workReportDTO = createWorkReportDTO(workReportTypeCode2);
        Date date = new Date();
        workReportDTO.date = date;

        WorkReportListDTO workReportListDTO = new WorkReportListDTO(Arrays
                .asList(workReportDTO));

        InstanceConstraintViolationsListDTO instanceConstraintViolationsListDTO = workReportService
                .addWorkReports(workReportListDTO);
        assertThat(
                instanceConstraintViolationsListDTO.instanceConstraintViolationsList
                        .size(), equalTo(0));
        List<WorkReport> workReports = workReportDAO.getAll();
        assertThat(workReports.size(), equalTo(previous + 1));

        assertThat(workReports.get(previous).getDate(), equalTo(date));
        assertThat(workReports.get(previous).getWorkReportLines().iterator()
                .next().getDate(), equalTo(date));
    }

    @Test
    public void importInvalidWorkReportCalculatedHours() {
        int previous = workReportDAO.getAll().size();

        WorkReportListDTO workReportListDTO = new WorkReportListDTO(Arrays
                .asList(createWorkReportDTO(workReportTypeCode3)));

        InstanceConstraintViolationsListDTO instanceConstraintViolationsListDTO = workReportService
                .addWorkReports(workReportListDTO);
        assertThat(
                instanceConstraintViolationsListDTO.instanceConstraintViolationsList
                        .size(), equalTo(1));
        List<WorkReport> workReports = workReportDAO.getAll();
        assertThat(workReports.size(), equalTo(previous));
    }

    @Test
    public void importValidWorkReportCalculatedHours() {
        int previous = workReportDAO.getAll().size();

        WorkReportDTO workReportDTO = createWorkReportDTO(workReportTypeCode3);
        WorkReportLineDTO workReportLineDTO = workReportDTO.workReportLines
                .iterator().next();

        int hours = 12;
        LocalTime start = new LocalTime(8, 0);
        LocalTime end = start.plusHours(hours);
        workReportLineDTO.clockStart = start.toDateTimeToday().toDate();
        workReportLineDTO.clockFinish = end.toDateTimeToday().toDate();

        WorkReportListDTO workReportListDTO = new WorkReportListDTO(Arrays
                .asList(workReportDTO));

        InstanceConstraintViolationsListDTO instanceConstraintViolationsListDTO = workReportService
                .addWorkReports(workReportListDTO);
        assertThat(
                instanceConstraintViolationsListDTO.instanceConstraintViolationsList
                        .size(), equalTo(0));
        List<WorkReport> workReports = workReportDAO.getAll();
        assertThat(workReports.size(), equalTo(previous + 1));

        Set<WorkReportLine> workReportLines = workReports.get(previous)
                .getWorkReportLines();
        assertThat(workReportLines.size(), equalTo(1));

        assertThat(workReportLines.iterator().next().getNumHours(),
                equalTo(hours));
    }

    @Test
    @Transactional
    public void importAndUpdateValidWorkReport() {
        int previous = workReportDAO.getAll().size();

        WorkReportDTO workReportDTO = createWorkReportDTO(workReportTypeCode);
        WorkReportListDTO workReportListDTO = new WorkReportListDTO(Arrays
                .asList(workReportDTO));

        InstanceConstraintViolationsListDTO instanceConstraintViolationsListDTO = workReportService
                .addWorkReports(workReportListDTO);
        assertThat(
                instanceConstraintViolationsListDTO.instanceConstraintViolationsList
                        .size(), equalTo(0));
        List<WorkReport> workReports = workReportDAO.getAll();
        assertThat(workReports.size(), equalTo(previous + 1));

        Set<WorkReportLine> workReportLines = workReports.get(previous)
                .getWorkReportLines();
        assertThat(workReportLines.size(), equalTo(1));

        assertThat(workReportLines.iterator().next().getNumHours(), equalTo(8));

        workReportDTO.workReportLines.add(createWorkReportLineDTO());
        WorkReportListDTO workReportListDTO2 = new WorkReportListDTO(Arrays
                .asList(workReportDTO));
        instanceConstraintViolationsListDTO = workReportService
                .addWorkReports(workReportListDTO2);

        assertThat(
                instanceConstraintViolationsListDTO.instanceConstraintViolationsList
                        .size(), equalTo(0));

    }
}