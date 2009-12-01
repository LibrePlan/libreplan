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

package org.navalplanner.web.planner.allocation;

import static org.navalplanner.web.I18nHelper._;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.orders.entities.AggregatedHoursGroup;
import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.ViewSwitcher;
import org.navalplanner.web.common.components.NewAllocationSelector;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.IAdvanceAllocationResultReceiver;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.Restriction;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.Restriction.IRestrictionSource;
import org.navalplanner.web.planner.order.PlanningState;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.timetracker.ICellForDetailItemRenderer;
import org.zkoss.ganttz.timetracker.OnColumnsRowRenderer;
import org.zkoss.ganttz.util.OnZKDesktopRegistry;
import org.zkoss.ganttz.util.script.IScriptsRegister;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Tab;
import org.zkoss.zul.api.Window;

/**
 * Controller for {@link ResourceAllocation} view.
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@org.springframework.stereotype.Component("resourceAllocationController")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResourceAllocationController extends GenericForwardComposer {

    private ViewSwitcher switcher;

    private IResourceAllocationModel resourceAllocationModel;

    private Grid orderElementHoursGrid;

    private ResourceAllocationRenderer resourceAllocationRenderer = new ResourceAllocationRenderer();

    private Component messagesContainer;

    private IMessagesForUser messagesForUser;

    private Listbox allocationsList;

    private Window window;

    private FormBinder formBinder;

    private ResourceAllocationsBeingEdited allocationsBeingEdited;

    private Intbox assignedHoursComponent;

    private Datebox taskStartDateBox;

    private Grid calculationTypesGrid;

    private Radiogroup calculationTypeSelector;

    private Datebox taskEndDate;

    private Button applyButton;

    private NewAllocationSelector newAllocationSelector;

    private Tab tbResourceAllocation;

    public static void registerNeededScripts() {
        getScriptsRegister()
                .register(ScriptsRequiredByAdvancedAllocation.class);
    }

    private static IScriptsRegister getScriptsRegister() {
        return OnZKDesktopRegistry.getLocatorFor(IScriptsRegister.class)
                .retrieve();
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.window = (Window) comp;
        messagesForUser = new MessagesForUser(messagesContainer);
        taskEndDate = new Datebox();
        makeReadyInputsForCalculationTypes();
        prepareCalculationTypesGrid();
    }

    private void makeReadyInputsForCalculationTypes() {
        final String width = "300px";
        taskEndDate.setWidth(width);
        assignedHoursComponent = new Intbox();
        assignedHoursComponent.setWidth(width);
    }

    private void prepareCalculationTypesGrid() {
        calculationTypesGrid.setModel(new ListModelList(Arrays
                .asList(CalculationTypeRadio.values())));
        calculationTypesGrid.setRowRenderer(OnColumnsRowRenderer.create(
                calculationTypesRenderer(), Arrays.asList(0, 1)));
    }

    private ICellForDetailItemRenderer<Integer, CalculationTypeRadio> calculationTypesRenderer() {
        return new ICellForDetailItemRenderer<Integer, CalculationTypeRadio>() {

            @Override
            public Component cellFor(Integer column, CalculationTypeRadio data) {
                if (column == 0) {
                    return data.createRadio();
                } else {
                    return data.input(ResourceAllocationController.this);
                }
            }
        };
    }

    /**
     * Shows Resource Allocation window
     * @param task
     * @param ganttTask
     * @param planningState
     */
    public void showWindow(Task task, org.zkoss.ganttz.data.Task ganttTask,
            PlanningState planningState) {
        if (formBinder != null) {
            formBinder.detach();
        }
        allocationsBeingEdited = resourceAllocationModel.initAllocationsFor(
                task, ganttTask, planningState);
        formBinder = allocationsBeingEdited
                .createFormBinder(resourceAllocationModel);
        formBinder.setAssignedHoursComponent(assignedHoursComponent);
        formBinder.setTaskStartDateBox(taskStartDateBox);
        formBinder.setEndDate(taskEndDate);
        formBinder.setApplyButton(applyButton);
        formBinder.setAllocationsList(allocationsList);
        formBinder.setMessagesForUser(messagesForUser);
        CalculationTypeRadio calculationTypeRadio = CalculationTypeRadio
                .from(formBinder.getCalculatedValue());
        calculationTypeRadio.doTheSelectionOn(calculationTypeSelector);
        tbResourceAllocation.setSelected(true);
        orderElementHoursGrid.setModel(new ListModelList(
                resourceAllocationModel.getHoursAggregatedByCriterions()));
        orderElementHoursGrid.setRowRenderer(createOrderElementHoursRenderer());
        newAllocationSelector.setAllocationsAdder(resourceAllocationModel);
        showWindow();
    }

    public enum HoursRendererColumn {

        CRITERIONS {
            @Override
            public Component cell(HoursRendererColumn column,
                    AggregatedHoursGroup data) {
                return new Label(data.getCriterionsJoinedByComma());
            }
        },
        HOURS {
            @Override
            public Component cell(HoursRendererColumn column,
                    AggregatedHoursGroup data) {
                Intbox result = new Intbox(data.getHours());
                result.setDisabled(true);
                return result;
            }
        };

        public abstract Component cell(HoursRendererColumn column,
                AggregatedHoursGroup data);
    }

    private static final ICellForDetailItemRenderer<HoursRendererColumn, AggregatedHoursGroup> hoursCellRenderer = new ICellForDetailItemRenderer<HoursRendererColumn, AggregatedHoursGroup>() {

        @Override
        public Component cellFor(
                HoursRendererColumn column,
                AggregatedHoursGroup data) {
            return column.cell(column, data);
        }
    };

    private RowRenderer createOrderElementHoursRenderer() {
        return OnColumnsRowRenderer
                .create(
                        hoursCellRenderer, Arrays.asList(HoursRendererColumn.values()));
    }

    private void showWindow() {
        Util.reloadBindings(window);
        try {
            window.doModal();
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Pick resources selected from {@link NewAllocationSelector} and add them to
     * resource allocation list
     *
     * @param e
     */
    public void onSelectWorkers(Event e) {
        try {
            newAllocationSelector.addChoosen();
        } finally {
            tbResourceAllocation.setSelected(true);
            newAllocationSelector.clearAll();
            Util.reloadBindings(allocationsList);
        }
    }

    /**
     * Close search worker in worker search tab
     *
     * @param e
     */
    public void onCloseSelectWorkers() {
        tbResourceAllocation.setSelected(true);
        newAllocationSelector.clearAll();
    }

    private final class AdvanceAllocationResultReceiver implements
            IAdvanceAllocationResultReceiver {

        private final AllocationResult allocation;

        private AdvanceAllocationResultReceiver(AllocationResult allocation) {
            this.allocation = allocation;
        }

        @Override
        public void cancel() {
            showWindow();
        }

        @Override
        public void accepted(AggregateOfResourceAllocations aggregate) {
            resourceAllocationModel.accept(allocation);
        }

        @Override
        public Restriction createRestriction() {
            return Restriction.build(new IRestrictionSource() {

                @Override
                public int getTotalHours() {
                    return allocation.getAggregate().getTotalHours();
                }

                @Override
                public LocalDate getStart() {
                    return allocation.getStart();
                }

                @Override
                public LocalDate getEnd() {
                    return getStart()
                            .plusDays(allocation.getDaysDuration());
                }

                @Override
                public CalculatedValue getCalculatedValue() {
                    return allocation.getCalculatedValue();
                }
            });
        }
    }

    public enum CalculationTypeRadio {

        NUMBER_OF_HOURS(CalculatedValue.NUMBER_OF_HOURS) {
            @Override
            public String getName() {
                return _("Calculate Number of Hours");
            }

            @Override
            public Component input(
                    ResourceAllocationController resourceAllocationController) {
                return resourceAllocationController.assignedHoursComponent;
            }
        },
        END_DATE(CalculatedValue.END_DATE) {
            @Override
            public String getName() {
                return _("Calculate End Date");
            }

            @Override
            public Component input(
                    ResourceAllocationController resourceAllocationController) {
                return resourceAllocationController.taskEndDate;
            }
        },
        RESOURCES_PER_DAY(CalculatedValue.RESOURCES_PER_DAY) {

            @Override
            public String getName() {
                return _("Calculate Resources per Day");
            }

            @Override
            public Component input(
                    ResourceAllocationController resourceAllocationController) {
                return new Label("");
            }
        };

        public static CalculationTypeRadio from(CalculatedValue calculatedValue) {
            Validate.notNull(calculatedValue);
            for (CalculationTypeRadio calculationTypeRadio : CalculationTypeRadio
                    .values()) {
                if (calculationTypeRadio.getCalculatedValue() == calculatedValue) {
                    return calculationTypeRadio;
                }
            }
            throw new RuntimeException("not found "
                    + CalculationTypeRadio.class.getSimpleName() + " for "
                    + calculatedValue);
        }

        public abstract Component input(
                ResourceAllocationController resourceAllocationController);

        public Radio createRadio() {
            Radio result = new Radio();
            result.setLabel(getName());
            result.setValue(toString());
            return result;
        }

        public void doTheSelectionOn(Radiogroup radiogroup) {
            for (int i = 0; i < radiogroup.getItemCount(); i++) {
                Radio radio = radiogroup.getItemAtIndex(i);
                if (name().equals(radio.getValue())) {
                    radiogroup.setSelectedIndex(i);
                    break;
                }
            }
        }

        private final CalculatedValue calculatedValue;

        private CalculationTypeRadio(CalculatedValue calculatedValue) {
            this.calculatedValue = calculatedValue;

        }

        public abstract String getName();

        public CalculatedValue getCalculatedValue() {
            return calculatedValue;
        }
    }

    public List<CalculationTypeRadio> getCalculationTypes() {
        return Arrays.asList(CalculationTypeRadio.values());
    }

    public void setCalculationTypeSelected(String enumName) {
        CalculationTypeRadio calculationTypeRadio = CalculationTypeRadio
                .valueOf(enumName);
        formBinder
                .setCalculatedValue(calculationTypeRadio.getCalculatedValue());
    }

    public Integer getOrderHours() {
        return resourceAllocationModel.getOrderHours();
    }

    public List<AllocationRow> getResourceAllocations() {
        return formBinder != null ? formBinder
                .getCurrentRows() : Collections
                .<AllocationRow> emptyList();
    }

    public ResourceAllocationRenderer getResourceAllocationRenderer() {
        return resourceAllocationRenderer;
    }

    // Triggered when closable button is clicked
    public void onClose(Event event) {
        cancel();
        event.stopPropagation();
    }

    public void cancel() {
        close();
        resourceAllocationModel.cancel();
    }


    private void close() {
        window.setVisible(false);
        clear();
    }

    private void clear() {
        newAllocationSelector.clearAll();
        allocationsList.getItems().clear();
    }

    public void accept() {
        resourceAllocationModel.accept();
        close();
    }

    public void goToAdvancedAllocation() {
        AllocationResult allocationResult = formBinder.getLastAllocation();
        if (allocationResult.getAggregate().isEmpty()) {
            formBinder.doApply();
            allocationResult = formBinder.getLastAllocation();
        }
        switcher.goToAdvancedAllocation(allocationResult,
                createResultReceiver(allocationResult));
        window.setVisible(false);
    }

    private IAdvanceAllocationResultReceiver createResultReceiver(
            final AllocationResult allocation) {
        return new AdvanceAllocationResultReceiver(allocation);
    }

    /**
     * Renders a {@link SpecificResourceAllocation} item
     * @author Diego Pino Garcia <dpino@igalia.com>
     */
    private class ResourceAllocationRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            renderResourceAllocation(item, (AllocationRow) data);
        }

        private void renderResourceAllocation(Listitem item,
                final AllocationRow row) throws Exception {
            item.setValue(row);
            // Label fields are fixed, can only be viewed
            append(item, new Label(row.getName()));
            append(item, row.getHoursInput());
            append(item, row.getResourcesPerDayInput());
            // On click delete button
            Button deleteButton = appendDeleteButton(item);
            formBinder.setDeleteButtonFor(row, deleteButton);
            deleteButton.addEventListener("onClick", new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    removeAllocation(row);
                }
            });
        }

        private void removeAllocation(AllocationRow row) {
            allocationsBeingEdited.remove(row);
            Util.reloadBindings(allocationsList);
        }

        /**
         * Appends delete {@link Button} to {@link Listitem}
         * @param listitem
         *            value for {@link Button}
         * @return
         */
        private Button appendDeleteButton(Listitem listitem) {
            Button button = new Button();
            button.setSclass("icono");
            button.setImage("/common/img/ico_borrar1.png");
            button.setHoverImage("/common/img/ico_borrar.png");
            button.setTooltiptext(_("Delete"));
            return append(listitem, button);
        }

        private <T extends Component> T append(Listitem item, T component) {
            Listcell listcell = new Listcell();
            listcell.appendChild(component);
            item.appendChild(listcell);
            return component;
        }
    }

    public ViewSwitcher getSwitcher() {
        return switcher;
    }

    public void setSwitcher(ViewSwitcher switcher) {
        this.switcher = switcher;
    }
}
