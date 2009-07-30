package org.navalplanner.business.advance.daos;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * A registry of Advance DAOs. Classes in which dependency injection (DI) is
 * not directly supported by Spring (e.g. entities) must use this class to
 * access resource DAOs. For the rest of classes (e.g. services, tests, etc.),
 * Spring DI is a more convenient option.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class AdvanceDaoRegistry {

    private static AdvanceDaoRegistry instance = new AdvanceDaoRegistry();

    @Autowired
    private IAdvanceAssigmentDAO advanceAssigmentDao;

    @Autowired
    private IAdvanceTypeDAO advanceTypeDao;

    @Autowired
    private IAdvanceMeasurementDAO advanceMeasurementDao;

    private AdvanceDaoRegistry() {
    }

    public static AdvanceDaoRegistry getInstance() {
        return instance;
    }

    public static IAdvanceMeasurementDAO getAdvanceMeasurementDao() {
        return getInstance().advanceMeasurementDao;
    }

    public static IAdvanceTypeDAO getAdvanceTypeDao() {
        return getInstance().advanceTypeDao;
    }

    public static IAdvanceAssigmentDAO getAdvanceAssigmentDao() {
        return getInstance().advanceAssigmentDao;
    }
}