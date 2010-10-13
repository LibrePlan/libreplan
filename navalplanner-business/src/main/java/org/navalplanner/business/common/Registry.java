/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.business.common;

import org.navalplanner.business.advance.daos.IAdvanceTypeDAO;
import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.daos.ICalendarAvailabilityDAO;
import org.navalplanner.business.calendars.daos.ICalendarDataDAO;
import org.navalplanner.business.calendars.daos.ICalendarExceptionDAO;
import org.navalplanner.business.calendars.daos.ICalendarExceptionTypeDAO;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.daos.IEntitySequenceDAO;
import org.navalplanner.business.costcategories.daos.ICostCategoryDAO;
import org.navalplanner.business.costcategories.daos.IHourCostDAO;
import org.navalplanner.business.costcategories.daos.IResourcesCostCategoryAssignmentDAO;
import org.navalplanner.business.costcategories.daos.ITypeOfWorkHoursDAO;
import org.navalplanner.business.externalcompanies.daos.IExternalCompanyDAO;
import org.navalplanner.business.labels.daos.ILabelDAO;
import org.navalplanner.business.labels.daos.ILabelTypeDAO;
import org.navalplanner.business.materials.daos.IMaterialCategoryDAO;
import org.navalplanner.business.materials.daos.IMaterialDAO;
import org.navalplanner.business.materials.daos.IUnitTypeDAO;
import org.navalplanner.business.orders.daos.IHoursGroupDAO;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.qualityforms.daos.IQualityFormDAO;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.ICriterionSatisfactionDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.daos.IMachineDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.daos.IWorkerDAO;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.daos.IScenarioDAO;
import org.navalplanner.business.templates.daos.IOrderElementTemplateDAO;
import org.navalplanner.business.users.daos.IProfileDAO;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.workreports.daos.IWorkReportDAO;
import org.navalplanner.business.workreports.daos.IWorkReportLineDAO;
import org.navalplanner.business.workreports.daos.IWorkReportTypeDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A registry, AKA service locator, for objects in which dependency injection
 * (DI) is not directly supported by Spring (e.g. entities) must use this class
 * to access DAOs. For the rest of classes (e.g. services, tests, etc.), Spring
 * DI is a more convenient option. The DAOs or services are added to the
 * registry as needed.
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Javier Moran Rua <jmoran@igalia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class Registry {

    private static final Registry singleton = new Registry();


    @Autowired
    private IAdvanceTypeDAO advanceTypeDao;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired

    private IUserDAO userDAO;

    @Autowired
    private IMachineDAO machineDAO;

    @Autowired
    private IWorkReportTypeDAO workReportTypeDAO;

    @Autowired
    private ITypeOfWorkHoursDAO typeOfWorkHoursDAO;

    @Autowired
    private IMaterialDAO materialDAO;

    @Autowired
    private IMaterialCategoryDAO materialCategoryDAO;

    @Autowired
    private IQualityFormDAO qualityFormDAO;

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    @Autowired
    private ILabelDAO labelDAO;

    @Autowired
    private ILabelTypeDAO labelTypeDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IProfileDAO profileDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IWorkerDAO workerDAO;

    @Autowired
    private IWorkReportLineDAO workReportLineDAO;

    @Autowired
    private IExternalCompanyDAO externalCompanyDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private IHoursGroupDAO hoursGroupDAO;

    @Autowired
    private ICostCategoryDAO costCategoryDAO;

    @Autowired
    private IWorkReportDAO workReportDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private ICriterionSatisfactionDAO criterionSatisfactionDAO;

    @Autowired
    private IResourcesCostCategoryAssignmentDAO
        resourcesCostCategoryAssignmentDAO;

    @Autowired
    private IOrderElementTemplateDAO orderElementTemplateDAO;

    @Autowired
    private IHourCostDAO hourCostDAO;

    @Autowired
    private ICalendarExceptionDAO calendarExceptionDAO;

    @Autowired
    private ICalendarDataDAO calendarDataDAO;

    @Autowired
    private ICalendarExceptionTypeDAO calendarExceptionTypeDAO;

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private IUnitTypeDAO unitTypeDAO;

    @Autowired
    private ICalendarAvailabilityDAO calendarAvailabilityDAO;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private IEntitySequenceDAO entitySequenceDAO;

    private Registry() {
    }

    public static Registry getInstance() {
        return singleton;
    }

    public static IUnitTypeDAO getUnitTypeDAO() {
        return getInstance().unitTypeDAO;
    }

    public static IAdvanceTypeDAO getAdvanceTypeDao() {
        return getInstance().advanceTypeDao;
    }

    public static ICriterionTypeDAO getCriterionTypeDAO() {
        return getInstance().criterionTypeDAO;
    }

    public static IUserDAO getUserDAO() {
        return getInstance().userDAO;
    }

    public static IMachineDAO getMachineDAO() {
        return getInstance().machineDAO;
    }

    public static IWorkReportTypeDAO getWorkReportTypeDAO() {
        return getInstance().workReportTypeDAO;
    }

    public static ITypeOfWorkHoursDAO getTypeOfWorkHoursDAO() {
        return getInstance().typeOfWorkHoursDAO;
    }

    public static IMaterialDAO getMaterialDAO() {
        return getInstance().materialDAO;
    }

    public static IMaterialCategoryDAO getMaterialCategoryDAO() {
        return getInstance().materialCategoryDAO;
    }

    public static IQualityFormDAO getQualityFormDAO() {
        return getInstance().qualityFormDAO;
    }

    public static IBaseCalendarDAO getBaseCalendarDAO() {
        return getInstance().baseCalendarDAO;
    }

    public static ILabelDAO getLabelDAO() {
        return getInstance().labelDAO;
    }

    public static ILabelTypeDAO getLabelTypeDAO() {
        return getInstance().labelTypeDAO;
    }

    public static IConfigurationDAO getConfigurationDAO() {
        return getInstance().configurationDAO;
    }

    public static IProfileDAO getProfileDAO() {
        return getInstance().profileDAO;
    }

    public static IOrderElementDAO getOrderElementDAO() {
        return getInstance().orderElementDAO;
    }

    public static IWorkerDAO getWorkerDAO() {
        return getInstance().workerDAO;
    }
    public static IWorkReportLineDAO getWorkReportLineDAO() {
        return getInstance().workReportLineDAO;
    }

    public static IExternalCompanyDAO getExternalCompanyDAO() {
        return getInstance().externalCompanyDAO;
    }

    public static ICriterionDAO getCriterionDAO() {
        return getInstance().criterionDAO;
    }

    public static IHoursGroupDAO getHoursGroupDAO() {
        return getInstance().hoursGroupDAO;
    }

    public static ICostCategoryDAO getCostCategoryDAO() {
        return getInstance().costCategoryDAO;
    }

    public static IOrderDAO getOrderDAO() {
        return getInstance().orderDAO;
    }

    public static IWorkReportDAO getWorkReportDAO() {
        return getInstance().workReportDAO;
    }

    public static IResourceDAO getResourceDAO() {
        return getInstance().resourceDAO;
    }

    public static ICriterionSatisfactionDAO getCriterionSatisfactionDAO() {
        return getInstance().criterionSatisfactionDAO;
    }

    public static IResourcesCostCategoryAssignmentDAO
        getResourcesCostCategoryAssignmentDAO() {

        return getInstance().resourcesCostCategoryAssignmentDAO;

    }

    public static IOrderElementTemplateDAO getOrderElementTemplateDAO() {
        return getInstance().orderElementTemplateDAO;
    }

    public static IHourCostDAO getHourCostDAO() {
        return getInstance().hourCostDAO;
    }

    public static ICalendarExceptionTypeDAO getCalendarExceptionTypeDAO() {
        return getInstance().calendarExceptionTypeDAO;
    }

    public static IScenarioDAO getScenarioDAO() {
        return getInstance().scenarioDAO;
    }

    public static IScenarioManager getScenarioManager() {
        return getInstance().scenarioManager;
    }

    public static ICalendarExceptionDAO getCalendarExceptionDAO() {
        return getInstance().calendarExceptionDAO;
    }

    public static ICalendarDataDAO getCalendarDataDAO() {
        return getInstance().calendarDataDAO;
    }

    public static ICalendarAvailabilityDAO getCalendarAvailabilityDAO() {
        return getInstance().calendarAvailabilityDAO;
    }

    public static ITaskElementDAO getTaskElementDAO() {
        return getInstance().taskElementDAO;
    }

    public static IEntitySequenceDAO getEntitySequenceDAO() {
        return getInstance().entitySequenceDAO;
    }
}
