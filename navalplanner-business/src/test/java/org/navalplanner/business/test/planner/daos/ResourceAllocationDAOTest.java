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

package org.navalplanner.business.test.planner.daos;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IHoursGroupDAO;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.orders.entities.TaskSource.TaskSourceSynchronization;
import org.navalplanner.business.planner.daos.IResourceAllocationDAO;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.daos.IWorkerDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.bootstrap.IScenariosBootstrap;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.workingday.ResourcesPerDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/*
 * @author Diego Pino García <dpino@igalia.com>
 */
@Transactional
public class ResourceAllocationDAOTest {

    public static OrderVersion setupVersionUsing(
            IScenarioManager scenarioManager, Order order) {
        Scenario current = scenarioManager.getCurrent();
        OrderVersion result = OrderVersion.createInitialVersion(current);
        order.setVersionForScenario(current, result);
        return result;
    }

    @Autowired
    private IResourceAllocationDAO resourceAllocationDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private IHoursGroupDAO hoursGroupDAO;

    @Autowired
    private IWorkerDAO workerDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    IResourceDAO resourceDAO;

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Autowired
    private IScenarioManager scenarioManager;

    enum ResourceAllocationType {
        SPECIFIC_RESOURCE_ALLOCATION, GENERIC_RESOURCE_ALLOCATION
    }

    @Before
    public void loadRequiredData() {
        scenariosBootstrap.loadRequiredData();
    }

    private OrderLine createValidOrderLine() {
        Order order = new Order();
        order.setInitDate(new Date());
        OrderVersion orderVersion = setupVersionUsing(scenarioManager, order);
        order.useSchedulingDataFor(orderVersion);
        OrderLine orderLine = OrderLine.create();
        order.add(orderLine);
        orderLine.setName(UUID.randomUUID().toString());
        orderLine.setCode(UUID.randomUUID().toString());
        orderElementDAO.save(orderLine);
        return orderLine;
    }

    private Resource createValidWorker() {
        Worker worker = Worker.create();
        worker.setFirstName(UUID.randomUUID().toString());
        worker.setSurname(UUID.randomUUID().toString());
        worker.setNif(UUID.randomUUID().toString());
        resourceDAO.save(worker);
        return worker;
    }

    private ResourceAllocation<?> createValidResourceAllocation(
            ResourceAllocationType type) {
        OrderLine orderLine = createValidOrderLine();
        Order order = orderLine.getOrder();
        OrderVersion orderVersion = setupVersionUsing(scenarioManager,
                order);
        orderLine.useSchedulingDataFor(orderVersion);
        assert orderLine.getSchedulingStateType().isSomewhatScheduled();

        orderElementDAO.save(orderLine);

        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setCode(UUID.randomUUID().toString());
        hoursGroupDAO.save(hoursGroup);

        List<HoursGroup> hoursGroups = Arrays.asList(hoursGroup);
        TaskSource taskSource = TaskSource.create(orderLine
                        .getCurrentSchedulingDataForVersion(),
                        hoursGroups);
        TaskSourceSynchronization synchronization = TaskSource
                .mustAdd(taskSource);
        synchronization.apply(TaskSource.persistTaskSources(taskSourceDAO));
        Task task = (Task) taskSource.getTask();
        if (ResourceAllocationType.SPECIFIC_RESOURCE_ALLOCATION.equals(type)) {
            SpecificResourceAllocation specificResourceAllocation = SpecificResourceAllocation
                    .createForTesting(ResourcesPerDay.amount(1),
                            task);
            Worker worker = (Worker) createValidWorker();
            resourceDAO.save(worker);
            specificResourceAllocation.setResource(worker);

            return specificResourceAllocation;
        }
        if (ResourceAllocationType.GENERIC_RESOURCE_ALLOCATION.equals(type)) {
            GenericResourceAllocation specificResourceAllocation = GenericResourceAllocation
                    .createForTesting(ResourcesPerDay.amount(1), task);
            return specificResourceAllocation;
        }
        return null;
    }

    private SpecificResourceAllocation createValidSpecificResourceAllocation() {
        return (SpecificResourceAllocation) createValidResourceAllocation(ResourceAllocationType.SPECIFIC_RESOURCE_ALLOCATION);
    }

    private GenericResourceAllocation createValidGenericResourceAllocation() {
        return (GenericResourceAllocation) createValidResourceAllocation(ResourceAllocationType.GENERIC_RESOURCE_ALLOCATION);
    }

