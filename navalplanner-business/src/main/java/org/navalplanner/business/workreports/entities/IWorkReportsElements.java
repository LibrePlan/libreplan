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
package org.navalplanner.business.workreports.entities;

import java.util.Date;
import java.util.Set;

import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.workreports.valueobjects.DescriptionValue;

/**
 * Interface which must be implemented by {@link WorkReport} and
 * {@link WorkReportLine}
 *
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 *
 */
public interface IWorkReportsElements {

    Date getDate();

    void setDate(Date date);

    Resource getResource();

    void setResource(Resource resource);

    Set<Label> getLabels();

    void setLabels(Set<Label> labels);

    Set<DescriptionValue> getDescriptionValues();

    void setDescriptionValues(Set<DescriptionValue> descriptionValues);

    OrderElement getOrderElement();

    void setOrderElement(OrderElement orderElement);

}
