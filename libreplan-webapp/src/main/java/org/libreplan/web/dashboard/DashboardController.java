/*
 * This file is part of LibrePlan
 *
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

package org.libreplan.web.dashboard;

import static org.libreplan.web.I18nHelper._;

import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.common.Util;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.CategoryModel;
import org.zkoss.zul.Chart;
import org.zkoss.zul.Label;
import org.zkoss.zul.PieModel;
import org.zkoss.zul.SimpleCategoryModel;
import org.zkoss.zul.SimplePieModel;
import org.zkoss.zul.Window;

/**
 * Controller for dashboardfororder view
 * @author Nacho Barrientos <nacho@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DashboardController extends GenericForwardComposer {

    private IDashboardModel dashboardModel;

    private Window dashboardWindow;

    private Chart progressKPIglobalProgressChart;
    private Chart progressKPItaskStatusChart;
    private Chart progressKPItaskDeadlineViolationStatusChart;
    private Chart timeKPImarginWithDeadlineChart;
    private Chart timeKPIEstimationAccuracyChart;
    private Chart timeKPILagInTaskCompletionChart;

    private Label noTasksWarningLabel;

    public DashboardController() {
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.dashboardWindow = (Window)comp;
        Util.createBindingsFor(this.dashboardWindow);
    }

    public void setCurrentOrder(Order order) {
        dashboardModel.setCurrentOrder(order);
        if(dashboardModel.tasksAvailable()) {
            this.reloadCharts();
        } else {
            this.hideChartsAndShowWarningMessage();
        }
        if (this.dashboardWindow != null) {
            Util.reloadBindings(this.dashboardWindow);
        }
    }

    private void reloadCharts() {
        generateProgressKPIglobalProgressChart();
        generateProgressKPItaskStatusChart();
        generateProgressKPItaskDeadlineViolationStatusChart();
        generateTimeKPImarginWithDeadlineChart();
        generateTimeKPIEstimationAccuracyChart();
        generateTimeKPILagInTaskCompletionChart();
    }

    private void hideChartsAndShowWarningMessage() {
        progressKPIglobalProgressChart.setVisible(false);
        progressKPItaskStatusChart.setVisible(false);
        progressKPItaskDeadlineViolationStatusChart.setVisible(false);
        timeKPImarginWithDeadlineChart.setVisible(false);
        timeKPIEstimationAccuracyChart.setVisible(false);
        timeKPILagInTaskCompletionChart.setVisible(false);
        noTasksWarningLabel.setVisible(true);
    }

    private void generateTimeKPILagInTaskCompletionChart() {
        CategoryModel categoryModel;
        categoryModel = refreshTimeKPILagInTaskCompletionCategoryModel();
        Font labelFont = new Font("serif", Font.PLAIN, 10);
        timeKPILagInTaskCompletionChart.setXAxisTickFont(labelFont);
        Color[] seriesColorMappings = {Color.BLUE};
        timeKPILagInTaskCompletionChart.setAttribute("series-color-mappings",
                seriesColorMappings);
        timeKPILagInTaskCompletionChart.setModel(categoryModel);
    }

    private void generateTimeKPIEstimationAccuracyChart() {
        CategoryModel categoryModel;
        categoryModel = refreshTimeKPIEstimationAccuracyCategoryModel();
        Font labelFont = new Font("serif", Font.PLAIN, 10);
        timeKPIEstimationAccuracyChart.setXAxisTickFont(labelFont);
        Color[] seriesColorMappings = {Color.BLUE};
        timeKPIEstimationAccuracyChart.setAttribute("series-color-mappings",
                seriesColorMappings);
        timeKPIEstimationAccuracyChart.setModel(categoryModel);
    }

    private void generateTimeKPImarginWithDeadlineChart() {
        CategoryModel categoryModel;
        categoryModel = refreshTimeKPImarginWithDeadlineCategoryModel();
        if (categoryModel == null) { // Project has no deadline set.
            timeKPImarginWithDeadlineChart.setVisible(false);
            return;
        }
        timeKPImarginWithDeadlineChart.setAttribute("range-axis-lower-bound",
                new Double(-3.0));
        timeKPImarginWithDeadlineChart.setAttribute("range-axis-upper-bound",
                new Double(3.0));
        Color[] seriesColorMappings = new Color[1];
        if(dashboardModel.getMarginWithDeadLine().compareTo(BigDecimal.ZERO) >= 0) {
            seriesColorMappings[0] = Color.GREEN;
        } else {
            seriesColorMappings[0] = Color.RED;
        }
        timeKPImarginWithDeadlineChart.setAttribute("series-color-mappings",
                seriesColorMappings);
        timeKPImarginWithDeadlineChart.setModel(categoryModel);
    }

    private void generateProgressKPItaskStatusChart() {
        PieModel model = refreshProgressKPItaskStatusPieModel();
        progressKPItaskStatusChart.setModel(model);
    }

    private void generateProgressKPItaskDeadlineViolationStatusChart() {
        PieModel model = refreshProgressKPItaskDeadlieViolationStatusPieModel();
        progressKPItaskDeadlineViolationStatusChart.setModel(model);
    }

    private void generateProgressKPIglobalProgressChart() {
        CategoryModel categoryModel;
        categoryModel = refreshProgressKPIglobalProgressCategoryModel();
        progressKPIglobalProgressChart.setAttribute("range-axis-lower-bound",
                new Double(0.0));
        progressKPIglobalProgressChart.setAttribute("range-axis-upper-bound",
                new Double(100.0));
        progressKPIglobalProgressChart.setModel(categoryModel);
    }

    private PieModel refreshProgressKPItaskStatusPieModel() {
        PieModel model = new SimplePieModel();
        model.setValue(_("Finished"), dashboardModel.getPercentageOfFinishedTasks());
        model.setValue(_("In progress"), dashboardModel.getPercentageOfInProgressTasks());
        model.setValue(_("Ready to start"), dashboardModel.getPercentageOfReadyToStartTasks());
        model.setValue(_("Blocked"), dashboardModel.getPercentageOfBlockedTasks());
        return model;
    }

    private PieModel refreshProgressKPItaskDeadlieViolationStatusPieModel() {
        PieModel model = new SimplePieModel();
        model.setValue(_("On schedule"), dashboardModel.getPercentageOfOnScheduleTasks());
        model.setValue(_("Violated deadline"), dashboardModel.getPercentageOfTasksWithViolatedDeadline());
        model.setValue(_("No deadline"), dashboardModel.getPercentageOfTasksWithNoDeadline());
        return model;
    }

    private CategoryModel refreshProgressKPIglobalProgressCategoryModel() {
        CategoryModel result = new SimpleCategoryModel();
        result.setValue(_("Current"), _("All tasks (hours)"),
                dashboardModel.getAdvancePercentageByHours());
        result.setValue(_("Expected"), _("All tasks (hours)"),
                dashboardModel.getTheoreticalAdvancePercentageByHoursUntilNow());
        result.setValue(_("Current"), _("Critical path (hours)"),
                dashboardModel.getCriticalPathProgressByNumHours());
        result.setValue(_("Expected"), _("Critical path (hours)"), dashboardModel
                .getTheoreticalProgressByNumHoursForCriticalPathUntilNow());
        result.setValue(_("Current"), _("Critical path (duration)"),
                dashboardModel.getCriticalPathProgressByDuration());
        result.setValue(_("Expected"), _("Critical path (duration)"),
                dashboardModel.getTheoreticalProgressByDurationForCriticalPathUntilNow());
        return result;
    }

    private CategoryModel refreshTimeKPImarginWithDeadlineCategoryModel() {
        CategoryModel result = null;
        BigDecimal marginWithDeadLine = dashboardModel.getMarginWithDeadLine();
        if (marginWithDeadLine != null) {
            result = new SimpleCategoryModel();
            result.setValue(_("None"), _("Deviation"), marginWithDeadLine);
        }
        return result;
    }

    private CategoryModel refreshTimeKPIEstimationAccuracyCategoryModel() {
        CategoryModel result = new SimpleCategoryModel();
        List<Double> values = dashboardModel.getFinishedTasksEstimationAccuracyHistogram();
        Iterator<Double> it = values.iterator();
        for(int ii= DashboardModel.EA_STRETCHES_MIN_VALUE;
                ii < DashboardModel.EA_STRETCHES_MAX_VALUE;
                ii += DashboardModel.EA_STRETCHES_PERCENTAGE_STEP) {
            result.setValue(_("None"), _(String.valueOf(ii)), it.next());
        }
        result.setValue(_("None"),
                _(">"+DashboardModel.EA_STRETCHES_MAX_VALUE),
                it.next());
        return result;
    }

    private CategoryModel refreshTimeKPILagInTaskCompletionCategoryModel() {
        CategoryModel result = new SimpleCategoryModel();
        List<Double> values = dashboardModel.getLagInTaskCompletionHistogram();
        Iterator<Double> it = values.iterator();
        for(double ii= DashboardModel.LTC_STRETCHES_MIN_VALUE;
                ii < DashboardModel.LTC_STRETCHES_MAX_VALUE;
                ii += DashboardModel.LTC_STRETCHES_STEP) {
            result.setValue(_("None"), _(String.valueOf(ii)), it.next());
        }
        result.setValue(_("None"),
                _(">"+DashboardModel.LTC_STRETCHES_MAX_VALUE),
                it.next());
        return result;
    }
}
