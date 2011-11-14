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

import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.common.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Window;

/**
 * Controller for dashboardfororder view
 * @author Nacho Barrientos <nacho@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DashboardController extends GenericForwardComposer {

    @Autowired
    private DashboardModel dashboardModel;

    private Window dashboardWindow;

    private Order order;

    public DashboardController() {
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.dashboardWindow = (Window)comp;
        Util.createBindingsFor(this.dashboardWindow);
    }

    public void setCurrentOrder(Order order) {
        this.order = order;
    }

    public void reload() {
        dashboardModel.setCurrentOrder(order);
        if (this.dashboardWindow != null) {
            Util.reloadBindings(this.dashboardWindow);
        }
    }

    /* Test */
    public String getTextForLabel() {
        if (dashboardModel.getPercentageOfFinishedTasks() == null) {
            return "NULL";
        }
        String out = "% Finished: " + dashboardModel.getPercentageOfFinishedTasks().toString() + "" +
                "% In progress: " + dashboardModel.getPercentageOfInProgressTasks() + " " +
                "% Ready to Start: " + dashboardModel.getPercentageOfReadyToStartTasks() + " " +
                "% Blocked: " + dashboardModel.getPercentageOfBlockedTasks() + " " +
                "A% hours: " + dashboardModel.getAdvancePercentageByHours() + " " +
                "TA% hours: " + dashboardModel.getTheoreticalAdvancePercentageByHoursUntilNow() + " " +
                "ACP% hours: " + dashboardModel.getCriticalPathProgressByNumHours() + " " +
                "TACP% hours: " + dashboardModel.getTheoreticalProgressByNumHoursForCriticalPathUntilNow() + " " +
                "ACP% duration: " + dashboardModel.getCriticalPathProgressByDuration() + " " +
                "TACP% duration: " + dashboardModel.getTheoreticalProgressByDurationForCriticalPathUntilNow();
        return out;
    }

}
