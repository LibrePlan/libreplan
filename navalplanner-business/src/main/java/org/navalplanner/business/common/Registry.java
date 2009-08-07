package org.navalplanner.business.common;

import org.navalplanner.business.workreports.daos.IWorkReportDAO;
import org.navalplanner.business.workreports.daos.IWorkReportLineDAO;
import org.navalplanner.business.workreports.daos.IWorkReportTypeDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A registry, AKA service locator, for objects in which dependency injection
 * (DI) is not directly supported by Spring (e.g. entities) must use this class
 * to access DAOs. For the rest of classes (e.g. services, tests, etc.), Spring
 * DI is a more convenient option.
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Registry {

    private static final Registry singleton = new Registry();

    @Autowired
    private IWorkReportDAO workReport;

    @Autowired
    private IWorkReportTypeDAO workReportType;

    @Autowired
    private IWorkReportLineDAO workReportLine;

    private Registry() {

    }

    public static Registry getInstance() {
        return singleton;
    }

    public static IWorkReportDAO getWorkReportDao() {
        return getInstance().workReport;
    }

    public static IWorkReportTypeDAO getWorkReportTypeDao() {
        return getInstance().workReportType;
    }

    public static IWorkReportLineDAO getWorkReportLineDao() {
        return getInstance().workReportLine;
    }

}
