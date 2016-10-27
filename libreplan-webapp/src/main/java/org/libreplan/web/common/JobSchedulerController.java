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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.entities.Connector;
import org.libreplan.business.common.entities.ConnectorException;
import org.libreplan.business.common.entities.JobClassNameEnum;
import org.libreplan.business.common.entities.JobSchedulerConfiguration;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.importers.SynchronizationInfo;
import org.quartz.CronExpression;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Popup;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Controller for job scheduler manager.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class JobSchedulerController extends BaseCRUDController<JobSchedulerConfiguration> {

    private static final Log LOG = LogFactory.getLog(JobSchedulerController.class);

    private Popup cronExpressionInputPopup;

    private Label jobGroup;

    private Label jobName;

    private Textbox cronExpressionTextBox;

    private Textbox cronExpressionSeconds;

    private Textbox cronExpressionMinutes;

    private Textbox cronExpressionHours;

    private Textbox cronExpressionDayOfMonth;

    private Textbox cronExpressionMonth;

    private Textbox cronExpressionDayOfWeek;

    private Textbox cronExpressionYear;

    private IJobSchedulerModel jobSchedulerModel;

    public JobSchedulerController() {
        jobSchedulerModel = (IJobSchedulerModel) SpringUtil.getBean("jobSchedulerModel");
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        Grid listJobSchedulings = (Grid) listWindow.getFellowIfAny("listJobSchedulings");
        listJobSchedulings.getModel();
        initCronExpressionPopup();
    }

    /**
     * Initializes cron expressions for popup.
     */
    private void initCronExpressionPopup() {
        cronExpressionTextBox = (Textbox) editWindow.getFellow("cronExpressionTextBox");

        cronExpressionInputPopup = (Popup) editWindow.getFellow("cronExpressionInputPopup");

        jobGroup = (Label) cronExpressionInputPopup.getFellow("jobGroup");

        jobName = (Label) cronExpressionInputPopup.getFellow("jobName");

        Grid cronExpressionGrid = (Grid) cronExpressionInputPopup.getFellow("cronExpressionGrid");

        cronExpressionSeconds = (Textbox) cronExpressionGrid.getFellow("cronExpressionSeconds");

        cronExpressionMinutes = (Textbox) cronExpressionGrid.getFellow("cronExpressionMinutes");

        cronExpressionHours = (Textbox) cronExpressionGrid.getFellow("cronExpressionHours");

        cronExpressionDayOfMonth = (Textbox) cronExpressionGrid.getFellow("cronExpressionDayOfMonth");

        cronExpressionMonth = (Textbox) cronExpressionGrid.getFellow("cronExpressionMonth");

        cronExpressionDayOfWeek = (Textbox) cronExpressionGrid.getFellow("cronExpressionDayOfWeek");

        cronExpressionYear = (Textbox) cronExpressionGrid.getFellow("cronExpressionYear");
    }

    /**
     * Returns a list of {@link JobSchedulerConfiguration}.
     */
    public List<JobSchedulerConfiguration> getJobSchedulerConfigurations() {
        return jobSchedulerModel.getJobSchedulerConfigurations();
    }

    /**
     * Returns {@link JobSchedulerConfiguration}.
     */
    public JobSchedulerConfiguration getJobSchedulerConfiguration() {
        return jobSchedulerModel.getJobSchedulerConfiguration();
    }

    /**
     * Returns all predefined jobs.
     */
    public JobClassNameEnum[] getJobNames() {
        return JobClassNameEnum.values();
    }

    /**
     * Returns list of connectorNames.
     */
    public List<String> getConnectorNames() {
        List<Connector> connectors = jobSchedulerModel.getConnectors();
        List<String> connectorNames = new ArrayList<>();

        for (Connector connector : connectors) {
            connectorNames.add(connector.getName());
        }

        return connectorNames;
    }

    /**
     * Renders job scheduling and returns {@link RowRenderer}.
     */
    public RowRenderer getJobSchedulingRenderer() {
        return (row, o, i) -> {
            final JobSchedulerConfiguration jobSchedulerConfiguration = (JobSchedulerConfiguration) o;
            row.setValue(o);

            Util.appendLabel(row, jobSchedulerConfiguration.getJobGroup());
            Util.appendLabel(row, jobSchedulerConfiguration.getJobName());
            Util.appendLabel(row, jobSchedulerConfiguration.getCronExpression());
            Util.appendLabel(row, getNextFireTime(jobSchedulerConfiguration));
            Hbox hbox = new Hbox();

            hbox.appendChild(createManualButton(event -> {
                try {
                    jobSchedulerModel.doManual(jobSchedulerConfiguration);
                    showSynchronizationInfo();
                } catch (ConnectorException e) {
                    messagesForUser.showMessage(Level.ERROR, e.getMessage());
                }
            }));

            hbox.appendChild(Util.createEditButton(event -> goToEditForm(jobSchedulerConfiguration)));

            hbox.appendChild(Util.createRemoveButton(event -> confirmDelete(jobSchedulerConfiguration)));

            row.appendChild(hbox);
        };
    }

    private RowRenderer getSynchronizationInfoRenderer() {
        return (row, o, i) -> {
            final SynchronizationInfo synchronizationInfo = (SynchronizationInfo) o;
            row.setValue(o);

            Groupbox groupbox = new Groupbox();
            groupbox.setClosable(true);
            Caption caption = new Caption();
            caption.setLabel(synchronizationInfo.getAction());
            groupbox.appendChild(caption);
            row.appendChild(groupbox);

            if ( synchronizationInfo.isSuccessful() ) {
                groupbox.appendChild(new Label(_("Completed")));
            } else {

                Listbox listbox = new Listbox();

                listbox.setModel(new SimpleListModel<>(synchronizationInfo.getFailedReasons()));
                groupbox.appendChild(listbox);
            }
        };
    }


    private List<SynchronizationInfo> getSynchronizationInfos() {
        return jobSchedulerModel.getSynchronizationInfos();
    }


    private void showSynchronizationInfo() {
        Map<String, Object> args = new HashMap<>();

        Window win = (Window) Executions.createComponents("/orders/_synchronizationInfo.zul", null, args);

        Window syncInfoWin = (Window) win.getFellowIfAny("syncInfoWin");

        Grid syncInfoGrid = (Grid) syncInfoWin.getFellowIfAny("syncInfoGrid");

        syncInfoGrid.setModel(new SimpleListModel<>(getSynchronizationInfos()));

        syncInfoGrid.setRowRenderer(getSynchronizationInfoRenderer());

        try {
            win.doModal();
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the next fire time for the specified job in {@link JobSchedulerConfiguration}.
     *
     * @param jobSchedulerConfiguration
     *            the job scheduler configuration
     */
    private String getNextFireTime(JobSchedulerConfiguration jobSchedulerConfiguration) {
        return jobSchedulerModel.getNextFireTime(jobSchedulerConfiguration);
    }

    /**
     * Creates and returns a button.
     *
     * @param eventListener
     *            Event listener for this button
     */
    private static Button createManualButton(EventListener eventListener) {
        Button button = new Button(_("Manual"));
        button.setTooltiptext(_("Manual"));
        button.setSclass("add-button");
        button.addEventListener(Events.ON_CLICK, eventListener);

        return button;
    }

    /**
     * Opens the <code>cronExpressionInputPopup</code>.
     */
    public void openPopup() {
        setupCronExpressionPopup(getJobSchedulerConfiguration());
        cronExpressionInputPopup.open(cronExpressionTextBox, "after_start");
    }

    /**
     * Sets the cronExpression values for <code>cronExpressionInputPopup</code>.
     *
     * @param jobSchedulerConfiguration
     *            where to read the values
     */
    private void setupCronExpressionPopup(final JobSchedulerConfiguration jobSchedulerConfiguration) {
        if ( jobSchedulerConfiguration != null ) {
            jobGroup.setValue(jobSchedulerConfiguration.getJobGroup());
            jobName.setValue(jobSchedulerConfiguration.getJobName());

            String cronExpression = jobSchedulerConfiguration.getCronExpression();

            if ( cronExpression == null || cronExpression.isEmpty() ) {
                return;
            }

            String[] cronExpressionArray = StringUtils.split(cronExpression);

            cronExpressionSeconds.setValue(cronExpressionArray[0]);
            cronExpressionMinutes.setValue(cronExpressionArray[1]);
            cronExpressionHours.setValue(cronExpressionArray[2]);
            cronExpressionDayOfMonth.setValue(cronExpressionArray[3]);
            cronExpressionMonth.setValue(cronExpressionArray[4]);
            cronExpressionDayOfWeek.setValue(cronExpressionArray[5]);

            if ( cronExpressionArray.length == 7 ) {
                cronExpressionYear.setValue(cronExpressionArray[6]);
            }
        }
    }

    /**
     * Sets the <code>cronExpressionTextBox</code> value from the <code>cronExpressionInputPopup</code>.
     */
    public void updateCronExpression() {
        String cronExpression = getCronExpressionString();
        try {
            // Check cron expression format
            new CronExpression(cronExpression);
        } catch (ParseException e) {
            LOG.info("Unable to parse cron expression", e);

            throw new WrongValueException(
                    cronExpressionInputPopup,
                    _("Unable to parse cron expression") + ":\n" + e.getMessage());
        }
        cronExpressionTextBox.setValue(cronExpression);
        cronExpressionInputPopup.close();
        Util.saveBindings(cronExpressionTextBox);
    }

    /**
     * Concatenating the cronExpression values.
     *
     * @return cronExpression string
     */
    private String getCronExpressionString() {
        String cronExpression = "";
        cronExpression += StringUtils.trimToEmpty(cronExpressionSeconds.getValue()) + " ";
        cronExpression += StringUtils.trimToEmpty(cronExpressionMinutes.getValue()) + " ";
        cronExpression += StringUtils.trimToEmpty(cronExpressionHours.getValue()) + " ";
        cronExpression += StringUtils.trimToEmpty(cronExpressionDayOfMonth.getValue()) + " ";
        cronExpression += StringUtils.trimToEmpty(cronExpressionMonth.getValue()) + " ";
        cronExpression += StringUtils.trimToEmpty(cronExpressionDayOfWeek.getValue());

        String year = StringUtils.trimToEmpty(cronExpressionYear.getValue());

        return !year.isEmpty() ? cronExpression + " " + year : cronExpression;
    }

    /**
     * Closes the popup.
     */
    public void cancelPopup() {
        cronExpressionInputPopup.close();
    }

    @Override
    protected String getEntityType() {
        return _("Job scheduling");
    }

    @Override
    protected String getPluralEntityType() {
        return _("Job scheduling");
    }

    @Override
    protected void initCreate() {
        jobSchedulerModel.initCreate();
    }

    @Override
    protected void initEdit(JobSchedulerConfiguration entity) {
        jobSchedulerModel.initEdit(entity);
    }

    @Override
    protected void save() {
        jobSchedulerModel.confirmSave();
        if ( jobSchedulerModel.scheduleOrUnscheduleJob() ) {
            messagesForUser.showMessage(Level.INFO, _("Job is scheduled/unscheduled"));
        }
    }

    @Override
    protected void cancel() {
        jobSchedulerModel.cancel();
    }

    @Override
    protected JobSchedulerConfiguration getEntityBeingEdited() {
        return jobSchedulerModel.getJobSchedulerConfiguration();
    }

    @Override
    protected void delete(JobSchedulerConfiguration entity) throws InstanceNotFoundException {
        jobSchedulerModel.remove(entity);
        if ( jobSchedulerModel.deleteScheduledJob(entity) ) {
            messagesForUser.showMessage(Level.INFO, _("Job is deleted from scheduler"));
        }
    }

}
