/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.business.orders.daos;

import java.util.Set;

import org.libreplan.business.common.daos.IGenericDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.SumChargedEffort;
import org.libreplan.business.workreports.entities.WorkReportLine;

/**
 * Contract for {@link SumChargedEffortDAO}
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public interface ISumChargedEffortDAO extends
        IGenericDAO<SumChargedEffort, Long> {

    /**
     * Update the {@link SumChargedEffort} objects with the changes in the
     * {@link WorkReportLine} set passed as argument. <br />
     *
     * If the {@link WorkReportLine} is new, the effort is added to the
     * corresponding {@link SumChargedEffort}. Otherwise, the difference of
     * effort is added or subtracted as required. <br />
     *
     * If there is not {@link SumChargedEffort} associated to the
     * {@link OrderElement} yet, it is created on demand.
     *
     * @param workReportLineSet
     */
    void updateRelatedSumChargedEffortWithWorkReportLineSet(
            Set<WorkReportLine> workReportLineSet);

    /**
     * Update the {@link SumChargedEffort} objects removing the values from the
     * {@link WorkReportLine} set passed as argument. <br />
     *
     * If the {@link WorkReportLine} is new, nothing is substracted. Otherwise,
     * the actual value saved in the database is substracted and not the one
     * coming in the objects passed.
     *
     * @param workReportLineSet
     */
    void updateRelatedSumChargedEffortWithDeletedWorkReportLineSet(
            Set<WorkReportLine> workReportLineSet);

    SumChargedEffort findByOrderElement(OrderElement orderElement);

    /**
     * Recalculates all the {@link SumChargedEffort} objets of an {@link Order}.
     * This is needed when some elements are moved inside the {@link Order}.
     *
     * @param order
     */
    void recalculateSumChargedEfforts(Order order);

}
