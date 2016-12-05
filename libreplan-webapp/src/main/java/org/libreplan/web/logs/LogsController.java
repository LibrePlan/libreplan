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

package org.libreplan.web.logs;
import org.libreplan.business.logs.entities.IssueLog;
import org.libreplan.business.logs.entities.RiskLog;
import org.libreplan.business.logs.entities.IssueTypeEnum;
import org.libreplan.business.logs.entities.LowMediumHighEnum;
import org.libreplan.business.logs.entities.RiskScoreStatesEnum;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.common.Util;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Window;


/**
 * Controller for Logs(issue and risk logs).
 *
 * @author Misha Gozhda <misha@libreplan-enterprise.com>
 */
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LogsController extends GenericForwardComposer {

    private Window issueLogWindow;

    private Window riskLogWindow;

    private Window logWindow;

    private IssueLogCRUDController issueLogController;

    private RiskLogCRUDController riskLogController;

    private static boolean projectNameVisibility = true;

    private static Order order = null;

    private IssueLog issueLogInMemory = null;

    private RiskLog riskLogInMemory = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("logsController", this, true);
        logWindow = (Window) comp.getFellowIfAny("logWindow");
        Util.createBindingsFor(logWindow);
        setupIssueLogController();
        setupRiskLogController();
    }

    public void setupIssueLogController() {
        issueLogWindow = (Window) self.getFellowIfAny("issueLogWindow");

        if ( issueLogController == null ) {
            issueLogController = new IssueLogCRUDController();
        }
        try {
            // Saving risk log, if it was created, but not saved
            saveRiskLogState();

            /*
             * Code below is needed to save issue log if it was created and changed, but not saved.
             * If the issue log was not saved - we set it to issue model and show when user go back to issue log tab.
             */
            if (issueLogController.isIssueLogSaved()) {
                issueLogInMemory = null;
            }
            if (issueLogInMemory != null) {
                issueLogController.setIssueLogToModel(issueLogInMemory);
                issueLogController.doAfterCompose(issueLogWindow);
                issueLogController.goToEditForm(issueLogInMemory);
                issueLogController.setDefaultStatus();
                return;
            }

            /*
             * Normal logic flow: no issue log created, and not saved.
             * This will show to user issue log list.
             */
            issueLogController.doAfterCompose(issueLogWindow);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void setupRiskLogController() {
        riskLogWindow = (Window) self.getFellowIfAny("riskLogWindow");

        if ( riskLogController == null ) {
            riskLogController = new RiskLogCRUDController();
        }
        try {
            // Saving issue log, if it was created, but not saved.
            saveIssueLogState();

            /*
             * Code below is needed to save risk log if it was created and changed, but not saved.
             * If the risk log was not saved - we set it to risk model and show when user go back to risk log tab.
             */
            if (riskLogController.isRiskLogSaved()) {
                riskLogInMemory = null;
            }
            if (riskLogInMemory != null) {
                riskLogController.setRiskLogToModel(riskLogInMemory);
                riskLogController.doAfterCompose(riskLogWindow);
                riskLogController.goToEditForm(riskLogInMemory);
                return;
            }

            /*
             * Normal logic flow: no risk log created, and not saved.
             * This will show to user risk log list.
             */
            riskLogController.doAfterCompose(riskLogWindow);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void goToOrderMode(Order order) {
        LogsController.projectNameVisibility = false;
        LogsController.order = order;
    }

    public static void goToGlobalMode(){
        projectNameVisibility = true;
        order = null;
    }

    public static boolean getProjectNameVisibility() {
        return projectNameVisibility;
    }

    public static Order getOrder() {
        return order;
    }

    private boolean isIssueLogChanged() {
        if (issueLogController.getIssueLog() != null) {
            IssueLog issueLog = issueLogController.getIssueLog();

            // "Date raised" and "Created by" are not handled
            if (!(issueLog.getOrder() == null &&
                    issueLog.getType() == IssueTypeEnum.getDefault() &&
                    "LOW".equals(issueLog.getStatus()) &&
                    issueLog.getDescription() == null &&
                    issueLog.getPriority() == LowMediumHighEnum.getDefault() &&
                    issueLog.getSeverity() == LowMediumHighEnum.getDefault() &&
                    issueLog.getAssignedTo() == null &&
                    issueLog.getDeadline() == null &&
                    issueLog.getDateResolved() == null &&
                    issueLog.getNotes() == null)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRiskLogChanged() {
        if (riskLogController.getRiskLog() != null) {
            RiskLog riskLog = riskLogController.getRiskLog();

            // "Date created" and "Created by" are not handled
            if (!(riskLog.getProjectName() == null &&
                    riskLog.getStatus() == null &&
                    riskLog.getProbability() == LowMediumHighEnum.getDefault() &&
                    riskLog.getImpact() == LowMediumHighEnum.getDefault() &&
                    riskLog.getDescription() == null &&
                    riskLog.getCounterMeasures() == null &&
                    riskLog.getScoreAfterCM() == RiskScoreStatesEnum.ZERO &&
                    riskLog.getContingency() == null &&
                    riskLog.getActionWhen() == null &&
                    riskLog.getResponsible() == null &&
                    riskLog.getNotes() == null)) {
                return true;
            }
        }
        return false;
    }

    private void saveIssueLogState() {
        if (issueLogController!=null && issueLogController.getIssueLog()!=null && isIssueLogChanged()) {
            issueLogInMemory = issueLogController.getIssueLog();
        }
    }

    private void saveRiskLogState() {
        if (riskLogController!=null && riskLogController.getRiskLog()!=null && isRiskLogChanged()) {
            riskLogInMemory = riskLogController.getRiskLog();
        }
    }
}