/*
 * This file is part of LibrePlan
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

package org.libreplan.web.test.ws.resources.criterion.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;
import static org.libreplan.web.test.ws.common.Util.assertNoConstraintViolations;
import static org.libreplan.web.test.ws.common.Util.getUniqueName;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.resources.daos.ICriterionTypeDAO;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.ws.common.api.InstanceConstraintViolationsDTO;
import org.libreplan.ws.common.api.ResourceEnumDTO;
import org.libreplan.ws.common.impl.ResourceEnumConverter;
import org.libreplan.ws.resources.criterion.api.CriterionDTO;
import org.libreplan.ws.resources.criterion.api.CriterionTypeDTO;
import org.libreplan.ws.resources.criterion.api.CriterionTypeListDTO;
import org.libreplan.ws.resources.criterion.api.ICriterionService;
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
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class CriterionServiceTest {

    @Autowired
    private ICriterionService criterionService;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Test
    public void testAddCriterionWithSameCodeDistinctTypes() {

        /* Build criterion type "ct1" (5 constraint violations). */
        CriterionDTO c1 = new CriterionDTO("new_c1_code", "new_c1", true,
                new ArrayList<CriterionDTO>());

        List<CriterionDTO> ct1Criterions = new ArrayList<CriterionDTO>();
        ct1Criterions.add(c1);

        CriterionTypeDTO ct1 = new CriterionTypeDTO("new_type_A", "desc",
                false, true, true, ResourceEnumDTO.RESOURCE, ct1Criterions);

        CriterionTypeDTO ct2 = new CriterionTypeDTO("new_type_B", "desc", true,
                true, false, ResourceEnumDTO.RESOURCE, null);

        /* Criterion type list. */
        CriterionTypeListDTO criterionTypes = createCriterionTypeListDTO(ct1,
                ct2);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = criterionService
                .addCriterionTypes(criterionTypes).instanceConstraintViolationsList;
        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 1);
    }

    @Test
    public void testAddAndGetCriterionTypes() {

        /* Build criterion type "ct1" (5 constraint violations). */
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
        CriterionDTO ct1c4 = new CriterionDTO(" C3 ", true,
            new ArrayList<CriterionDTO>()); // Repeated criterion name.
        CriterionDTO ct1c5 = new CriterionDTO(ct1c3.code, // Repeated criterion
                "c4", true, // code inside this criterion type.
                new ArrayList<CriterionDTO>());
        List<CriterionDTO> ct1Criterions = new ArrayList<CriterionDTO>();
        ct1Criterions.add(ct1c1);
        ct1Criterions.add(ct1c2);
        ct1Criterions.add(ct1c3);
        ct1Criterions.add(ct1c4);
        ct1Criterions.add(ct1c5);
        CriterionTypeDTO ct1 = new CriterionTypeDTO(null, "desc",
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
        CriterionTypeDTO ct2 = new CriterionTypeDTO(getUniqueName(), "desc",
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
        CriterionTypeDTO ct3 = new CriterionTypeDTO(getUniqueName(), "desc",
            true, true, true, ResourceEnumDTO.RESOURCE, ct3Criterions);

        /* Build criterion type "ct4" (1 constraint violation). */
        CriterionTypeDTO ct4 =
            new CriterionTypeDTO(              // Repeated criterion
            ' ' + ct3.name.toUpperCase() + ' ', // type name.
            "desc", true, true, true,
            ResourceEnumDTO.RESOURCE, new ArrayList<CriterionDTO>());

        /* Build criterion type "ct5" (1 constraint violation). */
        CriterionDTO ct5c1 = new CriterionDTO(ct3c1.code,  // Criterion code
            "c1", true, // used by another criterion type.
            new ArrayList<CriterionDTO>());
        List<CriterionDTO> ct5Criterions = new ArrayList<CriterionDTO>();
        ct5Criterions.add(ct5c1);
        CriterionTypeDTO ct5 = new CriterionTypeDTO(getUniqueName(), "desc",
            true, true, true, ResourceEnumDTO.RESOURCE, ct5Criterions);

        /* Build criterion type "ct6" (2 constraint violations). */
        CriterionDTO ct6c1 = new CriterionDTO(null,  // Missing criterion code.
            "c1", true, new ArrayList<CriterionDTO>());
        List<CriterionDTO> ct6Criterions = new ArrayList<CriterionDTO>();
        ct6Criterions.add(ct6c1);
        CriterionTypeDTO ct6 =
            new CriterionTypeDTO(null, getUniqueName(), // Missing criterion
                                                        // type code.
            "desc", true, true, true, ResourceEnumDTO.RESOURCE, ct6Criterions);

        /* Criterion type list. */
        CriterionTypeListDTO criterionTypes =
            createCriterionTypeListDTO(ct1, ct2, ct3, ct4, ct5, ct6);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            criterionService.addCriterionTypes(criterionTypes).
                instanceConstraintViolationsList;

        assertTrue(
            instanceConstraintViolationsList.toString(),
            instanceConstraintViolationsList.size() == 5);
        assertTrue(
            instanceConstraintViolationsList.get(0).
            constraintViolations.toString(),
            instanceConstraintViolationsList.get(0).
            constraintViolations.size() == 5); // ct1 constraint violations.
        assertTrue(
            instanceConstraintViolationsList.get(1).
            constraintViolations.toString(),
            instanceConstraintViolationsList.get(1).
            constraintViolations.size() == 2); // ct2 constraint violations.
        assertTrue(
            instanceConstraintViolationsList.get(2).
            constraintViolations.toString(),
            instanceConstraintViolationsList.get(2).
            constraintViolations.size() == 1); // ct4 constraint violations.
        assertTrue(
            instanceConstraintViolationsList.get(3).
            constraintViolations.toString(),
            instanceConstraintViolationsList.get(3).
            constraintViolations.size() == 1); // ct5 constraint violations.
        assertTrue(
            instanceConstraintViolationsList.get(4).
            constraintViolations.toString(),
            instanceConstraintViolationsList.get(4).
            constraintViolations.size() == 2); // ct6 constraint violations.

        /* Find criterion types. */
        List<CriterionTypeDTO> returnedCriterionTypes =
            criterionService.getCriterionTypes().criterionTypes;

        /* Test. */
        assertFalse(criterionTypeDAO.existsByCode(ct2.code));
        assertTrue(criterionTypeDAO.existsByCode(ct3.code));
        assertFalse(criterionTypeDAO.existsByCode(ct4.code));

        assertTrue(containsCriterionType(returnedCriterionTypes, ct3.code));

    }

    @Test
    public void testUpdateCriterionType() throws InstanceNotFoundException {

        /* Build criterion type with criteria: c1, c2->c2-1. */
        CriterionDTO c1 = new CriterionDTO("c1", true,
            new ArrayList<CriterionDTO>());
        CriterionDTO c2c1 = new CriterionDTO("c2-1",
            true, new ArrayList<CriterionDTO>());
        List<CriterionDTO> c2Criterions =  new ArrayList<CriterionDTO>();
        c2Criterions.add(c2c1);
        CriterionDTO c2 = new CriterionDTO("c2", true, c2Criterions);
        List<CriterionDTO> rootCriterions = new ArrayList<CriterionDTO>();
        rootCriterions.add(c1);
        rootCriterions.add(c2);
        CriterionTypeDTO ct = new CriterionTypeDTO(getUniqueName(),
            "desc", true, true, true, ResourceEnumDTO.WORKER, rootCriterions);

        /* Add criterion type. */
        assertNoConstraintViolations(criterionService.addCriterionTypes(
            createCriterionTypeListDTO(ct)));

        /*
         * Build a DTO for making the following update: add new root criterion
         * ("c3"), move "c2" to "c1" and modify c2's name, and update
         * criterion type description.
         */
        CriterionDTO c3 = new CriterionDTO("c3", true,
            new ArrayList<CriterionDTO>());
        CriterionDTO c2Updated = new CriterionDTO(c2.code, c2.name + "UPDATED",
            null, new ArrayList<CriterionDTO>());
        List<CriterionDTO> c1CriterionsUpdated = new ArrayList<CriterionDTO>();
        c1CriterionsUpdated.add(c2Updated);
        CriterionDTO c1Updated = new CriterionDTO(c1.code, null, null,
            c1CriterionsUpdated);
        List<CriterionDTO> rootCriterionsUpdated =
            new ArrayList<CriterionDTO>();
        rootCriterionsUpdated.add(c3);
        rootCriterionsUpdated.add(c1Updated);
        CriterionTypeDTO ctUpdated = new CriterionTypeDTO(ct.code, null,
            "desc" + "UPDATED", null, null, null, null, rootCriterionsUpdated);

        /* Update criterion type and test. */
        assertNoConstraintViolations(criterionService.addCriterionTypes(
            createCriterionTypeListDTO(ctUpdated)));

        CriterionType ctEntity = criterionTypeDAO.findByCode(ct.code);
        assertTrue(ctEntity.getCriterions().size() == 4);

        /* Test criterion hierarchy. */
        Criterion c1Entity = ctEntity.getCriterion(c1.name);
        Criterion c2Entity = ctEntity.getCriterion(c2Updated.name);
        Criterion c2c1Entity = ctEntity.getCriterion(c2c1.name);
        Criterion c3Entity = ctEntity.getCriterion(c3.name);

        assertNull(c1Entity.getParent());
        assertTrue(c1Entity.getChildren().size() == 1);
        assertTrue(c1Entity.getChildren().contains(c2Entity));
        assertTrue(c2Entity.getChildren().size() == 1);
        assertTrue(c2Entity.getChildren().contains(c2c1Entity));
        assertTrue(c2c1Entity.getChildren().size() == 0);
        assertNull(c3Entity.getParent());
        assertTrue(c3Entity.getChildren().size() == 0);

        /*
         * Basic properties in criteria "c1" and "c2", which are contained in
         * "ctUpdated", must not be modified, except c2's name property.
         */
        assertEquals(c1.name, c1Entity.getName());
        assertEquals(c1.active, c1Entity.isActive());

        assertEquals(c2Updated.name, c2Entity.getName());
        assertEquals(c2.active, c2Entity.isActive());

        /*
         * Basic properties values, except description, must be not be
         * modified.
         */
        assertEquals(ct.name, ctEntity.getName());
        assertEquals(ctUpdated.description, ctEntity.getDescription());
        assertEquals(ct.allowHierarchy, ctEntity.allowHierarchy());
        assertEquals(ct.allowSimultaneousCriterionsPerResource,
            ctEntity.isAllowSimultaneousCriterionsPerResource());
        assertEquals(ct.enabled, ctEntity.isEnabled());
        assertEquals(ResourceEnumConverter.fromDTO(ct.resource),
            ctEntity.getResource());

    }

    private boolean containsCriterionType(
        List<CriterionTypeDTO> criterionTypes, String code) {

        for (CriterionTypeDTO c : criterionTypes) {
            if (c.code.equals(code)) {
                return true;
            }
        }

        return false;

    }

    private CriterionTypeListDTO createCriterionTypeListDTO(
            CriterionTypeDTO... criterionTypes) {

        List<CriterionTypeDTO> criterionTypeList =
            new ArrayList<CriterionTypeDTO>();

        for (CriterionTypeDTO c : criterionTypes) {
            criterionTypeList.add(c);
        }

        return new CriterionTypeListDTO(criterionTypeList);

    }

}
