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

import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.NotNull;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.Resource;

/**
 * @author Diego Pino García <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class Task extends BaseEntity {

    public static final String RESOURCE = "resource";

    public static final String ORDER_ELEMENT = "orderElement";

    public static Task create() {
        Task workReportLine = new Task();
        workReportLine.setNewObject(true);
        return workReportLine;
    }

    public static Task create(Integer numHours, Resource resource,
            OrderElement orderElement, Set<Criterion> criterions) {
        Task workReportLine = new Task(numHours, resource,
                orderElement, criterions);
        workReportLine.setNewObject(true);
        return workReportLine;
    }

    private Integer numHours;

    @NotNull
    private Resource resource;

    @NotNull
    private OrderElement orderElement;

    private WorkReport workReport;

    private Set<Criterion> criterions = new HashSet<Criterion>();

    /**
     * Constructor for hibernate. Do not use!
     */
    public Task() {

    }

    private Task(Integer numHours, Resource resource,
            OrderElement orderElement, Set<Criterion> criterions) {
        this.numHours = numHours;
        this.resource = resource;
        this.orderElement = orderElement;
        this.criterions = criterions;
    }

    public Integer getNumHours() {
        return numHours;
    }

    public void setNumHours(Integer numHours) {
        this.numHours = numHours;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public OrderElement getOrderElement() {
        return orderElement;
    }

    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    public Set<Criterion> getCriterions() {
        return new HashSet<Criterion>(criterions);
    }

    public void setCriterions(Set<Criterion> criterions) {
        this.criterions = criterions;
    }

    public WorkReport getWorkReport() {
        return workReport;
    }

    public void setWorkReport(WorkReport workReport) {
        this.workReport = workReport;
    }
}
