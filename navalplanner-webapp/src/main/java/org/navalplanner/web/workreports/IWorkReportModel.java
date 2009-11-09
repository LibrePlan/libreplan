/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.workreports;

import java.util.Collection;
import java.util.List;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.business.workreports.entities.WorkReportType;

/**
 * Contract for {@link WorkRerportType}
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
public interface IWorkReportModel {

    /**
     * Gets the current {@link WorkReport}.
     *
     * @return A {@link WorkReport}
     */
    WorkReport getWorkReport();

    /**
     * Stores the current {@link WorkReport}.
     *
     * @throws ValidationException
     *             If validation fails
     */
    void save() throws ValidationException;

    /**
     * Makes some operations needed before create a new {@link WorkReport}.
     */
    void prepareForCreate(WorkReportType workReportType);

    /**
     * Makes some operations needed before edit a {@link WorkReport}.
     *
     * @param workReport
     *            The object to be edited
     */
    void prepareEditFor(WorkReport workReport);

    /**
     * Finds an @{link OrdrElement} by code
     *
     * @param code
     * @return
     */
    OrderElement findOrderElement(String code) throws InstanceNotFoundException;

    /**
     * Find a @{link Worker} by nif
     *
     * @param nif
     * @return
     * @throws InstanceNotFoundException
     */
    Worker findWorker(String nif) throws InstanceNotFoundException;

    /**
     * Converts @{link Resource} to @{link Worker}
     *
     * @param resource
     * @return
     * @throws InstanceNotFoundException
     */
    Worker asWorker(Resource resource) throws InstanceNotFoundException;

    /**
     * Get all {@link WorkReport} elements
     *
     * @return
     */
    List<WorkReport> getWorkReports();

    /**
     * Returns true if WorkReport is being edited
     *
     * @return
     */
    boolean isEditing();

    /**
     * Returns distinguished code for {@link OrderElement}
     *
     * @param orderElement
     * @return
     */
    String getDistinguishedCode(OrderElement orderElement)
            throws InstanceNotFoundException;

    /**
     * Add new {@link WorkReportLine} to {@link WorkReport}
     *
     * @return
     */
    WorkReportLine addWorkReportLine();

    /**
     * Removes {@link WorkReport}
     *
     * @param workReport
     */
    void remove(WorkReport workReport);

    /**
     * Removes {@link WorkReportLine}
     *
     * @param workReportLine
     */
    void removeWorkReportLine(WorkReportLine workReportLine);

    /**
     * Return all {@link WorkReportLine} associated with current {@link WorkReport}
     *
     * @return
     */
    List<WorkReportLine> getWorkReportLines();
}
