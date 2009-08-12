package org.navalplanner.business.test.resources.bootstrap;

import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.resources.bootstrap.ICriterionsBootstrap;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.WorkingRelationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class CriterionsBootstrapTest {

    @Autowired
    private ICriterionsBootstrap criterionsBootstrap;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Test
    public void testBootstrap() throws Exception {
        Criterion criterion = WorkingRelationship.FIRED.criterion();
        if (criterionDAO.existsByNameAndType(criterion)) {
            criterionDAO.removeByNameAndType(criterion);
        }
        criterion = WorkingRelationship.HIRED.criterion();
        if (criterionDAO.existsByNameAndType(criterion)) {
            criterionDAO.removeByNameAndType(criterion);
        }

        criterionsBootstrap.loadRequiredData();
        criterion = WorkingRelationship.FIRED.criterion();
        assertTrue(criterionDAO.existsByNameAndType(criterion));
        criterion = WorkingRelationship.HIRED.criterion();
        assertTrue(criterionDAO.existsByNameAndType(criterion));
    }

}
