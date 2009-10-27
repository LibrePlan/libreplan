/*
 * This file is part of ###PROJECT_NAME###
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

package org.navalplanner.web.planner.splitting;

import java.util.List;

import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.web.planner.PlanningState;
import org.navalplanner.web.planner.splitting.SplittingController.IActionOnOk;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;

import static org.navalplanner.web.I18nHelper._;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SplitTaskCommand implements ISplitTaskCommand {

    private PlanningState planningState;
    private SplittingController splittingController;

    @Override
    public void setState(PlanningState planningState) {
        this.planningState = planningState;
    }

    @Override
    public void doAction(final IContextWithPlannerTask<TaskElement> context,
            final TaskElement taskElement) {
        if (!taskElement.isLeaf()) {
            // TODO show some message if this action is not aplyable
            return;
        }
        final Task task = (Task) taskElement;
        int[] shares = createTwoEqualShares(taskElement);
        splittingController.show(
                ShareBean.toShareBeans(task.getName(), shares), task
                        .getWorkHours(), new IActionOnOk() {

                    @Override
                    public void doOkAction(ShareBean[] shares) {
                        TaskGroup newGroup = task.split(ShareBean
                                .toHours(shares));
                        List<TaskElement> children = newGroup.getChildren();
                        for (int i = 0; i < shares.length; i++) {
                            children.get(i).setName(shares[i].getName());
                        }
                        context.replace(task, newGroup);
                        planningState.removed(taskElement);
                        planningState.added(newGroup);
                    }
                });
    }

    private int[] createTwoEqualShares(TaskElement taskElement) {
        Integer workHours = taskElement.getWorkHours();
        int half = workHours / 2;
        return new int[] { half, half + workHours % 2 };
    }

    @Override
    public String getName() {
        return _("Split task");
    }

    @Override
    public void setSplitWindowController(SplittingController splittingController) {
        this.splittingController = splittingController;
    }

}
