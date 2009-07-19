package org.navalplanner.web.planner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.Dependency.Type;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.adapters.DomainDependency;
import org.zkoss.ganttz.util.DefaultFundamentalProperties;
import org.zkoss.ganttz.util.DependencyType;
import org.zkoss.ganttz.util.ITaskFundamentalProperties;

/**
 * Responsible of adaptating a {@link TaskElement} into a
 * {@link ITaskFundamentalProperties} <br />
 * This class is managed by spring so version checking and reatachments are
 * allowed, but they're not used by now.
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class TaskElementAdapter implements ITaskElementAdapter {

    private Order order;

    @Override
    public void setOrder(Order order) {
        this.order = order;
    }

    public TaskElementAdapter() {
    }

    @Override
    public ITaskFundamentalProperties adapt(final TaskElement taskElement) {
        // FIXME We'll need a custom ITaskFundamentalProperties
        // implementation to edit the taskElement

        DefaultFundamentalProperties result = new DefaultFundamentalProperties();
        result.setName(taskElement.getOrderElement().getName());
        result.setBeginDate(taskElement.getStartDate() != null ? taskElement
                .getStartDate() : order.getInitDate());
        OrderElement orderElement = taskElement.getOrderElement();
        Integer workHours = orderElement.getWorkHours();
        result.setLengthMilliseconds(workHours * 3600l * 1000);
        result.setNotes(null);
        return result;
    }

    @Override
    public List<DomainDependency<TaskElement>> getDependenciesOriginating(
            TaskElement taskElement) {
        Set<Dependency> dependenciesWithThisOrigin = taskElement
                .getDependenciesWithThisOrigin();
        List<DomainDependency<TaskElement>> result = new ArrayList<DomainDependency<TaskElement>>();
        for (Dependency dependency : dependenciesWithThisOrigin) {
            result.add(DomainDependency.createDependency(taskElement,
                    dependency.getDestination(), toGanntType(dependency
                            .getType())));
        }
        return result;
    }

    private DependencyType toGanntType(Type type) {
        switch (type) {
        case END_START:
            return DependencyType.END_START;
        case START_START:
            return DependencyType.START_START;
        case END_END:
            return DependencyType.END_END;
        case START_END:
        default:
            throw new RuntimeException(type + " not supported yet");
        }
    }

    private Type toDomainType(DependencyType type) {
        switch (type) {
        case END_START:
            return Type.END_START;
        case START_START:
            return Type.START_START;
        case END_END:
            return Type.END_END;
        default:
            throw new RuntimeException(type + " not supported yet");
        }
    }

    @Override
    public void addDependency(DomainDependency<TaskElement> dependency) {
        TaskElement source = dependency.getSource();
        TaskElement destination = dependency.getDestination();
        Type domainType = toDomainType(dependency.getType());
        Dependency.createDependency(source, destination, domainType);
    }

    @Override
    public boolean canAddDependency(DomainDependency<TaskElement> dependency) {
        return true;
    }
}