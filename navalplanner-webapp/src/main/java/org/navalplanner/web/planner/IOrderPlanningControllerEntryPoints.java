package org.navalplanner.web.planner;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.web.common.entrypoints.EntryPoint;
import org.navalplanner.web.common.entrypoints.EntryPoints;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@EntryPoints(page = "/planner/order.zul", registerAs = "planningControllerEntryPoints")
public interface IOrderPlanningControllerEntryPoints {

    @EntryPoint("plan")
    public void showSchedule(Order order);
}
