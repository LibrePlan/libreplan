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
import org.navalplanner.web.common.components.ExtendedJasperreport;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Listbox;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CompletedEstimatedHoursPerTaskController extends NavalplannerReportController {

    private static final String REPORT_NAME = "completedEstimatedHours";

    private ICompletedEstimatedHoursPerTaskModel completedEstimatedHoursPerTaskModel;

    private Listbox lbCriterions;

    private Datebox referenceDate;

    private BandboxSearch bandboxSelectOrder;

    private BandboxSearch bdLabels;

    private Listbox lbLabels;

    private BandboxSearch bdCriterions;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        completedEstimatedHoursPerTaskModel.init();
    }

    public List<Order> getOrders() {
        return completedEstimatedHoursPerTaskModel.getOrders();
    }

    @Override
    protected String getReportName() {
        return REPORT_NAME;
    }

    @Override
    protected JRDataSource getDataSource() {
        return completedEstimatedHoursPerTaskModel
                .getCompletedEstimatedHoursReportPerTask(getSelectedOrder(),
                        getDeadlineDate(), getSelectedLabels(),
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

    @Override
    protected Map<String, Object> getParameters() {
        Map<String, Object> result = super.getParameters();

        result.put("orderName", getSelectedOrder().getName());
        result.put("referenceDate", getDeadlineDate());
        result.put("criteria", getParameterCriterions());
        result.put("labels", getParameterLabels());

        return result;
    }

    public void showReport(ExtendedJasperreport jasperreport) {
        final Order order = getSelectedOrder();
        if (order == null) {
            throw new WrongValueException(bandboxSelectOrder,
                    _("Please, select a project"));
        }
        super.showReport(jasperreport);
    }

    public List<Label> getAllLabels() {
        return completedEstimatedHoursPerTaskModel.getAllLabels();
    }

    public void onSelectLabel() {
        Label label = (Label) bdLabels.getSelectedElement();
        if (label == null) {
            throw new WrongValueException(bdLabels, _("please, select a label"));
        }
        boolean result = completedEstimatedHoursPerTaskModel
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
        completedEstimatedHoursPerTaskModel.removeSelectedLabel(label);
        Util.reloadBindings(lbLabels);
    }

    public List<Label> getSelectedLabels() {
        return completedEstimatedHoursPerTaskModel.getSelectedLabels();
    }

    public List<Criterion> getSelectedCriterions() {
        return completedEstimatedHoursPerTaskModel.getSelectedCriterions();
    }

    public List<Criterion> getAllCriterions() {
        return completedEstimatedHoursPerTaskModel.getCriterions();
    }

    public void onSelectCriterion() {
        Criterion criterion = (Criterion) bdCriterions.getSelectedElement();
        if (criterion == null) {
            throw new WrongValueException(bdCriterions,
                    _("please, select a Criterion"));
        }
        boolean result = completedEstimatedHoursPerTaskModel
                .addSelectedCriterion(criterion);
        if (!result) {
            throw new WrongValueException(bdCriterions,
                    _("This Criterion has already been added."));
        } else {
            Util.reloadBindings(lbCriterions);
        }
    }

    public void onRemoveCriterion(Criterion criterion) {
        completedEstimatedHoursPerTaskModel.removeSelectedCriterion(criterion);
        Util.reloadBindings(lbCriterions);
    }

    private String getParameterCriterions() {
        return completedEstimatedHoursPerTaskModel.getSelectedCriteria();
    }

    private String getParameterLabels() {
        return completedEstimatedHoursPerTaskModel.getSelectedLabel();
    }
}
