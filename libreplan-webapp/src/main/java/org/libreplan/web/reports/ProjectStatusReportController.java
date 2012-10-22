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

package org.libreplan.web.reports;

import static org.libreplan.web.I18nHelper._;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.reports.dtos.ProjectStatusReportDTO;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Listbox;

import com.igalia.java.zk.components.JasperreportComponent;

/**
 * Controller for UI operations of Project Satus report.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@SuppressWarnings("serial")
public class ProjectStatusReportController extends LibrePlanReportController {

    private static final String REPORT_NAME = "projectStatusReport";

    private IProjectStatusReportModel projectStatusReportModel;

    private BandboxSearch bandboxSelectOrder;

    private BandboxSearch bandboxLabels;

    private Listbox listboxLabels;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("controller", this);
    }

    @Override
    protected String getReportName() {
        return REPORT_NAME;
    }

    @Override
    protected JRDataSource getDataSource() {
        List<ProjectStatusReportDTO> dtos = projectStatusReportModel
                .getProjectStatusReportDTOs(getSelectedOrder());

        if (dtos.isEmpty()) {
            return new JREmptyDataSource();
        }

        return new JRBeanCollectionDataSource(dtos);
    }
    @Override
    public void showReport(JasperreportComponent jasperreport) {
        final Order order = getSelectedOrder();
        if (order == null) {
            throw new WrongValueException(bandboxSelectOrder,
                    _("Please, select a project"));
        }
        super.showReport(jasperreport);
    }

    private Order getSelectedOrder() {
        return (Order) bandboxSelectOrder.getSelectedElement();
    }

    public List<Order> getOrders() {
        return projectStatusReportModel.getOrders();
    }

    @Override
    protected Map<String, Object> getParameters() {
        Map<String, Object> result = super.getParameters();

        Order order = getSelectedOrder();
        result.put("project", order.getName() + " (" + order.getCode() + ")");

        ProjectStatusReportDTO totalDTO = projectStatusReportModel
                .getTotalDTO();

        result.put("estimatedHours", totalDTO.getEstimatedHours());
        result.put("plannedHours", totalDTO.getPlannedHours());
        result.put("imputedHours", totalDTO.getImputedHours());

        result.put("budget", Util.addCurrencySymbol(totalDTO.getBudget()));
        result.put("hoursCost", Util.addCurrencySymbol(totalDTO.getHoursCost()));
        result.put("expensesCost",
                Util.addCurrencySymbol(totalDTO.getExpensesCost()));
        result.put("totalCost", Util.addCurrencySymbol(totalDTO.getTotalCost()));

        return result;
    }

    public List<Label> getAllLabels() {
        return projectStatusReportModel.getAllLabels();
    }

    public void addLabel() {
        Label label = (Label) bandboxLabels.getSelectedElement();
        if (label == null) {
            throw new WrongValueException(bandboxLabels,
                    _("please, select a label"));
        }
        projectStatusReportModel.addSelectedLabel(label);
        Util.reloadBindings(listboxLabels);
        bandboxLabels.clear();
    }

    public void removeLabel(Label label) {
        projectStatusReportModel.removeSelectedLabel(label);
        Util.reloadBindings(listboxLabels);
    }

    public Set<Label> getSelectedLabels() {
        return projectStatusReportModel.getSelectedLabels();
    }

}