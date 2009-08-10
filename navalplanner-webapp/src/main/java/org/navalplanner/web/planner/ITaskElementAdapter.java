package org.navalplanner.web.planner;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.TaskElement;
import org.zkoss.ganttz.adapters.IAdapterToTaskFundamentalProperties;

/**
 * Contract for {@link TaskElementAdapter} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ITaskElementAdapter extends IAdapterToTaskFundamentalProperties<TaskElement>{

    void setOrder(Order order);

}
