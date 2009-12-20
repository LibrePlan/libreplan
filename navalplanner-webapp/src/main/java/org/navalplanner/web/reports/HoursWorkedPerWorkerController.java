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

import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.components.ExtendedJasperreport;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Toolbarbutton;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class HoursWorkedPerWorkerController extends GenericForwardComposer {

    private IHoursWorkedPerWorkerModel hoursWorkedPerWorkerModel;

    private HoursWorkedPerWorkerReport workerReport;

    private Listbox lbWorkers;

    private Datebox startingDate;

    private Datebox endingDate;

    private ComboboxOutputFormat outputFormat;

    private Hbox URItext;

    private Toolbarbutton URIlink;

    private static final String HTML = "html";

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
    }

    public List<Worker> getWorkers() {
        return hoursWorkedPerWorkerModel.getWorkers();
    }

    public void showReport(ExtendedJasperreport report) {
        final String type = outputFormat.getOutputFormat();

        workerReport = new HoursWorkedPerWorkerReport(report);
        workerReport.setDatasource(getDataSource());
        workerReport.setParameters(getParameters());

        String URI = workerReport.show(type);
        if (type.equals(HTML)) {
            URItext.setStyle("display: none");
            Executions.getCurrent().sendRedirect(URI, "_blank");
        } else {
            URItext.setStyle("display: inline");
            URIlink.setHref(URI);
        }
    }

    private JRDataSource getDataSource() {
        return hoursWorkedPerWorkerModel.getWorkerReport(getSelectedWorkers(), getStartingDate(), getEndingDate());
    }

    private Map<String, Object> getParameters() {
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("startingDate", getStartingDate());
        result.put("endingDate", getEndingDate());

        return result;
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

}
