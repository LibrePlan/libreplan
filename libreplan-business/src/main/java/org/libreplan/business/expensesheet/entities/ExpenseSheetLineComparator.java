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

import java.util.Comparator;

/**
 * ExpenseSheetLine Comparator
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class ExpenseSheetLineComparator implements Comparator<ExpenseSheetLine> {

    @Override
    public int compare(ExpenseSheetLine arg0, ExpenseSheetLine arg1) {
        if ((arg0.getDate() == null) && (arg1.getDate() == null)) {
            return compareCode(arg0, arg1);
        }
        if (arg0.getDate() == null) {
            return -1;
        }
        if (arg1.getDate() == null) {
            return 1;
        }
        if (arg1.getDate().compareTo(arg0.getDate()) == 0) {
            return compareCode(arg0, arg1);
        }
        return arg1.getDate().compareTo(arg0.getDate());
    }

    private int compareCode(ExpenseSheetLine arg0, ExpenseSheetLine arg1) {
        if ((arg0.getCode() == null) && (arg1.getCode() == null)) {
            return 0;
        }
        if (arg0.getDate() == null) {
            return -1;
        }
        if (arg1.getDate() == null) {
            return 1;
        }
        return arg1.getCode().compareTo(arg0.getCode());
    }
}
