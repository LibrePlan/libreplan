/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

import java.util.Date;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;

import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Listbox;

import com.igalia.java.zk.components.JasperreportComponent;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class WorkingProgressPerTaskController extends NavalplannerReportController {

    private static final String REPORT_NAME = "workingProgressPerTaskReport";

    private IWorkingProgressPerTaskModel workingProgressPerTaskModel;

    private Datebox referenceDate;

    private Listbox lbCriterions;

    private BandboxSearch bandboxSelectOrder;

    private BandboxSearch bdLabels;

    private Listbox lbLabels;

    private BandboxSearch bdCriterions;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        workingProgressPerTaskModel.init();
    }

    public List<Order> getOrders() {
        return workingProgressPerTaskModel.getOrders();
    }

    protected String getReportName() {
        return REPORT_NAME;
    }

    protected JRDataSource getDataSource() {
        return workingProgressPerTaskModel.getWorkingProgressPerTaskReport(
                getSelectedOrder(), getDeadlineDate(), getSelectedLabels(),
                getSelectedCriterions());
    }

    private Order getSelectedOrder() {
        return (Order) bandboxSelectOrder.getSelectedElement();
    }

    private Date getDeadlineDate() {
        Date result = referenceDate.getValue();
        if (result == null) {
            referenceDate.setValue(new Date());
        }
        return referenceDate.getValue();
    }

    protected Map<String, Object> getParameters() {
        Map<String, Object> result = super.getParameters();

        result.put("orderName", getSelectedOrder().getName());
        result.put("referenceDate", getDeadlineDate());
        result.put("criteria", getParameterCriterions());
        result.put("labels", getParameterLabels());
        return result;
    }

    public void showReport(JasperreportComponent jasperreport){
        final Order order = getSelectedOrder();
        if (order == null) {
            throw new WrongValueException(bandboxSelectOrder,
                    _("Please, select a project"));
        }
        super.showReport(jasperreport);
    }

    public List<Label> getAllLabels() {
        return workingProgressPerTaskModel.getAllLabels();
    }

    public void onSelectLabel() {
        Label label = (Label) bdLabels.getSelectedElement();
        if (label == null) {
            throw new WrongValueException(bdLabels, _("please, select a label"));
        }
        boolean result = workingProgressPerTaskModel
                .addSelectedLabel(label);
        if (!result) {
            throw new WrongValueException(bdLabels,
                    _("This label has already been added."));
        } else {
            Util.reloadBindings(lbLabels);
        }
        bdLabels.clear();
    }

    public void onRemoveLabel(Label label) {
        workingProgressPerTaskModel.removeSelectedLabel(label);
        Util.reloadBindings(lbLabels);
    }

    public List<Label> getSelectedLabels() {
        return workingProgressPerTaskModel.getSelectedLabels();
    }

    public List<Criterion> getSelectedCriterions() {
        return workingProgressPerTaskModel.getSelectedCriterions();
    }

    public List<Criterion> getAllCriterions() {
        return workingProgressPerTaskModel.getCriterions();
    }

    public void onSelectCriterion() {
        Criterion criterion = (Criterion) bdCriterions.getSelectedElement();
        if (criterion == null) {
            throw new WrongValueException(bdCriterions,
                    _("please, select a Criterion"));
        }
        boolean result = workingProgressPerTaskModel
                .addSelectedCriterion(criterion);
        if (!result) {
            throw new WrongValueException(bdCriterions,
                    _("This Criterion has already been added."));
        } else {
            Util.reloadBindings(lbCriterions);
        }
    }

    public void onRemoveCriterion(Criterion criterion) {
        workingProgressPerTaskModel.removeSelectedCriterion(criterion);
        Util.reloadBindings(lbCriterions);
    }

    private String getParameterCriterions() {
        return workingProgressPerTaskModel.getSelectedCriteria();
    }

    private String getParameterLabels() {
        return workingProgressPerTaskModel.getSelectedLabel();
    }
}
