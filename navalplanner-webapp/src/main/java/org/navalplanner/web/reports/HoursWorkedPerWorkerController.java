/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
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

import org.navalplanner.business.resources.entities.Worker;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class HoursWorkedPerWorkerController extends NavalplannerReportController {

    private static final String REPORT_NAME = "hoursWorkedPerWorkerReport";

    private IHoursWorkedPerWorkerModel hoursWorkedPerWorkerModel;

    private Listbox lbWorkers;

    private Datebox startingDate;

    private Datebox endingDate;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
    }

    public List<Worker> getWorkers() {
        return hoursWorkedPerWorkerModel.getWorkers();
    }

    @Override
    protected String getReportName() {
        return REPORT_NAME;
    }

    @Override
    protected JRDataSource getDataSource() {
        return hoursWorkedPerWorkerModel.getHoursWorkedPerWorkerReport(getSelectedWorkers(),
                getStartingDate(), getEndingDate());
 }

    private List<Worker> getSelectedWorkers() {
        List<Worker> result = new ArrayList<Worker>();

        final Set<Listitem> listItems = lbWorkers.getSelectedItems();
        for (Listitem each: listItems) {
            result.add((Worker) each.getValue());
        }
        return result;
    }

    private Date getStartingDate() {
         return startingDate.getValue();
    }

    private Date getEndingDate() {
        return endingDate.getValue();
    }

    @Override
    protected Map<String, Object> getParameters() {
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("startingDate", getStartingDate());
        result.put("endingDate", getEndingDate());

        return result;
    }

}
