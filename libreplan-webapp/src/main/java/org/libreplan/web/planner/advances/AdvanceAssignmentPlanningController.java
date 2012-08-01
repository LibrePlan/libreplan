/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * Copyright (C) 2010-2011 WirelessGalicia, S.L.
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

package org.libreplan.web.planner.advances;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.web.common.Util;
import org.libreplan.web.orders.ManageOrderElementAdvancesController;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.TaskComponent;
import org.zkoss.ganttz.TaskList;
import org.zkoss.ganttz.adapters.PlannerConfiguration.IReloadChartListener;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.event.Event;
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

    private IContextWithPlannerTask<TaskElement> context;

    private IReloadChartListener reloadOverallProgressListener;

    private IReloadChartListener reloadEarnedValueListener;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.window = (Window) comp;
        setupAdvanceController();
    }

    public void showWindow(IContextWithPlannerTask<TaskElement> context,
            TaskElement task,
            PlanningState planningState) {
        this.context = context;
        advanceAssignmentPlanningModel.initAdvancesFor(task, context,
                planningState);
        showAdvanceWindow(advanceAssignmentPlanningModel.getOrderElement());

        try {
            window.setTitle(getTitle());
            Util.reloadBindings(window);
            this.window.setMode("modal");
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

    public void onClose(Event event) {
        accept();
        event.stopPropagation();
    }

    public void accept() {
        boolean result = manageOrderElementAdvancesController.close();
        if (result) {
            advanceAssignmentPlanningModel.accept();
            updateTaskComponents();
            close();
            reloadEarnedValueProgress();
            reloadOverallProgress();
        }
    }

    private void reloadOverallProgress() {
        if (reloadOverallProgressListener != null) {
            reloadOverallProgressListener.reloadChart();
        }
    }

    private void reloadEarnedValueProgress() {
        if (reloadEarnedValueListener != null) {
            reloadEarnedValueListener.reloadChart();
        }
    }

    private void updateTaskComponents() {
        if (context.getRelativeTo() instanceof TaskComponent) {
            // update the current taskComponent
            TaskComponent taskComponent = (TaskComponent) context
                    .getRelativeTo();
            updateTaskComponent(taskComponent);

            // update the current taskComponent's parents
            List<Task> parents = new ArrayList<Task>(context.getMapper()
                    .getParents(taskComponent.getTask()));
            TaskList taskList = taskComponent.getTaskList();
            for (Task task : parents) {
                TaskComponent parentComponent = taskList.find(task);
                if (parentComponent != null) {
                    updateTaskComponent(parentComponent);
                }
            }
        }
    }

    private void updateTaskComponent(TaskComponent taskComponent) {
        taskComponent.updateCompletionIfPossible();
        taskComponent.updateTooltipText();
        taskComponent.invalidate();
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

    public void reloadOverallProgressListener(IReloadChartListener reloadChartListener) {
        reloadOverallProgressListener = reloadChartListener;
    }

    public void setReloadEarnedValueListener(IReloadChartListener reloadEarnedValueListener) {
        this.reloadEarnedValueListener = reloadEarnedValueListener;
    }

}
