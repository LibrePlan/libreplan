/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.business.cashflow.entities;

import java.math.BigDecimal;

import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;

/**
 * This class is intended as a Hibernate component. Represents a cashflow output
 * that is identified by both a date and an amount.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class CashflowOutput {

    private LocalDate date;

    private BigDecimal amount;

    /**
     * Default constructor for Hibernate. Do not use!
     */
    protected CashflowOutput() {
    }

    public CashflowOutput(LocalDate date, BigDecimal amount) {
        this.date = date;
        this.amount = amount;
    }

    @NotNull(message = "date not specified")
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @NotNull(message = "amount not specified")
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

}