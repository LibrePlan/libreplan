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

package org.navalplanner.web.scenarios;

import static org.navalplanner.web.I18nHelper._;

import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.scenarios.bootstrap.PredefinedScenarios;
import org.navalplanner.business.scenarios.daos.IOrderVersionDAO;
import org.navalplanner.business.scenarios.daos.IScenarioDAO;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations to transfer orders between scenarios.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Qualifier("main")
@OnConcurrentModification(goToPage = "/scenarios/transferOrders.zul")
public class TransferOrdersModel implements ITransferOrdersModel {

    private Scenario sourceScenario;

    private Scenario destinationScenario;

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private IOrderVersionDAO orderVersionDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Scenario> getScenarios() {
        return scenarioDAO.list(Scenario.class);
    }

    @Override
    public Scenario getSourceScenario() {
        if (sourceScenario == null) {
            sourceScenario = PredefinedScenarios.MASTER.getScenario();
        }
        return sourceScenario;
    }

    @Override
    public void setSourceScenario(Scenario scenario) {
        sourceScenario = scenario;
    }

    @Override
    public Set<Order> getSourceScenarioOrders() {
        return sourceScenario.getOrders().keySet();
    }

    @Override
    public Scenario getDestinationScenario() {
        if (destinationScenario == null) {
            destinationScenario = PredefinedScenarios.MASTER.getScenario();
        }
        return destinationScenario;
    }

    @Override
    public void setDestinationScenario(Scenario scenario) {
        destinationScenario = scenario;
    }

    @Override
    public Set<Order> getDestinationScenarioOrders() {
        return destinationScenario.getOrders().keySet();
    }

    @Override
    public String getVersion(Order order, Scenario scenario) {
        OrderVersion orderVersion = scenario.getOrderVersion(order);
        return orderVersion.getId().toString();
    }

    @Override
    @Transactional
    public void transfer(Order order) throws ValidationException {
        Scenario sourceScenario = getSourceScenario();
        Scenario destinationScenario = getDestinationScenario();

        if (sourceScenario == null) {
            throw new ValidationException(
                    _("You should select a source scenario"));
        }
        if (destinationScenario == null) {
            throw new ValidationException(
                    _("You should select a destination scenario"));
        }

        if (sourceScenario.getId().equals(destinationScenario.getId())) {
            throw new ValidationException(
                    _("Source and destination scenarios should be different"));
        }

        OrderVersion sourceOrderVersion = sourceScenario.getOrderVersion(order);
        if (sourceOrderVersion == null) {
            throw new RuntimeException(
                    "OrderVersion must not be null for source scenario");
        }

        OrderVersion destinationOrderVersion = destinationScenario
                .getOrderVersion(order);
        if ((destinationOrderVersion != null)
                && (sourceOrderVersion.getId().equals(destinationOrderVersion
                        .getId()))) {
            throw new ValidationException(
                    _("Order version is the same in source and destination scenarios"));
        }

        order.useSchedulingDataFor(sourceOrderVersion);

        if ((destinationOrderVersion != null)
                && (destinationOrderVersion.getOwnerScenario().getId()
                        .equals(destinationScenario.getId()))) {
            order.writeSchedulingDataChangesTo(destinationScenario,
                    destinationOrderVersion);
            orderVersionDAO.save(destinationOrderVersion);
        } else {
            OrderVersion newOrderVersion = OrderVersion
                    .createInitialVersion(destinationScenario);
            destinationScenario.setOrderVersion(order, newOrderVersion);
            order.writeSchedulingDataChangesTo(destinationScenario,
                    newOrderVersion);
            scenarioDAO.save(destinationScenario);
            scenarioDAO.updateDerivedScenariosWithNewVersion(
                    destinationOrderVersion, order, destinationScenario,
                    newOrderVersion);
        }
    }

}