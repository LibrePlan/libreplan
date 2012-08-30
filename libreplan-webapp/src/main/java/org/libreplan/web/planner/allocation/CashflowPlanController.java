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
import org.libreplan.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.api.Datebox;
import org.zkoss.zul.api.Decimalbox;
import org.zkoss.zul.api.Groupbox;
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

    private CashflowPlan cashflowPlan;

    private String taskName;

    private RowRenderer cashflowOutputRenderer;

    public CashflowPlanController(CashflowPlan cashflowPlan, String taskName) {
        this.cashflowPlan = cashflowPlan;
        this.taskName = taskName;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        window = (Window) comp;
    }

    public String getWindowTitle() {
        return _("Cashflow plan for task: {0}", taskName);
    }

    public List<CashflowType> getCashflowTypes() {
        return Arrays.asList(CashflowType.values());
    }

    public CashflowType getSelectedCashflowType() {
        return cashflowPlan.getType();
    }

    public void setSelectedCashflowType(CashflowType cashflowType) {
        cashflowPlan.setType(cashflowType);
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
        window.setVisible(false);
    }

    public List<CashflowOutput> getCashflowOutputs() {
        List<CashflowOutput> outputs = new ArrayList<CashflowOutput>(
                cashflowPlan.getOutputs());
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
                                    cashflowPlan.removeOutput(output.getDate(),
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

        cashflowPlan.addOutput(date, amount);

        resetOutputInputs();

        Util.reloadBindings(cashflowGroupbox);
    }

    private void resetOutputInputs() {
        dateDatebox.setValue(null);
        amountDecimalbox.setValue(null);
    }

    public BigDecimal getTotal() {
        return cashflowPlan.calculateTotal();
    }

    public String getMoneyFormat() {
        return Util.getMoneyFormat();
    }

}
