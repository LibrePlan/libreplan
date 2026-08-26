/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2015 LibrePlan
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import org.libreplan.business.common.daos.ILimitsDAO;
import org.libreplan.business.common.entities.Limits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link LimitsDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 *
 * Limits.hbm.xml used to declare <class name="Limits" abstract="true" ...> with no subclass
 * anywhere - Hibernate's "increment" id generator can't initialize against an abstract-mapped
 * class, so save() always threw SQLGrammarException. Fixed (Phase 6, see
 * doc/technical/jdk25-migration/Phase5-found-bugs.md item 1) by dropping abstract="true" - Limits
 * was never meant to have subclasses, it's a plain type/value row. The app itself still never
 * writes these rows (per Jeroen: this is a cloud-deployment per-seat-license limit, e.g. max
 * users, that the DB administrator sets directly in the database - no in-app GUI is needed or
 * planned), but save() being broken was a landmine for anyone who assumed it worked, and fixing
 * the mapping is what lets the positive branches of getAll()/getLimitsByType() be tested at all.
 */
public class LimitsDAOTest {

    @Autowired
    private ILimitsDAO limitsDAO;

    @Test
    @Transactional
    public void testGetLimitsByTypeReturnsNullWhenAbsent() {
        assertNull(limitsDAO.getLimitsByType("does-not-exist-" + UUID.randomUUID()));
    }

    @Test
    @Transactional
    public void testSaveAndFindByType() {
        String type = "type-" + UUID.randomUUID();
        Limits limits = new Limits();
        limits.setType(type);
        limits.setValue(42);

        limitsDAO.save(limits);

        Limits found = limitsDAO.getLimitsByType(type);
        assertEquals(limits.getId(), found.getId());
        assertEquals(Integer.valueOf(42), found.getValue());
    }

    @Test
    @Transactional
    public void testGetAllIncludesSavedLimits() {
        int previous = limitsDAO.getAll().size();

        Limits limits = new Limits();
        limits.setType("type-" + UUID.randomUUID());
        limits.setValue(1);
        limitsDAO.save(limits);

        assertTrue(limitsDAO.getAll().size() > previous);
    }

}
