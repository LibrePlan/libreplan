/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 Igalia, S.L.
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

package org.libreplan.web.common;

import java.util.List;

import org.libreplan.business.common.entities.Connector;
import org.libreplan.business.common.entities.ConnectorException;
import org.libreplan.business.common.entities.JobSchedulerConfiguration;
import org.libreplan.business.common.entities.PredefinedConnectorProperties;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.importers.SynchronizationInfo;

/**
 * Contract for {@link JobSchedulerModel}.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public interface IJobSchedulerModel {

    /**
     * Returns all job scheduler configurations.
     *
     * @return list of <code>JobSchedulerConfiguration</code>
     */
    List<JobSchedulerConfiguration> getJobSchedulerConfigurations();

    /**
     * Returns next fire time for the specified job from <code>{@link JobSchedulerConfiguration}</code>.
     *
     * @param jobSchedulerConfiguration
     *            the job scheduler configuration
     */
    String getNextFireTime(JobSchedulerConfiguration jobSchedulerConfiguration);

    /**
     * Do manual action(replacement of scheduling).
     *
     * @param jobSchedulerConfiguration
     *            the job configuration
     * @throws ConnectorException
     *             if connector is not valid
     */
    void doManual(JobSchedulerConfiguration jobSchedulerConfiguration) throws ConnectorException;

    /**
     * Returns synchronization infos. Failures or successes info.
     */
    List<SynchronizationInfo> getSynchronizationInfos();

    /**
     * Prepares for create a new {@link JobSchedulerConfiguration}.
     */
    void initCreate();

    /**
     * Prepares for edit {@link JobSchedulerConfiguration}.
     *
     * @param jobSchedulerConfiguration
     *            object to be edited
     */
    void initEdit(JobSchedulerConfiguration jobSchedulerConfiguration);

    /**
     * Gets the current {@link JobSchedulerConfiguration}.
     *
     * @return A {@link JobSchedulerConfiguration}
     */
    JobSchedulerConfiguration getJobSchedulerConfiguration();

    /**
     * Saves the current {@link JobSchedulerConfiguration}.
     *
     * @throws ValidationException
     *             if validation fails
     */
    void confirmSave() throws ValidationException;

    /**
     * Cancels the current {@link JobSchedulerConfiguration}.
     */
    void cancel();

    /**
     * Removes the current {@link JobSchedulerConfiguration}.
     *
     * @param jobSchedulerConfiguration
     *            object to be removed
     */
    void remove(JobSchedulerConfiguration jobSchedulerConfiguration);

    /**
     * Returns list of connectors
     */
    List<Connector> getConnectors();

    /**
     * Schedule or unschedule jobs for the specified <code>connector</code>.
     *
     * Schedule all jobs of the specified <code>connector</code>'s property
     * {@link PredefinedConnectorProperties#ACTIVATED} is 'Y', otherwise unschedule the jobs.
     *
     * @param connector
     *            where to check if property is changed
     * @return true if (un)scheduling is successful, false otherwise
     */
    boolean scheduleOrUnscheduleJobs(Connector connector);

    /**
     * Schedule or unschedule job for the specified job in <code>{@link JobSchedulerConfiguration}</code>.
     *
     * @return true if scheduling is succeeded, false otherwise
     */
    boolean scheduleOrUnscheduleJob();

    /**
     * Delete job specified in <code>{@link JobSchedulerConfiguration}</code>.
     *
     * @param jobSchedulerConfiguration
     *            configuration for the job to be deleted
     * @return true if job is successfully deleted from the scheduler, false
     *         otherwise
     */
    boolean deleteScheduledJob(JobSchedulerConfiguration jobSchedulerConfiguration);

}