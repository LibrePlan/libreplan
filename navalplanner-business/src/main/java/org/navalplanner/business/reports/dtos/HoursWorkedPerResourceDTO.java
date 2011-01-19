/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.reports.dtos;

import java.util.Date;
import java.util.Set;

import org.joda.time.LocalTime;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.business.workreports.valueobjects.DescriptionValue;

public class HoursWorkedPerResourceDTO {

    private String workerName;

    private Date date;

    private LocalTime clockStart;

    private LocalTime clockFinish;

    private Integer numHours;

    private String orderElementCode;

    private String descriptionValues;

    private String labels;

    public HoursWorkedPerResourceDTO(
Resource resource,
            WorkReportLine workReportLine) {

        this.workerName = resource.getName();
        this.date = workReportLine.getDate();
        this.clockStart = workReportLine.getClockStart();
        this.clockFinish = workReportLine.getClockFinish();
        this.numHours = workReportLine.getNumHours();
        this.orderElementCode = workReportLine.getOrderElement().getCode();
        this.descriptionValues = descriptionValuesAsString(workReportLine.getDescriptionValues());
        this.labels = labelsAsString(workReportLine.getLabels());
    }

    private String labelsAsString(Set<Label> labels) {
        String result = "";
        for (Label label: labels) {
            result = label.getType().getName() + ": " + label.getName() + ", ";
        }
        return (result.length() > 0) ? result.substring(0, result.length() - 2) : result;
    }

    private String descriptionValuesAsString(Set<DescriptionValue> descriptionValues) {
        String result = "";
        for (DescriptionValue descriptionValue: descriptionValues) {
            result = descriptionValue.getFieldName() + ": " + descriptionValue.getValue() + ", ";
        }
        return (result.length() > 0) ? result.substring(0, result.length() - 2) : result;
    }

    public Integer getNumHours() {
        return numHours;
    }

    public void setNumHours(Integer numHours) {
        this.numHours = numHours;
    }

    public LocalTime getClockStart() {
        return clockStart;
    }

    public void setClockStart(LocalTime clockStart) {
        this.clockStart = clockStart;
    }

    public LocalTime getClockFinish() {
        return clockFinish;
    }

    public void setClockFinish(LocalTime clockFinish) {
        this.clockFinish = clockFinish;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getOrderElementCode() {
        return orderElementCode;
    }

    public void setOrderElementCode(String orderElementCode) {
        this.orderElementCode = orderElementCode;
    }

    public String getDescriptionValues() {
        return descriptionValues;
    }

    public void setDescriptionValues(String descriptionValues) {
        this.descriptionValues = descriptionValues;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

}