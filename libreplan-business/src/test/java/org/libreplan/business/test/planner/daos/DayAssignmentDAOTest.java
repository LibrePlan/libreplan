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

package org.libreplan.business.test.planner.daos;

import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.planner.daos.IDayAssignmentDAO;
import org.libreplan.business.planner.entities.DayAssignment;
import org.libreplan.business.resources.entities.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link DayAssignmentDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 *
 * Constructing a real, persisted DayAssignment row requires a fully wired
 * ResourceAllocation with an allocated calendar (Task + Order calendar + Worker
 * calendar + ResourcesPerDay.allocate(...)), which none of the existing DAO-level
 * integration tests in this suite do (ResourceAllocationDAOTest only ever saves
 * empty, zero-day allocations). Given that gap, only the empty/no-match branches of
 * listFilteredByDate/findByResources are characterized here - a positive-match case
 * would need substantially new test infrastructure outside this migration's scope.
 */
public class DayAssignmentDAOTest {

    @Autowired
    private IDayAssignmentDAO dayAssignmentDAO;

    @Test
    @Transactional
    public void testListFilteredByDateWithNoDataReturnsEmpty() {
        List<DayAssignment> result = dayAssignmentDAO.listFilteredByDate(
                new LocalDate(2020, 1, 1), new LocalDate(2020, 12, 31));
        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    public void testListFilteredByDateWithNullBoundsAndNoDataReturnsEmpty() {
        List<DayAssignment> result = dayAssignmentDAO.listFilteredByDate(null, null);
        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    public void testFindByResourcesWithEmptyListReturnsEmpty() {
        List<DayAssignment> result = dayAssignmentDAO.findByResources(Collections.<Resource> emptyList());
        assertTrue(result.isEmpty());
    }

}
