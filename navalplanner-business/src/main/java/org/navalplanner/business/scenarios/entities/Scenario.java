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

package org.navalplanner.business.scenarios.entities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.scenarios.daos.IScenarioDAO;

/**
 * Represents a scenario in the application.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class Scenario extends BaseEntity {

    private String name;

    private String description;

    /**
     * For each order tracked by this Scenario exists an OrderVersion that will
     * have specific data for that order
     */
    private Map<Order, OrderVersion> orders = new HashMap<Order, OrderVersion>();

    public static Scenario create(String name) {
        return create(new Scenario(name));
    }

    // Default constructor, needed by Hibernate
    protected Scenario() {
    }

    public Map<Order, OrderVersion> getOrders() {
        return Collections.unmodifiableMap(orders);
    }

    private Scenario(String name) {
        this.name = name;
    }

    public void addOrder(Order order) {
        if (!orders.values().contains(order)) {
            orders.put(order, OrderVersion.createInitialVersion());
        }
    }

    public Set<Order> getTrackedOrders() {
        return Collections.unmodifiableSet(orders.keySet());
    }

    @NotEmpty(message = "name not specified")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @AssertTrue(message = "name is already used")
    public boolean checkConstraintUniqueName() {
        if (StringUtils.isBlank(name)) {
            return true;
        }

        IScenarioDAO scenarioDAO = Registry.getScenarioDAO();

        if (isNewObject()) {
            return !scenarioDAO.existsByNameAnotherTransaction(
                    name);
        } else {
            try {
                Scenario scenario = scenarioDAO.findByName(name);
                return scenario.getId().equals(getId());
            } catch (InstanceNotFoundException e) {
                return true;
            }
        }
    }

}