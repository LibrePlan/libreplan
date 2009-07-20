package org.navalplanner.web.planner;

import java.util.List;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.services.IOrderService;
import org.navalplanner.business.planner.entities.TaskElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public abstract class OrderPlanningModel implements IOrderPlanningModel {

    @Autowired
    private IOrderService orderService;

    private List<TaskElement> state;

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
        PlannerConfiguration<TaskElement> configuration = createConfiguration(orderReloaded);
        ISaveCommand saveCommand = getSaveCommand();
        saveCommand.setState(state);
        configuration.addGlobalCommand(saveCommand);
        onTransaction.use(configuration);
    }

    private PlannerConfiguration<TaskElement> createConfiguration(
            Order orderReloaded) {
        ITaskElementAdapter taskElementAdapter = getTaskElementAdapter();
        taskElementAdapter.setOrder(orderReloaded);
        state = orderReloaded.getAssociatedTasks();
        forceLoadOfDependenciesCollections(state);
        return new PlannerConfiguration<TaskElement>(taskElementAdapter,
                new TaskElementNavigator(), state);
    }

    private void forceLoadOfDependenciesCollections(List<TaskElement> elements) {
        for (TaskElement task : elements) {
            forceLoadOfDepedenciesCollections(task);
            if (!task.isLeaf()) {
                forceLoadOfDependenciesCollections(task.getChildren());
            }
        }
    }

    private void forceLoadOfDepedenciesCollections(TaskElement task) {
        task.getDependenciesWithThisOrigin().size();
        task.getDependenciesWithThisDestination().size();
    }

    // spring method injection
    protected abstract ITaskElementAdapter getTaskElementAdapter();

    protected abstract ISaveCommand getSaveCommand();

    private Order reload(Order order) {
        try {
            return orderService.find(order.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
