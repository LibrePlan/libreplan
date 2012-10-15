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

import static org.libreplan.business.i18n.I18nHelper._;

/**
 * Available user roles.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public enum UserRole {

    // Access to all pages
    ROLE_SUPERUSER(_("Superuser")),
    // Web services roles
    ROLE_WS_READER(_("Web service reader")),
    ROLE_WS_WRITER(_("Web service writer")),
    ROLE_WS_SUBCONTRACTING(_("Web service subcontractor operations")),
    // Project operations roles
    ROLE_READ_ALL_PROJECTS(_("Read all projects")),
    ROLE_EDIT_ALL_PROJECTS(_("Edit all projects")),
    ROLE_CREATE_PROJECTS(_("Create projects")),
    // Special role for bound users
    ROLE_BOUND_USER(_("Bound user")),
    // Page roles
    ROLE_PLANNING(_("Planning")),
    ROLE_TEMPLATES(_("Templates")),
    ROLE_WORKERS(_("Workers")),
    ROLE_MACHINES(_("Machines")),
    ROLE_VIRTUAL_WORKERS(_("Virtual Workers")),
    ROLE_CALENDARS(_("Calendars")),
    ROLE_CALENDAR_EXCEPTION_DAYS(_("Calendar Exception Days")),
    ROLE_CRITERIA(_("Criteria")),
    ROLE_PROGRESS_TYPES(_("Progress Types")),
    ROLE_LABELS(_("Labels")),
    ROLE_MATERIALS(_("Materials")),
    ROLE_MATERIAL_UNITS(_("Material Units")),
    ROLE_QUALITY_FORMS(_("Quality Forms")),
    ROLE_TIMESHEETS(_("Timesheets")),
    ROLE_TIMESHEETS_TEMPLATES(_("Timesheets Templates")),
    ROLE_EXPENSES(_("Expenses")),
    ROLE_COST_CATEGORIES(_("Cost Categories")),
    ROLE_HOURS_TYPES(_("Hours Types")),
    ROLE_MAIN_SETTINGS(_("Main Settings")),
    ROLE_USER_ACCOUNTS(_("User Accounts")),
    ROLE_PROFILES(_("Profiles")),
    ROLE_COMPANIES(_("Companies")),
    ROLE_SEND_TO_SUBCONTRACTORS(_("Send To Subcontractors")),
    ROLE_RECEIVED_FROM_SUBCONTRACTORS(_("Received From Subcontractors")),
    ROLE_SEND_TO_CUSTOMERS(_("Send To Customers")),
    ROLE_RECEIVED_FROM_CUSTOMERS(_("Received From Customers")),
    ROLE_TIMESHEET_LINES_LIST(_("Timesheet Lines List")),
    ROLE_HOURS_WORKED_PER_RESOURCE_REPORT(_("Hours Worked Per Resource Report")),
    ROLE_TOTAL_WORKED_HOURS_BY_RESOURCE_IN_A_MONTH_REPORT(_("Total Worked Hours By Resource In A Month Report")),
    ROLE_WORK_AND_PROGRESS_PER_PROJECT_REPORT(_("Work And Progress Per Project Report")),
    ROLE_WORK_AND_PROGRESS_PER_TASK_REPORT(_("Work And Progress Per Task Report")),
    ROLE_ESTIMATED_PLANNED_HOURS_PER_TASK_REPORT(_("Estimated/Planned Hours Per Task Report")),
    ROLE_PROJECT_COSTS_REPORT(_("Project Costs Report")),
    ROLE_TASK_SCHEDULING_STATUS_IN_PROJECT_REPORT(_("Task Scheduling Status In Project Report")),
    ROLE_MATERIALS_NEED_AT_DATE_REPORT(_("Materials Needs At Date Report")),
    ROLE_PROJECT_STATUS_REPORT(_("Project Status Report"));

    private final String displayName;

    private UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
