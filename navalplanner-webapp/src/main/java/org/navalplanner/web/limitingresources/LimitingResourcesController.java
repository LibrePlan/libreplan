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

package org.navalplanner.web.limitingresources;

import static org.navalplanner.web.I18nHelper._;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.DateAndHour;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.LimitingResourceAllocator;
import org.navalplanner.business.planner.entities.LimitingResourceQueueElement;
import org.navalplanner.business.planner.entities.LimitingResourceQueueElementGap;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.limitingresources.LimitingResourcesPanel.IToolbarCommand;
import org.navalplanner.web.planner.order.BankHolidaysMarker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.resourceload.IFilterChangedListener;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.SeveralModificators;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Window;

/**
 * Controller for limiting resources view
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LimitingResourcesController implements Composer {

    @Autowired
    private ILimitingResourceQueueModel limitingResourceQueueModel;

    private List<IToolbarCommand> commands = new ArrayList<IToolbarCommand>();

    private Order filterBy;

    private org.zkoss.zk.ui.Component parent;

    private LimitingResourcesPanel limitingResourcesPanel;

    private TimeTracker timeTracker;

    private Grid gridUnassignedLimitingResourceQueueElements;

    private Window manualAllocationWindow;

    private Radiogroup radioAllocationDate;

    private Datebox startAllocationDate;

    private Map<LimitingResourceQueueElementGap, DateAndHour> endAllocationDates = new HashMap<LimitingResourceQueueElementGap, DateAndHour>();

    private final LimitingResourceQueueElementsRenderer limitingResourceQueueElementsRenderer =
        new LimitingResourceQueueElementsRenderer();

    private final QueueRenderer queueRenderer = new QueueRenderer();

    private final CandidateGapRenderer candidateGapRenderer = new CandidateGapRenderer();

        private transient IFilterChangedListener filterChangedListener;

    public LimitingResourcesController() {
    }

    public void add(IToolbarCommand... commands) {
        Validate.noNullElements(commands);
        this.commands.addAll(Arrays.asList(commands));
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        this.parent = comp;
        reload();
    }

    public void reload() {
        // by default show the task by resources
        boolean filterByResources = true;
        reload(filterByResources);
    }

    private Listbox listAssignableQueues;

    private Listbox listCandidateGaps;

    private void reload(boolean filterByResources) {
        try {

            if (filterBy == null) {
                limitingResourceQueueModel.initGlobalView(filterByResources);
            } else {
                limitingResourceQueueModel.initGlobalView(filterBy,
                        filterByResources);
            }

            // Initialize interval
            timeTracker = buildTimeTracker();
            limitingResourceQueueModel.setTimeTrackerState(timeTracker
                    .getDetailLevel());

            limitingResourcesPanel = buildLimitingResourcesPanel();
            addListeners();

            this.parent.getChildren().clear();
            this.parent.appendChild(limitingResourcesPanel);
            limitingResourcesPanel.afterCompose();

            gridUnassignedLimitingResourceQueueElements = (Grid) limitingResourcesPanel
                    .getFellowIfAny("gridUnassignedLimitingResourceQueueElements");
            manualAllocationWindow = (Window) limitingResourcesPanel.getFellowIfAny("manualAllocationWindow");
            listAssignableQueues = (Listbox) manualAllocationWindow.getFellowIfAny("listAssignableQueues");
            listCandidateGaps = (Listbox) manualAllocationWindow.getFellowIfAny("listCandidateGaps");
            radioAllocationDate = (Radiogroup) manualAllocationWindow.getFellowIfAny("radioAllocationDate");
            startAllocationDate = (Datebox) manualAllocationWindow.getFellowIfAny("startAllocationDate");

            addCommands(limitingResourcesPanel);
        } catch (IllegalArgumentException e) {
            try {
                e.printStackTrace();
                Messagebox.show(_("Limiting resources error") + e, _("Error"),
                        Messagebox.OK, Messagebox.ERROR);
            } catch (InterruptedException o) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addListeners() {
        filterChangedListener = new IFilterChangedListener() {

            @Override
            public void filterChanged(boolean filter) {
                onApplyFilter(filter);
            }
        };
        // this.limitingResourcesPanel.addFilterListener(filterChangedListener);
    }

    public void onApplyFilter(boolean filterByResources) {
        limitingResourcesPanel.clearComponents();
        reload(filterByResources);
    }

    private void addCommands(LimitingResourcesPanel limitingResourcesPanel) {
        limitingResourcesPanel.add(commands.toArray(new IToolbarCommand[0]));
    }

    private TimeTracker buildTimeTracker() {
        return timeTracker = new TimeTracker(limitingResourceQueueModel
                .getViewInterval(), ZoomLevel.DETAIL_THREE, SeveralModificators
                .create(),
                SeveralModificators.create(new BankHolidaysMarker()), parent);
    }

    private LimitingResourcesPanel buildLimitingResourcesPanel() {
        return new LimitingResourcesPanel(this, timeTracker);
    }

    /**
     * Returns unassigned {@link LimitingResourceQueueElement}
     *
     * It's necessary to convert elements to a DTO that encapsulates properties
     * such as task name or order name, since the only way of sorting by these
     * fields is by having properties getTaskName or getOrderName on the
     * elements returned
     *
     * @return
     */
    public List<LimitingResourceQueueElementDTO> getUnassignedLimitingResourceQueueElements() {
        List<LimitingResourceQueueElementDTO> result = new ArrayList<LimitingResourceQueueElementDTO>();
        for (LimitingResourceQueueElement each : limitingResourceQueueModel
                .getUnassignedLimitingResourceQueueElements()) {
            result.add(toLimitingResourceQueueElementDTO(each));
        }
        return result;
    }

    private LimitingResourceQueueElementDTO toLimitingResourceQueueElementDTO(
            LimitingResourceQueueElement element) {
        final Task task = element.getResourceAllocation().getTask();
        final Order order = limitingResourceQueueModel.getOrderByTask(task);
        return new LimitingResourceQueueElementDTO(element, order
                .getName(), task.getName(), element
                .getEarlierStartDateBecauseOfGantt());
    }

    /**
     * DTO for list of unassigned {@link LimitingResourceQueueElement}
     *
     * @author Diego Pino Garcia <dpino@igalia.com>
     *
     */
    public class LimitingResourceQueueElementDTO {

        private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

        private LimitingResourceQueueElement original;

        private String orderName;

        private String taskName;

        private String date;

        private Integer hoursToAllocate;

        private String resourceOrCriteria;

        public LimitingResourceQueueElementDTO(
                LimitingResourceQueueElement element, String orderName,
                String taskName, Date date) {
            this.original = element;
            this.orderName = orderName;
            this.taskName = taskName;
            this.date = DATE_FORMAT.format(date);
            this.hoursToAllocate = element.getIntentedTotalHours();
            this.resourceOrCriteria = getResourceOrCriteria(element.getResourceAllocation());
        }

        private String getResourceOrCriteria(ResourceAllocation<?> resourceAllocation) {
            if (resourceAllocation instanceof SpecificResourceAllocation) {
                final Resource resource = ((SpecificResourceAllocation) resourceAllocation)
                        .getResource();
                return (resource != null) ? resource.getName() : "";
            } else if (resourceAllocation instanceof GenericResourceAllocation) {
                Set<Criterion> criteria = ((GenericResourceAllocation) resourceAllocation).getCriterions();
                return Criterion.getNames(criteria);
            }
            return StringUtils.EMPTY;
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

    }

    public void filterBy(Order order) {
        this.filterBy = order;
    }

    public void saveQueues() {
        limitingResourceQueueModel.confirm();
        notifyUserThatSavingIsDone();
    }

    private void notifyUserThatSavingIsDone() {
        try {
            Messagebox.show(_("Scheduling saved"), _("Information"), Messagebox.OK,
                    Messagebox.INFORMATION);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public LimitingResourceQueueElementsRenderer getLimitingResourceQueueElementsRenderer() {
        return limitingResourceQueueElementsRenderer;
    }

    private class LimitingResourceQueueElementsRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) throws Exception {
            LimitingResourceQueueElementDTO element = (LimitingResourceQueueElementDTO) data;

            row.appendChild(label(element.getOrderName()));
            row.appendChild(label(element.getTaskName()));
            row.appendChild(label(element.getResourceOrCriteria()));
            row.appendChild(label(element.getDate()));
            row.appendChild(label(element.getHoursToAllocate().toString()));
            row.appendChild(operations(element));
            row.appendChild(automaticQueueing(element));
        }

        private Hbox operations(LimitingResourceQueueElementDTO element) {
            Hbox hbox = new Hbox();
            hbox.appendChild(assignButton(element));
            hbox.appendChild(removeButton(element));
            hbox.appendChild(manualButton(element));
            return hbox;
        }

        private Button manualButton(final LimitingResourceQueueElementDTO element) {
            Button result = new Button();
            result.setLabel(_("Manual"));
            result.setTooltiptext(_("Manual allocation"));
            result.addEventListener(Events.ON_CLICK, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    manualAllocateLimitingResourceQueueElement(element);
                }
            });
            return result;
        }

        private Button removeButton(final LimitingResourceQueueElementDTO element) {
            Button result = new Button();
            result.setLabel(_("Remove"));
            result.setTooltiptext(_("Remove limiting resource element"));
            result.addEventListener(Events.ON_CLICK, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    removeUnassignedLimitingResourceQueueElement(element);
                }
            });
            return result;
        }

        private void removeUnassignedLimitingResourceQueueElement(
                LimitingResourceQueueElementDTO dto) {

            LimitingResourceQueueElement element = dto.getOriginal();
            limitingResourceQueueModel
                    .removeUnassignedLimitingResourceQueueElement(element);
            Util.reloadBindings(gridUnassignedLimitingResourceQueueElements);
        }

        private Button assignButton(
                final LimitingResourceQueueElementDTO element) {
            Button result = new Button();
            result.setLabel(_("Assign"));
            result.setTooltiptext(_("Assign to queue"));
            result.addEventListener(Events.ON_CLICK, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    assignLimitingResourceQueueElement(element);
                }
            });
            return result;
        }

        private void assignLimitingResourceQueueElement(
                LimitingResourceQueueElementDTO dto) {

            LimitingResourceQueueElement element = dto.getOriginal();
            if (limitingResourceQueueModel
                    .assignLimitingResourceQueueElement(element)) {
                Util.reloadBindings(gridUnassignedLimitingResourceQueueElements);
                limitingResourcesPanel.appendQueueElementToQueue(element);
            } else {
                showErrorMessage(_("Cannot allocate selected element. There is not any queue " +
                        "that matches resource allocation criteria at any interval of time"));
            }
        }

        private void manualAllocateLimitingResourceQueueElement(LimitingResourceQueueElementDTO dto) {
            LimitingResourceQueueElement element = dto.getOriginal();
            try {
                setAssignableQueues(element);
                limitingResourceQueueModel.init(element);
                manualAllocationWindow.doModal();
            } catch (SuspendNotAllowedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        private void showErrorMessage(String error) {
            try {
                Messagebox.show(error, _("Error"), Messagebox.OK, Messagebox.ERROR);
            } catch (InterruptedException e) {

            }
        }

        private Checkbox automaticQueueing(
                final LimitingResourceQueueElementDTO element) {
            Checkbox result = new Checkbox();
            result.setTooltiptext(_("Select for automatic queuing"));
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
        limitingResourceQueueModel.unschedule(task.getLimitingResourceQueueElement());
        Util.reloadBindings(gridUnassignedLimitingResourceQueueElements);
    }

    private void feedValidGaps(LimitingResourceQueueElement element, LimitingResourceQueue queue) {
        feedValidGapsSince(element, queue, getStartDayBecauseOfGantt(element));
    }

    private DateAndHour getStartDayBecauseOfGantt(LimitingResourceQueueElement element) {
        return new DateAndHour(new LocalDate(element.getEarlierStartDateBecauseOfGantt()), 0);
    }

    private void feedValidGapsSince(LimitingResourceQueueElement element, LimitingResourceQueue queue, DateAndHour since) {
        List<LimitingResourceQueueElementGap> gaps = LimitingResourceAllocator.getValidGapsForElementSince(element, queue, since);
        endAllocationDates = calculateEndAllocationDates(element.getResourceAllocation(), queue.getResource(), gaps);
        listCandidateGaps.setModel(new SimpleListModel(gaps));
        if (!gaps.isEmpty()) {
            listCandidateGaps.setSelectedIndex(0);
            setStartAllocationDate(gaps.get(0).getStartTime());
            startAllocationDate.setDisabled(true);
            disable(radioAllocationDate, false);
        } else {
            disable(radioAllocationDate, true);
        }
        radioAllocationDate.setSelectedIndex(0);
    }

    private void setStartAllocationDate(DateAndHour time) {
        final Date date = (time != null) ? toDate(time.getDate()) : null;
        startAllocationDate.setValue(date);
    }

    private Map<LimitingResourceQueueElementGap, DateAndHour> calculateEndAllocationDates(
            ResourceAllocation<?> resourceAllocation, Resource resource,
            List<LimitingResourceQueueElementGap> gaps) {

        Map<LimitingResourceQueueElementGap, DateAndHour> result = new HashMap<LimitingResourceQueueElementGap, DateAndHour>();
        for (LimitingResourceQueueElementGap each: gaps) {
            result.put(each, calculateEndAllocationDate(resourceAllocation, resource, each));
        }
        return result;
    }

    private DateAndHour calculateEndAllocationDate(
            ResourceAllocation<?> resourceAllocation, Resource resource,
            LimitingResourceQueueElementGap gap) {

        if (gap.getEndTime() != null) {
            return LimitingResourceAllocator.startTimeToAllocateStartingFromEnd(resourceAllocation, resource, gap);
        }
        return null;
    }

    public void selectRadioAllocationDate(Event event) {
        Radiogroup radiogroup = (Radiogroup) event.getTarget().getParent();
        startAllocationDate.setDisabled(radiogroup.getSelectedIndex() != 2);
    }

    private void disable(Radiogroup radiogroup, boolean disabled) {
        for (Object obj: radiogroup.getChildren()) {
            final Radio each = (Radio) obj;
            each.setDisabled(disabled);
        }
    }

    public CandidateGapRenderer getCandidateGapRenderer() {
        return candidateGapRenderer;
    }

    private class CandidateGapRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            LimitingResourceQueueElementGap gap = (LimitingResourceQueueElementGap) data;

            item.setValue(gap);
            item.appendChild(cell(gap.getStartTime()));
            item.appendChild(cell(gap.getEndTime()));
        }

        public Listcell cell(DateAndHour time) {
            return new Listcell(formatTime(time));
        }

        private String formatTime(DateAndHour time) {
            return time == null ? _("END") : time.getDate().toString("dd/MM/yyyy") + " - " + time.getHour();
        }

    }

    private void setAssignableQueues(final LimitingResourceQueueElement element) {
        List<LimitingResourceQueue> queues = limitingResourceQueueModel.getAssignableQueues(element);
        listAssignableQueues.setModel(new SimpleListModel(queues));
        listAssignableQueues.addEventListener(Events.ON_SELECT, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                SelectEvent se = (SelectEvent) event;

                LimitingResourceQueue queue = getSelectedQueue(se);
                if (queue != null) {
                    feedValidGaps(element, queue);
                }
            }

            public LimitingResourceQueue getSelectedQueue(SelectEvent se) {
                final Listitem item = (Listitem) se.getSelectedItems().iterator().next();
                return (LimitingResourceQueue) item.getValue();
            }

        });
        listAssignableQueues.setSelectedIndex(0);
        feedValidGaps(element, queues.get(0));
    }

    private LimitingResourceQueue getSelectedQueue() {
        LimitingResourceQueue result = null;

        final Listitem item = listAssignableQueues.getSelectedItem();
        if (item != null) {
            result = (LimitingResourceQueue) item.getValue();
        }
        return result;
    }

    public ListitemRenderer getQueueRenderer() {
        return queueRenderer;
    }

    private class QueueRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            final LimitingResourceQueue queue = (LimitingResourceQueue) data;
            item.setValue(queue);
            item.appendChild(cell(queue));
        }

        private Listcell cell(LimitingResourceQueue queue) {
           Listcell result = new Listcell();
           result.setLabel(queue.getResource().getName());
           return result;
        }

    }

    public void accept(Event e) {
        final LimitingResourceQueue queue = getSelectedQueue();
        final DateAndHour time = getSelectedAllocationTime();
        Validate.notNull(time);
        limitingResourceQueueModel
                .assignEditingLimitingResourceQueueElementToQueueAt(queue, time);
        Util.reloadBindings(gridUnassignedLimitingResourceQueueElements);

        LimitingResourceQueueElement element = limitingResourceQueueModel
                .getLimitingResourceQueueElement();
        limitingResourcesPanel.appendQueueElementToQueue(element);
        closeManualAllocationWindow(e);
    }

    private DateAndHour getSelectedAllocationTime() {
        final LimitingResourceQueueElementGap selectedGap = getSelectedGap();
        int index = radioAllocationDate.getSelectedIndex();

        // Earliest date
        if (index == 0) {
            return getEarliestTime(selectedGap);
        // Latest date
        } else if (index == 1) {
            return getLatestTime(selectedGap);
        // Select start date
        } else if (index == 2) {
            LocalDate selectedDay = new LocalDate(startAllocationDate.getValue());
            DateAndHour allocationTime = getValidDayInGap(selectedDay, getSelectedGap());
            if (allocationTime == null) {
                throw new WrongValueException(startAllocationDate, _("Day is not valid within selected gap"));
            }
            return allocationTime;
        }
        return null;
    }

    private DateAndHour getEarliestTime(LimitingResourceQueueElementGap gap) {
        Validate.notNull(gap);
        return gap.getStartTime();
    }

    private DateAndHour getLatestTime(LimitingResourceQueueElementGap gap) {
        Validate.notNull(gap);
        LimitingResourceQueueElement element = limitingResourceQueueModel.getLimitingResourceQueueElement();
        LimitingResourceQueue queue = getSelectedQueue();
        return LimitingResourceAllocator.startTimeToAllocateStartingFromEnd(
                element.getResourceAllocation(), queue.getResource(), gap);
    }

    private LimitingResourceQueueElementGap getSelectedGap() {
        Listitem item = listCandidateGaps.getSelectedItem();
        if (item != null) {
            return (LimitingResourceQueueElementGap) item.getValue();
        }
        return null;
    }

    /**
     * Checks if date is a valid day within gap. A day is valid within a gap if
     * it is included between gap.startTime and the last day from which is
     * possible to start doing an allocation (endAllocationDate)
     *
     * If date is valid, returns DateAndHour in gap associated with that date
     *
     * @param date
     * @param gap
     * @return
     */
    private DateAndHour getValidDayInGap(LocalDate date, LimitingResourceQueueElementGap gap) {
        final DateAndHour endAllocationDate = endAllocationDates.get(gap);
        final LocalDate start = gap.getStartTime().getDate();
        final LocalDate end = endAllocationDate != null ? endAllocationDate.getDate() : null;

        if (start.equals(date)) {
            return gap.getStartTime();
        }
        if (end != null && end.equals(date)) {
            return endAllocationDate;
        }
        if ((start.compareTo(date) <= 0
                && (end == null || end.compareTo(date) >= 0))) {
            return new DateAndHour(date, 0);
        }

        return null;
    }

    public void cancel() {
        manualAllocationWindow.setVisible(false);
    }

    public void closeManualAllocationWindow(Event e) {
        manualAllocationWindow.setVisible(false);
        e.stopPropagation();
    }

    public void setStartAllocationDate(Event event) {
        setStartAllocationDate(getSelectedGap().getStartTime());
    }

    public void highlightCalendar(Event event) {
        Datebox datebox = (Datebox) event.getTarget();
        if (datebox.getValue() == null) {
            final LocalDate startDate = getSelectedGap().getStartTime().getDate();
            datebox.setValue(toDate(startDate));
        }
        highlightDaysInGap(datebox.getUuid(), getSelectedGap());
    }

    private Date toDate(LocalDate date) {
        return date.toDateTimeAtStartOfDay().toDate();
    }

    public void highlightDaysInGap(String uuid, LimitingResourceQueueElementGap gap) {
        final LocalDate start = gap.getStartTime().getDate();
        final LocalDate end = getEndAllocationDate(gap);

        final String jsCall = "highlightDaysInInterval('"
                + uuid + "', '"
                + jsonInterval(formatDate(start), formatDate(end)) + "', '"
                + jsonHighlightColor() + "');";
        Clients.evalJavaScript(jsCall);
    }

    private LocalDate getEndAllocationDate(LimitingResourceQueueElementGap gap) {
        final DateAndHour endTime = endAllocationDates.get(gap);
        return endTime != null ? endTime.getDate() : null;
    }

    public String formatDate(LocalDate date) {
        return (date != null) ? date.toString() : null;
    }

    private String jsonInterval(String start, String end) {
        StringBuilder result = new StringBuilder();

        result.append("{\"start\": \"" + start + "\", ");
        if (end != null) {
            result.append("\"end\": \"" + end + "\"");
        }
        result.append("}");

        return result.toString();
    }

    private String jsonHighlightColor() {
        return "{\"color\": \"blue\", \"bgcolor\": \"white\"}";
    }

}
