package org.zkoss.ganttz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.adapters.DomainDependency;
import org.zkoss.ganttz.adapters.IAdapterToTaskFundamentalProperties;
import org.zkoss.ganttz.adapters.IDomainAndBeansMapper;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.util.DependencyBean;
import org.zkoss.ganttz.util.DependencyRegistry;
import org.zkoss.ganttz.util.ITaskFundamentalProperties;
import org.zkoss.ganttz.util.TaskBean;
import org.zkoss.ganttz.util.TaskContainerBean;
import org.zkoss.ganttz.util.TaskLeafBean;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.impl.XulElement;

public class Planner extends XulElement {

    private static final Log LOG = LogFactory.getLog(Planner.class);

    private DependencyAddedListener dependencyAddedListener;
    private DependencyRegistry dependencyRegistry = new DependencyRegistry();
    private DependencyRemovedListener dependencyRemovedListener;
    private TaskRemovedListener taskRemovedListener;
    private ListDetails listDetails;

    private GanttPanel ganttPanel;

    private TaskEditFormComposer taskEditFormComposer = new TaskEditFormComposer();

    private OneToOneMapper<?> domainObjectsMapper;

    public Planner() {
    }

    TaskList getTaskList() {
        List<Object> children = findOneComponentOfType(GanttPanel.class)
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
        for (ListDetails l : Planner.findComponentsOfType(ListDetails.class,
                children)) {
            removeChild(l);
        }
    }

    public TaskEditFormComposer getModalFormComposer() {
        return taskEditFormComposer;
    }

    public void registerListeners() {
        if (dependencyRegistry == null)
            throw new IllegalStateException("dependencyRegistry must be set");
        ganttPanel.afterCompose();
        TaskList taskList = getTaskList();
        dependencyAddedListener = new DependencyAddedListener() {

            @Override
            public void dependenceAdded(Dependency dependency) {
                getDependencyList().addDependency(dependency);
                publishDependency(dependency);
            }
        };
        taskList.addDependencyListener(dependencyAddedListener);
        taskRemovedListener = new TaskRemovedListener() {

            @Override
            public void taskRemoved(Task taskRemoved) {
                dependencyRegistry.remove(taskRemoved.getTaskBean());
                listDetails.taskRemoved(taskRemoved.getTaskBean());
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
                dependencyRegistry.remove(dependency);
            }
        };
        getDependencyList().addDependencyRemovedListener(
                dependencyRemovedListener);
    }

    public void addTask(TaskBean newTask) {
        getTaskList().addTask(newTask);
        dependencyRegistry.addTopLevel(newTask);
    }

    private void publishDependency(Dependency dependency) {
        dependencyRegistry.add(dependency.getDependencyBean());
    }

    private static class OneToOneMapper<T> implements IDomainAndBeansMapper<T> {
        private Map<T, TaskBean> fromDomainToTaskBean = new HashMap<T, TaskBean>();

        private Map<TaskBean, T> fromTaskBeanToDomain = new HashMap<TaskBean, T>();

        @Override
        public TaskBean findAssociatedBean(T domainObject)
                throws IllegalArgumentException {
            if (domainObject == null)
                throw new IllegalArgumentException("domainObject is null");
            if (!fromDomainToTaskBean.containsKey(domainObject))
                throw new IllegalArgumentException("not found " + domainObject);
            return fromDomainToTaskBean.get(domainObject);
        }

        void register(TaskBean taskBean, T domainObject) {
            fromDomainToTaskBean.put(domainObject, taskBean);
            fromTaskBeanToDomain.put(taskBean, domainObject);
        }

        @Override
        public T findAssociatedDomainObject(TaskBean taskBean)
                throws IllegalArgumentException {
            if (taskBean == null)
                throw new IllegalArgumentException("taskBean is null");
            if (!fromTaskBeanToDomain.containsKey(taskBean))
                throw new IllegalArgumentException();
            return fromTaskBeanToDomain.get(taskBean);
        }

    }

    public <T> void setConfiguration(PlannerConfiguration<T> configuration) {
        if (configuration == null)
            return;
        this.dependencyRegistry = new DependencyRegistry();
        OneToOneMapper<T> mapper = new OneToOneMapper<T>();
        domainObjectsMapper = mapper;
        List<DomainDependency<T>> dependencies = new ArrayList<DomainDependency<T>>();
        for (T domainObject : configuration.getData()) {
            this.dependencyRegistry.addTopLevel(extractTaskBean(dependencies,
                    mapper, domainObject, configuration.getNavigator(),
                    configuration.getAdapter()));
        }
        List<DependencyBean> dependencyBeans = DomainDependency
                .toDependencyBeans(mapper, dependencies);
        for (DependencyBean dependencyBean : dependencyBeans) {
            this.dependencyRegistry.add(dependencyBean);
        }
        this.dependencyRegistry.applyAllRestrictions();
        recreate();
    }

    private <T> TaskBean extractTaskBean(
            List<DomainDependency<T>> dependencies, OneToOneMapper<T> mapper,
            T data, IStructureNavigator<T> navigator,
            IAdapterToTaskFundamentalProperties<T> adapter) {
        ITaskFundamentalProperties adapted = adapter.adapt(data);
        dependencies.addAll(adapter.getDependenciesOriginating(data));
        TaskBean result;
        if (navigator.isLeaf(data)) {
            result = new TaskLeafBean(adapted);
        } else {
            TaskContainerBean container = new TaskContainerBean(adapted);
            for (T child : navigator.getChildren(data)) {
                container.add(extractTaskBean(dependencies, mapper, child,
                        navigator, adapter));
            }
            return container;
        }
        mapper.register(result, data);
        return result;
    }

    public DependencyRegistry getDependencyRegistry() {
        return dependencyRegistry;
    }

    private void recreate() {
        removePreviousDetails();
        this.listDetails = new ListDetails(this.dependencyRegistry
                .getTopLevelTasks());
        insertBefore(this.listDetails,
                (Component) (getChildren().isEmpty() ? null : getChildren()
                        .get(0)));
        this.listDetails.afterCompose();
        removePreviousGanntPanel();
        this.ganttPanel = new GanttPanel(this.dependencyRegistry,
                taskEditFormComposer);
        appendChild(ganttPanel);
        registerListeners();
    }
}
