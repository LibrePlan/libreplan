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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.externalcompanies.entities.ComunicationType;
import org.libreplan.business.externalcompanies.entities.CustomerComunication;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.planner.tabs.IGlobalViewEntryPoints;
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
 * Controller for CRUD actions over a {@link CustomerComunication}
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@SuppressWarnings("serial")
public class CustomerComunicationCRUDController extends GenericForwardComposer {

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(CustomerComunicationCRUDController.class);

    private ICustomerComunicationModel customerComunicationModel;

    private CustomerComunicationRenderer customerComunicationRenderer = new CustomerComunicationRenderer();;

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

    public void goToEdit(CustomerComunication customerComunication) {
        if(customerComunication != null && customerComunication.getOrder() != null){
            Order order = customerComunication.getOrder();
            globalView.goToOrderDetails(order);
        }
    }

    public FilterComunicationEnum[] getFilterItems(){
        return FilterComunicationEnum.values();
    }

    public FilterComunicationEnum getCurrentFilterItem() {
       return customerComunicationModel.getCurrentFilter();
    }

    public void setCurrentFilterItem(FilterComunicationEnum selected) {
        customerComunicationModel.setCurrentFilter(selected);

        // update the customer comunication list
        listing.setModel(new SimpleListModel(getCustomerComunications()));
        listing.invalidate();
    }

    protected void save(CustomerComunication customerComunication)
            throws ValidationException {
        customerComunicationModel.confirmSave(customerComunication);
    }

    public List<CustomerComunication> getCustomerComunications() {
        FilterComunicationEnum currentFilter = customerComunicationModel.getCurrentFilter();
        switch(currentFilter){
            case ALL: return customerComunicationModel.getCustomerAllComunications();
            case NOT_REVIEWED: return customerComunicationModel.getCustomerComunicationWithoutReviewed();
            default: return customerComunicationModel.getCustomerAllComunications();
        }
    }

    public CustomerComunicationRenderer getCustomerComunicationRenderer() {
        return customerComunicationRenderer;
    }

    private class CustomerComunicationRenderer implements
            RowRenderer {

        @Override
        public void render(Row row, Object data) {
            CustomerComunication customerComunication = (CustomerComunication) data;
            row.setValue(customerComunication);

            final ComunicationType type = customerComunication.getComunicationType();
            appendLabel(row, type.toString());

            appendLabel(row, customerComunication.getOrder().getName());
            appendLabel(row, toString(customerComunication.getDeadline()));
            appendLabel(row, customerComunication.getOrder().getCode());
            appendLabel(row, customerComunication.getOrder()
                    .getCustomerReference());
            appendLabel(row,
                    toString(customerComunication.getComunicationDate()));
            appendCheckbox(row, customerComunication);
            appendOperations(row, customerComunication);
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

        private void appendCheckbox(Row row,
                final CustomerComunication customerComunication) {
            final Checkbox checkBoxReviewed = new Checkbox();
            checkBoxReviewed.setChecked(customerComunication.getReviewed());

            checkBoxReviewed.addEventListener(Events.ON_CHECK,
                    new EventListener() {

                        @Override
                        public void onEvent(Event arg0) throws Exception {
                            customerComunication.setReviewed(checkBoxReviewed.isChecked());
                            save(customerComunication);
                        }

                    });

            row.appendChild(checkBoxReviewed);
        }

        private void appendOperations(Row row,
                final CustomerComunication customerComunication) {
            Button buttonEdit = new Button(_("edit"));
            buttonEdit.addEventListener(Events.ON_CLICK, new EventListener() {

                @Override
                public void onEvent(Event arg0) throws Exception {
                    goToEdit(customerComunication);
                }

            });
            row.appendChild(buttonEdit);
        }
    }

}