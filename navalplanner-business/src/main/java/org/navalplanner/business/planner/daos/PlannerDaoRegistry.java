package org.navalplanner.business.planner.daos;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * A registry of Planner DAOs. Classes in which dependency injection (DI) is
 * not directly supported by Spring (e.g. entities) must use this class to
 * access resource DAOs. For the rest of classes (e.g. services, tests, etc.),
 * Spring DI is a more convenient option.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class PlannerDaoRegistry {

    private static PlannerDaoRegistry instance = new PlannerDaoRegistry();

    @Autowired
    private ITaskElementDao taskElement;

    private PlannerDaoRegistry() {
    }

    public static PlannerDaoRegistry getInstance() {
        return instance;
    }

    public static ITaskElementDao getTaskElementDao() {
        return getInstance().taskElement;
    }
}
