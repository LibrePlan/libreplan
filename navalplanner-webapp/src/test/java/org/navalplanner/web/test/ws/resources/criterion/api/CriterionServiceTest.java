/*
 * This file is part of ###PROJECT_NAME###
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

package org.navalplanner.web.test.ws.resources.criterion.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTO;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.api.ResourceEnumDTO;
import org.navalplanner.ws.resources.criterion.api.CriterionDTO;
import org.navalplanner.ws.resources.criterion.api.CriterionTypeDTO;
import org.navalplanner.ws.resources.criterion.api.CriterionTypeListDTO;
import org.navalplanner.ws.resources.criterion.api.ICriterionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
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
    private IAdHocTransactionService transactionService;

    @Autowired
    private ICriterionService criterionService;

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
        String ct1Name = null;
        CriterionTypeDTO ct1 = new CriterionTypeDTO(ct1Name, "desc",
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
        String ct2Name = getUniqueName();
        CriterionTypeDTO ct2 = new CriterionTypeDTO(ct2Name, "desc",
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
        String ct3Name = getUniqueName();
        CriterionTypeDTO ct3 = new CriterionTypeDTO(ct3Name, "desc",
            true, true, true, ResourceEnumDTO.RESOURCE, ct3Criterions);

        /* Build criterion type "ct4" (2 constraint violations). */
        CriterionDTO ct4c1 = new CriterionDTO(null, true, // Missing criterion
            new ArrayList<CriterionDTO>());               // name.
        CriterionDTO ct4c2 = new CriterionDTO("c2", true,
            new ArrayList<CriterionDTO>());
        List<CriterionDTO> ct4Criterions = new ArrayList<CriterionDTO>();
        ct4Criterions.add(ct4c1);
        ct4Criterions.add(ct4c2);
        CriterionTypeDTO ct4 =                       // Repeated criterion
            new CriterionTypeDTO(ct3Name,            // type name (see previous
            "desc", true, true, true,                // criterion type).
            ResourceEnumDTO.RESOURCE, ct4Criterions);

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

        assertFalse(containsCriterionType(returnedCriterionTypes, ct1Name));
        assertFalse(containsCriterionType(returnedCriterionTypes, ct2Name));
        assertTrue(containsCriterionType(returnedCriterionTypes, ct3Name));

    }

    @Test
    @NotTransactional
    public void testAddCriterionTypeThatAlreadyExistsInDB()
        throws InstanceNotFoundException {

        final String criterionTypeName = getUniqueName();

        IOnTransaction<InstanceConstraintViolationsListDTO>
            createCriterionType =
                new IOnTransaction<InstanceConstraintViolationsListDTO>() {

            @Override
            public InstanceConstraintViolationsListDTO execute() {

                CriterionTypeDTO criterionType = new CriterionTypeDTO(
                        criterionTypeName, "desc", true, true, true,
                    ResourceEnumDTO.RESOURCE, new ArrayList<CriterionDTO>());
                List<CriterionTypeDTO> criterionTypes =
                    new ArrayList<CriterionTypeDTO>();

                criterionTypes.add(criterionType);

                return criterionService.addCriterionTypes(
                    new CriterionTypeListDTO(criterionTypes));

            }
        };

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            transactionService.runOnTransaction(
                createCriterionType).
                    instanceConstraintViolationsList;
        assertTrue(instanceConstraintViolationsList.size() == 0);

        instanceConstraintViolationsList =
            transactionService.runOnTransaction(
                createCriterionType).
                    instanceConstraintViolationsList;
        assertTrue(instanceConstraintViolationsList.size() == 1);

    }

    private String getUniqueName() {
        return UUID.randomUUID().toString();
    }

    private boolean containsCriterionType(
        List<CriterionTypeDTO> criterionTypes, String criterionTypeName) {

        for (CriterionTypeDTO c : criterionTypes) {
            if (c.name.equals(criterionTypeName)) {
                return true;
            }
        }

        return false;

    }

}
