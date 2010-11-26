/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
package org.navalplanner.web.planner.tabs;

import static org.navalplanner.web.I18nHelper._;
import static org.navalplanner.web.planner.tabs.MultipleTabsPlannerController.BREADCRUMBS_SEPARATOR;
import static org.navalplanner.web.planner.tabs.MultipleTabsPlannerController.PLANNIFICATION;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.web.common.TemplateModel;
import org.navalplanner.web.montecarlo.MonteCarloController;
import org.navalplanner.web.planner.order.OrderPlanningController;
import org.navalplanner.web.planner.tabs.CreatedOnDemandTab.IComponentCreator;
import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public class MonteCarloTabCreator {

    private static String ORDER_LIMITING_RESOURCES_VIEW = _("MonteCarlo Method");

    public static ITab create(Mode mode,
            MonteCarloController monteCarloController,
            OrderPlanningController orderPlanningController,
            Component breadcrumbs) {

        return new MonteCarloTabCreator(mode, monteCarloController,
                orderPlanningController, breadcrumbs).build();
    }

    private final Mode mode;

    private final MonteCarloController monteCarloController;

    private final OrderPlanningController orderPlanningController;

    private final Component breadcrumbs;

    private MonteCarloTabCreator(Mode mode,
            MonteCarloController MonteCarloController,
            OrderPlanningController orderPlanningController,
            Component breadcrumbs) {
        this.mode = mode;
        this.monteCarloController = MonteCarloController;
        this.orderPlanningController = orderPlanningController;
        this.breadcrumbs = breadcrumbs;
    }

    private ITab build() {
        return TabOnModeType.forMode(mode)
                .forType(ModeType.GLOBAL, createGlobalMonteCarloTab())
                .forType(ModeType.ORDER, createOrderMonteCarloTab())
                .create();
    }

    private ITab createOrderMonteCarloTab() {

        IComponentCreator componentCreator = new IComponentCreator() {

            @Override
            public org.zkoss.zk.ui.Component create(
                    org.zkoss.zk.ui.Component parent) {

                Map<String, Object> arguments = new HashMap<String, Object>();
                arguments.put("monteCarloController",
                        monteCarloController);
                return Executions.createComponents(
                        "/montecarlo/_montecarlo.zul", parent, arguments);
            }

        };

        return new CreatedOnDemandTab(ORDER_LIMITING_RESOURCES_VIEW,
                "order-limiting-resources", componentCreator) {

            @Override
            protected void afterShowAction() {
                List<TaskElement> criticalPath = orderPlanningController.getCriticalPath();
                if (criticalPath == null) {
                    criticalPath = TemplateModel.getCriticalPathFor(mode.getOrder());
                }
                monteCarloController.setCriticalPath(criticalPath);

                breadcrumbs.getChildren().clear();
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(PLANNIFICATION));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs
                        .appendChild(new Label(ORDER_LIMITING_RESOURCES_VIEW));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(mode.getOrder().getName()));
            }
        };
    }

    private ITab createGlobalMonteCarloTab() {

        final IComponentCreator componentCreator = new IComponentCreator() {

            @Override
            public org.zkoss.zk.ui.Component create(
                    org.zkoss.zk.ui.Component parent) {
                // do nothing
                return null;
            }

        };
        return new CreatedOnDemandTab(_("MonteCarlo Method"),
                "limiting-resources", componentCreator) {
            @Override
            protected void afterShowAction() {
                // do nothing
            }
        };
    }

}
