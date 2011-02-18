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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;

import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.TaskStatusEnum;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.JasperreportComponent;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class WorkingArrangementsPerOrderController extends NavalplannerReportController {

    private static final String REPORT_NAME = "workingArrangementsPerOrderReport";

    private IWorkingArrangementsPerOrderModel workingArrangementsPerOrderModel;

    private Listbox lbTaskStatus;

    private Checkbox cbShowDependencies;

    private BandboxSearch bdOrder;

    private BandboxSearch bdLabels;

    private Listbox lbLabels;

    private BandboxSearch bdCriterions;

    private Listbox lbCriterions;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        setupTaskStatusListbox();
        workingArrangementsPerOrderModel.init();
    }

    private void setupTaskStatusListbox() {
        for(TaskStatusEnum status : getTasksStatus()) {
            Listitem item = new Listitem();
            item.setParent(lbTaskStatus);
            item.setValue(status);
            item.appendChild(new Listcell(status.toString()));
            lbTaskStatus.appendChild(item);
            if(status.equals(TaskStatusEnum.ALL)) {
                item.setSelected(true);
            }
        }
    }

    public List<Order> getAllOrders() {
        return workingArrangementsPerOrderModel.getOrders();
    }

    protected String getReportName() {
        return REPORT_NAME;
    }

    protected JRDataSource getDataSource() {
        return workingArrangementsPerOrderModel
                .getWorkingArrangementsPerOrderReportReport(getSelectedOrder(),
                        getSelectedTaskStatus(), showDependencies(),
                        getSelectedLabels(), getSelectedCriterions());
    }

    private boolean showDependencies() {
        return cbShowDependencies.isChecked();
    }

    private TaskStatusEnum getSelectedTaskStatus() {
        final Listitem item = lbTaskStatus.getSelectedItem();
        return (item != null) ? (TaskStatusEnum) item.getValue() : TaskStatusEnum.ALL;
    }

    private Order getSelectedOrder() {
        return (Order) bdOrder.getSelectedElement();
    }

    protected Map<String, Object> getParameters() {
        Map<String, Object> result = super.getParameters();

        result.put("orderName", getSelectedOrder().getName());

        // Task status
        final TaskStatusEnum taskStatus = getSelectedTaskStatus();
        result.put("taskStatus", _(taskStatus.toString()));

        return result;
    }

    public void showReport(JasperreportComponent jasperreport){
        if (getSelectedOrder() == null) {
            throw new WrongValueException(bdOrder, _("Please, select a project"));
        }
        super.showReport(jasperreport);
    }

    public List<TaskStatusEnum> getTasksStatus() {
        List<TaskStatusEnum> result = new ArrayList<TaskStatusEnum>();
        result.addAll(Arrays.asList(TaskStatusEnum.values()));
        Collections.sort(result, new TaskStatusEnumComparator());
        return result;
    }

    private class TaskStatusEnumComparator implements Comparator<TaskStatusEnum> {

        @Override
        public int compare(TaskStatusEnum arg0, TaskStatusEnum arg1) {
            return arg0.toString().compareTo(arg1.toString());
        }

    }

    public List<Label> getAllLabels() {
        return workingArrangementsPerOrderModel.getAllLabels();
    }

    public void onSelectLabel() {
        Label label = (Label) bdLabels.getSelectedElement();
        if (label == null) {
            throw new WrongValueException(bdLabels, _("please, select a label"));
        }
        boolean result = workingArrangementsPerOrderModel
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
        workingArrangementsPerOrderModel.removeSelectedLabel(label);
        Util.reloadBindings(lbLabels);
    }

    public List<Label> getSelectedLabels() {
        return workingArrangementsPerOrderModel.getSelectedLabels();
    }

    public List<Criterion> getSelectedCriterions() {
        return workingArrangementsPerOrderModel.getSelectedCriterions();
    }

    public List<Criterion> getAllCriterions() {
        return workingArrangementsPerOrderModel.getCriterions();
    }

    public void onSelectCriterion() {
        Criterion criterion = (Criterion) bdCriterions.getSelectedElement();
        if (criterion == null) {
            throw new WrongValueException(bdCriterions,
                    _("please, select a Criterion"));
        }
        boolean result = workingArrangementsPerOrderModel
                .addSelectedCriterion(criterion);
        if (!result) {
            throw new WrongValueException(bdCriterions,
                    _("This Criterion has already been added."));
        } else {
            Util.reloadBindings(lbCriterions);
        }
    }

    public void onRemoveCriterion(Criterion criterion) {
        workingArrangementsPerOrderModel.removeSelectedCriterion(criterion);
        Util.reloadBindings(lbCriterions);
    }

}
