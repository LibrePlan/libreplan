package org.navalplanner.business.test.resources.services;


import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.services.CriterionTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

/**
 * Test cases for {@link CriterionTypeService} <br />
 * @author Diego Pino García <dpino@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class CriterionTypeServiceTest {

    @Autowired
    private CriterionTypeService criterionTypeService;

    public CriterionType createValidCriterionType(String name) {
        return new CriterionType(name);
    }

    @Test
    public void testSaveCriterionType() throws ValidationException {
        String unique = UUID.randomUUID().toString();
        CriterionType criterionType = createValidCriterionType(unique);
        criterionTypeService.save(criterionType);
        assertTrue(criterionTypeService.exists(criterionType));
    }

    @Test
    public void testSaveCriterionTypeTwice() throws ValidationException {
        String unique = UUID.randomUUID().toString();
        CriterionType criterionType = createValidCriterionType(unique);
        criterionTypeService.save(criterionType);
        criterionTypeService.save(criterionType);
        assertTrue(criterionTypeService.exists(criterionType));
    }

     @Test(expected=ConstraintViolationException.class)
    public void testCannotSaveTwoCriterionTypesWithTheSameName() throws ValidationException {
        String unique = UUID.randomUUID().toString();
        CriterionType criterionType = createValidCriterionType(unique);
        criterionTypeService.save(criterionType);
        criterionType = createValidCriterionType(unique);
        criterionTypeService.save(criterionType);
    }
}
