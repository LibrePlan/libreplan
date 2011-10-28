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

package org.libreplan.business.workreports.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.NonUniqueResultException;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Valid;
import org.libreplan.business.common.IHumanIdentifiable;
import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.labels.entities.LabelType;
import org.libreplan.business.workreports.daos.IWorkReportTypeDAO;
import org.libreplan.business.workreports.valueobjects.DescriptionField;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
/**
 * @author Diego Pino García <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class WorkReportType extends IntegrationEntity implements IHumanIdentifiable{

    public static WorkReportType create() {
        return create(new WorkReportType());
    }

    public static WorkReportType create(String name, String code) {
        return create(new WorkReportType(name), code);
    }

    private String name;

    private Boolean dateIsSharedByLines = false;

    private Boolean resourceIsSharedInLines = false;

    private Boolean orderElementIsSharedInLines = false;

    private HoursManagementEnum hoursManagement = HoursManagementEnum
            .getDefault();

    private Set<WorkReportLabelTypeAssigment> workReportLabelTypeAssigments = new HashSet<WorkReportLabelTypeAssigment>();

    private Set<DescriptionField> headingFields = new HashSet<DescriptionField>();

    private Set<DescriptionField> lineFields = new HashSet<DescriptionField>();

    /**
     * Constructor for hibernate. Do not use!
     */
    public WorkReportType() {

    }

    private WorkReportType(String name) {
        this.name = name;
    }

    @NotEmpty(message = "name not specified or empty")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getDateIsSharedByLines() {
        return dateIsSharedByLines == null ? false : dateIsSharedByLines;
    }

    public void setDateIsSharedByLines(Boolean dateIsSharedByLines) {
        this.dateIsSharedByLines = dateIsSharedByLines;
    }

    public Boolean getResourceIsSharedInLines() {
        return resourceIsSharedInLines == null ? false
                : resourceIsSharedInLines;
    }

    public void setResourceIsSharedInLines(Boolean resourceIsSharedInLines) {
        this.resourceIsSharedInLines = resourceIsSharedInLines;
    }

    public Boolean getOrderElementIsSharedInLines() {
        return orderElementIsSharedInLines == null ? false
                : orderElementIsSharedInLines;
    }

    public void setOrderElementIsSharedInLines(
            Boolean orderElementIsSharedInLines) {
        this.orderElementIsSharedInLines = orderElementIsSharedInLines;
    }

    public HoursManagementEnum getHoursManagement() {
        return hoursManagement;
    }

    public void setHoursManagement(HoursManagementEnum hoursManagement) {
        this.hoursManagement = hoursManagement;
    }

    @Valid
    public Set<WorkReportLabelTypeAssigment> getWorkReportLabelTypeAssigments() {
        return workReportLabelTypeAssigments;
    }

    public void setWorkReportLabelTypeAssigments(
            Set<WorkReportLabelTypeAssigment> workReportLabelTypeAssigments) {
        this.workReportLabelTypeAssigments = workReportLabelTypeAssigments;
    }

    @Valid
    public Set<DescriptionField> getHeadingFields() {
        return headingFields;
    }

    public void setHeadingFields(Set<DescriptionField> headingFields) {
        this.headingFields = headingFields;
    }

    @Valid
    public Set<DescriptionField> getLineFields() {
        return lineFields;
    }

    public void setLineFields(Set<DescriptionField> lineFields) {
        this.lineFields = lineFields;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "Value is not valid.\n Code cannot contain chars like '_'.")
    public boolean checkConstraintWorkReportTypeCodeWithoutIncorrectCharacter() {
        if ((getCode() == null) || (getCode().contains("_"))) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "work report type name is already being used")
    public boolean checkConstraintUniqueWorkReportTypeName() {
        IWorkReportTypeDAO workReportTypeDAO = Registry.getWorkReportTypeDAO();
        if (isNewObject()) {
            return !workReportTypeDAO.existsByNameAnotherTransaction(this);
        } else {
            try {
                WorkReportType c = workReportTypeDAO.findUniqueByName(name);
                return c.getId().equals(getId());
            } catch (InstanceNotFoundException e) {
                return true;
            } catch (NonUniqueResultException e) {
                return false;
            } catch (HibernateOptimisticLockingFailureException e) {
                return true;
            }
        }
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "The field name must be unique.")
    public boolean checkConstraintUniqueNamesDescriptionFields() {
        for (DescriptionField descriptionField : getDescriptionFields()) {
            if (existSameFieldName(descriptionField)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "the assigned label type can not repeat in the work report type.")
    public boolean checkConstraintNotExistRepeatedLabelTypes() {
        for (WorkReportLabelTypeAssigment assignedLabelType : this.workReportLabelTypeAssigments) {
            if (existRepeatedLabelType(assignedLabelType)) {
                return false;
            }
        }
        return true;
    }

    public boolean existRepeatedLabelType(
            WorkReportLabelTypeAssigment assignedLabelType) {
        for (WorkReportLabelTypeAssigment oldAssignedLabelType : this.workReportLabelTypeAssigments) {
            if ((!oldAssignedLabelType.equals(assignedLabelType))
                    && (isTheSameLabelType(oldAssignedLabelType.getLabelType(),
                            assignedLabelType.getLabelType()))) {
                return true;
            }
        }
        return false;
    }

    public boolean isTheSameLabelType(LabelType oldLabelType,
            LabelType newLabelType) {
        if ((oldLabelType != null) && (newLabelType != null)
                && (oldLabelType.equals(newLabelType))) {
            return true;
        }
        return false;
    }

    public boolean existSameFieldName(DescriptionField descriptionField) {
        for (DescriptionField oldDescriptionField : getDescriptionFields()) {
            if ((!oldDescriptionField.equals(descriptionField))
                    && (isTheSameFieldName(oldDescriptionField.getFieldName(),
                            descriptionField.getFieldName()))) {
                return true;
            }
        }
        return false;
    }

    private boolean isTheSameFieldName(String oldName, String newName) {
        if ((oldName != null) && (newName != null) && (!oldName.isEmpty())
                && (!newName.isEmpty()) && (oldName.equals(newName))) {
            return true;
        }
        return false;
    }

    public Set<DescriptionField> getDescriptionFields() {
        Set<DescriptionField> descriptionFields = new HashSet<DescriptionField>();
        descriptionFields.addAll(this.getHeadingFields());
        descriptionFields.addAll(this.getLineFields());
        return descriptionFields;
    }

    /* Operation to manage the index */

    public void addDescriptionFieldToEndLine(DescriptionField descriptionField) {
        addDescriptionFieldToLine(descriptionField, getLineFieldsAndLabels()
                .size());
    }

    public void addDescriptionFieldToEndHead(DescriptionField descriptionField) {
        addDescriptionFieldToHead(descriptionField, getHeadingFieldsAndLabels()
                .size());
    }

    public void addLabelAssigmentToEndHead(
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment) {
        addLabelAssigmentToHead(workReportLabelTypeAssigment,
                getHeadingFieldsAndLabels().size());
    }

    public void addLabelAssigmentToEndLine(
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment) {
        addLabelAssigmentToLine(workReportLabelTypeAssigment,
                getLineFieldsAndLabels().size());
    }

    public void addDescriptionFieldToLine(DescriptionField descriptionField,
            int position) {
        if (isValidIndexToAdd(position, getLineFieldsAndLabels())) {
            updateIndexFromPosition(getLineFieldsAndLabels(), position, 1);
            descriptionField.setPositionNumber(position);
            getLineFields().add(descriptionField);
        }
    }

    public void addDescriptionFieldToHead(DescriptionField descriptionField,
            int position) {
        if (isValidIndexToAdd(position, getHeadingFieldsAndLabels())) {
            updateIndexFromPosition(getHeadingFieldsAndLabels(), position, 1);
            descriptionField.setPositionNumber(position);
            getHeadingFields().add(descriptionField);
        }
    }

    public void addLabelAssigmentToHead(
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment,
            int position) {
        if (isValidIndexToAdd(position, getHeadingFieldsAndLabels())) {
            updateIndexFromPosition(getHeadingFieldsAndLabels(), position, 1);
            workReportLabelTypeAssigment.setLabelsSharedByLines(true);
            workReportLabelTypeAssigment.setPositionNumber(position);
            getWorkReportLabelTypeAssigments()
                    .add(workReportLabelTypeAssigment);
        }
    }

    public void addLabelAssigmentToLine(
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment,
            int position) {
        if (isValidIndexToAdd(position, getLineFieldsAndLabels())) {
            updateIndexFromPosition(getLineFieldsAndLabels(), position, 1);
            workReportLabelTypeAssigment.setLabelsSharedByLines(false);
            workReportLabelTypeAssigment.setPositionNumber(

            position);
            getWorkReportLabelTypeAssigments()
                    .add(workReportLabelTypeAssigment);
        }
    }

    public void moveLabelToEndHead(
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment) {
        moveLabelToHead(workReportLabelTypeAssigment,
                getHeadingFieldsAndLabels().size() - 1);
    }

    public void moveLabelToEndLine(
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment) {
        moveLabelToLine(workReportLabelTypeAssigment, getLineFieldsAndLabels()
                .size() - 1);
    }

    public void moveDescriptionFieldToEndHead(DescriptionField descriptionField) {
        moveDescriptionFieldToHead(descriptionField,
                getHeadingFieldsAndLabels().size() - 1);
    }

    public void moveDescriptionFieldToEndLine(DescriptionField descriptionField) {
        moveDescriptionFieldToLine(descriptionField, getLineFieldsAndLabels()
                .size() - 1);
    }

    public void moveLabelToHead(
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment,
            int position) {
        if (isValidIndexToMove(position, getHeadingFieldsAndLabels())) {
            removeLabel(workReportLabelTypeAssigment);
            addLabelAssigmentToHead(workReportLabelTypeAssigment, position);
        }
    }

    public void moveLabelToLine(
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment,
            int position) {
        if (isValidIndexToMove(position, getLineFieldsAndLabels())) {
            removeLabel(workReportLabelTypeAssigment);
            addLabelAssigmentToLine(workReportLabelTypeAssigment, position);
        }
    }

    public void moveDescriptionFieldToHead(DescriptionField descriptionField,
            int position) {
        if (isValidIndexToMove(position, getHeadingFieldsAndLabels())) {
            removeDescriptionField(descriptionField);
            addDescriptionFieldToHead(descriptionField, position);
        }
    }

    public void moveDescriptionFieldToLine(DescriptionField descriptionField,
            int position) {
        if (isValidIndexToMove(position, getLineFieldsAndLabels())) {
            removeDescriptionField(descriptionField);
            addDescriptionFieldToLine(descriptionField, position);
        }
    }

    public void removeDescriptionField(DescriptionField descriptionField){
        if (getHeadingFields().contains(descriptionField)) {
            getHeadingFields().remove(descriptionField);
            updateIndexFromPosition(getHeadingFieldsAndLabels(),
                    descriptionField.getPositionNumber(), -1);
        } else {
            getLineFields().remove(descriptionField);
            updateIndexFromPosition(getLineFieldsAndLabels(), descriptionField
                    .getPositionNumber(), -1);
        }

    }

    public void removeLabel(WorkReportLabelTypeAssigment workReportLabelTypeAssigment) {
        getWorkReportLabelTypeAssigments().remove(workReportLabelTypeAssigment);
        if (workReportLabelTypeAssigment.getLabelsSharedByLines()) {
            updateIndexFromPosition(getHeadingFieldsAndLabels(),
                    workReportLabelTypeAssigment.getPositionNumber(), -1);
        } else {
            updateIndexFromPosition(getLineFieldsAndLabels(),
                    workReportLabelTypeAssigment.getPositionNumber(), -1);
        }
    }

    private void setIndex(Object object, Integer index) {
        if (object instanceof DescriptionField) {
            ((DescriptionField) object).setPositionNumber(index);
        } else {
            ((WorkReportLabelTypeAssigment) object).setPositionNumber(index);
        }
    }

    private void updateIndexFromPosition(List<Object> list, Integer position,
            Integer change) {
        for (int i = 0; i < list.size(); i++) {
            if (getIndex(list.get(i)).compareTo(position) >= 0) {
                setIndex(list.get(i), getIndex(list.get(i)) + change);
            }
        }
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "In Heading the index labels and fields  must be unique and consecutive.")
    public boolean checkConstraintTheIndexHeadingFieldsAndLabelMustBeUniqueAndConsecutive() {
        return validateTheIndexFieldsAndLabels(getHeadingFieldsAndLabels());
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "In Lines the index labels and fields  must be unique and consecutive.")
    public boolean checkConstraintTheIndexLineFieldsAndLabelMustBeUniqueAndConsecutive() {
        return validateTheIndexFieldsAndLabels(getLineFieldsAndLabels());
    }

    private boolean validateTheIndexFieldsAndLabels(
            List<Object> listFieldsAndLabels) {
        List<Object> result = getListToNull(listFieldsAndLabels);
        for (Object object : listFieldsAndLabels) {
            // Check if index is out of range
            Integer index = getIndex(object);
            if ((index.compareTo(0) < 0)
                    || (index.compareTo(result.size()) >= 0)) {
                return false;
            }
            // Check if index is repeated
            if (result.get(getIndex(object)) != null) {
                return false;
            }
            result.set(getIndex(object), object);
        }

        // Check if the indexs are consecutives
        for (Object object : result) {
            if (object == null) {
                return false;
            }
        }
        return true;
    }

    public List<Object> getHeadingFieldsAndLabels() {
        List<Object> result = new ArrayList<Object>();
        result.addAll(getHeadingLabels());
        result.addAll(getHeadingFields());
        return result;
    }

    public List<Object> getLineFieldsAndLabels() {
        List<Object> result = new ArrayList<Object>();
        result.addAll(getLineLabels());
        result.addAll(getLineFields());
        return result;
    }

    public List<WorkReportLabelTypeAssigment> getHeadingLabels() {
        List<WorkReportLabelTypeAssigment> result = new ArrayList<WorkReportLabelTypeAssigment>();
        for (WorkReportLabelTypeAssigment label : getWorkReportLabelTypeAssigments()) {
            if (label.getLabelsSharedByLines()) {
                result.add(label);
            }
        }
        return result;
    }

    public List<WorkReportLabelTypeAssigment> getLineLabels() {
        List<WorkReportLabelTypeAssigment> result = new ArrayList<WorkReportLabelTypeAssigment>();
        for (WorkReportLabelTypeAssigment label : getWorkReportLabelTypeAssigments()) {
            if (!label.getLabelsSharedByLines()) {
                result.add(label);
            }
        }
        return result;
    }

    private Integer getIndex(Object object) {
        if (object instanceof DescriptionField) {
            return ((DescriptionField) object).getPositionNumber();
        } else {
            return ((WorkReportLabelTypeAssigment) object).getPositionNumber();
        }
    }

    private List<Object> getListToNull(List<Object> list) {
        List<Object> result = new ArrayList<Object>(list.size());
        for (int i = 0; i < list.size(); i++) {
            result.add(null);
        }
        return result;
    }

    private boolean isValidIndexToMove(Integer position, List<Object> list) {
        return ((position.compareTo(0) >= 0) && (position.compareTo(list.size()) < 0));
    }

    private boolean isValidIndexToAdd(Integer position, List<Object> list) {
        return ((position.compareTo(0) >= 0) && (position
                .compareTo(list.size()) <= 0));
    }

    @Override
    protected IWorkReportTypeDAO getIntegrationEntityDAO() {
        return Registry.getWorkReportTypeDAO();
    }

    @Override
    public String getHumanId() {
        return name;
    }
}