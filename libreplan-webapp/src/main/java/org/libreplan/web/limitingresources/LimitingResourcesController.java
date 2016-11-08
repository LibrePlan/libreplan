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

package org.libreplan.web.limitingresources;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.GenericResourceAllocation;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.SpecificResourceAllocation;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.limiting.entities.LimitingResourceQueueDependency;
import org.libreplan.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.LimitingResourceQueue;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.web.common.Util;
import org.libreplan.web.limitingresources.LimitingResourcesPanel.IToolbarCommand;
import org.libreplan.web.planner.order.BankHolidaysMarker;
import org.libreplan.web.planner.taskedition.EditTaskController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.SeveralModifiers;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Window;
import org.zkoss.zul.Rows;

/**
 * Controller for limiting resources view.
 * Queue-based Resources Planning page.
 *
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LimitingResourcesController extends GenericForwardComposer<org.zkoss.zk.ui.Component> {

    @Autowired
    private ILimitingResourceQueueModel limitingResourceQueueModel;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    private List<IToolbarCommand> commands = new ArrayList<>();

    private LimitingResourcesPanel limitingResourcesPanel;

    private TimeTracker timeTracker;

    private Grid gridUnassignedLimitingResourceQueueElements;

    private Checkbox cbSelectAll;

    private Window manualAllocationWindow;

    private Window editTaskWindow;

    private final LimitingResourceQueueElementsRenderer limitingResourceQueueElementsRenderer =
                new LimitingResourceQueueElementsRenderer();

    public LimitingResourcesController() {
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        super.doAfterCompose(comp);
    }

    public void add(IToolbarCommand... commands) {
        Validate.noNullElements(commands);
        this.commands.addAll(Arrays.asList(commands));
    }

    public void reload() {
        transactionService.runOnReadOnlyTransaction(new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                reloadInTransaction();
                return null;
            }

            private void reloadInTransaction() {
                // FIXME: Temporary fix
                // It seems the page was already rendered, so clear it all as it's going to be rendered again
                self.getChildren().clear();

                limitingResourceQueueModel.initGlobalView();

                // Initialize interval
                timeTracker = buildTimeTracker();
                limitingResourcesPanel = buildLimitingResourcesPanel();

                self.appendChild(limitingResourcesPanel);
                limitingResourcesPanel.afterCompose();

                cbSelectAll = (Checkbox) limitingResourcesPanel.getFellowIfAny("cbSelectAll");

                initGridUnassignedLimitingResourceQueueElements();
                initManualAllocationWindow();
                initEditTaskWindow();

                addCommands(limitingResourcesPanel);
            }
        });
    }

    private void initGridUnassignedLimitingResourceQueueElements() {
        gridUnassignedLimitingResourceQueueElements =
                (Grid) limitingResourcesPanel.getFellowIfAny("gridUnassignedLimitingResourceQueueElements");

        gridUnassignedLimitingResourceQueueElements.setModel(
                new SimpleListModel<>(getUnassignedLimitingResourceQueueElements()));

        gridUnassignedLimitingResourceQueueElements.setRowRenderer(getLimitingResourceQueueElementsRenderer());
        getEarlierStartingDateColumn().sort(true, true);
    }

    private Column getEarlierStartingDateColumn() {
        return (Column) gridUnassignedLimitingResourceQueueElements.getColumns().getChildren().get(4);
    }

    private void initManualAllocationWindow() {
        manualAllocationWindow = (Window) limitingResourcesPanel.getFellowIfAny("manualAllocationWindow");
        ManualAllocationController manualAllocationController = getManualAllocationController();
        manualAllocationController.setLimitingResourcesController(this);
        manualAllocationController.setLimitingResourcesPanel(limitingResourcesPanel);
    }

    private ManualAllocationController getManualAllocationController() {
        return (ManualAllocationController) manualAllocationWindow.getAttribute("manualAllocationController", true);
    }

    private void initEditTaskWindow() {
        editTaskWindow = (Window) limitingResourcesPanel.getFellowIfAny("editTaskWindow");
    }

    public ILimitingResourceQueueModel getLimitingResourceQueueModel() {
        return limitingResourceQueueModel;
    }

    private void addCommands(LimitingResourcesPanel limitingResourcesPanel) {
        limitingResourcesPanel.add(commands.toArray(new IToolbarCommand[commands.size()]));
    }

    private TimeTracker buildTimeTracker() {
        timeTracker = new TimeTracker(
                limitingResourceQueueModel.getViewInterval(),
                ZoomLevel.DETAIL_THREE,
                SeveralModifiers.create(),
                SeveralModifiers.create(BankHolidaysMarker.create(getDefaultCalendar())),
                self);

        return timeTracker;
    }

    private BaseCalendar getDefaultCalendar() {
        return configurationDAO.getConfiguration().getDefaultCalendar();
    }

    private LimitingResourcesPanel buildLimitingResourcesPanel() {
        return new LimitingResourcesPanel(this, timeTracker);
    }

    /**
     * Returns unassigned {@link LimitingResourceQueueElement}.
     *
     * It's necessary to convert elements to a DTO that encapsulates properties
     * such as task name or order name, since the only way of sorting by these
     * fields is by having properties getTaskName or getOrderName on the elements returned.
     *
     * @return {@link List<LimitingResourceQueueElementDTO>}
     */
    public List<LimitingResourceQueueElementDTO> getUnassignedLimitingResourceQueueElements() {
        return limitingResourceQueueModel
                .getUnassignedLimitingResourceQueueElements()
                .stream()
                .map(this::toLimitingResourceQueueElementDTO)
                .collect(Collectors.toList());
    }

    private LimitingResourceQueueElementDTO toLimitingResourceQueueElementDTO(LimitingResourceQueueElement element) {
        final Task task = element.getResourceAllocation().getTask();
        final Order order = limitingResourceQueueModel.getOrderByTask(task);

        return new LimitingResourceQueueElementDTO(
                element,
                order.getName(),
                task.getName(),
                element.getEarliestStartDateBecauseOfGantt());
    }

    public static String getResourceOrCriteria(ResourceAllocation<?> resourceAllocation) {
        if ( resourceAllocation instanceof SpecificResourceAllocation ) {
            final Resource resource = ((SpecificResourceAllocation) resourceAllocation).getResource();

            return (resource != null) ? resource.getName() : "";

        } else if ( resourceAllocation instanceof GenericResourceAllocation ) {
            return Criterion.getCaptionFor((GenericResourceAllocation) resourceAllocation);
        }

        return StringUtils.EMPTY;
    }

    /**
     * DTO for list of unassigned {@link LimitingResourceQueueElement}.
     *
     * Note: this class has a natural ordering that is inconsistent with equals.
     *
     * @author Diego Pino Garcia <dpino@igalia.com>
     *
     */
    public static class LimitingResourceQueueElementDTO implements Comparable<LimitingResourceQueueElementDTO> {

        private LimitingResourceQueueElement original;

        private String orderName;

        private String taskName;

        private String date;

        private Integer hoursToAllocate;

        private String resourceOrCriteria;

        public LimitingResourceQueueElementDTO(LimitingResourceQueueElement element,
                                               String orderName,
                                               String taskName,
                                               Date date) {
            this.original = element;
            this.orderName = orderName;
            this.taskName = taskName;
            this.date = Util.formatDate(date);
            this.hoursToAllocate = element.getIntentedTotalHours();

            this.resourceOrCriteria =
                    LimitingResourcesController.getResourceOrCriteria(element.getResourceAllocation());
        }

        public LimitingResourceQueueElement getOriginal() {
            return original;
        }

        public String getOrderName() {
            return orderName;
        }

        public String getTaskName() {
            return taskName;
        }

        public String getDate() {
            return date;
        }

        public Integer getHoursToAllocate() {
            return (hoursToAllocate != null) ? hoursToAllocate : 0;
        }

        public String getResourceOrCriteria() {
            return resourceOrCriteria;
        }

        @Override
        public int compareTo(LimitingResourceQueueElementDTO dto) {
            return getOriginal().getId().compareTo(dto.getOriginal().getId());
        }

    }

    public void saveQueues() {
        limitingResourceQueueModel.confirm();
        notifyUserThatSavingIsDone();
    }

    private void notifyUserThatSavingIsDone() {
        Messagebox.show(_("Scheduling saved"), _("Information"), Messagebox.OK, Messagebox.INFORMATION);
    }

    public void editResourceAllocation(LimitingResourceQueueElement oldElement) {

        try {
            Task task = oldElement.getTask();

            EditTaskController editTaskController = getEditController();
            editTaskController.showEditFormResourceAllocationFromLimitingResources(task);

            // New resource allocation or resource allocation modified
            if ( editTaskController.getStatus() == Messagebox.OK ) {

                // Update resource allocation for element
                LimitingResourceQueueElement newElement = copyFrom(oldElement, getQueueElementFrom(task));

                // Replace old limiting resource with new one
                LimitingResourceQueue oldQueue = oldElement.getLimitingResourceQueue();

                List<LimitingResourceQueueElement> modified =
                        limitingResourceQueueModel.replaceLimitingResourceQueueElement(oldElement, newElement);

                // Refresh modified queues
                Set<LimitingResourceQueue> toRefreshQueues = new HashSet<>();
                toRefreshQueues.addAll(LimitingResourceQueue.queuesOf(modified));
                if ( oldQueue != null ) {
                    toRefreshQueues.add(oldQueue);
                }

                limitingResourcesPanel.refreshQueues(toRefreshQueues);
            }
        } catch (SuspendNotAllowedException e) {
            e.printStackTrace();
        }
    }

    private LimitingResourceQueueElement getQueueElementFrom(Task task) {
        return task.getResourceAllocation().getLimitingResourceQueueElement();
    }

    /**
     * Copies earliestStartDateBecauseOfGantt and dependencies from source to destination.
     *
     * @param source
     * @param destination
     * @return {@link LimitingResourceQueueElement}
     */
    private LimitingResourceQueueElement copyFrom(LimitingResourceQueueElement source,
                                                  LimitingResourceQueueElement destination) {

        destination.setEarlierStartDateBecauseOfGantt(source.getEarliestStartDateBecauseOfGantt());

        for (LimitingResourceQueueDependency each : source.getDependenciesAsOrigin()) {
            each.setOrigin(destination);
            destination.add(each);
        }
        for (LimitingResourceQueueDependency each : source.getDependenciesAsDestiny()) {
            each.setDestiny(destination);
            destination.add(each);
        }

        return destination;
    }

    private EditTaskController getEditController() {
        return (EditTaskController) editTaskWindow.getAttribute("editController", true);
    }

    public LimitingResourceQueueElementsRenderer getLimitingResourceQueueElementsRenderer() {
        return limitingResourceQueueElementsRenderer;
    }

    private class LimitingResourceQueueElementsRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object o, int i) throws Exception {
            LimitingResourceQueueElementDTO element = (LimitingResourceQueueElementDTO) o;

            row.setValue(o);

            row.appendChild(automaticQueueing());
            row.appendChild(label(element.getOrderName()));
            row.appendChild(label(element.getTaskName()));
            row.appendChild(label(element.getResourceOrCriteria()));
            row.appendChild(label(element.getDate()));
            row.appendChild(label(element.getHoursToAllocate().toString()));
            row.appendChild(operations(element));
        }

        private Hbox operations(LimitingResourceQueueElementDTO element) {
            Hbox hbox = new Hbox();
            hbox.appendChild(editResourceAllocationButton(element));
            hbox.appendChild(automaticButton(element));
            hbox.appendChild(manualButton(element));
            hbox.appendChild(removeButton(element));

            return hbox;
        }

        private Button editResourceAllocationButton(final LimitingResourceQueueElementDTO element) {
            Button result = new Button("", "/common/img/ico_editar1.png");
            result.setHoverImage("/common/img/ico_editar.png");
            result.setSclass("icono");
            result.setTooltiptext(_("Edit queue-based resource element"));

            result.addEventListener(Events.ON_CLICK, event -> {
                LimitingResourceQueueElement queueElement = element.getOriginal();

                editResourceAllocation(queueElement);
                if ( queueElement.getLimitingResourceQueue() == null ) {
                    reloadUnassignedLimitingResourceQueueElements();
                }
            });

            return result;
        }

        private Button manualButton(final LimitingResourceQueueElementDTO element) {
            Button result = new Button();
            result.setLabel(_("Manual"));
            result.setClass("add-button");
            result.setTooltiptext(_("Assign element to queue manually"));

            result.addEventListener(Events.ON_CLICK, event -> showManualAllocationWindow(element.getOriginal()));

            return result;
        }

        private Button removeButton(final LimitingResourceQueueElementDTO element) {
            Button result = new Button("", "/common/img/ico_borrar1.png");
            result.setHoverImage("/common/img/ico_borrar.png");
            result.setSclass("icono");
            result.setTooltiptext(_("Remove queue-based resource element"));

            result.addEventListener(Events.ON_CLICK, event -> removeUnassignedLimitingResourceQueueElement(element));

            return result;
        }

        private void removeUnassignedLimitingResourceQueueElement(LimitingResourceQueueElementDTO dto) {
            LimitingResourceQueueElement element = dto.getOriginal();
            limitingResourceQueueModel.removeUnassignedLimitingResourceQueueElement(element);
            reloadUnassignedLimitingResourceQueueElements();
        }

        private Button automaticButton(final LimitingResourceQueueElementDTO element) {
            Button result = new Button();
            result.setLabel(_("Automatic"));
            result.setClass("add-button");
            result.setTooltiptext(_("Assign element to queue automatically"));
            result.setStyle("margin-right: 5px");

            result.addEventListener(Events.ON_CLICK, event -> assignLimitingResourceQueueElement(element));

            return result;
        }

        private void assignLimitingResourceQueueElement(LimitingResourceQueueElementDTO dto) {
            List<LimitingResourceQueueElement> inserted =
                    limitingResourceQueueModel.assignLimitingResourceQueueElement(dto.getOriginal());

            if ( inserted.isEmpty() ) {
                showErrorMessage(
                        _("Cannot allocate selected element. There is not any queue " +
                                "that matches resource allocation criteria at any interval of time"));
                return;
            }

            limitingResourcesPanel.refreshQueues(LimitingResourceQueue.queuesOf(inserted));
            reloadUnassignedLimitingResourceQueueElements();
        }

        private Checkbox automaticQueueing() {
            Checkbox result = new Checkbox();
            result.setTooltiptext(_("Select for automatic queuing"));
            result.setStyle("margin-left: 2px");

            return result;
        }

        private Label label(String value) {
            return new Label(value);
        }
    }

    public List<LimitingResourceQueue> getLimitingResourceQueues() {
        return limitingResourceQueueModel.getLimitingResourceQueues();
    }

    public void unschedule(QueueTask task) {
        LimitingResourceQueueElement queueElement = task.getLimitingResourceQueueElement();
        LimitingResourceQueue queue = queueElement.getLimitingResourceQueue();

        limitingResourceQueueModel.unschedule(queueElement);
        limitingResourcesPanel.refreshQueue(queue);
        reloadUnassignedLimitingResourceQueueElements();
    }

    public boolean moveTask(LimitingResourceQueueElement element) {
        showManualAllocationWindow(element);
        return getManualAllocationWindowStatus() == Messagebox.OK;
    }

    private void showManualAllocationWindow(LimitingResourceQueueElement element) {
        getManualAllocationController().show(element);
    }

    public int getManualAllocationWindowStatus() {
        Integer status = getManualAllocationController().getStatus();
        return (status != null) ? status : -1;
    }

    public void reloadUnassignedLimitingResourceQueueElements() {
        gridUnassignedLimitingResourceQueueElements.setModel(
                new SimpleListModel<>(getUnassignedLimitingResourceQueueElements()));
    }

    public void selectedAllUnassignedQueueElements() {
        final boolean value = cbSelectAll.isChecked();

        final Rows rows = gridUnassignedLimitingResourceQueueElements.getRows();

        for (Object each: rows.getChildren()) {
            final Row row = (Row) each;
            Checkbox cbAutoQueueing = getAutoQueueing(row);
            cbAutoQueueing.setChecked(value);
        }
    }

    @SuppressWarnings("unchecked")
    private Checkbox getAutoQueueing(Row row) {
        return (Checkbox) row.getChildren().get(0);
    }

    public void assignAllSelectedElements() {
        List<LimitingResourceQueueElement> elements = getAllSelectedQueueElements();

        if ( !elements.isEmpty() ) {

            Set<LimitingResourceQueueElement> inserted =
                    limitingResourceQueueModel.assignLimitingResourceQueueElements(elements);

            clearSelectAllCheckbox();

            if ( inserted.isEmpty() ) {

                showErrorMessage(_("Cannot allocate selected element. There is not any queue " +
                        "that matches resource allocation criteria at any interval of time"));
                return;
            }

            limitingResourcesPanel.refreshQueues(LimitingResourceQueue.queuesOf(inserted));
            reloadUnassignedLimitingResourceQueueElements();
        }
    }

    private void clearSelectAllCheckbox() {
        cbSelectAll.setChecked(false);
    }

    private List<LimitingResourceQueueElement> getAllSelectedQueueElements() {
        List<LimitingResourceQueueElement> result = new ArrayList<>();

        final Rows rows = gridUnassignedLimitingResourceQueueElements.getRows();

        for (Object each : rows.getChildren()) {
            final Row row = (Row) each;
            Checkbox cbAutoQueueing = getAutoQueueing(row);

            if ( cbAutoQueueing.isChecked() ) {
                LimitingResourceQueueElementDTO dto = row.getValue();
                result.add(dto.getOriginal());
            }
        }

        return result;
    }

    private void showErrorMessage(String error) {
        Messagebox.show(error, _("Error"), Messagebox.OK, Messagebox.ERROR);
    }
}
