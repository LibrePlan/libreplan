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

package org.navalplanner.web.planner.order;

import static org.navalplanner.web.I18nHelper._;

import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;

/**
 * Command to subcontract a {@link TaskElement} as XML.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SubcontractCommand implements ISubcontractCommand {

    private SubcontractController subcontractController;

    @Override
    public String getName() {
        return _("Subcontract");
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public boolean isApplicableTo(TaskElement task) {
        return (task instanceof Task);
    }

    @Override
    public void doAction(IContextWithPlannerTask<TaskElement> context,
            final TaskElement task) {
        if (isApplicableTo(task)) {
            subcontractController.showWindow((Task) task, context.getTask());
        }
    }

    @Override
    public void setSubcontractController(
            SubcontractController subcontractController) {
        this.subcontractController = subcontractController;
    }

}