package org.navalplanner.business.workreports.daos;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * A registry of WorkReports DAOs. Classes in which dependency injection (DI) is
 * not directly supported by Spring (e.g. entities) must use this class to
 * access resource DAOs. For the rest of classes (e.g. services, tests, etc.),
 * Spring DI is a more convenient option.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public final class WorkReportsDAORegistry {

    private static WorkReportsDAORegistry instance = new WorkReportsDAORegistry();

    @Autowired
    private IWorkReportDAO workReport;

    @Autowired
    private IWorkReportTypeDAO workReportType;

    @Autowired
    private IWorkReportLineDAO workReportLine;

    private WorkReportsDAORegistry() {
    }

    public static WorkReportsDAORegistry getInstance() {
        return instance;
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
