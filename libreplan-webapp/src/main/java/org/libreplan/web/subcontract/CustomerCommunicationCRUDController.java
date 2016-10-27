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

import org.libreplan.business.externalcompanies.entities.CommunicationType;
import org.libreplan.business.externalcompanies.entities.CustomerCommunication;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.libreplan.web.planner.tabs.IGlobalViewEntryPoints;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;

import java.util.List;

import static org.libreplan.web.I18nHelper._;

/**
 * Controller for CRUD actions over a {@link CustomerCommunication}.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@SuppressWarnings("serial")
public class CustomerCommunicationCRUDController extends GenericForwardComposer {

    private ICustomerCommunicationModel customerCommunicationModel;

    private CustomerCommunicationRenderer customerCommunicationRenderer = new CustomerCommunicationRenderer();;

    protected IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private Grid listing;

    private IGlobalViewEntryPoints globalView;

    public CustomerCommunicationCRUDController() {
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("controller", this);

        injectsObjects();

        messagesForUser = new MessagesForUser(messagesContainer);
    }

    private void injectsObjects() {
        if ( customerCommunicationModel == null ) {
            customerCommunicationModel = (ICustomerCommunicationModel) SpringUtil.getBean("customerCommunicationModel");
        }

        if ( globalView == null ) {
            globalView = (IGlobalViewEntryPoints) SpringUtil.getBean("globalView");
        }
    }

    public void goToEdit(CustomerCommunication customerCommunication) {
        if (customerCommunication != null && customerCommunication.getOrder() != null) {
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
        refreshCustomerCommunicationsList();
    }

    private void refreshCustomerCommunicationsList(){
        // Update the customer communication list
        listing.setModel(new SimpleListModel<>(getCustomerCommunications()));
        listing.invalidate();
    }

    protected void save(CustomerCommunication customerCommunication) {
        customerCommunicationModel.confirmSave(customerCommunication);
    }

    public List<CustomerCommunication> getCustomerCommunications() {
        FilterCommunicationEnum currentFilter = customerCommunicationModel.getCurrentFilter();

        switch(currentFilter) {

            case NOT_REVIEWED:
                return customerCommunicationModel.getCustomerCommunicationWithoutReviewed();

            case ALL:
            default:
                return customerCommunicationModel.getCustomerAllCommunications();
        }
    }

    public CustomerCommunicationRenderer getCustomerCommunicationRenderer() {
        return customerCommunicationRenderer;
    }

    private class CustomerCommunicationRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data, int i) {
            CustomerCommunication customerCommunication = (CustomerCommunication) data;
            row.setValue(customerCommunication);

            final CommunicationType type = customerCommunication.getCommunicationType();
            if(!customerCommunication.getReviewed()){
                row.setSclass("communication-not-reviewed");
            }

            appendLabel(row, toString(type));
            appendLabel(row, customerCommunication.getOrder().getName());
            appendLabel(row, Util.formatDate(customerCommunication.getDeadline()));
            appendLabel(row, customerCommunication.getOrder().getCode());
            appendLabel(row, customerCommunication.getOrder().getCustomerReference());
            appendLabel(row, Util.formatDateTime(customerCommunication.getCommunicationDate()));
            appendCheckbox(row, customerCommunication);
            appendOperations(row, customerCommunication);
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

        private void appendCheckbox(final Row row, final CustomerCommunication customerCommunication) {
            final Checkbox checkBoxReviewed = new Checkbox();
            checkBoxReviewed.setChecked(customerCommunication.getReviewed());

            checkBoxReviewed.addEventListener(Events.ON_CHECK,
                    arg0 -> {
                        customerCommunication.setReviewed(checkBoxReviewed.isChecked());
                        save(customerCommunication);
                        updateRowClass(row,checkBoxReviewed.isChecked());
                    });

            row.appendChild(checkBoxReviewed);
        }

        private void updateRowClass(final Row row, Boolean reviewed){
            row.setSclass("");
            if(!reviewed){
                row.setSclass("communication-not-reviewed");
            }
        }

        private void appendOperations(Row row, final CustomerCommunication customerCommunication) {
            Button buttonEdit = new Button();
            buttonEdit.setSclass("icono");
            buttonEdit.setImage("/common/img/ico_editar1.png");
            buttonEdit.setHoverImage("/common/img/ico_editar.png");
            buttonEdit.setTooltiptext(_("Edit"));
            buttonEdit.addEventListener(Events.ON_CLICK, arg0 -> goToEdit(customerCommunication));
            row.appendChild(buttonEdit);
        }
    }

    /**
     * Apply filter to customers communications.
     */
    public void onApplyFilter() {
        refreshCustomerCommunicationsList();
    }
}
