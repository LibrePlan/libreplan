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
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
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
    private IOrderDAO orderDAO;

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
        OrderLine orderLine = OrderLine.create();
        orderLine.setName("bla");
        orderLine.setCode("000000000");
        orderLine.addHoursGroup(new HoursGroup());
        Order order = new Order();
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

    @Test
    public void theParentPropertyIsPresentWhenRetrievingTasks() {
        TaskGroup taskGroup = createValidTaskGroup();
        taskGroup.addTaskElement(createValidTask());
        taskElementService.save(taskGroup);
        flushAndEvict(taskGroup);
        TaskElement reloaded = taskElementService.findById(taskGroup.getId());
        TaskElement child = reloaded.getChildren().get(0);
        assertThat(child.getParent(), equalTo(reloaded));
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
        List<TaskElement> taskElements = reloaded.getChildren();
        assertThat(taskElements.size(), equalTo(2));
        assertThat(taskElements.get(0).getId(), equalTo(child1.getId()));
        assertThat(taskElements.get(1).getId(), equalTo(child2.getId()));

    }

    @Test
    public void savingTaskElementSavesAssociatedDependencies() {
        Task child1 = createValidTask();
        Task child2 = createValidTask();
        taskElementService.save(child2);
        Task oldChild2 = child2;
        flushAndEvict(child2);
        child2 = (Task) taskElementService.findById(child2.getId());
        Dependency dependency = Dependency.createDependency(child1, oldChild2,
                Type.START_END);
        taskElementService.save(child1);
        flushAndEvict(child1);
        TaskElement child1Reloaded = (TaskElement) taskElementService
                .findById(child1.getId());
        assertThat(child1Reloaded.getDependenciesWithThisOrigin().size(),
                equalTo(1));
        assertTrue(child1Reloaded.getDependenciesWithThisDestination()
                .isEmpty());

        assertThat(child2.getDependenciesWithThisDestination().size(),
                equalTo(1));
        assertTrue(child2.getDependenciesWithThisOrigin().isEmpty());
    }

    @Test
    public void aOrderLineGroupIsConvertedToATaskGroup() {
        OrderLineGroup orderLineGroup = OrderLineGroup.create();
        orderLineGroup.setName("foo");
        orderLineGroup.setCode("000000000");
        TaskElement task = taskElementService
                .convertToInitialSchedule(orderLineGroup);
        assertThat(task, is(TaskGroup.class));

        TaskGroup group = (TaskGroup) task;
        assertThat(group.getOrderElement(),
                equalTo((OrderElement) orderLineGroup));
    }

    @Test
    public void aOrderLineWithOneHourGroupIsConvertedToATask() {
        OrderLine orderLine = OrderLine.create();
        orderLine.setName("bla");
        orderLine.setCode("000000000");
        final int hours = 30;
        HoursGroup hoursGroup = createHoursGroup(hours);
        orderLine.addHoursGroup(hoursGroup);
        TaskElement taskElement = taskElementService
                .convertToInitialSchedule(orderLine);
        assertThat(taskElement, is(Task.class));

        Task group = (Task) taskElement;
        assertThat(group.getOrderElement(), equalTo((OrderElement) orderLine));
        assertThat(group.getHoursGroup(), equalTo(hoursGroup));
        assertThat(taskElement.getWorkHours(), equalTo(hours));
    }


    @Test
    public void theSublinesOfAnOrderLineGroupAreConverted() {
        OrderLineGroup orderLineGroup = OrderLineGroup.create();
        orderLineGroup.setName("foo");
        orderLineGroup.setCode("000000000");
        OrderLine orderLine = OrderLine.create();
        orderLine.setName("bla");
        orderLine.setCode("000000000");
        HoursGroup hoursGroup = createHoursGroup(30);
        orderLine.addHoursGroup(hoursGroup);
        orderLineGroup.add(orderLine);
        TaskElement task = taskElementService
                .convertToInitialSchedule(orderLineGroup);
        assertThat(task, is(TaskGroup.class));

        TaskGroup group = (TaskGroup) task;

        assertThat(group.getOrderElement(),
                equalTo((OrderElement) orderLineGroup));
        assertThat(group.getChildren().size(), equalTo(1));
        assertThat(group.getChildren().get(0).getOrderElement(),
                equalTo((OrderElement) orderLine));
    }

    @Test
    public void theWorkHoursOfATaskGroupAreTheSameThanTheTaskElement(){
        OrderLineGroup orderLineGroup = OrderLineGroup.create();
        orderLineGroup.setName("foo");
        orderLineGroup.setCode("000000000");
        OrderLine orderLine = OrderLine.create();
        orderLine.setName("bla");
        orderLine.setCode("000000000");
        orderLine.addHoursGroup(createHoursGroup(20));
        orderLine.addHoursGroup(createHoursGroup(30));
        orderLineGroup.add(orderLine);
        TaskElement task = taskElementService
                .convertToInitialSchedule(orderLineGroup);
        assertThat(task.getWorkHours(), equalTo(orderLineGroup.getWorkHours()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void aOrderLineWithNoHoursIsRejected() {
        OrderLine orderLine = OrderLine.create();
        orderLine.setName("bla");
        orderLine.setCode("000000000");
        taskElementService.convertToInitialSchedule(orderLine);
    }

    private HoursGroup createHoursGroup(int hours) {
        HoursGroup result = new HoursGroup();
        result.setWorkingHours(hours);
        return result;
    }

    @Test
    public void aOrderLineWithMoreThanOneHourIsConvertedToATaskGroup() {
        OrderLine orderLine = OrderLine.create();
        orderLine.setName("bla");
        orderLine.setCode("000000000");
        HoursGroup hours1 = createHoursGroup(30);
        orderLine.addHoursGroup(hours1);
        HoursGroup hours2 = createHoursGroup(10);
        orderLine.addHoursGroup(hours2);
        TaskElement taskElement = taskElementService
                .convertToInitialSchedule(orderLine);
        assertThat(taskElement, is(TaskGroup.class));

        TaskGroup group = (TaskGroup) taskElement;
        assertThat(group.getOrderElement(), equalTo((OrderElement) orderLine));
        assertThat(group.getChildren().size(), equalTo(2));

        Task child1 = (Task) group.getChildren().get(0);
        Task child2 = (Task) group.getChildren().get(1);

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

    @Test
    public void aTaskCanBeRemoved() {
        Task task = createValidTask();
        taskElementService.save(task);
        flushAndEvict(task);
        taskElementService.remove(task);
        sessionFactory.getCurrentSession().flush();
        assertNull(sessionFactory.getCurrentSession().get(TaskElement.class,
                task.getId()));
    }

    @Test
    public void aTaskGroupCanBeRemoved() {
        TaskGroup taskGroup = createValidTaskGroup();
        Task task = createValidTask();
        taskGroup.addTaskElement(task);
        taskElementService.save(taskGroup);
        flushAndEvict(taskGroup);
        taskElementService.remove(taskGroup);
        sessionFactory.getCurrentSession().flush();
        assertNull(sessionFactory.getCurrentSession().get(TaskGroup.class,
                taskGroup.getId()));
        assertNull(sessionFactory.getCurrentSession().get(TaskElement.class,
                task.getId()));
    }

}
