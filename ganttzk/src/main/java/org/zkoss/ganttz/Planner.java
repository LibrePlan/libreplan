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

package org.zkoss.ganttz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.data.Dependency;
import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.data.Position;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.extensions.ICommandOnTask;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.ganttz.util.ComponentsFinder;
import org.zkoss.ganttz.util.OnZKDesktopRegistry;
import org.zkoss.ganttz.util.script.IScriptsRegister;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.impl.XulElement;

public class Planner extends XulElement {

    private GanttDiagramGraph diagramGraph = new GanttDiagramGraph();

    private LeftPane leftPane;

    private GanttPanel ganttPanel;

    private List<? extends CommandContextualized<?>> contextualizedGlobalCommands;

    private CommandContextualized<?> goingDownInLastArrowCommand;

    private List<? extends CommandOnTaskContextualized<?>> commandsOnTasksContextualized;

    private CommandOnTaskContextualized<?> editTaskCommand;

    private FunctionalityExposedForExtensions<?> context;

    public Planner() {
        registerNeededScripts();
    }

    private void registerNeededScripts() {
        IScriptsRegister register = getScriptsRegister();
        register.register(ScriptsRequiredByPlanner.class);
    }

    TaskList getTaskList() {
        if (ganttPanel == null)
            return null;
        List<Object> children = ganttPanel.getChildren();
        return ComponentsFinder.findComponentsOfType(TaskList.class, children).get(0);
    }

    public String getContextPath() {
        return Executions.getCurrent().getContextPath();
    }

    public DependencyList getDependencyList() {
        if (ganttPanel == null)
            return null;
        List<Object> children = ganttPanel.getChildren();
        List<DependencyList> found = ComponentsFinder.findComponentsOfType(DependencyList.class,
                children);
        if (found.isEmpty())
            return null;
        return found.get(0);
    }

    public void addTasks(Position position, Collection<? extends Task> newTasks) {
        TaskList taskList = getTaskList();
        if (taskList != null && leftPane != null) {
            taskList.addTasks(position, newTasks);
            leftPane.addTasks(position, newTasks);
        }
    }

    public void addTask(Position position, Task task) {
        addTasks(position, Arrays.asList(task));
    }

    void addDependencies(Collection<? extends Dependency> dependencies) {
        DependencyList dependencyList = getDependencyList();
        if (dependencyList == null) {
            return;
        }
        for (DependencyComponent d : getTaskList().asDependencyComponents(
                dependencies)) {
            dependencyList.addDependencyComponent(d);
        }
    }

    public <T> void setConfiguration(PlannerConfiguration<T> configuration) {
        if (configuration == null)
            return;
        this.diagramGraph = new GanttDiagramGraph();
        FunctionalityExposedForExtensions<T> context = new FunctionalityExposedForExtensions<T>(
                this, configuration.getAdapter(), configuration.getNavigator(),
                diagramGraph);
        this.contextualizedGlobalCommands = contextualize(context,
                configuration.getGlobalCommands());
        this.commandsOnTasksContextualized = contextualize(context,
                configuration.getCommandsOnTasks());
        goingDownInLastArrowCommand = contextualize(context, configuration
                .getGoingDownInLastArrowCommand());
        editTaskCommand = contextualize(context, configuration
                .getEditTaskCommand());
        this.context = context;
        clear();
        context.add(configuration.getData());
        setupComponents();
    }

    private void clear() {
        this.leftPane = null;
        this.ganttPanel = null;
        getChildren().clear();
    }

    private <T> List<CommandOnTaskContextualized<T>> contextualize(
            FunctionalityExposedForExtensions<T> context,
            List<ICommandOnTask<T>> commands) {
        List<CommandOnTaskContextualized<T>> result = new ArrayList<CommandOnTaskContextualized<T>>();
        for (ICommandOnTask<T> c : commands) {
            result.add(contextualize(context, c));
        }
        return result;
    }

    private <T> CommandOnTaskContextualized<T> contextualize(
            FunctionalityExposedForExtensions<T> context,
            ICommandOnTask<T> commandOnTask) {
        return CommandOnTaskContextualized.create(commandOnTask, context
                .getMapper(), context);
    }

    private <T> CommandContextualized<T> contextualize(IContext<T> context,
            ICommand<T> command) {
        if (command == null)
            return null;
        return CommandContextualized.create(command, context);
    }

    private <T> List<CommandContextualized<T>> contextualize(
            IContext<T> context, Collection<? extends ICommand<T>> commands) {
        ArrayList<CommandContextualized<T>> result = new ArrayList<CommandContextualized<T>>();
        for (ICommand<T> command : commands) {
            result.add(contextualize(context, command));
        }
        return result;
    }

    public GanttDiagramGraph getGanttDiagramGraph() {
        return diagramGraph;
    }

    private void setupComponents() {
        this.leftPane = new LeftPane(contextualizedGlobalCommands,
                this.diagramGraph.getTopLevelTasks());
        this.ganttPanel = new GanttPanel(this.context,
                commandsOnTasksContextualized, editTaskCommand);
        leftPane.setParent(this);
        ganttPanel.setParent(this);
        leftPane.afterCompose();
        ganttPanel.afterCompose();
        this.leftPane
                .setGoingDownInLastArrowCommand(goingDownInLastArrowCommand);
    }

    void removeTask(Task task) {
        TaskList taskList = getTaskList();
        taskList.remove(task);
        getDependencyList().taskRemoved(task);
        leftPane.taskRemoved(task);
        setHeight(getHeight());// forcing smart update
        taskList.adjustZoomColumnsHeight();
        getDependencyList().redrawDependencies();
    }

    private IScriptsRegister getScriptsRegister() {
        return OnZKDesktopRegistry.getLocatorFor(IScriptsRegister.class)
                .retrieve();
    }
}
