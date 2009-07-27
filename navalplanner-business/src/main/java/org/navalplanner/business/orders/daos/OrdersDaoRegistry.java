package org.navalplanner.business.orders.daos;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * A registry of Orders DAOs. Classes in which dependency injection (DI) is
 * not directly supported by Spring (e.g. entities) must use this class to
 * access resource DAOs. For the rest of classes (e.g. services, tests, etc.),
 * Spring DI is a more convenient option.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class OrdersDaoRegistry {

    private static OrdersDaoRegistry instance = new OrdersDaoRegistry();

    @Autowired
    private IOrderDao order;

    private IOrderElementDao orderElement;

    private OrdersDaoRegistry() {
    }

    public static OrdersDaoRegistry getInstance() {
        return instance;
    }

    public static IOrderDao getOrderDao() {
        return getInstance().order;
    }

    public static IOrderElementDao getOrderElementDao() {
        return getInstance().orderElement;
    }
}
