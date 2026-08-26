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

package org.libreplan.business.test.common.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.daos.IJobSchedulerConfigurationDAO;
import org.libreplan.business.common.entities.JobClassNameEnum;
import org.libreplan.business.common.entities.JobSchedulerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link JobSchedulerConfigurationDAO}, written ahead of
 * the Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 */
public class JobSchedulerConfigurationDAOTest {

    @Autowired
    private IJobSchedulerConfigurationDAO jobSchedulerConfigurationDAO;

    private JobSchedulerConfiguration createValid(String jobGroup, String jobName, String connectorName) {
        JobSchedulerConfiguration config = JobSchedulerConfiguration.create();
        config.setJobGroup(jobGroup);
        config.setJobName(jobName);
        config.setConnectorName(connectorName);
        config.setCronExpression("0 0 0 * * ?");
        config.setJobClassName(JobClassNameEnum.IMPORT_ROSTER_FROM_TIM_JOB);
        jobSchedulerConfigurationDAO.save(config);
        return config;
    }

    @Test
    @Transactional
    public void testGetAllIncludesSaved() {
        JobSchedulerConfiguration config = createValid(
                "group-" + UUID.randomUUID(), "name-" + UUID.randomUUID(), "connector-" + UUID.randomUUID());
        boolean found = false;
        for (JobSchedulerConfiguration c : jobSchedulerConfigurationDAO.getAll()) {
            if (c.getId().equals(config.getId())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    @Transactional
    public void testFindByJobGroupAndJobNameReturnsMatch() {
        String group = "group-" + UUID.randomUUID();
        String name = "name-" + UUID.randomUUID();
        JobSchedulerConfiguration config = createValid(group, name, "connector-" + UUID.randomUUID());

        assertEquals(config.getId(), jobSchedulerConfigurationDAO.findByJobGroupAndJobName(group, name).getId());
    }

    @Test
    @Transactional
    public void testFindByJobGroupAndJobNameRequiresBothToMatch() {
        String group = "group-" + UUID.randomUUID();
        String name = "name-" + UUID.randomUUID();
        createValid(group, name, "connector-" + UUID.randomUUID());

        assertNull(jobSchedulerConfigurationDAO.findByJobGroupAndJobName(group, "different-name"));
        assertNull(jobSchedulerConfigurationDAO.findByJobGroupAndJobName("different-group", name));
    }

    @Test
    @Transactional
    public void testFindByConnectorNameReturnsOnlyMatches() {
        String connectorName = "connector-" + UUID.randomUUID();
        JobSchedulerConfiguration matching = createValid(
                "group-" + UUID.randomUUID(), "name-" + UUID.randomUUID(), connectorName);
        createValid("group-" + UUID.randomUUID(), "name-" + UUID.randomUUID(), "other-" + UUID.randomUUID());

        java.util.List<JobSchedulerConfiguration> results =
                jobSchedulerConfigurationDAO.findByConnectorName(connectorName);

        assertEquals(1, results.size());
        assertEquals(matching.getId(), results.get(0).getId());
    }

}
