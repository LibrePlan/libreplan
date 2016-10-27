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

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.expensesheet.entities.ExpenseSheet;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.libreplan.web.expensesheet.IExpenseSheetCRUDController;
import org.libreplan.web.expensesheet.IExpenseSheetModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import java.util.List;

import static org.libreplan.web.I18nHelper._;

/**
 * Controller for "Expenses" area in the user dashboard window.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
@SuppressWarnings("serial")
public class ExpensesAreaController extends GenericForwardComposer {

    private IExpenseSheetCRUDController expenseSheetCRUDController;

    private IExpensesAreaModel expensesAreaModel;

    private IExpenseSheetModel expenseSheetModel;

    private IMessagesForUser messagesForUser;

    private Grid expenseSheetsList;

    private RowRenderer expenseSheetsRenderer = new RowRenderer() {

        @Override
        public void render(Row row, Object data, int i) throws Exception {
            final ExpenseSheet expenseSheet = (ExpenseSheet) data;
            row.setValue(expenseSheet);

            Util.appendLabel(row, expenseSheet.getDescription());
            Util.appendLabel(row, expenseSheet.getCode());
            Util.appendLabel(row, Util.addCurrencySymbol(expenseSheet.getTotal()));
            Util.appendLabel(row, expenseSheet.getFirstExpense().toString());
            Util.appendLabel(row, expenseSheet.getLastExpense().toString());

            Util.appendOperationsAndOnClickEvent(
                    row,
                    event -> expenseSheetCRUDController.goToEditPersonalExpenseSheet(expenseSheet),
                    event ->  {
                        try {
                            if (Messagebox.show(
                                    _("Delete expense sheet \"{0}\". Are you sure?", expenseSheet.getHumanId()),
                                    _("Confirm"),
                                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION) == Messagebox.OK) {

                                expenseSheetModel.removeExpenseSheet(expenseSheet);

                                messagesForUser.showMessage(
                                        Level.INFO, _("Expense sheet \"{0}\" deleted", expenseSheet.getHumanId()));

                                Util.reloadBindings(expenseSheetsList);
                            }
                        } catch (InstanceNotFoundException e) {
                            messagesForUser.showMessage(
                                    Level.ERROR,
                                    _("Expense sheet \"{1}\" could not be deleted, it was already removed",
                                            expenseSheet.getHumanId()));
                        }
                    });
        }
    };

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("controller", this);

        messagesForUser = new MessagesForUser(comp.getFellow("messagesContainer"));

        injectObjects();
    }

    private void injectObjects() {
        if ( expenseSheetCRUDController == null ) {
            expenseSheetCRUDController = (IExpenseSheetCRUDController) SpringUtil.getBean("expenseSheetCRUDController");
        }

        if ( expensesAreaModel == null ) {
            expensesAreaModel = (IExpensesAreaModel) SpringUtil.getBean("expensesAreaModel");
        }

        if ( expenseSheetModel == null ) {
            expenseSheetModel = (IExpenseSheetModel) SpringUtil.getBean("expenseSheetModel");
        }
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
