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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.TaskStatusEnum;
import org.libreplan.web.common.Util;
import org.libreplan.web.dashboard.DashboardModel.Interval;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

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

    private Window dashboardWindow;

    private Grid gridTasksSummary;

    private Div projectDashboardChartsDiv;
    private Div projectDashboardNoTasksWarningDiv;

    public DashboardController() {
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.dashboardWindow = (Window) comp;
        self.setAttribute("controller", this);
        Util.createBindingsFor(this.dashboardWindow);
    }

    public void setCurrentOrder(Order order) {
        dashboardModel.setCurrentOrder(order);
        if (dashboardModel.tasksAvailable()) {
            showCharts();
        } else {
            hideCharts();
        }
        if (this.dashboardWindow != null) {
            renderGlobalProgress();
            renderTaskStatus();
            renderTaskCompletationLag();
            renderTasksSummary();
        }
    }

    private void renderTaskCompletationLag() {
        Map<Interval, Integer> taskCompletationData = dashboardModel
                .calculateTaskCompletation();
        TaskCompletationLag taskCompletation = TaskCompletationLag.create();
        for (Interval each : taskCompletationData.keySet()) {
            Integer value = taskCompletationData.get(each);
            taskCompletation.data(each.toString(), value);
        }
        taskCompletation.render();
    }

    private void renderTasksSummary() {
        Map<TaskStatusEnum, Integer> taskStatus = dashboardModel
                .calculateTaskStatus();

        taskStatus("lblTasksFinished", taskStatus.get(TaskStatusEnum.FINISHED));
        taskStatus("lblTasksBlocked", taskStatus.get(TaskStatusEnum.BLOCKED));
        taskStatus("lblTasksInProgress",
                taskStatus.get(TaskStatusEnum.IN_PROGRESS));
        taskStatus("lblTasksReadyToStart",
                taskStatus.get(TaskStatusEnum.READY_TO_START));
    }

    private void taskStatus(String key, Integer value) {
        Label label = (Label) gridTasksSummary.getFellowIfAny(key);
        if (label != null) {
            label.setValue(String.format(_("%d tasks"), value));
        }
    }

    private void renderTaskStatus() {
        TaskStatus taskStatus = TaskStatus.create();
        taskStatus.data(_("Finished"),
                dashboardModel.getPercentageOfFinishedTasks());
        taskStatus.data(_("In progress"),
                dashboardModel.getPercentageOfInProgressTasks());
        taskStatus.data(_("Ready to start"),
                dashboardModel.getPercentageOfReadyToStartTasks());
        taskStatus.data(_("Blocked"),
                dashboardModel.getPercentageOfBlockedTasks());
        taskStatus.render();
    }

    private void renderGlobalProgress() {
        GlobalProgress globalProgress = GlobalProgress.create();

        // Current values
        globalProgress.current(GlobalProgress.CRITICAL_PATH_DURATION,
                dashboardModel.getCriticalPathProgressByDuration());
        globalProgress.current(GlobalProgress.CRITICAL_PATH_HOURS,
                dashboardModel.getCriticalPathProgressByNumHours());
        globalProgress.current(GlobalProgress.ALL_TASKS_HOURS,
                dashboardModel.getAdvancePercentageByHours());
        // Expected values
        globalProgress.expected(GlobalProgress.CRITICAL_PATH_DURATION,
                dashboardModel.getExpectedCriticalPathProgressByDuration());
        globalProgress.expected(GlobalProgress.CRITICAL_PATH_HOURS,
                dashboardModel.getExpectedCriticalPathProgressByNumHours());
        globalProgress.expected(GlobalProgress.ALL_TASKS_HOURS,
                dashboardModel.getExpectedAdvancePercentageByHours());

        globalProgress.render();
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
     * @author Diego Pino García <dpino@igalia.com>
     * 
     */
    static class GlobalProgress {

        public static final String ALL_TASKS_HOURS = _("All tasks (hours)");

        public static final String CRITICAL_PATH_HOURS = _("Critical path (hours)");

        public static final String CRITICAL_PATH_DURATION = _("Critical path (duration)");

        private final Map<String, BigDecimal> current = new LinkedHashMap<String, BigDecimal>();

        private final Map<String, BigDecimal> expected = new LinkedHashMap<String, BigDecimal>();

        private static List<Series> series = new ArrayList<Series>() {
            {
                add(Series.create(_("Current"), "#33c"));
                add(Series.create(_("Expected"), "#c33"));
            }
        };

        private GlobalProgress() {

        }

        public void current(String key, BigDecimal value) {
            current.put(key, value);
        }

        public void expected(String key, BigDecimal value) {
            expected.put(key, value);
        }

        public static GlobalProgress create() {
            return new GlobalProgress();
        }

        public String getPercentages() {
            return String.format("'[%s, %s]'",
                    jsonifyPercentages(current.values()),
                    jsonifyPercentages(expected.values()));
        }

        private String jsonifyPercentages(Collection<BigDecimal> array) {
            List<String> result = new ArrayList<String>();

            int i = 1;
            for (BigDecimal each : array) {
                result.add(String.format("[%.2f, %d]", each.doubleValue(), i++));
            }
            return String.format("[%s]", StringUtils.join(result, ","));
        }

        private String jsonify(Collection<?> list) {
            Collection<String> result = new ArrayList<String>();
            for (Object each : list) {
                if (each.getClass() == String.class) {
                    result.add(String.format("\"%s\"", each.toString()));
                } else {
                    result.add(String.format("%s", each.toString()));
                }
            }
            return String.format("'[%s]'", StringUtils.join(result, ','));
        }

        public String getSeries() {
            return jsonify(series);
        }

        /**
         * The order of the ticks is taken from the keys in current
         * 
         * @return
         */
        public String getTicks() {
            return jsonify(current.keySet());
        }

        public void render() {
            String command = String.format(
                    "global_progress.render(%s, %s, %s);", getPercentages(),
                    getTicks(), getSeries());
            Clients.evalJavaScript(command);
        }

    }

    /**
     * 
     * @author Diego Pino García <dpino@igalia.com>
     * 
     */
    static class Series {

        private String label;

        private String color;

        private Series() {

        }

        public static Series create(String label) {
            Series series = new Series();
            series.label = label;
            return series;
        }

        public static Series create(String label, String color) {
            Series series = new Series();
            series.label = label;
            series.color = color;
            return series;
        }

        @Override
        public String toString() {
            return String.format("{\"label\": \"%s\", \"color\": \"%s\"}",
                    label, color);
        }

    }

    /**
     * 
     * @author Diego Pino García <dpino@igalia.com>
     * 
     */
    static class TaskStatus {

        private final Map<String, BigDecimal> data = new LinkedHashMap<String, BigDecimal>();

        private TaskStatus() {

        }

        public static TaskStatus create() {
            return new TaskStatus();
        }

        private String getData() {
            List<String> result = new ArrayList<String>();

            TreeSet<String> keys = new TreeSet<String>(data.keySet());
            for (String key : keys) {
                BigDecimal value = data.get(key);
                result.add(String.format("[\"%s\", %.2f]", key, value));
            }
            return String.format("'[%s]'", StringUtils.join(result, ","));
        }

        public void data(String key, BigDecimal value) {
            data.put(key, value);
        }

        public void render() {
            String command = String
                    .format("task_status.render(%s);", getData());
            Clients.evalJavaScript(command);
        }

    }

    static class TaskCompletationLag {

        private final String id = "task_completation_lag";

        private final Map<String, Integer> data = new LinkedHashMap<String, Integer>();

        private TaskCompletationLag() {

        }

        public static TaskCompletationLag create() {
            return new TaskCompletationLag();
        }

        public void data(String interval, Integer value) {
            data.put(interval, value);
        }

        public void render() {
            String _data = JSONHelper.values(data);
            String ticks = JSONHelper.keys(data);
            String command = String.format("%s.render(%s, %s);", id, _data,
                    ticks);
            Clients.evalJavaScript(command);
        }

    }

    static class JSONHelper {

        public static String format(Map<String, Integer> data) {
            List<String> result = new ArrayList<String>();
            for (String key : data.keySet()) {
                Integer value = data.get(data);
                result.add(String.format("[\"%s\", %d]", key, value));
            }
            return String.format("'[%s]'", StringUtils.join(result, ','));
        }

        public static String keys(Map<String, ?> map) {
            List<String> result = new ArrayList<String>();
            for (String each : map.keySet()) {
                result.add(String.format("\"%s\"", each));
            }
            return String.format("'[%s]'", StringUtils.join(result, ','));
        }

        public static String values(Map<?, Integer> map) {
            List<String> result = new ArrayList<String>();
            for (Integer each : map.values()) {
                result.add(each.toString());
            }
            return String.format("'[%s]'", StringUtils.join(result, ','));
        }

    }

}