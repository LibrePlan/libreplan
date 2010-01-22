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
package org.navalplanner.web.subcontract;

import static org.navalplanner.web.I18nHelper._;

import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.planner.entities.SubcontractedTaskData;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.subcontract.exceptions.ConnectionProblemsException;
import org.navalplanner.web.subcontract.exceptions.UnrecoverableErrorServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.api.Window;

/**
 * Controller for operations related with subcontracted tasks.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SubcontractedTasksController extends GenericForwardComposer {

    private Window window;

    private Component messagesContainer;
    private IMessagesForUser messagesForUser;

    @Autowired
    private ISubcontractedTasksModel subcontractedTasksModel;

    private SubcontractedTasksRenderer subcontractedTasksRenderer = new SubcontractedTasksRenderer();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        window = (Window) comp;
        window.setVariable("controller", this, true);
        messagesForUser = new MessagesForUser(messagesContainer);
    }

    public List<SubcontractedTaskData> getSubcontractedTasks() {
        return subcontractedTasksModel.getSubcontractedTasks();
    }

    public SubcontractedTasksRenderer getSubcontractedTasksRenderer() {
        return subcontractedTasksRenderer;
    }

    private class SubcontractedTasksRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) throws Exception {
            SubcontractedTaskData subcontractedTaskData = (SubcontractedTaskData) data;
            row.setValue(subcontractedTaskData);

            appendLabel(row, toString(subcontractedTaskData
                    .getSubcontratationDate()));
            appendLabel(row, toString(subcontractedTaskData
                    .getSubcontractCommunicationDate()));
            appendLabel(row, getExternalCompany(subcontractedTaskData));
            appendLabel(row, getOrderCode(subcontractedTaskData));
            appendLabel(row, subcontractedTaskData.getSubcontractedCode());
            appendLabel(row, getTaskName(subcontractedTaskData));
            appendLabel(row, subcontractedTaskData.getWorkDescription());
            appendLabel(row, toString(subcontractedTaskData
                    .getSubcontractPrice()));
            appendLabel(row, toString(subcontractedTaskData.getState()));
            appendOperations(row, subcontractedTaskData);
        }

        private String toString(Object object) {
            if (object == null) {
                return "";
            }

            return object.toString();
        }

        private void appendLabel(Row row, String label) {
            row.appendChild(new Label(label));
        }

        private String getOrderCode(SubcontractedTaskData subcontractedTaskData) {
            return subcontractedTasksModel.getOrderCode(subcontractedTaskData);
        }

        private String getTaskName(SubcontractedTaskData subcontractedTaskData) {
            return subcontractedTaskData.getTask().getName();
        }

        private String getExternalCompany(
                SubcontractedTaskData subcontractedTaskData) {
            return subcontractedTaskData.getExternalCompany().getName();
        }

        private void appendOperations(Row row,
                SubcontractedTaskData subcontractedTaskData) {
            row.appendChild(getSendButton(subcontractedTaskData));
        }

        private Button getSendButton(
                final SubcontractedTaskData subcontractedTaskData) {
            Button sendButton = new Button(_("Send"));
            sendButton.addEventListener(Events.ON_CLICK, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    try {
                        subcontractedTasksModel
                                .sendToSubcontractor(subcontractedTaskData);
                        messagesForUser.showMessage(Level.INFO,
                                _("Subcontracted task sent successfully"));
                    } catch (UnrecoverableErrorServiceException e) {
                        messagesForUser
                                .showMessage(Level.ERROR, e.getMessage());
                    } catch (ConnectionProblemsException e) {
                        messagesForUser
                                .showMessage(Level.ERROR, e.getMessage());
                    } catch (ValidationException e) {
                        messagesForUser.showInvalidValues(e);
                    }
                    Util.reloadBindings(window);
                }

            });

            sendButton.setDisabled(!subcontractedTaskData.isSendable());

            return sendButton;
        }

    }

}
