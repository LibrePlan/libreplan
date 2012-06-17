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
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Min;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.joda.time.LocalDate;
import org.libreplan.business.common.IHumanIdentifiable;
import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.entities.EntitySequence;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.expensesheet.daos.IExpenseSheetDAO;
import org.libreplan.business.resources.entities.Resource;

/**
 * ExpenseSheet Entity
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class ExpenseSheet extends IntegrationEntity implements IHumanIdentifiable {

    private LocalDate firstExpense;

    private LocalDate lastExpense;

    private BigDecimal total;

    private String description;

    private boolean personal = false;

    @Valid
    private SortedSet<ExpenseSheetLine> expenseSheetLines = new TreeSet<ExpenseSheetLine>(
            new ExpenseSheetLineComparator());

    private Integer lastExpenseSheetLineSequenceCode = 0;

    /**
     * Constructor for Hibernate. Do not use!
     */
    protected ExpenseSheet() {
    }

    private ExpenseSheet(LocalDate firstExpense, LocalDate lastExpense, BigDecimal total) {
        this.setFirstExpense(firstExpense);
        this.setLastExpense(lastExpense);
        this.setTotal(total);
    }

    public static ExpenseSheet create() {
        return create(new ExpenseSheet());
    }

    public static ExpenseSheet create(LocalDate firstExpense, LocalDate lastExpense,
            BigDecimal total) {
        return create(new ExpenseSheet(firstExpense, lastExpense, total));
    }

    @Override
    protected IExpenseSheetDAO getIntegrationEntityDAO() {
        return Registry.getExpenseSheetDAO();
    }

    protected void setFirstExpense(LocalDate firstExpense) {
        this.firstExpense = firstExpense;
    }

    public LocalDate getFirstExpense() {
        return firstExpense;
    }

    protected void setLastExpense(LocalDate lastExpense) {
        this.lastExpense = lastExpense;
    }

    public LocalDate getLastExpense() {
        return lastExpense;
    }

    protected void setTotal(BigDecimal total) {
        this.total = total;
    }

    @Min(message = "total must be greater or equal than 0", value = 0)
    @NotNull(message = "total not specified")
    public BigDecimal getTotal() {
        return total;
    }

    @NotEmpty(message = "the expense sheet must have least a expense sheet line.")
    @NotNull(message = "the expense sheet must have least a expense sheet line.")
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

    public void keepSortedExpenseSheetLines(ExpenseSheetLine expenseSheetLine,
            LocalDate newDate) {
        this.expenseSheetLines.remove(expenseSheetLine);
        expenseSheetLine.setDate(newDate);
        this.expenseSheetLines.add(expenseSheetLine);
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

    public void updateTotal() {
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

    @Override
    public String getHumanId() {
        return getCode() + (description != null ? description : "");
    }

    public ExpenseSheetLine getExpenseSheetLineByCode(String code)
            throws ValidationException {

        if (StringUtils.isBlank(code)) {
            throw new ValidationException(
                    "missing the code with which find the expense sheet line");
        }

        for (ExpenseSheetLine l : this.expenseSheetLines) {
            if (l.getCode().equalsIgnoreCase(StringUtils.trim(code))) {
                return l;
            }
        }
        return null;
    }

    public boolean isPersonal() {
        return personal;
    }

    public void setPersonal(boolean personal) {
        this.personal = personal;
    }

    public boolean isNotPersonal() {
        return !personal;
    }

    @AssertTrue(message = "a personal expense sheet must have the same resource in all the lines")
    public boolean checkConstraintPersonalExpenseSheetMustHaveTheSameResourceInAllLines() {
        if (!personal) {
            return true;
        }

        Resource resource = expenseSheetLines.iterator().next().getResource();

        for (ExpenseSheetLine line : expenseSheetLines) {
            Resource resourceLine = line.getResource();
            if ((resourceLine == null)
                    || (!resourceLine.getId().equals(resource.getId()))) {
                return false;
            }
        }

        return true;
    }

}