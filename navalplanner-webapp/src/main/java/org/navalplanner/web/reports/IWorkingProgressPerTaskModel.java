package org.navalplanner.web.reports;

import java.util.Date;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.resources.entities.Worker;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public interface IWorkingProgressPerTaskModel {

    JRDataSource getWorkingProgressPerTaskReport(Order order, Date deadlineDate);

    List<Order> getOrders();

}
