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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.libreplan.business.costcategories.daos.IHourCostDAO;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.workreports.daos.IWorkReportLineDAO;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Class to calculate the money cost of a {@link TaskElement}.<br />
 *
 * Money cost is calculated checking the hours reported for that task and using
 * the cost category of each resource in the different dates.<br />
 *
 * Money cost is stored in a map that will be cached in memeroy. This map could
 * be reseted when needed with method {@code resetMoneyCostMap}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class MoneyCostCalculator implements IMoneyCostCalculator {

    @Autowired
    private IWorkReportLineDAO workReportLineDAO;

    @Autowired
    private IHourCostDAO hourCostDAO;

    private Map<OrderElement, MoneyCost> moneyCostTotalMap = new HashMap<OrderElement, MoneyCost>();

    public class MoneyCost {
        private BigDecimal costOfHours;
        private BigDecimal costOfExpenses;

        public MoneyCost() {
        }

        public void setCostOfHours(BigDecimal costOfHours) {
            this.costOfHours = costOfHours;
        }

        public BigDecimal getCostOfHours() {
            return costOfHours;
        }

        public void setCostOfExpenses(BigDecimal costOfExpenses) {
            this.costOfExpenses = costOfExpenses;
        }

        public BigDecimal getCostOfExpenses() {
            return costOfExpenses;
        }

    }

    @Override
    public void resetMoneyCostMap() {
        moneyCostTotalMap = new HashMap<OrderElement, MoneyCost>();
    }

    @Override
    public BigDecimal getMoneyCostTotal(OrderElement orderElement) {
        BigDecimal result = BigDecimal.ZERO.setScale(2);
        BigDecimal moneyCostOfHours = getCostOfHours(orderElement);
        if (moneyCostOfHours != null) {
            result = result.add(moneyCostOfHours);
        }
        BigDecimal moneyCostOfExpenses = getCostOfExpenses(orderElement);
        if (moneyCostOfExpenses != null) {
            result = result.add(moneyCostOfExpenses).setScale(2, RoundingMode.HALF_UP);
        }
        return result;
    }

    @Override
    public BigDecimal getCostOfHours(OrderElement orderElement) {
        MoneyCost moneyCost = moneyCostTotalMap.get(orderElement);
        if (moneyCost != null) {
            BigDecimal result = moneyCost.getCostOfHours();
            if (result != null) {
                return result;
            }
        }

        BigDecimal result = BigDecimal.ZERO.setScale(2);
        for (OrderElement each : orderElement.getChildren()) {
            result = result.add(getCostOfHours(each));
        }

        result = result.add(getMoneyCostFromOwnWorkReportLines(orderElement))
                .setScale(2, RoundingMode.HALF_UP);

        if (moneyCost == null) {
            moneyCost = new MoneyCost();
        }
        moneyCost.setCostOfHours(result);
        moneyCostTotalMap.put(orderElement, moneyCost);
        return result;
    }

    private BigDecimal getMoneyCostFromOwnWorkReportLines(OrderElement orderElement) {
        List<WorkReportLine> workReportLines = workReportLineDAO
                .findByOrderElement(orderElement);

        BigDecimal result = BigDecimal.ZERO.setScale(2);
        for (WorkReportLine workReportLine : workReportLines) {
            BigDecimal priceCost = hourCostDAO
                    .getPriceCostFromResourceDateAndType(
                            workReportLine.getResource(),
                            workReportLine.getLocalDate(),
                            workReportLine.getTypeOfWorkHours());

            // If cost undefined via CostCategory get it from type
            if (priceCost == null) {
                priceCost = workReportLine.getTypeOfWorkHours()
                        .getDefaultPrice();
            }

            BigDecimal cost = priceCost.multiply(workReportLine.getEffort()
                    .toHoursAsDecimalWithScale(2));
            result = result.add(cost);
        }

        return result;
    }

    /**
     * Divides {@code moneyCost} by {@code budget} if {@code budget} is
     * different from 0. Otherwise, returns 0.
     *
     * @param moneyCost
     * @param budget
     * @return A BigDecimal from 0 to 1 with the proportion
     */
    public static BigDecimal getMoneyCostProportion(BigDecimal moneyCost, BigDecimal budget) {
        if (budget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return moneyCost.divide(budget, 2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getCostOfExpenses(OrderElement orderElement) {
        MoneyCost moneyCost = moneyCostTotalMap.get(orderElement);
        if (moneyCost != null) {
            BigDecimal result = moneyCost.getCostOfExpenses();
            if (result != null) {
                return result;
            }
        }

        BigDecimal result = BigDecimal.ZERO.setScale(2);
        if ((orderElement.getSumExpenses()) != null) {
            result = result.add(orderElement.getSumExpenses().getTotalDirectExpenses());
            result = result.add(orderElement.getSumExpenses().getTotalIndirectExpenses()).setScale(
                    2, RoundingMode.HALF_UP);
        }

        if (moneyCost == null) {
            moneyCost = new MoneyCost();
        }
        moneyCost.setCostOfExpenses(result);
        moneyCostTotalMap.put(orderElement, moneyCost);
        return result;
    }

}
