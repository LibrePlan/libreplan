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

import static org.zkoss.ganttz.i18n.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.zkoss.ganttz.adapters.IDisabilityConfiguration;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.data.Dependency;
import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.data.Position;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.data.GanttDiagramGraph.IGraphChangeListener;
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.extensions.ICommandOnTask;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.TimeTrackerComponent;
import org.zkoss.ganttz.timetracker.TimeTrackerComponentWithoutColumns;
import org.zkoss.ganttz.util.ComponentsFinder;
import org.zkoss.ganttz.util.LongOperationFeedback;
import org.zkoss.ganttz.util.OnZKDesktopRegistry;
import org.zkoss.ganttz.util.LongOperationFeedback.ILongOperation;
import org.zkoss.ganttz.util.script.IScriptsRegister;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zul.Separator;
import org.zkoss.zul.api.Button;

public class Planner extends HtmlMacroComponent  {

    public static void registerNeededScripts() {
        IScriptsRegister register = getScriptsRegister();
        register.register(ScriptsRequiredByPlanner.class);
    }

    private static IScriptsRegister getScriptsRegister() {
        return OnZKDesktopRegistry.getLocatorFor(IScriptsRegister.class)
                .retrieve();
    }

    private GanttDiagramGraph diagramGraph;

    private LeftPane leftPane;

    private GanttPanel ganttPanel;

    private List<? extends CommandContextualized<?>> contextualizedGlobalCommands;

    private CommandContextualized<?> goingDownInLastArrowCommand;

    private List<? extends CommandOnTaskContextualized<?>> commandsOnTasksContextualized;

    private CommandOnTaskContextualized<?> doubleClickCommand;

    private FunctionalityExposedForExtensions<?> context;

    private transient IDisabilityConfiguration disabilityConfiguration;

    private boolean isShowingCriticalPath = false;

    public Planner() {
        registerNeededScripts();
    }

    TaskList getTaskList() {
        if (ganttPanel == null) {
            return null;
        }
        List<Object> children = ganttPanel.getChildren();
        return ComponentsFinder.findComponentsOfType(TaskList.class, children).get(0);
    }

    public String getContextPath() {
        return Executions.getCurrent().getContextPath();
    }

