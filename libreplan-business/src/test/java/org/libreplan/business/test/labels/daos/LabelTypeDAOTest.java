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

package org.libreplan.business.test.labels.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.labels.daos.ILabelTypeDAO;
import org.libreplan.business.labels.entities.LabelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Test for {@LabelTypeDAO}
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
@Transactional
public class LabelTypeDAOTest {

    @Autowired
    ILabelTypeDAO labelTypeDAO;

    @Test
    public void testInSpringContainer() {
        assertNotNull(labelTypeDAO);
    }

    @Test
    public void testSaveLabelType() {
        LabelType labelType = LabelType.create(UUID.randomUUID().toString());
        labelTypeDAO.save(labelType);
        assertTrue(labelType.getId() != null);
    }

    @Test
    public void testRemoveLabelType() throws InstanceNotFoundException {
        LabelType labelType = LabelType.create(UUID.randomUUID().toString());
        labelTypeDAO.save(labelType);
        labelTypeDAO.remove(labelType.getId());
        assertFalse(labelTypeDAO.exists(labelType.getId()));
    }

    @Test
    public void testListLabelTypes() {
        int previous = labelTypeDAO.list(LabelType.class).size();
        LabelType labelType = LabelType.create(UUID.randomUUID().toString());
        labelTypeDAO.save(labelType);
        List<LabelType> list = labelTypeDAO.list(LabelType.class);
        assertEquals(previous + 1, list.size());
    }
}
