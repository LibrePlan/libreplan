package org.navalplanner.business.test.resources.daos;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.PredefinedCriterionTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Test
    public void testInSpringContainer() {
        assertNotNull(criterionDAO);
    }

    public static Criterion createValidCriterion() {
        return createValidCriterion(UUID.randomUUID().toString());
    }

    public static Criterion createValidCriterion(String name) {
        CriterionType criterionType = CriterionTypeDAOTest.createValidCriterionType();

        return Criterion.withNameAndType(name, criterionType);
    }

    private void saveCriterionType(Criterion criterion) {
        CriterionType criterionType = criterion.getType();
        if (criterionTypeDAO.existsByName(criterionType)) {
            try {
                criterionType = criterionTypeDAO.findUniqueByName(criterionType);
            } catch (InstanceNotFoundException ex) {

            }
        } else {
            criterionTypeDAO.save(criterionType);
        }
        criterion.setType(criterionType);
    }

    @Test
    public void testSaveCriterions() throws Exception {
        Criterion criterion = createValidCriterion();
        // A valid CriterionType must exists before saving Criterion
        saveCriterionType(criterion);
        criterionDAO.save(criterion);
        assertTrue(criterionDAO.exists(criterion.getId()));
    }

    @Test
    public void testRemove() throws InstanceNotFoundException {
        Criterion criterion = createValidCriterion();
         saveCriterionType(criterion);
        criterionDAO.save(criterion);
        criterionDAO.remove(criterion.getId());
        assertFalse(criterionDAO.exists(criterion.getId()));
    }

    @Test
    public void testList() {
        int previous = criterionDAO.list(Criterion.class).size();
        Criterion criterion1 = createValidCriterion();
        saveCriterionType(criterion1);
        Criterion criterion2 = createValidCriterion();
        saveCriterionType(criterion2);
        criterionDAO.save(criterion1);
        criterionDAO.save(criterion2);
        List<Criterion> list = criterionDAO.list(Criterion.class);
        assertEquals(previous + 2, list.size());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void schemaEnsuresCannotExistTwoDifferentCriterionsWithSameNameAndType()
            throws ValidationException {
        String unique = UUID.randomUUID().toString();
        Criterion criterion = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        criterionDAO.save(criterion);
        criterionDAO.flush();
        Criterion criterion2 = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        criterionDAO.save(criterion2);
        criterionDAO.flush();
    }
}
