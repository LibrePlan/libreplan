package org.navalplanner.business.orders.daos;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * A registry of Orders DAOs. Classes in which dependency injection (DI) is
 * not directly supported by Spring (e.g. entities) must use this class to
 * access resource DAOs. For the rest of classes (e.g. services, tests, etc.),
 * Spring DI is a more convenient option.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class OrdersDAORegistry {

    private static OrdersDAORegistry instance = new OrdersDAORegistry();

    @Autowired
    private IOrderDAO order;

    @Autowired
    private IOrderElementDAO orderElement;

    private OrdersDAORegistry() {
    }

    public static OrdersDAORegistry getInstance() {
        return instance;
    }

    public static IOrderDAO getOrderDao() {
        return getInstance().order;
    }

    public static IOrderElementDAO getOrderElementDao() {
        return getInstance().orderElement;
    }
}
