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
import java.util.Collections;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Min;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.entities.EntitySequence;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.expensesheet.daos.IExpenseSheetDAO;

/**
 * ExpenseSheet Entity
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class ExpenseSheet extends IntegrationEntity {

    private Date firstExpense;

    private Date lastExpense;

    private BigDecimal total;

    private String description;

    @Valid
    @NotEmpty(message = "the expense sheet must have least a expense sheet line.")
    @NotNull(message = "the expense sheet must have least a expense sheet line.")
    private SortedSet<ExpenseSheetLine> expenseSheetLines = new TreeSet<ExpenseSheetLine>(
            new ExpenseSheetLineComparator());

    private Integer lastExpenseSheetLineSequenceCode = 0;

    protected ExpenseSheet() {
    }

    protected ExpenseSheet(Date firstExpense, Date lastExpense, BigDecimal total) {
        this.setFirstExpense(firstExpense);
        this.setLastExpense(lastExpense);
        this.setTotal(total);
    }

    public static ExpenseSheet create() {
        ExpenseSheet expenseSheet = new ExpenseSheet();
        expenseSheet.setNewObject(true);
        return expenseSheet;
    }

    public static ExpenseSheet create(Date firstExpense, Date lastExpense, BigDecimal total) {
        ExpenseSheet expenseSheet = new ExpenseSheet(firstExpense, lastExpense, total);
        expenseSheet.setNewObject(true);
        return expenseSheet;
    }

    @Override
    protected IExpenseSheetDAO getIntegrationEntityDAO() {
        return Registry.getExpenseSheetDAO();
    }

    protected void setFirstExpense(Date firstExpense) {
        this.firstExpense = firstExpense;
    }

    public Date getFirstExpense() {
        return firstExpense;
    }

    protected void setLastExpense(Date lastExpense) {
        this.lastExpense = lastExpense;
    }

    public Date getLastExpense() {
        return lastExpense;
    }

    protected void setTotal(BigDecimal total) {
        this.total = total;
    }

    @Min(message = "length less than 0", value = 0)
    @NotNull(message = "total not specified")
    public BigDecimal getTotal() {
        return total;
    }

    public void setExpenseSheetLines(SortedSet<ExpenseSheetLine> expenseSheetLines) {
        this.expenseSheetLines = expenseSheetLines;
    }

    public SortedSet<ExpenseSheetLine> getExpenseSheetLines() {
        return Collections.unmodifiableSortedSet(expenseSheetLines);
    }

    public void setLastExpenseSheetLineSequenceCode(Integer lastExpenseSheetLineSequenceCode) {
        this.lastExpenseSheetLineSequenceCode = lastExpenseSheetLineSequenceCode;
    }

    @NotNull(message = "last expense sheet line sequence code not specified")
    public Integer getLastExpenseSheetLineSequenceCode() {
        return lastExpenseSheetLineSequenceCode;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @AssertTrue(message = "The expense sheet line codes must be unique.")
    public boolean checkConstraintNonRepeatedExpenseSheetLinesCodes() {
        return getFirstRepeatedCode(this.expenseSheetLines) == null;
    }

    @AssertTrue(message = "The expense sheet line collection cannot be empty or null.")
    public boolean checkConstraintNotEmptyExpenseSheetLines() {
        return ((getExpenseSheetLines() != null) && (!getExpenseSheetLines().isEmpty()));
    }

    public void generateExpenseSheetLineCodes(int numberOfDigits) {
        for (ExpenseSheetLine line : this.getExpenseSheetLines()) {
            if ((line.getCode() == null) || (line.getCode().isEmpty())
                    || (!line.getCode().startsWith(this.getCode()))) {
                this.incrementLastExpenseSheetLineSequenceCode();
                String lineCode = EntitySequence.formatValue(numberOfDigits,
                        this.getLastExpenseSheetLineSequenceCode());
                line.setCode(this.getCode() + EntitySequence.CODE_SEPARATOR_CHILDREN + lineCode);
            }
        }
    }

    public void incrementLastExpenseSheetLineSequenceCode() {
        if (lastExpenseSheetLineSequenceCode == null) {
            lastExpenseSheetLineSequenceCode = 0;
        }
        lastExpenseSheetLineSequenceCode++;
    }

    public void updateCalculatedProperties() {
        updateFistAndLastExpenseDate();
        updateTotal();
    }

    public void updateFistAndLastExpenseDate() {
        if (this.getExpenseSheetLines() != null && !this.getExpenseSheetLines().isEmpty()) {
            ExpenseSheetLine firstLine = this.getExpenseSheetLines().first();
            setLastExpense(firstLine.getDate());

            ExpenseSheetLine lastLine = this.getExpenseSheetLines().last();
            setFirstExpense(lastLine.getDate());
        } else {
            setLastExpense(null);
            setFirstExpense(null);
        }
    }

    public void updateTotal() throws ValidationException {
        this.setTotal(new BigDecimal(0));
        for (ExpenseSheetLine line : this.expenseSheetLines) {
            if (line.getValue() != null) {
                this.setTotal(this.getTotal().add(line.getValue()));
            }
        }
    }

    public void add(ExpenseSheetLine line) {
        this.expenseSheetLines.add(line);
        this.updateCalculatedProperties();
    }

    public void remove(ExpenseSheetLine line) {
        this.expenseSheetLines.remove(line);
        this.updateCalculatedProperties();
    }

}