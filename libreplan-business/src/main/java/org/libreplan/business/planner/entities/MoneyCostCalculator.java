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

package org.libreplan.business.planner.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.libreplan.business.costcategories.daos.IHourCostDAO;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.workreports.daos.IWorkReportLineDAO;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Class to calculate the money cost of a {@link TaskElement}.
 *
 * Money cost is calculated checking the hours reported for that task and using
 * the cost category of each resource in the different dates.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
/**
 * @author mrego
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class MoneyCostCalculator implements IMoneyCostCalculator {

    @Autowired
    private IWorkReportLineDAO workReportLineDAO;

    @Autowired
    private IHourCostDAO hourCostDAO;

    @Override
    public BigDecimal getMoneyCost(OrderElement orderElement) {
        List<WorkReportLine> workReportLines = workReportLineDAO
                .findByOrderElementAndChildren(orderElement, false);

        BigDecimal result = BigDecimal.ZERO.setScale(2);
        for (WorkReportLine workReportLine : workReportLines) {
            BigDecimal priceCost = hourCostDAO
                    .getPriceCostFromResourceDateAndType(
                            workReportLine.getResource(),
                            workReportLine.getLocalDate(),
                            workReportLine.getTypeOfWorkHours());
            BigDecimal cost = priceCost.multiply(workReportLine.getEffort()
                    .toHoursAsDecimalWithScale(2));
            result = result.add(cost);
        }

        return result.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Divides {@code moneyCost} by {@code budget} if {@code budget} is
     * different from 0. Otherwise, returns 0.
     *
     * @param moneyCost
     * @param budget
     * @return A BigDecimal from 0 to 1 with the proportion
     */
    public static BigDecimal getMoneyCostProportion(BigDecimal moneyCost,
            BigDecimal budget) {
        if (budget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return moneyCost.divide(budget);
    }

}
