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
import java.util.Date;

import org.joda.time.LocalDate;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.common.Util;
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
        BigDecimal budgetedCost = costStatusModel
                .getBudgetedCostWorkPerformedAt(today);
        BigDecimal actualCost = costStatusModel
                .getActualCostWorkPerformedAt(today);

        BigDecimal costVariance = costStatusModel.getCostVariance(budgetedCost,
                actualCost);
        setCostVariance(costVariance);

        BigDecimal costPerformanceIndex = costStatusModel
                .getCostPerformanceIndex(budgetedCost, actualCost);
        setCostPerformanceIndex(costPerformanceIndex);

        BigDecimal budgetAtCompletion = costStatusModel.getBudgetAtCompletion();
        setBudgetAtCompletion(budgetAtCompletion);

        BigDecimal estimateAtCompletion = costStatusModel
                .getEstimateAtCompletion(budgetAtCompletion,
                        costPerformanceIndex);
        setEstimateAtCompletion(estimateAtCompletion);

        BigDecimal varianceAtCompletion = costStatusModel
                .getVarianceAtCompletion(budgetAtCompletion,
                        estimateAtCompletion);
        setVarianceAtCompletion(varianceAtCompletion);
    }

    private void setEstimateAtCompletion(BigDecimal value) {
        lblEAC.setValue(String.format("%.2f %%", value.doubleValue()));
    }

    private void setCostPerformanceIndex(BigDecimal value) {
        lblCPI.setValue(String.format("%.2f %%", value.doubleValue()));
    }

    private void setBudgetAtCompletion(BigDecimal value) {
        lblBAC.setValue(String.format(_("%s h"), value.toString()));
    }

    private void setCostVariance(BigDecimal value) {
        lblCV.setValue(String.format(_("%s h"), value.toString()));
    }

    private void setVarianceAtCompletion(BigDecimal value) {
        lblVAC.setValue(String.format(_("%s h"), value.toString()));
    }

}