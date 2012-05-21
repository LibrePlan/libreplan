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

package org.libreplan.business.externalcompanies.entities;

import java.util.Date;

import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.orders.entities.Order;

/**
 * Entity CustomerCommunication
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CustomerCommunication extends BaseEntity {

    private Date deadline;

    private CommunicationType communicationType;

    private Date communicationDate;

    private Boolean reviewed = false;

    private Order order;

    protected CustomerCommunication() {
        this.setCommunicationDate(new Date());
    }

    private CustomerCommunication(Date deadline) {
        this.setDeadline(deadline);
        this.setCommunicationDate(new Date());
    }

    public static CustomerCommunication create() {
        return create(new CustomerCommunication());
    }

    public static CustomerCommunication createToday(Date deadline) {
        return create(new CustomerCommunication(deadline));
    }

    public static CustomerCommunication createTodayNewProject(Date deadline) {
        return create(new CustomerCommunication(deadline, new Date(),
                CommunicationType.NEW_PROJECT));
    }

    protected CustomerCommunication(Date deadline, Date communicationDate,
            CommunicationType communicationType) {
        this.setDeadline(deadline);
        this.setCommunicationDate(communicationDate);
        this.setCommunicationType(communicationType);
    }

    protected CustomerCommunication(Date deadline, Date communicationDate,
            CommunicationType type, Order order) {
        this.setDeadline(deadline);
        this.setCommunicationDate(communicationDate);
        this.setCommunicationType(type);
        this.setOrder(order);
    }

    public static CustomerCommunication create(Date deadline,
            Date communicationDate, CommunicationType communicationType) {
        return (CustomerCommunication) create(new CustomerCommunication(
                deadline, communicationDate, communicationType));
    }

    public static CustomerCommunication create(Date deadline,
            Date communicationDate, CommunicationType type,
            Order order) {
        return (CustomerCommunication) create(new CustomerCommunication(
                deadline, communicationDate, type, order));
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setCommunicationType(CommunicationType type) {
        this.communicationType = type;
    }

    public CommunicationType getCommunicationType() {
        return this.communicationType;
    }

    public void setCommunicationDate(Date communicationDate) {
        this.communicationDate = communicationDate;
    }

    public Date getCommunicationDate() {
        return communicationDate;
    }

    public void setReviewed(Boolean reviewed) {
        this.reviewed = reviewed;
    }

    public Boolean getReviewed() {
        return reviewed;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @NotNull(message = "order not specified")
    public Order getOrder() {
        return order;
    }
}
