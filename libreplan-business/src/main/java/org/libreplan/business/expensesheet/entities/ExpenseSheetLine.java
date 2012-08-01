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

package org.libreplan.business.expensesheet.entities;

import java.math.BigDecimal;

import org.hibernate.validator.Min;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.daos.IIntegrationEntityDAO;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.resources.entities.Resource;

/**
 * ExpenseSheetLine Entity
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class ExpenseSheetLine extends IntegrationEntity {

    private BigDecimal value;

    private String concept;

    private LocalDate date;

    private OrderElement orderElement;

    private Resource resource;

    private ExpenseSheet expenseSheet;

    /**
     * Constructor for Hibernate. Do not use!
     */
    protected ExpenseSheetLine() {

    }

    private ExpenseSheetLine(BigDecimal value, String concept, LocalDate date,
            OrderElement orderElement) {
        this.orderElement = orderElement;
        this.concept = concept;
        this.value = value;
        this.setDate(date);
    }

    public static ExpenseSheetLine create(BigDecimal value, String concept, LocalDate date,
            OrderElement orderElement) {
        return create(new ExpenseSheetLine(value, concept, date, orderElement));
    }

    public void setValue(BigDecimal value) {
        boolean different = isDifferent(value);

        this.value = value;

        if (this.expenseSheet != null && different) {
            this.expenseSheet.updateTotal();
        }
    }

    private boolean isDifferent(BigDecimal value) {
        if (this.value == null && value == null) {
            return false;
        }
        if (this.value != null && value != null) {
            return (this.value.compareTo(value) != 0);
        }
        return true;
    }

    @Min(message = "value must be greater or equal than 0", value = 0)
    @NotNull(message = "value not specified")
    public BigDecimal getValue() {
        return value;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getConcept() {
        return concept;
    }

    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    @NotNull(message = "task not specified")
    public OrderElement getOrderElement() {
        return orderElement;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    @Override
    protected IIntegrationEntityDAO<? extends IntegrationEntity> getIntegrationEntityDAO() {
        return Registry.getExpenseSheetLineDAO();
    }

    public void setDate(LocalDate date) {
        boolean different = isDifferent(date);
        this.date = date;
        if (this.expenseSheet != null && different) {
            this.expenseSheet.updateFistAndLastExpenseDate();
        }
    }

    private boolean isDifferent(LocalDate date) {
        if (this.date == null && date == null) {
            return false;
        }
        if (this.date != null && date != null) {
            return (this.date.compareTo(date) != 0);
        }
        return true;
    }

    @NotNull(message = "date not specified")
    public LocalDate getDate() {
        return date;
    }

    public void setExpenseSheet(ExpenseSheet expenseSheet) {
        this.expenseSheet = expenseSheet;
    }

    @NotNull(message = "expense sheet not specified")
    public ExpenseSheet getExpenseSheet() {
        return expenseSheet;
    }

}
