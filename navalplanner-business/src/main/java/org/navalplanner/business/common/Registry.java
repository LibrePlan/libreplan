package org.navalplanner.business.common;

import org.navalplanner.business.advance.daos.IAdvanceAssigmentDAO;
import org.navalplanner.business.advance.daos.IAdvanceMeasurementDAO;
import org.navalplanner.business.advance.daos.IAdvanceTypeDAO;
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

    @Autowired
    private IAdvanceAssigmentDAO advanceAssigmentDao;

    @Autowired
    private IAdvanceTypeDAO advanceTypeDao;

    @Autowired
    private IAdvanceMeasurementDAO advanceMeasurementDao;

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
