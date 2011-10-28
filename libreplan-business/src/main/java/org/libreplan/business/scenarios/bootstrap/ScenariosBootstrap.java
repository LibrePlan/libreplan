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

package org.libreplan.business.scenarios.bootstrap;

import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.scenarios.daos.IScenarioDAO;
import org.libreplan.business.scenarios.entities.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the default {@link Scenario}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope("singleton")
public class ScenariosBootstrap implements IScenariosBootstrap {

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private IOrderDAO orderDAO;

    private Scenario mainScenario;

    @Override
    @Transactional
    public void loadRequiredData() {
        for (PredefinedScenarios predefinedScenario : PredefinedScenarios
                .values()) {
            if (!scenarioDAO.existsByNameAnotherTransaction(predefinedScenario
                    .getName())) {
                Scenario scenario = createAtDB(predefinedScenario);
                if (predefinedScenario == PredefinedScenarios.MASTER) {
                    mainScenario = scenario;
                }
            }
        }
        if (mainScenario == null) {
            mainScenario = PredefinedScenarios.MASTER.getScenario();
        }
    }

    private Scenario createAtDB(PredefinedScenarios predefinedScenario) {
        Scenario scenario = predefinedScenario.createScenario();
        for (Order each : orderDAO.getOrders()) {
            scenario.addOrder(each);
        }
        scenarioDAO.save(scenario);
        scenario.dontPoseAsTransientObjectAnymore();
        return scenario;
    }

    @Override
    public Scenario getMain() {
        if (mainScenario == null) {
            throw new IllegalStateException(
                    "loadRequiredData should have been called on "
                            + ScenariosBootstrap.class.getName());
        }
        return mainScenario;
    }
}
