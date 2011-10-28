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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.scenarios.entities.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO implementation for {@link Scenario}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ScenarioDAO extends GenericDAOHibernate<Scenario, Long> implements
        IScenarioDAO {

    @Autowired
    private IOrderVersionDAO orderVersionDAO;

    @Override
    public void save(Scenario scenario) throws ValidationException {
        saveNewlyAddedOrderVersionsFor(scenario);
        super.save(scenario);
    }

    private void saveNewlyAddedOrderVersionsFor(Scenario scenario) {
        List<OrderVersion> newOrders = getNewOrders(scenario);
        for (OrderVersion each : newOrders) {
            orderVersionDAO.save(each);
        }
    }

    private List<OrderVersion> getNewOrders(Scenario scenario) {
        Collection<OrderVersion> values = scenario.getOrders().values();
        List<OrderVersion> newOrders = new ArrayList<OrderVersion>();
        for (OrderVersion each : values) {
            if (each.isNewObject()) {
                newOrders.add(each);
            }
        }
        return newOrders;
    }

    @Override
    @Transactional(readOnly = true)
    public Scenario findByName(String name) throws InstanceNotFoundException {
        if (StringUtils.isBlank(name)) {
            throw new InstanceNotFoundException(null, Scenario.class.getName());
        }

        Scenario scenario = (Scenario) getSession().createCriteria(
                Scenario.class).add(
                Restrictions.eq("name", name.trim()).ignoreCase())
                .uniqueResult();

        if (scenario == null) {
            throw new InstanceNotFoundException(name, Scenario.class.getName());
        } else {
            return scenario;
        }

    }

    @Override
    public boolean existsByName(String name) {
        try {
            findByName(name);
            return true;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsByNameAnotherTransaction(String name) {
        return existsByName(name);
    }

    @Override
    public List<Scenario> getAll() {
        return list(Scenario.class);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @Override
    public boolean thereIsOtherWithSameName(Scenario scenario) {
        try {
            Scenario withSameName = findByName(scenario.getName());
            return areDifferentInDB(withSameName, scenario);
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    private boolean areDifferentInDB(Scenario one, Scenario other) {
        if ((one.getId() == null) || (other.getId() == null)) {
            return true;
        }
        return !one.getId().equals(other.getId());
    }

    @Override
    public List<Scenario> findByPredecessor(Scenario scenario) {
        if (scenario == null) {
            return Collections.emptyList();
        }

        Criteria c = getSession().createCriteria(Scenario.class).add(
                Restrictions.eq("predecessor", scenario));
        return (List<Scenario>) c.list();
    }

    @Override
    public List<Scenario> getDerivedScenarios(Scenario scenario) {
        List<Scenario> result = new ArrayList<Scenario>();

        List<Scenario> children = findByPredecessor(scenario);
        result.addAll(children);

        for (Scenario child : children) {
            result.addAll(getDerivedScenarios(child));
        }

        return result;
    }

    @Override
    public void updateDerivedScenariosWithNewVersion(
            OrderVersion previousOrderVersion, Order order,
            Scenario currentScenario, OrderVersion newOrderVersion) {
        for (Scenario each : getDerivedScenarios(currentScenario)) {
            if (each.usesVersion(previousOrderVersion, order)) {
                if (newOrderVersion == null) {
                    each.removeOrderVersionForOrder(order);
                } else {
                    each.setOrderVersion(order, newOrderVersion);
                }
                save(each);
            }
        }
    }

}
