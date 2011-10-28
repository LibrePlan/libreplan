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
import org.libreplan.business.labels.daos.ILabelDAO;
import org.libreplan.business.labels.daos.ILabelTypeDAO;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.labels.entities.LabelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Test for {@LabelDAO}
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
@Transactional
public class LabelDAOTest {

    @Autowired
    ILabelDAO labelDAO;

    @Autowired
    ILabelTypeDAO labelTypeDAO;

    @Test
    public void testInSpringContainer() {
        assertNotNull(labelDAO);
    }

    public Label createValidLabel() {
        LabelType labelType = LabelType.create(UUID.randomUUID().toString());
        labelTypeDAO.save(labelType);
        Label label = Label.create(UUID.randomUUID().toString());
        label.setType(labelType);
        return label;
    }

    @Test
    public void testSaveLabel() {
        Label label = createValidLabel();
        labelDAO.save(label);
        assertTrue(label.getId() != null);
    }

    @Test
    public void testRemoveLabel() throws InstanceNotFoundException {
        Label label = createValidLabel();
        labelDAO.save(label);
        labelDAO.remove(label.getId());
        assertFalse(labelDAO.exists(label.getId()));
    }

    @Test
    public void testListLabels() {
        int previous = labelDAO.list(Label.class).size();
        Label label = createValidLabel();
        labelDAO.save(label);
        List<Label> list = labelDAO.list(Label.class);
        assertEquals(previous + 1, list.size());
    }
}
