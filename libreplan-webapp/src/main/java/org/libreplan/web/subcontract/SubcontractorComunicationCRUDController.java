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

package org.libreplan.web.subcontract;

import static org.libreplan.web.I18nHelper._;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.externalcompanies.entities.ComunicationType;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.entities.SubcontractedTaskData;
import org.libreplan.business.planner.entities.SubcontractorComunication;
import org.libreplan.business.planner.entities.SubcontractorComunicationValue;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
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
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;

/**
 * Controller for CRUD actions over a {@link SubcontractorComunication}
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@SuppressWarnings("serial")
public class SubcontractorComunicationCRUDController extends GenericForwardComposer {

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(SubcontractorComunicationCRUDController.class);

    private ISubcontractorComunicationModel subcontractorComunicationModel;

    private SubcontractorComunicationRenderer subcontractorComunicationRenderer = new SubcontractorComunicationRenderer();;

    protected IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private Grid listing;

    private Grid listingValues;

    private Label labelValue;

    private Popup pp;

    @Resource
    private IGlobalViewEntryPoints globalView;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("controller", this);
        messagesForUser = new MessagesForUser(messagesContainer);
    }

    public void goToEdit(SubcontractorComunication subcontractorComunication) {
        if(subcontractorComunication != null){
            TaskElement task = subcontractorComunication.getSubcontractedTaskData().getTask();
            OrderElement orderElement = task.getOrderElement();
            Order order = subcontractorComunicationModel.getOrder(orderElement);
            globalView.goToAdvanceTask(order,task);
        }
    }

    public FilterComunicationEnum[] getFilterItems(){
        return FilterComunicationEnum.values();
    }

    public FilterComunicationEnum getCurrentFilterItem() {
       return subcontractorComunicationModel.getCurrentFilter();
    }

    public void setCurrentFilterItem(FilterComunicationEnum selected) {
        subcontractorComunicationModel.setCurrentFilter(selected);

        // update the subcontractor comunication list
        listing.setModel(new SimpleListModel(getSubcontractorComunications()));
        listing.invalidate();
    }

    protected void save(SubcontractorComunication subcontractorComunication)
            throws ValidationException {
        subcontractorComunicationModel.confirmSave(subcontractorComunication);
    }

    public List<SubcontractorComunication> getSubcontractorComunications() {
        FilterComunicationEnum currentFilter = subcontractorComunicationModel.getCurrentFilter();
        switch(currentFilter){
            case ALL: return subcontractorComunicationModel.getSubcontractorAllComunications();
            case NOT_REVIEWED: return subcontractorComunicationModel.getSubcontractorComunicationWithoutReviewed();
            default: return subcontractorComunicationModel.getSubcontractorAllComunications();
        }
    }

    public SubcontractorComunicationRenderer getSubcontractorComunicationRenderer() {
        return subcontractorComunicationRenderer;
    }

    private class SubcontractorComunicationRenderer implements
            RowRenderer {

        @Override
        public void render(Row row, Object data) {
            SubcontractorComunication subcontractorComunication = (SubcontractorComunication) data;
            row.setValue(subcontractorComunication);

            appendLabel(row, subcontractorComunication.getComunicationType().toString());
            appendLabel(row, subcontractorComunication.getSubcontractedTaskData().getTask().getName());
            appendLabel(row,  getOrderName(subcontractorComunication.getSubcontractedTaskData()));
            appendLabel(row,  getOrderCode(subcontractorComunication.getSubcontractedTaskData()));
            appendLabel(row, subcontractorComunication.getSubcontractedTaskData().getExternalCompany().getName());
            appendLabel(row, toString(subcontractorComunication.getComunicationDate()));
            appendLabelWitTooltip(row,subcontractorComunication);
            appendCheckbox(row, subcontractorComunication);
            appendOperations(row, subcontractorComunication);
        }

        private String toString(Date date) {
            if (date == null) {
                return "";
            }

            return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
        }

        private String getOrderCode(SubcontractedTaskData subcontractedTaskData) {
            return subcontractorComunicationModel.getOrderCode(subcontractedTaskData);
        }

        private String getOrderName(SubcontractedTaskData subcontractedTaskData) {
            return subcontractorComunicationModel.getOrderName(subcontractedTaskData);
        }

        private String getLastValue(
                SubcontractorComunication subcontractorComunication) {
            SubcontractorComunicationValue value = subcontractorComunication
                    .getLastSubcontratorComunicationValues();
            return (value != null) ? value.toString() : "";
        }

        private void appendLabel(Row row, String label) {
            row.appendChild(new Label(label));
        }

        private void appendLabelWitTooltip(final Row row,final SubcontractorComunication subcontractorComunication) {
            String lastValue = getLastValue(subcontractorComunication);
            final Label compLabel = new Label(lastValue);

            compLabel.setTooltip(pp);
            compLabel.addEventListener(Events.ON_MOUSE_OVER,
                    new EventListener() {
                        @Override
                        public void onEvent(Event arg0) throws Exception {
                            List<SubcontractorComunicationValue> model = subcontractorComunication
                                    .getSubcontratorComunicationValues();
                            listingValues.setModel(new SimpleListModel(model));
                            listingValues.invalidate();
                        }
                    });
            row.appendChild(compLabel);
        }

        private void appendCheckbox(Row row,
                final SubcontractorComunication subcontractorComunication) {
            final Checkbox checkBoxReviewed = new Checkbox();
            checkBoxReviewed.setChecked(subcontractorComunication.getReviewed());

            checkBoxReviewed.addEventListener(Events.ON_CHECK,
                    new EventListener() {

                        @Override
                        public void onEvent(Event arg0) throws Exception {
                            subcontractorComunication.setReviewed(checkBoxReviewed.isChecked());
                            save(subcontractorComunication);
                        }

                    });

            row.appendChild(checkBoxReviewed);
        }

        private void appendOperations(Row row,
                final SubcontractorComunication subcontractorComunication) {
            Button buttonEdit = new Button(_("edit"));
            buttonEdit.addEventListener(Events.ON_CLICK, new EventListener() {

                @Override
                public void onEvent(Event arg0) throws Exception {
                    goToEdit(subcontractorComunication);
                }

            });
            row.appendChild(buttonEdit);
        }
    }

}