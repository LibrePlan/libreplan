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

import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.business.workreports.entities.WorkReportType;

/**
 * Contract for {@link WorkRerportType}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IWorkReportTypeModel {

    /**
     * Gets the current {@link WorkReportType}.
     *
     * @return A {@link WorkReportType}
     */
    WorkReportType getWorkReportType();

    /**
     * Gets the {@link List} of {@link WorkReportType}.
     *
     * @return A {@link List} of {@link WorkReportType}
     */
    List<WorkReportType> getWorkReportTypes();

    /**
     * Stores the current {@link WorkReportType}.
     *
     * @throws ValidationException
     *             If validation fails
     */
    void save() throws ValidationException;

    /**
     * Deletes the {@link WorkReportType} passed as parameter.
     *
     * @param workReportType
     *            The object to be removed
     */
    void confirmRemove(WorkReportType workReportType);

    /**
     * Makes some operations needed before create a new {@link WorkReportType}.
     */
    void prepareForCreate();

    /**
     * Makes some operations needed before edit a {@link WorkReportType}.
     *
     * @param workReportType
     *            The object to be edited
     */
    void initEdit(WorkReportType workReportType);

    /**
     * Makes some operations needed before remove a {@link WorkReportType}.
     *
     * @param workReportType
     *            The object to be removed
     */
    void prepareForRemove(WorkReportType workReportType);

    /**
     * Check if it's or not editing a {@link WorkReportType}
     *
     * @return true if it's editing a {@link WorkReportType}
     */
    boolean isEditing();

    /**
     * Check if there is any {@link WorkReport} bound to {@link WorkReportType}
     *
     * @param workReportType
     * @return
     */
    boolean thereAreWorkReportsFor(WorkReportType workReportType);

}
