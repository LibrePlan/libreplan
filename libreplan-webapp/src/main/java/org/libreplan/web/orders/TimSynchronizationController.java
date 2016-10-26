/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

package org.libreplan.web.orders;

import static org.libreplan.web.I18nHelper._;

import java.util.HashMap;
import java.util.Map;

import org.libreplan.business.common.daos.IConnectorDAO;
import org.libreplan.business.common.entities.Connector;
import org.libreplan.business.common.entities.ConnectorException;
import org.libreplan.business.common.entities.PredefinedConnectors;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderSyncInfo;
import org.libreplan.importers.IExportTimesheetsToTim;
import org.libreplan.importers.SynchronizationInfo;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Label;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Window;

/**
 * Controller for Tim synchronization.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
class TimSynchronizationController extends GenericForwardComposer {

    private OrderCRUDController orderController;

    private Window editWindow;

    private Groupbox timGroupBox;

    private Textbox txtProductCode;

    private Label labelProductCode, labelLastSyncDate;

    private IExportTimesheetsToTim exportTimesheetsToTim;

    private IConnectorDAO connectorDAO;

    private Component messagesContainer;

    private IMessagesForUser messagesForUser;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        exportTimesheetsToTim = (IExportTimesheetsToTim) SpringUtil.getBean("exportTimesheetsToTim");
        connectorDAO = (IConnectorDAO) SpringUtil.getBean("connectorDAO");

        comp.setAttribute("timSynchronizationController", this, true);
        loadComponentsEditWindow(comp);
        showOrHideTimEditWindow();
        updateOrderLastSyncInfoScreen();
    }

    public void setOrderController(OrderCRUDController orderController) {
        this.orderController = orderController;
    }

    /**
     * Returns current {@link Order}
     */
    private Order getOrder() {
        return orderController.getOrder();
    }

    private void loadComponentsEditWindow(Component comp) {
        txtProductCode = (Textbox) comp.getFellowIfAny("txtProductCode");
        labelLastSyncDate = (Label) comp.getFellowIfAny("labelLastSyncDate");
        labelProductCode = (Label) comp.getFellowIfAny("labelProductCode");
        timGroupBox = (Groupbox) comp.getFellowIfAny("timGroupBox");

        messagesForUser = new MessagesForUser(messagesContainer);
    }

    /**
     * Show or hide <code>TimEditWindow</code> based on Tim
     * {@link Connector#isActivated()}
     */
    private void showOrHideTimEditWindow() {
        timGroupBox.setVisible(isTimActivated());
    }

    /**
     * Updates the UI text last synchronized date and the text product code
     */
    private void updateOrderLastSyncInfoScreen() {
        OrderSyncInfo orderSyncInfo = exportTimesheetsToTim.getOrderLastSyncInfo(getOrder());
        if ( orderSyncInfo != null ) {
            labelLastSyncDate.setValue(Util.formatDateTime(orderSyncInfo.getLastSyncDate()));
            labelProductCode.setValue("(" + orderSyncInfo.getKey() + ")");
        }
    }

    /**
     * Returns true if Tim is Activated. Used to show/hide Tim edit window
     */
    public boolean isTimActivated() {
        Connector connector = connectorDAO.findUniqueByName(PredefinedConnectors.TIM.getName());

        return connector != null && connector.isActivated();
    }


    public void startExportToTim() {
        txtProductCode.setConstraint("no empty:" + _("cannot be empty"));
        try {
            exportTimesheetsToTim.exportTimesheets(txtProductCode.getValue(), getOrder());

            updateOrderLastSyncInfoScreen();

            shwoImpExpInfo();

        } catch (ConnectorException e) {
            messagesForUser.showMessage(Level.ERROR, _("Exporting timesheets to Tim failed. Check the Tim connector"));
        }
    }

    private void shwoImpExpInfo() {
        Map<String, Object> args = new HashMap<>();

        SynchronizationInfo synchronizationInfo = exportTimesheetsToTim.getSynchronizationInfo();
        args.put("action", synchronizationInfo.getAction());
        args.put("showSuccess", synchronizationInfo.isSuccessful());
        args.put("failedReasons", new SimpleListModel<>(synchronizationInfo.getFailedReasons()));

        Window timImpExpInfoWindow = (Window) Executions.createComponents("/orders/_timImpExpInfo.zul", null, args);

        try {
            timImpExpInfoWindow.doModal();
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        }
    }
}
