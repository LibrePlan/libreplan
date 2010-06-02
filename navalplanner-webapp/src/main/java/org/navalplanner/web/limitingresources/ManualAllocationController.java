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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.DateAndHour;
import org.navalplanner.business.planner.entities.LimitingResourceAllocator;
import org.navalplanner.business.planner.entities.LimitingResourceQueueElement;
import org.navalplanner.business.planner.entities.LimitingResourceQueueElementGap;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.navalplanner.business.resources.entities.Resource;
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
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
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

    private Datebox startAllocationDate;

    private Listbox listAssignableQueues;

    private Listbox listCandidateGaps;

    private Map<LimitingResourceQueueElementGap, DateAndHour> endAllocationDates = new HashMap<LimitingResourceQueueElementGap, DateAndHour>();

    private final QueueRenderer queueRenderer = new QueueRenderer();

    private final CandidateGapRenderer candidateGapRenderer = new CandidateGapRenderer();

    public ManualAllocationController() {

    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        this.self = comp;
        self.setVariable("manualAllocationController", this, true);
        listAssignableQueues = (Listbox) self.getFellowIfAny("listAssignableQueues");
        listCandidateGaps = (Listbox) self.getFellowIfAny("listCandidateGaps");
        radioAllocationDate = (Radiogroup) self.getFellowIfAny("radioAllocationDate");
        startAllocationDate = (Datebox) self.getFellowIfAny("startAllocationDate");
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

    private void setAssignableQueues(final LimitingResourceQueueElement element) {
        List<LimitingResourceQueue> queues = getLimitingResourceQueueModel().getAssignableQueues(element);
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

    public void accept(Event e) {
        final LimitingResourceQueue queue = getSelectedQueue();
        final DateAndHour time = getSelectedAllocationTime();
        Validate.notNull(time);
        getLimitingResourceQueueModel()
                .assignEditingLimitingResourceQueueElementToQueueAt(queue, time);
        limitingResourcesController.reloadUnassignedLimitingResourceQueueElements();

        LimitingResourceQueueElement element = getLimitingResourceQueueModel()
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
        LimitingResourceQueueElement element = getLimitingResourceQueueModel().getLimitingResourceQueueElement();
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
        self.setVisible(false);
    }

    public void closeManualAllocationWindow(Event e) {
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

    public void show(LimitingResourceQueueElement element) {
        try {
            setAssignableQueues(element);
            getLimitingResourceQueueModel().init(element);
            ((Window) self).doModal();
        } catch (SuspendNotAllowedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

}
