package org.navalplanner.web.reports;

import java.util.Date;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;

import org.navalplanner.business.orders.entities.Order;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public interface ICompletedEstimatedHoursPerTaskModel {

    JRDataSource getCompletedEstimatedHoursReportPerTask(Order order, Date referenceDate);

    List<Order> getOrders();

}
