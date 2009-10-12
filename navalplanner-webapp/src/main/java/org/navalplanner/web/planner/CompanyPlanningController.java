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

import org.navalplanner.web.common.ViewSwitcher;
import org.navalplanner.web.common.entrypoints.IURLHandlerRegistry;
import org.navalplanner.web.common.entrypoints.URLHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.resourceload.ScriptsRequiredByResourceLoadPanel;
import org.zkoss.ganttz.util.OnZKDesktopRegistry;
import org.zkoss.ganttz.util.script.IScriptsRegister;

/**
 * Controller for company planning view. Representation of company orders in the
 * planner.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CompanyPlanningController implements
        ICompanyPlanningControllerEntryPoints {

    @Autowired
    private ViewSwitcher viewSwitcher;

    @Autowired
    private EditTaskController editTaskController;

    public EditTaskController getEditTaskController() {
        return editTaskController;
    }

    @Autowired
    private IURLHandlerRegistry urlHandlerRegistry;

    @Autowired
    private ICompanyPlanningModel model;

    private Planner planner;

    public CompanyPlanningController() {
        getScriptsRegister().register(ScriptsRequiredByResourceLoadPanel.class);
    }

    private IScriptsRegister getScriptsRegister() {
        return OnZKDesktopRegistry.getLocatorFor(IScriptsRegister.class)
                .retrieve();
    }

    @Override
    public void showSchedule() {
        model.setConfigurationToPlanner(planner, viewSwitcher,
                editTaskController);
    }

    public void registerPlanner(Planner planner) {
        this.planner = planner;
        final URLHandler<ICompanyPlanningControllerEntryPoints> handler = urlHandlerRegistry
                .getRedirectorFor(ICompanyPlanningControllerEntryPoints.class);
        handler.registerListener(this, planner.getPage());
    }

    public ViewSwitcher getViewSwitcher() {
        return viewSwitcher;
    }

}
