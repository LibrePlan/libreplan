package org.navalplanner.web.planner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.TaskElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;

import static org.navalplanner.web.I18nHelper._;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public abstract class OrderPlanningModel implements IOrderPlanningModel {

    @Autowired
    private IOrderDAO orderDAO;

    private PlanningState planningState;

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

    @Override
    @Transactional(readOnly = true)
    public void createConfiguration(Order order,
            ResourceAllocationController resourceAllocationController,
            EditTaskController editTaskController,
            SplittingController splittingController,
            IConfigurationOnTransaction onTransaction) {
        Order orderReloaded = reload(order);
        if (!orderReloaded.isSomeTaskElementScheduled())
            throw new IllegalArgumentException(_("The order {0} must be scheduled", order));
        PlannerConfiguration<TaskElement> configuration = createConfiguration(orderReloaded);

        ISaveCommand saveCommand = getSaveCommand();
        saveCommand.setState(planningState);
        configuration.addGlobalCommand(saveCommand);

        IResourceAllocationCommand resourceAllocationCommand = getResourceAllocationCommand();
        resourceAllocationCommand
                .setResourceAllocationController(resourceAllocationController);
        configuration.addCommandOnTask(resourceAllocationCommand);

        ISplitTaskCommand splitCommand = getSplitCommand();
        splitCommand.setState(planningState);
        splitCommand.setSplitWindowController(splittingController);
        configuration.addCommandOnTask(splitCommand);

        IMergeTaskCommand mergeCommand = getMergeTaskCommand();
        mergeCommand.setState(planningState);
        configuration.addCommandOnTask(mergeCommand);

        IEditTaskCommand editTaskCommand = getEditTaskCommand();
        editTaskCommand.setEditTaskController(editTaskController);
        configuration.setEditTaskCommand(editTaskCommand);

        onTransaction.use(configuration);
    }

    private PlannerConfiguration<TaskElement> createConfiguration(
            Order orderReloaded) {
        ITaskElementAdapter taskElementAdapter = getTaskElementAdapter();
        taskElementAdapter.setOrder(orderReloaded);
        planningState = new PlanningState(retainOnlyTopLevel(orderReloaded
                .getAssociatedTasks()));
        forceLoadOfDependenciesCollections(planningState.getInitial());
        forceLoadOfWorkingHours(planningState.getInitial());
        return new PlannerConfiguration<TaskElement>(taskElementAdapter,
                new TaskElementNavigator(), planningState.getInitial());
    }

    private Collection<? extends TaskElement> retainOnlyTopLevel(
            List<TaskElement> associatedTasks) {
        Set<TaskElement> descendantsFromOther = new HashSet<TaskElement>();
        for (TaskElement taskElement : associatedTasks) {
            descandants(descendantsFromOther, taskElement);
        }
        ArrayList<TaskElement> result = new ArrayList<TaskElement>();
        for (TaskElement taskElement : associatedTasks) {
            if (!descendantsFromOther.contains(taskElement)) {
                result.add(taskElement);
            }
        }
        return result;
    }

    private void descandants(
            Set<TaskElement> accumulated,
            TaskElement taskElement) {
        if (taskElement.isLeaf()) {
            return;
        }
        for (TaskElement t : taskElement.getChildren()) {
            accumulated.add(t);
            descandants(accumulated, t);
        }
    }

    private void forceLoadOfWorkingHours(List<TaskElement> initial) {
        for (TaskElement taskElement : initial) {
            taskElement.getOrderElement().getWorkHours();
            if (!taskElement.isLeaf()) {
                forceLoadOfWorkingHours(taskElement.getChildren());
            }
        }
    }

    private void forceLoadOfDependenciesCollections(
            Collection<? extends TaskElement> elements) {
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


    protected abstract IResourceAllocationCommand getResourceAllocationCommand();

    protected abstract ISplitTaskCommand getSplitCommand();

    protected abstract IMergeTaskCommand getMergeTaskCommand();

    protected abstract IEditTaskCommand getEditTaskCommand();

    private Order reload(Order order) {
        try {
            return orderDAO.find(order.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
