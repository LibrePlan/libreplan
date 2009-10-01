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

package org.zkoss.ganttz.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.extensions.ICommandOnTask;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;

/**
 * A object that defines several extension points for gantt planner
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class PlannerConfiguration<T> {

    private static class NullCommand<T> implements ICommand<T> {

        @Override
        public void doAction(IContext<T> context) {
            // do nothing
        }

        @Override
        public String getName() {
            return "";
        }

    }

    private static class NullCommandOnTask<T> implements ICommandOnTask<T> {

        @Override
        public void doAction(IContextWithPlannerTask<T> context, T task) {
            // do nothing
        }

        @Override
        public String getName() {
            return "";
        }

    }

    private IAdapterToTaskFundamentalProperties<T> adapter;

    private IStructureNavigator<T> navigator;

    private List<? extends T> data;

    private List<ICommand<T>> globalCommands = new ArrayList<ICommand<T>>();

    private List<ICommandOnTask<T>> commandsOnTasks = new ArrayList<ICommandOnTask<T>>();

    private ICommand<T> goingDownInLastArrowCommand = new NullCommand<T>();

    private ICommandOnTask<T> editTaskCommand = new NullCommandOnTask<T>();

    public PlannerConfiguration(IAdapterToTaskFundamentalProperties<T> adapter,
            IStructureNavigator<T> navigator, List<? extends T> data) {
        this.adapter = adapter;
        this.navigator = navigator;
        this.data = data;
    }

    public IAdapterToTaskFundamentalProperties<T> getAdapter() {
        return adapter;
    }

    public IStructureNavigator<T> getNavigator() {
        return navigator;
    }

    public List<? extends T> getData() {
        return data;
    }

    public void addCommandOnTask(ICommandOnTask<T> commandOnTask) {
        Validate.notNull(commandOnTask);
        this.commandsOnTasks.add(commandOnTask);
    }

    public void addGlobalCommand(ICommand<T> command) {
        Validate.notNull(command);
        this.globalCommands.add(command);
    }

    public List<ICommandOnTask<T>> getCommandsOnTasks() {
        return Collections.unmodifiableList(commandsOnTasks);
    }

    public List<ICommand<T>> getGlobalCommands() {
        return Collections.unmodifiableList(globalCommands);
    }

    public ICommand<T> getGoingDownInLastArrowCommand() {
        return goingDownInLastArrowCommand;
    }

    public void setGoingDownInLastArrowCommand(
            ICommand<T> goingDownInLastArrowCommand) {
        Validate.notNull(goingDownInLastArrowCommand);
        this.goingDownInLastArrowCommand = goingDownInLastArrowCommand;
    }

    public ICommandOnTask<T> getEditTaskCommand() {
        return editTaskCommand;
    }

    public void setEditTaskCommand(ICommandOnTask<T> editTaskCommand) {
        Validate.notNull(editTaskCommand);
        this.editTaskCommand = editTaskCommand;
    }
}
