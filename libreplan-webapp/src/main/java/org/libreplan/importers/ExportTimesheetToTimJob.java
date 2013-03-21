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
 * A job that exports time sheets to Tim SOAP server
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class ExportTimesheetToTimJob extends QuartzJobBean {
    private static final Log LOG = LogFactory
                                         .getLog(ExportTimesheetToTimJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context)
            throws JobExecutionException {
        ApplicationContext applicationContext = (ApplicationContext) context
                .getJobDetail().getJobDataMap().get("applicationContext");

        IExportTimesheetsToTim exportTimesheetsToTim = (IExportTimesheetsToTim) applicationContext
                .getBean("exportTimesheetsToTim");

        try {
            List<SynchronizationInfo> syncInfos = exportTimesheetsToTim
                    .exportTimesheets();

            LOG.info("Export scuccessful: "
                    + (syncInfos == null || syncInfos.isEmpty()));

        } catch (ConnectorException e) {
            LOG.error("Export timesheet to Tim failed", e);
        }
    }

}
