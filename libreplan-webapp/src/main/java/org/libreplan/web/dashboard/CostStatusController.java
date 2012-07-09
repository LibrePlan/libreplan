/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.web.dashboard;

import static org.libreplan.web.I18nHelper._;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import org.joda.time.LocalDate;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.common.Util;
import org.libreplan.web.planner.chart.EarnedValueChartFiller.EarnedValueType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;

/**
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         Contains operations for calculations in the CostStatus table in the
 *         Dashboard view
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CostStatusController extends GenericForwardComposer {

    private ICostStatusModel costStatusModel;

    // Cost Variance
    public Label lblCV;

    // Cost Performance Index
    public Label lblCPI;

    // Budget at Completion
    public Label lblBAC;

    // Estimate at Completion
    public Label lblEAC;

    // Variance at Completion
    public Label lblVAC;

    // Cost Variance
    public Label lblACWP;

    // Estimate To Complete
    public Label lblETC;

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        super.doAfterCompose(comp);
        self.setAttribute("controller", this);
        Util.createBindingsFor(self);
    }

    public void setOrder(Order order) {
        costStatusModel.setCurrentOrder(order);
    }

    public void render() {
        LocalDate today = LocalDate.fromDateFields(new Date());
        BigDecimal actualCost = costStatusModel
                .getActualCostWorkPerformedAt(today);
        setHoursLabel(lblACWP, actualCost);

        BigDecimal budgetedCost = costStatusModel
                .getBudgetedCostWorkPerformedAt(today);
        BigDecimal costVariance = costStatusModel.getCostVariance(budgetedCost,
                actualCost);
        setHoursLabel(lblCV, costVariance);

        BigDecimal costPerformanceIndex = costStatusModel
                .getCostPerformanceIndex(budgetedCost, actualCost);
        setPercentageLabel(lblCPI, costPerformanceIndex);

        BigDecimal budgetAtCompletion = costStatusModel.getBudgetAtCompletion();
        setHoursLabel(lblBAC, budgetAtCompletion);

        BigDecimal estimateAtCompletion = costStatusModel
                .getEstimateAtCompletion(budgetAtCompletion,
                        costPerformanceIndex);
        setHoursLabel(lblEAC, estimateAtCompletion);

        BigDecimal varianceAtCompletion = costStatusModel
                .getVarianceAtCompletion(budgetAtCompletion,
                        estimateAtCompletion);
        setHoursLabel(lblVAC, varianceAtCompletion);

        BigDecimal estimateToComplete = costStatusModel.getEstimateToComplete(
                estimateAtCompletion, actualCost);
        setHoursLabel(lblETC, estimateToComplete);
    }

    private void setHoursLabel(Label label, BigDecimal value) {
        label.setValue(_("{0} h", value.setScale(2, RoundingMode.HALF_UP)));
    }

    private void setPercentageLabel(Label label, BigDecimal value) {
        label.setValue(value.setScale(2, RoundingMode.HALF_UP) + " %");
    }

    public String getLabel(EarnedValueType type) {
        return type.getAcronym() + " (" + type.getName() + ")";
    }

    public String getLabelCV() {
        return getLabel(EarnedValueType.CV);
    }

    public String getLabelACWP() {
        return getLabel(EarnedValueType.ACWP);
    }

    public String getLabelCPI() {
        return getLabel(EarnedValueType.CPI);
    }

    public String getLabelETC() {
        return getLabel(EarnedValueType.ETC);
    }

    public String getLabelEAC() {
        return getLabel(EarnedValueType.EAC);
    }

    public String getLabelBAC() {
        return getLabel(EarnedValueType.BAC);
    }

    public String getLabelVAC() {
        return getLabel(EarnedValueType.VAC);
    }

}
