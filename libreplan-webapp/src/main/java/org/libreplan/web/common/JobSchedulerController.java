/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

package org.libreplan.web.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IJobSchedulerConfigurationDAO;
import org.libreplan.business.common.entities.JobSchedulerConfiguration;
import org.libreplan.importers.ISchedulerManager;
import org.libreplan.importers.SchedulerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Rows;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Toolbarbutton;

/**
 * Controller for job scheduler manager
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class JobSchedulerController extends GenericForwardComposer {
    private static final Log LOG = LogFactory
            .getLog(JobSchedulerController.class);

    private Grid jobSchedulerGrid, cronExpressionGrid;

    private Popup cronExpressionInputPopup;

    private Label jobGroupLabel, jobNameLable;

    @Autowired
    ISchedulerManager schedulerManager;

    @Autowired
    IJobSchedulerConfigurationDAO jobSchedulerConfigurationDAO;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("jobSchedulerController", this, true);
        reloadSchedulerJobs();
    }

    public List<SchedulerInfo> getSchedulerInfo() {
        List<SchedulerInfo> schedulerInfoList = schedulerManager
                .getSchedulerInfos();
        Collections.sort(schedulerInfoList, new Comparator<SchedulerInfo>() {

            @Override
            public int compare(SchedulerInfo o1, SchedulerInfo o2) {
                int result = o1
                        .getJobSchedulerConfiguration()
                        .getJobGroup()
                        .compareTo(
                                o2.getJobSchedulerConfiguration().getJobGroup());
                if (result == 0) {
                    result = o1
                            .getJobSchedulerConfiguration()
                            .getJobName()
                            .compareTo(
                                    o2.getJobSchedulerConfiguration()
                                            .getJobName());
                }
                return result;
            }
        });
        return schedulerInfoList;
    }

    private void reloadSchedulerJobs() {
        jobSchedulerGrid.setModel(new SimpleListModel(getSchedulerInfo()));
        jobSchedulerGrid.invalidate();
    }

    public JobSchedulingRenderer getJobSchedulingRenderer() {
        return new JobSchedulingRenderer();
    }

    public class JobSchedulingRenderer implements RowRenderer {
        @Override
        public void render(Row row, Object data) {

            SchedulerInfo schedulerInfo = (SchedulerInfo) data;
            Util.appendLabel(row, schedulerInfo.getJobSchedulerConfiguration()
                    .getJobGroup());
            Util.appendLabel(row, schedulerInfo.getJobSchedulerConfiguration()
                    .getJobName());
            appendCronExpressionAndButton(row, schedulerInfo);
            Util.appendLabel(row, schedulerInfo.getNextFireTime());
            appendManualStart(row, schedulerInfo);
        }
    }

    private void appendCronExpressionAndButton(final Row row,
            final SchedulerInfo schedulerInfo) {
        final Hbox hBox = new Hbox();
        hBox.setWidth("100%");
        Cell cell = new Cell();
        cell.setHflex("2");
        cell.setAlign("left");

        Label label = new Label(schedulerInfo.getJobSchedulerConfiguration()
                .getCronExpression());
        cell.appendChild(label);

        Cell cell2 = new Cell();
        cell2.setHflex("1");
        cell2.setWidth("10px");
        final Toolbarbutton button = new Toolbarbutton();
        button.setImage("/common/img/ico_editar.png");
        cell2.appendChild(button);
        hBox.appendChild(cell);
        hBox.appendChild(cell2);

        button.addEventListener(Events.ON_CLICK, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                setupCronExpressionPopup(schedulerInfo);
                cronExpressionInputPopup.open(jobSchedulerGrid, "at_pointer");
            }
        });
        row.appendChild(hBox);
    }


    private void setupCronExpressionPopup(final SchedulerInfo schedulerInfo) {
        List<CronExpression> list = new ArrayList<CronExpression>();
        list.add(getCronExpression(schedulerInfo.getJobSchedulerConfiguration()
                .getCronExpression()));
        cronExpressionGrid.setModel(new SimpleListModel(list));

        jobGroupLabel = (Label) cronExpressionInputPopup
                .getFellowIfAny("jobGroup");
        jobNameLable = (Label) cronExpressionInputPopup
                .getFellowIfAny("jobName");
        jobGroupLabel.setValue(schedulerInfo.getJobSchedulerConfiguration()
                .getJobGroup());
        jobNameLable.setValue(schedulerInfo.getJobSchedulerConfiguration()
                .getJobName());
    }

    @Autowired
    private IAdHocTransactionService adHocTransactionService;

    private void saveJobConfigurationAndReschedule(
            final String jobGroup, final String jobName, final String cronExp) {
        adHocTransactionService
                .runOnAnotherTransaction(new IOnTransaction<Void>() {
                    @Override
                    public Void execute() {
                        JobSchedulerConfiguration jobSchedulerConfiguration = jobSchedulerConfigurationDAO
                                .findByJobGroupAndJobName(jobGroup, jobName);
                        jobSchedulerConfiguration.setCronExpression(cronExp);
                        jobSchedulerConfigurationDAO
                                .save(jobSchedulerConfiguration);
                        schedulerManager.rescheduleJob(jobSchedulerConfiguration);
                        reloadSchedulerJobs();
                        return null;
                    }
                });
    }

    private CronExpression getCronExpression(String cronExpressionStr) {
        CronExpression cronExpression = new CronExpression();
        StringTokenizer st = new StringTokenizer(cronExpressionStr);
        int countTokens = st.countTokens();
        if (countTokens < 6) {
            throw new IllegalArgumentException("Cron expression is not valid");
        }
        cronExpression.setSeconds(getNextToken(st.nextToken()));
        cronExpression.setMinutes(getNextToken(st.nextToken()));
        cronExpression.setHours(getNextToken(st.nextToken()));
        cronExpression.setDayOfMonth(getNextToken(st.nextToken()));
        cronExpression.setMonth(getNextToken(st.nextToken()));
        cronExpression.setDayOfWeek(getNextToken(st.nextToken()));
        if (countTokens > 6) { // optional
            cronExpression.setYear(getNextToken(st.nextToken()));
        } else {
            cronExpression.setYear("");
        }

        return cronExpression;
    }

    private String getNextToken(String token) {
        return token.isEmpty() ? "" : token.trim();
    }

    private void appendManualStart(final Row row,
            final SchedulerInfo schedulerInfo) {
        final Button rescheduleButton = new Button("Manual");
        rescheduleButton.addEventListener(Events.ON_CLICK, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                schedulerManager.doManual(schedulerInfo
                        .getJobSchedulerConfiguration().getJobName());
            }
        });
        row.appendChild(rescheduleButton);
    }

    public void reschedule() {
        jobGroupLabel = (Label) cronExpressionInputPopup
                .getFellowIfAny("jobGroup");
        jobNameLable = (Label) cronExpressionInputPopup
                .getFellowIfAny("jobName");

        Rows rows = cronExpressionGrid.getRows();
        Row row = (Row) rows.getChildren().get(0);
        CronExpression cronExp = (CronExpression) row.getValue();
        saveJobConfigurationAndReschedule(jobGroupLabel.getValue(),
                jobNameLable.getValue(), convertToCronExpressionStr(cronExp));
        cronExpressionInputPopup.close();
        getJobSchedulingRenderer();
    }

    private String convertToCronExpressionStr(CronExpression cronExp) {
        return String.format("%1$s %2$s %3$s %4$s %5$s %6$s %7$s",
                cronExp.getSeconds(), cronExp.getMinutes(), cronExp.getHours(),
                cronExp.getDayOfMonth(), cronExp.getMonth(),
                cronExp.getDayOfWeek(), cronExp.getYear()).trim();
    }

    public void cancel() {
        cronExpressionInputPopup.invalidate();
        cronExpressionInputPopup.close();
    }

    /**
     * Class representing cron expression
     */
    public class CronExpression {
        private String seconds;
        private String minutes;
        private String hours;
        private String dayOfMonth;
        private String month;
        private String dayOfWeek;
        private String year;

        public String getSeconds() {
            return seconds;
        }

        public void setSeconds(String seconds) {
            Validate.notEmpty(seconds, "Seconds is mandatory");
            this.seconds = seconds;
        }

        public String getMinutes() {
            return minutes;
        }

        public void setMinutes(String minutes) {
            Validate.notEmpty(minutes, "Minutes is mandatory");
            this.minutes = minutes;
        }

        public String getHours() {
            return hours;
        }

        public void setHours(String hours) {
            Validate.notEmpty(hours, "Hours is mandatory");
            this.hours = hours;
        }

        public String getDayOfMonth() {
            return dayOfMonth;
        }

        public void setDayOfMonth(String dayOfMonth) {
            Validate.notEmpty(dayOfMonth, "day of month is mandatory");
            this.dayOfMonth = dayOfMonth;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            Validate.notEmpty(month, "month is mandatory");
            this.month = month;
        }

        public String getDayOfWeek() {
            return dayOfWeek;
        }

        public void setDayOfWeek(String dayOfWeek) {
            Validate.notEmpty(dayOfWeek, "day of week is mandatory");
            this.dayOfWeek = dayOfWeek;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

    }

}
