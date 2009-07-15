package org.navalplanner.business.test.planner.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
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
import org.junit.matchers.JUnitMatchers;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.orders.services.IOrderService;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.Dependency.Type;
import org.navalplanner.business.planner.services.ITaskElementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class TaskElementServiceTest {

    @Autowired
    private ITaskElementService taskElementService;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IOrderService orderService;

    private HoursGroup associatedHoursGroup;

    @Test
    public void canSaveTask() {
        Task task = createValidTask();
        taskElementService.save(task);
        flushAndEvict(task);
        TaskElement fromDB = taskElementService.findById(task.getId());
        assertThat(fromDB.getId(), equalTo(task.getId()));
        assertThat(fromDB, is(Task.class));
        checkProperties(task, fromDB);
        HoursGroup reloaded = ((Task) fromDB).getHoursGroup();
        assertThat(reloaded.getId(), equalTo(reloaded.getId()));
    }

    private Task createValidTask() {
        associatedHoursGroup = new HoursGroup();
        Task task = Task.createTask(associatedHoursGroup);
        OrderLine orderLine = createOrderLine();
        orderLine.addHoursGroup(associatedHoursGroup);
        task.setOrderElement(orderLine);
        return task;
    }

    private OrderLine createOrderLine() {
        OrderLine orderLine = new OrderLine();
        orderLine.setName("bla");
        orderLine.addHoursGroup(new HoursGroup());
        Order order = new Order();
        order.setName("bla");
        order.setInitDate(new Date());
        order.add(orderLine);
        try {
            orderService.save(order);
            sessionFactory.getCurrentSession().flush();
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        return orderLine;
    }

    @Test
    public void afterSavingTheVersionIsIncreased() {
        Task task = createValidTask();
        assertNull(task.getVersion());
        taskElementService.save(task);
        assertNotNull(task.getVersion());
    }

    @Test
    public void canSaveTaskGroup() {
        TaskGroup taskGroup = createValidTaskGroup();
        taskElementService.save(taskGroup);
        flushAndEvict(taskGroup);
        TaskElement reloaded = taskElementService.findById(taskGroup.getId());
        assertThat(reloaded.getId(), equalTo(taskGroup.getId()));
        assertThat(reloaded, is(TaskGroup.class));
        checkProperties(taskGroup, reloaded);
    }

    private TaskGroup createValidTaskGroup() {
        TaskGroup result = new TaskGroup();
        OrderLine orderLine = createOrderLine();
        result.setOrderElement(orderLine);
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
    public void savingGroupSavesAssociatedTaskElements() {
        Task child1 = createValidTask();
        Task child2 = createValidTask();
        TaskGroup taskGroup = createValidTaskGroup();
        taskGroup.addTaskElement(child1);
        taskGroup.addTaskElement(child2);
        taskElementService.save(taskGroup);
        flushAndEvict(taskGroup);
        TaskGroup reloaded = (TaskGroup) taskElementService.findById(taskGroup
                .getId());
        List<TaskElement> taskElements = reloaded.getTaskElements();
        assertThat(taskElements.size(), equalTo(2));
        assertThat(taskElements.get(0).getId(), equalTo(child1.getId()));
        assertThat(taskElements.get(1).getId(), equalTo(child2.getId()));

    }

    @Test
    public void savingTaskElementSavesAssociatedDependencies() {
        Task child1 = createValidTask();
        Task child2 = createValidTask();
        TaskGroup taskGroup = createValidTaskGroup();
        taskGroup.addTaskElement(child1);
        taskGroup.addTaskElement(child2);
        Dependency dependency = Dependency.createDependency(child1, child2,
                Type.START_END);
        taskElementService.save(taskGroup);
        flushAndEvict(taskGroup);
        TaskGroup reloaded = (TaskGroup) taskElementService.findById(taskGroup
                .getId());
        assertThat(reloaded.getTaskElements().get(0)
                .getDependenciesWithThisOrigin().size(), equalTo(1));
        assertTrue(reloaded.getTaskElements().get(0)
                .getDependenciesWithThisDestination().isEmpty());

        assertThat(reloaded.getTaskElements().get(1)
                .getDependenciesWithThisDestination().size(), equalTo(1));
        assertTrue(reloaded.getTaskElements().get(1)
                .getDependenciesWithThisOrigin().isEmpty());
    }

    @Test
    public void aOrderLineGroupIsConvertedToATaskGroup() {
        OrderLineGroup orderLineGroup = new OrderLineGroup();
        orderLineGroup.setName("foo");
        TaskElement task = taskElementService
                .convertToInitialSchedule(orderLineGroup);
        assertThat(task, is(TaskGroup.class));

        TaskGroup group = (TaskGroup) task;
        assertThat(group.getOrderElement(),
                equalTo((OrderElement) orderLineGroup));
    }

    @Test
    public void aOrderLineWithOneHourIsConvertedToATask() {
        OrderLine orderLine = new OrderLine();
        orderLine.setName("bla");
        HoursGroup hoursGroup = createHoursGroup(30);
        orderLine.addHoursGroup(hoursGroup);
        TaskElement taskElement = taskElementService
                .convertToInitialSchedule(orderLine);
        assertThat(taskElement, is(Task.class));

        Task group = (Task) taskElement;
        assertThat(group.getOrderElement(), equalTo((OrderElement) orderLine));
        assertThat(group.getHoursGroup(), equalTo(hoursGroup));
    }

    @Test
    public void theSublinesOfAnOrderLineGroupAreConverted() {
        OrderLineGroup orderLineGroup = new OrderLineGroup();
        orderLineGroup.setName("foo");
        OrderLine orderLine = new OrderLine();
        orderLine.setName("bla");
        HoursGroup hoursGroup = createHoursGroup(30);
        orderLine.addHoursGroup(hoursGroup);
        orderLineGroup.add(orderLine);
        TaskElement task = taskElementService
                .convertToInitialSchedule(orderLineGroup);
        assertThat(task, is(TaskGroup.class));

        TaskGroup group = (TaskGroup) task;

        assertThat(group.getOrderElement(),
                equalTo((OrderElement) orderLineGroup));
        assertThat(group.getTaskElements().size(), equalTo(1));
        assertThat(group.getTaskElements().get(0).getOrderElement(),
                equalTo((OrderElement) orderLine));
    }

    @Test(expected = IllegalArgumentException.class)
    public void aOrderLineWithNoHoursIsRejected() {
        OrderLine orderLine = new OrderLine();
        orderLine.setName("bla");
        taskElementService.convertToInitialSchedule(orderLine);
    }

    private HoursGroup createHoursGroup(int hours) {
        HoursGroup result = new HoursGroup();
        result.setWorkingHours(hours);
        return result;
    }

    @Test
    public void aOrderLineWithMoreThanOneHourIsConvertedToATaskGroup() {
        OrderLine orderLine = new OrderLine();
        orderLine.setName("bla");
        HoursGroup hours1 = createHoursGroup(30);
        orderLine.addHoursGroup(hours1);
        HoursGroup hours2 = createHoursGroup(10);
        orderLine.addHoursGroup(hours2);
        TaskElement taskElement = taskElementService
                .convertToInitialSchedule(orderLine);
        assertThat(taskElement, is(TaskGroup.class));

        TaskGroup group = (TaskGroup) taskElement;
        assertThat(group.getOrderElement(), equalTo((OrderElement) orderLine));
        assertThat(group.getTaskElements().size(), equalTo(2));

        Task child1 = (Task) group.getTaskElements().get(0);
        Task child2 = (Task) group.getTaskElements().get(1);

        assertThat(child1.getOrderElement(), equalTo((OrderElement) orderLine));
        assertThat(child2.getOrderElement(), equalTo((OrderElement) orderLine));

        assertThat(child1.getHoursGroup(), not(equalTo(child2.getHoursGroup())));

        assertThat(child1.getHoursGroup(), JUnitMatchers
                .either(equalTo(hours1)).or(equalTo(hours2)));
        assertThat(child2.getHoursGroup(), JUnitMatchers
                .either(equalTo(hours1)).or(equalTo(hours2)));
    }

    @Test
    public void testInverseManyToOneRelationshipInOrderElement() {
        Task task = createValidTask();
        taskElementService.save(task);
        flushAndEvict(task);
        sessionFactory.getCurrentSession().evict(task.getOrderElement());
        TaskElement fromDB = taskElementService.findById(task.getId());
        OrderElement orderElement = fromDB.getOrderElement();
        assertThat(orderElement.getTaskElements().size(), equalTo(1));
        assertThat(orderElement.getTaskElements().iterator().next(),
                equalTo(fromDB));
    }
}
