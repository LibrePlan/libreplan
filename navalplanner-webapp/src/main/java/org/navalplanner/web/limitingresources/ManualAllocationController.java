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

package org.navalplanner.web.limitingresources;

import static org.navalplanner.web.I18nHelper._;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.limiting.entities.DateAndHour;
import org.navalplanner.business.planner.limiting.entities.Gap;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceAllocator;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.common.Util;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Window;

/**
 * Controller for manual allocation of queue elements
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ManualAllocationController extends GenericForwardComposer {

    private LimitingResourcesController limitingResourcesController;

    private LimitingResourcesPanel limitingResourcesPanel;

    private Radiogroup radioAllocationDate;

    private Radio earliestDate, latestDate, selectStartDate;

    private Datebox startAllocationDate;

    private Listbox listAssignableQueues;

    private Listbox listCandidateGaps;

    private Checkbox cbAllocationType;

    private Map<Gap, DateAndHour> endAllocationDates = new HashMap<Gap, DateAndHour>();

    private final QueueRenderer queueRenderer = new QueueRenderer();

    private final CandidateGapRenderer candidateGapRenderer = new CandidateGapRenderer();

    private Grid gridLimitingOrderElementHours;
    private Grid gridCurrentQueue;

    public ManualAllocationController() {

    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        this.self = comp;
        self.setVariable("manualAllocationController", this, true);
        listAssignableQueues = (Listbox) self.getFellowIfAny("listAssignableQueues");
        listCandidateGaps = (Listbox) self.getFellowIfAny("listCandidateGaps");

        radioAllocationDate = (Radiogroup) self.getFellowIfAny("radioAllocationDate");
        earliestDate = (Radio) self.getFellowIfAny("earliestDate");
        latestDate = (Radio) self.getFellowIfAny("latestDate");
        selectStartDate = (Radio) self.getFellowIfAny("selectStartDate");

        startAllocationDate = (Datebox) self.getFellowIfAny("startAllocationDate");
        cbAllocationType = (Checkbox) self.getFellowIfAny("cbAllocationType");

        gridLimitingOrderElementHours = (Grid) self
                .getFellowIfAny("gridLimitingOrderElementHours");
        gridCurrentQueue = (Grid) self
                .getFellowIfAny("gridCurrentQueue");
    }

    public void setLimitingResourcesPanel(LimitingResourcesPanel limitingResourcesPanel) {
        this.limitingResourcesPanel = limitingResourcesPanel;
    }

    public void setLimitingResourcesController(LimitingResourcesController limitingResourcesController) {
        this.limitingResourcesController = limitingResourcesController;
    }

    public ILimitingResourceQueueModel getLimitingResourceQueueModel() {
        return limitingResourcesController.getLimitingResourceQueueModel();
    }

    private void feedValidGaps(LimitingResourceQueueElement element, LimitingResourceQueue queue) {
        feedValidGapsSince(element, queue, getStartDayBecauseOfGantt(element));
    }

    private DateAndHour getStartDayBecauseOfGantt(LimitingResourceQueueElement element) {
        return new DateAndHour(new LocalDate(element.getEarlierStartDateBecauseOfGantt()), 0);
    }

    private void feedValidGapsSince(LimitingResourceQueueElement element, LimitingResourceQueue queue, DateAndHour since) {
        List<Gap> gaps = LimitingResourceAllocator.getValidGapsForElementSince(element, queue, since);
        endAllocationDates = calculateEndAllocationDates(element.getResourceAllocation(), queue.getResource(), gaps);
        listCandidateGaps.setModel(new SimpleListModel(gaps));

        if (!isAppropriative()) {
            if (gaps.isEmpty()) {
                disable(radioAllocationDate, true);
            } else {
                listCandidateGaps.setSelectedIndex(0);
                setStartAllocationDate(gaps.get(0).getStartTime());
            }
            radioAllocationDate.setSelectedIndex(0);
        }
        enableRadiobuttons(isAppropriative());
        listCandidateGaps.setSelectedIndex(0);
    }

    private boolean isAppropriative() {
        return cbAllocationType.isChecked();
    }

    private void enableRadiobuttons(boolean isAppropriative) {
        final LimitingResourceQueueElement beingEdited = getBeingEditedElement();
        if (isAppropriative) {
            listCandidateGaps.setDisabled(true);

            earliestDate.setDisabled(true);
            latestDate.setDisabled(true);
            selectStartDate.setDisabled(false);
            selectStartDate.setSelected(true);

            startAllocationDate.setDisabled(false);
            startAllocationDate.setValue(beingEdited.getEarlierStartDateBecauseOfGantt());
        } else {
            listCandidateGaps.setDisabled(false);

            earliestDate.setDisabled(false);
            earliestDate.setSelected(true);
            latestDate.setDisabled(false);
            selectStartDate.setDisabled(false);

            startAllocationDate.setDisabled(true);
        }
    }

    private void setStartAllocationDate(DateAndHour time) {
        final Date date = (time != null) ? toDate(time.getDate()) : null;
        startAllocationDate.setValue(date);
    }

    private Map<Gap, DateAndHour> calculateEndAllocationDates(
            ResourceAllocation<?> resourceAllocation, Resource resource,
            List<Gap> gaps) {

        Map<Gap, DateAndHour> result = new HashMap<Gap, DateAndHour>();
        for (Gap each: gaps) {
            result.put(each, calculateEndAllocationDate(resourceAllocation, resource, each));
        }
        return result;
    }

    private DateAndHour calculateEndAllocationDate(
            ResourceAllocation<?> resourceAllocation, Resource resource,
            Gap gap) {

        if (gap.getEndTime() != null) {
            return LimitingResourceAllocator.startTimeToAllocateStartingFromEnd(resourceAllocation, resource, gap);
        }
        return null;
    }

    public void selectRadioAllocationDate(Event event) {
        Radiogroup radiogroup = (Radiogroup) event.getTarget().getFellow("radioAllocationDate");
        startAllocationDate.setDisabled(radiogroup.getSelectedIndex() != 2);
    }

    private void disable(Radiogroup radiogroup, boolean disabled) {
        for (Object obj: radiogroup.getChildren()) {
            final Radio each = (Radio) obj;
            each.setDisabled(disabled);
        }
    }

    private void setAssignableQueues(final LimitingResourceQueueElement element) {
        List<LimitingResourceQueue> queues = getLimitingResourceQueueModel().getAssignableQueues(element);
        listAssignableQueues.setModel(new SimpleListModel(queues));
        listAssignableQueues.setItemRenderer(queueRenderer);
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

    public void accept(Event e) {
        LimitingResourceQueueElement element = getBeingEditedElement();
        LimitingResourceQueue queue = getSelectedQueue();
        DateAndHour time = getSelectedAllocationTime();

        if (isAppropriative()) {
            appropriativeAllocation(element, queue, time);
        } else {
            nonAppropriativeAllocation(element, queue, time);
        }

        limitingResourcesController.reloadUnassignedLimitingResourceQueueElements();
        setStatus(Messagebox.OK);
        close(e);
    }

    private void nonAppropriativeAllocation(LimitingResourceQueueElement element, LimitingResourceQueue queue, DateAndHour time) {
        Validate.notNull(time);
        getLimitingResourceQueueModel()
                .nonAppropriativeAllocation(element, queue, time);
        limitingResourcesPanel.appendQueueElementToQueue(element);
    }

    private void appropriativeAllocation(LimitingResourceQueueElement element, LimitingResourceQueue queue, DateAndHour time) {
        Validate.notNull(time);
        getLimitingResourceQueueModel().appropriativeAllocation(element, queue, time);
        limitingResourcesPanel.refreshQueue(queue);
    }

    private DateAndHour getSelectedAllocationTime() {
        final Gap selectedGap = getSelectedGap();
        int index = radioAllocationDate.getSelectedIndex();

        // Earliest date
        if (index == 0) {
            return getEarliestTime(selectedGap);
        // Latest date
        } else if (index == 1) {
            return getLatestTime(selectedGap);
        // Select start date
        } else if (index == 2) {
            final LocalDate selectedDay = new LocalDate(startAllocationDate.getValue());
            if (isAppropriative()) {
                LimitingResourceQueueElement beingEdited = getBeingEditedElement();
                if (selectedDay.compareTo(new LocalDate(beingEdited.getEarlierStartDateBecauseOfGantt())) < 0) {
                    throw new WrongValueException(startAllocationDate, _("Day is not valid"));
                }
                return new DateAndHour(selectedDay, 0);
            } else {
                DateAndHour allocationTime = getValidDayInGap(selectedDay, getSelectedGap());
                if (allocationTime == null) {
                    throw new WrongValueException(startAllocationDate, _("Day is not valid"));
                }
                return allocationTime;
            }
        }
        return null;
    }

    private DateAndHour getEarliestTime(Gap gap) {
        Validate.notNull(gap);
        return gap.getStartTime();
    }

    private DateAndHour getLatestTime(Gap gap) {
        Validate.notNull(gap);
        LimitingResourceQueueElement element = getLimitingResourceQueueModel().getLimitingResourceQueueElement();
        LimitingResourceQueue queue = getSelectedQueue();
        return LimitingResourceAllocator.startTimeToAllocateStartingFromEnd(
                element.getResourceAllocation(), queue.getResource(), gap);
    }

    private Gap getSelectedGap() {
        Listitem item = listCandidateGaps.getSelectedItem();
        if (item != null) {
            return (Gap) item.getValue();
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
    private DateAndHour getValidDayInGap(LocalDate date, Gap gap) {
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
        self.setVisible(false);
        setStatus(Messagebox.CANCEL);
    }

    public void close(Event e) {
        self.setVisible(false);
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

        if (isAppropriative()) {
            final LimitingResourceQueueElement beingEdited = getBeingEditedElement();
            highlightDaysFromDate(datebox.getUuid(), new LocalDate(beingEdited.getEarlierStartDateBecauseOfGantt()));
        } else {
            highlightDaysInGap(datebox.getUuid(), getSelectedGap());
        }
    }

    private LimitingResourceQueueElement getBeingEditedElement() {
        return getLimitingResourceQueueModel().getLimitingResourceQueueElement();
    }

    private Date toDate(LocalDate date) {
        return date.toDateTimeAtStartOfDay().toDate();
    }

    /**
     * Highlight calendar days within gap
     *
     * @param uuid
     * @param gap
     */
    public void highlightDaysInGap(String uuid, Gap gap) {
        final LocalDate start = gap.getStartTime().getDate();
        final LocalDate end = getEndAllocationDate(gap);

        final String jsCall = "highlightDaysInInterval('"
                + uuid + "', '"
                + jsonInterval(formatDate(start), formatDate(end)) + "', '"
                + jsonHighlightColor() + "');";
        Clients.evalJavaScript(jsCall);
    }

    /**
     * Highlight calendar days starting from start
     *
     * @param uuid
     * @param start
     */
    public void highlightDaysFromDate(String uuid, LocalDate start) {
        final String jsCall = "highlightDaysInInterval('"
                + uuid + "', '"
                + jsonInterval(formatDate(start), null) + "', '"
                + jsonHighlightColor() + "');";
        Clients.evalJavaScript(jsCall);
    }

    private LocalDate getEndAllocationDate(Gap gap) {
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

    public CandidateGapRenderer getCandidateGapRenderer() {
        return candidateGapRenderer;
    }

    private class CandidateGapRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            Gap gap = (Gap) data;

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

    public void show(LimitingResourceQueueElement element) {
        try {
            clear();
            setAssignableQueues(element);
            getLimitingResourceQueueModel().init(element);
            Util.reloadBindings(gridLimitingOrderElementHours);
            Util.reloadBindings(gridCurrentQueue);
            ((Window) self).doModal();
            ((Window) self).setTitle(_("Manual assignment"));
        } catch (SuspendNotAllowedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void clear() {
        setStatus(Messagebox.CANCEL);
        cbAllocationType.setChecked(false);
    }

    public ListitemRenderer getQueueRenderer() {
        return queueRenderer;
    }

    public Integer getStatus() {
        return (Integer) self.getVariable("status", true);
    }

    public void setStatus(int status) {
        self.setVariable("status", new Integer(status), true);
    }

    private class QueueRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            final LimitingResourceQueue queue = (LimitingResourceQueue) data;
            item.setValue(queue);
//            item.setLabel("test1");
            item.appendChild(cell(queue));
        }

        private Listcell cell(LimitingResourceQueue queue) {
           Listcell result = new Listcell();
           result.setLabel(queue.getResource().getName());
//           result.setLabel("test2");
           return result;
        }

    }

    public void onCheckAllocationType(Event e) {
        Checkbox checkbox = (Checkbox) e.getTarget();
        enableRadiobuttons(checkbox.isChecked());
    }

    public int getHours() {
        if (getBeingEditedElement() == null) {
            return 0;
        }
        return getBeingEditedElement().getIntentedTotalHours();
    }

    public String getResourceOrCriteria() {
        if (getBeingEditedElement() == null) {
            return "";
        }
        return LimitingResourcesController
                .getResourceOrCriteria(getBeingEditedElement()
                        .getResourceAllocation());
    }

    public String getCurrentQueue() {
        if (getBeingEditedElement() == null
                || getBeingEditedElement().getLimitingResourceQueue() == null) {
            return _("Unnasigned");
        }
        return getBeingEditedElement().getLimitingResourceQueue().getResource()
                .getName();
    }

    public String getCurrentStart() {
        if (getBeingEditedElement() == null
                || getBeingEditedElement().getStartDate() == null) {
            return _("Unnasigned");
        }
        return getBeingEditedElement().getStartDate().toString();
    }

    public String getCurrentEnd() {
        if (getBeingEditedElement() == null
                || getBeingEditedElement().getEndDate() == null) {
            return _("Unnasigned");
        }
        return getBeingEditedElement().getEndDate().toString();
    }

}
