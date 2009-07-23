package org.navalplanner.business.planner.services;

import java.util.List;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.planner.daos.ITaskElementDao;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Transactional
public class TaskElementService implements ITaskElementService {

    @Autowired
    private ITaskElementDao taskElementDao;

    @Override
    public void save(TaskElement task) {
        taskElementDao.save(task);
    }

    @Override
    public TaskElement findById(Long id) {
        try {
            return taskElementDao.find(id);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TaskElement convertToInitialSchedule(OrderElement order) {
        if (order instanceof OrderLineGroup) {
            OrderLineGroup group = (OrderLineGroup) order;
            return convertToTaskGroup(group);
        } else {
            OrderLine line = (OrderLine) order;
            if (line.getHoursGroups().isEmpty())
                throw new IllegalArgumentException(
                        "the line must have at least one "
                                + HoursGroup.class.getSimpleName()
                                + " associated");
            return line.getHoursGroups().size() > 1 ? convertToTaskGroup(line)
                    : convertToTask(line);
        }
    }

    private TaskGroup convertToTaskGroup(OrderLine line) {
        TaskGroup result = new TaskGroup();
        result.setOrderElement(line);
        for (HoursGroup hoursGroup : line.getHoursGroups()) {
            result.addTaskElement(taskFrom(line, hoursGroup));
        }
        return result;
    }

    private Task convertToTask(OrderLine line) {
        HoursGroup hoursGroup = line.getHoursGroups().get(0);
        return taskFrom(line, hoursGroup);
    }

    private Task taskFrom(OrderLine line, HoursGroup hoursGroup) {
        Task result = Task.createTask(hoursGroup);
        result.setOrderElement(line);
        return result;
    }

    private TaskGroup convertToTaskGroup(OrderLineGroup group) {
        TaskGroup result = new TaskGroup();
        result.setOrderElement(group);
        for (OrderElement orderElement : group.getChildren()) {
            result.addTaskElement(convertToInitialSchedule(orderElement));
        }
        return result;
    }

    @Override
    public void convertToScheduleAndSave(Order order) {
        List<OrderElement> orderElements = order.getOrderElements();
        for (OrderElement orderElement : orderElements) {
            save(convertToInitialSchedule(orderElement));
        }
    }

    @Override
    @Transactional
    public void remove(TaskElement taskElement) {
        try {
            taskElementDao.remove(taskElement.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
