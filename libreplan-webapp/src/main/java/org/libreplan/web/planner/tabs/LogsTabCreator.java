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
package org.libreplan.web.planner.tabs;

import static org.libreplan.web.I18nHelper._;
import static org.libreplan.web.planner.tabs.MultipleTabsPlannerController.BREADCRUMBS_SEPARATOR;
import static org.libreplan.web.planner.tabs.MultipleTabsPlannerController.getSchedulingLabel;

import java.util.HashMap;
import java.util.Map;

import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.common.Util;
import org.libreplan.web.logs.LogsController;
import org.libreplan.web.planner.order.IOrderPlanningGate;
import org.libreplan.web.planner.tabs.CreatedOnDemandTab.IComponentCreator;
import org.libreplan.web.security.SecurityUtils;
import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;

/**
 * Creates global Tab for Logs(issue and risk logs).
 *
 * @author Misha Gozhda <misha@libreplan-enterprise.com>
 */
public class LogsTabCreator {

    public static ITab create(Mode mode, LogsController logsController, LogsController logsControllerGlobal,
                              Component breadcrumbs, IOrderPlanningGate orderPlanningGate,
                              Map<String, String[]> parameters) {

        return new LogsTabCreator(
                mode, logsController, logsControllerGlobal, breadcrumbs, orderPlanningGate).build();
    }

    private final Mode mode;

    private final LogsController logsControllerGlobal;

    private final LogsController logsController;

    private final Component breadcrumbs;

    private LogsTabCreator(Mode mode, LogsController logsController, LogsController logsControllerGlobal,
                           Component breadcrumbs, IOrderPlanningGate orderPlanningGate) {
        this.mode = mode;
        this.logsController = logsController;
        this.logsControllerGlobal = logsControllerGlobal;
        this.breadcrumbs = breadcrumbs;
    }

    private ITab build() {
        return TabOnModeType.forMode(mode)
                .forType(ModeType.GLOBAL, createGlobalLogsTab())
                .forType(ModeType.ORDER, createOrderLogsTab())
                .create();
    }


    private ITab createGlobalLogsTab() {
        IComponentCreator componentCreator = new IComponentCreator() {
            @Override
            public Component create(Component parent) {
                Map<String, Object> arguments = new HashMap<>();
                arguments.put("logsController", logsControllerGlobal);
                return Executions.createComponents("/logs/_logs.zul", parent, arguments);
            }

        };
        return new CreatedOnDemandTab(_("Logs"), "logs-global", componentCreator) {
            @Override
            protected void beforeShowAction() {
                if (!SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_PLANNING)) {
                    Util.sendForbiddenStatusCodeInHttpServletResponse();
                }
            }

            @Override
            protected void afterShowAction() {
                if (breadcrumbs.getChildren() != null) {
                    breadcrumbs.getChildren().clear();
                }

                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(getSchedulingLabel()));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(_("Logs")));
            }
        };
    }

    private ITab createOrderLogsTab() {
        IComponentCreator componentCreator = new IComponentCreator() {
            @Override
            public Component create(Component parent) {
                Map<String, Object> arguments = new HashMap<>();
                arguments.put("logsController", logsController);
                return Executions.createComponents("/logs/_logs.zul", parent, arguments);
            }

        };
        return new CreatedOnDemandTab(_("Logs"), "logs-order", componentCreator) {
            @Override
            protected void beforeShowAction() {
                if (!SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_PLANNING)) {
                    Util.sendForbiddenStatusCodeInHttpServletResponse();
                }
            }

            @Override
            protected void afterShowAction() {
                if (breadcrumbs.getChildren() != null) {
                    breadcrumbs.getChildren().clear();
                }

                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(getSchedulingLabel()));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(_("Logs")));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(mode.getOrder().getName()));
            }
        };
    }

}
