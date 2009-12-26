/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jasperreports.engine.JRDataSource;

import org.navalplanner.business.orders.entities.Order;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class WorkingProgressPerTaskController extends NavalplannerReportController {

    private static final String REPORT_NAME = "workingProgressPerTaskReport";

    private IWorkingProgressPerTaskModel workingProgressPerTaskModel;

    private Listbox lbOrders;

    private Datebox referenceDate;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
    }

    public List<Order> getOrders() {
        return workingProgressPerTaskModel.getOrders();
    }

    protected String getReportName() {
        return REPORT_NAME;
    }

    protected JRDataSource getDataSource() {
        return workingProgressPerTaskModel.getWorkingProgressPerTaskReport(
                getSelectedOrder(), getDeadlineDate());
   }

    private Order getSelectedOrder() {
        final Listitem item = lbOrders.getSelectedItem();
        return (item != null) ? (Order) item.getValue() : null;
    }

    private Date getDeadlineDate() {
        Date result = referenceDate.getValue();
        if (result == null) {
            referenceDate.setValue(new Date());
        }
        return referenceDate.getValue();
    }

    protected Map<String, Object> getParameters() {
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("referenceDate", getDeadlineDate());

        return result;
    }

}
