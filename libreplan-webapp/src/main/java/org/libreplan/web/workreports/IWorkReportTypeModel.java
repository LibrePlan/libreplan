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
import org.libreplan.business.workreports.entities.WorkReportLabelTypeAssigment;
import org.libreplan.business.workreports.entities.WorkReportType;
import org.libreplan.business.workreports.valueobjects.DescriptionField;
import org.libreplan.web.common.IIntegrationEntityModel;

/**
 * Contract for {@link WorkRerportType}
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
     * @return true if it's editing a {@link WorkReportType}
     */
    boolean isEditing();

    /**
     * Set if it's or not shows {@link WorkReportType} list
     * @return true if it's shows the list.
     */
    void setListing(boolean listing);

    /**
     * Check if there is any {@link WorkReport} bound to {@link WorkReportType}
     * @param workReportType
     * @return
     */
    boolean thereAreWorkReportsFor(WorkReportType workReportType);

    /**
     * Check if there is any {@link WorkReport} bound to {@link WorkReportType}
     * which have been edited.
     * @param workReportType
     * @return
     */
    boolean thereAreWorkReportsFor();

    /**
     * Gets the current list of assigned {@link DescripitonField} to the edited
     * {@link WorkReportType}.
     * @return A List {@link DescripitonField}
     */
    public List<DescriptionField> getDescriptionFields();

    /**
     * Gets the current list of {@link LabelType}
     * @return A List {@link LabelType}
     */
    Map<LabelType, List<Label>> getMapLabelTypes();

    /**
     * Add a new {@link DescriptionField} to {@link WorkReportType} For default
     * to the LineFields collection.
     * @param
     * @return
     */
    public void addNewDescriptionField();

    /**
     * Delete a {@link DescriptionField} from {@link WorkReportType}
     * @param DescriptionField
     * @return
     */
    public void removeDescriptionField(DescriptionField descriptionField);

    /**
     * Change the @{PositionInWorkReportEnum} of a {@link DescriptionField} to
     * other collection of {@link WorkReportType}
     * @param @{PositionInWorkReportEnum} ,@{DescriptionField}
     * @return
     */
    void changePositionDescriptionField(
            PositionInWorkReportEnum newPosition,
            DescriptionField descriptionField);

    /**
     * return the @{PositionInWorkReportEnum} of a {@link DescriptionField}.
     * @param @{DescriptionField}
     * @return @{PositionInWorkReportEnum}
     */
    PositionInWorkReportEnum getPosition(
            DescriptionField descriptionField);

    /**
     * Check if a @{DescriptionField} is into the
     * @{PositionInWorkReportEnum.HEADING}
     * @return true if it's is into the @{PositionInWorkReportEnum.HEADING}
     */
    boolean isHeadingDescriptionField(DescriptionField descriptionField);

    /**
     * Gets the {@link List} of {@link WorkReportLabelTypeAssigment}.
     * @return A {@link List} of {@link WorkReportLabelTypeAssigment}
     */
    Set<WorkReportLabelTypeAssigment> getWorkReportLabelTypeAssigments();

    /**
     * Add a new {@link WorkReportLabelTypeAssigment} to {@link WorkReportType}.
     * @param
     * @return
     */
    void addNewWorkReportLabelTypeAssigment();

    /**
     * Delete a {@link WorkReportLabelTypeAssigment} from {@link WorkReportType}
     * @param {@link WorkReportLabelTypeAssigment}
     * @return
     */
    void removeWorkReportLabelTypeAssigment(
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment);

    /**
     * Check if a {@link WorkReportLabelTypeAssigment} is shared by lines
     * @return a @{PositionInWorkReportEnum.HEADING} if it's is shared by lines.
     */
    PositionInWorkReportEnum getLabelAssigmentPosition(
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment);

    /**
     * Set weather the {@link WorkReportLabelTypeAssigment} is shared by lines
     * @return
     */
    void setLabelAssigmentPosition(
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment,
            PositionInWorkReportEnum position);

    /* Operation to assign the requirements fields */

    /**
     * Check if a {@link Date} is shared by lines
     * @return a @{PositionInWorkReportEnum.HEADING} if it's is shared by lines.
     */
    PositionInWorkReportEnum getDatePosition();

    /**
     * Check if a {@link Resource} is shared by lines
     * @return a @{PositionInWorkReportEnum.HEADING} if it's is shared by lines.
     */
    PositionInWorkReportEnum getResourcePosition();

    /**
     * Check if a {@link OrderElement} is shared by lines
     * @return a @{PositionInWorkReportEnum.HEADING} if it's is shared by lines.
     */
    PositionInWorkReportEnum getOrderElementPosition();

    /**
     * Set weather the {@link Date} is shared by lines
     * @return
     */
    void setDatePosition(PositionInWorkReportEnum position);

    /**
     * Set weather the {@link Resource} is shared by lines
     * @return
     */
    void setResourcePosition(PositionInWorkReportEnum position);

    /**
     * Set weather the {@link OrderElement} is shared by lines
     * @return
     */
    void setOrderElementPosition(PositionInWorkReportEnum position);

    /* Operations to validate the data workReportType */

    /**
     * Check if the name of a {@link WorkReportType} is valid.
     * @throw @{IllegalArgumentException} if it's is null, empty or not unique.
     */
    public void validateWorkReportTypeName(String name)
            throws IllegalArgumentException;

    /**
     * Check if the code of a {@link WorkReportType} is valid.
     * @throw @{IllegalArgumentException} if it's is null, empty or not unique.
     */
    void validateWorkReportTypeCode(String code)
            throws IllegalArgumentException;

    /**
     * Check if the leghts of the collection of {@link DescriptionField} are
     * valids.
     * @return the @{DescriptionField} with the length negative or zero.
     */
    DescriptionField validateLengthLineFields() throws IllegalArgumentException;

    /**
     * Check if the fieldNames of the collection of {@link DescriptionField} are
     * valids.
     * @return the @{DescriptionField} with the fieldName null, empty or not
     *         unique.
     */
    DescriptionField validateFieldNameLineFields()
            throws IllegalArgumentException;

    // /**
    // * Check if the fieldName of a {@link DescriptionField} is equal to the
    // * fieldName of another {@link DescriptionField}.
    // * @return true if exist other {@link DescriptionField} with the same
    // * fieldName.
    // */
    // boolean existSameFieldName(DescriptionField descriptionField);

    /**
     * Check if the collection of @{LabelType} are valids.
     * @return the @{WorkReportLabelTypeAssigment} with the LabelType null.
     */
    WorkReportLabelTypeAssigment validateLabelTypes();

    /**
     * Check if the collection of @{Label} are valids.
     * @return the @{WorkReportLabelTypeAssigment} with the Label null.
     */
    WorkReportLabelTypeAssigment validateLabels();

    /* Operation to manage the ordered list of fields and labels */
    boolean validateTheIndexFieldsAndLabels();

    List<Object> getOrderedListHeading();

    List<Object> getOrderedListLines();

    void upFieldOrLabel(Object objectToUp, boolean intoHeading);

    void downFieldOrLabel(Object objectToDown, boolean intoHeading);
}
