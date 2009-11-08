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

package org.navalplanner.web.planner.taskedition;

import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.TaskEditFormComposer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Intbox;

/**
 * Controller for edit {@link Task} popup.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@org.springframework.stereotype.Component("editTaskController")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EditTaskController extends GenericForwardComposer {

    private TaskElement currentTaskElement;

    private Intbox hours;

    private Intbox duration;

    /**
     * Controller from the Gantt to manage common fields on edit {@link Task}
     * popup.
     */
    private TaskEditFormComposer taskEditFormComposer = new TaskEditFormComposer();

    public void showEditFormFor(Component openRelativeTo,
            org.zkoss.ganttz.data.Task task,
            TaskElement taskElement) {
        this.currentTaskElement = taskElement;
        taskEditFormComposer.showEditFormFor(openRelativeTo, task);
        updateComponentValuesForTask();
    }

    private void updateComponentValuesForTask() {
        if (currentTaskElement instanceof Task) {
            showDurationRow((Task) currentTaskElement);
        } else {
            hideDurationRow();
        }
        hours.setValue(currentTaskElement.getWorkHours());
    }

    private void hideDurationRow() {
        hours.getFellow("durationRow").setVisible(false);
    }

    private void showDurationRow(Task task) {
        hours.getFellow("durationRow").setVisible(true);
        duration.setValue(task.getDaysDuration());
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        taskEditFormComposer.doAfterCompose(comp);
    }
}
