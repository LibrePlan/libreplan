/*
 * This file is part of LibrePlan
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

package org.libreplan.business.test.planner.daos;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.externalcompanies.daos.ICustomerCommunicationDAO;
import org.libreplan.business.externalcompanies.daos.IExternalCompanyDAO;
import org.libreplan.business.externalcompanies.entities.CommunicationType;
import org.libreplan.business.externalcompanies.entities.CustomerCommunication;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.orders.entities.SchedulingDataForVersion;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.orders.entities.TaskSource.TaskSourceSynchronization;
import org.libreplan.business.planner.daos.ISubcontractedTaskDataDAO;
import org.libreplan.business.planner.daos.ISubcontractorCommunicationDAO;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.daos.ITaskSourceDAO;
import org.libreplan.business.planner.entities.SubcontractedTaskData;
import org.libreplan.business.planner.entities.SubcontractorCommunication;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.bootstrap.IScenariosBootstrap;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.test.calendars.entities.BaseCalendarTest;
import org.libreplan.business.test.externalcompanies.daos.ExternalCompanyDAOTest;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link SubcontractorCommunication}
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class SubcontractorCommunicationDAOTest {

    @Autowired
    ISubcontractorCommunicationDAO subcontractorCommunicationDAO;

    @Autowired
    ISubcontractedTaskDataDAO subcontractedTaskDataDAO;

    @Autowired
    IExternalCompanyDAO externalCompanyDAO;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private IBaseCalendarDAO calendarDAO;

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Before
    public void loadRequiredData() {
        scenariosBootstrap.loadRequiredData();
    }

    private HoursGroup associatedHoursGroup;

    private ExternalCompany getSubcontractorExternalCompanySaved() {
        ExternalCompany externalCompany = ExternalCompanyDAOTest
                .createValidExternalCompany();
        externalCompany.setSubcontractor(true);

        externalCompanyDAO.save(externalCompany);
        externalCompanyDAO.flush();
        sessionFactory.getCurrentSession().evict(externalCompany);

        externalCompany.dontPoseAsTransientObjectAnymore();

        return externalCompany;
    }

    private OrderLine createOrderLine() {
        OrderLine orderLine = OrderLine.create();
        orderLine.setName("bla");
        orderLine.setCode("code-" + UUID.randomUUID());
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setCode("hours-group-code-" + UUID.randomUUID());
        orderLine.addHoursGroup(hoursGroup);
        Order order = Order.create();
        OrderVersion orderVersion = ResourceAllocationDAOTest
                .setupVersionUsing(scenarioManager, order);
        order.setName("bla-" + UUID.randomUUID());
        order.setInitDate(new Date());
        order.setCode("code-" + UUID.randomUUID());
        order.useSchedulingDataFor(orderVersion);
        order.add(orderLine);

        //add a basic calendar
        BaseCalendar basicCalendar = BaseCalendarTest.createBasicCalendar();
        calendarDAO.save(basicCalendar);
        order.setCalendar(basicCalendar);

        try {
            orderDAO.save(order);
            sessionFactory.getCurrentSession().flush();
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        return orderLine;
    }

    private Task createValidTask() {
        associatedHoursGroup = new HoursGroup();
        associatedHoursGroup.setCode("hours-group-code-" + UUID.randomUUID());
        OrderLine orderLine = createOrderLine();
        orderLine.addHoursGroup(associatedHoursGroup);
        OrderVersion orderVersion = ResourceAllocationDAOTest
                .setupVersionUsing(scenarioManager,
                orderLine.getOrder());
        orderLine.useSchedulingDataFor(orderVersion);
        SchedulingDataForVersion schedulingDataForVersion = orderLine
                .getCurrentSchedulingDataForVersion();
        TaskSource taskSource = TaskSource.create(schedulingDataForVersion,
                Arrays.asList(associatedHoursGroup));
        TaskSourceSynchronization mustAdd = TaskSource.mustAdd(taskSource);
        mustAdd.apply(TaskSource.persistTaskSources(taskSourceDAO));
        Task task = (Task) taskSource.getTask();
        return task;
    }

    public SubcontractedTaskData createValidSubcontractedTaskData(String name) {
        Task task = createValidTask();
        SubcontractedTaskData subcontractedTaskData = SubcontractedTaskData
                .create(task);
        subcontractedTaskData.setExternalCompany(getSubcontractorExternalCompanySaved());

        task.setSubcontractedTaskData(subcontractedTaskData);
        taskElementDAO.save(task);
        taskElementDAO.flush();
        sessionFactory.getCurrentSession().evict(task);
        sessionFactory.getCurrentSession().evict(subcontractedTaskData);

        subcontractedTaskDataDAO.save(subcontractedTaskData);
        return subcontractedTaskData;
    }

    public SubcontractorCommunication createValidSubcontractorCommunication(){
        SubcontractedTaskData subcontractedTaskData = createValidSubcontractedTaskData("Task A");
        Date communicationDate = new Date();
        SubcontractorCommunication subcontractorCommunication = SubcontractorCommunication
                .create(subcontractedTaskData, CommunicationType.NEW_PROJECT,
                        communicationDate, false);
        return subcontractorCommunication;
    }

    @Test
    public void testSubcontractorCommunicationDAOInSpringContainer() {
        assertNotNull(subcontractorCommunicationDAO);
    }

    @Test
    public void testSaveCustomerCommunication() {
        SubcontractorCommunication subcontractorCommunication = createValidSubcontractorCommunication();
        subcontractorCommunicationDAO.save(subcontractorCommunication);
        assertTrue(subcontractorCommunication.getId() != null);
    }

    @Test
    public void testRemoveCustomerCommunication()
            throws InstanceNotFoundException {
        SubcontractorCommunication customerCommunication = createValidSubcontractorCommunication();
        subcontractorCommunicationDAO.save(customerCommunication);

        assertTrue(customerCommunication.getId() != null);
        Long idSubcontratecTaskData = customerCommunication.getSubcontractedTaskData().getId();
        Long idCommunication = customerCommunication.getId();

        subcontractorCommunicationDAO.remove(customerCommunication.getId());

        try{
            subcontractorCommunicationDAO.findExistingEntity(idCommunication);
            fail("error");
        }catch(RuntimeException e){
            //ok
        }
        try{
            subcontractedTaskDataDAO.findExistingEntity(idSubcontratecTaskData);
        }catch(RuntimeException e){
            fail("error");
        }
    }

    @Test
    public void testSaveCustomerCommunicationWithoutSubcontratedTaskData()
            throws InstanceNotFoundException {
        SubcontractorCommunication subcontractorCommunication = createValidSubcontractorCommunication();
        subcontractorCommunication.setSubcontractedTaskData(null);
        try {
            subcontractorCommunicationDAO.save(subcontractorCommunication);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // Ok
        }
    }

}