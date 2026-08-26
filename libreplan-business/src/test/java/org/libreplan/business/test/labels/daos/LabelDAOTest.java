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
public class LabelDAOTest {

    @Autowired
    ILabelDAO labelDAO;

    @Autowired
    ILabelTypeDAO labelTypeDAO;

    @Test
    @Transactional
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
    @Transactional
    public void testSaveLabel() {
        Label label = createValidLabel();
        labelDAO.save(label);
        assertTrue(label.getId() != null);
    }

    @Test
    @Transactional
    public void testRemoveLabel() throws InstanceNotFoundException {
        Label label = createValidLabel();
        labelDAO.save(label);
        labelDAO.remove(label.getId());
        assertFalse(labelDAO.exists(label.getId()));
    }

    @Test
    @Transactional
    public void testListLabels() {
        int previous = labelDAO.list(Label.class).size();
        Label label = createValidLabel();
        labelDAO.save(label);
        List<Label> list = labelDAO.list(Label.class);
        assertEquals(previous + 1, list.size());
    }

    /*
     * Characterization tests added for the Hibernate Criteria -> JPA Criteria API migration
     * (Jakarta EE / Hibernate 6). LabelDAO's own methods (findByNameAndType/findByType/
     * existsByName) and the generic IntegrationEntityDAO methods it inherits
     * (findByCode/existsByCode/findAll) had no test coverage before.
     */

    @Test
    @Transactional
    public void testFindByNameAndTypeReturnsMatch() {
        Label label = createValidLabel();
        labelDAO.save(label);
        Label found = labelDAO.findByNameAndType(label.getName(), label.getType());
        assertEquals(label.getId(), found.getId());
    }

    @Test
    @Transactional
    public void testFindByNameAndTypeReturnsNullWhenTypeDoesNotMatch() {
        Label label = createValidLabel();
        labelDAO.save(label);
        LabelType otherType = LabelType.create(UUID.randomUUID().toString());
        labelTypeDAO.save(otherType);
        assertEquals(null, labelDAO.findByNameAndType(label.getName(), otherType));
    }

    @Test
    @Transactional
    public void testFindByTypeOnlyReturnsLabelsOfThatType() {
        Label label = createValidLabel();
        labelDAO.save(label);
        LabelType otherType = LabelType.create(UUID.randomUUID().toString());
        labelTypeDAO.save(otherType);
        Label otherLabel = Label.create(UUID.randomUUID().toString());
        otherLabel.setType(otherType);
        labelDAO.save(otherLabel);

        List<Label> found = labelDAO.findByType(label.getType());
        assertEquals(1, found.size());
        assertEquals(label.getId(), found.get(0).getId());
    }

    @Test
    @Transactional
    public void testExistsByNameTrueWhenPresent() {
        Label label = createValidLabel();
        labelDAO.save(label);
        assertTrue(labelDAO.existsByName(label.getName()));
    }

    @Test
    @Transactional
    public void testExistsByNameFalseWhenAbsent() {
        assertFalse(labelDAO.existsByName("does-not-exist-" + UUID.randomUUID()));
    }

    @Test
    @Transactional
    public void testFindByCodeIsCaseInsensitiveAndTrims() throws InstanceNotFoundException {
        String code = "MiXeD-" + UUID.randomUUID();
        Label label = Label.create(code, UUID.randomUUID().toString());
        LabelType labelType = LabelType.create(UUID.randomUUID().toString());
        labelTypeDAO.save(labelType);
        label.setType(labelType);
        labelDAO.save(label);

        assertEquals(label.getId(), labelDAO.findByCode(code).getId());
        assertEquals(label.getId(), labelDAO.findByCode(code.toLowerCase()).getId());
        assertEquals(label.getId(), labelDAO.findByCode(code.toUpperCase()).getId());
        assertEquals(label.getId(), labelDAO.findByCode("  " + code + "  ").getId());
    }

    @Test(expected = InstanceNotFoundException.class)
    @Transactional
    public void testFindByCodeThrowsWhenNotFound() throws InstanceNotFoundException {
        labelDAO.findByCode("does-not-exist-" + UUID.randomUUID());
    }

    @Test
    @Transactional
    public void testExistsByCodeTrueWhenPresent() {
        Label label = createValidLabel();
        labelDAO.save(label);
        assertTrue(labelDAO.existsByCode(label.getCode()));
    }

    @Test
    @Transactional
    public void testExistsByCodeFalseWhenAbsent() {
        assertFalse(labelDAO.existsByCode("does-not-exist-" + UUID.randomUUID()));
    }

    @Test
    @Transactional
    public void testFindAllIsOrderedByCodeAscending() {
        String commonPrefix = UUID.randomUUID().toString();

        Label first = Label.create(commonPrefix + "-1-aaa", UUID.randomUUID().toString());
        LabelType labelType = LabelType.create(UUID.randomUUID().toString());
        labelTypeDAO.save(labelType);
        first.setType(labelType);
        labelDAO.save(first);

        Label second = Label.create(commonPrefix + "-2-bbb", UUID.randomUUID().toString());
        second.setType(labelType);
        labelDAO.save(second);

        Label third = Label.create(commonPrefix + "-3-ccc", UUID.randomUUID().toString());
        third.setType(labelType);
        labelDAO.save(third);

        List<Label> ourLabelsInReturnedOrder = new java.util.ArrayList<>();
        for (Label l : labelDAO.findAll()) {
            if (l.getCode().startsWith(commonPrefix)) {
                ourLabelsInReturnedOrder.add(l);
            }
        }

        assertEquals(3, ourLabelsInReturnedOrder.size());
        assertEquals(first.getId(), ourLabelsInReturnedOrder.get(0).getId());
        assertEquals(second.getId(), ourLabelsInReturnedOrder.get(1).getId());
        assertEquals(third.getId(), ourLabelsInReturnedOrder.get(2).getId());
    }
}
