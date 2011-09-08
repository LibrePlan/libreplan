/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.test.planner.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.planner.daos.IAssignmentFunctionDAO;
import org.navalplanner.business.planner.entities.AssignmentFunction;
import org.navalplanner.business.planner.entities.ManualFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/*
 * @author Diego Pino García <dpino@igalia.com>
 */
@Transactional
public class AssignmentFunctionDAOTest {

    @Autowired
    private IAssignmentFunctionDAO assignmentFunctionDAO;

    @Test
    public void testInSpringContainer() {
        assertTrue(assignmentFunctionDAO != null);
    }

    private AssignmentFunction createValidAssignmentFunction() {
        return ManualFunction.create();
    }

    @Test
    public void testSaveAssignmentFunction() {
        AssignmentFunction assignmentFunction = createValidAssignmentFunction();
        assignmentFunctionDAO.save(assignmentFunction);
        assertTrue(assignmentFunctionDAO.exists(assignmentFunction.getId()));
    }

    @Test
    public void testRemoveAssignmentFunction()
            throws InstanceNotFoundException {
        AssignmentFunction assignmentFunction = createValidAssignmentFunction();
        assignmentFunctionDAO.save(assignmentFunction);
        assignmentFunctionDAO.remove(assignmentFunction.getId());
        assertFalse(assignmentFunctionDAO.exists(assignmentFunction.getId()));
    }

    @Test
    public void testListAssignmentFunction() {
        int previous = assignmentFunctionDAO.list(AssignmentFunction.class).size();

        AssignmentFunction assignmentFunction1 = createValidAssignmentFunction();
        assignmentFunctionDAO.save(assignmentFunction1);
        AssignmentFunction assignmentFunction2 = createValidAssignmentFunction();
        assignmentFunctionDAO.save(assignmentFunction1);
        assignmentFunctionDAO.save(assignmentFunction2);

        List<AssignmentFunction> list = assignmentFunctionDAO
                .list(AssignmentFunction.class);
        assertEquals(previous + 2, list.size());
    }
}
