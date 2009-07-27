package org.navalplanner.web.planner;

import java.math.BigDecimal;
import java.util.Set;

import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.Util;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;
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

    private ResourceAllocationListitemRender resourceAllocationRenderer = new ResourceAllocationListitemRender();

    private Listbox resourcesList;

    private Window window;

    public Set<Criterion> getCriterions() {
        return resourceAllocationModel.getCriterions();
    }

    public Set<ResourceAllocation> getResourceAllocations() {
        return resourceAllocationModel.getResourceAllocations();
    }

    public ResourceAllocationListitemRender getResourceAllocationRenderer() {
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


    public void showWindow(Task task) {
        resourceAllocationModel.setTask(task);
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
                        "Worker not valid in some resource allocation");
            }
        }

        Clients.closeErrorBox(window.getFellow("resourcesList"));
        window.setVisible(false);
    }

    /**
     * Renders every {@link ResourceAllocation} showing a form to modify its
     * information.
     *
     * @author Manuel Rego Casasnovas <mrego@igalia.com>
     */
    public class ResourceAllocationListitemRender implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            final ResourceAllocation resourceAllocation = (ResourceAllocation) data;
            item.setValue(resourceAllocation);

            resourceAllocationModel.setResourceAllocation(resourceAllocation);

            final Worker worker = resourceAllocationModel.getWorker();

            Listcell cellResource = new Listcell();
            final Textbox resourceTextbox = new Textbox();
            Util.bind(
                resourceTextbox, new Util.Getter<String>() {

                    @Override
                public String get() {
                    if (worker == null) {
                        return "";
                        }
                        return worker.getNif();
                }
            }, new Util.Setter<String>() {

                    @Override
                public void set(String value) {
                    Worker worker = resourceAllocationModel
                            .findWorkerByNif(value);
                    if (worker == null) {
                        throw new WrongValueException(resourceTextbox,
                                "Worker not found");
                    } else {
                        resourceAllocationModel
                                .setWorker(
                                        (SpecificResourceAllocation) resourceAllocation,
                                        worker);
                        }
                    }
            });
            resourceTextbox.addEventListener(Events.ON_CHANGE,
                    new EventListener() {

                        @Override
                        public void onEvent(Event event) throws Exception {
                            Util.reloadBindings(resourcesList);
                        }
                    });
            cellResource.appendChild(resourceTextbox);
            cellResource.setParent(item);

            Listcell cellPercentage = new Listcell();
            cellPercentage.appendChild(Util.bind(
                    new Decimalbox(),
                    new Util.Getter<BigDecimal>() {

                        @Override
                        public BigDecimal get() {
                            return resourceAllocation.getPercentage();
                        }
                    }, new Util.Setter<BigDecimal>() {

                        @Override
                        public void set(BigDecimal value) {
                            resourceAllocation.setPercentage(value);
                        }
                    }));
            cellPercentage.setParent(item);

            Listcell cellMessage = new Listcell();
            String message = "";

            if (worker != null) {
                if (!resourceAllocationModel.workerSatisfiesCriterions()) {
                    message = "The worker does not satisfy the criterions";
                }
            }

            cellMessage.appendChild(new Label(message));
            cellMessage.setParent(item);
        }
    }

}
