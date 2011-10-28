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

package org.libreplan.business.scenarios.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.joda.time.DateTime;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.common.IHumanIdentifiable;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.scenarios.bootstrap.PredefinedScenarios;
import org.libreplan.business.scenarios.daos.IScenarioDAO;

/**
 * Represents a scenario in the application.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class Scenario extends BaseEntity implements IHumanIdentifiable {

    private String name;

    private String description;

    /**
     * For each order tracked by this Scenario exists an OrderVersion that will
     * have specific data for that order
     */
    private Map<Order, OrderVersion> orders = new HashMap<Order, OrderVersion>();

    private Scenario predecessor = null;

    private DateTime lastNotOwnedReassignationsTimeStamp;

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

    public Scenario(String name, Scenario predecessor) {
        this.name = name;
        this.predecessor = predecessor;
    }

    public void removeVersion(OrderVersion orderVersion) {
        Iterator<OrderVersion> iterator = orders.values().iterator();
        while (iterator.hasNext()) {
            OrderVersion each = iterator.next();
            if (ObjectUtils.equals(orderVersion, each)) {
                iterator.remove();
            }
        }
    }

    public OrderVersion addOrder(Order order) {
        addOrder(order, OrderVersion.createInitialVersion(this));
        return orders.get(order);
    }

    public void addOrder(Order order, OrderVersion orderVersion) {
        if (!orders.keySet().contains(order)) {
            orders.put(order, orderVersion);
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

    public Scenario getPredecessor() {
        return predecessor;
    }

    public List<Scenario> getPredecessors() {
        List<Scenario> result = new ArrayList<Scenario>();
        Scenario current = getPredecessor();
        while (current != null) {
            result.add(current);
            current = current.getPredecessor();
        }
        return result;
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

    public boolean isDerived() {
        return predecessor != null;
    }

    public boolean isMaster() {
        return PredefinedScenarios.MASTER.getScenario().getId().equals(getId());
    }

    public Scenario newDerivedScenario() {
        Scenario result = new Scenario("Derived from " + name, this);
        for (Order order : orders.keySet()) {
            result.addOrder(order, orders.get(order));
        }
        return result;
    }

    public boolean isPredefined() {
        if (name == null) {
            return false;
        }

        for (PredefinedScenarios predefinedScenario : PredefinedScenarios
                .values()) {
            if (predefinedScenario.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public OrderVersion getOrderVersion(Order order) {
        return orders.get(order);
    }

    public void setOrderVersion(Order order, OrderVersion newOrderVersion) {
        orders.put(order, newOrderVersion);
    }

    public boolean contains(Order each) {
        return orders.containsKey(each);
    }

    /**
     * @return If this scenario is related to previousOrderVersion for the order
     *         specified
     */
    public boolean usesVersion(OrderVersion previousOrderVersion, Order order) {
        Validate.notNull(order);
        OrderVersion orderVersionForThisScenario = getOrderVersion(order);
        if (previousOrderVersion == null) {
            return (orderVersionForThisScenario == null);
        }
        return orderVersionForThisScenario != null
                && orderVersionForThisScenario.getId().equals(
                        previousOrderVersion.getId());
    }

    public void removeOrderVersionForOrder(Order order) {
        orders.remove(order);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((getId() == null || isNewObject()) ? super.hashCode()
                        : getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || isNewObject()) {
            return false;
        }
        if (!(obj instanceof Scenario)) {
            return false;
        }
        Scenario other = (Scenario) obj;
        if (getId() == null) {
            if (other.getId() != null) {
                return false;
            }
        } else if (!getId().equals(other.getId())) {
            return false;
        }
        return true;
    }

    public List<Entry<Order, OrderVersion>> getOrderVersionsNeedingReassignation() {
        List<Entry<Order, OrderVersion>> result = new ArrayList<Entry<Order, OrderVersion>>();
        for (Entry<Order, OrderVersion> each : orders.entrySet()) {
            OrderVersion orderVersion = each.getValue();
            if (needsReassignation(orderVersion)) {
                result.add(each);
            }
        }
        return result;
    }

    private boolean needsReassignation(OrderVersion orderVersion) {
        boolean isOwnerScenario = this.equals(orderVersion.getOwnerScenario());
        return !isOwnerScenario
                && orderVersion
                        .hasBeenModifiedAfter(lastNotOwnedReassignationsTimeStamp);
    }

    @Override
    public String getHumanId() {
        return name;
    }

}
