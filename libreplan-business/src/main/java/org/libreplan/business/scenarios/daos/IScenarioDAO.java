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

package org.libreplan.business.scenarios.daos;

import java.util.List;

import org.libreplan.business.common.daos.IGenericDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.scenarios.entities.Scenario;

/**
 * Contract for {@link ScenarioDAO}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IScenarioDAO extends IGenericDAO<Scenario, Long> {

    Scenario findByName(String name) throws InstanceNotFoundException;

    boolean existsByName(String name);

    boolean existsByNameAnotherTransaction(String name);

    List<Scenario> getAll();

    boolean thereIsOtherWithSameName(Scenario scenario);

    List<Scenario> findByPredecessor(Scenario scenario);

    List<Scenario> getDerivedScenarios(Scenario scenario);

    void updateDerivedScenariosWithNewVersion(
            OrderVersion previousOrderVersion, Order order,
            Scenario currentScenario, OrderVersion newOrderVersion);

}
