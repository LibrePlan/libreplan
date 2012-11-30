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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.codehaus.plexus.util.StringUtils;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.reports.dtos.ProjectStatusReportDTO;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
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

    private BandboxSearch bandboxCriteria;

    private Listbox listboxCriteria;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("controller", this);

        messagesForUser = new MessagesForUser(messagesContainer);
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
        if (order == null && projectStatusReportModel.isNotFiltering()) {
            messagesForUser
                    .showMessage(
                            Level.ERROR,
                            _("You should filter the report by project, labels or criteria"));
        } else {
            super.showReport(jasperreport);
        }
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
        if (order != null) {
            result.put("project", order.getName() + " (" + order.getCode()
                    + ")");
        } else {
            result.put("filter", getFilterSummary());
        }

        ProjectStatusReportDTO totalDTO = projectStatusReportModel
                .getTotalDTO();

        result.put("estimatedHours", totalDTO.getEstimatedHours());
        result.put("plannedHours", totalDTO.getPlannedHours());
        result.put("imputedHours", totalDTO.getImputedHours());
        result.put("hoursMark", totalDTO.getHoursMark());

        result.put("budget", Util.addCurrencySymbol(totalDTO.getBudget()));
        result.put("hoursCost", Util.addCurrencySymbol(totalDTO.getHoursCost()));
        result.put("expensesCost",
                Util.addCurrencySymbol(totalDTO.getExpensesCost()));
        result.put("totalCost", Util.addCurrencySymbol(totalDTO.getTotalCost()));
        result.put("costMark", totalDTO.getCostMark());

        return result;
    }

    private String getFilterSummary() {
        String filter = "";

        Set<Label> labels = projectStatusReportModel.getSelectedLabels();
        if (!labels.isEmpty()) {
            List<String> labelNames = new ArrayList<String>();
            for (Label label : labels) {
                labelNames.add(label.getName());
            }
            filter += _("Labels") + ": "
                    + StringUtils.join(labelNames.toArray(), ", ");
        }

        Set<Criterion> criteria = projectStatusReportModel.getSelectedCriteria();
        if (!criteria.isEmpty()){
            List<String> criterionNames = new ArrayList<String>();
            for (Criterion criterion : criteria) {
                criterionNames.add(criterion.getName());
            }
            if (!filter.isEmpty()) {
                filter += ". ";
            }
            filter += _("Criteria") + ": "
                    + StringUtils.join(criterionNames.toArray(), ", ");
        }

        return filter;
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

    public List<Criterion> getAllCriteria() {
        return projectStatusReportModel.getAllCriteria();
    }

    public void addCriterion() {
        Criterion criterion = (Criterion) bandboxCriteria.getSelectedElement();
        if (criterion == null) {
            throw new WrongValueException(bandboxCriteria,
                    _("please, select a criterion"));
        }
        projectStatusReportModel.addSelectedCriterion(criterion);
        Util.reloadBindings(listboxCriteria);
        bandboxCriteria.clear();
    }

    public void removeCriterion(Criterion criterion) {
        projectStatusReportModel.removeSelectedCriterion(criterion);
        Util.reloadBindings(listboxCriteria);
    }

    public Set<Criterion> getSelectedCriteria() {
        return projectStatusReportModel.getSelectedCriteria();
    }

}