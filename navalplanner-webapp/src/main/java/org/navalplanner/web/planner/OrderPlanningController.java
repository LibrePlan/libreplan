package org.navalplanner.web.planner;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.web.common.entrypoints.IURLHandlerRegistry;
import org.navalplanner.web.common.entrypoints.URLHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.util.ITaskFundamentalProperties;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderPlanningController implements
        IOrderPlanningControllerEntryPoints {

    @Autowired
    private IURLHandlerRegistry urlHandlerRegistry;

    private Planner planner;

    public OrderPlanningController() {
    }

    @Override
    public void showSchedule(Order order) {
        PlannerConfiguration<ITaskFundamentalProperties> configuration = new DataForPlanner()
                .getMediumLoad();
        // TODO just for trying passing medium load
        planner.setConfiguration(configuration);
    }

    public void registerPlanner(Planner planner) {
        this.planner = planner;
        final URLHandler<IOrderPlanningControllerEntryPoints> handler = urlHandlerRegistry
                .getRedirectorFor(IOrderPlanningControllerEntryPoints.class);
        handler.registerListener(this, planner.getPage());
    }

}
