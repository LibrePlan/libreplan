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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.entities.ConnectorException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * A job that synchronizes order elements and time sheets with JIRA issues
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class JiraOrderElementSynchronizerJob extends QuartzJobBean {

    private static final Log LOG = LogFactory
            .getLog(JiraOrderElementSynchronizerJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context)
            throws JobExecutionException {
        ApplicationContext applicationContext = (ApplicationContext) context
                .getJobDetail().getJobDataMap().get("applicationContext");

        IJiraOrderElementSynchronizer jiraOrderElementSynchronizer = (IJiraOrderElementSynchronizer) applicationContext
                .getBean("jiraOrderElementSynchronizer");

        try {
            List<SynchronizationInfo> syncInfos = jiraOrderElementSynchronizer
                    .syncOrderElementsWithJiraIssues();

            LOG.info("Synchronization scuccessful: "
                    + (syncInfos == null || syncInfos.isEmpty()));

        } catch (ConnectorException e) {
            LOG.error("Synchronize order elements failed", e);
        }
    }

}
