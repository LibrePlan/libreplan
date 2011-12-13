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

package org.libreplan.business.planner.entities;

import java.util.Date;

import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.externalcompanies.entities.DeliverDate;

/**
 *  Entity {@link SubcontractorDeliverDate}.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia>
 */
public class SubcontractorDeliverDate extends BaseEntity implements DeliverDate{

    private Date saveDate;

    private Date subcontractorDeliverDate;

    private Date communicationDate;

    protected SubcontractorDeliverDate(){

    }

    private SubcontractorDeliverDate(Date saveDate, Date subcontractorDeliverDate, Date communicationDate){
        this.setSaveDate(saveDate);
        this.setSubcontractorDeliverDate(subcontractorDeliverDate);
        this.setCommunicationDate(communicationDate);
    }

    public static SubcontractorDeliverDate create(){
        return create(new SubcontractorDeliverDate());
    }

    public static SubcontractorDeliverDate create(Date saveDate, Date subcontractorDeliverDate, Date communicationDate){
        return create(new SubcontractorDeliverDate(saveDate, subcontractorDeliverDate, communicationDate));
    }

    public void setSaveDate(Date saveDate) {
        this.saveDate = saveDate;
    }

    public Date getSaveDate() {
        return saveDate;
    }

    public void setSubcontractorDeliverDate(Date subcontractorDeliverDate) {
        this.subcontractorDeliverDate = subcontractorDeliverDate;
    }

    public Date getSubcontractorDeliverDate() {
        return subcontractorDeliverDate;
    }

    public void setCommunicationDate(Date communicationDate) {
        this.communicationDate = communicationDate;
    }

    public Date getCommunicationDate() {
        return communicationDate;
    }
}
