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

package org.libreplan.web.users.dashboard;

import static org.libreplan.web.I18nHelper._;

import java.util.List;

import javax.annotation.Resource;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.expensesheet.entities.ExpenseSheet;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.libreplan.web.expensesheet.IExpenseSheetCRUDController;
import org.libreplan.web.expensesheet.IExpenseSheetModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

/**
 * Controller for "Expenses" area in the user dashboard window
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@SuppressWarnings("serial")
public class ExpensesAreaController extends GenericForwardComposer {

    @Resource
    private IExpenseSheetCRUDController expenseSheetCRUDController;

    private IExpensesAreaModel expensesAreaModel;

    private IExpenseSheetModel expenseSheetModel;

    private IMessagesForUser messagesForUser;

    private Grid expenseSheetsList;

    private RowRenderer expenseSheetsRenderer = new RowRenderer() {

        @Override
        public void render(Row row, Object data) throws Exception {
            final ExpenseSheet expenseSheet = (ExpenseSheet) data;
            row.setValue(expenseSheet);

            Util.appendLabel(row, expenseSheet.getDescription());
            Util.appendLabel(row, expenseSheet.getCode());
            Util.appendLabel(row,
                    Util.addCurrencySymbol(expenseSheet.getTotal()));
            Util.appendLabel(row, expenseSheet.getFirstExpense().toString());
            Util.appendLabel(row, expenseSheet.getLastExpense().toString());

            Util.appendOperationsAndOnClickEvent(row, new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    expenseSheetCRUDController
                            .goToEditPersonalExpenseSheet(expenseSheet);
                }
            }, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    try {
                        if (Messagebox
                                .show(_("Delete expense sheet \"{0}\". Are you sure?",
                                        expenseSheet.getHumanId()),
                                        _("Confirm"), Messagebox.OK
                                                | Messagebox.CANCEL,
                                        Messagebox.QUESTION) == Messagebox.OK) {
                            expenseSheetModel.removeExpenseSheet(expenseSheet);
                            messagesForUser.showMessage(
                                    Level.INFO,
                                    _("Expense sheet \"{0}\" deleted",
                                            expenseSheet.getHumanId()));
                            Util.reloadBindings(expenseSheetsList);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (InstanceNotFoundException ie) {
                        messagesForUser
                                .showMessage(
                                        Level.ERROR,
                                        _("Expense sheet \"{1}\" could not be deleted, it was already removed",
                                                expenseSheet.getHumanId()));
                    }
                }
            });
        }
    };

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("controller", this);

        messagesForUser = new MessagesForUser(
                comp.getFellow("messagesContainer"));
    }

    public void newExpenseSheet() {
        expenseSheetCRUDController.goToCreatePersonalExpenseSheet();
    }

    public List<ExpenseSheet> getPersonalExpenseSheets() {
        return expensesAreaModel.getPersonalExpenseSheets();
    }

    public RowRenderer getExpenseSheetsRenderer() {
        return expenseSheetsRenderer;
    }

}
