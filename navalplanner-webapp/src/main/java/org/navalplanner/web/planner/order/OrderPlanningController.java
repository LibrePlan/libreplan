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

package org.navalplanner.web.planner.order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.web.common.ViewSwitcher;
import org.navalplanner.web.planner.allocation.ResourceAllocationController;
import org.navalplanner.web.planner.calendar.CalendarAllocationController;
import org.navalplanner.web.planner.taskedition.TaskPropertiesController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.extensions.ICommand;
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

    public ResourceAllocationController getResourceAllocationController() {
        return resourceAllocationController;
    }


    @Autowired
    private TaskPropertiesController taskPropertiesController;

    public TaskPropertiesController getTaskPropertiesController() {
        return taskPropertiesController;
    }

    @Autowired
    private IOrderPlanningModel model;

    private Planner planner;

    @Autowired
    private CalendarAllocationController calendarAllocationController;

    private Order order;

    private List<ICommand<TaskElement>> additional = new ArrayList<ICommand<TaskElement>>();

    public OrderPlanningController() {
        getScriptsRegister().register(ScriptsRequiredByResourceLoadPanel.class);
        ResourceAllocationController.registerNeededScripts();
    }

    private IScriptsRegister getScriptsRegister() {
        return OnZKDesktopRegistry.getLocatorFor(IScriptsRegister.class)
                .retrieve();
    }

    public void setOrder(Order order,
            ICommand<TaskElement>... additionalCommands) {
        Validate.notNull(additionalCommands);
        Validate.noNullElements(additionalCommands);
        this.order = order;
        this.additional = Arrays.asList(additionalCommands);
        if (planner != null) {
            updateConfiguration();
        }
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
                resourceAllocationController, taskPropertiesController,
                calendarAllocationController, additional);
    }

}
