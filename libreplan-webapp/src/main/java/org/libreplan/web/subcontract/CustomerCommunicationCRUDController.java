/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 WirelessGalicia, S.L.
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.externalcompanies.entities.CommunicationType;
import org.libreplan.business.externalcompanies.entities.CustomerCommunication;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.planner.tabs.IGlobalViewEntryPoints;
import org.zkoss.util.Locales;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;

/**
 * Controller for CRUD actions over a {@link CustomerCommunication}
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@SuppressWarnings("serial")
public class CustomerCommunicationCRUDController extends GenericForwardComposer {

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(CustomerCommunicationCRUDController.class);

    private ICustomerCommunicationModel customerCommunicationModel;

    private CustomerCommunicationRenderer customerCommunicationRenderer = new CustomerCommunicationRenderer();;

    protected IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private Grid listing;

    @Resource
    private IGlobalViewEntryPoints globalView;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("controller", this);
        messagesForUser = new MessagesForUser(messagesContainer);
    }

    public void goToEdit(CustomerCommunication customerCommunication) {
        if(customerCommunication != null && customerCommunication.getOrder() != null){
            Order order = customerCommunication.getOrder();
            globalView.goToOrderDetails(order);
        }
    }

    public FilterCommunicationEnum[] getFilterItems(){
        return FilterCommunicationEnum.values();
    }

    public FilterCommunicationEnum getCurrentFilterItem() {
       return customerCommunicationModel.getCurrentFilter();
    }

    public void setCurrentFilterItem(FilterCommunicationEnum selected) {
        customerCommunicationModel.setCurrentFilter(selected);

        // update the customer communication list
        listing.setModel(new SimpleListModel(getCustomerCommunications()));
        listing.invalidate();
    }

    protected void save(CustomerCommunication customerCommunication)
            throws ValidationException {
        customerCommunicationModel.confirmSave(customerCommunication);
    }

    public List<CustomerCommunication> getCustomerCommunications() {
        FilterCommunicationEnum currentFilter = customerCommunicationModel.getCurrentFilter();
        switch(currentFilter){
            case ALL: return customerCommunicationModel.getCustomerAllCommunications();
            case NOT_REVIEWED: return customerCommunicationModel.getCustomerCommunicationWithoutReviewed();
            default: return customerCommunicationModel.getCustomerAllCommunications();
        }
    }

    public CustomerCommunicationRenderer getCustomerCommunicationRenderer() {
        return customerCommunicationRenderer;
    }

    private class CustomerCommunicationRenderer implements
            RowRenderer {

        @Override
        public void render(Row row, Object data) {
            CustomerCommunication customerCommunication = (CustomerCommunication) data;
            row.setValue(customerCommunication);

            final CommunicationType type = customerCommunication.getCommunicationType();
            appendLabel(row, toString(type));

            appendLabel(row, customerCommunication.getOrder().getName());
            appendLabel(row, toString(customerCommunication.getDeadline()));
            appendLabel(row, customerCommunication.getOrder().getCode());
            appendLabel(row, customerCommunication.getOrder()
                    .getCustomerReference());
            appendLabel(row,
                    toString(customerCommunication.getCommunicationDate()));
            appendCheckbox(row, customerCommunication);
            appendOperations(row, customerCommunication);
        }

        private String toString(Date date) {
            if (date == null) {
                return "";
            }
            return new SimpleDateFormat("dd/MM/yyyy", Locales.getCurrent()).format(date);
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

        private void appendCheckbox(Row row,
                final CustomerCommunication customerCommunication) {
            final Checkbox checkBoxReviewed = new Checkbox();
            checkBoxReviewed.setChecked(customerCommunication.getReviewed());

            checkBoxReviewed.addEventListener(Events.ON_CHECK,
                    new EventListener() {

                        @Override
                        public void onEvent(Event arg0) throws Exception {
                            customerCommunication.setReviewed(checkBoxReviewed.isChecked());
                            save(customerCommunication);
                        }

                    });

            row.appendChild(checkBoxReviewed);
        }

        private void appendOperations(Row row,
                final CustomerCommunication customerCommunication) {
            Button buttonEdit = new Button();
            buttonEdit.setSclass("icono");
            buttonEdit.setImage("/common/img/ico_editar1.png");
            buttonEdit.setHoverImage("/common/img/ico_editar.png");
            buttonEdit.setTooltiptext(_("Edit"));
            buttonEdit.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event arg0) throws Exception {
                    goToEdit(customerCommunication);
                }
            });
            row.appendChild(buttonEdit);
        }
    }

}