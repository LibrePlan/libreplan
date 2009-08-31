package org.navalplanner.web.planner;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.WorkerSearch;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.api.Window;

/**
 * Controller for {@link ResourceAllocation} view.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@org.springframework.stereotype.Component("resourceAllocationController")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResourceAllocationController extends GenericForwardComposer {

    private IResourceAllocationModel resourceAllocationModel;

    private ResourceAllocationRenderer resourceAllocationRenderer = new ResourceAllocationRenderer();

    private Listbox resourcesList;

    private Window window;

    public Set<Criterion> getCriterions() {
        Set<Criterion> criterions = resourceAllocationModel.getCriterions();
        if (criterions.isEmpty()) {
            window.getFellow("requiredCriterions").setVisible(false);
            window.getFellow("requiredCriterionsEmpty").setVisible(true);
        } else {
            window.getFellow("requiredCriterionsEmpty").setVisible(false);
            window.getFellow("requiredCriterions").setVisible(true);
        }

        return criterions;
    }

    public Set<ResourceAllocation> getResourceAllocations() {
        return resourceAllocationModel.getResourceAllocations();
    }

    public ResourceAllocationRenderer getResourceAllocationRenderer() {
        return resourceAllocationRenderer;
    }

    public void addResourceAllocation() {
        resourceAllocationModel.addResourceAllocation();
        Util.reloadBindings(resourcesList);
    }

    public void removeResourceAllocation() {
        Set<Listitem> selectedItems = resourcesList.getSelectedItems();
        for (Listitem listitem : selectedItems) {
            ResourceAllocation resourceAllocation = (ResourceAllocation) listitem
                    .getValue();
            resourceAllocationModel.removeResourceAllocation(resourceAllocation);
        }
        Util.reloadBindings(resourcesList);
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.window = (Window) comp;
    }


    public void showWindow(Task task, org.zkoss.ganttz.data.Task ganttTask) {
        resourceAllocationModel.setTask(task);
        resourceAllocationModel.setGanttTask(ganttTask);
        Util.reloadBindings(window);
        try {
            window.doModal();
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void back() {
        Set<ResourceAllocation> resourceAllocations = resourceAllocationModel.getResourceAllocations();
        for (ResourceAllocation resourceAllocation : resourceAllocations) {
            if (((SpecificResourceAllocation) resourceAllocation).getWorker() == null) {
                throw new WrongValueException(
                        window.getFellow("resourcesList"),
                        _("Worker not valid in some resource allocation"));
            }
        }

        if (!resourceAllocationModel.getTask()
                .isValidResourceAllocationWorkers()) {
            throw new WrongValueException(window.getFellow("resourcesList"),
                    _("There is some Worker assigned twice (or more)"));
        }

        Clients.closeErrorBox(window.getFellow("resourcesList"));

        resourceAllocationModel.updateGanttTaskDuration();

        window.setVisible(false);
    }

    public void showSearchResources(Event e) {
        WorkerSearch workerSearch = new WorkerSearch();
        workerSearch.setParent(self.getParent());
        workerSearch.afterCompose();

        Window window = workerSearch.getWindow();
        try {
            window.doModal();
        } catch (SuspendNotAllowedException e1) {
            e1.printStackTrace();
            return;
        } catch (InterruptedException e1) {
            e1.printStackTrace();
            return;
        }

        // Get selected workers and add specificResourceAllocations
        List<Worker> workers = workerSearch.getWorkers();
        for (Worker worker : workers) {
            resourceAllocationModel.addSpecificResourceAllocation(worker);
        }

        Util.reloadBindings(resourcesList);
    }

    /**
     *
     * Renders a {@link SpecificResourceAllocation} item
     *
     * @author Diego Pino Garcia <dpino@igalia.com>
     *
     */
    private class ResourceAllocationRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            final SpecificResourceAllocation resourceAllocation = (SpecificResourceAllocation) data;

            item.setValue(resourceAllocation);

            // Label fields are fixed, can only be viewed
            appendLabel(item, resourceAllocation.getWorker().getName());
            appendLabel(item, resourceAllocation.getWorker().getNif());
            // Pecentage field is editable
            bindPercentage(appendDecimalbox(item), resourceAllocation);
            // On click delete button
            appendButton(item, _("Delete")).addEventListener("onClick",
                    new EventListener() {

                        @Override
                        public void onEvent(Event event) throws Exception {
                            resourceAllocationModel
                                    .removeResourceAllocation(resourceAllocation);
                            Util.reloadBindings(resourcesList);
                        }
                    });
        }

        /**
         * Appends {@link Label} to {@link Listitem}
         *
         * @param listitem
         * @param name value for {@link Label}
         */
        private void appendLabel(Listitem listitem, String name) {
            Label label = new Label(name);

            Listcell listCell = new Listcell();
            listCell.appendChild(label);
            listitem.appendChild(listCell);
        }

        /**
         * Appends {@link Button} to {@link Listitem}
         *
         * @param listitem
         * @param label value for {@link Button}
         * @return
         */
        private Button appendButton(Listitem listitem, String label) {
            Button button = new Button(label);

            Listcell listCell = new Listcell();
            listCell.appendChild(button);
            listitem.appendChild(listCell);

            return button;
        }

        /**
         * Append a Textbox @{link Percentage} to listItem
         *
         * @param listItem
         */
        private Decimalbox appendDecimalbox(Listitem item) {
            Decimalbox decimalbox = new Decimalbox();

            // Insert textbox in listcell and append to listItem
            Listcell listCell = new Listcell();
            listCell.appendChild(decimalbox);
            item.appendChild(listCell);

            return decimalbox;
        }

        /**
         * Binds Textbox @{link Percentage} to a {@link ResourceAllocation}
         * {@link Percentage}
         *
         * @param txtPercentage
         * @param resourceAllocation
         */
        private void bindPercentage(final Decimalbox decimalbox,
                final ResourceAllocation resourceAllocation) {
            Util.bind(decimalbox, new Util.Getter<BigDecimal>() {

                @Override
                public BigDecimal get() {
                    return resourceAllocation.getPercentage().scaleByPowerOfTen(2);
                }

            }, new Util.Setter<BigDecimal>() {

                @Override
                public void set(BigDecimal value) {
                    resourceAllocation
                            .setPercentage(value.setScale(2).divide(
                            new BigDecimal(100), BigDecimal.ROUND_DOWN));
                }
            });
        }
    }
}
