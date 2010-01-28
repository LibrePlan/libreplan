/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.ws.workreports.api;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.ws.common.api.IntegrationEntityDTO;
import org.navalplanner.ws.common.api.LabelReferenceDTO;

/**
 * DTO for {@link WorkReport} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@XmlRootElement(name = "work-report")
public class WorkReportDTO extends IntegrationEntityDTO {

    public final static String ENTITY_TYPE = "work-report";

    @XmlAttribute(name = "work-report-type")
    public String workReportType;

    @XmlAttribute
    public Date date;

    @XmlAttribute
    public String resource;

    @XmlAttribute(name = "work-order")
    public String orderElement;

    @XmlElementWrapper(name = "label-list")
    @XmlElement(name = "label")
    public Set<LabelReferenceDTO> labels = new HashSet<LabelReferenceDTO>();

    @XmlElementWrapper(name = "text-field-list")
    @XmlElement(name = "text-field")
    public Set<DescriptionValueDTO> descriptionValues = new HashSet<DescriptionValueDTO>();

    @XmlElementWrapper(name = "work-report-line-list")
    @XmlElement(name = "work-report-line")
    public Set<WorkReportLineDTO> workReportLines = new HashSet<WorkReportLineDTO>();

    public WorkReportDTO() {
    }

    public WorkReportDTO(String code, String workReportType, Date date,
            String resource, String orderElement, Set<LabelReferenceDTO> labels,
            Set<DescriptionValueDTO> descriptionValues,
            Set<WorkReportLineDTO> workReportLines) {
        super(code);
        this.workReportType = workReportType;
        this.date = date;
        this.resource = resource;
        this.orderElement = orderElement;
        this.labels = labels;
        this.descriptionValues = descriptionValues;
        this.workReportLines = workReportLines;
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}