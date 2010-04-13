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

import java.util.List;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.scenarios.daos.IScenarioDAO;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.web.users.services.CustomUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model to manage UI operations from main template.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TemplateModel implements ITemplateModel {

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private IUserDAO userDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Scenario> getScenarios() {
        return scenarioDAO.getAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Scenario getScenarioByName(String name) {
        try {
            return scenarioDAO.findByName(name);
        } catch (InstanceNotFoundException e) {
            return null;
        }
    }

    @Override
    @Transactional
    public void setScenario(String loginName, Scenario scenario) {
        try {
            User user = userDAO.findByLoginName(loginName);
            user.setLastConnectedScenario(scenario);
            userDAO.save(user);

            CustomUser customUser = (CustomUser) SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();
            customUser.setScenario(scenario);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
