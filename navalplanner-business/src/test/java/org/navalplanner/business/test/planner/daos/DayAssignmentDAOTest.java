/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.test.planner.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.planner.daos.IDayAssignmentDAO;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.GenericDayAssignment;
import org.navalplanner.business.planner.entities.SpecificDayAssignment;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * @author Diego Pino García <dpino@igalia.com>
 */
@Transactional
public class DayAssignmentDAOTest {

    @Autowired
    private IDayAssignmentDAO dayAssignmentDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    private SpecificDayAssignment createValidSpecificDayAssignment() {
        return SpecificDayAssignment.create(new LocalDate(2009, 1, 2), 8,
                createValidResource());
    }

    private Resource createValidResource() {
        Worker worker = Worker.create("first", "surname", "1221332132A");
        resourceDAO.save(worker);
        return worker;
    }

    private GenericDayAssignment createValidGenericDayAssignment() {
        return GenericDayAssignment.create(new LocalDate(2009, 1, 2), 8,
                createValidResource());
    }

    @Test
    public void testInSpringContainer() {
        assertTrue(dayAssignmentDAO != null);
    }

    @Test
    public void testSaveSpecificResourceAllocation() {
        SpecificDayAssignment dayAssignment = createValidSpecificDayAssignment();
        dayAssignmentDAO.save(dayAssignment);
        assertTrue(dayAssignmentDAO.exists(dayAssignment.getId()));
    }

    @Test
    public void testSaveGenericResourceAllocation() {
        GenericDayAssignment dayAssignment = createValidGenericDayAssignment();
        dayAssignmentDAO.save(dayAssignment);
        assertTrue(dayAssignmentDAO.exists(dayAssignment.getId()));
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void theRelatedResourceMustExistInOrderToSave() {
        Worker transientWorker = Worker.create("first", "surname",
                "1221332132A");
        SpecificDayAssignment dayAssignment = SpecificDayAssignment.create(
                new LocalDate(2009, 1, 2), 8, transientWorker);
        dayAssignmentDAO.save(dayAssignment);
    }

    @Test
    public void testRemoveSpecificResourceAllocation()
            throws InstanceNotFoundException {
        SpecificDayAssignment dayAssignment = createValidSpecificDayAssignment();
        dayAssignmentDAO.save(dayAssignment);
        dayAssignmentDAO.remove(dayAssignment.getId());
        assertFalse(dayAssignmentDAO.exists(dayAssignment.getId()));
    }

    @Test
    public void testRemoveGenericResourceAllocation()
            throws InstanceNotFoundException {
        GenericDayAssignment dayAssignment = createValidGenericDayAssignment();
        dayAssignmentDAO.save(dayAssignment);
        dayAssignmentDAO.remove(dayAssignment.getId());
        assertFalse(dayAssignmentDAO.exists(dayAssignment.getId()));
    }

    @Test
    public void testListSpecificDayAssignment() {
        int previous = dayAssignmentDAO.list(DayAssignment.class)
                .size();

        SpecificDayAssignment dayAssignment1 = createValidSpecificDayAssignment();
        dayAssignmentDAO.save(dayAssignment1);
        SpecificDayAssignment dayAssignment2 = createValidSpecificDayAssignment();
        dayAssignmentDAO.save(dayAssignment1);
        dayAssignmentDAO.save(dayAssignment2);

        List<SpecificDayAssignment> list = dayAssignmentDAO
                .list(SpecificDayAssignment.class);
        assertEquals(previous + 2, list.size());
    }

    @Test
    public void testListGenericDayAssignment() {
        int previous = dayAssignmentDAO.list(DayAssignment.class)
                .size();

        GenericDayAssignment dayAssignment1 = createValidGenericDayAssignment();
        dayAssignmentDAO.save(dayAssignment1);
        GenericDayAssignment dayAssignment2 = createValidGenericDayAssignment();
        dayAssignmentDAO.save(dayAssignment1);
        dayAssignmentDAO.save(dayAssignment2);

        List<GenericDayAssignment> list = dayAssignmentDAO
                .list(GenericDayAssignment.class);
        assertEquals(previous + 2, list.size());
    }
}
