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

package org.navalplanner.web.scenarios;

import static org.navalplanner.web.I18nHelper._;

import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.api.Listbox;

/**
 * Controller for UI operations to transfer orders between scenarios.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class TransferOrdersController extends GenericForwardComposer {

    @Autowired
    private ITransferOrdersModel transferOrdersModel;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private BandboxSearch sourceScenarioBandboxSearch;

    private Listbox sourceScenarioOrders;

    private BandboxSearch destinationScenarioBandboxSearch;

    private Listbox destinationScenarioOrders;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        comp.setVariable("transferOrdersController", this, true);

        sourceScenarioBandboxSearch.setListboxEventListener(Events.ON_CLICK,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        setSourceScenario();
                    }
                });

        sourceScenarioBandboxSearch.setListboxEventListener(Events.ON_OK,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        setSourceScenario();
                    }
                });

        destinationScenarioBandboxSearch.setListboxEventListener(
                Events.ON_CLICK, new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        setDestinationScenario();
                    }
                });

        destinationScenarioBandboxSearch.setListboxEventListener(Events.ON_OK,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        setDestinationScenario();
                    }
                });
    }

    private void setSourceScenario() {
        Scenario sourceScenario = (Scenario) sourceScenarioBandboxSearch
                .getSelectedElement();
        transferOrdersModel.setSourceScenario(sourceScenario);
        Util.reloadBindings(sourceScenarioOrders);
        sourceScenarioBandboxSearch.close();
    }

    private void setDestinationScenario() {
        Scenario destinationScenario = (Scenario) destinationScenarioBandboxSearch
                .getSelectedElement();
        transferOrdersModel.setDestinationScenario(destinationScenario);
        Util.reloadBindings(destinationScenarioOrders);
        destinationScenarioBandboxSearch.close();
    }

    public List<Scenario> getScenarios() {
        return transferOrdersModel.getScenarios();
    }

    public Scenario getSourceScenario() {
        return transferOrdersModel.getSourceScenario();
    }

    public Set<Order> getSourceScenarioOrders() {
        return transferOrdersModel.getSourceScenarioOrders();
    }

    public Scenario getDestinationScenario() {
        return transferOrdersModel.getDestinationScenario();
    }

    public Set<Order> getDestinationScenarioOrders() {
        return transferOrdersModel.getDestinationScenarioOrders();
    }

    public ListitemRenderer getSourceOrderRenderer() {
        return new OrderRenderer(true);
    }

    public ListitemRenderer getDestinationOrderRenderer() {
        return new OrderRenderer(false);
    }

    private class OrderRenderer implements ListitemRenderer {

        private final boolean source;

        public OrderRenderer(boolean source) {
            this.source = source;
        }

        @Override
        public void render(Listitem item, Object data) {
            Order order = (Order) data;
            item.setValue(data);

            item.appendChild(new Listcell(order.getCode()));
            item.appendChild(new Listcell(order.getName()));
            Scenario scenario = source ? transferOrdersModel
                    .getSourceScenario() : transferOrdersModel
                    .getDestinationScenario();
            item.appendChild(new Listcell(transferOrdersModel.getVersion(order,
                    scenario)));

            if (source) {
                Listcell cell = new Listcell();
                cell.appendChild(getTransferButton(order));
                item.appendChild(cell);
            }
        }

        private Button getTransferButton(final Order order) {
            Button transferButton = new Button(_("Transfer"));
            transferButton.addEventListener(Events.ON_CLICK,
                    new EventListener() {
                        @Override
                        public void onEvent(Event event) {
                            try {
                                transferOrdersModel.transfer(order);
                                Util.reloadBindings(destinationScenarioOrders);
                                messagesForUser.showMessage(Level.INFO,
                                        _("Project {0} transfered", order
                                                .getName()));
                            } catch (ValidationException e) {
                                messagesForUser.showInvalidValues(e);
                            }
                        }
                    });
            return transferButton;
        }

    }

}
