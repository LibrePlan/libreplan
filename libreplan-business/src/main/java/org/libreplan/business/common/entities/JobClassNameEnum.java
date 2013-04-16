/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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


/**
 * Defines the job class package and name to be used as data type in
 * {@link JobSchedulerConfiguration}
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public enum JobClassNameEnum {

    IMPORT_ROSTER_FROM_TIM_JOB("org.libreplan.importers", "ImportRosterFromTimJob"),
    EXPORT_TIMESHEET_TO_TIM_JOB("org.libreplan.importers","ExportTimesheetToTimJob"),
    SYNC_ORDERELEMENTS_WITH_JIRA_ISSUES_JOB("org.libreplan.importers","JiraOrderElementSynchronizerJob");

    private String packageName;
    private String name;

    private JobClassNameEnum(String packageName, String name) {
        this.packageName = packageName;
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }

}
