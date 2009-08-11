package org.navalplanner.business.test.planner.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.runner.RunWith;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
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

}
