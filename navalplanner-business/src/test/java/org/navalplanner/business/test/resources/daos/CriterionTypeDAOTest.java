/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.test.resources.daos;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 */

/**
 * Test cases for CriterionTypeDAO <br />
 * @author Diego Pino García <dpino@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class CriterionTypeDAOTest {

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    public static final String DEFAULT_CRITERION_TYPE = "TEST_DEFAULT";

    public static CriterionType createValidCriterionType(String name,String description) {
        return CriterionType.create(name,description);
    }

    public static CriterionType createValidCriterionType() {
        String unique = UUID.randomUUID().toString();
        String description = "";
        return createValidCriterionType(unique,description);
    }

    @Test
    public void testSaveCriterionType() throws Exception {
        CriterionType criterionType = createValidCriterionType();
        criterionTypeDAO.save(criterionType);
        assertTrue(criterionTypeDAO.exists(criterionType.getId()));
    }

    @Test
    public void testCriterionTypeCanBeSavedTwice() throws ValidationException {
        CriterionType criterionType = createValidCriterionType();
        criterionTypeDAO.save(criterionType);
        criterionTypeDAO.save(criterionType);
        assertTrue(criterionTypeDAO.exists(criterionType.getId())
                || criterionTypeDAO
                        .existsOtherCriterionTypeByName(criterionType));
    }

    @Test(expected = ValidationException.class)
    @NotTransactional
    public void testCannotSaveTwoDifferentCriterionTypesWithTheSameName() {
        IOnTransaction<Void> createTypeWithRepeatedName = new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                CriterionType criterionType = createValidCriterionType("bla",
                        "");
                criterionTypeDAO.save(criterionType);
                return null;
            }
        };
        transactionService.runOnTransaction(createTypeWithRepeatedName);
        transactionService.runOnTransaction(createTypeWithRepeatedName);
    }

    @Test
    @NotTransactional
    public void testUpdateWithExistingName() {

        final String name1 = getUniqueName();
        final String name2 = getUniqueName();

        IOnTransaction<Void> createCriterionTypes = new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                criterionTypeDAO.save(createValidCriterionType(name1, ""));
                criterionTypeDAO.save(createValidCriterionType(name2, ""));
                return null;
            }
        };

        IOnTransaction<Void> updateCriterionType1 = new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                CriterionType criterionType = null;
                try {
                    criterionType = criterionTypeDAO.findUniqueByName(name1);
                } catch (InstanceNotFoundException e) {
                    fail("InstanceNotFoundException not expected");
                }
                criterionType.setName(name2);
                criterionTypeDAO.save(criterionType);
                return null;
            }
        };

        transactionService.runOnTransaction(createCriterionTypes);

        try {
            transactionService.runOnTransaction(updateCriterionType1);
            fail("ValidationException expected");
        } catch (ValidationException e) {
        }

    }

    @Test
    @NotTransactional
    public void testUpdateWithTheSameName() {

        final String name1 = getUniqueName();
        final String name2 = getUniqueName();

        IOnTransaction<Void> createCriterionTypes = new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                criterionTypeDAO.save(createValidCriterionType(name1, ""));
                criterionTypeDAO.save(createValidCriterionType(name2, ""));
                return null;
            }
        };

        IOnTransaction<Void> updateCriterionType1 = new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                CriterionType criterionType = null;
                try {
                    criterionType = criterionTypeDAO.findUniqueByName(name1);
                } catch (InstanceNotFoundException e) {
                    fail("InstanceNotFoundException not expected");
                }
                criterionType.setDescription("New description");
                criterionTypeDAO.save(criterionType);
                return null;
            }
        };

        transactionService.runOnTransaction(createCriterionTypes);
        transactionService.runOnTransaction(updateCriterionType1);

    }

    @Test
    public void testRemove() throws InstanceNotFoundException {
        CriterionType criterionType = createValidCriterionType();
        criterionTypeDAO.save(criterionType);
        criterionTypeDAO.remove(criterionType.getId());
        assertFalse(criterionTypeDAO.exists(criterionType.getId()));
    }

    @Test
    public void testList() {
        int previous = criterionTypeDAO.list(CriterionType.class).size();
        CriterionType criterion1 = createValidCriterionType();
        CriterionType criterion2 = createValidCriterionType();
        criterionTypeDAO.save(criterion1);
        criterionTypeDAO.save(criterion2);
        List<CriterionType> list = criterionTypeDAO.list(CriterionType.class);
        assertEquals(previous + 2, list.size());
    }

    @Test
    public void testGetCriterionTypes() {
        int previous = criterionTypeDAO.list(CriterionType.class).size();
        CriterionType criterion1 = createValidCriterionType();
        CriterionType criterion2 = createValidCriterionType();
        criterionTypeDAO.save(criterion1);
        criterionTypeDAO.save(criterion2);
        List<CriterionType> list = criterionTypeDAO.getCriterionTypes();
        assertEquals(previous + 2, list.size());
    }

    @Test
    public void testGetCriterionTypesByResourceType() {
        // Add RESOURCE criterionType
        CriterionType criterionType = createValidCriterionType();
        criterionType.setResource(ResourceEnum.WORKER);
        criterionTypeDAO.save(criterionType);

        // Add WORKER criterionType
        criterionType = createValidCriterionType();
        criterionType.setResource(ResourceEnum.WORKER);
        criterionTypeDAO.save(criterionType);

        // Get number of criterionTypes of type RESOURCE
        List<ResourceEnum> resources = new ArrayList<ResourceEnum>();
        resources.add(ResourceEnum.WORKER);
        List<CriterionType> criterions = criterionTypeDAO.getCriterionTypesByResources(resources);
        int numberOfCriterionsOfTypeResource = criterions.size();

        // Get number of criterionTypes of type WORKER
        resources.add(ResourceEnum.WORKER);
        criterions = criterionTypeDAO
                .getCriterionTypesByResources(resources);
        int numberOfCriterionsOfTypeResourceAndWorker = criterions.size();

        assertTrue(numberOfCriterionsOfTypeResourceAndWorker >= numberOfCriterionsOfTypeResource);
    }

    private String getUniqueName() {
        return UUID.randomUUID().toString();
    }

}
