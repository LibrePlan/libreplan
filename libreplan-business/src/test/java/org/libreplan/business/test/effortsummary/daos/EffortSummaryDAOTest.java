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

package org.libreplan.business.test.effortsummary.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.effortsummary.daos.IEffortSummaryDAO;
import org.libreplan.business.effortsummary.entities.EffortSummary;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.orders.entities.SchedulingDataForVersion;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.orders.entities.TaskSource.TaskSourceSynchronization;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.daos.ITaskSourceDAO;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.bootstrap.IScenariosBootstrap;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.test.planner.daos.ResourceAllocationDAOTest;
import org.libreplan.business.workingday.EffortDuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class EffortSummaryDAOTest {

    @Autowired
    private IEffortSummaryDAO effortSummaryDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private SessionFactory sessionFactory;

    @javax.annotation.Resource
    private IDataBootstrap configurationBootstrap;

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Before
    public void loadRequiredaData() {
        configurationBootstrap.loadRequiredData();
        scenariosBootstrap.loadRequiredData();
    }

    public static Worker generateValidWorker() {
        Worker worker = Worker.create();
        worker.setFirstName("First name");
        worker.setSurname("Surname");
        worker.setNif("NIF" + UUID.randomUUID().toString());
        return worker;
    }

    public EffortSummary generateValidEffortSummary(int numberOfItems,
            Resource resource, LocalDate startDate) {
        return generateValidEffortSummaryForTask(numberOfItems, resource, null,
                startDate);
    }

    public EffortSummary generateValidEffortSummaryForTask(int numberOfItems,
            Resource resource, Task task, LocalDate startDate) {
        LocalDate startDate2 = new LocalDate(startDate);
        LocalDate endDate = startDate.plusDays(numberOfItems - 1);
        int[] availableEffort = new int[numberOfItems];
        int[] assignedEffort = new int[numberOfItems];
        Random generator = new Random();

        for (int i = 0; i < numberOfItems; i++) {
            availableEffort[i] = generator.nextInt(86400);
            assignedEffort[i] = generator.nextInt(86400);
        }
        EffortSummary summary = EffortSummary.create(startDate2, endDate,
                availableEffort, assignedEffort, resource, task);
        return summary;
    }

    private Task generateValidTask() {
        HoursGroup associatedHoursGroup = new HoursGroup();
        associatedHoursGroup.setCode("hours-group-code-" + UUID.randomUUID());
        OrderLine orderLine = generateValidOrderLine();
        orderLine.addHoursGroup(associatedHoursGroup);
        OrderVersion orderVersion = ResourceAllocationDAOTest
                .setupVersionUsing(scenarioManager, orderLine.getOrder());
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

    private OrderLine generateValidOrderLine() {
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
        order.setCalendar(configurationDAO.getConfiguration()
                .getDefaultCalendar());
        try {
            orderDAO.save(order);
            sessionFactory.getCurrentSession().flush();
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        return orderLine;
    }

    @Test
    public void testList() {
        final int numberOfItems = 1000;
        Worker worker = generateValidWorker();
        resourceDAO.save(worker);
        LocalDate date = new LocalDate();
        EffortSummary summary = generateValidEffortSummary(
                numberOfItems, worker, date);
        effortSummaryDAO.save(summary);

        List<EffortSummary> list = effortSummaryDAO.list();
        assertEquals(1, list.size());
        EffortSummary effort = list.get(0);
        assertEquals(numberOfItems, effort.getAssignedEffort().length);
        assertEquals(numberOfItems, effort.getAvailableEffort().length);
    }

    @Test
    public void testListBetweenDates() {
        final int numberOfItems = 1000;
        Worker worker = generateValidWorker();
        resourceDAO.save(worker);
        LocalDate startDate = new LocalDate();
        LocalDate endDate = startDate.plusDays(numberOfItems - 2);
        EffortSummary summary = generateValidEffortSummary(
                numberOfItems, worker, startDate);
        effortSummaryDAO.save(summary);

        EffortSummary effort = effortSummaryDAO
                .listForResourceBetweenDates(worker, startDate, endDate);
        assertEquals(numberOfItems - 1, effort.getAssignedEffort().length);
    }

    @Test
    public void testFindByResource() {
        final int numberOfItems = 1000;
        Worker worker = generateValidWorker();
        resourceDAO.save(worker);
        LocalDate date = new LocalDate();
        EffortSummary summary = generateValidEffortSummary(
                numberOfItems, worker, date);
        effortSummaryDAO.save(summary);
        EffortSummary effort = effortSummaryDAO.findGlobalInformationForResource(worker);
        assertEquals(effort.getResource().getId(), worker.getId());
    }

    @Test
    public void testFindByResourceNotFound() {
        Worker worker = generateValidWorker();
        resourceDAO.save(worker);
        EffortSummary effort = effortSummaryDAO.findGlobalInformationForResource(worker);
        assertNull(effort);
    }

    @Test
    public void testSaveOrUpdate() {
        LocalDate date = new LocalDate();
        final int numberOfItems = 1000;

        // create global effort
        Worker worker = generateValidWorker();
        resourceDAO.save(worker);
        resourceDAO.flush();
        worker.dontPoseAsTransientObjectAnymore();
        EffortSummary global = generateValidEffortSummary(numberOfItems,
                worker, date);
        effortSummaryDAO.save(global);

        // store original assigned effort for two days to compare later
        EffortDuration effortDay1 = global.getAssignedEffortForDate(date);
        EffortDuration effortDay5 = global.getAssignedEffortForDate(date
                .plusDays(5));

        // create task effort
        Task task = generateValidTask();
        taskElementDAO.save(task);
        taskElementDAO.flush();
        task.dontPoseAsTransientObjectAnymore();
        EffortSummary taskEffort = generateValidEffortSummaryForTask(
                numberOfItems, worker, task, date);
        effortSummaryDAO.saveOrUpdate(Collections.singleton(taskEffort));

        // store new assigned effort for the same days
        EffortDuration taskEffortDay1 = taskEffort
                .getAssignedEffortForDate(date);
        EffortDuration taskEffortDay5 = taskEffort
                .getAssignedEffortForDate(date.plusDays(5));

        // retrieve updated global effort
        EffortSummary globalEffort = effortSummaryDAO
                .findGlobalInformationForResource(worker);
        assertNotNull(globalEffort);

        // check that the two efforts have been added
        assertEquals(effortDay1.plus(taskEffortDay1),
                globalEffort.getAssignedEffortForDate(date));
        assertEquals(effortDay5.plus(taskEffortDay5),
                globalEffort.getAssignedEffortForDate(date.plusDays(5)));

        // replace taskEffort with an empty object
        taskEffort = EffortSummary.create(date, date, new int[0], new int[0],
                worker, task);
        effortSummaryDAO.saveOrUpdate(Collections.singleton(taskEffort));

        // retrieve updated global effort
        globalEffort = effortSummaryDAO
                .findGlobalInformationForResource(worker);

        // check that the two efforts have been added and the old one was
        // substracted
        assertEquals(effortDay1, globalEffort.getAssignedEffortForDate(date));
        assertEquals(effortDay5,
                globalEffort.getAssignedEffortForDate(date.plusDays(5)));
    }
}
