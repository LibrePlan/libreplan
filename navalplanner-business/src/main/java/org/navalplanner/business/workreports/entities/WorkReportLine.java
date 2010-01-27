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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.joda.time.Hours;
import org.joda.time.LocalTime;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.workreports.daos.IWorkReportLineDAO;
import org.navalplanner.business.workreports.valueobjects.DescriptionField;
import org.navalplanner.business.workreports.valueobjects.DescriptionValue;

/**
 * @author Diego Pino García <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class WorkReportLine extends IntegrationEntity implements Comparable {

    public static final String DATE = "date";

    public static final String RESOURCE = "resource";

    public static final String ORDER_ELEMENT = "orderElement";

    public static final String HOURS = "numHours";

    public static WorkReportLine create() {
        return create(new WorkReportLine());
    }

    public static WorkReportLine create(Integer numHours, Resource resource,
            OrderElement orderElement) {
        return create(new WorkReportLine(numHours, resource, orderElement));
    }

    private Integer numHours;

    private Date date;

    private LocalTime clockStart;

    private LocalTime clockFinish;

    private Resource resource;

    private OrderElement orderElement;

    private Set<Label> labels = new HashSet<Label>();

    private Set<DescriptionValue> descriptionValues = new HashSet<DescriptionValue>();

    private WorkReport workReport;

    private TypeOfWorkHours typeOfWorkHours;

    /**
     * Constructor for hibernate. Do not use!
     */
    public WorkReportLine() {

    }

    private WorkReportLine(Integer numHours, Resource resource,
            OrderElement orderElement) {
        this.numHours = numHours;
        this.resource = resource;
        this.orderElement = orderElement;
    }

    @NotNull(message = "number of hours not specified")
    public Integer getNumHours() {
        return numHours;
    }

    public void setNumHours(Integer numHours) {
        this.numHours = numHours;
        if ((workReport != null)
                && (workReport.getWorkReportType() != null)
                && (workReport.getWorkReportType().getHoursManagement()
                        .equals(HoursManagementEnum.HOURS_CALCULATED_BY_CLOCK))) {
            this.numHours = getDiferenceBetweenTimeStartAndFinish();
        }
    }

    public LocalTime getClockFinish() {
        return clockFinish;
    }

    public void setClockFinish(Date clockFinish) {
        if (clockFinish != null) {
            setClockFinish(LocalTime.fromDateFields(clockFinish));
        }
    }

    public void setClockFinish(LocalTime clockFinish) {
        this.clockFinish = clockFinish;
        updateNumHours();
    }

    public LocalTime getClockStart() {
        return clockStart;
    }

    public void setClockStart(Date clockStart) {
        if (clockStart != null) {
            setClockStart(LocalTime.fromDateFields(clockStart));
        }
    }

    public void setClockStart(LocalTime clockStart) {
        this.clockStart = clockStart;
        updateNumHours();
    }

    @NotNull(message = "date not specified")
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
        if ((workReport != null) && (workReport.getWorkReportType() != null)) {
            if (workReport.getWorkReportType().getDateIsSharedByLines()) {
                this.date = workReport.getDate();
            }
        }
    }

    @NotNull(message = "resource not specified")
    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
        if ((workReport != null) && (workReport.getWorkReportType() != null)) {
            if (workReport.getWorkReportType().getResourceIsSharedInLines()) {
                this.resource = workReport.getResource();
            }
        }
    }

    @NotNull(message = "order element not specified")
    public OrderElement getOrderElement() {
        return orderElement;
    }

    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
        if ((workReport != null) && (workReport.getWorkReportType() != null)) {
            if (workReport.getWorkReportType().getOrderElementIsSharedInLines()) {
                this.orderElement = workReport.getOrderElement();
            }
        }
    }

    public Set<Label> getLabels() {
        return labels;
    }

    public void setLabels(Set<Label> labels) {
        this.labels = labels;
    }

    @NotNull(message = "work report not specified")
    public WorkReport getWorkReport() {
        return workReport;
    }

    public void setWorkReport(WorkReport workReport) {
        this.workReport = workReport;

        // update and copy the fields and label for each line
        updateItsFieldsAndLabels();

        // copy the required fields if these are shared by lines
        updatesAllSharedDataByLines();

        // update calculated hours
        updateNumHours();
    }

    @Valid
    public Set<DescriptionValue> getDescriptionValues() {
        return descriptionValues;
    }

    public void setDescriptionValues(Set<DescriptionValue> descriptionValues) {
        this.descriptionValues = descriptionValues;
    }

    @NotNull(message = "type of work hours not specified")
    public TypeOfWorkHours getTypeOfWorkHours() {
        return typeOfWorkHours;
    }

    public void setTypeOfWorkHours(TypeOfWorkHours typeOfWorkHours) {
        this.typeOfWorkHours = typeOfWorkHours;
    }

    @Override
    public int compareTo(Object arg0) {
        if (date != null) {
            final WorkReportLine workReportLine = (WorkReportLine) arg0;
            return date.compareTo(workReportLine.getDate());
        }
        return -1;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "closckStart:the clockStart must be not null if number of hours is calcultate by clock")
    public boolean checkConstraintClockStartMustBeNotNullIfIsCalculatedByClock() {
        if (workReport.getWorkReportType().getHoursManagement().equals(
                HoursManagementEnum.HOURS_CALCULATED_BY_CLOCK)) {
            return (getClockStart() != null);
        }
        return true;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "closckFinish:the clockStart must be not null if number of hours is calcultate by clock")
    public boolean checkConstraintClockFinishMustBeNotNullIfIsCalculatedByClock() {
        if (workReport.getWorkReportType().getHoursManagement().equals(
                HoursManagementEnum.HOURS_CALCULATED_BY_CLOCK)) {
            return (getClockFinish() != null);
        }
        return true;
    }

    void updateItsFieldsAndLabels() {
        if (workReport != null) {
            assignItsLabels(workReport.getWorkReportType());
            assignItsDescriptionValues(workReport.getWorkReportType());
        }
    }

    private void assignItsLabels(WorkReportType workReportType) {
        if (workReportType != null) {
            labels.clear();
            for (WorkReportLabelTypeAssigment labelTypeAssigment : workReportType
                    .getLineLabels()) {
                labels.add(labelTypeAssigment.getDefaultLabel());
            }
        }
    }

    private void assignItsDescriptionValues(WorkReportType workReportType) {
        if (workReportType != null) {
            descriptionValues.clear();
            for (DescriptionField descriptionField : workReportType
                    .getLineFields()) {
                DescriptionValue descriptionValue = DescriptionValue.create(
                        descriptionField.getFieldName(), null);
                descriptionValues.add(descriptionValue);
            }
        }
    }

    void updatesAllSharedDataByLines() {
        // copy the required fields if these are shared by lines
        updateSharedDateByLines();
        updateSharedResourceByLines();
        updateSharedOrderElementByLines();
    }

    void updateSharedDateByLines() {
        if ((workReport != null) && (workReport.getWorkReportType() != null)
                && (workReport.getWorkReportType().getDateIsSharedByLines())) {
            setDate(workReport.getDate());
        }
    }

    void updateSharedResourceByLines() {
        if ((workReport != null)
                && (workReport.getWorkReportType() != null)
                && (workReport.getWorkReportType().getResourceIsSharedInLines())) {
            setResource(workReport.getResource());
        }
    }

    void updateSharedOrderElementByLines() {
        if ((workReport != null)
                && (workReport.getWorkReportType() != null)
                && (workReport.getWorkReportType()
                        .getOrderElementIsSharedInLines())) {
            setOrderElement(workReport.getOrderElement());
        }
    }

    private void updateNumHours() {
        if ((workReport != null)
                && (workReport.getWorkReportType() != null)
                && workReport.getWorkReportType().getHoursManagement().equals(
                        HoursManagementEnum.HOURS_CALCULATED_BY_CLOCK)) {
            setNumHours(getDiferenceBetweenTimeStartAndFinish());
        }
    }

    private Integer getDiferenceBetweenTimeStartAndFinish() {
        if ((clockStart != null) && (clockFinish != null)) {
            return Hours.hoursBetween(clockStart, clockFinish).getHours();
        }
        return null;
    }

    @Override
    protected IWorkReportLineDAO getIntegrationEntityDAO() {
        return Registry.getWorkReportLineDAO();
    }

    @AssertTrue(message = "fields should match with work report data if are shared by lines")
    public boolean checkConstraintFieldsMatchWithWorkReportIfAreSharedByLines() {
        if (workReport.getWorkReportType().getDateIsSharedByLines()) {
            if (!workReport.getDate().equals(date)) {
                return false;
            }
        }
        if (workReport.getWorkReportType().getOrderElementIsSharedInLines()) {
            if (!workReport.getOrderElement().getId().equals(
                    orderElement.getId())) {
                return false;
            }
        }
        if (workReport.getWorkReportType().getResourceIsSharedInLines()) {
            if (!workReport.getResource().getId().equals(resource.getId())) {
                return false;
            }
        }
        return true;
    }

    @AssertTrue(message = "number of hours is not properly calculated based on clock")
    public boolean checkConstraintHoursCalculatedByClock() {
        if (workReport.getWorkReportType().getHoursManagement().equals(
                HoursManagementEnum.HOURS_CALCULATED_BY_CLOCK)) {
            if (getDiferenceBetweenTimeStartAndFinish() != numHours) {
                return false;
            }
        }
        return true;
    }

}
