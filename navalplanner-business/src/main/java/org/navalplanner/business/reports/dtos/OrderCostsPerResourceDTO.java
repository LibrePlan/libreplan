/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import org.joda.time.LocalTime;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.business.workreports.valueobjects.DescriptionValue;

public class OrderCostsPerResourceDTO implements
        Comparable<OrderCostsPerResourceDTO> {

    private String workerName;

    private Date date;

    private LocalTime clockStart;

    private LocalTime clockFinish;

    private Integer numHours;

    private String orderElementName;

    private String orderElementCode;

    private String descriptionValues;

    private String labels;

    private String hoursType;

    private String hoursTypeCode;

    // Attached outside the DTO
    private BigDecimal cost;

    // Attached outside the DTO
    private BigDecimal costPerHour;

    // Attached outside the DTO
    private String orderName;

    // Attached outside the DTO
    private String orderCode;

    private OrderElement orderElement;

    private Worker worker;

    public OrderCostsPerResourceDTO(Worker worker,
            WorkReportLine workReportLine) {

        this.workerName = worker.getName();
        if (workReportLine.getLocalDate() != null) {
            this.date = workReportLine.getLocalDate().toDateTimeAtStartOfDay()
                .toDate();
        }
        this.clockStart = workReportLine.getClockStart();
        this.clockFinish = workReportLine.getClockFinish();
        this.numHours = workReportLine.getNumHours();
        this.descriptionValues = descriptionValuesAsString(workReportLine.getDescriptionValues());
        this.labels = labelsAsString(workReportLine.getLabels());
        this.hoursType = workReportLine.getTypeOfWorkHours().getName();
        this.hoursTypeCode = workReportLine.getTypeOfWorkHours().getCode();
        this.orderElement = workReportLine.getOrderElement();
        this.orderElementCode = workReportLine.getOrderElement().getCode();
        this.orderElementName = workReportLine.getOrderElement().getName();
        this.worker = worker;
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

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
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

    public OrderElement getOrderElement() {
        return orderElement;
    }

    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
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

    public int compareTo(OrderCostsPerResourceDTO o) {
        String comparator = this.orderName + this.orderElementCode
                + this.workerName;
        int result = comparator.compareToIgnoreCase(o.orderName
                + o.getOrderElementCode() + o.workerName);
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

    public void setOrderElementName(String orderElementName) {
        this.orderElementName = orderElementName;
    }

    public String getOrderElementName() {
        return orderElementName;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getOrderCode() {
        return orderCode;
    }

}