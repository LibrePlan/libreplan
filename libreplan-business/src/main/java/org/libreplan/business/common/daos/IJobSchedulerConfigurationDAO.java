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

package org.libreplan.business.common.daos;

import java.util.List;

import org.libreplan.business.common.entities.JobSchedulerConfiguration;

/**
 * Contract for {@link JobSchedulerConfigurationDAO}
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public interface IJobSchedulerConfigurationDAO extends IGenericDAO<JobSchedulerConfiguration, Long> {

    /**
     * Returns all {@link JobSchedulerConfiguration}
     */
    List<JobSchedulerConfiguration> getAll();

    /**
     * Searches and returns {@link JobSchedulerConfiguration} for the given
     * <code>connectorName</code>
     *
     * @param connectorName
     *            the name of the connector
     */
    List<JobSchedulerConfiguration> findByConnectorName(String connectorName);

    /**
     * Searches and returns {@link JobSchedulerConfiguration} for the given
     * <code>jobGroup</code> and <code>jobName</code>
     *
     * @param jobGroup
     * @param jobName
     */
    JobSchedulerConfiguration findByJobGroupAndJobName(String jobGroup, String jobName);

    /**
     * Returns true if there exists other @{link JobSchedulerConfiguration} with
     * the same <code>{@link JobSchedulerConfiguration#getJobGroup()}</code> and
     * <code>{@link JobSchedulerConfiguration#getJobName()</code>
     *
     * @param jobSchedulerConfiguration
     *            the <code>{@link JobSchedulerConfiguration}</code>
     */
    boolean existsByJobGroupAndJobNameAnotherTransaction(JobSchedulerConfiguration jobSchedulerConfiguration);

    /**
     * Returns unique {@link JobSchedulerConfiguration} for the specified
     * <code>JobGroup</code> and <code>JobName</code>
     *
     * @param jobGroup
     *            the jobGroup
     * @param jobName
     *            the jobName
     */
    JobSchedulerConfiguration findUniqueByJobGroupAndJobNameAnotherTransaction(String jobGroup, String jobName);
}
