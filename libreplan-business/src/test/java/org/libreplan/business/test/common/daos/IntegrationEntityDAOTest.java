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

package org.libreplan.business.test.common.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.test.calendars.entities.BaseCalendarTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link IntegrationEntityDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 *
 * IntegrationEntityDAO is a generic base class with no @Repository bean of its
 * own, so its behavior is exercised here through IBaseCalendarDAO (BaseCalendar
 * extends IntegrationEntity, and BaseCalendarDAO does not override findByCode,
 * existsByCode or findAll).
 */
public class IntegrationEntityDAOTest {

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    private BaseCalendar createValidCalendarWithCode(String code) {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        calendar.setCode(code);
        baseCalendarDAO.save(calendar);
        return calendar;
    }

    @Test
    @Transactional
    public void testFindByCodeReturnsMatch() throws InstanceNotFoundException {
        String code = "code-" + UUID.randomUUID();
        BaseCalendar calendar = createValidCalendarWithCode(code);
        assertEquals(calendar.getId(), baseCalendarDAO.findByCode(code).getId());
    }

    @Test
    @Transactional
    public void testFindByCodeIsCaseInsensitiveAndTrims() throws InstanceNotFoundException {
        String mixedCaseCode = "MiXeD-" + UUID.randomUUID();
        BaseCalendar calendar = createValidCalendarWithCode(mixedCaseCode);

        assertEquals(calendar.getId(), baseCalendarDAO.findByCode(mixedCaseCode.toLowerCase()).getId());
        assertEquals(calendar.getId(), baseCalendarDAO.findByCode(mixedCaseCode.toUpperCase()).getId());
        assertEquals(calendar.getId(), baseCalendarDAO.findByCode("  " + mixedCaseCode + "  ").getId());
    }

    @Test(expected = InstanceNotFoundException.class)
    @Transactional
    public void testFindByCodeThrowsWhenNotFound() throws InstanceNotFoundException {
        baseCalendarDAO.findByCode("does-not-exist-" + UUID.randomUUID());
    }

    @Test(expected = InstanceNotFoundException.class)
    @Transactional
    public void testFindByCodeThrowsWhenBlank() throws InstanceNotFoundException {
        baseCalendarDAO.findByCode("   ");
    }

    @Test
    @Transactional
    public void testExistsByCodeTrueWhenPresent() {
        String code = "code-" + UUID.randomUUID();
        createValidCalendarWithCode(code);
        assertTrue(baseCalendarDAO.existsByCode(code));
    }

    @Test
    @Transactional
    public void testExistsByCodeFalseWhenAbsent() {
        assertFalse(baseCalendarDAO.existsByCode("does-not-exist-" + UUID.randomUUID()));
    }

    @Test
    @Transactional
    public void testFindAllReturnsOrderedByCode() {
        String prefix = UUID.randomUUID().toString();
        BaseCalendar c3 = createValidCalendarWithCode(prefix + "-3");
        BaseCalendar c1 = createValidCalendarWithCode(prefix + "-1");
        BaseCalendar c2 = createValidCalendarWithCode(prefix + "-2");

        List<BaseCalendar> ours = new java.util.ArrayList<>();
        for (BaseCalendar c : baseCalendarDAO.findAll()) {
            if (c.getCode() != null && c.getCode().startsWith(prefix)) {
                ours.add(c);
            }
        }

        assertEquals(3, ours.size());
        assertEquals(c1.getId(), ours.get(0).getId());
        assertEquals(c2.getId(), ours.get(1).getId());
        assertEquals(c3.getId(), ours.get(2).getId());
    }

}
