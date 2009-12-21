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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.navalplanner.business.orders.entities.AggregatedHoursGroup;
import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.navalplanner.business.planner.entities.DerivedAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.ResourceEnum;
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
import org.zkoss.ganttz.timetracker.IConvertibleToColumn;
import org.zkoss.ganttz.timetracker.OnColumnsRowRenderer;
import org.zkoss.ganttz.util.OnZKDesktopRegistry;
import org.zkoss.ganttz.util.script.IScriptsRegister;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;
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

    private static final Log LOG = LogFactory
            .getLog(ResourceAllocationController.class);

    private ViewSwitcher switcher;

    private IResourceAllocationModel resourceAllocationModel;

    private Grid orderElementHoursGrid;

    private ResourceAllocationRenderer resourceAllocationRenderer = new ResourceAllocationRenderer();

    private Component messagesContainer;

    private IMessagesForUser messagesForUser;

    private Grid allocationsGrid;

    private Window window;

    private FormBinder formBinder;

    private AllocationRowsHandler allocationRows;

    private Intbox assignedHoursComponent;

    private Datebox taskStartDateBox;

    private Grid calculationTypesGrid;

    private Radiogroup calculationTypeSelector;

    private Checkbox recommendedAllocationCheckbox;

    private Datebox taskEndDate;

    private Decimalbox allResourcesPerDay;

    private Button applyButton;

    private NewAllocationSelector newAllocationSelector;

    private Tab tbResourceAllocation;

    private Tab workerSearchTab;

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
        allResourcesPerDay = new Decimalbox();
        makeReadyInputsForCalculationTypes();
        prepareCalculationTypesGrid();
    }

    private void makeReadyInputsForCalculationTypes() {
        final String width = "300px";
        taskEndDate.setWidth(width);
        assignedHoursComponent = new Intbox();
    }

    private void prepareCalculationTypesGrid() {
        calculationTypesGrid.setModel(new ListModelList(Arrays
                .asList(CalculationTypeRadio.values())));
        calculationTypesGrid.setRowRenderer(OnColumnsRowRenderer.create(
                calculationTypesRenderer(), Arrays.asList(0)));
    }

    private ICellForDetailItemRenderer<Integer, CalculationTypeRadio> calculationTypesRenderer() {
        return new ICellForDetailItemRenderer<Integer, CalculationTypeRadio>() {

            @Override
            public Component cellFor(Integer column, CalculationTypeRadio data) {
                return data.createRadio();
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
        try {
            if (formBinder != null) {
                formBinder.detach();
            }
            window.setTitle(task.getName());
            allocationRows = resourceAllocationModel.initAllocationsFor(
                    task, ganttTask, planningState);
            formBinder = allocationRows
                    .createFormBinder(resourceAllocationModel);
            formBinder.setAssignedHoursComponent(assignedHoursComponent);
            formBinder.setTaskStartDateBox(taskStartDateBox);
            formBinder.setEndDate(taskEndDate);
            formBinder.setAllResourcesPerDay(allResourcesPerDay);
            formBinder.setApplyButton(applyButton);
            formBinder.setAllocationsGrid(allocationsGrid);
            formBinder.setMessagesForUser(messagesForUser);
            formBinder.setWorkerSearchTab(workerSearchTab);
            formBinder.setCheckbox(recommendedAllocationCheckbox);
            CalculationTypeRadio calculationTypeRadio = CalculationTypeRadio
                    .from(formBinder.getCalculatedValue());
            calculationTypeRadio.doTheSelectionOn(calculationTypeSelector);
            tbResourceAllocation.setSelected(true);
            orderElementHoursGrid.setModel(new ListModelList(
                    resourceAllocationModel.getHoursAggregatedByCriterions()));
            orderElementHoursGrid.setRowRenderer(createOrderElementHoursRenderer());
            newAllocationSelector.setAllocationsAdder(resourceAllocationModel);
            showWindow();
        } catch (WrongValueException e) {
            LOG.error("there was a WrongValueException initializing window", e);
            throw e;
        }
    }

    public enum HoursRendererColumn {


        CRITERIONS {
            @Override
            public Component cell(HoursRendererColumn column,
                    AggregatedHoursGroup data) {
                return new Label(data.getCriterionsJoinedByComma());
            }
        },
        RESOURCE_TYPE{

            @Override
            public Component cell(HoursRendererColumn column,
                    AggregatedHoursGroup data) {
                return new Label(asString(data.getResourceType()));
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

        private static String asString(ResourceEnum resourceType) {
            switch (resourceType) {
            case RESOURCE:
                return _("Resource");
            case MACHINE:
                return _("Machine");
            case WORKER:
                return _("Worker");
            default:
                LOG.warn("no i18n for " + resourceType.name());
                return resourceType.name();
            }
        }

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
            Util.reloadBindings(allocationsGrid);
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
                return resourceAllocationController.allResourcesPerDay;
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

    public enum DerivedAllocationColumn implements IConvertibleToColumn {
        NAME("Name") {
            @Override
            public Component cellFor(DerivedAllocation data) {
                return new Label(data.getName());
            }
        },
        ALPHA("Alpha") {
            @Override
            public Component cellFor(DerivedAllocation data) {
                return new Label(String.format("%3.2f", data.getAlpha()));
            }
        },
        HOURS("Total Hours") {
            @Override
            public Component cellFor(DerivedAllocation data) {
                return new Label(data.getHours() + "");
            }
        };

        private final String name;

        private DerivedAllocationColumn(String name) {
            this.name = name;
        }

        @Override
        public org.zkoss.zul.api.Column toColumn() {
            return new Column(_(name));
        }

        public static void appendColumnsTo(Grid grid) {
            Columns columns = new Columns();
            grid.appendChild(columns);
            for (DerivedAllocationColumn each : values()) {
                columns.appendChild(each.toColumn());
            }
        }

        public static RowRenderer createRenderer() {
            return OnColumnsRowRenderer.create(cellRenderer, Arrays
                    .asList(DerivedAllocationColumn.values()));
        }

        private static final ICellForDetailItemRenderer<DerivedAllocationColumn, DerivedAllocation> cellRenderer= new ICellForDetailItemRenderer<DerivedAllocationColumn, DerivedAllocation>() {

                                @Override
                                public Component cellFor(
                                        DerivedAllocationColumn column,
                                        DerivedAllocation data) {
                                    return column.cellFor(data);
                                }
                            };

        abstract Component cellFor(DerivedAllocation data);
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

    public List<? extends Object> getResourceAllocations() {
        return formBinder != null ? plusAggregatingRow(formBinder
                .getCurrentRows()) : Collections
                .<AllocationRow> emptyList();
    }

    private List<Object> plusAggregatingRow(List<AllocationRow> currentRows) {
        List<Object> result = new ArrayList<Object>(currentRows);
        result.add(null);
        return result;
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
        allocationsGrid.setModel(new SimpleListModel(Collections.emptyList()));
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

    private class ResourceAllocationRenderer implements RowRenderer {

        @Override
        public void render(Row item, Object data) throws Exception {
            if (data instanceof AllocationRow) {
                AllocationRow row = (AllocationRow) data;
                renderResourceAllocation(item, row);
            } else {
                renderAggregatingRow(item);
            }
        }

        private void renderResourceAllocation(Row row, final AllocationRow data)
                throws Exception {
            row.setValue(data);
            append(row, data.createDetail());
            append(row, new Label(data.getName()));
            append(row, data.getHoursInput());
            append(row, data.getResourcesPerDayInput());
            // On click delete button
            Button deleteButton = appendDeleteButton(row);
            formBinder.setDeleteButtonFor(data, deleteButton);
            deleteButton.addEventListener("onClick", new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    removeAllocation(data);
                }
            });
        }

        private void renderAggregatingRow(Row row) {
            ResourceAllocationController controller = ResourceAllocationController.this;
            append(row, new Label());
            append(row, new Label(_("Sum of all rows")));
            append(row, CalculationTypeRadio.NUMBER_OF_HOURS.input(controller));
            append(row, CalculationTypeRadio.RESOURCES_PER_DAY
                    .input(controller));
            append(row, new Label());
        }

        private void removeAllocation(AllocationRow row) {
            allocationRows.remove(row);
            Util.reloadBindings(allocationsGrid);
        }

        private Button appendDeleteButton(Row row) {
            Button button = new Button();
            button.setSclass("icono");
            button.setImage("/common/img/ico_borrar1.png");
            button.setHoverImage("/common/img/ico_borrar.png");
            button.setTooltiptext(_("Delete"));
            return append(row, button);
        }

        private <T extends Component> T append(Row row, T component) {
            row.appendChild(component);
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
