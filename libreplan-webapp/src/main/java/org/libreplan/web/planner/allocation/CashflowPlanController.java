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

package org.libreplan.web.planner.allocation;

import static org.libreplan.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.joda.time.LocalDate;
import org.libreplan.business.cashflow.entities.CashflowOutput;
import org.libreplan.business.cashflow.entities.CashflowPlan;
import org.libreplan.business.cashflow.entities.CashflowType;
import org.libreplan.business.expensesheet.entities.ExpenseSheetLine;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.web.common.ConstraintChecker;
import org.libreplan.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.api.Combobox;
import org.zkoss.zul.api.Datebox;
import org.zkoss.zul.api.Decimalbox;
import org.zkoss.zul.api.Groupbox;
import org.zkoss.zul.api.Intbox;
import org.zkoss.zul.api.Window;

/**
 * Controller for {@link CashflowPlan}.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@SuppressWarnings("serial")
public class CashflowPlanController extends GenericForwardComposer {

    private Window window;

    private Datebox dateDatebox;

    private Decimalbox amountDecimalbox;

    private Groupbox cashflowGroupbox;

    private RowRenderer cashflowOutputRenderer;

    private ICashflowPlanModel cashflowPlanModel;

    private RowRenderer expenseSheetLineRenderer;

    private ComboitemRenderer cashflowTypeRenderer;

    private Hbox deferredPaymentOptionHbox;

    private Hbox outputOptionHbox;

    private Intbox daysIntbox;

    private Combobox typeCombobox;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        window = (Window) comp;
    }

    public void setCashflowPlan(CashflowPlan cashflowPlan) {
        cashflowPlanModel.setCashflowPlan(cashflowPlan);
        updateOpionsVisibility();
        Util.reloadBindings(window);
    }

    private void updateOpionsVisibility() {
        CashflowPlan cashflowPlan = getCashflowPlan();
        if (cashflowPlan != null) {
            boolean manual = cashflowPlan.isManual();
            outputOptionHbox.setVisible(manual);
            deferredPaymentOptionHbox.setVisible(!manual);
        }
    }

    public String getWindowTitle() {
        Task task = cashflowPlanModel.getTask();
        if (task == null) {
            return "";
        }
        return _("Cashflow plan for task: {0}", task.getName());
    }

    public List<CashflowType> getCashflowTypes() {
        return Arrays.asList(CashflowType.values());
    }

    public CashflowType getSelectedCashflowType() {
        CashflowPlan cashflowPlan = getCashflowPlan();
        if (cashflowPlan == null) {
            return CashflowType.MANUAL;
        }
        return cashflowPlan.getType();
    }

    public void setSelectedCashflowType(CashflowType cashflowType) {
        if (cashflowType == null) {
            throw new WrongValueException(typeCombobox, _("cannot be empty"));
        }
        getCashflowPlan().setType(cashflowType);
        updateOpionsVisibility();
        Util.reloadBindings(cashflowGroupbox);
    }

    public void showWindow() {
        try {
            window.doModal();
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void back() {
        ConstraintChecker.isValid(window);
        window.setVisible(false);
    }

    public List<CashflowOutput> getCashflowOutputs() {
        List<CashflowOutput> outputs = new ArrayList<CashflowOutput>(
                cashflowPlanModel.getOutputs());
        Collections.sort(outputs, new Comparator<CashflowOutput>() {
            @Override
            public int compare(CashflowOutput o1, CashflowOutput o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        return outputs;
    }

    public RowRenderer getCashflowOutputRenderer() {
        if (cashflowOutputRenderer == null) {
            cashflowOutputRenderer = new RowRenderer() {

                @Override
                public void render(Row row, Object data) throws Exception {
                    final CashflowOutput output = (CashflowOutput) data;
                    row.setValue(output);

                    Util.appendLabel(row, output.getDate().toString());
                    Util.appendLabel(row,
                            Util.addCurrencySymbol(output.getAmount()));
                    row.appendChild(Util
                            .createRemoveButton(new EventListener() {
                                @Override
                                public void onEvent(Event event)
                                        throws Exception {
                                    getCashflowPlan().removeOutput(
                                            output.getDate(),
                                            output.getAmount());
                                    Util.reloadBindings(cashflowGroupbox);
                                }
                            }));
                }
            };
        }
        return cashflowOutputRenderer;
    }

    public void addOutput() {
        LocalDate date = LocalDate.fromDateFields(dateDatebox.getValue());
        if (date == null) {
            throw new WrongValueException(dateDatebox, _("cannot be empty"));
        }
        BigDecimal amount = amountDecimalbox.getValue();
        if (amount == null) {
            throw new WrongValueException(amountDecimalbox,
                    _("cannot be empty"));
        }

        getCashflowPlan().addOutput(date, amount);

        resetOutputInputs();

        Util.reloadBindings(cashflowGroupbox);
    }

    private void resetOutputInputs() {
        dateDatebox.setValue(null);
        amountDecimalbox.setValue(null);
    }

    public BigDecimal getTotal() {
        return cashflowPlanModel.getTotalOutputs();
    }

    public String getMoneyFormat() {
        return Util.getMoneyFormat();
    }

    private CashflowPlan getCashflowPlan() {
        return cashflowPlanModel.getCashflowPlan();
    }

    public List<ExpenseSheetLine> getExpenseSheetLines() {
        List<ExpenseSheetLine> lines = cashflowPlanModel.getExpenseSheetLines();
        Collections.sort(lines, new Comparator<ExpenseSheetLine>() {
            @Override
            public int compare(ExpenseSheetLine o1, ExpenseSheetLine o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        return lines;
    }

    public BigDecimal getTotalExpenses() {
        return cashflowPlanModel.getTotalExpenses();
    }

    public RowRenderer getExpenseSheetLineRenderer() {
        if (expenseSheetLineRenderer == null) {
            expenseSheetLineRenderer = new RowRenderer() {

                @Override
                public void render(Row row, Object data) throws Exception {
                    ExpenseSheetLine line = (ExpenseSheetLine) data;
                    row.setValue(line);

                    Util.appendLabel(row, line.getDate().toString());
                    Util.appendLabel(row,
                            Util.addCurrencySymbol(line.getValue()));
                    Util.appendLabel(row, line.getConcept());
                }
            };
        }
        return expenseSheetLineRenderer;
    }

    public ComboitemRenderer getCashflowTypeRenderer() {
        if (cashflowTypeRenderer == null) {
            cashflowTypeRenderer = new ComboitemRenderer() {

                @Override
                public void render(Comboitem item, Object data) throws Exception {
                    CashflowType type = (CashflowType) data;
                    item.setValue(type);

                    item.setLabel(_(type.getName()));
                    item.setDescription(_(type.getDescription()));
                }
            };
        }
        return cashflowTypeRenderer;
    }

    public Integer getDays() {
        CashflowPlan cashflowPlan = getCashflowPlan();
        if (cashflowPlan == null) {
            return 0;
        }
        return cashflowPlan.getDelayDays();
    }

    public void setDays(Integer days) {
        if (days == null || days < 0) {
            throw new WrongValueException(daysIntbox,
                    _("cannot be empty or negative"));
        }
        getCashflowPlan().setDelayDays(days);
        Util.reloadBindings(cashflowGroupbox);
    }

}
