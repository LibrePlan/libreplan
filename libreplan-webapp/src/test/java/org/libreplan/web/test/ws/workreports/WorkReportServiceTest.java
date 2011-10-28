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

package org.libreplan.web.test.ws.workreports;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.costcategories.daos.ITypeOfWorkHoursDAO;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.labels.daos.ILabelDAO;
import org.libreplan.business.labels.daos.ILabelTypeDAO;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.labels.entities.LabelType;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workreports.daos.IWorkReportDAO;
import org.libreplan.business.workreports.daos.IWorkReportTypeDAO;
import org.libreplan.business.workreports.entities.HoursManagementEnum;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportLabelTypeAssigment;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.business.workreports.entities.WorkReportType;
import org.libreplan.business.workreports.valueobjects.DescriptionField;
import org.libreplan.ws.common.api.InstanceConstraintViolationsDTO;
import org.libreplan.ws.common.api.InstanceConstraintViolationsListDTO;
import org.libreplan.ws.common.api.LabelReferenceDTO;
import org.libreplan.ws.common.impl.DateConverter;
import org.libreplan.ws.workreports.api.DescriptionValueDTO;
import org.libreplan.ws.workreports.api.IWorkReportService;
import org.libreplan.ws.workreports.api.WorkReportDTO;
import org.libreplan.ws.workreports.api.WorkReportLineDTO;
import org.libreplan.ws.workreports.api.WorkReportListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
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
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
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
    private IWorkReportDAO workReportDAO;

    @Autowired
    private ILabelDAO labelDAO;

    @Autowired
    private ILabelTypeDAO labelTypeDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    private final String workReportTypeCode = "TypeCode-A";

    private final String workReportTypeCode2 = "TypeCode-B";

    private final String workReportTypeCode3 = "TypeCode-C";

    private final String workReportTypeCode4 = "TypeCode-D";

    private final String workReportTypeCode5 = "TypeCode-E";

    private final String resourceCode = "ResourceCode-A";

    private final String orderElementCode = "OrderElementCode-A";

    private final String typeOfWorkHoursCode = "TypeOfWorkHoursCode-A";

    private final String field1 = "field1";

    private final String field2 = "field2";

    private final String labelTypeA = "labelTypeA";

    private final String labelTypeB = "labelTypeB";

    private final String labelA1 = "labelA1";

    private final String labelA2 = "labelA2";

    private final String labelB1 = "labelB1";

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
    public void createAPairOfLabelTypes() {
        LabelType labelType_A = LabelType.create(labelTypeA, labelTypeA);
        LabelType labelType_B = LabelType.create(labelTypeB, labelTypeB);

        Label label_A1 = Label.create(labelA1, labelA1);
        Label label_A2 = Label.create(labelA2, labelA2);
        Label label_B1 = Label.create(labelB1, labelB1);

        labelType_A.addLabel(label_A1);
        labelType_A.addLabel(label_A2);
        labelType_B.addLabel(label_B1);

        labelTypeDAO.save(labelType_A);
        labelTypeDAO.save(labelType_B);
        labelTypeDAO.flush();
        sessionFactory.getCurrentSession().evict(labelType_A);
        sessionFactory.getCurrentSession().evict(labelType_B);
        labelType_A.dontPoseAsTransientObjectAnymore();
        labelType_B.dontPoseAsTransientObjectAnymore();
    }

    @Test
    @Rollback(false)
    public void givenTypeOfWorkHoursStored() {
        TypeOfWorkHours typeOfWorkHours = TypeOfWorkHours.create();
        typeOfWorkHours.setCode(typeOfWorkHoursCode);
        typeOfWorkHours.setName("type-of-work-hours-name-" + UUID.randomUUID());
        typeOfWorkHours.setDefaultPrice(BigDecimal.TEN);

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

    @Test
    @Rollback(false)
    public void givenWorkReportTypeStored4() {
        WorkReportType type = givenWorkReportTypeStored(false, false, false,
                null,workReportTypeCode4);
        type.addDescriptionFieldToEndHead(DescriptionField.create(field1, 10));
        type.addDescriptionFieldToEndLine(DescriptionField.create(field2, 10));

        workReportTypeDAO.save(type);
        workReportTypeDAO.flush();
        sessionFactory.getCurrentSession().evict(type);
        type.dontPoseAsTransientObjectAnymore();
    }

    @Test
    @Rollback(false)
    public void givenWorkReportTypeStored5() {
        WorkReportType type = givenWorkReportTypeStored(false, false, false,
                null, workReportTypeCode5);
        WorkReportLabelTypeAssigment labelAssigment1 = WorkReportLabelTypeAssigment
                .create(true);
        WorkReportLabelTypeAssigment labelAssigment2 = WorkReportLabelTypeAssigment
                .create(false);

        try {
            labelAssigment1.setLabelType(labelTypeDAO.findByCode(labelTypeA));
            labelAssigment1.setDefaultLabel(labelDAO.findByCode(labelA1));
            labelAssigment1.setPositionNumber(0);

            labelAssigment2.setLabelType(labelTypeDAO.findByCode(labelTypeB));
            labelAssigment2.setDefaultLabel(labelDAO.findByCode(labelB1));
            labelAssigment2.setPositionNumber(0);

            type.addLabelAssigmentToEndHead(labelAssigment1);
            type.addLabelAssigmentToEndLine(labelAssigment2);

            workReportTypeDAO.save(type);
            workReportTypeDAO.flush();
            sessionFactory.getCurrentSession().evict(type);
            type.dontPoseAsTransientObjectAnymore();

        } catch (InstanceNotFoundException e) {
            assertTrue(false);
        }
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
        workReportLineDTO.date = DateConverter
                .toXMLGregorianCalendar(new Date());
        workReportLineDTO.typeOfWorkHours = typeOfWorkHoursCode;
        workReportLineDTO.numHours = "8:15";

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
    public void importInvalidLabelsToWorkReport() {
        // create work report with a work report line
        WorkReportDTO workReportDTO = createWorkReportDTO(workReportTypeCode5);

        // create invalid description value to add into head and lines.
        LabelReferenceDTO labelDTO1 = new LabelReferenceDTO("codeLabelNoexiste");
        LabelReferenceDTO labelDTO2 = new LabelReferenceDTO(labelA1);

        // it assigne a label type LabelTypeA, but it should be a label type
        // LabelTypeB
        workReportDTO.labels.add(labelDTO1);
        for (WorkReportLineDTO lineDTO : workReportDTO.workReportLines) {
            lineDTO.labels.add(labelDTO2);
        }

        WorkReportListDTO workReportListDTO = new WorkReportListDTO(Arrays
                .asList(workReportDTO));
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = workReportService
                .addWorkReports(workReportListDTO).instanceConstraintViolationsList;

        // Test
        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 1);
    }

    @Test
    @Transactional
    public void importValidLabelsToWorkReport() {
        // create work report with a work report line
        WorkReportDTO workReportDTO = createWorkReportDTO(workReportTypeCode5);

        // create invalid description value to add into head and lines.
        LabelReferenceDTO labelDTO1 = new LabelReferenceDTO(labelA1);
        LabelReferenceDTO labelDTO2 = new LabelReferenceDTO(labelB1);

        // it assigne a label type LabelTypeA, but it should be a label type
        // LabelTypeB
        workReportDTO.labels.add(labelDTO1);
        for (WorkReportLineDTO lineDTO : workReportDTO.workReportLines) {
            lineDTO.labels.add(labelDTO2);
        }

        WorkReportListDTO workReportListDTO = new WorkReportListDTO(Arrays
                .asList(workReportDTO));
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = workReportService
                .addWorkReports(workReportListDTO).instanceConstraintViolationsList;

        // Test
        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 0);
    }

    @Test
    @Transactional
    public void importInvalidDescriptionValuesToWorkReport() {
        // create work report with a work report line
        WorkReportDTO workReportDTO = createWorkReportDTO(workReportTypeCode4);

        // create invalid description value to add into head and lines.
        DescriptionValueDTO valueDTO1 = new DescriptionValueDTO(field1 + "X",
                "incorrecto");
        DescriptionValueDTO valueDTO2 = new DescriptionValueDTO(field2 + "X",
                "incorrecto");
        workReportDTO.descriptionValues.add(valueDTO1);
        for (WorkReportLineDTO lineDTO : workReportDTO.workReportLines) {
            lineDTO.descriptionValues.add(valueDTO2);
        }

        WorkReportListDTO workReportListDTO = new WorkReportListDTO(Arrays
                .asList(workReportDTO));
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = workReportService
                .addWorkReports(workReportListDTO).instanceConstraintViolationsList;

        // Test
        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 1);
        assertTrue(instanceConstraintViolationsList.get(0).constraintViolations
                .toString(),
                instanceConstraintViolationsList.get(0).constraintViolations
                        .size() == 2);
    }

    @Test
    @Transactional
    public void importValidDescriptionValuesToWorkReport() {
        // create work report with a work report line
        WorkReportDTO workReportDTO = createWorkReportDTO(workReportTypeCode4);

        // create invalid description value to add into head and lines.
        DescriptionValueDTO valueDTO1 = new DescriptionValueDTO(field1,
                "correcto");
        DescriptionValueDTO valueDTO2 = new DescriptionValueDTO(field2,
                "correcto");
        workReportDTO.descriptionValues.add(valueDTO1);
        for (WorkReportLineDTO lineDTO : workReportDTO.workReportLines) {
            lineDTO.descriptionValues.add(valueDTO2);
        }

        WorkReportListDTO workReportListDTO = new WorkReportListDTO(Arrays
                .asList(workReportDTO));
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = workReportService
                .addWorkReports(workReportListDTO).instanceConstraintViolationsList;

        // Test
        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 0);
    }

    @Test
    @NotTransactional
    public void importValidWorkReport() {
        int previous = transactionService
                .runOnTransaction(new IOnTransaction<Integer>() {
                    @Override
                    public Integer execute() {
                        return workReportDAO.getAll().size();
                    }
                });

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {

        WorkReportListDTO workReportListDTO = new WorkReportListDTO(Arrays
                .asList(createWorkReportDTO(workReportTypeCode)));

        InstanceConstraintViolationsListDTO instanceConstraintViolationsListDTO = workReportService
                .addWorkReports(workReportListDTO);
        assertThat(
                instanceConstraintViolationsListDTO.instanceConstraintViolationsList
                        .size(), equalTo(0));
                return null;
            }
        });

        List<WorkReport> workReports = transactionService
                .runOnTransaction(new IOnTransaction<List<WorkReport>>() {
                    @Override
                    public List<WorkReport> execute() {
                        List<WorkReport> list = workReportDAO.getAll();
                        for (WorkReport workReport : list) {
                            Set<WorkReportLine> workReportLines = workReport
                                    .getWorkReportLines();
                            for (WorkReportLine line : workReportLines) {
                                line.getEffort().getHours();
                            }
                        }
                        return list;
                    }
                });

        assertThat(workReports.size(), equalTo(previous + 1));

        Set<WorkReportLine> workReportLines = workReports.get(previous)
                .getWorkReportLines();
        assertThat(workReportLines.size(), equalTo(1));

        assertThat(workReportLines.iterator().next().getEffort(),
                equalTo(EffortDuration.sum(EffortDuration.hours(8),
                        EffortDuration.minutes(15))));

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
    @NotTransactional
    public void importValidWorkReportWithDateAtWorkReportLevel() {
        int previous = transactionService
                .runOnTransaction(new IOnTransaction<Integer>() {
                    @Override
                    public Integer execute() {
                        return workReportDAO.getAll().size();
                    }
                });

        WorkReportDTO workReportDTO = createWorkReportDTO(workReportTypeCode2);
        Date date = new LocalDate().toDateTimeAtStartOfDay().toDate();
        workReportDTO.date = DateConverter.toXMLGregorianCalendar(date);

        WorkReportListDTO workReportListDTO = new WorkReportListDTO(Arrays
                .asList(workReportDTO));

        InstanceConstraintViolationsListDTO instanceConstraintViolationsListDTO = workReportService
                .addWorkReports(workReportListDTO);
        assertThat(
                instanceConstraintViolationsListDTO.instanceConstraintViolationsList
                        .size(), equalTo(0));
        List<WorkReport> workReports = transactionService
                .runOnTransaction(new IOnTransaction<List<WorkReport>>() {
                    @Override
                    public List<WorkReport> execute() {
                        List<WorkReport> list = workReportDAO.getAll();
                        for (WorkReport workReport : list) {
                            Set<WorkReportLine> workReportLines = workReport
                                    .getWorkReportLines();
                            for (WorkReportLine line : workReportLines) {
                                line.getDate();
                            }
                        }
                        return list;
                    }
                });
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
    @NotTransactional
    public void importValidWorkReportCalculatedHours() {
        int previous = transactionService
                .runOnTransaction(new IOnTransaction<Integer>() {
                    @Override
                    public Integer execute() {
                        return workReportDAO.getAll().size();
                    }
                });

        WorkReportDTO workReportDTO = createWorkReportDTO(workReportTypeCode3);
        WorkReportLineDTO workReportLineDTO = workReportDTO.workReportLines
                .iterator().next();

        int hours = 12;
        LocalTime start = new LocalTime(8, 0);
        LocalTime end = start.plusHours(hours);
        workReportLineDTO.clockStart = DateConverter
                .toXMLGregorianCalendar(start);
        workReportLineDTO.clockFinish = DateConverter
                .toXMLGregorianCalendar(end);

        WorkReportListDTO workReportListDTO = new WorkReportListDTO(Arrays
                .asList(workReportDTO));

        InstanceConstraintViolationsListDTO instanceConstraintViolationsListDTO = workReportService
                .addWorkReports(workReportListDTO);
        assertThat(
                instanceConstraintViolationsListDTO.instanceConstraintViolationsList
                        .size(), equalTo(0));
        List<WorkReport> workReports = transactionService
                .runOnTransaction(new IOnTransaction<List<WorkReport>>() {
                    @Override
                    public List<WorkReport> execute() {
                        List<WorkReport> list = workReportDAO.getAll();
                        for (WorkReport workReport : list) {
                            Set<WorkReportLine> workReportLines = workReport
                                    .getWorkReportLines();
                            for (WorkReportLine line : workReportLines) {
                                line.getEffort().getHours();
                            }
                        }
                        return list;
                    }
                });
        assertThat(workReports.size(), equalTo(previous + 1));

        Set<WorkReportLine> workReportLines = workReports.get(previous)
                .getWorkReportLines();
        assertThat(workReportLines.size(), equalTo(1));

        assertThat(workReportLines.iterator().next().getEffort().getHours(),
                equalTo(hours));
    }

    @Test
    @NotTransactional
    public void importAndUpdateValidWorkReport() {
        int previous = transactionService
                .runOnTransaction(new IOnTransaction<Integer>() {
                    @Override
                    public Integer execute() {
                        return workReportDAO.getAll().size();
                    }
                });

        WorkReportDTO workReportDTO = createWorkReportDTO(workReportTypeCode);
        WorkReportListDTO workReportListDTO = new WorkReportListDTO(Arrays
                .asList(workReportDTO));

        InstanceConstraintViolationsListDTO instanceConstraintViolationsListDTO = workReportService
                .addWorkReports(workReportListDTO);
        assertThat(
                instanceConstraintViolationsListDTO.instanceConstraintViolationsList
                        .size(), equalTo(0));
        List<WorkReport> workReports = transactionService
                .runOnTransaction(new IOnTransaction<List<WorkReport>>() {
                    @Override
                    public List<WorkReport> execute() {
                        List<WorkReport> list = workReportDAO.getAll();
                        for (WorkReport workReport : list) {
                            Set<WorkReportLine> workReportLines = workReport
                                    .getWorkReportLines();
                            for (WorkReportLine line : workReportLines) {
                                line.getEffort().getHours();
                            }
                        }
                        return list;
                    }
                });
        assertThat(workReports.size(), equalTo(previous + 1));

        Set<WorkReportLine> workReportLines = workReports.get(previous)
                .getWorkReportLines();
        assertThat(workReportLines.size(), equalTo(1));

        assertThat(workReportLines.iterator().next().getEffort(),
                equalTo(EffortDuration.sum(EffortDuration.hours(8),
                        EffortDuration.minutes(15))));

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