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
import org.navalplanner.business.planner.daos.IAssigmentFunctionDAO;
import org.navalplanner.business.planner.entities.AssigmentFunction;
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
public class AssigmentFunctionDAOTest {

    @Autowired
    private IAssigmentFunctionDAO assigmentFunctionDAO;

    @Test
    public void testInSpringContainer() {
        assertTrue(assigmentFunctionDAO != null);
    }

    private AssigmentFunction createValidAssigmentFunction() {
        return AssigmentFunction.create();
    }

    @Test
    public void testSaveAssigmentFunction() {
        AssigmentFunction assigmentFunction = createValidAssigmentFunction();
        assigmentFunctionDAO.save(assigmentFunction);
        assertTrue(assigmentFunctionDAO.exists(assigmentFunction.getId()));
    }

    @Test
    public void testRemoveAssigmentFunction()
            throws InstanceNotFoundException {
        AssigmentFunction assigmentFunction = createValidAssigmentFunction();
        assigmentFunctionDAO.save(assigmentFunction);
        assigmentFunctionDAO.remove(assigmentFunction.getId());
        assertFalse(assigmentFunctionDAO.exists(assigmentFunction.getId()));
    }

    @Test
    public void testListAssigmentFunction() {
        int previous = assigmentFunctionDAO.list(AssigmentFunction.class).size();

        AssigmentFunction assigmentFunction1 = createValidAssigmentFunction();
        assigmentFunctionDAO.save(assigmentFunction1);
        AssigmentFunction assigmentFunction2 = createValidAssigmentFunction();
        assigmentFunctionDAO.save(assigmentFunction1);
        assigmentFunctionDAO.save(assigmentFunction2);

        List<AssigmentFunction> list = assigmentFunctionDAO
                .list(AssigmentFunction.class);
        assertEquals(previous + 2, list.size());
    }
}
