/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.business.workreports.daos;

import java.util.List;

import org.joda.time.LocalDate;
import org.libreplan.business.common.daos.IIntegrationEntityDAO;
import org.libreplan.business.common.entities.PersonalTimesheetsPeriodicityEnum;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportType;

/**
 * Dao for {@link WorkReport}
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
public interface IWorkReportDAO extends IIntegrationEntityDAO<WorkReport> {

    List<WorkReport> getAllByWorkReportType(WorkReportType workReportType);

    List<WorkReport> getAll();

    List<WorkReport> allWorkReportsWithAssociatedOrdersUnproxied();

    int getFirstReportYear();

    int getLastReportYear();

    /**
     * Returns the {@link WorkReport} of the predefined type personal timesheet
     * for the given <code>resource</code> in the specified <code>date</code>
     * depending on the configured <code>periodicity</code>.<br />
     *
     * If there isn't any, it returns <code>null</code>.
     */
    WorkReport getPersonalTimesheetWorkReport(Resource resource, LocalDate date,
            PersonalTimesheetsPeriodicityEnum periodicity);

    boolean isAnyPersonalTimesheetAlreadySaved();

    List<WorkReport> findPersonalTimesheetsByResourceAndOrderElement(
            Resource resource);

}
