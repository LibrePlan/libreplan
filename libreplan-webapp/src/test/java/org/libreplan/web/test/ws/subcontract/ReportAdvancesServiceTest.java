/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * Copyright (C) 2011 WirelessGalicia, S.L.
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

package org.libreplan.web.test.ws.subcontract;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.externalcompanies.daos.IExternalCompanyDAO;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.orders.entities.SchedulingDataForVersion;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.orders.entities.TaskSource.TaskSourceSynchronization;
import org.libreplan.business.planner.daos.ISubcontractedTaskDataDAO;
import org.libreplan.business.planner.daos.ISubcontractorCommunicationDAO;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.daos.ITaskSourceDAO;
import org.libreplan.business.planner.entities.SubcontractedTaskData;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.web.orders.OrderModelTest;
import org.libreplan.ws.common.api.AdvanceMeasurementDTO;
import org.libreplan.ws.common.impl.DateConverter;
import org.libreplan.ws.subcontract.api.IReportAdvancesService;
import org.libreplan.ws.subcontract.api.OrderElementWithAdvanceMeasurementsOrEndDateDTO;
import org.libreplan.ws.subcontract.api.OrderElementWithAdvanceMeasurementsOrEndDateListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link IReportAdvancesService}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class ReportAdvancesServiceTest {

    @Autowired
    private IAdHocTransactionService transactionService;

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Resource
    private IDataBootstrap scenariosBootstrap;

    @Before
    public void loadRequiredaData() {

        IOnTransaction<Void> load = new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                defaultAdvanceTypesBootstrapListener.loadRequiredData();
                configurationBootstrap.loadRequiredData();
                scenariosBootstrap.loadRequiredData();
                return null;
            }
        };

        transactionService.runOnAnotherTransaction(load);
    }

    @Autowired
    private IReportAdvancesService reportAdvancesService;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IExternalCompanyDAO externalCompanyDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private ISubcontractorCommunicationDAO subcontractorCommunicationDAO;

    @Autowired
    private ISubcontractedTaskDataDAO subcontractorTaskDataDAO;

    private HoursGroup associatedHoursGroup;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private ISubcontractedTaskDataDAO subcontractedTaskDataDAO;

    private ExternalCompany getExternalCompany(String name,
            String nif){
        ExternalCompany externalCompany = ExternalCompany.create(name, nif);
        externalCompany.setSubcontractor(true);

        externalCompanyDAO.save(externalCompany);
        externalCompanyDAO.flush();
        sessionFactory.getCurrentSession().evict(externalCompany);

        externalCompany.dontPoseAsTransientObjectAnymore();

        return externalCompany;
    }

    private ExternalCompany getSubcontractorExternalCompanySaved() {
        return transactionService
                .runOnAnotherTransaction(new IOnTransaction<ExternalCompany>() {
                    @Override
                    public ExternalCompany execute() {
                        return getExternalCompany("Company"
                                + UUID.randomUUID().toString(), UUID
                                .randomUUID().toString());
                    }
                });
    }

    @Test
    public void validAdvancesReport() {
        Order order = givenOrder();
        String orderElementCode = order.getChildren().get(0).getCode();

        Map<LocalDate, BigDecimal> values = givenValidMapValues(1, 0,
                BigDecimal.ZERO);

        OrderElementWithAdvanceMeasurementsOrEndDateListDTO orderElementWithAdvanceMeasurementsListDTO = givenOrderElementWithAdvanceMeasurementsListDTO(
                orderElementCode, values);
        reportAdvancesService
                .updateAdvancesOrEndDate(orderElementWithAdvanceMeasurementsListDTO);

        Order foundOrder = orderDAO.findExistingEntity(order.getId());
        assertNotNull(foundOrder);
        assertThat(foundOrder.getChildren().size(), equalTo(1));

        OrderElement orderElement = foundOrder.getChildren().get(0);
        assertNotNull(orderElement);

        DirectAdvanceAssignment directAdvanceAssignmentSubcontractor = orderElement
                .getDirectAdvanceAssignmentSubcontractor();
        assertNotNull(directAdvanceAssignmentSubcontractor);
        assertTrue(directAdvanceAssignmentSubcontractor
                .getReportGlobalAdvance());
        assertThat(directAdvanceAssignmentSubcontractor
                .getAdvanceMeasurements().size(), equalTo(1));

        for (Entry<LocalDate, BigDecimal> entry : values.entrySet()) {
            AdvanceMeasurement advanceMeasurement = directAdvanceAssignmentSubcontractor
                    .getAdvanceMeasurements().first();
            assertThat(advanceMeasurement.getDate(), equalTo(entry.getKey()));
            assertThat(advanceMeasurement.getValue(), equalTo(entry.getValue()));
        }
    }

    @Test
    public void validAdvancesReportToSubcontratedOrderElement() {
        int previousCommunications = subcontractorCommunicationDAO.getAll().size();

        OrderLine orderLine = createOrderLine();

        String orderElementCode = orderLine.getCode();

        Map<LocalDate, BigDecimal> values = givenValidMapValues(1, 0,
                BigDecimal.ZERO);

        OrderElementWithAdvanceMeasurementsOrEndDateListDTO orderElementWithAdvanceMeasurementsListDTO = givenOrderElementWithAdvanceMeasurementsListDTO(
                orderElementCode, values);
        reportAdvancesService
                .updateAdvancesOrEndDate(orderElementWithAdvanceMeasurementsListDTO);

        Order foundOrder = orderDAO.findExistingEntity(orderLine.getOrder().getId());
        assertNotNull(foundOrder);
        assertThat(foundOrder.getChildren().size(), equalTo(1));

        OrderElement orderElement = foundOrder.getChildren().get(0);
        assertNotNull(orderElement);

        DirectAdvanceAssignment directAdvanceAssignmentSubcontractor = orderElement
                .getDirectAdvanceAssignmentSubcontractor();
        assertNotNull(directAdvanceAssignmentSubcontractor);
        assertTrue(directAdvanceAssignmentSubcontractor
                .getReportGlobalAdvance());
        assertThat(directAdvanceAssignmentSubcontractor
                .getAdvanceMeasurements().size(), equalTo(1));

        for (Entry<LocalDate, BigDecimal> entry : values.entrySet()) {
            AdvanceMeasurement advanceMeasurement = directAdvanceAssignmentSubcontractor
                    .getAdvanceMeasurements().first();
            assertThat(advanceMeasurement.getDate(), equalTo(entry.getKey()));
            assertThat(advanceMeasurement.getValue(), equalTo(entry.getValue()));
        }

        int currentCommunications = subcontractorCommunicationDAO.getAll().size();
        assertThat((previousCommunications + 1), equalTo(currentCommunications));
    }

    @Test
    public void validAdvancesReportWithSeveralDates() {
        Order order = givenOrder();
        String orderElementCode = order.getChildren().get(0).getCode();

        int numMeasures = 3;
        Map<LocalDate, BigDecimal> values = givenValidMapValues(numMeasures, 5,
                BigDecimal.TEN);
        assertThat(values.size(), equalTo(numMeasures));

        OrderElementWithAdvanceMeasurementsOrEndDateListDTO orderElementWithAdvanceMeasurementsListDTO = givenOrderElementWithAdvanceMeasurementsListDTO(
                orderElementCode, values);
        reportAdvancesService
                .updateAdvancesOrEndDate(orderElementWithAdvanceMeasurementsListDTO);

        Order foundOrder = orderDAO.findExistingEntity(order.getId());
        assertNotNull(foundOrder);
        assertThat(foundOrder.getChildren().size(), equalTo(1));

        OrderElement orderElement = foundOrder.getChildren().get(0);
        assertNotNull(orderElement);

        DirectAdvanceAssignment directAdvanceAssignmentSubcontractor = orderElement
                .getDirectAdvanceAssignmentSubcontractor();
        assertNotNull(directAdvanceAssignmentSubcontractor);
        assertTrue(directAdvanceAssignmentSubcontractor
                .getReportGlobalAdvance());
        assertThat(directAdvanceAssignmentSubcontractor
                .getAdvanceMeasurements().size(), equalTo(numMeasures));

        assertThat(directAdvanceAssignmentSubcontractor
                .getAdvanceMeasurements().size(), equalTo(values.size()));

        for (AdvanceMeasurement measure : directAdvanceAssignmentSubcontractor
                .getAdvanceMeasurements()) {
            assertTrue(values.containsKey(measure.getDate()));
            assertTrue(values.containsValue(measure.getValue()));
            assertThat(values.get(measure.getDate()), equalTo(measure
                    .getValue()));
        }
    }

    private OrderElementWithAdvanceMeasurementsOrEndDateListDTO givenOrderElementWithAdvanceMeasurementsListDTO(
            String orderElementCode, Map<LocalDate, BigDecimal> values) {
        OrderElementWithAdvanceMeasurementsOrEndDateDTO orderElementWithAdvanceMeasurementsDTO = new OrderElementWithAdvanceMeasurementsOrEndDateDTO();
        orderElementWithAdvanceMeasurementsDTO.code = orderElementCode;

        orderElementWithAdvanceMeasurementsDTO.advanceMeasurements = givenAdvanceMeasurementDTOs(values);

        ExternalCompany externalCompany = getSubcontractorExternalCompanySaved();

        return new OrderElementWithAdvanceMeasurementsOrEndDateListDTO(externalCompany
                .getNif(), Arrays
                .asList(orderElementWithAdvanceMeasurementsDTO));
    }

    private Set<AdvanceMeasurementDTO> givenAdvanceMeasurementDTOs(
            Map<LocalDate, BigDecimal> values) {
        Set<AdvanceMeasurementDTO> advanceMeasurementDTOs = new HashSet<AdvanceMeasurementDTO>();
        for (Entry<LocalDate, BigDecimal> entry : values.entrySet()) {
            advanceMeasurementDTOs.add(new AdvanceMeasurementDTO(DateConverter
                    .toXMLGregorianCalendar(entry.getKey()), entry.getValue()));
        }
        return advanceMeasurementDTOs;
    }

    private Map<LocalDate, BigDecimal> givenValidMapValues(int iterations,
            int separatorDay, BigDecimal separatorPercentage) {
        Map<LocalDate, BigDecimal> values = new HashMap<LocalDate, BigDecimal>();
        LocalDate currentDate = new LocalDate();
        BigDecimal currentValue = new BigDecimal(10);

        for (int i = 0; i < iterations; i++) {
            values.put(currentDate, currentValue);
            currentDate = currentDate.plusDays(separatorDay);
            currentValue = currentValue.add(separatorPercentage);
            if (currentValue.compareTo(new BigDecimal(100)) >= 0) {
                break;
            }
        }

        return values;
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
        OrderVersion version = OrderModelTest.setupVersionUsing(
                scenarioManager, order);
        order.useSchedulingDataFor(version);

        OrderLine orderLine = OrderLine
                .createOrderLineWithUnfixedPercentage(1000);
        orderLine.useSchedulingDataFor(version);
        order.add(orderLine);
        orderLine.setCode(UUID.randomUUID().toString());
        orderLine.setName("Order line name");

        orderDAO.save(order);
        orderDAO.flush();
        try {
            return orderDAO.find(order.getId());
        } catch (InstanceNotFoundException e) {
            return null;
        }
    }

    private OrderLine createOrderLine(){
        return transactionService
        .runOnAnotherTransaction(new IOnTransaction<OrderLine>() {
            @Override
            public OrderLine execute() {
                Order order = givenValidOrderAlreadyStored();
                OrderLine orderLine = (OrderLine) order.getChildren().get(0);
                createValidSubcontractedTaskData("subcotrated_task_"+UUID.randomUUID().toString(), orderLine);
                return orderLine;
            }
        });
    }

    private Task createValidTask(OrderLine orderLine) {
        associatedHoursGroup = new HoursGroup();
        associatedHoursGroup.setCode("hours-group-code-" + UUID.randomUUID());

        orderLine.addHoursGroup(associatedHoursGroup);
        SchedulingDataForVersion schedulingDataForVersion = orderLine
                .getCurrentSchedulingDataForVersion();

        TaskSource taskSource = TaskSource.create(schedulingDataForVersion,
                Arrays.asList(associatedHoursGroup));

        orderLine.getCurrentSchedulingData().requestedCreationOf(taskSource);

        TaskSourceSynchronization mustAdd = TaskSource.mustAdd(taskSource);
        mustAdd.apply(TaskSource.persistTaskSources(taskSourceDAO));

        Task task = (Task) taskSource.getTask();
        return task;
    }

    public SubcontractedTaskData createValidSubcontractedTaskData(final String name, final OrderLine orderLine) {
        Task task = createValidTask(orderLine);
        SubcontractedTaskData subcontractedTaskData = SubcontractedTaskData
                .create(task);
        subcontractedTaskData.setExternalCompany(getSubcontractorExternalCompanySaved());

        task.setSubcontractedTaskData(subcontractedTaskData);
        taskElementDAO.save(task);
        task.dontPoseAsTransientObjectAnymore();
        taskElementDAO.flush();

        subcontractedTaskDataDAO.save(subcontractedTaskData);
        return subcontractedTaskData;
    }
}
