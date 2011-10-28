/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.zkoss.ganttz;

import java.util.Collection;
import java.util.List;

import org.zkoss.ganttz.adapters.IDisabilityConfiguration;
import org.zkoss.ganttz.data.Position;
import org.zkoss.ganttz.data.Task;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;

/**
 * LeftPane of the planner. Responsible of showing global commands and the
 * leftTasksTree <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class LeftPane extends HtmlMacroComponent {

    private final List<Task> topLevelTasks;

    private LeftTasksTree leftTasksTree;

    private final IDisabilityConfiguration disabilityConfiguration;

    private FilterAndParentExpandedPredicates predicate;

    public void setGoingDownInLastArrowCommand(
            CommandContextualized<?> goingDownInLastArrowCommand) {
        this.leftTasksTree
                .setGoingDownInLastArrowCommand(goingDownInLastArrowCommand);
    }

    public LeftPane(IDisabilityConfiguration disabilityConfiguration,
            List<Task> topLevelTasks,
            FilterAndParentExpandedPredicates predicate) {
        this.topLevelTasks = topLevelTasks;
        this.disabilityConfiguration = disabilityConfiguration;
        this.predicate = predicate;
    }

    @Override
    public void afterCompose() {
        super.afterCompose();
        leftTasksTree = new LeftTasksTree(disabilityConfiguration,
                topLevelTasks, predicate);
        getContainer().appendChild(leftTasksTree);
        leftTasksTree.afterCompose();
    }

    private Component getContainer() {
        Component container = getFellow("listdetails_container");
        return container;
    }

    public void taskRemoved(Task task) {
        leftTasksTree.taskRemoved(task);
    }

    public void addTask(Position position, Task newTask) {
        leftTasksTree.addTask(position, newTask);
    }

    public void addTasks(Position position, Collection<? extends Task> newTasks) {
        leftTasksTree.addTasks(position, newTasks);
    }

    public void setPredicate(FilterAndParentExpandedPredicates predicate) {
        this.predicate = predicate;
        leftTasksTree.setPredicate(predicate);
    }

}
