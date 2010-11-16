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

package org.navalplanner.business.common.entities;

import org.navalplanner.business.calendars.entities.CalendarData;
import org.navalplanner.business.calendars.entities.CalendarExceptionType;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.costcategories.entities.CostCategory;
import org.navalplanner.business.costcategories.entities.ResourcesCostCategoryAssignment;
import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.materials.entities.UnitType;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.business.workreports.entities.WorkReportType;

/**
 * It represents the entities which use code generation
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public enum EntityNameEnum {

    ORDER("Order", false), CRITERION("Criterion", true), LABEL("Label", true), MACHINE(
            "Machine", true), WORKER("Worker", true), UNIT_TYPE("Unit type",
            true), CALENDAR("Calendar", true), WORK_HOURS_TYPE(
            "Type of work hours", true), MATERIAL_CATEGORY("Material category",
            true), WORK_REPORT("Work report", true), WORKREPORTTYPE(
            "Work report type", false), CALENDAR_EXCEPTION_TYPE(
            "Calendar exception type", true), COST_CATEGORY("Cost category",
            true), RESOURCE_CALENDAR("Resource calendar", true), CRITERION_SATISFACTION(
            "Criterion satisfaction", true), RESOURCE_COST_CATEGORY_ASSIGNMENT(
            "Resource cost category assignment", true);

    private String description;

    private boolean canContainLowBar;

    private EntityNameEnum(String description, boolean canContainLowBar) {
        this.description = description;
        this.canContainLowBar = canContainLowBar;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean canContainLowBar() {
        return this.canContainLowBar;
    }

    public IIntegrationEntityDAO<? extends IntegrationEntity> getIntegrationEntityDAO() {
        switch (this) {
        case ORDER:
            return (IIntegrationEntityDAO<Order>) Registry.getOrderDAO();
        case CRITERION:
            return (IIntegrationEntityDAO<CriterionType>) Registry
                    .getCriterionTypeDAO();
        case LABEL:
            return (IIntegrationEntityDAO<LabelType>) Registry
                    .getLabelTypeDAO();
        case MACHINE:
            return (IIntegrationEntityDAO<Machine>) Registry.getMachineDAO();
        case WORKER:
            return (IIntegrationEntityDAO<Worker>) Registry.getWorkerDAO();
        case UNIT_TYPE:
            return (IIntegrationEntityDAO<UnitType>) Registry.getUnitTypeDAO();
        case CALENDAR:
        case RESOURCE_CALENDAR:
            return (IIntegrationEntityDAO<CalendarData>) Registry
                    .getCalendarDataDAO();
        case WORK_HOURS_TYPE:
            return (IIntegrationEntityDAO<TypeOfWorkHours>) Registry
                    .getTypeOfWorkHoursDAO();
        case MATERIAL_CATEGORY:
            return (IIntegrationEntityDAO<MaterialCategory>) Registry
                    .getMaterialCategoryDAO();
        case WORK_REPORT:
            return (IIntegrationEntityDAO<WorkReport>) Registry
                    .getWorkReportDAO();
        case WORKREPORTTYPE:
            return (IIntegrationEntityDAO<WorkReportType>) Registry
                    .getWorkReportTypeDAO();
        case CALENDAR_EXCEPTION_TYPE:
            return (IIntegrationEntityDAO<CalendarExceptionType>) Registry
                    .getCalendarExceptionTypeDAO();
        case COST_CATEGORY:
            return (IIntegrationEntityDAO<CostCategory>) Registry
                    .getCostCategoryDAO();
        case CRITERION_SATISFACTION:
            return (IIntegrationEntityDAO<CriterionSatisfaction>) Registry
                    .getCriterionSatisfactionDAO();
        case RESOURCE_COST_CATEGORY_ASSIGNMENT:
            return (IIntegrationEntityDAO<ResourcesCostCategoryAssignment>) Registry
                    .getResourcesCostCategoryAssignmentDAO();
        default:
            throw new RuntimeException("can't handle the code sequence of the "
                    + description);
        }
    }

    public String getSequenceLiteral() {
        return getDescription() + " sequences";
    }

}
