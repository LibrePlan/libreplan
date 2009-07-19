package org.navalplanner.web.planner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.services.IOrderService;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.Dependency.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.adapters.DomainDependency;
import org.zkoss.ganttz.adapters.IAdapterToTaskFundamentalProperties;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.util.DefaultFundamentalProperties;
import org.zkoss.ganttz.util.DependencyType;
import org.zkoss.ganttz.util.ITaskFundamentalProperties;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class OrderPlanningModel implements IOrderPlanningModel {

    @Autowired
    private IOrderService orderService;

    private final class TaskElementToFundamentalProperties implements
            IAdapterToTaskFundamentalProperties<TaskElement> {

        private final Order order;

        private List<Dependency> dependenciesCreated = new ArrayList<Dependency>();

        private TaskElementToFundamentalProperties(Order order) {
            this.order = order;
        }

        @Override
        public ITaskFundamentalProperties adapt(final TaskElement taskElement) {
            // FIXME We'll need a custom ITaskFundamentalProperties
            // implementation to edit the taskElement

            DefaultFundamentalProperties result = new DefaultFundamentalProperties();
            result.setName(taskElement.getOrderElement().getName());
            result
                    .setBeginDate(taskElement.getStartDate() != null ? taskElement
                            .getStartDate()
                            : order.getInitDate());
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
            Dependency domainDependency = Dependency.createDependency(source,
                    destination, domainType);

        }

        @Override
        public boolean canAddDependency(DomainDependency<TaskElement> dependency) {
            return true;
        }
    }

    private final class TaskElementNavigator implements
            IStructureNavigator<TaskElement> {
        @Override
        public List<TaskElement> getChildren(TaskElement object) {
            return object.getChildren();
        }

        @Override
        public boolean isLeaf(TaskElement object) {
            return object.isLeaf();
        }
    }

    @Transactional(readOnly = true)
    public void createConfiguration(Order order,
            ConfigurationOnTransaction onTransaction) {
        Order orderReloaded = reload(order);
        if (!orderReloaded.isSomeTaskElementScheduled())
            throw new IllegalArgumentException("the order " + order
                    + " must be scheduled");
        onTransaction.use(createConfiguration(orderReloaded));
    }

    private PlannerConfiguration<TaskElement> createConfiguration(
            Order orderReloaded) {
        return new PlannerConfiguration<TaskElement>(
                new TaskElementToFundamentalProperties(orderReloaded),
                new TaskElementNavigator(), orderReloaded.getAssociatedTasks());
    }

    private Order reload(Order order) {
        try {
            return orderService.find(order.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
