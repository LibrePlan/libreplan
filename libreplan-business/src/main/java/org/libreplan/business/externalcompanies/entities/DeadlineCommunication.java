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

/**
 * Entity DeadlineCommunication
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class DeadlineCommunication extends BaseEntity {

    private Date saveDate;

    private Date deliverDate;

    protected DeadlineCommunication(){

    }

    private DeadlineCommunication(Date saveDate, Date deliverDate){
        this.setSaveDate(saveDate);
        this.setDeliverDate(deliverDate);
    }

    public static DeadlineCommunication create(Date saveDate, Date deliverDate){
        return create(new DeadlineCommunication(saveDate, deliverDate));
    }

    public static DeadlineCommunication create() {
        return create(new DeadlineCommunication());
    }

    public void setSaveDate(Date saveDate) {
        this.saveDate = saveDate;
    }

    @NotNull
    public Date getSaveDate() {
        return saveDate;
    }

    public void setDeliverDate(Date deliverDate) {
        this.deliverDate = deliverDate;
    }

    public Date getDeliverDate() {
        return deliverDate;
    }

}
