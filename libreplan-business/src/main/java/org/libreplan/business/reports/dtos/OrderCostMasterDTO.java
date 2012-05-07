/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 WirelessGalicia, S.L.
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

import org.libreplan.business.orders.entities.OrderElement;


/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class OrderCostMasterDTO implements Comparable<OrderCostMasterDTO> {

    private String orderElementName;

    private String orderElementCode;

    // Attached outside the DTO
    private String orderName;

    // Attached outside the DTO
    private String orderCode;

    private OrderElement orderElement;

    /*
     * type net.sf.jasperreports.engine.JRDataSource;
     */
    private Object listExpensesDTO;

    /*
     * type net.sf.jasperreports.engine.JRDataSource;
     */
    private Object listWorkReportLineDTO;

    public OrderCostMasterDTO(OrderElement orderElement, Object dsWRL, Object dsES) {
        this.orderElement = orderElement;
        this.orderElementCode = orderElement.getCode();
        this.orderElementName = orderElement.getName();
        this.listExpensesDTO = dsES;
        this.listWorkReportLineDTO = dsWRL;
    }

    public String getOrderElementCode() {
        return orderElementCode;
    }

    public void setOrderElementCode(String orderElementCode) {
        this.orderElementCode = orderElementCode;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public OrderElement getOrderElement() {
        return orderElement;
    }

    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
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

    public void setListExpensesDTO(Object listExpensesDTO) {
        this.listExpensesDTO = listExpensesDTO;
    }

    public Object getListExpensesDTO() {
        return listExpensesDTO;
    }

    @Override
    public int compareTo(OrderCostMasterDTO o) {
        String comparator = this.orderElementName;
        int result = comparator.compareToIgnoreCase(o.getOrderElementName());
        if (result == 0) {
            comparator = this.orderElementCode;
            result = comparator.compareToIgnoreCase(o.getOrderElementCode());
        }
        return result;
    }

    public void setListWorkReportLineDTO(Object listWorkReportLineDTO) {
        this.listWorkReportLineDTO = listWorkReportLineDTO;
    }

    public Object getListWorkReportLineDTO() {
        return listWorkReportLineDTO;
    }

}