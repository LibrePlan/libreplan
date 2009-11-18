package org.navalplanner.web.test.ws.resources.criterion.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTO;
import org.navalplanner.ws.resources.criterion.api.CriterionDTO;
import org.navalplanner.ws.resources.criterion.api.CriterionTypeDTO;
import org.navalplanner.ws.resources.criterion.api.CriterionTypeListDTO;
import org.navalplanner.ws.resources.criterion.api.ICriterionService;
import org.navalplanner.ws.resources.criterion.api.ResourceEnumDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for <code>ICriterionService</code>.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE })
@Transactional
public class CriterionServiceTest {

    @Autowired
    private ICriterionService criterionService;

    @BeforeClass
    public static void populateDb() throws Throwable {
    }

    @AfterClass
    public static void cleanDb() throws Throwable {
    }

    /**
     * Tests <code>ICriterionService:addCriterionTypes</code> (indirectly,
     * <code>ICriterionService:getCriterionTypes</code> is also tested).
     */
    @Test
    public void testAddCriterionTypes() {

        /* Build criterion type "ct1" (4 constraint violations). */
        CriterionDTO ct1c1 = new CriterionDTO(null, true, // Missing criterion
                                                          // name.
            new ArrayList<CriterionDTO>());
        CriterionDTO ct1c2c1 = new CriterionDTO("c2-1", true,
            new ArrayList<CriterionDTO>());
        List<CriterionDTO> ct1c2Criterions =  new ArrayList<CriterionDTO>();
        ct1c2Criterions.add(ct1c2c1);
        CriterionDTO ct1c2 = new CriterionDTO("c2", true,  // Criterion
                                                           // hierarchy is not
                                                           // allowed in the
                                                           // criterion type
                                                           // (see above).
            ct1c2Criterions);
        CriterionDTO ct1c3 = new CriterionDTO("c3", true,
            new ArrayList<CriterionDTO>());
        CriterionDTO ct1c4 = new CriterionDTO("c3", true,
            new ArrayList<CriterionDTO>()); // Repeated criterion name.
        List<CriterionDTO> ct1Criterions = new ArrayList<CriterionDTO>();
        ct1Criterions.add(ct1c1);
        ct1Criterions.add(ct1c2);
        ct1Criterions.add(ct1c3);
        ct1Criterions.add(ct1c4);
        CriterionTypeDTO ct1 = new CriterionTypeDTO(null, "ct-1 desc",
            false, true, true, ResourceEnumDTO.RESOURCE, // Missing criterion
            ct1Criterions);                              // type name.

        /* Build criterion type "ct2" (2 constraint violations). */
        CriterionDTO ct2c1 = new CriterionDTO("c1", true, // Its criterion type
            new ArrayList<CriterionDTO>());               // is not enabled.
        CriterionDTO ct2c2c1 = new CriterionDTO("c2-1",
            true, new ArrayList<CriterionDTO>()); // Its criterion father is
                                                  // not active.
        List<CriterionDTO> ct2c2Criterions =  new ArrayList<CriterionDTO>();
        ct2c2Criterions.add(ct2c2c1);
        CriterionDTO ct2c2 = new CriterionDTO("c2", false,
            ct2c2Criterions);
        List<CriterionDTO> ct2Criterions = new ArrayList<CriterionDTO>();
        ct2Criterions.add(ct2c1);
        ct2Criterions.add(ct2c2);
        CriterionTypeDTO ct2 = new CriterionTypeDTO("ct-2", "ct-2 desc",
            true, true, false, ResourceEnumDTO.RESOURCE, ct2Criterions);

        /* Build criterion type "ct3" (OK). */
        CriterionDTO ct3c1 = new CriterionDTO("c1", true,
            new ArrayList<CriterionDTO>());
        CriterionDTO ct3c2c1 = new CriterionDTO("c2-1",
            true, new ArrayList<CriterionDTO>());
        List<CriterionDTO> ct3c2Criterions =  new ArrayList<CriterionDTO>();
        ct3c2Criterions.add(ct3c2c1);
        CriterionDTO ct3c2 = new CriterionDTO("c2", true,
            ct3c2Criterions);
        List<CriterionDTO> ct3Criterions = new ArrayList<CriterionDTO>();
        ct3Criterions.add(ct3c1);
        ct3Criterions.add(ct3c2);
        CriterionTypeDTO ct3 = new CriterionTypeDTO("ct-3", "ct-3 desc",
            true, true, true, ResourceEnumDTO.RESOURCE, ct3Criterions);

        /* Build criterion type "ct4" (2 constraint violations). */
        CriterionDTO ct4c1 = new CriterionDTO(null, true, // Missing criterion
            new ArrayList<CriterionDTO>());               // name.
        CriterionDTO ct4c2 = new CriterionDTO("c2", true,
            new ArrayList<CriterionDTO>());
        List<CriterionDTO> ct4Criterions = new ArrayList<CriterionDTO>();
        ct4Criterions.add(ct4c1);
        ct4Criterions.add(ct4c2);
        CriterionTypeDTO ct4 = new CriterionTypeDTO("ct-3", // Repeated
            "ct-4 desc", true, true, true,                  // criterion type
            ResourceEnumDTO.RESOURCE, ct4Criterions);       // name.

        /* Criterion type list. */
        List<CriterionTypeDTO> criterionTypes =
            new ArrayList<CriterionTypeDTO>();
        criterionTypes.add(ct1);
        criterionTypes.add(ct2);
        criterionTypes.add(ct3);
        criterionTypes.add(ct4);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            criterionService.addCriterionTypes(
                new CriterionTypeListDTO(criterionTypes)).
                    instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.size() == 3);
        assertTrue(instanceConstraintViolationsList.get(0).
            constraintViolations.size() == 4);
        assertTrue(instanceConstraintViolationsList.get(1).
            constraintViolations.size() == 2);
        assertTrue(instanceConstraintViolationsList.get(2).
            constraintViolations.size() == 2);

        /* Find criterion types. */
        List<CriterionTypeDTO> returnedCriterionTypes =
            criterionService.getCriterionTypes().criterionTypes;

        assertTrue(returnedCriterionTypes.size() == 1);
        assertEquals(returnedCriterionTypes.get(0).name, "ct-3");

    }

}
