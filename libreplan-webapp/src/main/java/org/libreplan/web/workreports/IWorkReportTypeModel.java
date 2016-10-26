/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.web.workreports;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.labels.entities.LabelType;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.workreports.entities.PositionInWorkReportEnum;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportLabelTypeAssignment;
import org.libreplan.business.workreports.entities.WorkReportType;
import org.libreplan.business.workreports.valueobjects.DescriptionField;
import org.libreplan.web.common.IIntegrationEntityModel;

/**
 * Contract for {@link WorkReportType}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IWorkReportTypeModel extends IIntegrationEntityModel {

    /**
     * Gets the current {@link WorkReportType}.
     *
     * @return A {@link WorkReportType}
     */
    WorkReportType getWorkReportType();

    /**
     * Gets the {@link List} of {@link WorkReportType WorkReportTypes} except
     * the {@link WorkReportType} used for personal timesheets.
     *
     * @return A {@link List} of {@link WorkReportType}
     */
    List<WorkReportType> getWorkReportTypesExceptPersonalAndJiraTimesheets();

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
     * Check if it's or not editing a {@link WorkReportType}.
     *
     * @return true if it's editing a {@link WorkReportType}
     */
    boolean isEditing();

    /**
     * Set if it's or not shows {@link WorkReportType} list.
     */
    void setListing(boolean listing);

    /**
     * Check if there is any {@link WorkReport} bound to {@link WorkReportType}.
     * @param workReportType
     * @return boolean
     */
    boolean thereAreWorkReportsFor(WorkReportType workReportType);

    /**
     * Check if there is any {@link WorkReport} bound to {@link WorkReportType} which have been edited.
     *
     * @return boolean
     */
    boolean thereAreWorkReportsFor();

    /**
     * Gets the current list of assigned {@link DescripitonField} to the edited {@link WorkReportType}.
     *
     * @return A List {@link DescriptionField}
     */
    List<DescriptionField> getDescriptionFields();

    /**
     * Gets the current list of {@link LabelType}.
     *
     * @return A List {@link LabelType}
     */
    Map<LabelType, List<Label>> getMapLabelTypes();

    /**
     * Add a new {@link DescriptionField} to {@link WorkReportType} For default to the LineFields collection.
     */
    void addNewDescriptionField();

    /**
     * Delete a {@link DescriptionField} from {@link WorkReportType}
     * @param descriptionField
     */
    void removeDescriptionField(DescriptionField descriptionField);

    /**
     * Change the @{PositionInWorkReportEnum} of a {@link DescriptionField} to other collection of {@link WorkReportType}.
     * @param newPosition
     * @param descriptionField
     */
    void changePositionDescriptionField(PositionInWorkReportEnum newPosition, DescriptionField descriptionField);

    /**
     * @param descriptionField
     * @return {@link PositionInWorkReportEnum}
     */
    PositionInWorkReportEnum getPosition(DescriptionField descriptionField);

    /**
     * Check if a @{DescriptionField} is into the {@link PositionInWorkReportEnum#HEADING}.
     *
     * @return true if it's is into the @{PositionInWorkReportEnum.HEADING}
     */
    boolean isHeadingDescriptionField(DescriptionField descriptionField);

    /**
     * Gets the {@link List} of {@link WorkReportLabelTypeAssignment}.
     *
     * @return A {@link List} of {@link WorkReportLabelTypeAssignment}
     */
    Set<WorkReportLabelTypeAssignment> getWorkReportLabelTypeAssignments();

    /**
     * Add a new {@link WorkReportLabelTypeAssignment} to {@link WorkReportType}.
     */
    void addNewWorkReportLabelTypeAssignment();

    /**
     * Delete a {@link WorkReportLabelTypeAssignment} from {@link WorkReportType}.
     *
     * @param workReportLabelTypeAssignment
     */
    void removeWorkReportLabelTypeAssignment(WorkReportLabelTypeAssignment workReportLabelTypeAssignment);

    /**
     * Check if a {@link WorkReportLabelTypeAssignment} is shared by lines.
     *
     * @return a @{PositionInWorkReportEnum.HEADING} if it's is shared by lines.
     */
    PositionInWorkReportEnum getLabelAssignmentPosition(WorkReportLabelTypeAssignment workReportLabelTypeAssignment);

    /**
     * Set weather the {@link WorkReportLabelTypeAssignment} is shared by lines.
     */
    void setLabelAssignmentPosition(
            WorkReportLabelTypeAssignment workReportLabelTypeAssignment, PositionInWorkReportEnum position);

    /**
     * Check if a {@link java.util.Date} is shared by lines.
     *
     * @return a {@link PositionInWorkReportEnum#HEADING} if it's is shared by lines.
     */
    PositionInWorkReportEnum getDatePosition();

    /**
     * Check if a {@link org.libreplan.business.resources.entities.Resource} is shared by lines.
     *
     * @return a {@link PositionInWorkReportEnum#HEADING} if it's is shared by lines.
     */
    PositionInWorkReportEnum getResourcePosition();

    /**
     * Check if a {@link OrderElement} is shared by lines.
     *
     * @return a {@link PositionInWorkReportEnum#HEADING} if it's is shared by lines.
     */
    PositionInWorkReportEnum getOrderElementPosition();

    /**
     * Set weather the {@link java.util.Date} is shared by lines.
     */
    void setDatePosition(PositionInWorkReportEnum position);

    /**
     * Set weather the {@link org.libreplan.business.resources.entities.Resource} is shared by lines.
     */
    void setResourcePosition(PositionInWorkReportEnum position);

    /**
     * Set weather the {@link OrderElement} is shared by lines.
     */
    void setOrderElementPosition(PositionInWorkReportEnum position);

    /**
     * Check if the name of a {@link WorkReportType} is valid.
     *
     * @throw {@link IllegalArgumentException} if it's is null, empty or not unique.
     */
    void validateWorkReportTypeName(String name) throws IllegalArgumentException;

    /**
     * Check if the code of a {@link WorkReportType} is valid.
     *
     * @throw {@link IllegalArgumentException} if it's is null, empty or not unique.
     */
    void validateWorkReportTypeCode(String code) throws IllegalArgumentException;

    /**
     * Check if the lengths of the collection of {@link DescriptionField} are valid.
     *
     * @return the {@link DescriptionField} with the length negative or zero.
     */
    DescriptionField validateLengthLineFields() throws IllegalArgumentException;

    /**
     * Check if the fieldNames of the collection of {@link DescriptionField} are valid.
     *
     * @return the {@link DescriptionField} with the fieldName null, empty or not
     *         unique.
     */
    DescriptionField validateFieldNameLineFields() throws IllegalArgumentException;

    /**
     * Check if the collection of @{LabelType} are valid.
     *
     * @return the {@link WorkReportLabelTypeAssignment} with the LabelType null.
     */
    WorkReportLabelTypeAssignment validateLabelTypes();

    /**
     * Check if the collection of @{Label} are valid.
     *
     * @return the {@link WorkReportLabelTypeAssignment} with the Label null.
     */
    WorkReportLabelTypeAssignment validateLabels();

    boolean validateTheIndexFieldsAndLabels();

    List<Object> getOrderedListHeading();

    List<Object> getOrderedListLines();

    void upFieldOrLabel(Object objectToUp, boolean intoHeading);

    void downFieldOrLabel(Object objectToDown, boolean intoHeading);
}
