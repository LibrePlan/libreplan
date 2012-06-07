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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.TaskStatusEnum;
import org.libreplan.web.dashboard.DashboardModel.Interval;
import org.libreplan.web.planner.order.OrderPlanningController;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import br.com.digilabs.jqplot.Chart;
import br.com.digilabs.jqplot.JqPlotUtils;
import br.com.digilabs.jqplot.chart.BarChart;
import br.com.digilabs.jqplot.chart.PieChart;
import br.com.digilabs.jqplot.elements.Serie;

/**
 * @author Nacho Barrientos <nacho@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         Controller for dashboardfororder view
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DashboardController extends GenericForwardComposer {

    private IDashboardModel dashboardModel;

    private Label lblOvertimeRatio;
    private Label lblAvailabilityRatio;
    private Label lblAbsolute;

    private org.zkoss.zk.ui.Component costStatus;

    private Div projectDashboardChartsDiv;
    private Div projectDashboardNoTasksWarningDiv;

    public DashboardController() {
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        super.doAfterCompose(comp);
    }

    public String loadResourceFile(String filename) {
        final String newline = "\n";

        ApplicationContext ctx = new ClassPathXmlApplicationContext();
        Resource res = ctx.getResource(filename);
        BufferedReader reader;
        StringBuilder sb = new StringBuilder();
        try {
           reader = new BufferedReader(new InputStreamReader(res.getInputStream()));
           String line;

           while ((line = reader.readLine()) != null) {
              sb.append(line);
              sb.append(newline);
           }
        } catch (IOException e) {
            System.out.println(e);
        }
        return sb.toString();
    }

    public void setCurrentOrder(Order order, OrderPlanningController orderPlanningController) {
        dashboardModel.setCurrentOrder(order, orderPlanningController);
        if (dashboardModel.tasksAvailable()) {
            if (self != null) {
                renderGlobalProgress();
                renderTaskStatus();
                renderTaskCompletationLag();
                renderDeadlineViolation();
                renderMarginWithDeadline();
                renderEstimationAccuracy();
                renderCostStatus(order);
                renderOvertimeRatio();
                renderAvailabilityRatio();
            }
            showCharts();
        } else {
            hideCharts();
        }
    }

    private void renderOvertimeRatio() {
        lblOvertimeRatio.setValue(String.format("%.2f", dashboardModel
                .getOvertimeRatio().doubleValue()));
        String valueMeaning = (dashboardModel.getOvertimeRatio().doubleValue() > 1) ? "negative"
                : "positive";
        lblOvertimeRatio.setSclass("dashboard-label-remarked " + valueMeaning);
    }

    private void renderAvailabilityRatio() {
        lblAvailabilityRatio.setValue(String.format("%.2f", dashboardModel
                .getAvailabilityRatio().doubleValue()));
        String valueMeaning = (dashboardModel.getAvailabilityRatio()
                .doubleValue() > 1) ? "negative" : "positive";
        lblAvailabilityRatio.setSclass("dashboard-label-remarked "
                + valueMeaning);
    }

    private void renderCostStatus(Order order) {
        CostStatusController costStatusController = getCostStatusController();
        costStatusController.setOrder(order);
        costStatusController.render();
    }

    private CostStatusController getCostStatusController() {
        return (CostStatusController) costStatus.getAttribute("controller");
    }

    private void renderMarginWithDeadline() {

        Integer absoluteMargin = dashboardModel.getAbsoluteMarginWithDeadLine();
        BigDecimal relativeMargin = dashboardModel.getMarginWithDeadLine();

        if ((lblAbsolute != null) && (absoluteMargin != null)) {
            lblAbsolute
                    .setValue(String
                            .format(_("There is a margin of %d days with the project global deadline (%.2f %%)."),
                                    absoluteMargin + 0,
                                    relativeMargin.doubleValue() * 100));
        } else {
            lblAbsolute
                    .setValue(_("It has not been defined a project deadline"));
        }

    }

    private void renderDeadlineViolation() {
        final String divId = "deadline-violation";

        PieChart<Number> pieChart = new PieChart<Number>(
                _("Task deadline violations"));
        pieChart.addValue(_("On schedule"),
                dashboardModel.getPercentageOfOnScheduleTasks());
        pieChart.addValue(_("Violated deadline"),
                dashboardModel.getPercentageOfTasksWithViolatedDeadline());
        pieChart.addValue(_("No deadline"),
                dashboardModel.getPercentageOfTasksWithNoDeadline());

        // FIXME: Replace by more suitable colors
        pieChart.addIntervalColors("red", "blue", "green");

        renderPieChart(pieChart, divId);
    }

    /**
     *
     * Use this method to render a {@link PieChart}
     *
     * FIXME: jqplot4java doesn't include a method for changing the colors or a
     * {@link PieChart}. The only way to do it is to add the colors to an
     * Interval, generate the output Javascript code and replace the string
     * 'intervalColors' by 'seriesColors'
     *
     * @param chart
     * @param divId
     */
    private void renderPieChart(Chart<?> chart, String divId) {
        String jsCode = JqPlotUtils.createJquery(chart, divId);
        jsCode = jsCode.replace("intervalColors", "seriesColors");
        Clients.evalJavaScript(jsCode);
    }

    private void renderChart(Chart<?> chart, String divId) {
        String jsCode = JqPlotUtils.createJquery(chart, divId);
        Clients.evalJavaScript(jsCode);
    }

    private void renderTaskCompletationLag() {
        final String divId = "task-completation-lag";

        BarChart<Integer> barChart;
        barChart = new BarChart<Integer>("Task Completation Lead/Lag");

        barChart.setFillZero(true);
        barChart.setHighlightMouseDown(true);
        barChart.setStackSeries(false);
        barChart.setBarMargin(30);

        barChart.addSeries(new Serie("Tasks"));

        TaskCompletationData taskCompletationData = TaskCompletationData
                .create(dashboardModel);
        barChart.setTicks(taskCompletationData.getTicks());
        barChart.addValues(taskCompletationData.getValues());

        barChart.getAxes().getXaxis()
                .setLabel(_("Number of Days / Days Interval"));

        renderChart(barChart, divId);
    }

    private void renderEstimationAccuracy() {
        final String divId = "estimation-accuracy";

        BarChart<Integer> barChart;
        barChart = new BarChart<Integer>("Estimation Accuracy");

        barChart.setFillZero(true);
        barChart.setHighlightMouseDown(true);
        barChart.setStackSeries(false);
        barChart.setBarMargin(30);

        barChart.addSeries(new Serie("Tasks"));

        EstimationAccuracy estimationAccuracyData = EstimationAccuracy
                .create(dashboardModel);
        barChart.setTicks(estimationAccuracyData.getTicks());
        barChart.addValues(estimationAccuracyData.getValues());

        barChart.getAxes().getXaxis()
                .setLabel(_("Number of Tasks / % Deviation"));

        renderChart(barChart, divId);
    }

    private String statusLegend(TaskStatusEnum status,
            Map<TaskStatusEnum, Integer> taskStatus) {
        return status + String.format(_(" (%d tasks)"), taskStatus.get(status));
    }

    private void renderTaskStatus() {
        final String divId = "task-status";

        Map<TaskStatusEnum, Integer> taskStatus = dashboardModel
                .calculateTaskStatus();
        PieChart<Number> taskStatusPieChart = new PieChart<Number>(
                _("Task Status"));

        taskStatusPieChart.addValue(
                statusLegend(TaskStatusEnum.FINISHED, taskStatus),
                dashboardModel.getPercentageOfFinishedTasks());
        taskStatusPieChart.addValue(
                statusLegend(TaskStatusEnum.IN_PROGRESS, taskStatus),
                dashboardModel.getPercentageOfInProgressTasks());
        taskStatusPieChart.addValue(
                statusLegend(TaskStatusEnum.READY_TO_START, taskStatus),
                dashboardModel.getPercentageOfReadyToStartTasks());
        taskStatusPieChart.addValue(
                statusLegend(TaskStatusEnum.BLOCKED, taskStatus),
                dashboardModel.getPercentageOfBlockedTasks());

        // FIXME: Replace by more suitable colors
        taskStatusPieChart.addIntervalColors("red", "blue", "green", "yellow");

        renderPieChart(taskStatusPieChart, divId);
    }

    private void renderGlobalProgress() {
        GlobalProgressChart globalProgressChart = GlobalProgressChart.create();

        // Current values
        globalProgressChart.current(GlobalProgressChart.CRITICAL_PATH_DURATION,
                dashboardModel.getCriticalPathProgressByDuration());
        globalProgressChart.current(GlobalProgressChart.CRITICAL_PATH_HOURS,
                dashboardModel.getCriticalPathProgressByNumHours());
        globalProgressChart.current(GlobalProgressChart.ALL_TASKS_HOURS,
                dashboardModel.getAdvancePercentageByHours());
        // Expected values
        globalProgressChart.expected(
                GlobalProgressChart.CRITICAL_PATH_DURATION,
                dashboardModel.getExpectedCriticalPathProgressByDuration());
        globalProgressChart.expected(GlobalProgressChart.CRITICAL_PATH_HOURS,
                dashboardModel.getExpectedCriticalPathProgressByNumHours());
        globalProgressChart.expected(GlobalProgressChart.ALL_TASKS_HOURS,
                dashboardModel.getExpectedAdvancePercentageByHours());

        globalProgressChart.render();
    }

    private void showCharts() {
        projectDashboardChartsDiv.setVisible(true);
        projectDashboardNoTasksWarningDiv.setVisible(false);
    }

    private void hideCharts() {
        projectDashboardChartsDiv.setVisible(false);
        projectDashboardNoTasksWarningDiv.setVisible(true);
    }

    /**
     *
     * @author Diego Pino García<dpino@igalia.com>
     *
     */
    static class TaskCompletationData {

        private final IDashboardModel dashboardModel;

        private Map<Interval, Integer> taskCompletationData;

        private TaskCompletationData(IDashboardModel dashboardModel) {
            this.dashboardModel = dashboardModel;
        }

        public static TaskCompletationData create(IDashboardModel dashboardModel) {
            return new TaskCompletationData(dashboardModel);
        }

        private Map<Interval, Integer> getData() {
            if (taskCompletationData == null) {
                taskCompletationData = dashboardModel
                        .calculateTaskCompletion();
            }
            return taskCompletationData;
        }

        public String[] getTicks() {
            Set<Interval> intervals = getData().keySet();
            String[] result = new String[intervals.size()];
            int i = 0;
            for (Interval each : intervals) {
                result[i++] = each.toString();

            }
            return result;
        }

        public Collection<Integer> getValues() {
            return getData().values();
        }

    }

    /**
     *
     * @author Diego Pino García<dpino@igalia.com>
     *
     */
    static class EstimationAccuracy {

        private final IDashboardModel dashboardModel;

        private Map<Interval, Integer> estimationAccuracyData;

        private EstimationAccuracy(IDashboardModel dashboardModel) {
            this.dashboardModel = dashboardModel;
        }

        public static EstimationAccuracy create(IDashboardModel dashboardModel) {
            return new EstimationAccuracy(dashboardModel);
        }

        private Map<Interval, Integer> getData() {
            if (estimationAccuracyData == null) {
                estimationAccuracyData = dashboardModel
                        .calculateEstimationAccuracy();
            }
            return estimationAccuracyData;
        }

        public String[] getTicks() {
            Set<Interval> intervals = getData().keySet();
            String[] result = new String[intervals.size()];
            int i = 0;
            for (Interval each : intervals) {
                result[i++] = each.toString();

            }
            return result;
        }

        public Collection<Integer> getValues() {
            return getData().values();
        }

    }

}
