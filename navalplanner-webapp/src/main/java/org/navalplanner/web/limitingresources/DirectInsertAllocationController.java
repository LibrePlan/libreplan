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

import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.DateAndHour;
import org.navalplanner.business.planner.entities.LimitingResourceQueueElement;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Window;

/**
 * Controller for manual allocation of queue elements
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DirectInsertAllocationController extends GenericForwardComposer {

    private LimitingResourcesController limitingResourcesController;

    private LimitingResourcesPanel limitingResourcesPanel;

    private Listbox listAssignableQueues;

    private Datebox startAllocationDate;

    private final QueueRenderer queueRenderer = new QueueRenderer();

    public DirectInsertAllocationController() {

    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        this.self = comp;
        self.setVariable("directInsertAllocationController", this, true);
        listAssignableQueues = (Listbox) self.getFellowIfAny("listAssignableQueues");
        startAllocationDate = (Datebox) self.getFellowIfAny("startAllocationDate");
    }

    private void setAssignableQueues(final LimitingResourceQueueElement element) {
        List<LimitingResourceQueue> queues = getLimitingResourceQueueModel().getAssignableQueues(element);
        listAssignableQueues.setModel(new SimpleListModel(queues));
        listAssignableQueues.setSelectedIndex(0);
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

    public void accept(Event e) {
        setStatus(Messagebox.OK);
        final LimitingResourceQueueElement element = getLimitingResourceQueueModel().getLimitingResourceQueueElement();
        final LimitingResourceQueue queue = getSelectedQueue();
        final DateAndHour allocationTime = getSelectedAllocationTime();
        getLimitingResourceQueueModel().insertQueueElementIntoQueueAt(element, queue, allocationTime);
        limitingResourcesController.reloadUnassignedLimitingResourceQueueElements();
        limitingResourcesPanel.refreshQueue(queue);
        close(e);
    }

    private DateAndHour getSelectedAllocationTime() {
        Date selectedDay = startAllocationDate.getValue();
        return new DateAndHour(new LocalDate(selectedDay), 0);
    }

    private LimitingResourceQueue getSelectedQueue() {
        LimitingResourceQueue result = null;

        final Listitem item = listAssignableQueues.getSelectedItem();
        if (item != null) {
            result = (LimitingResourceQueue) item.getValue();
        }
        return result;
    }

    public void cancel() {
        self.setVisible(false);
        setStatus(Messagebox.CANCEL);
    }

    public void close(Event e) {
        self.setVisible(false);
        e.stopPropagation();
    }

    private LimitingResourceQueueElement getEditingElement() {
        return getLimitingResourceQueueModel().getLimitingResourceQueueElement();
    }

    public void highlightCalendar(Event event) {
        final LimitingResourceQueueElement element = getEditingElement();
        Datebox datebox = (Datebox) event.getTarget();
        highlightDaysFromDate(datebox.getUuid(), new LocalDate(element.getEarlierStartDateBecauseOfGantt()));
    }

    public void highlightDaysFromDate(String uuid, LocalDate start) {
        final String jsCall = "highlightDaysInInterval('"
                + uuid + "', '"
                + jsonInterval(formatDate(start), null) + "', '"
                + jsonHighlightColor() + "');";
        Clients.evalJavaScript(jsCall);
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

    public void show(LimitingResourceQueueElement element) {
        try {
            setStatus(Messagebox.CANCEL);
            startAllocationDate.setValue(element.getEarlierStartDateBecauseOfGantt());
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

    public Integer getStatus() {
        return (Integer) self.getVariable("status", true);
    }

    public void setStatus(int status) {
        self.setVariable("status", new Integer(status), true);
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
