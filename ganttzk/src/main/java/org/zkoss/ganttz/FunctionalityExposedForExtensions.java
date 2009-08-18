package org.zkoss.ganttz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.zkoss.ganttz.adapters.DomainDependency;
import org.zkoss.ganttz.adapters.IAdapterToTaskFundamentalProperties;
import org.zkoss.ganttz.adapters.IDomainAndBeansMapper;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.data.Dependency;
import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.data.ITaskFundamentalProperties;
import org.zkoss.ganttz.data.Position;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.data.TaskContainer;
import org.zkoss.ganttz.data.TaskLeaf;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.zk.ui.Component;

public class FunctionalityExposedForExtensions<T> implements IContext<T> {

    private static class OneToOneMapper<T> implements IDomainAndBeansMapper<T> {
        private Map<T, Task> fromDomainToTask = new HashMap<T, Task>();

        private Map<Task, T> fromTaskToDomain = new HashMap<Task, T>();

        private Map<Task, TaskContainer> fromTaskToParent = new HashMap<Task, TaskContainer>();

        private List<Task> topLevel = new ArrayList<Task>();

        @Override
        public Task findAssociatedBean(T domainObject)
                throws IllegalArgumentException {
            if (domainObject == null)
                throw new IllegalArgumentException("domainObject is null");
            if (!fromDomainToTask.containsKey(domainObject))
                throw new IllegalArgumentException("not found " + domainObject);
            return fromDomainToTask.get(domainObject);
        }

        /**
         * @param insertionPositionForTop
         *            the position in which to insert the task at the top level,
         *            if it must be added to the top level. If it is
         *            <code>null/code> it is appended to the end
         * @param task
         * @param domainObject
         * @param parent
         */
        void register(Integer insertionPositionForTop, Task task,
                T domainObject, TaskContainer parent) {
            fromDomainToTask.put(domainObject, task);
            fromTaskToDomain.put(task, domainObject);
            if (parent != null) {
                fromTaskToParent.put(task, parent);
            } else if (insertionPositionForTop != null) {
                topLevel.add(insertionPositionForTop, task);
            } else
                topLevel.add(task);
        }

        void remove(T domainObject) {
            Task toBeRemoved = findAssociatedBean(domainObject);
            fromDomainToTask.remove(domainObject);
            fromTaskToDomain.remove(toBeRemoved);
            TaskContainer parent = fromTaskToParent.get(toBeRemoved);
            if (parent != null) {
                parent.remove(toBeRemoved);
            }
            fromTaskToParent.remove(toBeRemoved);
            topLevel.remove(toBeRemoved);
        }

        @Override
        public T findAssociatedDomainObject(Task task)
                throws IllegalArgumentException {
            if (task == null)
                throw new IllegalArgumentException("taskBean is null");
            if (!fromTaskToDomain.containsKey(task))
                throw new IllegalArgumentException();
            return fromTaskToDomain.get(task);
        }

        @Override
        public Position findPositionFor(Task task) {
            List<TaskContainer> ancestors = ancestorsOf(task);
            if (ancestors.isEmpty()) {
                return Position.createAtTopPosition(topLevel.indexOf(task));
            }
            TaskContainer parent = ancestors.get(0);
            return Position.createPosition(ancestors, parent.getTasks()
                    .indexOf(task));
        }

        @Override
        public Position findPositionFor(T domainObject) {
            return findPositionFor(findAssociatedBean(domainObject));
        }

        private List<TaskContainer> ancestorsOf(Task task) {
            ArrayList<TaskContainer> result = new ArrayList<TaskContainer>();
            TaskContainer taskContainer = fromTaskToParent.get(task);
            while (taskContainer != null) {
                result.add(taskContainer);
                taskContainer = fromTaskToParent.get(taskContainer);
            }
            return result;
        }

    }

    private final Planner planner;
    private final IAdapterToTaskFundamentalProperties<T> adapter;
    private final IStructureNavigator<T> navigator;
    private final OneToOneMapper<T> mapper = new OneToOneMapper<T>();
    private final GanttDiagramGraph diagramGraph;

    public FunctionalityExposedForExtensions(Planner planner,
            IAdapterToTaskFundamentalProperties<T> adapter,
            IStructureNavigator<T> navigator, GanttDiagramGraph diagramGraph) {
        this.planner = planner;
        this.adapter = adapter;
        this.navigator = navigator;
        this.diagramGraph = diagramGraph;
    }

