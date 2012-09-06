/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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
package org.libreplan.business.users.bootstrap;

import static org.libreplan.business.users.entities.UserRole.ROLE_BUDGET_REPORT;
import static org.libreplan.business.users.entities.UserRole.ROLE_CALENDARS;
import static org.libreplan.business.users.entities.UserRole.ROLE_CALENDAR_EXCEPTION_DAYS;
import static org.libreplan.business.users.entities.UserRole.ROLE_COMPANIES;
import static org.libreplan.business.users.entities.UserRole.ROLE_COST_CATEGORIES;
import static org.libreplan.business.users.entities.UserRole.ROLE_CREATE_PROJECTS;
import static org.libreplan.business.users.entities.UserRole.ROLE_CRITERIA;
import static org.libreplan.business.users.entities.UserRole.ROLE_EDIT_ALL_PROJECTS;
import static org.libreplan.business.users.entities.UserRole.ROLE_ESTIMATED_PLANNED_HOURS_PER_TASK_REPORT;
import static org.libreplan.business.users.entities.UserRole.ROLE_EXPENSES;
import static org.libreplan.business.users.entities.UserRole.ROLE_HOURS_TYPES;
import static org.libreplan.business.users.entities.UserRole.ROLE_HOURS_WORKED_PER_RESOURCE_REPORT;
import static org.libreplan.business.users.entities.UserRole.ROLE_LABELS;
import static org.libreplan.business.users.entities.UserRole.ROLE_MACHINES;
import static org.libreplan.business.users.entities.UserRole.ROLE_MAIN_SETTINGS;
import static org.libreplan.business.users.entities.UserRole.ROLE_MATERIALS;
import static org.libreplan.business.users.entities.UserRole.ROLE_MATERIALS_NEED_AT_DATE_REPORT;
import static org.libreplan.business.users.entities.UserRole.ROLE_MATERIAL_UNITS;
import static org.libreplan.business.users.entities.UserRole.ROLE_PLANNING;
import static org.libreplan.business.users.entities.UserRole.ROLE_PROFILES;
import static org.libreplan.business.users.entities.UserRole.ROLE_PROGRESS_TYPES;
import static org.libreplan.business.users.entities.UserRole.ROLE_PROJECT_COSTS_REPORT;
import static org.libreplan.business.users.entities.UserRole.ROLE_QUALITY_FORMS;
import static org.libreplan.business.users.entities.UserRole.ROLE_READ_ALL_PROJECTS;
import static org.libreplan.business.users.entities.UserRole.ROLE_RECEIVED_FROM_CUSTOMERS;
import static org.libreplan.business.users.entities.UserRole.ROLE_RECEIVED_FROM_SUBCONTRACTORS;
import static org.libreplan.business.users.entities.UserRole.ROLE_SEND_TO_CUSTOMERS;
import static org.libreplan.business.users.entities.UserRole.ROLE_SEND_TO_SUBCONTRACTORS;
import static org.libreplan.business.users.entities.UserRole.ROLE_TASK_SCHEDULING_STATUS_IN_PROJECT_REPORT;
import static org.libreplan.business.users.entities.UserRole.ROLE_TEMPLATES;
import static org.libreplan.business.users.entities.UserRole.ROLE_TIMESHEETS;
import static org.libreplan.business.users.entities.UserRole.ROLE_TIMESHEETS_TEMPLATES;
import static org.libreplan.business.users.entities.UserRole.ROLE_TIMESHEET_LINES_LIST;
import static org.libreplan.business.users.entities.UserRole.ROLE_TOTAL_WORKED_HOURS_BY_RESOURCE_IN_A_MONTH_REPORT;
import static org.libreplan.business.users.entities.UserRole.ROLE_USER_ACCOUNTS;
import static org.libreplan.business.users.entities.UserRole.ROLE_VIRTUAL_WORKERS;
import static org.libreplan.business.users.entities.UserRole.ROLE_WORKERS;
import static org.libreplan.business.users.entities.UserRole.ROLE_WORK_AND_PROGRESS_PER_PROJECT_REPORT;
import static org.libreplan.business.users.entities.UserRole.ROLE_WORK_AND_PROGRESS_PER_TASK_REPORT;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.libreplan.business.users.entities.Profile;
import org.libreplan.business.users.entities.UserRole;

