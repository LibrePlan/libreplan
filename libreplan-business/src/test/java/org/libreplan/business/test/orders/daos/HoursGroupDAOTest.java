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

package org.libreplan.business.test.orders.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.daos.IHoursGroupDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link HoursGroupDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 */
public class HoursGroupDAOTest {

    @Autowired
    private IHoursGroupDAO hoursGroupDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    private HoursGroup createValid(String code) {
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setCode(code);
        return hoursGroup;
    }

    @Test
    @Transactional
    public void testFindUniqueByCodeAnotherTransactionThrowsWhenNotFound() {
        HoursGroup notSaved = createValid("does-not-exist-" + UUID.randomUUID());
        try {
            hoursGroupDAO.findUniqueByCodeAnotherTransaction(notSaved);
            assertTrue("expected InstanceNotFoundException", false);
        } catch (InstanceNotFoundException e) {
            // expected
        }
    }

    @Test
    public void testFindUniqueByCodeAnotherTransactionReturnsMatch() throws InstanceNotFoundException {
        final String code = "code-" + UUID.randomUUID();

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                HoursGroup hoursGroup = createValid(code);
                hoursGroupDAO.save(hoursGroup);
                return null;
            }
        });

        HoursGroup lookup = createValid(code);
        HoursGroup found = hoursGroupDAO.findUniqueByCodeAnotherTransaction(lookup);
        assertEquals(code, found.getCode());
    }

    @Test
    public void testExistsByCodeAnotherTransactionTrueWhenSameCodeUsedByAnotherEntity() {
        final String code = "code-" + UUID.randomUUID();

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                HoursGroup hoursGroup = createValid(code);
                hoursGroupDAO.save(hoursGroup);
                return null;
            }
        });

        HoursGroup other = createValid(code);
        assertTrue(hoursGroupDAO.existsByCodeAnotherTransaction(other));
    }

    @Test
    @Transactional
    public void testFindRepeatedHoursGroupCodeInDBNullWhenNoConflict() {
        HoursGroup hoursGroup = createValid("code-" + UUID.randomUUID());
        assertNull(hoursGroupDAO.findRepeatedHoursGroupCodeInDB(java.util.Collections.singletonList(hoursGroup)));
    }

}
