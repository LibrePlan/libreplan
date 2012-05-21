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

package org.libreplan.business.orders.entities;

import java.math.BigDecimal;

import org.libreplan.business.common.BaseEntity;

/**
 * It represents the sum of expenses to an {@link OrderElement}, avoiding the
 * need to iterate among the expense sheet lines to get this information.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class SumExpenses extends BaseEntity {

    private OrderElement orderElement;

    private BigDecimal totalDirectExpenses = BigDecimal.ZERO;

    private BigDecimal totalIndirectExpenses = BigDecimal.ZERO;

    protected SumExpenses() {
    }

    private SumExpenses(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    public static SumExpenses create(OrderElement orderElement) {
        return create(new SumExpenses(orderElement));
    }

    public OrderElement getOrderElement() {
        return orderElement;
    }

    public void reset() {
        resetDirectExpenses();
        resetIndirectExpenses();
    }

    private void resetDirectExpenses() {
        this.setTotalDirectExpenses(BigDecimal.ZERO);
    }

    private void resetIndirectExpenses() {
        this.setTotalIndirectExpenses(BigDecimal.ZERO);
    }

    public void setTotalDirectExpenses(BigDecimal totalDirectExpenses) {
        this.totalDirectExpenses = totalDirectExpenses;
    }

    public BigDecimal getTotalDirectExpenses() {
        return totalDirectExpenses;
    }

    public void setTotalIndirectExpenses(BigDecimal totalIndirectExpenses) {
        this.totalIndirectExpenses = totalIndirectExpenses;
    }

    public BigDecimal getTotalIndirectExpenses() {
        return totalIndirectExpenses;
    }

    public void addDirectExpenses(BigDecimal directExpenses) {
        this.totalDirectExpenses = this.totalDirectExpenses.add(directExpenses);
    }

    public void subtractDirectExpenses(BigDecimal directExpenses) {
        this.totalDirectExpenses = this.totalDirectExpenses.subtract(directExpenses);
        if (this.totalDirectExpenses.compareTo(BigDecimal.ZERO) < 0) {
            this.resetDirectExpenses();
        }
    }

    public void addIndirectExpenses(BigDecimal indirectExpenses) {
        this.totalIndirectExpenses = this.totalIndirectExpenses.add(indirectExpenses);
    }

    public void subtractIndirectExpenses(BigDecimal indirectExpenses) {
        this.totalIndirectExpenses = this.totalIndirectExpenses.subtract(indirectExpenses);
        if (this.totalIndirectExpenses.compareTo(BigDecimal.ZERO) < 0) {
            this.resetIndirectExpenses();
        }
    }

    public boolean isTotalDirectExpensesZero() {
        return isZero(this.totalDirectExpenses);
    }

    public boolean isTotalIndirectExpensesZero() {
        return isZero(this.totalIndirectExpenses);
    }

    public boolean isZero() {
        return (isZero(this.totalIndirectExpenses) && isZero(this.totalDirectExpenses));
    }

    private boolean isZero(BigDecimal value) {
        return ((value == null) || (value.compareTo(BigDecimal.ZERO) == 0));
    }
}