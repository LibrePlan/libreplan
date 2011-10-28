/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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
package org.libreplan.web.scenarios;

import java.util.Set;

import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.bootstrap.IScenariosBootstrap;
import org.libreplan.business.scenarios.daos.IScenarioDAO;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.web.security.SecurityUtils;
import org.libreplan.web.users.services.CustomUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class CurrentUserScenarioAwareManager implements IScenarioManager {

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Override
    @Transactional(readOnly = true)
    public Scenario getCurrent() {
        Scenario scenario = scenarioAssociatedToLoggedUser();
        return reload(scenario);
    }

    private Scenario scenarioAssociatedToLoggedUser() {
        CustomUser loggedUser = SecurityUtils.getLoggedUser();
        if (loggedUser == null) {
            return scenariosBootstrap.getMain();
        }
        return loggedUser.getScenario();
    }

    private Scenario reload(Scenario scenario) {
        if (scenario.getId() == null) {
            return scenario;
        }
        return forceLoad(scenarioDAO.findExistingEntity(scenario.getId()));
    }

    private Scenario forceLoad(Scenario scenario) {
        scenarioDAO.reattach(scenario);
        Set<Order> orders = scenario.getOrders().keySet();
        for (Order order : orders) {
            orderDAO.reattach(order);
            order.getName();
        }
        return scenario;
    }

}
