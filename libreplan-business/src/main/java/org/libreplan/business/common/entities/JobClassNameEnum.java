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
 * Defines the job class package and name to be used as data type in {@link JobSchedulerConfiguration}.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
public enum JobClassNameEnum {

    IMPORT_ROSTER_FROM_TIM_JOB("org.libreplan.importers", "ImportRosterFromTimJob"),
    EXPORT_TIMESHEET_TO_TIM_JOB("org.libreplan.importers", "ExportTimesheetToTimJob"),
    SYNC_ORDERELEMENTS_WITH_JIRA_ISSUES_JOB("org.libreplan.importers", "JiraOrderElementSynchronizerJob"),

    SEND_EMAIL_TASK_ASSIGNED_TO_RESOURCE("org.libreplan.importers.notifications.jobs", "SendEmailOnTaskAssignedToResourceJob"),
    SEND_EMAIL_RESOURCE_REMOVED_FROM_TASK("org.libreplan.importers.notifications.jobs", "SendEmailOnResourceRemovedFromTaskJob"),
    SEND_EMAIL_MILESTONE_REACHED("org.libreplan.importers.notifications.jobs", "SendEmailOnMilestoneReachedJob"),
    SEND_EMAIL_TASK_SHOULD_START("org.libreplan.importers.notifications.jobs", "SendEmailOnTaskShouldStartJob"),
    SEND_EMAIL_TASK_SHOULD_FINISH("org.libreplan.importers.notifications.jobs", "SendEmailOnTaskShouldFinishJob"),
    SEND_EMAIL_TIMESHEET_DATA_MISSING("org.libreplan.importers.notifications.jobs", "SendEmailOnTimesheetDataMissingJob");

    private String packageName;

    private String name;

    JobClassNameEnum(String packageName, String name) {
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
