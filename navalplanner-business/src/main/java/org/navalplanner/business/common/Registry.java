package org.navalplanner.business.common;

import org.navalplanner.business.advance.daos.IAdvanceTypeDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A registry, AKA service locator, for objects in which dependency injection
 * (DI) is not directly supported by Spring (e.g. entities) must use this class
 * to access DAOs. For the rest of classes (e.g. services, tests, etc.), Spring
 * DI is a more convenient option. The DAOs or services are added to the
 * registry as needed.
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Registry {

    private static final Registry singleton = new Registry();

    @Autowired
    private IAdvanceTypeDAO advanceTypeDao;

    private Registry() {
    }

    public static Registry getInstance() {
        return singleton;
    }

    public static IAdvanceTypeDAO getAdvanceTypeDao() {
        return getInstance().advanceTypeDao;
    }

}
