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

package org.navalplanner.web.planner.limiting.allocation;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.orders.entities.AggregatedHoursGroup;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.AllocationSelector;
import org.navalplanner.web.common.components.NewAllocationSelector;
import org.navalplanner.web.common.components.NewAllocationSelectorCombo;
import org.navalplanner.web.planner.allocation.ResourceAllocationController.HoursRendererColumn;
import org.navalplanner.web.planner.order.PlanningState;
import org.navalplanner.web.planner.taskedition.EditTaskController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.timetracker.ICellForDetailItemRenderer;
import org.zkoss.ganttz.timetracker.OnColumnsRowRenderer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Tab;

/**
 * Controller for {@link ResourceAllocation} view.
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@org.springframework.stereotype.Component("limitingResourceAllocationController")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LimitingResourceAllocationController extends GenericForwardComposer {

    private static final Log LOG = LogFactory
            .getLog(LimitingResourceAllocationController.class);

    @Autowired
    private ILimitingResourceAllocationModel resourceAllocationModel;

    private EditTaskController editTaskController;

    private Tab tabLimitingResourceAllocation;

    private Tab tabLimitingWorkerSearch;

    private Grid gridLimitingOrderElementHours;

    private Grid gridLimitingAllocations;

    private Label totalEstimatedHours;

    private boolean disableHours = true;

    private NewAllocationSelectorCombo limitingNewAllocationSelectorCombo;

    private NewAllocationSelector limitingNewAllocationSelector;

    private GridLimitingAllocationRenderer gridLimitingAllocationRenderer = new GridLimitingAllocationRenderer();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        limitingNewAllocationSelector.setLimitingResourceFilter(true);
        limitingNewAllocationSelectorCombo.setLimitingResourceFilter(true);
        limitingNewAllocationSelector.allowSelectMultipleResources(false);
    }

    public void setDisableHours(boolean disable) {
        this.disableHours = disable;
    }

    /**
     * Shows Resource Allocation window
     * @param task
     * @param ganttTask
     * @param planningState
     */
    public void init(org.navalplanner.business.planner.entities.Task task,
            PlanningState planningState,
            IMessagesForUser messagesForUser) {
        try {
            resourceAllocationModel.init(task, planningState);
            resourceAllocationModel.setLimitingResourceAllocationController(this);

            // if exist resource allocation with day assignments, it can not
            // change them
            boolean existsDaysAssignments = existsResourceAllocationWithDayAssignments();
            tabLimitingWorkerSearch.setDisabled(existsDaysAssignments);
            limitingNewAllocationSelectorCombo
                    .setDisabled(existsDaysAssignments);

            limitingNewAllocationSelector.setAllocationsAdder(resourceAllocationModel);
            limitingNewAllocationSelectorCombo
                    .setAllocationsAdder(resourceAllocationModel);
            gridLimitingOrderElementHours.setModel(new ListModelList(
                    resourceAllocationModel.getHoursAggregatedByCriteria()));
            gridLimitingOrderElementHours.setRowRenderer(createOrderElementHoursRenderer());
            Util.reloadBindings(gridLimitingAllocations);
            Util.reloadBindings(totalEstimatedHours);
        } catch (Exception e) {
            LOG.error(e.getStackTrace());
        }
    }

    private static final ICellForDetailItemRenderer<HoursRendererColumn, AggregatedHoursGroup> hoursCellRenderer =
        new ICellForDetailItemRenderer<HoursRendererColumn, AggregatedHoursGroup>() {

        @Override
        public Component cellFor(HoursRendererColumn column,
                AggregatedHoursGroup data) {
            return column.cell(column, data);
        }
    };

    private RowRenderer createOrderElementHoursRenderer() {
        return OnColumnsRowRenderer.create(hoursCellRenderer, Arrays
                .asList(HoursRendererColumn.values()));
    }

    public Integer getOrderHours() {
        return resourceAllocationModel.getOrderHours();
    }

    public List<LimitingAllocationRow> getResourceAllocationRows() {
        return resourceAllocationModel.getResourceAllocationRows();
    }

    public void onSelectWorkers(AllocationSelector allocationSelector) {
        try {
            allocationSelector.addChoosen();
        } finally {
            tabLimitingResourceAllocation.setSelected(true);
            allocationSelector.clearAll();
            Util.reloadBindings(gridLimitingAllocations);
        }
    }

    public void onCloseSelectWorkers() {
        clear();
    }

    public void clear() {
        limitingNewAllocationSelector.clearAll();
        limitingNewAllocationSelectorCombo.clearAll();
    }

    public GridLimitingAllocationRenderer getGridLimitingAllocationRenderer() {
        return gridLimitingAllocationRenderer;
    }

    public void accept() {
        resourceAllocationModel.confirmSave();
    }

    public class GridLimitingAllocationRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) throws Exception {
            LimitingAllocationRow resourceAllocation = (LimitingAllocationRow) data;

            row.appendChild(label(resourceAllocation.getAllocationTypeStr()));
            row.appendChild(label(resourceAllocation.getAllocation()));
            row.appendChild(intboxHours(resourceAllocation));
            row.appendChild(listboxPriority(resourceAllocation));
        }

        private Label label(String value) {
            return new Label(value);
        }

        private Intbox intboxHours(final LimitingAllocationRow resourceAllocation) {
            Intbox result = bindToHours(new Intbox(), resourceAllocation);
            result.setDisabled(resourceAllocation.hasDayAssignments() && disableHours);
            return result;
        }

        private Intbox bindToHours(Intbox intbox, final LimitingAllocationRow resourceAllocation) {
            Util.bind(intbox, new Util.Getter<Integer>() {

                @Override
                public Integer get() {
                    return resourceAllocation.getHours();
                }

            }, new Util.Setter<Integer>() {

                @Override
                public void set(Integer value) {
                    resourceAllocation.setHours(value);
                }
            });
            return intbox;
        }

        private Listbox listboxPriority(final LimitingAllocationRow resourceAllocation) {
            Listbox result = bindToPriority(buildPriorityList(resourceAllocation.getPriority()), resourceAllocation);
            result.setDisabled(resourceAllocation.hasDayAssignments());
            return result;
        }

        private Listbox buildPriorityList(int selectedValue) {
            Listbox result = listbox();
            for (int i = 1; i <= 10; i++) {
                Listitem item = new Listitem();
                Listcell cell = new Listcell(new Integer(i).toString());
                cell.setParent(item);
                if (i == selectedValue) {
                    item.setSelected(true);
                }
                item.setParent(result);
            }
            return result;
        }

        private Listbox listbox() {
            Listbox result = new Listbox();
            result.setMold("select");
            return result;
        }

        private Listbox bindToPriority(Listbox listbox, final LimitingAllocationRow resourceAllocation) {
            listbox.addEventListener("onSelect", new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    String priority = getSelectedValue((Listbox) event.getTarget());
                    resourceAllocation.setPriorityStr(priority);
                }
            });
            return listbox;
        }

        private String getSelectedValue(Listbox listbox) {
            final Listitem item = listbox.getSelectedItem();
            final Listcell cell = (Listcell) item.getChildren().get(0);
            return cell.getLabel();
        }

    }

    public boolean existsResourceAllocationWithDayAssignments() {
        final LimitingAllocationRow limitingAllocationRow = getLimitingAllocationRow();
        return (limitingAllocationRow != null) ? limitingAllocationRow
                .hasDayAssignments() : false;
    }

    private LimitingAllocationRow getLimitingAllocationRow() {
        final List<LimitingAllocationRow> limitingAllocationRows = resourceAllocationModel
                .getResourceAllocationRows();
        return (limitingAllocationRows.size() > 0) ? limitingAllocationRows
                .get(0) : null;
    }

    public void setEditTaskController(EditTaskController editTaskController) {
        this.editTaskController = editTaskController;
    }

    public IMessagesForUser getMessagesForUser() {
        return editTaskController.getMessagesForUser();
    }

}
