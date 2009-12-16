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
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.workreports.valueobjects.DescriptionField;
import org.navalplanner.business.workreports.valueobjects.DescriptionValue;

/**
 * @author Diego Pino García <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class WorkReportLine extends BaseEntity implements Comparable {

    public static final String DATE = "date";

    public static final String RESOURCE = "resource";

    public static final String ORDER_ELEMENT = "orderElement";

    public static final String HOURS = "numHours";

    public static WorkReportLine create() {
        WorkReportLine workReportLine = new WorkReportLine();
        workReportLine.setNewObject(true);
        return workReportLine;
    }

    public static WorkReportLine create(Integer numHours, Resource resource,
            OrderElement orderElement) {
        WorkReportLine workReportLine = new WorkReportLine(numHours, resource,
                orderElement);
        workReportLine.setNewObject(true);
        return workReportLine;
    }

    private Integer numHours;

    private Date date;

    private Date clockStart;

    private Date clockFinish;

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
    }

    public Date getClockFinish() {
        return clockFinish;
    }

    public void setClockFinish(Date clockFinish) {
        this.clockFinish = clockFinish;
        updateNumHours();
    }

    public Date getClockStart() {
        return clockStart;
    }

    public void setClockStart(Date clockStart) {
        this.clockStart = clockStart;
        updateNumHours();
    }

    @NotNull(message = "date not specified")
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @NotNull(message = "resource not specified")
    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @NotNull(message = "order element not specified")
    public OrderElement getOrderElement() {
        return orderElement;
    }

    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    public Set<Label> getLabels() {
        return labels;
    }

    public void setLabels(Set<Label> labels) {
        this.labels = labels;
    }

    public WorkReport getWorkReport() {
        return workReport;
    }

    public void setWorkReport(WorkReport workReport) {
        this.workReport = workReport;

        // update and copy the fields and label for each line
        updateItsFieldsAndLabels();

        // copy the required fields if these are shared by lines
        updatesAllSharedDataByLines();
    }

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
        if (workReport.getWorkReportType().getHoursManagement().equals(
                HoursManagementEnum.HOURS_CALCULATED_BY_CLOCK)) {
            setNumHours(getDiferenceBetweenTimeStartAndFinish());
        }
    }

    private Integer getDiferenceBetweenTimeStartAndFinish() {
        if ((getClockStart() != null) && (getClockFinish() != null)) {
            Long divisor = new Long(3600000);
            Long topHour = new Long(24);
            Long clockStart = new Long(0);
            Long clockFinish = new Long(0);
            Long numHours;

            clockStart = (getClockStart().getTime()) / divisor;
            clockFinish = (getClockFinish().getTime()) / divisor;

            // if clock start greater than clock finish
            if (clockStart.compareTo(clockFinish) > 0) {
                numHours = topHour - clockStart;
                numHours = numHours + clockFinish;
            } else {
                numHours = clockFinish - clockStart;
            }
            return numHours.intValue();
        }
        return null;
    }

}
