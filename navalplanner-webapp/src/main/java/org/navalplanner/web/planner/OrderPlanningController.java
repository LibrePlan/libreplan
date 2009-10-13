/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.planner;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.web.common.ViewSwitcher;
import org.navalplanner.web.planner.allocation.ResourceAllocationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.resourceload.ScriptsRequiredByResourceLoadPanel;
import org.zkoss.ganttz.util.OnZKDesktopRegistry;
import org.zkoss.ganttz.util.script.IScriptsRegister;
import org.zkoss.zk.ui.util.Composer;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderPlanningController implements Composer {

    @Autowired
    private ViewSwitcher viewSwitcher;

    @Autowired
    private ResourceAllocationController resourceAllocationController;

    @Autowired
    private SplittingController splittingController;

    public ResourceAllocationController getResourceAllocationController() {
        return resourceAllocationController;
    }


    @Autowired
    private EditTaskController editTaskController;

    public EditTaskController getEditTaskController() {
        return editTaskController;
    }

    @Autowired
    private IOrderPlanningModel model;

    private Planner planner;

    @Autowired
    private CalendarAllocationController calendarAllocationController;

    private Order order;

    public OrderPlanningController() {
        getScriptsRegister().register(ScriptsRequiredByResourceLoadPanel.class);
        ResourceAllocationController.registerNeededScripts();
    }

    private IScriptsRegister getScriptsRegister() {
        return OnZKDesktopRegistry.getLocatorFor(IScriptsRegister.class)
                .retrieve();
    }

    public void setOrder(Order order) {
        this.order = order;
        if (planner != null) {
            updateConfiguration();
        }
    }

    public SplittingController getSplittingController() {
        return splittingController;
    }

    public CalendarAllocationController getCalendarAllocationController() {
        return calendarAllocationController;
    }

    public ViewSwitcher getViewSwitcher() {
        return viewSwitcher;
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        if (order == null) {
            throw new IllegalStateException("an order should have been set");
        }
        this.planner = (Planner) comp;
        updateConfiguration();
    }

    private void updateConfiguration() {
        model.setConfigurationToPlanner(planner, order, viewSwitcher,
                resourceAllocationController, editTaskController,
                splittingController, calendarAllocationController);
    }

}
