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
import org.zkoss.ganttz.data.Dependency;
import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.data.ITaskFundamentalProperties;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.data.TaskContainer;
import org.zkoss.ganttz.data.TaskLeaf;
import org.zkoss.ganttz.extensions.IContext;

public class FunctionalityExposedForExtensions<T> implements IContext<T> {

    private static class OneToOneMapper<T> implements IDomainAndBeansMapper<T> {
        private Map<T, Task> fromDomainToTask = new HashMap<T, Task>();

        private Map<Task, T> fromTaskToDomain = new HashMap<Task, T>();

        @Override
        public Task findAssociatedBean(T domainObject)
                throws IllegalArgumentException {
            if (domainObject == null)
                throw new IllegalArgumentException("domainObject is null");
            if (!fromDomainToTask.containsKey(domainObject))
                throw new IllegalArgumentException("not found " + domainObject);
            return fromDomainToTask.get(domainObject);
        }

        void register(Task task, T domainObject) {
            fromDomainToTask.put(domainObject, task);
            fromTaskToDomain.put(task, domainObject);
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

    private Task extractTask(
            List<DomainDependency<T>> accumulatedDependencies, T data) {
        ITaskFundamentalProperties adapted = adapter.adapt(data);
        accumulatedDependencies
                .addAll(adapter.getDependenciesOriginating(data));
        final Task result;
        if (navigator.isLeaf(data)) {
            result = new TaskLeaf(adapted);
        } else {
            TaskContainer container = new TaskContainer(adapted);
            for (T child : navigator.getChildren(data)) {
                container.add(extractTask(accumulatedDependencies, child));
            }
            result = container;
        }
        mapper.register(result, data);
        return result;
    }

    public void add(Collection<? extends T> domainObjects) {
        List<DomainDependency<T>> totalDependencies = new ArrayList<DomainDependency<T>>();
        for (T object : domainObjects) {
            Task task = extractTask(totalDependencies, object);
            diagramGraph.addTopLevel(task);
            this.planner.addTask(task);
        }
        for (Dependency dependency : DomainDependency
                .toDependencies(mapper, totalDependencies)) {
            this.diagramGraph.add(dependency);
        }
        this.diagramGraph.applyAllRestrictions();
    }

    @Override
    public void add(T domainObject) {
        LinkedList<T> list = new LinkedList<T>();
        list.add(domainObject);
        add(list);
    }

    IDomainAndBeansMapper<T> getMapper() {
        return mapper;
    }

}