/**
 * Defines the default {@link org.libreplan.business.users.entities.Profile
 * Profiles}
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public enum PredefinedProfiles {

    SYSTEMS_ADMINISTRATOR("Systems Administrator", ROLE_MAIN_SETTINGS,
            ROLE_USER_ACCOUNTS, ROLE_PROFILES),

    PROJECT_MANAGER("Project Manager", ROLE_READ_ALL_PROJECTS,
            ROLE_EDIT_ALL_PROJECTS, ROLE_CREATE_PROJECTS, ROLE_PLANNING,
            ROLE_TEMPLATES, ROLE_WORKERS, ROLE_MACHINES, ROLE_VIRTUAL_WORKERS,
            ROLE_CALENDARS, ROLE_CALENDAR_EXCEPTION_DAYS, ROLE_CRITERIA,
            ROLE_PROGRESS_TYPES, ROLE_LABELS, ROLE_MATERIALS,
            ROLE_MATERIAL_UNITS, ROLE_QUALITY_FORMS,
            ROLE_RECEIVED_FROM_SUBCONTRACTORS, ROLE_RECEIVED_FROM_CUSTOMERS,
            ROLE_WORK_AND_PROGRESS_PER_PROJECT_REPORT,
            ROLE_WORK_AND_PROGRESS_PER_TASK_REPORT,
            ROLE_ESTIMATED_PLANNED_HOURS_PER_TASK_REPORT,
            ROLE_TASK_SCHEDULING_STATUS_IN_PROJECT_REPORT,
            ROLE_MATERIALS_NEED_AT_DATE_REPORT),

    HUMAN_RESOURCES_AND_COSTS_MANAGER("Human Resources & Costs Manager",
            ROLE_WORKERS, ROLE_MACHINES, ROLE_VIRTUAL_WORKERS, ROLE_CALENDARS,
            ROLE_CALENDAR_EXCEPTION_DAYS, ROLE_COST_CATEGORIES,
            ROLE_HOURS_TYPES, ROLE_PROJECT_COSTS_REPORT),

    TIME_TRACKING_AND_EXPENSES_RESPONSIBLE(
            "Time Tracking & Expenses Responsible", ROLE_TIMESHEETS,
            ROLE_TIMESHEETS_TEMPLATES, ROLE_EXPENSES, ROLE_HOURS_TYPES,
            ROLE_TIMESHEET_LINES_LIST,
            ROLE_HOURS_WORKED_PER_RESOURCE_REPORT,
            ROLE_TOTAL_WORKED_HOURS_BY_RESOURCE_IN_A_MONTH_REPORT),

    OUTSOURCING_MANAGER("Outsourcing Manager", ROLE_COMPANIES,
            ROLE_SEND_TO_SUBCONTRACTORS, ROLE_RECEIVED_FROM_SUBCONTRACTORS,
            ROLE_SEND_TO_CUSTOMERS, ROLE_RECEIVED_FROM_CUSTOMERS),

    REPORTS_RESPONSIBLE("Reports Responsible", ROLE_READ_ALL_PROJECTS,
            ROLE_HOURS_WORKED_PER_RESOURCE_REPORT,
            ROLE_TOTAL_WORKED_HOURS_BY_RESOURCE_IN_A_MONTH_REPORT,
            ROLE_WORK_AND_PROGRESS_PER_PROJECT_REPORT,
            ROLE_WORK_AND_PROGRESS_PER_TASK_REPORT,
            ROLE_ESTIMATED_PLANNED_HOURS_PER_TASK_REPORT,
            ROLE_PROJECT_COSTS_REPORT,
            ROLE_TASK_SCHEDULING_STATUS_IN_PROJECT_REPORT,
            ROLE_MATERIALS_NEED_AT_DATE_REPORT,
            ROLE_BUDGET_REPORT);

    private String name;
    private UserRole[] roles;

    private PredefinedProfiles(String name, UserRole... roles) {
        this.name = name;
        this.roles = roles;
    }

    public Profile createProfile() {
        return Profile
                .create(name, new HashSet<UserRole>(Arrays.asList(roles)));
    }

    public String getName() {
        return name;
    }

    public Set<UserRole> getRoles() {
        return new HashSet<UserRole>(Arrays.asList(roles));
    }

}
