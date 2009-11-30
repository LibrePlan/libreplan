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

import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.workreports.ValueObjects.DescriptionField;

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

    @NotNull
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private Boolean dateIsSharedByLines;

    private Boolean resourceIsSharedInLines;

    private Boolean orderElementIsSharedInLines;

    private HoursManagementEnum hoursManagement;

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

    public Set<DescriptionField> getHeadingFields() {
        return headingFields;
    }

    public void setHeadingFields(Set<DescriptionField> headingFields) {
        this.headingFields = headingFields;
    }

    public Set<DescriptionField> getLineFields() {
        return lineFields;
    }

    public void setLineFields(Set<DescriptionField> lineFields) {
        this.lineFields = lineFields;
    }

}
