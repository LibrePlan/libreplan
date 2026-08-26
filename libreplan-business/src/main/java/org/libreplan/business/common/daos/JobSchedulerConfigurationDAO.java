/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

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
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<JobSchedulerConfiguration> cq = cb.createQuery(JobSchedulerConfiguration.class);
        Root<JobSchedulerConfiguration> root = cq.from(JobSchedulerConfiguration.class);
        cq.where(cb.equal(root.get("jobGroup"), jobGroup), cb.equal(root.get("jobName"), jobName));
        return getSession().createQuery(cq).uniqueResult();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobSchedulerConfiguration> findByConnectorName(String connectorName) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<JobSchedulerConfiguration> cq = cb.createQuery(JobSchedulerConfiguration.class);
        Root<JobSchedulerConfiguration> root = cq.from(JobSchedulerConfiguration.class);
        cq.where(cb.equal(root.get("connectorName"), connectorName));
        return getSession().createQuery(cq).getResultList();
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
