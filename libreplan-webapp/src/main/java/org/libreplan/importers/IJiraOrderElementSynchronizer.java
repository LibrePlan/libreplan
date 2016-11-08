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

package org.libreplan.importers;

import java.util.List;

import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.common.entities.ConnectorException;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderSyncInfo;
import org.libreplan.importers.jira.IssueDTO;

/**
 * Synchronize order elements inclusive progress assignments and measurements of an existing order with Jira issues.
 * Jira issues will be retrieved from Jira RESTful web service using {@link JiraRESTClient}.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public interface IJiraOrderElementSynchronizer {

    /**
     * Gets all distinct JIRA lables from an external 'php' script.
     *
     * FIXME: This is because at this moment Jira doesn't support Labels request.
     * As workaround we build a simple php script to do the query in
     * Jira database and returns a comma separated string(labels).
     * Once Jira supports the labels request this method will be modified.
     * More info: https://jira.atlassian.com/browse/JRA-29409
     *
     * @return A list of labels
     * @throws ConnectorException
     *             if connector not found
     */
    List<String> getAllJiraLabels() throws ConnectorException;

    /**
     * Get all JIRA issues based on the specified <code>label</code> parameter from JIRA RESTFul web service.
     *
     * @param label
     *            search criteria for JIRA issues
     *
     * @return list of JIRA issues
     * @throws ConnectorException
     *             if connector not found or contains invalid connection values
     */
    List<IssueDTO> getJiraIssues(String label) throws ConnectorException;

    /**
     * Synchronizes the list of {@link OrderElement}s,
     * {@link DirectAdvanceAssignment}s and {@link AdvanceMeasurement}s of the given {@link Order} with JIRA issues.
     *
     * Loops through all JIRA <code>issues</code> and check if an {@link OrderElement} of the given <code>order</code> exists.
     * If it exists, update the {@link OrderElement} with the issue item.
     * If not create new {@link OrderElement}, update it with the issue item and add to
     * the <code>order</code> and start synchronization of {@link DirectAdvanceAssignment} and {@link AdvanceMeasurement}.
     *
     * @param order
     *            an existing order where its orderElements will be synchronized
     *            with JIRA issues
     * @param issues
     *            JIRA issues
     */
    void syncOrderElementsWithJiraIssues(List<IssueDTO> issues, Order order);

    /**
     * Saves synchronization info.
     *
     * @param key
     *            the key(label)
     * @param order
     *            an order which already synchronized
     */
    void saveSyncInfo(String key, Order order);

    /**
     * Gets the most recent synchronized info.
     *
     * @param order
     *            the order
     * @return recent synchronized time sheet info
     */
    OrderSyncInfo getOrderLastSyncInfo(Order order);

    /**
     * Returns synchronization info, success or fail info.
     */
    SynchronizationInfo getSynchronizationInfo();

    /**
     * Synchronize order elements with JIRA issues if they already synchronized using
     * {@link IJiraOrderElementSynchronizer#syncOrderElementsWithJiraIssues(List, Order).
     *
     * It gets then an already synchronized orders from the {@link OrderSyncInfo} and re-synchronize them.
     *
     * @return a list of {@link SynchronizationInfo}
     *
     * @throws ConnectorException
     *             if connector not found or contains invalid connection values
     */
    List<SynchronizationInfo> syncOrderElementsWithJiraIssues() throws ConnectorException;
}
