package org.navalplanner.web.planner;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.WorkerSearch;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.InputEvent;
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
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@org.springframework.stereotype.Component("resourceAllocationController")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResourceAllocationController extends GenericForwardComposer {

    private IResourceAllocationModel resourceAllocationModel;

    private ResourceAllocationRenderer resourceAllocationRenderer = new ResourceAllocationRenderer();

    private Component messagesContainer;

    private IMessagesForUser messagesForUser;

    private Listbox resourcesList;

    private Decimalbox genericResourceAllocationPercentage;

    private Window window;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.window = (Window) comp;
        messagesForUser = new MessagesForUser(messagesContainer);
    }

    /**
     * Shows Resource Allocation window
     *
     * @param task
     * @param ganttTask
     */
    public void showWindow(Task task, org.zkoss.ganttz.data.Task ganttTask) {
        resourceAllocationModel.setTask(task);
        resourceAllocationModel.setGanttTask(ganttTask);

        updateGenericResourceAllocationPercentage();
        // Add new generic resources to resources list
        addGenericResources();
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
     * GenericResourceAllocationPercentage initial value is the sum of all
     * {@link ResourceAllocation} percentages
     */
    private void updateGenericResourceAllocationPercentage() {
        BigDecimal genericPercentage = resourceAllocationModel
                .getSumPercentageResourceAllocations();
        genericResourceAllocationPercentage.setValue(genericPercentage
                .scaleByPowerOfTen(2).setScale(2, BigDecimal.ROUND_HALF_EVEN));
    }

    /**
     * Check how many {@link ResourceAllocation} object can be assigned to this
     * {@link Task} and add them to {@link ResourceAllocation} list
     */
    private void addGenericResources() {
        int n = resourceAllocationModel.getNumberUnassignedResources();
        for (int i = 0; i < n; i++) {
            resourceAllocationModel.addGenericResourceAllocation();
        }
    }

    /**
     * Shows WorkerSearch window, add picked workers as
     * {@link SpecificResourceAllocation} to {@link ResourceAllocation} list
     *
     * @return
     */
    public void showSearchResources() {
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

        addSpecificResourceAllocations(workerSearch.getWorkers());

        Util.reloadBindings(resourcesList);
    }

    /**
     * Adds a list of {@link Worker} to {@link ResourceAllocation} list
     *
     * @param workers
     */
    private void addSpecificResourceAllocations(List<Worker> workers) {
        for (Worker worker : workers) {
            addSpecificResourceAllocation(worker);
        }
        updateGenericPercentages();
    }

    /**
     * Update percentages of {@link GenericResourceAllocation} items when
     * genericResourceAllocationPercentage box is changed
     *
     * @param e
     */
    public void updateGenericPercentages(InputEvent e) {
        updateGenericPercentages(new BigDecimal((String) e.getValue()));
    }

    public void updateGenericPercentages() {
        updateGenericPercentages(genericResourceAllocationPercentage.getValue());
    }

    private void updateGenericPercentages(BigDecimal genericPercentage) {
        if (genericPercentage != null) {
            resourceAllocationModel.updateGenericPercentages(genericPercentage
                    .divide(new BigDecimal(100)));
        }
        Util.reloadBindings(resourcesList);
    }

    private void addSpecificResourceAllocation(Worker worker) {
        try {
            resourceAllocationModel.addSpecificResourceAllocation(worker);
        } catch (Exception e1) {
            messagesForUser.showMessage(Level.ERROR, e1.getMessage());
        }
    }

    /**
     * Returns list of {@link Criterion} separated by comma
     *
     * @return
     */
    public String getTaskCriterions() {
        Set<String> criterionNames = new HashSet<String>();

        Set<Criterion> criterions = resourceAllocationModel.getCriterions();
        for (Criterion criterion : criterions) {
            criterionNames.add(criterion.getName());
        }

        return StringUtils.join(criterionNames, ",");
    }

    /**
     * Returns hours of {@link Task}
     *
     * @return
     */
    public String getTaskHours() {
        Task task = resourceAllocationModel.getTask();
        return (task != null && task.getHours() != null) ? task.getHours()
                .toString() : "";
    }

    /**
     * Returns type of {@link Task} based on value of fixedDuration attribute
     *
     * @return
     */
    public String getTaskType() {
        Task task = resourceAllocationModel.getTask();
        return (task != null && task.getFixedDuration()) ? _("Fixed duration")
                : _("Variable duration");
    }

    public Set<ResourceAllocation> getResourceAllocations() {
        return resourceAllocationModel.getResourceAllocations();
    }

    public ResourceAllocationRenderer getResourceAllocationRenderer() {
        return resourceAllocationRenderer;
    }

    public void cancel() {
        close();
        resourceAllocationModel.cancel();
    }

    private void close() {
        self.setVisible(false);
        clear();
    }

    private void clear() {
        genericResourceAllocationPercentage.setValue(null);
        resourcesList.getItems().clear();
    }

    public void save() {
        resourceAllocationModel.save();
        close();
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
            if (data instanceof SpecificResourceAllocation) {
                renderSpecificResourceAllocation(item,
                        (SpecificResourceAllocation) data);
            }
            if (data instanceof GenericResourceAllocation) {
                renderGenericResourceAllocation(item,
                        (GenericResourceAllocation) data);
            }
        }

        private void renderSpecificResourceAllocation(Listitem item,
                final SpecificResourceAllocation resourceAllocation)
                throws Exception {
            item.setValue(resourceAllocation);

            // Label fields are fixed, can only be viewed
            appendLabel(item, resourceAllocation.getWorker().getName());
            // appendLabel(item, resourceAllocation.getWorker().getNif());
            // Percentage field is editable
            bindPercentage(appendDecimalbox(item), resourceAllocation);
            // On click delete button
            appendButton(item, _("Delete")).addEventListener("onClick",
                    new EventListener() {

                        @Override
                        public void onEvent(Event event) throws Exception {
                            removeSpecificResourceAllocation(resourceAllocation);
                        }
                    });
        }

        private void removeSpecificResourceAllocation(
                SpecificResourceAllocation resourceAllocation) {
            resourceAllocationModel
                    .removeSpecificResourceAllocation(resourceAllocation);
            updateGenericPercentages();
            Util.reloadBindings(resourcesList);
        }

        private void renderGenericResourceAllocation(Listitem item,
                final GenericResourceAllocation resourceAllocation)
                throws Exception {
            item.setValue(resourceAllocation);

            // Set name
            appendLabel(item, _("Generic"));
            // Set percentage
            BigDecimal percentage = resourceAllocation.getPercentage();
            if (!new BigDecimal(0).equals(resourceAllocation.getPercentage())) {
                percentage = percentage.scaleByPowerOfTen(2).setScale(2,
                        BigDecimal.ROUND_CEILING);
            }
            appendLabel(item, percentage.toString());
            // No buttons
            appendLabel(item, "");
        }

        /**
         * Appends {@link Label} to {@link Listitem}
         *
         * @param listitem
         * @param name
         *            value for {@link Label}
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
         * @param label
         *            value for {@link Button}
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
                    return (resourceAllocation.getPercentage() != null) ? resourceAllocation
                            .getPercentage().scaleByPowerOfTen(2)
                            : new BigDecimal(0);
                }

            }, new Util.Setter<BigDecimal>() {

                @Override
                public void set(BigDecimal value) {
                    if (value != null) {
                        value = value.setScale(2).divide(new BigDecimal(100),
                                BigDecimal.ROUND_DOWN);
                        updateGenericPercentages();
                        decimalbox.setValue(value);
                    }
                }
            });
        }
    }
}
