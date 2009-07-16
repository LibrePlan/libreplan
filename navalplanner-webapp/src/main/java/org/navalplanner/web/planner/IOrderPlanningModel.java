package org.navalplanner.web.planner;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.TaskElement;
import org.zkoss.ganttz.adapters.PlannerConfiguration;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IOrderPlanningModel {

    public interface ConfigurationOnTransaction {
        public void use(PlannerConfiguration<TaskElement> configuration);
    }

    void createConfiguration(Order order,
            ConfigurationOnTransaction onTransaction);

}
