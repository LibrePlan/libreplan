package org.navalplanner.web.reports;

import java.util.Date;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;

import org.navalplanner.business.orders.entities.Order;

/**
 *
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 *
 */
public interface IOrderCostsPerResourceModel {

    JRDataSource getOrderReport(List<Order> orders, Date startingDate,
            Date endingDate);

    List<Order> getOrders();

}
