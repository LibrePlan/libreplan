package org.navalplanner.business.test.resources.daos;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

/**
 * Test cases for CriterionDAO <br />
 * Created at May 13, 2009
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class CriterionDAOTest {

    @Autowired
    private ICriterionDAO criterionDAO;

    @Test
    public void testInSpringContainer() {
        assertNotNull(criterionDAO);
    }

    @Test
    public void testSaveCriterions() throws Exception {
        Criterion criterion = createValidCriterion();
        criterionDAO.save(criterion);
        assertNotNull(criterion.getId());
        assertTrue(criterionDAO.exists(criterion.getId()));
    }

    public static Criterion createValidCriterion() {
        return new Criterion();
    }

    @Test(expected = InstanceNotFoundException.class)
    public void testRemoveNotExistent() throws InstanceNotFoundException {
        criterionDAO.remove(Long.MAX_VALUE);
    }

    @Test
    public void testRemove() throws InstanceNotFoundException {
        Criterion criterion = createValidCriterion();
        criterionDAO.save(criterion);
        assertTrue(criterionDAO.exists(criterion.getId()));
        criterionDAO.remove(criterion.getId());
        assertFalse(criterionDAO.exists(criterion.getId()));
    }

    @Test
    public void testList() {
        int previous = criterionDAO.list(Criterion.class).size();
        Criterion criterion1 = createValidCriterion();
        Criterion criterion2 = createValidCriterion();
        criterionDAO.save(criterion1);
        criterionDAO.save(criterion2);
        List<Criterion> list = criterionDAO.list(Criterion.class);
        assertEquals(previous + 2, list.size());
    }
}
