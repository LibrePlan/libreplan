/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 - ComtecSF S.L.
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

import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.navalplanner.ws.common.api.LabelReferenceDTO;

/**
 * Interface which must be implemented by {@link WorkReportDTO} and
 * {@link WorkReportLineDTO}
 *
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 *
 */
public interface IWorkReportDTOsElements {
    XMLGregorianCalendar getDate();

    void setDate(XMLGregorianCalendar calendar);

    String getResource();

    void setResource(String resource);

    String getOrderElement();

    void setOrderElement(String orderElement);

    Set<LabelReferenceDTO> getLabels();

    void setLabels(Set<LabelReferenceDTO> labels);

    Set<DescriptionValueDTO> getDescriptionValues();

    void setDescriptionValues(Set<DescriptionValueDTO> descriptionValues);

}
