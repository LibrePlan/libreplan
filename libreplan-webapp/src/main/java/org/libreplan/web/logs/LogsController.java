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
}