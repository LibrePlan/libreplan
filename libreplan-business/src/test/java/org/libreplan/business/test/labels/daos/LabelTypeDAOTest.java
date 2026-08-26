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

import org.hibernate.NonUniqueResultException;
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
public class LabelTypeDAOTest {

    @Autowired
    ILabelTypeDAO labelTypeDAO;

    @Test
    @Transactional
    public void testInSpringContainer() {
        assertNotNull(labelTypeDAO);
    }

    @Test
    @Transactional
    public void testSaveLabelType() {
        LabelType labelType = LabelType.create(UUID.randomUUID().toString());
        labelTypeDAO.save(labelType);
        assertTrue(labelType.getId() != null);
    }

    @Test
    @Transactional
    public void testRemoveLabelType() throws InstanceNotFoundException {
        LabelType labelType = LabelType.create(UUID.randomUUID().toString());
        labelTypeDAO.save(labelType);
        labelTypeDAO.remove(labelType.getId());
        assertFalse(labelTypeDAO.exists(labelType.getId()));
    }

    @Test
    @Transactional
    public void testListLabelTypes() {
        int previous = labelTypeDAO.list(LabelType.class).size();
        LabelType labelType = LabelType.create(UUID.randomUUID().toString());
        labelTypeDAO.save(labelType);
        List<LabelType> list = labelTypeDAO.list(LabelType.class);
        assertEquals(previous + 1, list.size());
    }

    /*
     * Characterization tests added for the Hibernate Criteria -> JPA Criteria API migration
     * (Jakarta EE / Hibernate 6). findUniqueByName/isUnique/existsByName had no test coverage
     * before, including the NonUniqueResultException path relied on by callers.
     */

    @Test
    @Transactional
    public void testFindUniqueByNameReturnsMatch() throws InstanceNotFoundException, NonUniqueResultException {
        String name = "name-" + UUID.randomUUID();
        LabelType labelType = LabelType.create(name);
        labelTypeDAO.save(labelType);

        assertEquals(labelType.getId(), labelTypeDAO.findUniqueByName(name).getId());
    }

    @Test(expected = InstanceNotFoundException.class)
    @Transactional
    public void testFindUniqueByNameThrowsWhenNotFound() throws InstanceNotFoundException, NonUniqueResultException {
        labelTypeDAO.findUniqueByName("does-not-exist-" + UUID.randomUUID());
    }

    // Note: a genuine duplicate name can't actually be constructed to exercise
    // findUniqueByName's NonUniqueResultException path - the "name" column has a DB-level
    // unique constraint (uk_t157wpumxra7aoutij7lv2peh), so saveWithoutValidating still fails
    // with a ConstraintViolationException on flush rather than allowing two rows to exist.

    @Test
    @Transactional
    public void testIsUniqueTrueWhenNameNotUsed() {
        LabelType labelType = LabelType.create("unused-name-" + UUID.randomUUID());
        assertTrue(labelTypeDAO.isUnique(labelType));
    }

    // Note: isUnique() is @Transactional(REQUIRES_NEW), so it runs on a separate connection
    // that can't see rows saved earlier in this same @Transactional test method (they're
    // uncommitted). Positive-match branches (name used by another entity / by the same
    // entity) can't be exercised from a single-transaction test the same way the delegating
    // "...AnotherTransaction" wrapper methods can't - see testIsUniqueTrueWhenNameNotUsed
    // above for the one branch (name not found at all) that doesn't depend on transaction
    // visibility of this test's own uncommitted data.

    @Test
    @Transactional
    public void testExistsByNameTrueWhenPresent() {
        String name = "name-" + UUID.randomUUID();
        LabelType labelType = LabelType.create(name);
        labelTypeDAO.save(labelType);

        LabelType candidate = LabelType.create(name);
        assertTrue(labelTypeDAO.existsByName(candidate));
    }

    @Test
    @Transactional
    public void testExistsByNameFalseWhenAbsent() {
        LabelType candidate = LabelType.create("does-not-exist-" + UUID.randomUUID());
        assertFalse(labelTypeDAO.existsByName(candidate));
    }

}
