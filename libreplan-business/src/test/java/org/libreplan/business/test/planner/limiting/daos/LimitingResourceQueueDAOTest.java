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

package org.libreplan.business.test.planner.limiting.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.planner.limiting.daos.ILimitingResourceQueueDAO;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.resources.entities.LimitingResourceQueue;
import org.libreplan.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link LimitingResourceQueueDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 */
public class LimitingResourceQueueDAOTest {

    @Autowired
    private ILimitingResourceQueueDAO limitingResourceQueueDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    private Worker createValidWorker() {
        Worker worker = Worker.create(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        resourceDAO.save(worker);
        return worker;
    }

    @Test
    @Transactional
    public void testFindQueueByResourceReturnsMatch() {
        Worker worker = createValidWorker();
        LimitingResourceQueue queue = LimitingResourceQueue.create();
        queue.setResource(worker);
        limitingResourceQueueDAO.save(queue);

        assertEquals(queue.getId(), limitingResourceQueueDAO.findQueueByResource(worker).getId());
    }

    @Test
    @Transactional
    public void testFindQueueByResourceReturnsNullWhenAbsent() {
        Worker worker = createValidWorker();
        assertNull(limitingResourceQueueDAO.findQueueByResource(worker));
    }

    @Test
    @Transactional
    public void testGetAllIncludesSaved() {
        Worker worker = createValidWorker();
        LimitingResourceQueue queue = LimitingResourceQueue.create();
        queue.setResource(worker);
        limitingResourceQueueDAO.save(queue);

        boolean found = false;
        for (LimitingResourceQueue q : limitingResourceQueueDAO.getAll()) {
            if (q.getId().equals(queue.getId())) {
                found = true;
            }
        }
        assertTrue(found);
    }

}
