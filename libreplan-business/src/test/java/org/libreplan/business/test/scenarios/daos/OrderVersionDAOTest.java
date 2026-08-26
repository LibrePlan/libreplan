/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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
package org.libreplan.business.test.scenarios.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.scenarios.daos.IOrderVersionDAO;
import org.libreplan.business.scenarios.daos.IScenarioDAO;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.scenarios.entities.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/*
 * Characterization tests added for the Hibernate Criteria -> JPA Criteria API migration
 * (Jakarta EE / Hibernate 6). OrderVersionDAO had no test file at all before.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE, BUSINESS_SPRING_CONFIG_TEST_FILE })
public class OrderVersionDAOTest {

    @Autowired
    private IOrderVersionDAO orderVersionDAO;

    @Autowired
    private IScenarioDAO scenarioDAO;

    private Scenario createAndSaveScenario() {
        Scenario scenario = Scenario.create(UUID.randomUUID().toString());
        scenarioDAO.save(scenario);
        return scenario;
    }

    @Test
    @Transactional
    public void testInSpringContainer() {
        assertNotNull(orderVersionDAO);
    }

    @Test
    @Transactional
    public void getOrderVersionByOwnerScenarioReturnsEmptyListForNullScenario() {
        List<OrderVersion> result = orderVersionDAO.getOrderVersionByOwnerScenario(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    public void getOrderVersionByOwnerScenarioReturnsEmptyListWhenNoneMatch() {
        Scenario scenario = createAndSaveScenario();
        List<OrderVersion> result = orderVersionDAO.getOrderVersionByOwnerScenario(scenario);
        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    public void getOrderVersionByOwnerScenarioOnlyReturnsVersionsOwnedByThatScenario() {
        Scenario ownerScenario = createAndSaveScenario();
        Scenario otherScenario = createAndSaveScenario();

        OrderVersion ownedVersion = OrderVersion.createInitialVersion(ownerScenario);
        orderVersionDAO.save(ownedVersion);

        OrderVersion otherVersion = OrderVersion.createInitialVersion(otherScenario);
        orderVersionDAO.save(otherVersion);

        List<OrderVersion> result = orderVersionDAO.getOrderVersionByOwnerScenario(ownerScenario);

        assertEquals(1, result.size());
        assertEquals(ownedVersion.getId(), result.get(0).getId());
    }

}
