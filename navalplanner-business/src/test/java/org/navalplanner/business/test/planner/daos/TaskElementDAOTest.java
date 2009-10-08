/*
 * This file is part of ###PROJECT_NAME###
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

package org.navalplanner.business.test.planner.daos;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Date;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.daos.TaskElementDAO;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.TaskMilestone;
import org.navalplanner.business.planner.entities.Dependency.Type;
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
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class TaskElementDAOTest {

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private SessionFactory sessionFactory;

    private HoursGroup associatedHoursGroup;

    private Task createValidTask() {
        associatedHoursGroup = new HoursGroup();
        Task task = Task.createTask(associatedHoursGroup);
        OrderLine orderLine = createOrderLine();
        orderLine.addHoursGroup(associatedHoursGroup);
        task.setOrderElement(orderLine);
        return task;
    }

    private OrderLine createOrderLine() {
        OrderLine orderLine = OrderLine.create();
        orderLine.setName("bla");
        orderLine.setCode("000000000");
        orderLine.addHoursGroup(new HoursGroup());
        Order order = Order.create();
        order.setName("bla");
        order.setInitDate(new Date());
        order.add(orderLine);
        try {
            order.checkValid();
            orderDAO.save(order);
            sessionFactory.getCurrentSession().flush();
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        return orderLine;
    }

    private TaskGroup createValidTaskGroup() {
        TaskGroup result = TaskGroup.create();
        OrderLine orderLine = createOrderLine();
        result.setOrderElement(orderLine);
        return result;
    }

    private TaskMilestone createValidTaskMilestone() {
        TaskMilestone result = TaskMilestone.create();
        return result;
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
        assertThat(fromDB, is(Task.class));
        checkProperties(task, fromDB);
        HoursGroup reloaded = ((Task) fromDB).getHoursGroup();
        assertThat(reloaded.getId(), equalTo(reloaded.getId()));
    }

    @Test
    public void canSaveMilestone() {
        TaskMilestone milestone = createValidTaskMilestone();
        taskElementDAO.save(milestone);
        // flushAndEvict(milestone);
        // TaskElement fromDB;
        // try {
        // fromDB = taskElementDAO.find(milestone.getId());
        // } catch (InstanceNotFoundException e) {
        // throw new RuntimeException(e);
        // }
        // assertThat(fromDB.getId(), equalTo(milestone.getId()));
        // assertThat(fromDB, is(TaskMilestone.class));
    }

    @Test
    public void afterSavingTheVersionIsIncreased() {
        Task task = createValidTask();
        assertNull(task.getVersion());
        taskElementDAO.save(task);
        task.dontPoseAsTransientObjectAnymore();
        assertNotNull(task.getVersion());
    }

    @Test
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
        assertThat(reloaded, is(TaskGroup.class));
        checkProperties(taskGroup, reloaded);
    }

    @Test
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
    public void savingTaskElementSavesAssociatedDependencies() {
        Task child1 = createValidTask();
        Task child2 = createValidTask();
        taskElementDAO.save(child2);
        Task oldChild2 = child2;
        flushAndEvict(child2);
        try {
            child2 = (Task) taskElementDAO.find(child2.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        Dependency.create(child1, oldChild2, Type.START_END);
        taskElementDAO.save(child1);
        oldChild2.dontPoseAsTransientObjectAnymore();
        flushAndEvict(child1);
        TaskElement child1Reloaded;
        try {
            child1Reloaded = (TaskElement) taskElementDAO.find(child1.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        assertThat(child1Reloaded.getDependenciesWithThisOrigin().size(),
                equalTo(1));
        assertTrue(child1Reloaded.getDependenciesWithThisDestination()
                .isEmpty());

        assertThat(child2.getDependenciesWithThisDestination().size(),
                equalTo(1));
        assertTrue(child2.getDependenciesWithThisOrigin().isEmpty());
    }

    @Test
    public void testInverseManyToOneRelationshipInOrderElement() {
        Task task = createValidTask();
        taskElementDAO.save(task);
        flushAndEvict(task);
        sessionFactory.getCurrentSession().evict(task.getOrderElement());
        TaskElement fromDB;
        try {
            fromDB = taskElementDAO.find(task.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        OrderElement orderElement = fromDB.getOrderElement();
        assertThat(orderElement.getTaskElements().size(), equalTo(1));
        assertThat(orderElement.getTaskElements().iterator().next(),
                equalTo(fromDB));
    }

    @Test
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
        assertNull(sessionFactory.getCurrentSession().get(TaskElement.class,
                task.getId()));
    }

    @Test
    public void aTaskGroupCanBeRemoved() {
        TaskGroup taskGroup = createValidTaskGroup();
        Task task = createValidTask();
        taskGroup.addTaskElement(task);
        taskElementDAO.save(taskGroup);
        flushAndEvict(taskGroup);
        try {
            taskElementDAO.remove(taskGroup.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        sessionFactory.getCurrentSession().flush();
        assertNull(sessionFactory.getCurrentSession().get(TaskGroup.class,
                taskGroup.getId()));
        assertNull(sessionFactory.getCurrentSession().get(TaskElement.class,
                task.getId()));
    }

}
