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

import org.libreplan.business.common.entities.ConnectorException;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.workreports.entities.WorkReportType;
import org.libreplan.importers.jira.IssueDTO;

/**
 * Synchronize the timesheets of order tasks of an existing order with jira
 * issues.
 *
 * A {@link WorkReportType} with the name "jira-connector" must also be exist
 * and configured properly prior to start synchronization.
 *
 * Jira issues will be retrieved from Jira RESTful web service during
 * synchronization of order elements
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public interface IJiraTimesheetSynchronizer {

    /**
     * Synchronize jira timesheet with the specified jira <code>issues</code> .
     *
     * Loop through all jira <code>issues</code> and check if timesheet is
     * already exist for the specified issue item. If it is, update the
     * timesheet with that issue item. If not create new one
     *
     * @param issues
     *            the jira issues
     * @param order
     *            an existing order
     * @throws ConnectorException
     *             if not valid connector or connector contains invalid values
     */
    void syncJiraTimesheetWithJiraIssues(List<IssueDTO> issues, Order order) throws ConnectorException;

    /**
     * returns synchronization info, success or fail info
     */
    SynchronizationInfo getSynchronizationInfo();

}
