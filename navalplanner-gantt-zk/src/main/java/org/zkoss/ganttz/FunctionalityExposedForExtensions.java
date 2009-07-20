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
import org.zkoss.ganttz.data.DependencyBean;
import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.data.ITaskFundamentalProperties;
import org.zkoss.ganttz.data.TaskBean;
import org.zkoss.ganttz.data.TaskContainerBean;
import org.zkoss.ganttz.data.TaskLeafBean;
import org.zkoss.ganttz.extensions.IContext;

public class FunctionalityExposedForExtensions<T> implements IContext<T> {

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

    private TaskBean extractTaskBean(
            List<DomainDependency<T>> accumulatedDependencies, T data) {
        ITaskFundamentalProperties adapted = adapter.adapt(data);
        accumulatedDependencies
                .addAll(adapter.getDependenciesOriginating(data));
        final TaskBean result;
        if (navigator.isLeaf(data)) {
            result = new TaskLeafBean(adapted);
        } else {
            TaskContainerBean container = new TaskContainerBean(adapted);
            for (T child : navigator.getChildren(data)) {
                container.add(extractTaskBean(accumulatedDependencies, child));
            }
            result = container;
        }
        mapper.register(result, data);
        return result;
    }

    public void add(Collection<? extends T> domainObjects) {
        List<DomainDependency<T>> totalDependencies = new ArrayList<DomainDependency<T>>();
        for (T object : domainObjects) {
            TaskBean taskBean = extractTaskBean(totalDependencies, object);
            diagramGraph.addTopLevel(taskBean);
            this.planner.addTask(taskBean);
        }
        for (DependencyBean dependencyBean : DomainDependency
                .toDependencyBeans(mapper, totalDependencies)) {
            this.diagramGraph.add(dependencyBean);
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
