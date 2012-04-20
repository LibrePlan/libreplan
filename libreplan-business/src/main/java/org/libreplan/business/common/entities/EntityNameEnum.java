/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.business.common.entities;

import org.libreplan.business.calendars.entities.CalendarData;
import org.libreplan.business.calendars.entities.CalendarExceptionType;
import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.daos.IIntegrationEntityDAO;
import org.libreplan.business.costcategories.entities.CostCategory;
import org.libreplan.business.costcategories.entities.ResourcesCostCategoryAssignment;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.expensesheet.entities.ExpenseSheet;
import org.libreplan.business.labels.entities.LabelType;
import org.libreplan.business.materials.entities.MaterialCategory;
import org.libreplan.business.materials.entities.UnitType;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.resources.entities.CriterionSatisfaction;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.resources.entities.Machine;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportType;

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
            "Resource cost category assignment", true), EXPENSE_SHEET("Expense sheet", true);

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
        case EXPENSE_SHEET:
            return (IIntegrationEntityDAO<ExpenseSheet>) Registry.getExpenseSheetDAO();
        default:
            throw new RuntimeException("can't handle the code sequence of the "
                    + description);
        }
    }

    public String getSequenceLiteral() {
        return getDescription() + " sequences";
    }

}
