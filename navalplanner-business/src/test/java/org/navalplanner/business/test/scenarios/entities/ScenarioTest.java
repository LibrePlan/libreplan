/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.test.scenarios.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link Scenario} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class ScenarioTest {

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Before
    public void loadRequiredaData() {
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
    }

    @Test
    public void checkNewDerivedScenario() {
        String predecessorScenarioName = "parent";
        Scenario predecessor = Scenario.create(predecessorScenarioName);

        Order order = Order.create();
        String orderName = "order1";
        order.setName(orderName);
        predecessor.addOrder(order);

        Scenario child = predecessor.newDerivedScenario();
        assertThat(child.getPredecessor().getName(), equalTo(predecessor
                .getName()));

        assertThat(child.getOrders().size(), equalTo(1));
        assertThat(child.getOrders().keySet().iterator().next().getName(),
                equalTo(orderName));
    }

}