    @Test
    public void testInSpringContainer() {
        assertNotNull(resourceAllocationDAO);
        assertNotNull(orderElementDAO);
        assertNotNull(taskElementDAO);
        assertNotNull(hoursGroupDAO);
        assertNotNull(workerDAO);
        assertNotNull(resourceDAO);
    }

    @Test
    public void testSaveSpecificResourceAllocation() {
        SpecificResourceAllocation resourceAllocation = createValidSpecificResourceAllocation();
        resourceAllocationDAO.save(resourceAllocation);
        assertTrue(resourceAllocationDAO.exists(resourceAllocation.getId()));
    }

    @Test
    public void testSaveGenericResourceAllocation() {
        GenericResourceAllocation resourceAllocation = createValidGenericResourceAllocation();
        resourceAllocationDAO.save(resourceAllocation);
        assertTrue(resourceAllocationDAO.exists(resourceAllocation.getId()));
    }

    @Test
    public void testRemoveSpecificResourceAllocation()
            throws InstanceNotFoundException {
        SpecificResourceAllocation resourceAllocation = createValidSpecificResourceAllocation();
        resourceAllocationDAO.save(resourceAllocation);
        resourceAllocationDAO.remove(resourceAllocation.getId());
        assertFalse(resourceAllocationDAO.exists(resourceAllocation.getId()));
    }

    @Test
    public void testRemoveGenericResourceAllocation()
            throws InstanceNotFoundException {
        GenericResourceAllocation resourceAllocation = createValidGenericResourceAllocation();
        resourceAllocationDAO.save(resourceAllocation);
        resourceAllocationDAO.remove(resourceAllocation.getId());
        assertFalse(resourceAllocationDAO.exists(resourceAllocation.getId()));
    }

    @Test
    public void testListSpecificResourceAllocation() {
        int previous = resourceAllocationDAO.list(ResourceAllocation.class).size();

        SpecificResourceAllocation resourceAllocation1 = createValidSpecificResourceAllocation();
        resourceAllocationDAO.save(resourceAllocation1);
        ResourceAllocation<?> resourceAllocation2 = createValidSpecificResourceAllocation();
        resourceAllocationDAO.save(resourceAllocation1);
        resourceAllocationDAO.save(resourceAllocation2);

        List<SpecificResourceAllocation> list = resourceAllocationDAO
                .list(SpecificResourceAllocation.class);
        assertEquals(previous + 2, list.size());
    }

    @Test
    public void testListGenericResourceAllocation() {
        int previous = resourceAllocationDAO.list(ResourceAllocation.class)
                .size();

        GenericResourceAllocation resourceAllocation1 = createValidGenericResourceAllocation();
        resourceAllocationDAO.save(resourceAllocation1);
        ResourceAllocation<?> resourceAllocation2 = createValidGenericResourceAllocation();
        resourceAllocationDAO.save(resourceAllocation1);
        resourceAllocationDAO.save(resourceAllocation2);

        List<GenericResourceAllocation> list = resourceAllocationDAO
                .list(GenericResourceAllocation.class);
        assertEquals(previous + 2, list.size());
    }

    @Test
    public void testFindAllocationsRelatedToResourcesWithDateFilter() {
        Scenario current = scenarioManager.getCurrent();
        ResourceAllocation<?> resourceAllocation1 = createValidSpecificResourceAllocation();
        resourceAllocationDAO.save(resourceAllocation1);

        LocalDate intervalInitDate = resourceAllocation1.getTask()
                .getStartAsLocalDate();
        LocalDate intervalEndDate = resourceAllocation1.getTask()
                .getEndAsLocalDate();
        List<Resource> resources = resourceAllocation1.getAssociatedResources();

        assertTrue(resourceAllocationDAO.findAllocationsRelatedToAnyOf(
                current, resources,
                intervalInitDate, intervalEndDate).contains(resourceAllocation1));

        intervalEndDate = intervalInitDate;
        intervalInitDate = intervalInitDate.minusMonths(1);
        assertTrue(resourceAllocationDAO.findAllocationsRelatedToAnyOf(current,
                resources,
                intervalInitDate, intervalEndDate).contains(resourceAllocation1));

        intervalEndDate = intervalEndDate.minusMonths(1);
        assertFalse(resourceAllocationDAO.findAllocationsRelatedToAnyOf(
                current, resources,
                intervalInitDate, intervalEndDate).contains(resourceAllocation1));

        assertTrue(resourceAllocationDAO.findAllocationsRelatedToAnyOf(current,
                resources,
                intervalInitDate, null).contains(resourceAllocation1));
  }
}
