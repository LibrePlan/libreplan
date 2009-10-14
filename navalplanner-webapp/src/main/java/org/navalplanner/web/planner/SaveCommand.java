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

import java.util.List;

import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.zul.Messagebox;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
/**
 * A command that saves the changes in the taskElements.
 * It can be considered the final step in the conversation <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class SaveCommand implements ISaveCommand {

    @Autowired
    private ITaskElementDAO taskElementDAO;

    private PlanningState state;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Override
    public void setState(PlanningState state) {
        this.state = state;
    }

    @Override
    public void doAction(IContext<TaskElement> context) {
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                doTheSaving();
                return null;
            }
        });
        notifyUserThatSavingIsDone();
    }

    private void notifyUserThatSavingIsDone() {
        try {
            Messagebox.show(_("Scheduling saved"), _("Information"), Messagebox.OK,
                    Messagebox.INFORMATION);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void doTheSaving() {
        saveTasksToSave();
        removeTasksToRemove();
        taskElementDAO.removeOrphanedDayAssignments();
    }

    private void removeTasksToRemove() {
        for (TaskElement taskElement : state.getToRemove()) {
            if (taskElementDAO.exists(taskElement.getId())) {
                // it might have already been saved in a previous save action
                try {
                    taskElementDAO.remove(taskElement.getId());
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void saveTasksToSave() {
        for (TaskElement taskElement : state.getTasksToSave()) {
            taskElementDAO.save(taskElement);
            if (taskElement instanceof Task) {
                if (!((Task) taskElement).isValidResourceAllocationWorkers()) {
                    throw new RuntimeException(_("The task '{0}' has some repeated Worker assigned",
                                taskElement.getName()));
                }
                for (ResourceAllocation<?> resourceAllocation : ((Task) taskElement)
                        .getResourceAllocations()) {
                    resourceAllocation.dontPoseAsTransientObjectAnymore();
                    for (DayAssignment dayAssignment : (List<? extends DayAssignment>) resourceAllocation
                            .getAssignments()) {
                        dayAssignment.dontPoseAsTransientObjectAnymore();
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return _("Save");
    }

}
