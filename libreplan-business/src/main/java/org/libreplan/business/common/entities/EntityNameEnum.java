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

import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.daos.IIntegrationEntityDAO;

/**
 * It represents the entities which use code generation.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public enum EntityNameEnum {

    ORDER("Project", false),
    CRITERION("Criterion", true),
    LABEL("Label", true),
    MACHINE("Machine", true),
    WORKER("Worker", true),
    UNIT_TYPE("Material unit", true),
    CALENDAR("Calendar", true),
    WORK_HOURS_TYPE("Hours type", true),
    MATERIAL_CATEGORY("Material category", true),
    WORK_REPORT("Timesheet", true),
    WORKREPORTTYPE("Timesheet template", false),
    CALENDAR_EXCEPTION_TYPE("Calendar exception day", true),
    COST_CATEGORY("Cost category", true),
    RESOURCE_CALENDAR("Resource calendar", true),
    CRITERION_SATISFACTION("Criterion satisfaction", true),
    RESOURCE_COST_CATEGORY_ASSIGNMENT("Resource cost category assignment", true),
    EXPENSE_SHEET("Expense sheet", true),
    ISSUE_LOG("Issue log", true),
    RISK_LOG("Risk log", true);

    private String description;

    private boolean canContainLowBar;

    EntityNameEnum(String description, boolean canContainLowBar) {
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
                return Registry.getOrderDAO();

            case CRITERION:
                return Registry.getCriterionTypeDAO();

            case LABEL:
                return Registry.getLabelTypeDAO();

            case MACHINE:
                return Registry.getMachineDAO();

            case WORKER:
                return Registry.getWorkerDAO();

            case UNIT_TYPE:
                return Registry.getUnitTypeDAO();

            case CALENDAR:
            case RESOURCE_CALENDAR:
                return Registry.getCalendarDataDAO();

            case WORK_HOURS_TYPE:
                return Registry.getTypeOfWorkHoursDAO();

            case MATERIAL_CATEGORY:
                return Registry.getMaterialCategoryDAO();

            case WORK_REPORT:
                return Registry.getWorkReportDAO();

            case WORKREPORTTYPE:
                return Registry.getWorkReportTypeDAO();

            case CALENDAR_EXCEPTION_TYPE:
                return Registry.getCalendarExceptionTypeDAO();

            case COST_CATEGORY:
                return Registry.getCostCategoryDAO();

            case CRITERION_SATISFACTION:
                return Registry.getCriterionSatisfactionDAO();

            case RESOURCE_COST_CATEGORY_ASSIGNMENT:
                return Registry.getResourcesCostCategoryAssignmentDAO();

            case EXPENSE_SHEET:
                return Registry.getExpenseSheetDAO();

            case ISSUE_LOG:
                return Registry.getIssueLogDAO();

            case RISK_LOG:
                return Registry.getRiskLogDAO();

            default:
                throw new RuntimeException("can't handle the code sequence of the " + description);
        }
    }

}
