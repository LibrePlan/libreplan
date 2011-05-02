/*
 * This file is part of NavalPlan
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
package org.navalplanner.web.subcontract;

import static org.navalplanner.web.I18nHelper._;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.subcontract.exceptions.ConnectionProblemsException;
import org.navalplanner.web.subcontract.exceptions.UnrecoverableErrorServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.servlets.CallbackServlet;
import org.zkoss.ganttz.servlets.CallbackServlet.IServletRequestHandler;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.api.Window;

/**
 * Controller for operations related with report advances.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReportAdvancesController extends GenericForwardComposer {

    private Window window;

    private Component messagesContainer;
    private IMessagesForUser messagesForUser;

    @Autowired
    private IReportAdvancesModel reportAdvancesModel;

    private ReportAdvancesOrderRenderer reportAdvancesOrderRenderer = new ReportAdvancesOrderRenderer();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        window = (Window) comp;
        window.setVariable("controller", this, true);
        messagesForUser = new MessagesForUser(messagesContainer);
    }

    public List<Order> getOrdersWithExternalCodeInAnyOrderElement() {
        return reportAdvancesModel.getOrdersWithExternalCodeInAnyOrderElement();
    }

    public ReportAdvancesOrderRenderer getReportAdvancesOrderRenderer() {
        return reportAdvancesOrderRenderer;
    }

    private class ReportAdvancesOrderRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) throws Exception {
            Order order = (Order) data;
            row.setValue(order);

            appendLabel(row, order.getCode());
            appendLabel(row, toString(order.getCustomerReference()));
            appendLabel(row, order.getName());

            DirectAdvanceAssignment directAdvanceAssignment = order
                    .getDirectAdvanceAssignmentOfTypeSubcontractor();

            AdvanceMeasurement lastAdvanceMeasurementReported = reportAdvancesModel
                    .getLastAdvanceMeasurementReported(directAdvanceAssignment);

            if (lastAdvanceMeasurementReported != null) {
                appendLabel(row, toString(lastAdvanceMeasurementReported.getDate()));
                appendLabel(row, toString(lastAdvanceMeasurementReported.getValue()));
            } else {
                appendLabel(row, "");
                appendLabel(row, "");
            }

            AdvanceMeasurement lastAdvanceMeasurement = reportAdvancesModel
                    .getLastAdvanceMeasurement(directAdvanceAssignment);

            if (lastAdvanceMeasurement != null) {
                appendLabel(row, toString(lastAdvanceMeasurement.getDate()));
                appendLabel(row, toString(lastAdvanceMeasurement.getValue()));
            } else {
                appendLabel(row, "");
                appendLabel(row, "");
            }

            if (reportAdvancesModel
                    .isAnyAdvanceMeasurementNotReported(directAdvanceAssignment)) {
                appendLabel(row, _("Pending update"));
                appendOperations(row, order, false);
            } else {
                appendLabel(row, _("Updated"));
                appendOperations(row, order, true);
            }

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

        private void appendOperations(Row row, Order order,
                boolean sendButtonDisabled) {
            Hbox hbox = new Hbox();
            hbox.appendChild(getExportButton(order));
            hbox.appendChild(getSendButton(order, sendButtonDisabled));
            row.appendChild(hbox);
        }

        private Button getExportButton(
                final Order order) {
            Button exportButton = new Button(_("XML"));
            exportButton.addEventListener(Events.ON_CLICK, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    String uri = CallbackServlet.registerAndCreateURLFor(
                            (HttpServletRequest) Executions.getCurrent()
                                    .getNativeRequest(),
                            new IServletRequestHandler() {

                                @Override
                                public void handle(HttpServletRequest request,
                                        HttpServletResponse response)
                                        throws ServletException, IOException {
                                    response.setContentType("text/xml");
                                    String xml = reportAdvancesModel
                                            .exportXML(order);
                                    response.getWriter().write(xml);
                                }

                            }, false);

                    Executions.getCurrent().sendRedirect(uri, "_blank");
                }

            });

            return exportButton;
        }

        private Button getSendButton(final Order order,
                boolean sendButtonDisabled) {
            Button sendButton = new Button(_("Send"));
            sendButton.addEventListener(Events.ON_CLICK, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    try {
                        reportAdvancesModel.sendAdvanceMeasurements(order);
                        messagesForUser.showMessage(Level.INFO,
                                _("Progress sent successfully"));
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

            sendButton.setDisabled(sendButtonDisabled);

            return sendButton;
        }

    }

}
