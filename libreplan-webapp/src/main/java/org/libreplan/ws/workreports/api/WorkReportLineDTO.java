/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.ws.workreports.api;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.ws.common.api.IntegrationEntityDTO;
import org.libreplan.ws.common.api.LabelReferenceDTO;

/**
 * DTO for {@link WorkReportLine} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@XmlRootElement(name = "work-report-line")
@XmlAccessorType(XmlAccessType.FIELD)
public class WorkReportLineDTO extends IntegrationEntityDTO implements
        IWorkReportDTOsElements {

    public final static String ENTITY_TYPE = "work-report-line";

    @XmlAttribute
    public XMLGregorianCalendar date;

    @XmlAttribute
    public String resource;

    @XmlAttribute(name = "work-order")
    public String orderElement;

    @XmlAttribute(name = "hour-type")
    public String typeOfWorkHours;

    @XmlAttribute(name = "start-hour")
    public XMLGregorianCalendar clockStart;

    @XmlAttribute(name = "finish-hour")
    public XMLGregorianCalendar clockFinish;

    @XmlAttribute(name = "hours")
    public String numHours;

    @XmlElementWrapper(name = "label-list")
    @XmlElement(name = "label")
    public Set<LabelReferenceDTO> labels = new HashSet<LabelReferenceDTO>();

    @XmlElementWrapper(name = "text-field-list")
    @XmlElement(name = "text-field")
    public Set<DescriptionValueDTO> descriptionValues = new HashSet<DescriptionValueDTO>();

    public WorkReportLineDTO() {
    }

    public WorkReportLineDTO(String code, XMLGregorianCalendar date,
            String resource,
 String orderElement, String typeOfWorkHours,
            XMLGregorianCalendar clockStart, XMLGregorianCalendar clockFinish,
            String numHours, Set<LabelReferenceDTO> labels,
            Set<DescriptionValueDTO> descriptionValues) {
        super(code);
        this.date = date;
        this.resource = resource;
        this.orderElement = orderElement;
        this.typeOfWorkHours = typeOfWorkHours;
        this.clockStart = clockStart;
        this.clockFinish = clockFinish;
        this.numHours = numHours;
        this.labels = labels;
        this.descriptionValues = descriptionValues;
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

    @Override
    public XMLGregorianCalendar getDate() {
        return date;
    }

    @Override
    public void setDate(XMLGregorianCalendar calendar) {
        this.date = calendar;

    }

    @Override
    public String getResource() {
        return resource;
    }

    @Override
    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public String getOrderElement() {
        return orderElement;
    }

    @Override
    public void setOrderElement(String orderElement) {
        this.orderElement = orderElement;
    }

    @Override
    public Set<LabelReferenceDTO> getLabels() {
        return labels;
    }

    @Override
    public void setLabels(Set<LabelReferenceDTO> labels) {
        this.labels = labels;
    }

    @Override
    public Set<DescriptionValueDTO> getDescriptionValues() {
        return descriptionValues;
    }

    @Override
    public void setDescriptionValues(Set<DescriptionValueDTO> descriptionValues) {
        this.descriptionValues = descriptionValues;
    }

}
