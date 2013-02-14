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

import static org.libreplan.web.I18nHelper._;

import java.text.ParseException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.entities.JobSchedulerConfiguration;
import org.libreplan.importers.SchedulerInfo;
import org.quartz.CronExpression;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.api.Textbox;

/**
 * Controller for job scheduler manager
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class JobSchedulerController extends GenericForwardComposer {

    private static final Log LOG = LogFactory
            .getLog(JobSchedulerController.class);

    private Grid jobSchedulerGrid;

    private Popup cronExpressionInputPopup;

    private Label jobGroup;

    private Label jobName;

    private Textbox cronExpressionSeconds;
    private Textbox cronExpressionMinutes;
    private Textbox cronExpressionHours;
    private Textbox cronExpressionDayOfMonth;
    private Textbox cronExpressionMonth;
    private Textbox cronExpressionDayOfWeek;
    private Textbox cronExpressionYear;

    private IJobSchedulerModel jobSchedulerModel;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("jobSchedulerController", this);
    }

    public List<SchedulerInfo> getSchedulerInfos() {
        return jobSchedulerModel.getSchedulerInfos();
    }

    public RowRenderer getJobSchedulingRenderer() {
        return new RowRenderer() {

            @Override
            public void render(Row row, Object data) {
                SchedulerInfo schedulerInfo = (SchedulerInfo) data;
                row.setValue(data);

                Util.appendLabel(row, schedulerInfo
                        .getJobSchedulerConfiguration().getJobGroup());
                Util.appendLabel(row, schedulerInfo
                        .getJobSchedulerConfiguration().getJobName());
                appendCronExpressionAndButton(row, schedulerInfo);
                Util.appendLabel(row, schedulerInfo.getNextFireTime());
                appendManualStart(row, schedulerInfo);
            }
        };
    }

    private void appendCronExpressionAndButton(final Row row,
            final SchedulerInfo schedulerInfo) {
        final Hbox hBox = new Hbox();
        hBox.setWidth("170px");

        Label label = new Label(schedulerInfo.getJobSchedulerConfiguration()
                .getCronExpression());
        label.setHflex("1");
        hBox.appendChild(label);

        Button button = Util.createEditButton(new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                setupCronExpressionPopup(schedulerInfo);
                cronExpressionInputPopup.open(hBox);
            }
        });
        hBox.appendChild(button);

        row.appendChild(hBox);
    }


    private void setupCronExpressionPopup(final SchedulerInfo schedulerInfo) {
        JobSchedulerConfiguration jobSchedulerConfiguration = schedulerInfo.getJobSchedulerConfiguration();
        jobGroup.setValue(jobSchedulerConfiguration.getJobGroup());
        jobName.setValue(jobSchedulerConfiguration.getJobName());

        String cronExpression = jobSchedulerConfiguration.getCronExpression();
        String[] cronExpressionArray = StringUtils.split(cronExpression);

        cronExpressionSeconds.setValue(cronExpressionArray[0]);
        cronExpressionMinutes.setValue(cronExpressionArray[1]);
        cronExpressionHours.setValue(cronExpressionArray[2]);
        cronExpressionDayOfMonth.setValue(cronExpressionArray[3]);
        cronExpressionMonth.setValue(cronExpressionArray[4]);
        cronExpressionDayOfWeek.setValue(cronExpressionArray[5]);

        if (cronExpressionArray.length == 7) {
            cronExpressionYear.setValue(cronExpressionArray[6]);
        }
    }

    private void appendManualStart(final Row row,
            final SchedulerInfo schedulerInfo) {
        final Button rescheduleButton = new Button(_("Manual"));
        rescheduleButton.addEventListener(Events.ON_CLICK, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                jobSchedulerModel.doManual(schedulerInfo);
            }
        });
        row.appendChild(rescheduleButton);
    }

    public void reschedule() {
        String cronExpression = getCronExpressionString();
        try {
            // Check cron expression format
            new CronExpression(cronExpression);
        } catch (ParseException e) {
            LOG.info("Unable to parse cron expression", e);
            throw new WrongValueException(cronExpressionInputPopup,
                    _("Unable to parse cron expression") + ":\n"
                            + e.getMessage());
        }

        jobSchedulerModel.saveJobConfigurationAndReschedule(
                jobGroup.getValue(), jobName.getValue(), cronExpression);
        cronExpressionInputPopup.close();
        Util.reloadBindings(jobSchedulerGrid);
    }

    private String getCronExpressionString() {
        String cronExpression = "";
        cronExpression += StringUtils.trimToEmpty(cronExpressionSeconds.getValue()) + " ";
        cronExpression += StringUtils.trimToEmpty(cronExpressionMinutes.getValue()) + " ";
        cronExpression += StringUtils.trimToEmpty(cronExpressionHours.getValue()) + " ";
        cronExpression += StringUtils.trimToEmpty(cronExpressionDayOfMonth.getValue()) + " ";
        cronExpression += StringUtils.trimToEmpty(cronExpressionMonth.getValue()) + " ";
        cronExpression += StringUtils.trimToEmpty(cronExpressionDayOfWeek.getValue());

        String year = StringUtils.trimToEmpty(cronExpressionYear.getValue());
        if (!year.isEmpty()) {
            cronExpression += " " + year;
        }

        return cronExpression;
    }

    public void cancel() {
        cronExpressionInputPopup.close();
    }

}
