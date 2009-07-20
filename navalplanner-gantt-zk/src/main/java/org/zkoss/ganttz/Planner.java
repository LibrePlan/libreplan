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
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.ganttz.util.DependencyBean;
import org.zkoss.ganttz.util.GanttDiagramGraph;
import org.zkoss.ganttz.util.TaskBean;
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

    private void removePreviousGanntPanel() {
        List<Object> children = getChildren();
        for (GanttPanel ganttPanel : findComponentsOfType(GanttPanel.class,
                children)) {
            removeChild(ganttPanel);
        }
    }

    public DependencyList getDependencyList() {
        List<Object> children = ganttPanel.getChildren();
        List<DependencyList> found = findComponentsOfType(DependencyList.class,
                children);
        if (found.isEmpty())
            return null;
        return found.get(0);
    }

    private void removePreviousDetails() {
        List<Object> children = getChildren();
        for (LeftTasksTree l : Planner.findComponentsOfType(
                LeftTasksTree.class, children)) {
            removeChild(l);
        }
    }

    public TaskEditFormComposer getModalFormComposer() {
        return taskEditFormComposer;
    }

    public boolean canAddDependency(DependencyBean dependency) {
        return dependencyAdder.canAddDependency(dependency);
    }

    public void registerListeners() {
        ganttPanel.afterCompose();
        TaskList taskList = getTaskList();
        dependencyAddedListener = new DependencyAddedListener() {

            @Override
            public void dependenceAdded(Dependency dependency) {
                getDependencyList().addDependency(dependency);
                diagramGraph.add(dependency.getDependencyBean());
                dependencyAdder.addDependency(dependency.getDependencyBean());
            }
        };
        taskList.addDependencyListener(dependencyAddedListener);
        taskRemovedListener = new TaskRemovedListener() {

            @Override
            public void taskRemoved(Task taskRemoved) {
                diagramGraph.remove(taskRemoved.getTaskBean());
                leftPane.taskRemoved(taskRemoved.getTaskBean());
                TaskList taskList = getTaskList();
                setHeight(getHeight());// forcing smart update
                taskList.adjustZoomColumnsHeight();
                getDependencyList().redrawDependencies();
            }
        };
        taskList.addTaskRemovedListener(taskRemovedListener);
        dependencyRemovedListener = new DependencyRemovedListener() {

            @Override
            public void dependenceRemoved(Dependency dependency) {
                diagramGraph.remove(dependency);
            }
        };
        getDependencyList().addDependencyRemovedListener(
                dependencyRemovedListener);
    }

    public void addTask(TaskBean newTask) {
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

        public void addDependency(DependencyBean bean) {
            adapter.addDependency(toDomainDependency(bean));
        }

        private DomainDependency<T> toDomainDependency(DependencyBean bean) {
            T source = mapper.findAssociatedDomainObject(bean.getSource());
            T destination = mapper.findAssociatedDomainObject(bean
                    .getDestination());
            DomainDependency<T> dep = DomainDependency.createDependency(source,
                    destination, bean.getType());
            return dep;
        }

        public boolean canAddDependency(DependencyBean bean) {
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
        context.add(configuration.getData());
        dependencyAdder = new DependencyAdderAdapter<T>(configuration
                .getAdapter(), context.getMapper());
        this.contextualizedCommands = contextualize(context,
                configuration
                .getCommands());
        recreate();
    }

    private <T> List<CommandContextualized<T>> contextualize(
            IContext<T> context,
            Collection<? extends ICommand<T>> commands) {
        ArrayList<CommandContextualized<T>> result = new ArrayList<CommandContextualized<T>>();
        for (ICommand<T> command : commands) {
            result.add(CommandContextualized.create(command, context));
        }
        return result;
    }

    public GanttDiagramGraph getGanttDiagramGraph() {
        return diagramGraph;
    }

    private void recreate() {
        removePreviousDetails();
        this.leftPane = new LeftPane(contextualizedCommands, this.diagramGraph
                .getTopLevelTasks());
        insertBefore(this.leftPane, (Component) (getChildren().isEmpty() ? null
                : getChildren().get(0)));
        this.leftPane.afterCompose();
        removePreviousGanntPanel();
        this.ganttPanel = new GanttPanel(this.diagramGraph,
                taskEditFormComposer);
        appendChild(ganttPanel);
        registerListeners();
    }
}
