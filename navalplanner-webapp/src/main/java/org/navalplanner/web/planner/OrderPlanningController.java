package org.navalplanner.web.planner;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.web.common.entrypoints.IURLHandlerRegistry;
import org.navalplanner.web.common.entrypoints.URLHandler;
import org.navalplanner.web.planner.IOrderPlanningModel.ConfigurationOnTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.TaskEditFormComposer;
import org.zkoss.ganttz.adapters.PlannerConfiguration;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderPlanningController implements
        IOrderPlanningControllerEntryPoints {

    @Autowired
    private ResourceAllocationController resourceAllocationController;

    public ResourceAllocationController getResourceAllocationController() {
        return resourceAllocationController;
    }

    private TaskEditFormComposer taskEditFormComposer = new TaskEditFormComposer();

    public TaskEditFormComposer getTaskEditFormComposer() {
        return taskEditFormComposer;
    }

    @Autowired
    private IURLHandlerRegistry urlHandlerRegistry;

    @Autowired
    private IOrderPlanningModel model;

    private Planner planner;

    public OrderPlanningController() {
    }

    @Override
    public void showSchedule(Order order) {
        model.createConfiguration(order, resourceAllocationController,
                taskEditFormComposer,
                new ConfigurationOnTransaction() {

            @Override
            public void use(PlannerConfiguration<TaskElement> configuration) {
                planner.setConfiguration(configuration);
            }
        });
    }

    public void registerPlanner(Planner planner) {
        this.planner = planner;
        final URLHandler<IOrderPlanningControllerEntryPoints> handler = urlHandlerRegistry
                .getRedirectorFor(IOrderPlanningControllerEntryPoints.class);
        handler.registerListener(this, planner.getPage());
    }

}
