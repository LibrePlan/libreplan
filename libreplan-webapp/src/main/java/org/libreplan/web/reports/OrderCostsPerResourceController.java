/*
 * This file is part of LibrePlan
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

package org.libreplan.web.reports;

import static org.libreplan.web.I18nHelper._;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;

import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Listbox;

/**
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class OrderCostsPerResourceController extends LibrePlanReportController {

    private static final String REPORT_NAME = "orderCostsPerResourceReport";

    private IOrderCostsPerResourceModel orderCostsPerResourceModel;

    private Datebox startingDate;

    private Datebox endingDate;

    private Listbox lbOrders;

    private Listbox lbLabels;

    private Listbox lbCriterions;

    private BandboxSearch bdOrders;

    private BandboxSearch bdLabels;

    private BandboxSearch bdCriterions;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        orderCostsPerResourceModel.init();
    }

    protected JRDataSource getDataSource() {
        return orderCostsPerResourceModel.getOrderReport(getSelectedOrders(),
                getStartingDate(), getEndingDate(), getSelectedLabels(),
                getSelectedCriterions());
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> result = super.getParameters();

        result.put("startingDate", getStartingDate());
        result.put("endingDate", getEndingDate());
        result.put("criteria", getParameterCriterions());
        result.put("labels", getParameterLabels());
        result.put("subReportWRL", "costWorkReportLinesReport.jasper");
        result.put("subReportES", "costExpenseSheetLinesReport.jasper");
        result.put("currencySymbol", Util.getCurrencySymbol());

        return result;
    }

    public List<Order> getAllOrders() {
        return orderCostsPerResourceModel.getOrders();
    }

    public List<Order> getSelectedOrders() {
        return Collections.unmodifiableList(orderCostsPerResourceModel
                .getSelectedOrders());
    }

    public void onSelectOrder() {
        Order order = (Order) bdOrders.getSelectedElement();
        if (order == null) {
            throw new WrongValueException(bdOrders, _("please, select a project"));
        }
        boolean result = orderCostsPerResourceModel.addSelectedOrder(order);
        if (!result) {
            throw new WrongValueException(bdOrders,
                    _("This project has already been added."));
        } else {
            Util.reloadBindings(lbOrders);
        }
        bdOrders.clear();
    }

    public void onRemoveOrder(Order order) {
        orderCostsPerResourceModel.removeSelectedOrder(order);
        Util.reloadBindings(lbOrders);
    }

    private Date getStartingDate() {
         return startingDate.getValue();
    }

    private Date getEndingDate() {
        return endingDate.getValue();
    }

    public Constraint checkConstraintStartingDate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Date startDateLine = (Date) value;
                if ((startDateLine != null) && (getEndingDate() != null)
                        && (startDateLine.compareTo(getEndingDate()) > 0)) {
                    ((Datebox) comp).setValue(null);
                    throw new WrongValueException(comp,
                            _("must be lower than finish date"));
                }
            }
        };
    }

    public Constraint checkConstraintEndingDate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Date endingDate = (Date) value;
                if ((endingDate != null) && (getStartingDate() != null)
                        && (endingDate.compareTo(getStartingDate()) < 0)) {
                    ((Datebox) comp).setValue(null);
                    throw new WrongValueException(comp,
                            _("must be after finish date"));
                }
            }
        };
    }

    public List<Label> getAllLabels() {
        return orderCostsPerResourceModel.getAllLabels();
    }

    public void onSelectLabel() {
        Label label = (Label) bdLabels.getSelectedElement();
        if (label == null) {
            throw new WrongValueException(bdLabels, _("please, select a label"));
        }
        boolean result = orderCostsPerResourceModel.addSelectedLabel(label);
        if (!result) {
            throw new WrongValueException(bdLabels,
                    _("Label has already been added."));
        } else {
            Util.reloadBindings(lbLabels);
        }
        bdLabels.clear();
    }

    public void onRemoveLabel(Label label) {
        orderCostsPerResourceModel.removeSelectedLabel(label);
        Util.reloadBindings(lbLabels);
    }

    public List<Label> getSelectedLabels() {
        return orderCostsPerResourceModel.getSelectedLabels();
    }

    public List<Criterion> getSelectedCriterions() {
        return orderCostsPerResourceModel.getSelectedCriterions();
    }

    public List<Criterion> getAllCriterions() {
        return orderCostsPerResourceModel.getCriterions();
    }

    public void onSelectCriterion() {
        Criterion criterion = (Criterion) bdCriterions.getSelectedElement();
        if (criterion == null) {
            throw new WrongValueException(bdCriterions,
                    _("please, select a Criterion"));
        }
        boolean result = orderCostsPerResourceModel
                .addSelectedCriterion(criterion);
        if (!result) {
            throw new WrongValueException(bdCriterions,
                    _("This Criterion has already been added."));
        } else {
            Util.reloadBindings(lbCriterions);
        }
        bdCriterions.clear();
    }

    public void onRemoveCriterion(Criterion criterion) {
        orderCostsPerResourceModel.removeSelectedCriterion(criterion);
        Util.reloadBindings(lbCriterions);
    }

    @Override
    protected String getReportName() {
        return REPORT_NAME;
    }

    private String getParameterCriterions() {
        return orderCostsPerResourceModel.getSelectedCriteria();
    }

    private String getParameterLabels() {
        return orderCostsPerResourceModel.getSelectedLabel();
    }
}
