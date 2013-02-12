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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.libreplan.business.common.daos.IJobSchedulerConfigurationDAO;
import org.libreplan.business.common.entities.JobSchedulerConfiguration;
import org.libreplan.importers.ISchedulerManager;
import org.libreplan.importers.SchedulerInfo;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to {@link JobSchedulerConfiguration}.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/common/job_scheduler_configuration.zul")
public class JobSchedulerModel implements IJobSchedulerModel {

    @Autowired
    private ISchedulerManager schedulerManager;

    @Autowired
    private IJobSchedulerConfigurationDAO jobSchedulerConfigurationDAO;

    @Override
    public List<SchedulerInfo> getSchedulerInfos() {
        List<SchedulerInfo> schedulerInfoList = schedulerManager
                .getSchedulerInfos();
        Collections.sort(schedulerInfoList, new Comparator<SchedulerInfo>() {

            @Override
            public int compare(SchedulerInfo o1, SchedulerInfo o2) {
                int result = o1
                        .getJobSchedulerConfiguration()
                        .getJobGroup()
                        .compareTo(
                                o2.getJobSchedulerConfiguration().getJobGroup());
                if (result == 0) {
                    result = o1
                            .getJobSchedulerConfiguration()
                            .getJobName()
                            .compareTo(
                                    o2.getJobSchedulerConfiguration()
                                            .getJobName());
                }
                return result;
            }
        });
        return schedulerInfoList;
    }

    @Override
    public void doManual(SchedulerInfo schedulerInfo) {
        schedulerManager.doManual(schedulerInfo.getJobSchedulerConfiguration()
                .getJobName());
    }

    @Override
    @Transactional
    public void saveJobConfigurationAndReschedule(String jobGroup,
            String jobName, String cronExp) {
        JobSchedulerConfiguration jobSchedulerConfiguration = jobSchedulerConfigurationDAO
                .findByJobGroupAndJobName(jobGroup, jobName);
        jobSchedulerConfiguration.setCronExpression(cronExp);
        jobSchedulerConfigurationDAO.save(jobSchedulerConfiguration);
        schedulerManager.rescheduleJob(jobSchedulerConfiguration);
    }

}
