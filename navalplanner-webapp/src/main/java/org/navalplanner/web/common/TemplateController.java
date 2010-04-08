/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.common;

import java.util.Collections;
import java.util.List;

import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.navalplanner.web.security.SecurityUtils;
import org.navalplanner.web.users.services.CustomUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.context.SecurityContextHolder;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Window;

/**
 * Controller to manage UI operations from main template.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TemplateController extends GenericForwardComposer {

    @Autowired
    private ITemplateModel templateModel;

    @Autowired
    private IScenarioManager scenarioManager;

    private Window window;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        window = (Window) comp.getFellow("changeScenarioWindow");
    }

    public Scenario getScenario() {
        return scenarioManager.getCurrent();
    }

    public void changeScenario() throws SuspendNotAllowedException,
            InterruptedException {
        window.doModal();
    }

    public List<Scenario> getScenarios() {
        if (templateModel == null) {
            return Collections.emptyList();
        }
        return templateModel.getScenarios();
    }

    public void accept() {
        BandboxSearch scenarioBandboxSearch = (BandboxSearch) window
                .getFellow("scenarioBandboxSearch");
        Scenario scenario = (Scenario) scenarioBandboxSearch
                .getSelectedElement();

        templateModel.setScenario(SecurityUtils.getSessionUserLoginName(),
                scenario);

        CustomUser customUser = (CustomUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        customUser.setScenario(scenario);

        window.setVisible(false);
        Executions.sendRedirect("/");
    }

    public void cancel() {
        window.setVisible(false);
    }

}
