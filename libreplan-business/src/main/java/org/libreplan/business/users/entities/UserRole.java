/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.business.users.entities;

import static org.libreplan.business.i18n.I18nHelper._t;

/**
 * Available user roles.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
public enum UserRole {

    // Access to all pages
    ROLE_SUPERUSER(_t("Superuser")),

    // Web services roles
    ROLE_WS_READER(_t("Web service reader")),
    ROLE_WS_WRITER(_t("Web service writer")),
    ROLE_WS_SUBCONTRACTING(_t("Web service subcontractor operations")),

    // Project operations roles
    ROLE_READ_ALL_PROJECTS(_t("Read all projects")),
    ROLE_EDIT_ALL_PROJECTS(_t("Edit all projects")),
    ROLE_CREATE_PROJECTS(_t("Create projects")),

    // Special role for bound users
    ROLE_BOUND_USER(_t("Bound user")),

    // Page roles
    ROLE_PLANNING(_t("Planning")),
    ROLE_TEMPLATES(_t("Templates")),
    ROLE_IMPORT_PROJECTS(_t("Import projects")),
    ROLE_WORKERS(_t("Workers")),
    ROLE_MACHINES(_t("Machines")),
    ROLE_VIRTUAL_WORKERS(_t("Virtual Workers")),
    ROLE_CALENDARS(_t("Calendars")),
    ROLE_CALENDAR_EXCEPTION_DAYS(_t("Calendar Exception Days")),
    ROLE_CRITERIA(_t("Criteria")),
    ROLE_PROGRESS_TYPES(_t("Progress Types")),
    ROLE_LABELS(_t("Labels")),
    ROLE_MATERIALS(_t("Materials")),
    ROLE_MATERIAL_UNITS(_t("Material Units")),
    ROLE_QUALITY_FORMS(_t("Quality Forms")),
    ROLE_TIMESHEETS(_t("Timesheets")),
    ROLE_TIMESHEETS_TEMPLATES(_t("Timesheets Templates")),
    ROLE_EXPENSES(_t("Expenses")),
    ROLE_COST_CATEGORIES(_t("Cost Categories")),
    ROLE_HOURS_TYPES(_t("Hours Types")),
    ROLE_MAIN_SETTINGS(_t("Main Settings")),
    ROLE_USER_ACCOUNTS(_t("User Accounts")),
    ROLE_PROFILES(_t("Profiles")),
    ROLE_JOB_SCHEDULING(_t("Job Scheduling")),
    ROLE_COMPANIES(_t("Companies")),
    ROLE_SEND_TO_SUBCONTRACTORS(_t("Send To Subcontractors")),
    ROLE_RECEIVED_FROM_SUBCONTRACTORS(_t("Received From Subcontractors")),
    ROLE_SEND_TO_CUSTOMERS(_t("Send To Customers")),
    ROLE_RECEIVED_FROM_CUSTOMERS(_t("Received From Customers")),
    ROLE_TIMESHEET_LINES_LIST(_t("Timesheet Lines List")),
    ROLE_HOURS_WORKED_PER_RESOURCE_REPORT(_t("Hours Worked Per Resource Report")),
    ROLE_TOTAL_WORKED_HOURS_BY_RESOURCE_IN_A_MONTH_REPORT(_t("Total Worked Hours By Resource In A Month Report")),
    ROLE_WORK_AND_PROGRESS_PER_PROJECT_REPORT(_t("Work And Progress Per Project Report")),
    ROLE_WORK_AND_PROGRESS_PER_TASK_REPORT(_t("Work And Progress Per Task Report")),
    ROLE_ESTIMATED_PLANNED_HOURS_PER_TASK_REPORT(_t("Estimated/Planned Hours Per Task Report")),
    ROLE_PROJECT_COSTS_REPORT(_t("Project Costs Report")),
    ROLE_TASK_SCHEDULING_STATUS_IN_PROJECT_REPORT(_t("Task Scheduling Status In Project Report")),
    ROLE_MATERIALS_NEED_AT_DATE_REPORT(_t("Materials Needed At Date Report")),
    ROLE_PROJECT_STATUS_REPORT(_t("Project Status Report")),

    ROLE_EDIT_EMAIL_TEMPLATES(_t("Edit E-mail Templates")),
    ROLE_USE_FILES(_t("Use files for order")),

    ROLE_EMAIL_TASK_ASSIGNED_TO_RESOURCE(_t("Email: task assigned to resource")),
    ROLE_EMAIL_RESOURCE_REMOVED_FROM_TASK(_t("Email: resource removed from task")),
    ROLE_EMAIL_MILESTONE_REACHED(_t("Email: milestone reached")),
    ROLE_EMAIL_TASK_SHOULD_FINISH(_t("Email: task should finish")),
    ROLE_EMAIL_TASK_SHOULD_START(_t("Email: task should start")),
    ROLE_EMAIL_TIMESHEET_DATA_MISSING(_t("Email: timesheet data missing"));

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
