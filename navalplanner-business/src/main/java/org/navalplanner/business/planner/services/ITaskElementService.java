package org.navalplanner.business.planner.services;

import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.entities.TaskElement;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ITaskElementService {

    void save(TaskElement task);

    TaskElement findById(Long id);

    /**
     * @param order
     * @return
     */
    TaskElement convertToInitialSchedule(OrderElement order);

}
