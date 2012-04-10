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

package org.libreplan.web.planner.order;

import static org.libreplan.web.I18nHelper._;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.externalcompanies.entities.EndDateCommunicationToCustomer;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.planner.entities.SubcontractState;
import org.libreplan.business.planner.entities.SubcontractedTaskData;
import org.libreplan.business.planner.entities.SubcontractorDeliverDate;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.TaskEditFormComposer;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.api.Datebox;
import org.zkoss.zul.api.Tabpanel;

/**
 * Controller for subcontract a task.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@org.springframework.stereotype.Component("subcontractController")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SubcontractController extends GenericForwardComposer {

    private Tabpanel tabpanel;

    private ISubcontractModel subcontractModel;

    private Grid gridDeliverDate;

    private DeliverDatesRenderer deliverDatesRenderer = new DeliverDatesRenderer();

    protected IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private IContextWithPlannerTask<TaskElement> currentContext;

    private Grid gridEndDates;

    private TaskEditFormComposer taskEditFormComposer = new TaskEditFormComposer();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        tabpanel = (Tabpanel) comp;
        messagesForUser = new MessagesForUser(messagesContainer);
    }

    public void init(Task task, IContextWithPlannerTask<TaskElement> context,
            TaskEditFormComposer taskEditFormComposer) {
        this.currentContext = context;
        subcontractModel.init(task, context.getTask());
        this.taskEditFormComposer = taskEditFormComposer;
        Util.reloadBindings(tabpanel);
    }

    public void accept() throws ValidationException {
        subcontractModel.confirm();
    }

    public void cancel() {
        subcontractModel.cancel();
    }

    public List<ExternalCompany> getSubcontractorExternalCompanies() {
        return subcontractModel.getSubcontractorExternalCompanies();
    }

    public SubcontractedTaskData getSubcontractedTaskData() {
        return subcontractModel.getSubcontractedTaskData();
    }

    public void setExternalCompany(Comboitem comboitem) {
        if (comboitem != null && comboitem.getValue() != null) {
            ExternalCompany externalCompany = (ExternalCompany) comboitem
                    .getValue();
            subcontractModel.setExternalCompany(externalCompany);
        } else {
            subcontractModel.setExternalCompany(null);
        }
    }

    public Date getEndDate() {
        return subcontractModel.getEndDate();
    }

    public void setEndDate(Date endDate) {
        subcontractModel.setEndDate(endDate);
    }

    public void removeSubcontractedTaskData() {
        subcontractModel.removeSubcontractedTaskData();
    }

    public SortedSet<SubcontractorDeliverDate> getDeliverDates() {
        return subcontractModel.getDeliverDates();
    }

    public void addDeliverDate(Datebox newDeliverDate){
        if (newDeliverDate == null || newDeliverDate.getValue() == null) {
            messagesForUser.showMessage(Level.ERROR,
                    _("You must select a valid date. "));
            return;
        }
        if (thereIsSomeCommunicationDateEmpty()) {
            messagesForUser
            .showMessage(
                    Level.ERROR,
                    _("It will only be possible to add a Deliver Date if all the deliver date exiting in the table have a CommunicationDate not empty. "));
            return;
        }
        if(subcontractModel.alreadyExistsRepeatedDeliverDate(newDeliverDate.getValue())){
            messagesForUser
            .showMessage(
                    Level.ERROR,
                    _("It already exists a deliver date with the same date. "));
            return;
        }
        subcontractModel.addDeliverDate(newDeliverDate.getValue());
        Util.reloadBindings(gridDeliverDate);
        gridDeliverDate.invalidate();
    }

    private boolean thereIsSomeCommunicationDateEmpty(){
        for(SubcontractorDeliverDate subDeliverDate : subcontractModel.getDeliverDates()){
            if(subDeliverDate.getCommunicationDate() == null){
                return true;
            }
        }
        return false;
    }

    public DeliverDatesRenderer getDeliverDatesRenderer(){
        return new DeliverDatesRenderer();
    }

    private class DeliverDatesRenderer implements RowRenderer{

        @Override
        public void render(Row row, Object data) throws Exception {
            SubcontractorDeliverDate subcontractorDeliverDate = (SubcontractorDeliverDate) data;
            row.setValue(subcontractorDeliverDate);

            appendLabel(row, toString(subcontractorDeliverDate.getSaveDate()));
            appendLabel(row, toString(subcontractorDeliverDate.getSubcontractorDeliverDate()));
            appendLabel(row, toString(subcontractorDeliverDate.getCommunicationDate()));
            appendOperations(row, subcontractorDeliverDate);
        }

        private String toString(Date date) {
            if (date == null) {
                return "";
            }
            return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
        }

        private void appendLabel(Row row, String label) {
            row.appendChild(new Label(label));
        }

        private void appendOperations(Row row,
                SubcontractorDeliverDate subcontractorDeliverDate) {
            Hbox hbox = new Hbox();
            hbox.appendChild(getDeleteButton(subcontractorDeliverDate));
            row.appendChild(hbox);
        }

        private Button getDeleteButton(
                final SubcontractorDeliverDate subcontractorDeliverDate) {

            Button deleteButton = new Button();
            deleteButton.setDisabled(isNotUpdate(subcontractorDeliverDate));
            deleteButton.setSclass("icono");
            deleteButton.setImage("/common/img/ico_borrar1.png");
            deleteButton.setHoverImage("/common/img/ico_borrar.png");
            deleteButton.setTooltiptext(_("Delete"));
            deleteButton.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) {
                    removeRequiredDeliverDate(subcontractorDeliverDate);
                }
            });

            return deleteButton;
        }

        private boolean isNotUpdate(final SubcontractorDeliverDate subDeliverDate){
            SubcontractorDeliverDate lastDeliverDate = getSubcontractedTaskData()
                    .getRequiredDeliveringDates().first();
            if ((lastDeliverDate != null) && (lastDeliverDate.equals(subDeliverDate))) {
                return (lastDeliverDate.getCommunicationDate() != null);
            }
            return true;
        }
    }

    public void removeRequiredDeliverDate(SubcontractorDeliverDate subcontractorDeliverDate){
        subcontractModel.removeRequiredDeliverDate(subcontractorDeliverDate);
        Util.reloadBindings(gridDeliverDate);
    }

    public boolean isSent(){
        return !isNotSent();
    }

    public boolean isNotSent() {
        if (this.getSubcontractedTaskData() != null && this.getSubcontractedTaskData().getState() != null) {
            return ((this.getSubcontractedTaskData().getState()
                    .equals(SubcontractState.PENDING_INITIAL_SEND)) || (this
                    .getSubcontractedTaskData().getState()
                    .equals(SubcontractState.FAILED_SENT)));
        }
        return false;
    }

    public SortedSet<EndDateCommunicationToCustomer> getAskedEndDates() {
        return subcontractModel.getAskedEndDates();
    }

    public EndDatesRenderer getEndDatesRenderer() {
        return new EndDatesRenderer();
    }

    private class EndDatesRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) throws Exception {
            EndDateCommunicationToCustomer endDateFromSubcontractor = (EndDateCommunicationToCustomer) data;
            row.setValue(endDateFromSubcontractor);

            appendLabel(row, toString(endDateFromSubcontractor.getEndDate(), "dd/MM/yyyy"));
            appendLabel(row,
                    toString(endDateFromSubcontractor.getCommunicationDate(), "dd/MM/yyyy HH:mm"));
            appendOperations(row, endDateFromSubcontractor);
        }

        private String toString(Date date, String precision) {
            if (date == null) {
                return "";
            }
            return new SimpleDateFormat(precision).format(date);
        }

        private void appendLabel(Row row, String label) {
            row.appendChild(new Label(label));
        }

        private void appendOperations(Row row,
                EndDateCommunicationToCustomer endDateFromSubcontractor) {
            Hbox hbox = new Hbox();
            hbox.appendChild(getUpdateButton(endDateFromSubcontractor));
            row.appendChild(hbox);
        }

        private Button getUpdateButton(final EndDateCommunicationToCustomer endDateFromSubcontractor) {

            Button updateButton = new Button(_("Update task end"));
            updateButton.setDisabled(!isUpgradeable(endDateFromSubcontractor));

            updateButton.setTooltiptext(_("Update task end"));
            updateButton.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) {
                    updateTaskEnd(endDateFromSubcontractor.getEndDate());
                }
            });

            return updateButton;
        }

        private boolean isUpgradeable(EndDateCommunicationToCustomer endDateFromSubcontractor) {
            EndDateCommunicationToCustomer lastEndDateReported = getSubcontractedTaskData()
                    .getLastEndDatesCommunicatedFromSubcontractor();
            if (lastEndDateReported != null) {
                if (lastEndDateReported.equals(endDateFromSubcontractor)) {
                    Date newEndDate = lastEndDateReported.getEndDate();
                    Date endDateTask = taskEditFormComposer.getTaskDTO().deadlineDate;
                    if (endDateTask != null) {
                        return (newEndDate.compareTo(endDateTask) != 0);
                    }
                }
            }
            return false;
        }

    }

    public void updateTaskEnd(Date date) {
        if (taskEditFormComposer != null) {
            taskEditFormComposer.getTaskDTO().deadlineDate = date;
        }
        refressGridEndDates();
    }

    public void refressGridEndDates() {
        Util.reloadBindings(gridEndDates);
    }
}