package org.navalplanner.business.test.resources.bootstrap;

import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.ArrayList;
import java.util.List;

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

    private List<Criterion> somePredefinedCriterions;

    public CriterionsBootstrapTest() {
        somePredefinedCriterions = getSomePredefinedCriterions();
    }

    private List<Criterion> getSomePredefinedCriterions() {
        List<Criterion> result = new ArrayList<Criterion>();
        for (WorkingRelationship workingRelationship : WorkingRelationship.values()) {
            result.add(workingRelationship.criterion());
        }
        return result;
    }

    @Test
    public void testBootstrap() throws Exception {
        givenNoSomePredefinedCriterionExists();
        criterionsBootstrap.loadRequiredData();
        thenAllSomePredefinedCriterionsExist();
    }

    private void givenNoSomePredefinedCriterionExists() {
        for (Criterion criterion : somePredefinedCriterions) {
            remove(criterion);
        }
    }

    private void thenAllSomePredefinedCriterionsExist() {
        for (Criterion criterion : somePredefinedCriterions) {
            assertTrue(criterionDAO.existsByNameAndType(criterion));
        }
    }

    private void remove(Criterion criterion) {
        if (criterionDAO.existsByNameAndType(criterion)) {
            criterionDAO.removeByNameAndType(criterion);
        }
    }

}
