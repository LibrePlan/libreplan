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

package org.libreplan.business.planner.entities;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.libreplan.business.expensesheet.entities.ExpenseSheetLine;

/**
 * Computes aggregate values on a set{@link ExpenseSheetLine}.
 * <p>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class AggregateOfExpensesLines {

    public static AggregateOfExpensesLines createFromAll(
            Collection<ExpenseSheetLine> lines, BigDecimal totalByTask) {
        return new AggregateOfExpensesLines(lines, totalByTask);
    }

    private BigDecimal totalByTask;

    private Set<ExpenseSheetLine> expensesLines;

    private SortedMap<LocalDate, BigDecimal> mapExpenses = new TreeMap<LocalDate, BigDecimal>();

    private AggregateOfExpensesLines(
            Collection<ExpenseSheetLine> expensesLines, BigDecimal totalByTask) {
        Validate.notNull(expensesLines);
        Validate.noNullElements(expensesLines);
        this.totalByTask = totalByTask;
        this.expensesLines = new HashSet<ExpenseSheetLine>(expensesLines);
        createExpensesMap();
    }

    private void createExpensesMap() {
        mapExpenses = new TreeMap<LocalDate, BigDecimal>();
        for (ExpenseSheetLine line : expensesLines) {
            if (mapExpenses.get(line.getDate()) == null) {
                mapExpenses.put(line.getDate(), line.getValue());
            } else {
                BigDecimal sum = mapExpenses.get(line.getDate()).add(
                        line.getValue());
                mapExpenses.put(line.getDate(), sum);
            }
        }
    }

    public BigDecimal getTotalByTask() {
        if (this.totalByTask == null) {
            return BigDecimal.ZERO;
        }
        return totalByTask;
    }

    public boolean isEmpty() {
        return expensesLines.isEmpty();
    }

    public BigDecimal expensesBetween(LocalDate start, LocalDate end) {
        BigDecimal sum = BigDecimal.ZERO;

        while (start.compareTo(end) < 0) {
            if (mapExpenses.get(start) != null) {
                sum = sum.add(mapExpenses.get(start));
            }
            start = start.plusDays(1);
        }
        return sum;
    }

}
