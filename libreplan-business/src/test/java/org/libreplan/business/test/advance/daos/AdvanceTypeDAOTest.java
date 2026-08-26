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

package org.libreplan.business.test.advance.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.advance.daos.IAdvanceAssignmentDAO;
import org.libreplan.business.advance.daos.IAdvanceTypeDAO;
import org.libreplan.business.advance.entities.AdvanceAssignment;
import org.libreplan.business.advance.entities.AdvanceType;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link AdvanceTypeDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 */
public class AdvanceTypeDAOTest {

    @Autowired
    private IAdvanceTypeDAO advanceTypeDAO;

    @Autowired
    private IAdvanceAssignmentDAO advanceAssignmentDAO;

    private AdvanceType createValidAdvanceType(String unitName, boolean active) {
        BigDecimal value = new BigDecimal(100);
        BigDecimal precision = BigDecimal.ONE;
        AdvanceType advanceType = AdvanceType.create(unitName, value, true, precision, active, false);
        advanceTypeDAO.save(advanceType);
        return advanceType;
    }

    @Test
    @Transactional
    public void testExistsNameAdvanceTypeTrueWhenPresent() {
        String name = "unit-" + UUID.randomUUID();
        createValidAdvanceType(name, true);
        assertTrue(advanceTypeDAO.existsNameAdvanceType(name));
    }

    @Test
    @Transactional
    public void testExistsNameAdvanceTypeFalseWhenAbsent() {
        assertFalse(advanceTypeDAO.existsNameAdvanceType("does-not-exist-" + UUID.randomUUID()));
    }

    @Test
    @Transactional
    public void testFindByNameReturnsMatch() {
        String name = "unit-" + UUID.randomUUID();
        AdvanceType advanceType = createValidAdvanceType(name, true);
        assertEquals(advanceType.getId(), advanceTypeDAO.findByName(name).getId());
    }

    @Test
    @Transactional
    public void testFindByNameReturnsNullWhenAbsent() {
        assertEquals(null, advanceTypeDAO.findByName("does-not-exist-" + UUID.randomUUID()));
    }

    @Test
    @Transactional
    public void testFindByNameIsCaseSensitive() {
        String name = "MiXeD-" + UUID.randomUUID();
        createValidAdvanceType(name, true);
        // findByName uses Restrictions.eq (case-sensitive), unlike findByNameCaseInsensitive
        assertEquals(null, advanceTypeDAO.findByName(name.toLowerCase()));
    }

    @Test
    @Transactional
    public void testFindActivesAdvanceTypesOnlyReturnsActiveOnes() {
        String activeName = "active-" + UUID.randomUUID();
        String inactiveName = "inactive-" + UUID.randomUUID();
        AdvanceType active = createValidAdvanceType(activeName, true);
        createValidAdvanceType(inactiveName, false);

        boolean foundActive = false;
        for (AdvanceType at : advanceTypeDAO.findActivesAdvanceTypes()) {
            assertTrue(at.getActive());
            if (at.getId().equals(active.getId())) {
                foundActive = true;
            }
            assertFalse(at.getUnitName().equals(inactiveName));
        }
        assertTrue(foundActive);
    }

    @Test
    @Transactional
    public void testIsAlreadyInUseFalseWhenNoAssignment() {
        AdvanceType advanceType = createValidAdvanceType("unit-" + UUID.randomUUID(), true);
        assertFalse(advanceTypeDAO.isAlreadyInUse(advanceType));
    }

    @Test
    @Transactional
    public void testIsAlreadyInUseTrueWhenAssignmentExists() {
        AdvanceType advanceType = createValidAdvanceType("unit-" + UUID.randomUUID(), true);
        AdvanceAssignment advance = DirectAdvanceAssignment.create(false, BigDecimal.TEN);
        advance.setAdvanceType(advanceType);
        advanceAssignmentDAO.save(advance);

        assertTrue(advanceTypeDAO.isAlreadyInUse(advanceType));
    }

    @Test
    @Transactional
    public void testFindByNameCaseInsensitiveMatchesAnyCase() throws InstanceNotFoundException {
        String mixedCaseName = "MiXeD-" + UUID.randomUUID();
        AdvanceType advanceType = createValidAdvanceType(mixedCaseName, true);

        assertEquals(advanceType.getId(), advanceTypeDAO.findByNameCaseInsensitive(mixedCaseName).getId());
        assertEquals(advanceType.getId(), advanceTypeDAO.findByNameCaseInsensitive(mixedCaseName.toLowerCase()).getId());
        assertEquals(advanceType.getId(), advanceTypeDAO.findByNameCaseInsensitive(mixedCaseName.toUpperCase()).getId());
    }

    @Test(expected = InstanceNotFoundException.class)
    @Transactional
    public void testFindByNameCaseInsensitiveThrowsWhenNotFound() throws InstanceNotFoundException {
        advanceTypeDAO.findByNameCaseInsensitive("does-not-exist-" + UUID.randomUUID());
    }

}