    /**
     * @param topInsertionPosition
     *            the position in which to register the task at top level. It
     *            can be <code>null</code>
     * @param accumulatedDependencies
     * @param data
     * @param parent
     * @return
     */
    private Task extractTask(Integer topInsertionPosition,
            List<DomainDependency<T>> accumulatedDependencies,
            T data, TaskContainer parent) {
        ITaskFundamentalProperties adapted = adapter.adapt(data);
        accumulatedDependencies.addAll(adapter.getOutcomingDependencies(data));
        accumulatedDependencies.addAll(adapter.getIncomingDependencies(data));
        final Task result;
        if (navigator.isLeaf(data)) {
            result = new TaskLeaf(adapted);
        } else {
            TaskContainer container = new TaskContainer(adapted);
            for (T child : navigator.getChildren(data)) {
                container.add(extractTask(null, accumulatedDependencies, child,
                        container));
            }
            result = container;
        }
        mapper.register(topInsertionPosition, result, data, parent);
        return result;
    }

    public void add(Position position, Collection<? extends T> domainObjects) {
        List<DomainDependency<T>> totalDependencies = new ArrayList<DomainDependency<T>>();
        List<Task> tasksCreated = new ArrayList<Task>();
        for (T object : domainObjects) {
            Task task = extractTask(position.getInsertionPosition(),
                    totalDependencies, object, position.getParent());
            tasksCreated.add(task);
        }
        if (position.isAppendToTop() || position.isAtTop()) {
            this.diagramGraph.addTopLevel(tasksCreated);
        } else {
            this.diagramGraph.addTasks(tasksCreated);
            TaskContainer parent = position.getParent();
            parent.addAll(position.getInsertionPosition(), tasksCreated);
            this.diagramGraph.childrenAddedTo(parent);
        }
        for (Dependency dependency : DomainDependency.toDependencies(mapper,
                totalDependencies)) {
            this.diagramGraph.add(dependency);
        }
        this.diagramGraph.enforceAllRestrictions();
        this.planner.addTasks(position, tasksCreated);
    }

    public void add(Collection<? extends T> domainObjects) {
        add(Position.createAppendToTopPosition(), domainObjects);
    }

    @Override
    public void add(T domainObject) {
        add(Position.createAppendToTopPosition(), domainObject);
    }

    @Override
    public void add(Position position, T domainObject) {
        LinkedList<T> list = new LinkedList<T>();
        list.add(domainObject);
        add(position, list);
    }

    IDomainAndBeansMapper<T> getMapper() {
        return mapper;
    }

    @Override
    public void reload(PlannerConfiguration<?> configuration) {
        planner.setConfiguration(configuration);
    }

    @Override
    public Position remove(T domainObject) {
        Task task = mapper.findAssociatedBean(domainObject);
        Position position = mapper.findPositionFor(task);
        diagramGraph.remove(task);
        task.removed();
        planner.removeTask(task);
        mapper.remove(domainObject);
        return position;
    }

    @Override
    public Component getRelativeTo() {
        return planner;
    }

    @Override
    public void replace(T oldDomainObject, T newDomainObject) {
        Position position = remove(oldDomainObject);
        add(position, newDomainObject);
    }

    public GanttDiagramGraph getDiagramGraph() {
        return diagramGraph;
    }

    private DomainDependency<T> toDomainDependency(Dependency bean) {
        T source = mapper.findAssociatedDomainObject(bean.getSource());
        T destination = mapper
                .findAssociatedDomainObject(bean.getDestination());
        DomainDependency<T> dep = DomainDependency.createDependency(source,
                destination, bean.getType());
        return dep;
    }

    public void addDependency(DependencyComponent dependencyComponent) {
        Dependency dependency = dependencyComponent.getDependency();
        if (!canAddDependency(dependency))
            return;
        getDependencyList().addDependencyComponent(dependencyComponent);
        diagramGraph.add(dependency);
        adapter.addDependency(toDomainDependency(dependency));
    }

    private boolean canAddDependency(Dependency dependency) {
        return adapter.canAddDependency(toDomainDependency(dependency));
    }

    private DependencyList getDependencyList() {
        return planner.getDependencyList();
    }

    public void removeDependency(Dependency dependency) {
        adapter.removeDependency(toDomainDependency(dependency));
        diagramGraph.remove(dependency);
    }

}
