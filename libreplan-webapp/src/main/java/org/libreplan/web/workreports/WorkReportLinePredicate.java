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

package org.libreplan.web.workreports;

import java.util.Date;

import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.zkoss.ganttz.IPredicate;

/**
 * Checks if {@link WorkReportLine} matches the constraints
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class WorkReportLinePredicate implements IPredicate {

    private Resource resource;
    private Date startDate;
    private Date finishDate;
    private OrderElement orderElement;
    private TypeOfWorkHours hourType;

    public WorkReportLinePredicate(Resource resource, Date startDate,
            Date finishDate, OrderElement orderElement, TypeOfWorkHours hourType) {
        this.resource = resource;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.orderElement = orderElement;
        this.hourType = hourType;
    }

    @Override
    public boolean accepts(Object object) {
        WorkReportLine line = (WorkReportLine) object;
        return (isEqual(resource, line.getResource())
                && isEqual(orderElement, line.getOrderElement())
                && isEqual(hourType, line.getTypeOfWorkHours()) && acceptFiltersDates(line));
    }

    private boolean isEqual(BaseEntity entityPredicate, BaseEntity entityLine) {
        if ((entityLine == null) || (entityPredicate == null)) {
            return true;
        }
        return entityPredicate.getId().equals(entityLine.getId());
    }

    private boolean acceptFiltersDates(WorkReportLine workReportLine) {
        // Check if exist work report items into interval between the start date
        // and finish date.
        if (isInTheRangeFilterDates(workReportLine.getDate())) {
            return true;
        }
        return false;
    }

    private boolean isInTheRangeFilterDates(Date date) {
        // Check if date is into interval between the startdate and finish date
        return (isGreaterToStartDate(date, startDate) && isLowerToFinishDate(
                date, finishDate));
    }

    private boolean isGreaterToStartDate(Date date, Date startDate) {
        if (startDate == null) {
            return true;
        }

        if (date != null && (date.compareTo(startDate) >= 0)) {
            return true;
        }
        return false;
    }

    private boolean isLowerToFinishDate(Date date, Date finishDate) {
        if (finishDate == null) {
            return true;
        }
        if (date != null && (date.compareTo(finishDate) <= 0)) {
            return true;
        }
        return false;
    }

}
