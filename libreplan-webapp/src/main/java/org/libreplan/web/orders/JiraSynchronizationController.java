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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.daos.IConnectorDAO;
import org.libreplan.business.common.entities.Connector;
import org.libreplan.business.common.entities.ConnectorException;
import org.libreplan.business.common.entities.PredefinedConnectors;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderSyncInfo;
import org.libreplan.importers.IJiraOrderElementSynchronizer;
import org.libreplan.importers.IJiraTimesheetSynchronizer;
import org.libreplan.importers.SynchronizationInfo;
import org.libreplan.importers.jira.IssueDTO;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Popup;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Window;

/**
 * Controller for JIRA synchronization.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class JiraSynchronizationController extends GenericForwardComposer {

    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(JiraSynchronizationController.class);

    private OrderCRUDController orderController;

    private Window editWindow;

    private Groupbox jiraGroupBox;

    private Popup jirasyncPopup;

    private Button startJiraSyncButton, cancelJiraSyncButton, syncWithJiraButton;

    private Textbox txtImportedLabel, txtLastSyncDate;

    private Combobox comboJiraLabel;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private IJiraOrderElementSynchronizer jiraOrderElementSynchronizer;

    private IJiraTimesheetSynchronizer jiraTimesheetSynchronizer;

    private IConnectorDAO connectorDAO;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        jiraOrderElementSynchronizer = (IJiraOrderElementSynchronizer) SpringUtil.getBean("jiraOrderElementSynchronizer");
        jiraTimesheetSynchronizer = (IJiraTimesheetSynchronizer) SpringUtil.getBean("jiraTimesheetSynchronizer");
        connectorDAO = (IConnectorDAO) SpringUtil.getBean("connectorDAO");

        comp.setAttribute("jiraSynchroniaztionController", this, true);
        loadComponentsEditWindow();
        showOrHideJiraEditWindow();
        updateOrderLastSyncInfoScreen();
    }

    public void setOrderController(OrderCRUDController orderController) {
        this.orderController = orderController;
    }

    /**
     * Returns current {@link Order}.
     */
    private Order getOrder() {
        return orderController.getOrder();
    }

    private void loadComponentsEditWindow() {
        txtLastSyncDate = (Textbox) editWindow.getFellowIfAny("txtLastSyncDate");
        txtImportedLabel = (Textbox) editWindow.getFellowIfAny("txtImportedLabel");
        jiraGroupBox = (Groupbox) editWindow.getFellowIfAny("jiraGroupBox");
        syncWithJiraButton = (Button) editWindow.getFellow("syncWithJiraButton");
        messagesForUser = new MessagesForUser(messagesContainer);
    }

    /**
     * Show or hide <code>JiraEditWindow</code> based on JIRA {@link Connector#isActivated()}.
     */
    private void showOrHideJiraEditWindow() {
        jiraGroupBox.setVisible(isJiraActivated());
    }

    /**
     * Updates the UI text last synchronized date and the text imported label.
     */
    private void updateOrderLastSyncInfoScreen() {
        OrderSyncInfo orderSyncInfo = jiraOrderElementSynchronizer.getOrderLastSyncInfo(getOrder());

        if ( orderSyncInfo != null ) {
            txtLastSyncDate.setValue(Util.formatDateTime(orderSyncInfo
                    .getLastSyncDate()));
            txtImportedLabel.setValue(orderSyncInfo.getKey());
        }
    }

    /**
     * Returns true if JIRA is Activated.
     * Used to show/hide JIRA edit window.
     */
    public boolean isJiraActivated() {
        Connector connector = connectorDAO.findUniqueByName(PredefinedConnectors.JIRA.getName());
        return connector != null && connector.isActivated();
    }

    /**
     * Synchronize with JIRA.
     */
    public void syncWithJira() {
        try {
            List<String> items = jiraOrderElementSynchronizer.getAllJiraLabels();

            if ( !(txtImportedLabel.getText()).isEmpty() ) {
                startSyncWithJira(txtImportedLabel.getText());

                return;
            }

            setupJiraSyncPopup(editWindow, new SimpleListModelExt(items));

            jirasyncPopup.open(syncWithJiraButton, "before_start");

        } catch (ConnectorException e) {
            messagesForUser.showMessage(Level.ERROR, _("Failed: {0}", e.getMessage()));
        } catch (WebApplicationException e) {
            LOG.info(e);
            messagesForUser.showMessage(Level.ERROR, _("Cannot connect to JIRA server"));
        }
    }

    /**
     * Start synchronize with jira for the specified <code>label</code>.
     *
     * @param label
     *            the JIRA label
     */
    public void startSyncWithJira(String label) {
        try {
            Order order = getOrder();

            List<IssueDTO> issues = jiraOrderElementSynchronizer.getJiraIssues(label);

            if ( issues == null || issues.isEmpty() ) {
                messagesForUser.showMessage(Level.ERROR, _("No JIRA issues to import"));
                return;
            }

            order.setCodeAutogenerated(false);

            jiraOrderElementSynchronizer.syncOrderElementsWithJiraIssues(issues, order);

            orderController.saveAndContinue(false);

            jiraOrderElementSynchronizer.saveSyncInfo(label, order);

            updateOrderLastSyncInfoScreen();

            if ( jirasyncPopup != null ) {
                jirasyncPopup.close();
            }

            jiraTimesheetSynchronizer.syncJiraTimesheetWithJiraIssues(issues, order);

            showSyncInfo();

            // Reload order info in all tabs
            Tab previousTab = orderController.getCurrentTab();
            orderController.initEdit(order);
            orderController.selectTab(previousTab.getId());
        } catch (ConnectorException e) {
            messagesForUser.showMessage(Level.ERROR, _("Failed: {0}", e.getMessage()));
        } catch (WebApplicationException e) {
            LOG.info(e);
            messagesForUser.showMessage(Level.ERROR, _("Cannot connect to JIRA server"));
        }
    }

    /**
     * Shows the success or failure info of synchronization.
     */
    private void showSyncInfo() {
        Map<String, Object> args = new HashMap<>();

        SynchronizationInfo syncOrderElementInfo = jiraOrderElementSynchronizer.getSynchronizationInfo();

        boolean succeeded = isSyncSucceeded(syncOrderElementInfo);

        args.put("syncOrderElementSuccess", succeeded);
        if ( syncOrderElementInfo != null ) {
            args.put("syncOrderElementFailedReasons", new SimpleListModel<>(syncOrderElementInfo.getFailedReasons()));
        }

        SynchronizationInfo jiraSyncInfoTimesheet = jiraTimesheetSynchronizer.getSynchronizationInfo();

        succeeded = isSyncSucceeded(jiraSyncInfoTimesheet);

        args.put("syncTimesheetSuccess", succeeded);
        if ( jiraSyncInfoTimesheet != null ) {
            args.put("syncTimesheetFailedReasons", new SimpleListModel<>(jiraSyncInfoTimesheet.getFailedReasons()));
        }

        Window jiraSyncInfoWindow = (Window) Executions.createComponents("/orders/_jiraSyncInfo.zul", null, args);

        try {
            jiraSyncInfoWindow.doModal();
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isSyncSucceeded(SynchronizationInfo syncInfo) {
        return syncInfo != null && syncInfo.isSuccessful();
    }

    /**
     * Setups the pop-up components.
     *
     * @param comp
     *            the compenent(editWidnow)
     * @param model
     *            labels list model to render the combobox
     *            <code>comboJiraLabel</code>
     */
    private void setupJiraSyncPopup(Component comp, ListModel model) {

        startJiraSyncButton = (Button) comp.getFellow("startJiraSyncButton");
        startJiraSyncButton.setLabel(_("Start sync"));

        startJiraSyncButton.addEventListener(Events.ON_CLICK, event -> startSyncWithJira(comboJiraLabel.getValue()));

        cancelJiraSyncButton = (Button) comp.getFellow("cancelJiraSyncButton");
        cancelJiraSyncButton.setLabel(_("Cancel"));

        cancelJiraSyncButton.addEventListener(Events.ON_CLICK, event -> jirasyncPopup.close());
        comboJiraLabel = (Combobox) comp.getFellowIfAny("comboJiraLabel");
        comboJiraLabel.setModel(model);

        jirasyncPopup = (Popup) comp.getFellow("jirasyncPopup");

    }

    /**
     * This class provides case insensitive search for the {@link Combobox}.
     */
    private class SimpleListModelExt extends SimpleListModel {

        public SimpleListModelExt(List data) {
            super(data);
        }

        public ListModel getSubModel(Object value, int nRows) {
            //TODO change deprecated method
            final String idx = value == null ? "" : objectToString(value);

            if ( nRows < 0 ) {
                nRows = 10;
            }
            final LinkedList data = new LinkedList();
            for (int i = 0; i < getSize(); i++) {
                if ( idx.equals("") || entryMatchesText(getElementAt(i).toString(), idx) ) {


                    if ( --nRows <= 0 ) {
                        break;
                    }
                }
            }

            return new SimpleListModelExt(data);
        }

        public boolean entryMatchesText(String entry, String text) {
            return entry.toLowerCase().contains(text.toLowerCase());
        }
    }

}
