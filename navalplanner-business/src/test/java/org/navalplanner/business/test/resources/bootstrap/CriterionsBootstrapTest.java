package org.navalplanner.business.test.resources.bootstrap;

import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.resources.bootstrap.ICriterionsBootstrap;
import org.navalplanner.business.resources.entities.WorkingRelationship;
import org.navalplanner.business.resources.services.ICriterionService;
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
    private ICriterionService criterionService;

    @Test
    public void testBootstrap() throws Exception {
        if (criterionService.exists(WorkingRelationship.FIRED.criterion())) {
            criterionService.remove(WorkingRelationship.FIRED.criterion());
        }
        if (criterionService.exists(WorkingRelationship.HIRED.criterion())) {
            criterionService.remove(WorkingRelationship.HIRED.criterion());
        }
        criterionsBootstrap.loadRequiredData();
        assertTrue(criterionService.exists(WorkingRelationship.FIRED
                .criterion()));
        assertTrue(criterionService.exists(WorkingRelationship.HIRED
                .criterion()));
    }

}
