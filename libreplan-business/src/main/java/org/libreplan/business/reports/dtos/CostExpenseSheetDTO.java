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

import org.libreplan.business.expensesheet.entities.ExpenseSheetLine;
import org.libreplan.business.orders.entities.Order;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CostExpenseSheetDTO extends ReportPerOrderElementDTO implements
        Comparable<CostExpenseSheetDTO> {

    private Date date;
    private String concept;
    private String resource;
    private BigDecimal value;
    private String orderElementCode;
    private Order order;

    public CostExpenseSheetDTO(ExpenseSheetLine bean) {
        super(bean.getOrderElement());
        this.date = bean.getDate().toDateTimeAtStartOfDay().toDate();
        this.concept = bean.getConcept();
        if (bean.getResource() != null) {
            this.resource = bean.getResource().getShortDescription();
        }
        this.value = bean.getValue();
        this.setOrderElementCode(bean.getOrderElement().getCode());
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getConcept() {
        return concept;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public int compareTo(CostExpenseSheetDTO o) {
        if (date == null) {
            return -1;
        }
        if (o.getDate() == null) {
            return 1;
        }
        return date.compareTo(o.getDate());
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrderElementCode(String orderElementCode) {
        this.orderElementCode = orderElementCode;
    }

    public String getOrderElementCode() {
        return orderElementCode;
    }
}