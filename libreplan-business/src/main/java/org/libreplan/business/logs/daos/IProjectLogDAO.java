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

package org.libreplan.business.logs.daos;

import java.util.List;

import org.libreplan.business.common.daos.IIntegrationEntityDAO;
import org.libreplan.business.logs.entities.IssueLog;
import org.libreplan.business.logs.entities.ProjectLog;
import org.libreplan.business.logs.entities.RiskLog;

/**
 * Contract for {@link ProjectLogDAO}
 * 
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public interface IProjectLogDAO extends IIntegrationEntityDAO<ProjectLog> {

    /**
     * Gets all the issue-logs
     * 
     * @return a list of {@link IssueLog} objects
     */
    List<IssueLog> getIssueLogs();

    /**
     * Gets all the risk logs
     * 
     * @return a list of {@link RiskLog} objects
     */
    List<RiskLog> getRiskLogs();
}