    public DependencyList getDependencyList() {
        if (ganttPanel == null) {
            return null;
        }
        List<Object> children = ganttPanel.getChildren();
        List<DependencyList> found = ComponentsFinder.findComponentsOfType(DependencyList.class,
                children);
        if (found.isEmpty()) {
            return null;
        }
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

    public void zoomIncrease() {
        if (ganttPanel == null) {
            return;
        }
        LongOperationFeedback.execute(ganttPanel, new ILongOperation() {

            @Override
            public String getName() {
                return _("increasing zoom");
            }

            @Override
            public void doAction() throws Exception {
                ganttPanel.zoomIncrease();
            }
        });
    }

    public void zoomDecrease() {
        if (ganttPanel == null) {
            return;
        }
        LongOperationFeedback.execute(ganttPanel, new ILongOperation() {
            @Override
            public String getName() {
                return _("decreasing zoom");
            }

            @Override
            public void doAction() throws Exception {
                ganttPanel.zoomDecrease();
            }
        });
    }

    public <T> void setConfiguration(PlannerConfiguration<T> configuration) {
        if (configuration == null) {
            return;
        }
        this.diagramGraph = new GanttDiagramGraph(configuration
                .getStartConstraints(), configuration.getEndConstraints(),
                configuration.isDependenciesConstraintsHavePriority());
        FunctionalityExposedForExtensions<T> newContext = new FunctionalityExposedForExtensions<T>(
                this, configuration.getAdapter(), configuration.getNavigator(),
                diagramGraph, configuration.getFirstLevelModificators(),
                configuration.getSecondLevelModificators());
        this.contextualizedGlobalCommands = contextualize(newContext,
                configuration.getGlobalCommands());
        this.commandsOnTasksContextualized = contextualize(newContext,
                configuration.getCommandsOnTasks());
        goingDownInLastArrowCommand = contextualize(newContext, configuration
                .getGoingDownInLastArrowCommand());
        doubleClickCommand = contextualize(newContext, configuration
                .getDoubleClickCommand());
        this.context = newContext;
        this.disabilityConfiguration = configuration;
        newContext.add(configuration.getData());
        setupComponents();

        setAt("insertionPointLeftPanel", leftPane);
        leftPane.afterCompose();
        setAt("insertionPointRightPanel", ganttPanel);
        ganttPanel.afterCompose();

        TimeTrackerComponent timetrackerheader = new TimeTrackerComponentWithoutColumns(
                ganttPanel.getTimeTracker(), "timetrackerheader");

        setAt("insertionPointTimetracker", timetrackerheader);
        timetrackerheader.afterCompose();

        Component chartComponent = configuration.getChartComponent();
        if (chartComponent != null) {
            setAt("insertionPointChart", chartComponent);
        }

        if (!configuration.isCriticalPathEnabled()) {
            Button showCriticalPathButton = (Button) getFellow("showCriticalPath");
            showCriticalPathButton.setVisible(false);
        }
    }

    private void setAt(String insertionPointId, Component component) {
        Component insertionPoint = getFellow(insertionPointId);
        insertionPoint.getChildren().clear();
        insertionPoint.appendChild(component);
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
        if (command == null) {
            return null;
        }
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

    private void setupComponents() {
        insertGlobalCommands();
        this.leftPane = new LeftPane(this.diagramGraph.getTopLevelTasks());
        this.ganttPanel = new GanttPanel(this.context,
                commandsOnTasksContextualized, doubleClickCommand,
                disabilityConfiguration);
    }

    @SuppressWarnings("unchecked")
    private void insertGlobalCommands() {
        Component toolbar = getToolbar();
        Component firstSeparator = getFirstSeparatorFromToolbar();
        toolbar.getChildren().removeAll(getBefore(toolbar, firstSeparator));
        for (CommandContextualized<?> c : contextualizedGlobalCommands) {
            toolbar.insertBefore(c.toButton(), firstSeparator);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Component> getBefore(Component parent, Component child) {
        List<Component> children = parent.getChildren();
        List<Component> result = new ArrayList<Component>();
        for (Component object : children) {
            if (object == child) {
                break;
            }
            result.add(object);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Component getFirstSeparatorFromToolbar() {
        Component toolbar = getToolbar();
        List<Component> children = toolbar.getChildren();
        List<Separator> separators = ComponentsFinder
                .findComponentsOfType(
                Separator.class, children);
        return separators.get(0);
    }

    private Component getToolbar() {
        Component toolbar = getFellow("toolbar");
        return toolbar;
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

    @Override
    public void afterCompose() {
        super.afterCompose();
    }

    public TimeTracker getTimeTracker() {
        return ganttPanel.getTimeTracker();
    }

    private IGraphChangeListener showCriticalPathOnChange = new IGraphChangeListener() {

        @Override
        public void execute() {
            context.showCriticalPath();
        }
    };

    public void showCriticalPath() {
        Button showCriticalPathButton = (Button) getFellow("showCriticalPath");
        if (disabilityConfiguration.isCriticalPathEnabled()) {
            if (isShowingCriticalPath) {
                context.hideCriticalPath();
                diagramGraph.removePostGraphChangeListener(showCriticalPathOnChange);
                showCriticalPathButton.setSclass("planner-command");
            } else {
                context.showCriticalPath();
                diagramGraph.addPostGraphChangeListener(showCriticalPathOnChange);
                showCriticalPathButton.setSclass("planner-command clicked");
            }
            isShowingCriticalPath = !isShowingCriticalPath;
        }
    }

}
