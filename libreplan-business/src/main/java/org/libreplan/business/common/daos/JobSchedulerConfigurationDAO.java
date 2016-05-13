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

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.entities.JobSchedulerConfiguration;
import org.libreplan.business.orders.entities.OrderSyncInfo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for {@link JobSchedulerConfiguration}
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class JobSchedulerConfigurationDAO extends GenericDAOHibernate<JobSchedulerConfiguration, Long>
        implements IJobSchedulerConfigurationDAO {

    @Override
    @Transactional(readOnly = true)
    public List<JobSchedulerConfiguration> getAll() {
        return list(JobSchedulerConfiguration.class);
    }

    @Override
    @Transactional(readOnly = true)
    public JobSchedulerConfiguration findByJobGroupAndJobName(String jobGroup, String jobName) {
        return (JobSchedulerConfiguration) getSession().createCriteria(JobSchedulerConfiguration.class)
                .add(Restrictions.eq("jobGroup", jobGroup))
                .add(Restrictions.eq("jobName", jobName)).uniqueResult();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobSchedulerConfiguration> findByConnectorName(String connectorName) {
        Criteria c = getSession().createCriteria(JobSchedulerConfiguration.class)
                .add(Restrictions.eq("connectorName", connectorName));

        return ((List<JobSchedulerConfiguration>) c.list());
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsByJobGroupAndJobNameAnotherTransaction(JobSchedulerConfiguration jobSchedulerConfiguration) {
        return existsOtherJobByGroupAndName(jobSchedulerConfiguration);
    }

    /**
     * Returns true if other {@link JobSchedulerConfiguration} which is the same
     * as the given <code>{@link OrderSyncInfo} already exists
     *
     * @param jobSchedulerConfiguration
     *            the {@link JobSchedulerConfiguration}
     */
    private boolean existsOtherJobByGroupAndName(JobSchedulerConfiguration jobSchedulerConfiguration) {
        JobSchedulerConfiguration found = findByJobGroupAndJobName(
                jobSchedulerConfiguration.getJobGroup(),
                jobSchedulerConfiguration.getJobName());

        return found != null && found != jobSchedulerConfiguration;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public JobSchedulerConfiguration findUniqueByJobGroupAndJobNameAnotherTransaction(String jobGroup, String jobName) {
        return findByJobGroupAndJobName(jobGroup, jobName);
    }

}
