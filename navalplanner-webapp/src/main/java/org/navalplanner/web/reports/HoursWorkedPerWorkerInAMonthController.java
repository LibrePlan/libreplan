/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.reports;

import static org.navalplanner.web.I18nHelper._;

import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public class HoursWorkedPerWorkerInAMonthController extends NavalplannerReportController {

    private static final String REPORT_NAME = "hoursWorkedPerWorkerInAMonthReport";

    private static final String months[] = { _("January"), _("February"),
        _("March"), _("April"), _("May"), _("June"), _("July"),
        _("August"), _("September"), _("October"), _("November"),
        _("December") };

    @Autowired
    private IHoursWorkedPerWorkerInAMonthModel hoursWorkedPerWorkerInAMonthModel;

    private Listbox lbYears;

    private Listbox lbMonths;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        hoursWorkedPerWorkerInAMonthModel.init();
    }

    @Override
    protected String getReportName() {
        return REPORT_NAME;
    }

    private String getSelectedMonth() {
        return getSelectedValue(lbMonths);
    }

    private String getSelectedValue(Listbox listbox) {
        Listitem item = listbox.getSelectedItem();
        return (item != null) ? (String) item.getValue() : getFirst(listbox);
    }

    private String getFirst(Listbox listbox) {
        final Listitem item = (Listitem) listbox.getItems().iterator().next();
        return (String) item.getValue();
    }

    private String getSelectedYear() {
        return getSelectedValue(lbYears);
    }

    @Override
    protected JRDataSource getDataSource() {
        return hoursWorkedPerWorkerInAMonthModel.getHoursWorkedPerWorkerReport(
                asInt(getSelectedYear()), asInt(getSelectedMonth()));
    }

    private Integer asInt(String str) {
        return Integer.parseInt(str);
    }

    @Override
    protected Map<String, Object> getParameters() {
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("year", getSelectedYear());
        result.put("month", monthAsLiteral(getSelectedMonth()));
        result.put("showNote", hoursWorkedPerWorkerInAMonthModel.isShowReportMessage());
        return result;
    }

    private String monthAsLiteral(String monthNumber) {
        Integer number = Integer.parseInt(monthNumber);
        return months[number-1];
    }

}