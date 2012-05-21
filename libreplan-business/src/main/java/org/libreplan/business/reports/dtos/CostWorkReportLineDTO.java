/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 WirelessGalcia S.L.
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

package org.libreplan.business.reports.dtos;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import org.joda.time.LocalTime;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.business.workreports.valueobjects.DescriptionValue;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CostWorkReportLineDTO implements Comparable<CostWorkReportLineDTO> {

    private OrderElement orderElement;

    private String workerName;

    private Date date;

    private LocalTime clockStart;

    private LocalTime clockFinish;

    private BigDecimal numHours;

    private String descriptionValues;

    private String labels;

    private String hoursType;

    private String hoursTypeCode;

    // Attached outside the DTO
    private BigDecimal cost;

    // Attached outside the DTO
    private BigDecimal costPerHour;

    private Worker worker;

    private Boolean costTypeHours = Boolean.TRUE;

    public CostWorkReportLineDTO(Worker worker, WorkReportLine workReportLine) {

        this.workerName = worker.getName();
        if (workReportLine.getLocalDate() != null) {
            this.date = workReportLine.getLocalDate().toDateTimeAtStartOfDay().toDate();
        }
        this.clockStart = workReportLine.getClockStart();
        this.clockFinish = workReportLine.getClockFinish();
        this.numHours = workReportLine.getEffort().toHoursAsDecimalWithScale(2);
        this.descriptionValues = descriptionValuesAsString(workReportLine.getDescriptionValues());
        this.labels = labelsAsString(workReportLine.getLabels());
        this.hoursType = workReportLine.getTypeOfWorkHours().getName();
        this.hoursTypeCode = workReportLine.getTypeOfWorkHours().getCode();
        this.worker = worker;
    }

    public CostWorkReportLineDTO(OrderCostsPerResourceDTO dto) {
        this.workerName = dto.getWorkerName();
        this.date = dto.getDate();
        this.clockStart = dto.getClockStart();
        this.clockFinish = dto.getClockFinish();
        this.numHours = dto.getNumHours();
        this.descriptionValues = dto.getDescriptionValues();
        this.labels = dto.getLabels();
        this.hoursType = dto.getHoursType();
        this.hoursTypeCode = dto.getHoursTypeCode();
        this.worker = dto.getWorker();
    }

    private String labelsAsString(Set<Label> labels) {
        String result = "";
        for (Label label : labels) {
            result = label.getType().getName() + ": " + label.getName() + ", ";
        }
        return (result.length() > 0) ? result.substring(0, result.length() - 2) : result;
    }

    private String descriptionValuesAsString(Set<DescriptionValue> descriptionValues) {
        String result = "";
        for (DescriptionValue descriptionValue : descriptionValues) {
            result = descriptionValue.getFieldName() + ": " + descriptionValue.getValue() + ", ";
        }
        return (result.length() > 0) ? result.substring(0, result.length() - 2) : result;
    }

    public BigDecimal getNumHours() {
        return numHours;
    }

    public void setNumHours(BigDecimal numHours) {
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

    public String getHoursType() {
        return hoursType;
    }

    public void setHoursType(String hoursType) {
        this.hoursType = hoursType;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BigDecimal getCostPerHour() {
        return costPerHour;
    }

    public void setCostPerHour(BigDecimal costPerHour) {
        this.costPerHour = costPerHour;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public int compareTo(CostWorkReportLineDTO o) {
        String comparator = this.workerName;
        int result = comparator.compareToIgnoreCase(o.workerName);
        if (result == 0) {
            if ((this.date != null) && (o.getDate() != null)) {
                if (this.date.compareTo(o.getDate()) == 0) {
                    return this.hoursType.compareToIgnoreCase(o.getHoursType());
                }
                return this.date.compareTo(o.getDate());
            } else {
                return -1;
            }
        }
        return result;
    }

    public void setHoursTypeCode(String hoursTypeCode) {
        this.hoursTypeCode = hoursTypeCode;
    }

    public String getHoursTypeCode() {
        return hoursTypeCode;
    }

    public void setCostTypeHours(Boolean costTypeHours) {
        this.costTypeHours = costTypeHours;
    }

    public Boolean getCostTypeHours() {
        return costTypeHours;
    }

    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    public OrderElement getOrderElement() {
        return orderElement;
    }

}