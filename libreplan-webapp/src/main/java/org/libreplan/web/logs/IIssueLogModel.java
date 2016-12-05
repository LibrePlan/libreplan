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

package org.libreplan.web.logs;

import java.util.List;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.logs.entities.IssueLog;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.users.entities.User;

/**
 * Contract for {@link IssueLogModel}
 * 
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public interface IIssueLogModel {

    /**
     * Returns a list of all {@link IssueLog}
     */
    List<IssueLog> getIssueLogs();

    /**
     * Returns a list of all {@link Order}
     */
    List<Order> getOrders();

    /**
     * Returns current {@link Order}
     */
    Order getOrder();

    /**
     * Sets the order
     * 
     * @param order
     *            the order to be set
     */
    void setOrder(Order order);

    /**
     * Returns a list of all {@link User}
     */
    List<User> getUsers();

    /**
     * Returns the {@link User}
     */
    User getCreatedBy();

    /**
     * Sets the user
     * 
     * @param user
     *            the user to be set
     */
    void setCreatedBy(User user);

    /**
     * Prepares for create a new {@link IssueLog}.
     */
    void initCreate();

    /**
     * Prepares for edit {@link IssueLog}
     * 
     * @param issueLog
     *            an object to be edited
     */
    void initEdit(IssueLog issueLog);

    /**
     * Gets the current {@link IssueLog}.
     * 
     * @return A {@link IssueLog}
     */
    IssueLog getIssueLog();

    /**
     * Saves the current {@link IssueLog}
     * 
     * @throws ValidationException
     *             if validation fails
     */
    void confirmSave() throws ValidationException;

    /**
     * Cancels the current {@link IssueLog}
     */
    void cancel();

    /**
     * Removes the current {@link IssueLog}
     * 
     * @param issueLog
     *            an object to be removed
     */
    void remove(IssueLog issueLog);

    /**
     * Returns a list of {@link IssueLog} for a specified {@link Order}
     *
     * @param order parent element for IssueLogs
     */
    List<IssueLog> getByParent(Order order);

    /**
     * Setter for {@link IssueLog}
     */
    void setIssueLog(IssueLog log);
}
