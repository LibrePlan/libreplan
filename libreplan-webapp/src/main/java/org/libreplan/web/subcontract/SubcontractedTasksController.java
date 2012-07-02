/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * Copyright (C) 2011-2012 WirelessGalicia, S.L.
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
package org.libreplan.web.subcontract;

import static org.libreplan.web.I18nHelper._;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.SubcontractedTaskData;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.libreplan.web.subcontract.exceptions.ConnectionProblemsException;
import org.libreplan.web.subcontract.exceptions.UnrecoverableErrorServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.servlets.CallbackServlet;
import org.zkoss.ganttz.servlets.CallbackServlet.DisposalMode;
import org.zkoss.ganttz.servlets.CallbackServlet.IServletRequestHandler;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Column;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelExt;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.api.Window;

/**
 * Controller for operations related with subcontracted tasks.
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SubcontractedTasksController extends GenericForwardComposer {

    private Window window;

    private Column columnBySort;

    private Grid listing;

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
        public void render(Row row, Object data) {
            SubcontractedTaskData subcontractedTaskData = (SubcontractedTaskData) data;
            row.setValue(subcontractedTaskData);

            Order order = getOrder(subcontractedTaskData);

            appendLabel(row,
                    toString(subcontractedTaskData.getSubcontratationDate(), "dd/MM/yyyy HH:mm"));
            appendLabel(
                    row,
                    toString(subcontractedTaskData.getSubcontractCommunicationDate(),
                            "dd/MM/yyyy HH:mm"));
            appendLabel(row, getExternalCompany(subcontractedTaskData));
            appendLabel(row, getOrderCode(order));
            appendLabel(row, getOrderName(order));
            appendLabel(row, subcontractedTaskData.getSubcontractedCode());
            appendLabel(row, getTaskName(subcontractedTaskData));
            appendLabel(row, subcontractedTaskData.getWorkDescription());
            appendLabel(row, Util.addCurrencySymbol(subcontractedTaskData.getSubcontractPrice()));
            appendLabel(row,
                    toString(subcontractedTaskData.getLastRequiredDeliverDate(), "dd/MM/yyyy"));
            appendLabel(row, _(toString(subcontractedTaskData.getState())));
            appendOperations(row, subcontractedTaskData);
        }

        private String getOrderCode(Order order) {
            return (order != null) ? order.getCode() : "";
        }

        private String getOrderName(Order order) {
            return (order != null) ? order.getName() : "";
        }

        private Order getOrder(SubcontractedTaskData subcontractedTaskData) {
            return subcontractedTasksModel.getOrder(subcontractedTaskData);
        }

        private String toString(Object object) {
            if (object == null) {
                return "";
            }

            return object.toString();
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

        private String getTaskName(SubcontractedTaskData subcontractedTaskData) {
            return subcontractedTaskData.getTask().getName();
        }

        private String getExternalCompany(
                SubcontractedTaskData subcontractedTaskData) {
            return subcontractedTaskData.getExternalCompany().getName();
        }

        private void appendOperations(Row row,
                SubcontractedTaskData subcontractedTaskData) {
            Hbox hbox = new Hbox();
            hbox.appendChild(getExportButton(subcontractedTaskData));
            hbox.appendChild(getSendButton(subcontractedTaskData));
            row.appendChild(hbox);
        }

        private Button getExportButton(
                final SubcontractedTaskData subcontractedTaskData) {
            Button exportButton = new Button("XML");
            exportButton.addEventListener(Events.ON_CLICK, new EventListener() {

                IServletRequestHandler requestHandler = new IServletRequestHandler() {

                    @Override
                    public void handle(HttpServletRequest request,
                            HttpServletResponse response)
                            throws ServletException, IOException {
                        response.setContentType("text/xml");
                        String xml = subcontractedTasksModel
                                .exportXML(subcontractedTaskData);
                        response.getWriter().write(xml);
                    }

                };

                @Override
                public void onEvent(Event event) {
                    String uri = CallbackServlet.registerAndCreateURLFor(
                            (HttpServletRequest) Executions.getCurrent()
                                    .getNativeRequest(), requestHandler, false,
                            DisposalMode.WHEN_NO_LONGER_REFERENCED);

                    Executions.getCurrent().sendRedirect(uri, "_blank");
                }

            });

            return exportButton;
        }

        private Button getSendButton(
                final SubcontractedTaskData subcontractedTaskData) {
            Button sendButton = new Button(_("Send"));
            sendButton.addEventListener(Events.ON_CLICK, new EventListener() {

                @Override
                public void onEvent(Event event) {
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
                    reload();
                }

            });

            sendButton.setDisabled(!subcontractedTaskData.isSendable());

            return sendButton;
        }

    }

    public void reload() {
        Util.reloadBindings(window);
        forceSortGridSatisfaction();
    }

    public void forceSortGridSatisfaction() {
        Column column = (Column) columnBySort;
        ListModelExt model = (ListModelExt) listing.getModel();
        if ("ascending".equals(column.getSortDirection())) {
            model.sort(column.getSortAscending(), true);
        }
        if ("descending".equals(column.getSortDirection())) {
            model.sort(column.getSortDescending(), false);
        }
    }

    public void initRender() {
        forceSortGridSatisfaction();
    }
}
