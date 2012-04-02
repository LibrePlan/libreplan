/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 WirelessGalicia, S.L.
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

import org.libreplan.business.common.BaseEntity;

/**
 * Entity EndDateCommunicationToCustomer
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class EndDateCommunicationToCustomer extends BaseEntity {

    private Date saveDate;

    private Date endDate;

    private Date communicationDate;

    protected EndDateCommunicationToCustomer() {
        this.setSaveDate(new Date());
    }

    protected EndDateCommunicationToCustomer(Date endDate) {
        this.setEndDate(endDate);
        this.setSaveDate(new Date());
    }

    protected EndDateCommunicationToCustomer(Date saveDate, Date endDate,
 Date communicationDate) {
        this.setSaveDate(saveDate);
        this.setEndDate(endDate);
        this.setCommunicationDate(communicationDate);
    }

    public static EndDateCommunicationToCustomer create() {
        return create(new EndDateCommunicationToCustomer());
    }

    public static EndDateCommunicationToCustomer create(Date endDate) {
        return create(new EndDateCommunicationToCustomer(endDate));
    }

    public static EndDateCommunicationToCustomer create(Date saveDate, Date endDate,
            Date communicationDate) {
        return create(new EndDateCommunicationToCustomer(saveDate, endDate, communicationDate));
    }

    public void setSaveDate(Date saveDate) {
        this.saveDate = saveDate;
    }

    public Date getSaveDate() {
        return saveDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setCommunicationDate(Date communicationDate2) {
        this.communicationDate = communicationDate2;
    }

    public Date getCommunicationDate() {
        return communicationDate;
    }

}
