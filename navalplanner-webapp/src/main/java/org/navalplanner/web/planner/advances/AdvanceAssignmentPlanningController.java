/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.planner.advances;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.orders.ManageOrderElementAdvancesController;
import org.navalplanner.web.planner.order.PlanningState;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Window;

/**
 * Controller for {@link Advance} assignment in the order planning view.
 * @author Susana Montes Pedreira <smontes@wirelessgailicia.com>
 */
@org.springframework.stereotype.Component("advanceAssignmentPlanningController")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AdvanceAssignmentPlanningController extends GenericForwardComposer {

    private static final Log LOG = LogFactory
            .getLog(AdvanceAssignmentPlanningController.class);

    private ManageOrderElementAdvancesController manageOrderElementAdvancesController;

    private IAdvanceAssignmentPlanningModel advanceAssignmentPlanningModel;

    private Window window;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.window = (Window) comp;
        setupAdvanceController();
    }

    public void showWindow(IContextWithPlannerTask<TaskElement> context,
            TaskElement task,
            PlanningState planningState) {

        advanceAssignmentPlanningModel.initAdvancesFor(task, context,
                planningState);
        showAdvanceWindow(advanceAssignmentPlanningModel.getOrderElement());

        try {
            window.setTitle(getTitle());
            Util.reloadBindings(window);
            this.window.doModal();
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupAdvanceController() {
        Component orderElementAdvances = window
                .getFellowIfAny("orderElementAdvances");
        manageOrderElementAdvancesController = (ManageOrderElementAdvancesController) orderElementAdvances
                .getVariable("manageOrderElementAdvancesController", true);
    }

    private void showAdvanceWindow(OrderElement orderElement) {
        manageOrderElementAdvancesController.openWindow(orderElement);
    }

    public void cancel() {
        advanceAssignmentPlanningModel.cancel();
        close();
    }

    public void accept() {
        boolean result = manageOrderElementAdvancesController.close();
        if (result) {
            advanceAssignmentPlanningModel.accept();
            close();
        }
    }

    private void close() {
        window.setVisible(false);
    }

    public String getTitle(){
        String title = "Advance Assignments";
        if ((advanceAssignmentPlanningModel != null)
                && (advanceAssignmentPlanningModel.getOrderElement() != null)) {
            title = advanceAssignmentPlanningModel.getOrderElement().getName();
        }
        return title;
    }
}
