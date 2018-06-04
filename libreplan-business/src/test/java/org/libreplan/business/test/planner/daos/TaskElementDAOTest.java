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

package org.libreplan.business.test.planner.daos;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.externalcompanies.daos.IExternalCompanyDAO;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.orders.entities.SchedulingDataForVersion;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.orders.entities.TaskSource.IOptionalPersistence;
import org.libreplan.business.orders.entities.TaskSource.TaskGroupSynchronization;
import org.libreplan.business.orders.entities.TaskSource.TaskSourceSynchronization;
import org.libreplan.business.planner.daos.ISubcontractedTaskDataDAO;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.daos.ITaskSourceDAO;
import org.libreplan.business.planner.daos.TaskElementDAO;
import org.libreplan.business.planner.entities.Dependency;
import org.libreplan.business.planner.entities.Dependency.Type;
import org.libreplan.business.planner.entities.SpecificResourceAllocation;
import org.libreplan.business.planner.entities.SubcontractedTaskData;
import org.libreplan.business.planner.entities.SubcontractorDeliverDate;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.planner.entities.TaskMilestone;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.bootstrap.IScenariosBootstrap;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.test.externalcompanies.daos.ExternalCompanyDAOTest;
import org.libreplan.business.workingday.IntraDayDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test cases for {@link TaskElementDAO}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE, BUSINESS_SPRING_CONFIG_TEST_FILE })
public class TaskElementDAOTest {

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private ISubcontractedTaskDataDAO subcontractedTaskDataDAO;

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private IResourceDAO resourceDAO;

    @Before
    public void loadRequiredData() {
        scenariosBootstrap.loadRequiredData();
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
        configurationBootstrap.loadRequiredData();
    }

    private Task createValidTask() {
        HoursGroup associatedHoursGroup = new HoursGroup();
        associatedHoursGroup.setCode("hours-group-code-" + UUID.randomUUID());
        OrderLine orderLine = createOrderLine();
        orderLine.addHoursGroup(associatedHoursGroup);
        OrderVersion orderVersion = ResourceAllocationDAOTest.setupVersionUsing(scenarioManager, orderLine.getOrder());
        orderLine.useSchedulingDataFor(orderVersion);
        SchedulingDataForVersion schedulingDataForVersion = orderLine.getCurrentSchedulingDataForVersion();

        TaskSource taskSource = TaskSource.create(schedulingDataForVersion, Collections.singletonList(associatedHoursGroup));

        TaskSourceSynchronization mustAdd = TaskSource.mustAdd(taskSource);
        mustAdd.apply(TaskSource.persistTaskSources(taskSourceDAO));

        return (Task) taskSource.getTask();
    }

