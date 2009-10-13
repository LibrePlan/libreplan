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

package org.navalplanner.web.planner;

import static org.navalplanner.web.I18nHelper._;

import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.TaskMilestone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;

/**
 * Command to add a new {@link TaskMilestone} <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AddMilestoneCommand implements IAddMilestoneCommand {

    private PlanningState planningState;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Override
    public void setState(PlanningState planningState) {
        this.planningState = planningState;
    }

    @Override
    @Transactional(readOnly = true)
    public void doAction(IContextWithPlannerTask<TaskElement> context,
            TaskElement task) {
        TaskMilestone milestone = new TaskMilestone();
        milestone.setName("new milestone");

        taskElementDAO.save(task);
        getRoot(task).addTaskElement(getRoot(task).getChildren().indexOf(task),
                milestone);

        context.add(context.getMapper().findPositionFor(task), milestone);
        planningState.added(milestone.getParent());
    }

    private TaskGroup getRoot(TaskElement task) {
        if (task.getParent() == null) {
            return (TaskGroup) task;
        }

        return getRoot(task.getParent());
    }

    @Override
    public String getName() {
        return _("Add Milestone");
    }

}