package org.zkoss.ganttz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.adapters.DomainDependency;
import org.zkoss.ganttz.adapters.IAdapterToTaskFundamentalProperties;
import org.zkoss.ganttz.adapters.IDomainAndBeansMapper;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.data.Dependency;
import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.impl.XulElement;

public class Planner extends XulElement {

    private static final Log LOG = LogFactory.getLog(Planner.class);

    private DependencyAddedListener dependencyAddedListener;
    private GanttDiagramGraph diagramGraph = new GanttDiagramGraph();
    private DependencyRemovedListener dependencyRemovedListener;
    private TaskRemovedListener taskRemovedListener;
    private LeftPane leftPane;

    private GanttPanel ganttPanel;

    private TaskEditFormComposer taskEditFormComposer = new TaskEditFormComposer();

    private DependencyAdderAdapter<?> dependencyAdder;

    private List<? extends CommandContextualized<?>> contextualizedCommands;

    private CommandContextualized<?> goingDownInLastArrowCommand;

    public Planner() {
    }

    TaskList getTaskList() {
        if (ganttPanel == null)
            return null;
        List<Object> children = ganttPanel
                .getChildren();
        return Planner.findComponentsOfType(TaskList.class, children).get(0);
    }

    private <T> T findOneComponentOfType(Class<T> type) {
        List<T> result = findComponentsOfType(type, getChildren());
        if (result.isEmpty()) {
            throw new RuntimeException("it should have found a "
                    + type.getSimpleName() + " in "
                    + Planner.class.getSimpleName());
        }
        return result.get(0);
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
        List<Object> children = ganttPanel.getChildren();
        List<DependencyList> found = findComponentsOfType(DependencyList.class,
                children);
        if (found.isEmpty())
            return null;
        return found.get(0);
    }

    public TaskEditFormComposer getModalFormComposer() {
        return taskEditFormComposer;
    }

    public boolean canAddDependency(Dependency dependency) {
        return dependencyAdder.canAddDependency(dependency);
    }

    public void registerListeners() {
        TaskList taskList = getTaskList();
        dependencyAddedListener = new DependencyAddedListener() {

            @Override
            public void dependenceAdded(DependencyComponent dependencyComponent) {
                getDependencyList().addDependencyComponent(dependencyComponent);
                diagramGraph.add(dependencyComponent.getDependency());
                dependencyAdder.addDependency(dependencyComponent.getDependency());
            }
        };
        taskList.addDependencyListener(dependencyAddedListener);
        taskRemovedListener = new TaskRemovedListener() {

            @Override
            public void taskComponentRemoved(TaskComponent taskComponentRemoved) {
                diagramGraph.remove(taskComponentRemoved.getTask());
                leftPane.taskRemoved(taskComponentRemoved.getTask());
                TaskList taskList = getTaskList();
                setHeight(getHeight());// forcing smart update
                taskList.adjustZoomColumnsHeight();
                getDependencyList().redrawDependencies();
            }
        };
        taskList.addTaskRemovedListener(taskRemovedListener);
        dependencyRemovedListener = new DependencyRemovedListener() {

            @Override
            public void dependenceRemoved(DependencyComponent dependencyComponent) {
                diagramGraph.remove(dependencyComponent);
                dependencyAdder.removeDependency(dependencyComponent.getDependency());
            }
        };
        getDependencyList().addDependencyRemovedListener(
                dependencyRemovedListener);
    }

    public void addTask(Task newTask) {
        TaskList taskList = getTaskList();
        if (taskList != null && leftPane != null) {
            taskList.addTask(newTask);
            leftPane.addTask(newTask);
        }
    }

    private static class DependencyAdderAdapter<T> {

        private final IAdapterToTaskFundamentalProperties<T> adapter;
        private final IDomainAndBeansMapper<T> mapper;

        public DependencyAdderAdapter(
                IAdapterToTaskFundamentalProperties<T> adapter,
                IDomainAndBeansMapper<T> mapper) {
            this.adapter = adapter;
            this.mapper = mapper;
        }

        public void addDependency(Dependency bean) {
            adapter.addDependency(toDomainDependency(bean));
        }

        public void removeDependency(Dependency bean){
            adapter.removeDependency(toDomainDependency(bean));
        }

        private DomainDependency<T> toDomainDependency(Dependency bean) {
            T source = mapper.findAssociatedDomainObject(bean.getSource());
            T destination = mapper.findAssociatedDomainObject(bean
                    .getDestination());
            DomainDependency<T> dep = DomainDependency.createDependency(source,
                    destination, bean.getType());
            return dep;
        }

        public boolean canAddDependency(Dependency bean) {
            return adapter.canAddDependency(toDomainDependency(bean));
        }

    }

    public <T> void setConfiguration(PlannerConfiguration<T> configuration) {
        if (configuration == null)
            return;
        this.diagramGraph = new GanttDiagramGraph();
        FunctionalityExposedForExtensions<T> context = new FunctionalityExposedForExtensions<T>(
                this, configuration.getAdapter(), configuration.getNavigator(),
                diagramGraph);
        dependencyAdder = new DependencyAdderAdapter<T>(configuration
                .getAdapter(), context.getMapper());
        this.contextualizedCommands = contextualize(context,
                configuration
                .getGlobalCommands());
        goingDownInLastArrowCommand = contextualize(context, configuration
                .getGoingDownInLastArrowCommand());
        clear();
        context.add(configuration.getData());
        recreate();
        registerListeners();
    }

    private void clear() {
        this.leftPane = null;
        this.ganttPanel = null;
        getChildren().clear();
    }

    private <T> CommandContextualized<T> contextualize(
            IContext<T> context, ICommand<T> command) {
        return CommandContextualized.create(command, context);
    }

    private <T> List<CommandContextualized<T>> contextualize(
            IContext<T> context,
            Collection<? extends ICommand<T>> commands) {
        ArrayList<CommandContextualized<T>> result = new ArrayList<CommandContextualized<T>>();
        for (ICommand<T> command : commands) {
            result.add(contextualize(context, command));
        }
        return result;
    }

    public GanttDiagramGraph getGanttDiagramGraph() {
        return diagramGraph;
    }

    private void recreate() {
        this.leftPane = new LeftPane(contextualizedCommands, this.diagramGraph
                .getTopLevelTasks());
        this.leftPane.setParent(this);
        this.leftPane.afterCompose();
        this.leftPane
                .setGoingDownInLastArrowCommand(goingDownInLastArrowCommand);
        this.ganttPanel = new GanttPanel(this.diagramGraph,
                taskEditFormComposer);
        ganttPanel.setParent(this);
        ganttPanel.afterCompose();
    }
}
