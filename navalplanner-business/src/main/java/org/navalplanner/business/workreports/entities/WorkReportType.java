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

package org.navalplanner.business.workreports.entities;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.NonUniqueResultException;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Valid;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.workreports.ValueObjects.DescriptionField;
import org.navalplanner.business.workreports.daos.IWorkReportTypeDAO;
/**
 * @author Diego Pino García <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class WorkReportType extends BaseEntity {

    public static WorkReportType create() {
        WorkReportType workReportType = new WorkReportType();
        workReportType.setNewObject(true);
        return workReportType;
    }

    public static WorkReportType create(String name, String code) {
        WorkReportType workReportType = new WorkReportType(name, code);
        workReportType.setNewObject(true);
        return workReportType;
    }

    private String name;

    private String code;

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

    private WorkReportType(String name, String code) {
        this.name = name;
        this.code = code;
    }

    @NotEmpty
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @NotEmpty
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
    public boolean validateWorkReportTypeCode() {
        if ((code == null) || (code.contains("_"))) {
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
            }
        }
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "work report type code is already being used")
    public boolean checkConstraintUniqueWorkReportTypeCode() {

        IWorkReportTypeDAO workReportTypeDAO = Registry.getWorkReportTypeDAO();

        if (isNewObject()) {
            return !workReportTypeDAO.existsByCodeAnotherTransaction(this);
        } else {
            try {
                WorkReportType c = workReportTypeDAO.findUniqueByCode(code);
                return c.getId().equals(getId());
            } catch (InstanceNotFoundException e) {
                return true;
            } catch (NonUniqueResultException e) {
                return false;
            }

        }
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "The field name must be unique.")
    public boolean validateTheUniqueNamesDescriptionFields() {
        for (DescriptionField descriptionField : getDescriptionFields()) {
            if (existSameFieldName(descriptionField)) {
                return false;
            }
        }
        return true;
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

    private Set<DescriptionField> getDescriptionFields() {
        Set<DescriptionField> descriptionFields = new HashSet<DescriptionField>();
        descriptionFields.addAll(this.getHeadingFields());
        descriptionFields.addAll(this.getLineFields());
        return descriptionFields;
    }
}
