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

package org.libreplan.web.subcontract;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * DTO UpdateDeliveringDate
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@XmlRootElement(name = "update-delivering-date")
public class UpdateDeliveringDateDTO {

    @XmlAttribute(name = "customer-reference")
    public String customerReference;

    @XmlAttribute(name = "external-code")
    public String externalCode;

    @XmlAttribute(name = "external-company-nif")
    public String externalCompanyNif;

    @XmlAttribute(name = "deliver-date")
    public XMLGregorianCalendar deliverDate;

    public UpdateDeliveringDateDTO(){
    }

    public UpdateDeliveringDateDTO(String externalCompanyNif,
            String customerReference, String externalCode,
            XMLGregorianCalendar deliverDate) {
        this.customerReference = customerReference;
        this.deliverDate = deliverDate;
        this.externalCompanyNif = externalCompanyNif;
        this.externalCode = externalCode;
    }
}
