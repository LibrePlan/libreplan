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
 * Entity CustomerComunication
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CustomerComunication extends BaseEntity{

    private Date deadline;

    private ComunicationType comunicationType;

    private Date comunicationDate;

    private Boolean reviewed = false;

    private Order order;

    protected CustomerComunication() {
        this.setComunicationDate(new Date());
    }

    private CustomerComunication(Date deadline) {
        this.setDeadline(deadline);
        this.setComunicationDate(new Date());
    }

    public static CustomerComunication create() {
        return create(new CustomerComunication());
    }

    public static CustomerComunication createToday(Date deadline) {
        return create(new CustomerComunication(deadline));
    }

    public static CustomerComunication createTodayNewProject(Date deadline) {
        return create(new CustomerComunication(deadline,
                new Date(), ComunicationType.NEW_PROJECT));
    }

    protected CustomerComunication(Date deadline, Date comunicationDate, ComunicationType comunicationType) {
        this.setDeadline(deadline);
        this.setComunicationDate(comunicationDate);
        this.setComunicationType(comunicationType);
    }

    protected CustomerComunication(Date deadline, Date comunicationDate, ComunicationType comunicationType, Order order) {
        this.setDeadline(deadline);
        this.setComunicationDate(comunicationDate);
        this.setComunicationType(comunicationType);
        this.setOrder(order);
    }

    public static CustomerComunication create(Date deadline, Date comunicationDate, ComunicationType comunicationType) {
        return (CustomerComunication) create(new CustomerComunication(deadline, comunicationDate, comunicationType));
    }

    public static CustomerComunication create(Date deadline, Date comunicationDate, ComunicationType comunicationType, Order order) {
        return (CustomerComunication) create(new CustomerComunication(deadline, comunicationDate, comunicationType, order));
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setComunicationType(ComunicationType comunicationType) {
        this.comunicationType = comunicationType;
    }

    public ComunicationType getComunicationType() {
        return comunicationType;
    }

    public void setComunicationDate(Date comunicationDate) {
        this.comunicationDate = comunicationDate;
    }

    public Date getComunicationDate() {
        return comunicationDate;
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
