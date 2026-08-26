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

package org.libreplan.business.test.calendars.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.calendars.daos.ICalendarExceptionDAO;
import org.libreplan.business.calendars.daos.ICalendarExceptionTypeDAO;
import org.libreplan.business.calendars.entities.CalendarException;
import org.libreplan.business.calendars.entities.CalendarExceptionType;
import org.libreplan.business.calendars.entities.CalendarExceptionTypeColor;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.workingday.EffortDuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link CalendarExceptionTypeDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 */
public class CalendarExceptionTypeDAOTest {

    @Autowired
    private ICalendarExceptionTypeDAO calendarExceptionTypeDAO;

    @Autowired
    private ICalendarExceptionDAO calendarExceptionDAO;

    private CalendarExceptionType createValidType(String name) {
        CalendarExceptionType type = CalendarExceptionType.create(name, CalendarExceptionTypeColor.DEFAULT, true);
        calendarExceptionTypeDAO.save(type);
        return type;
    }

    @Test
    @Transactional
    public void testExistsByNameEntityTrueWhenPresent() {
        String name = "type-" + UUID.randomUUID();
        CalendarExceptionType type = createValidType(name);
        assertTrue(calendarExceptionTypeDAO.existsByName(type));
    }

    @Test
    @Transactional
    public void testExistsByNameEntityFalseWhenAbsent() {
        CalendarExceptionType notSaved = CalendarExceptionType.create();
        notSaved.setName("does-not-exist-" + UUID.randomUUID());
        assertFalse(calendarExceptionTypeDAO.existsByName(notSaved));
    }

    @Test
    @Transactional
    public void testExistsByNameStringTrueWhenPresent() {
        String name = "type-" + UUID.randomUUID();
        createValidType(name);
        assertTrue(calendarExceptionTypeDAO.existsByName(name));
    }

    @Test
    @Transactional
    public void testExistsByNameStringFalseWhenAbsent() {
        assertFalse(calendarExceptionTypeDAO.existsByName("does-not-exist-" + UUID.randomUUID()));
    }

    // Note: existsByNameAnotherTransaction/findUniqueByNameAnotherTransaction use
    // Propagation.REQUIRES_NEW and simply delegate to existsByName/findUniqueByName
    // (already covered above) - they add no Criteria logic of their own and can't be
    // exercised from within a single @Transactional test method (the REQUIRES_NEW
    // transaction can't see this test's uncommitted data), so they're intentionally
    // not covered here.

    @Test
    @Transactional
    public void testFindUniqueByNameIsCaseInsensitiveAndTrims() throws InstanceNotFoundException {
        String mixedCaseName = "MiXeD-" + UUID.randomUUID();
        CalendarExceptionType type = createValidType(mixedCaseName);

        assertEquals(type.getId(), calendarExceptionTypeDAO.findUniqueByName(mixedCaseName).getId());
        assertEquals(type.getId(), calendarExceptionTypeDAO.findUniqueByName(mixedCaseName.toLowerCase()).getId());
        assertEquals(type.getId(), calendarExceptionTypeDAO.findUniqueByName(mixedCaseName.toUpperCase()).getId());
        assertEquals(type.getId(), calendarExceptionTypeDAO.findUniqueByName("  " + mixedCaseName + "  ").getId());
    }

    @Test(expected = InstanceNotFoundException.class)
    @Transactional
    public void testFindUniqueByNameThrowsWhenNotFound() throws InstanceNotFoundException {
        calendarExceptionTypeDAO.findUniqueByName("does-not-exist-" + UUID.randomUUID());
    }

    @Test(expected = InstanceNotFoundException.class)
    @Transactional
    public void testFindUniqueByNameThrowsWhenBlank() throws InstanceNotFoundException {
        calendarExceptionTypeDAO.findUniqueByName("   ");
    }

    @Test
    @Transactional
    public void testHasCalendarExceptionsFalseWhenNone() {
        CalendarExceptionType type = createValidType("type-" + UUID.randomUUID());
        assertFalse(calendarExceptionTypeDAO.hasCalendarExceptions(type));
    }

    @Test
    @Transactional
    public void testHasCalendarExceptionsTrueWhenPresent() {
        CalendarExceptionType type = createValidType("type-" + UUID.randomUUID());
        CalendarException exception = CalendarException.create(new LocalDate(2020, 1, 1), EffortDuration.zero(), type);
        calendarExceptionDAO.save(exception);

        assertTrue(calendarExceptionTypeDAO.hasCalendarExceptions(type));
    }

    @Test
    @Transactional
    public void testHasCalendarExceptionsDoesNotConfuseDifferentTypes() {
        CalendarExceptionType type1 = createValidType("type-" + UUID.randomUUID());
        CalendarExceptionType type2 = createValidType("type-" + UUID.randomUUID());
        CalendarException exception = CalendarException.create(new LocalDate(2020, 1, 1), EffortDuration.zero(), type2);
        calendarExceptionDAO.save(exception);

        assertFalse(calendarExceptionTypeDAO.hasCalendarExceptions(type1));
        assertTrue(calendarExceptionTypeDAO.hasCalendarExceptions(type2));
    }

    @Test
    @Transactional
    public void testGetAllIncludesSavedType() {
        String name = "type-" + UUID.randomUUID();
        CalendarExceptionType type = createValidType(name);

        boolean found = false;
        for (CalendarExceptionType t : calendarExceptionTypeDAO.getAll()) {
            if (t.getId().equals(type.getId())) {
                found = true;
            }
        }
        assertTrue(found);
    }

}
