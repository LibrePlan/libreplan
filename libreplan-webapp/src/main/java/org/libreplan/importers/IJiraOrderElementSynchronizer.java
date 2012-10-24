/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.importers.jira.Issue;

/**
 * Synchronize order elements inclusive progress assignments and measurements of
 * an existing order with Jira issues.
 *
 * Jira issues will be retrieved from Jira RESTful web service using
 * {@link JiraRESTClient}
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public interface IJiraOrderElementSynchronizer {

    /**
     * Gets all distinct jira lables from an external 'php' script.
     *
     * This is because at this moment Jira doesn't support Labels request. As
     * workaround we build a simple php script to do the query in Jira database
     * and returns a comma separated string(labels). Once Jira supports the
     * labels request this method will be modified.
     *
     * @return A list of labels
     */
    List<String> getAllJiraLabels();

    /**
     * Get all jira issues based on the specified <code>label</code> parameter
     * from jira RESTFul web service
     *
     * @param label
     *            search criteria for jira issues
     *
     * @return list of jira issues
     */
    List<Issue> getJiraIssues(String label);

    /**
     * Synchronizes the list of {@link OrderElement}s,
     * {@link DirectAdvanceAssignment}s and {@link AdvanceMeasurement}s of the
     * given {@link Order} with jira issues.
     *
     * Loops through all jira <code>issues</code> and check if an
     * {@link OrderElement} of the given <code>order</code> exists. If it
     * exists, update the {@link OrderElement} with the issue item. If not
     * create new {@link OrderElement}, update it with the issue item and add to
     * the <code>order</code> and start synchronization of
     * {@link DirectAdvanceAssignment} and {@link AdvanceMeasurement}
     *
     * @param order
     *            an existing order where its orderElements will be synchronized
     *            with jira issues
     * @param issues
     *            jira issues
     */
    void syncOrderElementsWithJiraIssues(Order order, List<Issue> issues);

    /**
     * returns synchronization info, success or fail info
     */
    JiraSyncInfo getJiraSyncInfo();

}
