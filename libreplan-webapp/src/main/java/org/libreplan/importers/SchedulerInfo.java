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

package org.libreplan.importers;

import org.libreplan.business.common.entities.JobSchedulerConfiguration;

/**
 * Holds information about the scheduler, The information comes partly form
 * {@link JobSchedulerConfiguration} and partly form {@link SchedulerManager}
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class SchedulerInfo {

    private JobSchedulerConfiguration jobSchedulerConfiguration;
    private String nextFireTime;

    public SchedulerInfo() {

    }

    public SchedulerInfo(JobSchedulerConfiguration jobSchedulerConfiguration) {
        this.jobSchedulerConfiguration = jobSchedulerConfiguration;
    }

    public String getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(String nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public JobSchedulerConfiguration getJobSchedulerConfiguration() {
        return jobSchedulerConfiguration;
    }

    public void setJobSchedulerConfiguration(
            JobSchedulerConfiguration jobSchedulerConfiguration) {
        this.jobSchedulerConfiguration = jobSchedulerConfiguration;
    }

}
