/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.planner.allocation;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.orders.entities.AggregatedHoursGroup;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.web.common.ConstraintChecker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.NewAllocationSelector;
import org.navalplanner.web.planner.allocation.LimitingResourceAllocationModel.LimitingResourceAllocationRow;
import org.navalplanner.web.planner.allocation.ResourceAllocationController.HoursRendererColumn;
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

    private Tab tabLimitingResourceAllocation;

    private Grid gridLimitingOrderElementHours;

    private Grid gridLimitingAllocations;

    private NewAllocationSelector limitingNewAllocationSelector;

    private GridLimitingAllocationRenderer gridLimitingAllocationRenderer = new GridLimitingAllocationRenderer();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        limitingNewAllocationSelector.setLimitingResourceFilter(true);
        limitingNewAllocationSelector.allowSelectMultipleResources(false);
    }

    /**
     * Shows Resource Allocation window
     * @param task
     * @param ganttTask
     * @param planningState
     */
    public void init(org.navalplanner.business.planner.entities.Task task,
            IMessagesForUser messagesForUser) {
        try {
            resourceAllocationModel.init(task);
            limitingNewAllocationSelector.setAllocationsAdder(resourceAllocationModel);
            gridLimitingOrderElementHours.setModel(new ListModelList(
                    resourceAllocationModel.getHoursAggregatedByCriteria()));
            gridLimitingOrderElementHours.setRowRenderer(createOrderElementHoursRenderer());
        } catch (Exception e) {

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

    public List<LimitingResourceAllocationRow> getResourceAllocations() {
        return resourceAllocationModel.getResourceAllocations();
    }

    public void removeAllResourceAllocations() {
        resourceAllocationModel.removeAllResourceAllocations();
    }

    public void onSelectWorkers(Event event) {
        try {
            addSelectedResources();
        } finally {
            tabLimitingResourceAllocation.setSelected(true);
            limitingNewAllocationSelector.clearAll();
            Util.reloadBindings(gridLimitingAllocations);
        }
    }

    private void addSelectedResources() {
        resourceAllocationModel.removeAllResourceAllocations();
        limitingNewAllocationSelector.addChoosen();
    }

    public void onCloseSelectWorkers() {
        clear();
    }

    public void clear() {
        resourceAllocationModel.removeAllResourceAllocations();
        limitingNewAllocationSelector.clearAll();
    }

    public GridLimitingAllocationRenderer getGridLimitingAllocationRenderer() {
        return gridLimitingAllocationRenderer;
    }

    public class GridLimitingAllocationRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) throws Exception {
            LimitingResourceAllocationRow resourceAllocation = (LimitingResourceAllocationRow) data;

            row.appendChild(label(resourceAllocation.getAllocationType()));
            row.appendChild(label(resourceAllocation.getAllocation()));
            row.appendChild(intboxHours(resourceAllocation));
            row.appendChild(listboxPriority(resourceAllocation));
        }

        private Label label(String value) {
            return new Label(value);
        }

        private Intbox intboxHours(final LimitingResourceAllocationRow resourceAllocation) {
            return bindToHours(new Intbox(), resourceAllocation);
        }

        private Intbox bindToHours(Intbox intbox, final LimitingResourceAllocationRow resourceAllocation) {
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

        private Listbox listboxPriority(final LimitingResourceAllocationRow resourceAllocation) {
            return bindToPriority(buildPriorityList(resourceAllocation.getPriority()), resourceAllocation);
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

        private Listbox bindToPriority(Listbox listbox, final LimitingResourceAllocationRow resourceAllocation) {
            listbox.addEventListener("onSelect", new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    resourceAllocation.setPriorityStr((String) event.getData());
                }
            });
            return listbox;
        }

    }

}
