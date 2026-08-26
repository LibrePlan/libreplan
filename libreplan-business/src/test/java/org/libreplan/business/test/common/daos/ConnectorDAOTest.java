/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

package org.libreplan.business.test.common.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.daos.IConnectorDAO;
import org.libreplan.business.common.entities.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link ConnectorDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 */
public class ConnectorDAOTest {

    @Autowired
    private IConnectorDAO connectorDAO;

    private Connector createValidConnector(String name) {
        Connector connector = Connector.create(name);
        connectorDAO.save(connector);
        return connector;
    }

    @Test
    @Transactional
    public void testFindUniqueByNameReturnsMatch() {
        String name = "connector-" + UUID.randomUUID();
        Connector connector = createValidConnector(name);
        assertEquals(connector.getId(), connectorDAO.findUniqueByName(name).getId());
    }

    @Test
    @Transactional
    public void testFindUniqueByNameReturnsNullWhenAbsent() {
        assertNull(connectorDAO.findUniqueByName("does-not-exist-" + UUID.randomUUID()));
    }

    @Test
    @Transactional
    public void testFindUniqueByNameIsCaseSensitive() {
        String name = "MiXeD-" + UUID.randomUUID();
        createValidConnector(name);
        assertNull(connectorDAO.findUniqueByName(name.toLowerCase()));
    }

    @Test
    @Transactional
    public void testGetAllIncludesSaved() {
        Connector connector = createValidConnector("connector-" + UUID.randomUUID());
        boolean found = false;
        for (Connector c : connectorDAO.getAll()) {
            if (c.getId().equals(connector.getId())) {
                found = true;
            }
        }
        assertTrue(found);
    }

}
