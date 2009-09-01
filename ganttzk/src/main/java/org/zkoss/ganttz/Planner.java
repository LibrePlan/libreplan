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
import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.ganttz.extensions.ITabFactory;
import org.zkoss.ganttz.util.IMenuItemsRegister;
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
        return Planner.findComponentsOfType(TaskList.class, children).get(0);
    }

    public static <T> List<T> findComponentsOfType(Class<T> type,
            List<? extends Object> children) {
        ArrayList<T> result = new ArrayList<T>();
        for (Object child : children) {
            if (type.isInstance(child)) {
                result.add(type.cast(child));
            }
        }
        return result;
    }

    public String getContextPath() {
        return Executions.getCurrent().getContextPath();
    }

    public DependencyList getDependencyList() {
        if (ganttPanel == null)
            return null;
        List<Object> children = ganttPanel.getChildren();
        List<DependencyList> found = findComponentsOfType(DependencyList.class,
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
        visualizeTabs(context, configuration.getTabFactories());
    }

    private <T> TabsRegistry createTabs(IContext<T> context,
            List<ITabFactory<T>> factories) {
        List<ITab> tabs = new ArrayList<ITab>();
        tabs.add(createTasksPlanningTab());
        for (ITabFactory<T> factory : factories) {
            tabs.add(factory.create(context));
        }
        TabsRegistry tabsRegistry = new TabsRegistry(this);
        for (ITab t : tabs) {
            tabsRegistry.add(t);
        }
        return tabsRegistry;
    }

    private <T> void visualizeTabs(
            FunctionalityExposedForExtensions<T> context,
            List<ITabFactory<T>> tabFactories) {
        TabsRegistry tabs = createTabs(context, tabFactories);
        registryTabs(tabs);
        tabs.showFirst();
    }

    private void registryTabs(TabsRegistry tabs) {
        if (!getMenuItemsRegisterLocator().isRegistered())
            return;
        tabs.registerAtMenu(getMenuItemsRegisterLocator().retrieve());
    }

    private OnZKDesktopRegistry<IMenuItemsRegister> getMenuItemsRegisterLocator() {
        return OnZKDesktopRegistry.getLocatorFor(IMenuItemsRegister.class);
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

    private TasksPlanningTab createTasksPlanningTab() {
        this.leftPane = new LeftPane(contextualizedGlobalCommands,
                this.diagramGraph.getTopLevelTasks());
        this.ganttPanel = new GanttPanel(this.context,
                commandsOnTasksContextualized, editTaskCommand);
        TasksPlanningTab result = new TasksPlanningTab(this, leftPane,
                ganttPanel);
        result.afterCompose();
        this.leftPane
                .setGoingDownInLastArrowCommand(goingDownInLastArrowCommand);
        return result;
    }

    void removeTask(Task task) {
        TaskList taskList = getTaskList();
        taskList.remove(task);
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
