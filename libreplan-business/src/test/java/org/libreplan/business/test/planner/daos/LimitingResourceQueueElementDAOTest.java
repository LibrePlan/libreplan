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
package org.libreplan.business.test.planner.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.planner.limiting.daos.ILimitingResourceQueueDAO;
import org.libreplan.business.planner.limiting.daos.ILimitingResourceQueueElementDAO;
import org.libreplan.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.libreplan.business.resources.entities.LimitingResourceQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
@Transactional
public class LimitingResourceQueueElementDAOTest {

    @Autowired
    private ILimitingResourceQueueDAO limitingResourceQueueDAO;

    @Autowired
    private ILimitingResourceQueueElementDAO limitingResourceQueueElementDAO;

    @Test
    public void testInSpringContainer() {
        assertNotNull(limitingResourceQueueDAO);
    }

    private LimitingResourceQueueElement createValidLimitingResourceQueueElement() {
        return LimitingResourceQueueElement.create();
    }

    private LimitingResourceQueue createValidLimitingResourceQueue() {
        return LimitingResourceQueue.create();
    }

    @Test
    public void testSaveLimitingResourceQueue() {
        LimitingResourceQueue limitingResourceQueue = createValidLimitingResourceQueue();
        limitingResourceQueueDAO.save(limitingResourceQueue);
        assertTrue(limitingResourceQueueDAO.exists(limitingResourceQueue.getId()));
    }

    @Test
    public void testRemoveLimitingResourceQueue() throws InstanceNotFoundException {
        LimitingResourceQueue limitingResourceQueue = createValidLimitingResourceQueue();
        limitingResourceQueueDAO.save(limitingResourceQueue);
        limitingResourceQueueDAO.remove(limitingResourceQueue.getId());
        assertFalse(limitingResourceQueueDAO.exists(limitingResourceQueue.getId()));
    }

    @Test
    public void testListLimitingResourceQueue() {
        int previous = limitingResourceQueueDAO.list(LimitingResourceQueue.class).size();

        LimitingResourceQueue limitingResourceQueue1 = createValidLimitingResourceQueue();
        limitingResourceQueueDAO.save(limitingResourceQueue1);
        LimitingResourceQueue limitingResourceQueue2 = createValidLimitingResourceQueue();
        limitingResourceQueueDAO.save(limitingResourceQueue1);
        limitingResourceQueueDAO.save(limitingResourceQueue2);

        List<LimitingResourceQueue> list = limitingResourceQueueDAO
                .list(LimitingResourceQueue.class);
        assertEquals(previous + 2, list.size());
    }

    @Test
    public void testLimitingResourceQueueHasElements() {
        LimitingResourceQueueElement element = createValidLimitingResourceQueueElement();
        LimitingResourceQueue queue = createValidLimitingResourceQueue();
        queue.addLimitingResourceQueueElement(element);
        limitingResourceQueueDAO.save(queue);
        assertTrue(!limitingResourceQueueElementDAO.getAssigned().isEmpty());
    }

}