    private OrderLine createOrderLine() {
        OrderLine orderLine = OrderLine.create();
        orderLine.setName("bla");
        orderLine.setCode("code-" + UUID.randomUUID());
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setCode("hours-group-code-" + UUID.randomUUID());
        orderLine.addHoursGroup(hoursGroup);
        Order order = Order.create();
        OrderVersion orderVersion = ResourceAllocationDAOTest.setupVersionUsing(scenarioManager, order);
        order.setName("bla-" + UUID.randomUUID());
        order.setInitDate(new Date());
        order.setCode("code-" + UUID.randomUUID());
        order.useSchedulingDataFor(orderVersion);
        order.add(orderLine);
        order.setCalendar(configurationDAO.getConfiguration().getDefaultCalendar());
        try {
            orderDAO.save(order);
            sessionFactory.getCurrentSession().flush();
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        return orderLine;
    }

    private TaskGroup createValidTaskGroup() {
        OrderLine orderLine = createOrderLine();
        OrderVersion orderVersion = ResourceAllocationDAOTest.setupVersionUsing(scenarioManager, orderLine.getOrder());
        orderLine.useSchedulingDataFor(orderVersion);
        SchedulingDataForVersion schedulingDataForVersion = orderLine.getCurrentSchedulingDataForVersion();
        TaskSource taskSource = TaskSource.createForGroup(schedulingDataForVersion);

        TaskGroupSynchronization synchronization = new TaskGroupSynchronization(taskSource, Collections.emptyList()) {

            @Override
            protected TaskElement apply(List<TaskElement> children, IOptionalPersistence persistence) {
                TaskGroup result = TaskGroup.create(taskSource);
                Date today = new Date();
                result.setStartDate(today);
                result.setEndDate(plusDays(today, 3));
                setTask(taskSource, result);
                taskSourceDAO.save(taskSource);

                return result;
            }

        };
        synchronization.apply(TaskSource.persistTaskSources(taskSourceDAO));

        return (TaskGroup) taskSource.getTask();
    }

    private Date plusDays(Date today, int days) {
        LocalDate result = LocalDate.fromDateFields(today).plusDays(days);
        return result.toDateTimeAtStartOfDay().toDate();
    }

    private TaskMilestone createValidTaskMilestone() {
        return TaskMilestone.create(new Date());
    }

    private void checkProperties(TaskElement inMemory, TaskElement fromDB) {
        assertThat(fromDB.getStartDate(), equalTo(inMemory.getStartDate()));
        assertThat(fromDB.getEndDate(), equalTo(inMemory.getEndDate()));
    }

    private void flushAndEvict(Object entity) {
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().evict(entity);
    }

    @Test
    @Transactional
    public void canSaveTask() {
        Task task = createValidTask();
        taskElementDAO.save(task);
        flushAndEvict(task);
        TaskElement fromDB;
        try {
            fromDB = taskElementDAO.find(task.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        assertThat(fromDB.getId(), equalTo(task.getId()));
        assertThat(fromDB, instanceOf(Task.class));
        checkProperties(task, fromDB);
        HoursGroup reloaded = ((Task) fromDB).getHoursGroup();
        assertThat(reloaded.getId(), equalTo(reloaded.getId()));
    }

    @Test
    @Transactional
    public void canSaveMilestone() {
        TaskMilestone milestone = createValidTaskMilestone();
        taskElementDAO.save(milestone);
        flushAndEvict(milestone);
        TaskElement fromDB;
        try {
            fromDB = taskElementDAO.find(milestone.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        assertThat(fromDB.getId(), equalTo(milestone.getId()));
        assertThat(fromDB, instanceOf(TaskMilestone.class));
    }

    @Test
    @Transactional
    public void afterSavingTheVersionIsIncreased() {
        Task task = createValidTask();
        assertNull(task.getVersion());
        taskElementDAO.save(task);
        task.dontPoseAsTransientObjectAnymore();
        assertNotNull(task.getVersion());
    }

    @Test
    @Transactional
    public void canSaveTaskGroup() {
        TaskGroup taskGroup = createValidTaskGroup();
        taskElementDAO.save(taskGroup);
        flushAndEvict(taskGroup);
        TaskElement reloaded;
        try {
            reloaded = taskElementDAO.find(taskGroup.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        assertThat(reloaded.getId(), equalTo(taskGroup.getId()));
        assertThat(reloaded, instanceOf(TaskGroup.class));
        checkProperties(taskGroup, reloaded);
    }

    @Test
    @Transactional
    public void theParentPropertyIsPresentWhenRetrievingTasks() {
        TaskGroup taskGroup = createValidTaskGroup();
        taskGroup.addTaskElement(createValidTask());
        taskElementDAO.save(taskGroup);
        flushAndEvict(taskGroup);
        TaskElement reloaded;
        try {
            reloaded = taskElementDAO.find(taskGroup.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        TaskElement child = reloaded.getChildren().get(0);
        assertThat(child.getParent(), equalTo(reloaded));
    }

    @Test
    @Transactional
    public void theTopMostPropertyIsTheToplevelTask() {
        TaskGroup taskGroup = createValidTaskGroup();
        TaskElement taskElement = createValidTask();
        taskGroup.addTaskElement(taskElement);
        taskElementDAO.save(taskGroup);

        TaskGroup taskGroup2 = createValidTaskGroup();
        TaskElement taskElement2 = createValidTask();
        taskGroup2.addTaskElement(taskElement2);
        taskElementDAO.save(taskGroup2);

        taskGroup.addTaskElement(taskGroup2);
        taskElementDAO.save(taskGroup);

        flushAndEvict(taskGroup);
        flushAndEvict(taskGroup2);

        TaskElement reloaded;
        try {
            reloaded = taskElementDAO.find(taskGroup.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }

        List<TaskElement> children = reloaded.getChildren();

        for (TaskElement child : children) {
            if (child instanceof TaskGroup) {
                TaskElement child2 = child.getChildren().get(0);
                assertThat(child2.getTopMost(), equalTo(reloaded));
            }
        }
    }

    @Test
    @Transactional
    public void savingGroupSavesAssociatedTaskElements() {
        Task child1 = createValidTask();
        Task child2 = createValidTask();
        TaskGroup taskGroup = createValidTaskGroup();
        taskGroup.addTaskElement(child1);
        taskGroup.addTaskElement(child2);
        taskElementDAO.save(taskGroup);
        flushAndEvict(taskGroup);
        TaskGroup reloaded;
        try {
            reloaded = (TaskGroup) taskElementDAO.find(taskGroup.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        List<TaskElement> taskElements = reloaded.getChildren();
        assertThat(taskElements.size(), equalTo(2));
        assertThat(taskElements.get(0).getId(), equalTo(child1.getId()));
        assertThat(taskElements.get(1).getId(), equalTo(child2.getId()));

    }

    @Test
    public void savingTaskElementSavesAssociatedDependencies() throws InstanceNotFoundException {
        IOnTransaction<Task> createValidTask = new IOnTransaction<Task>() {
            @Override
            public Task execute() {
                return createValidTask();
            }
        };

        final Task child1 = transactionService.runOnTransaction(createValidTask);
        final Task child2 = transactionService.runOnTransaction(createValidTask);

        IOnTransaction<Void> createDependency = new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                child1.dontPoseAsTransientObjectAnymore();
                child2.dontPoseAsTransientObjectAnymore();
                Dependency.create(child1, child2, Type.START_END);
                taskElementDAO.save(child1);

                return null;
            }
        };

        transactionService.runOnTransaction(createDependency);
        assertThat(child2.getDependenciesWithThisDestination().size(), equalTo(1));
        assertTrue(child2.getDependenciesWithThisOrigin().isEmpty());

        IOnTransaction<Void> checkDependencyWasSaved = new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                TaskElement fromDB = taskElementDAO.findExistingEntity(child1.getId());
                assertThat(fromDB.getDependenciesWithThisOrigin().size(), equalTo(1));
                assertTrue(fromDB.getDependenciesWithThisDestination().isEmpty());

                return null;
            }
        };

        transactionService.runOnTransaction(checkDependencyWasSaved);
    }

    public void aTaskCanBeRemoved() {
        Task task = createValidTask();
        taskElementDAO.save(task);
        flushAndEvict(task);
        try {
            taskElementDAO.remove(task.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        sessionFactory.getCurrentSession().flush();
        assertNull(sessionFactory.getCurrentSession().get(TaskElement.class, task.getId()));
    }

    @Test
    public void aTaskCanBeRemovedFromItsTaskSource() {
        final Task task = transactionService.runOnTransaction(new IOnTransaction<Task>(){
            @Override
            public Task execute() {
                Task task = createValidTask();
                taskElementDAO.save(task);

                return task;
            }});

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    taskSourceDAO.remove(task.getTaskSource().getId());
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }

                sessionFactory.getCurrentSession().flush();
                assertNull(sessionFactory.getCurrentSession().get(TaskElement.class, task.getId()));

                return null;
            }});
    }

    @Test
    public void aTaskGroupCanBeRemovedFromItsTaskSourceIfBelowTasksSourcesAreRemovedFirst() {
        final TaskGroup taskGroupWithOneChild = transactionService.runOnTransaction(new IOnTransaction<TaskGroup>() {
            @Override
            public TaskGroup execute() {
                TaskGroup taskGroup = createValidTaskGroup();
                Task task = createValidTask();
                taskGroup.addTaskElement(task);
                return taskGroup;
            }
        });

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    taskSourceDAO.remove(taskGroupWithOneChild.getChildren().get(0).getTaskSource().getId());
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
                try {
                    taskSourceDAO.remove(taskGroupWithOneChild.getTaskSource().getId());
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
                sessionFactory.getCurrentSession().flush();
                assertNull(sessionFactory.getCurrentSession().get(TaskElement.class, taskGroupWithOneChild.getId()));
                return null;
            }
        });
    }

    @Autowired
    private IExternalCompanyDAO externalCompanyDAO;

    private ExternalCompany getSubcontractorExternalCompanySaved() {
        ExternalCompany externalCompany = ExternalCompanyDAOTest.createValidExternalCompany();
        externalCompany.setSubcontractor(true);

        externalCompanyDAO.save(externalCompany);
        externalCompanyDAO.flush();
        sessionFactory.getCurrentSession().evict(externalCompany);

        externalCompany.dontPoseAsTransientObjectAnymore();

        return externalCompany;
    }

    @Test
    @Transactional
    public void testStoreSubcontractedTaskData() throws InstanceNotFoundException {
        Task task = createValidTask();

        SubcontractedTaskData subcontractedTaskData = SubcontractedTaskData.create(task);
        subcontractedTaskData.addRequiredDeliveringDates(SubcontractorDeliverDate.create(new Date(),new Date(), null));
        subcontractedTaskData.setExternalCompany(getSubcontractorExternalCompanySaved());

        task.setSubcontractedTaskData(subcontractedTaskData);
        taskElementDAO.save(task);
        taskElementDAO.flush();
        sessionFactory.getCurrentSession().evict(task);
        sessionFactory.getCurrentSession().evict(subcontractedTaskData);

        Task taskFound = (Task) taskElementDAO.find(task.getId());
        assertNotNull(taskFound.getSubcontractedTaskData());

        SubcontractedTaskData subcontractedTaskDataFound = subcontractedTaskDataDAO.find(subcontractedTaskData.getId());
        assertNotNull(subcontractedTaskDataFound.getTask());
    }

    private org.libreplan.business.resources.entities.Resource createValidWorker() {
        Worker worker = Worker.create();
        worker.setFirstName(UUID.randomUUID().toString());
        worker.setSurname(UUID.randomUUID().toString());
        worker.setNif(UUID.randomUUID().toString());
        resourceDAO.save(worker);
        return worker;
    }

    @Test
    @Transactional
    public void testSaveTaskElementUpdatesSumOfHoursAllocatedAttribute() throws InstanceNotFoundException {
        IOnTransaction<Long> createTaskElement = new IOnTransaction<Long>() {
            @Override
            public Long execute() {
                Task task = createValidTask();
                TaskGroup taskGroup = createValidTaskGroup();
                taskGroup.addTaskElement(task);

                SpecificResourceAllocation allocation = SpecificResourceAllocation.create(task);
                allocation.setResource(createValidWorker());
                LocalDate start = task.getStartAsLocalDate();
                task.setIntraDayEndDate(IntraDayDate.startOfDay(start.plusDays(3)));
                allocation.onIntervalWithinTask(start, start.plusDays(3)).allocateHours(24);
                assertTrue(allocation.getAssignedHours() > 0);

                task.addResourceAllocation(allocation);
                taskElementDAO.save(taskGroup);

                return task.getId();
            }
        };

        final Long id = transactionService.runOnTransaction(createTaskElement);

        IOnTransaction<Void> checkAllocatedHoursWereUpdated = new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                TaskElement task1;
                try {
                    task1 = taskElementDAO.find(id);
                } catch (InstanceNotFoundException e) {
                    fail();
                    return null;
                }
                assertTrue(task1.getSumOfAssignedEffort().getHours() == 24);
                assertTrue(task1.getParent().getSumOfAssignedEffort().getHours() == 24);

                return null;
            }
        };

        transactionService.runOnTransaction(checkAllocatedHoursWereUpdated);
    }

}
