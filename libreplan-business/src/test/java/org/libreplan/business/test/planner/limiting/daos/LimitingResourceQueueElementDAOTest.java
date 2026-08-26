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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.planner.limiting.daos.ILimitingResourceQueueDAO;
import org.libreplan.business.planner.limiting.daos.ILimitingResourceQueueElementDAO;
import org.libreplan.business.planner.limiting.entities.LimitingResourceQueueElement;
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
 * Characterization tests for {@link LimitingResourceQueueElementDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 */
public class LimitingResourceQueueElementDAOTest {

    @Autowired
    private ILimitingResourceQueueElementDAO limitingResourceQueueElementDAO;

    @Autowired
    private ILimitingResourceQueueDAO limitingResourceQueueDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    private LimitingResourceQueue createValidQueue() {
        Worker worker = Worker.create(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        resourceDAO.save(worker);
        LimitingResourceQueue queue = LimitingResourceQueue.create();
        queue.setResource(worker);
        limitingResourceQueueDAO.save(queue);
        return queue;
    }

    @Test
    @Transactional
    public void testGetAllIncludesSaved() {
        LimitingResourceQueueElement element = LimitingResourceQueueElement.create();
        limitingResourceQueueElementDAO.save(element);

        boolean found = false;
        for (LimitingResourceQueueElement e : limitingResourceQueueElementDAO.getAll()) {
            if (e.getId().equals(element.getId())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    @Transactional
    public void testGetAssignedAndGetUnassignedPartitionByQueue() {
        LimitingResourceQueueElement assigned = LimitingResourceQueueElement.create();
        assigned.setLimitingResourceQueue(createValidQueue());
        limitingResourceQueueElementDAO.save(assigned);

        LimitingResourceQueueElement unassigned = LimitingResourceQueueElement.create();
        limitingResourceQueueElementDAO.save(unassigned);

        List<LimitingResourceQueueElement> assignedResult = limitingResourceQueueElementDAO.getAssigned();
        boolean assignedFound = false;
        for (LimitingResourceQueueElement e : assignedResult) {
            if (e.getId().equals(assigned.getId())) {
                assignedFound = true;
            }
            assertFalse(e.getId().equals(unassigned.getId()));
        }
        assertTrue(assignedFound);

        List<LimitingResourceQueueElement> unassignedResult = limitingResourceQueueElementDAO.getUnassigned();
        boolean unassignedFound = false;
        for (LimitingResourceQueueElement e : unassignedResult) {
            if (e.getId().equals(unassigned.getId())) {
                unassignedFound = true;
            }
            assertFalse(e.getId().equals(assigned.getId()));
        }
        assertTrue(unassignedFound);
    }

    @Test
    @Transactional
    public void testGetAssignedOrderedByCreationTimestamp() {
        LimitingResourceQueue queue = createValidQueue();

        LimitingResourceQueueElement first = LimitingResourceQueueElement.create();
        first.setLimitingResourceQueue(queue);
        first.setCreationTimestamp(1000L);
        limitingResourceQueueElementDAO.save(first);

        LimitingResourceQueueElement second = LimitingResourceQueueElement.create();
        second.setLimitingResourceQueue(queue);
        second.setCreationTimestamp(2000L);
        limitingResourceQueueElementDAO.save(second);

        List<LimitingResourceQueueElement> ordered = limitingResourceQueueElementDAO.getAssigned();
        int firstIndex = ordered.indexOf(first);
        int secondIndex = ordered.indexOf(second);
        assertTrue(firstIndex < secondIndex);
    }

}
