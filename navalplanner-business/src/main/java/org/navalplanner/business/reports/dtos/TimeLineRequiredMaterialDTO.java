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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.navalplanner.business.materials.entities.MaterialAssignment;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.entities.TaskElement;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class TimeLineRequiredMaterialDTO{

    private String date;

    private Date initDate;

    private String status;

    private String code;

    private String category;

    private String name;

    private BigDecimal units = BigDecimal.ZERO;

    private BigDecimal unitPrice = BigDecimal.ZERO;

    private BigDecimal totalPrice = BigDecimal.ZERO;

    private Date estimatedAvailability;

    private String order;

    private String task;

    public TimeLineRequiredMaterialDTO(Date date) {
        setDate(date);
    }

    public TimeLineRequiredMaterialDTO(MaterialAssignment materialAssignment,
            TaskElement taskElement, Date requiredDate, OrderElement order) {
        setDate(requiredDate);
        this.initDate = requiredDate;
        this.status = materialAssignment.getStatus().name();
        this.code = materialAssignment.getMaterial().getCode();
        this.category = materialAssignment.getMaterial().getCategory().getName();
        this.name = materialAssignment.getMaterial().getDescription();
        this.units = materialAssignment.getUnits();
        this.unitPrice = materialAssignment.getUnitPrice();
        this.totalPrice = materialAssignment.getTotalPrice();
        this.estimatedAvailability = materialAssignment
                .getEstimatedAvailability();
        this.order = order.getCode() + " - " + order.getName();
        this.task = getTaskName(taskElement);
    }

    public String getTaskName(TaskElement taskElement) {
        if (taskElement != null) {
            String result = taskElement.getName();
            if (result != null && (!name.isEmpty())) {
                result = taskElement.getOrderElement().getName();
            }
            return result;
        }
        return null;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private void setDate(Date newDate) {
        if (newDate != null) {
            this.date = (new SimpleDateFormat("dd/MM/yyyy")).format(newDate);
        } else {
            this.date = null;
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getUnits() {
        return units;
    }

    public void setUnits(BigDecimal units) {
        this.units = units;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Date getEstimatedAvailability() {
        return estimatedAvailability;
    }

    public void setEstimatedAvailability(Date estimatedAvailability) {
        this.estimatedAvailability = estimatedAvailability;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

}
