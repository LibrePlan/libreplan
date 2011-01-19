/*
 * This file is part of NavalPlan
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
package org.navalplanner.web.planner;

import java.util.Date;
import java.util.List;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.web.common.components.finders.FilterPair;
import org.navalplanner.web.orders.OrderPredicate;
import org.zkoss.ganttz.IPredicate;

/**
 * Checks if {@link Order}, the start date and finish date from associated
 * {@link Task} matches attributes
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CompanyPredicate extends OrderPredicate implements IPredicate {

    public CompanyPredicate(List<FilterPair> filters, Date startDate,
            Date finishDate, Boolean includeOrderElements) {
        super(filters, startDate, finishDate, includeOrderElements);
    }

    @Override
    protected boolean acceptFiltersDates(Order order) {
        TaskGroup associatedTaskElement = order.getAssociatedTaskElement();
        return (this.acceptStartDate(associatedTaskElement.getStartDate()) && (acceptFinishDate(associatedTaskElement
                .getEndDate())));
    }
}