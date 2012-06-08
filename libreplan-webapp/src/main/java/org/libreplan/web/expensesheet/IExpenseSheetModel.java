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

package org.libreplan.web.expensesheet;

import java.util.List;
import java.util.SortedSet;

import org.joda.time.LocalDate;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.expensesheet.entities.ExpenseSheet;
import org.libreplan.business.expensesheet.entities.ExpenseSheetLine;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.common.IIntegrationEntityModel;

/**
 * Contract for {@link ExpenseSheet}
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface IExpenseSheetModel extends IIntegrationEntityModel {

    void confirmSave();

    void prepareToList();

    void initCreate(boolean personal);

    void prepareToEdit(ExpenseSheet expenseSheet);

    List<ExpenseSheet> getExpenseSheets();

    void removeExpenseSheet(ExpenseSheet expenseSheet)
            throws InstanceNotFoundException;

    ExpenseSheet getExpenseSheet();

    SortedSet<ExpenseSheetLine> getExpenseSheetLines();

    void removeExpenseSheetLine(ExpenseSheetLine expenseSheetLine);

    void addExpenseSheetLine();

    ExpenseSheetLine getNewExpenseSheetLine();

    List<Order> getOrders();

    List<OrderElement> getTasks();

    void setSelectedProject(Order project);

    Order getSelectedProject();

    void generateExpenseSheetLineCodesIfIsNecessary();

    /**
     * Returns the {@link Resource} associated to the <b>personal</b>
     * {@link ExpenseSheet}.<br />
     *
     * In <b>personal</b> {@link ExpenseSheet ExpenseSheets} all
     * {@link ExpenseSheetLine} has the same {@link Resource}.<br />
     *
     * It tries to get the {@link Resource} from the first {@link ExpenseSheet}
     * and if not it tries to get it from bound {@link User}.
     */
    Resource getResource();

    void keepSortedExpenseSheetLines(ExpenseSheetLine expenseSheetLine, LocalDate newDate);

}
