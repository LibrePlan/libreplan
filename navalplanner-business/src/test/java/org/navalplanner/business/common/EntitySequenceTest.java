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

package org.navalplanner.business.common;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.daos.IEntitySequenceDAO;
import org.navalplanner.business.common.entities.EntityNameEnum;
import org.navalplanner.business.common.entities.EntitySequence;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link EntitySequence}. <br />
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class EntitySequenceTest {

    @Autowired
    IEntitySequenceDAO entitySequenceDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Test
    public void testCreateActiveEntitySequence() throws Exception {
        try {
            entitySequenceDAO.save(givenEntitySequence("prefix_test",
                    EntityNameEnum.CALENDAR, true));
            entitySequenceDAO.flush();
        } catch (ValidationException e) {
            fail("It should not throw an exception");
        }
        assertTrue(entitySequenceDAO.getAll().size() == 1);

    }

    @Test
    public void testCreateEntitySequenceWithEmptyPrefix() throws Exception {
        try {
            entitySequenceDAO.save(givenEntitySequence("",
                    EntityNameEnum.CALENDAR, true));
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        }

    }

    @Test
    public void testCreateEntitySequenceWithPrefixWithWhiteSpace()
            throws Exception {
        try {
            entitySequenceDAO.save(givenEntitySequence(
                    "prefix with white spaces", EntityNameEnum.CALENDAR, true));
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        }

    }

    @Test
    public void testCreateEntitySequenceWithEmptyEntityName() throws Exception {
        try {
            entitySequenceDAO.save(givenEntitySequence("prefix", null, false));
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        }

    }

    @Test
    public void testCreateEntitySequenceWithNumberOfDigitsNotSpecified()
            throws Exception {
        try {
            EntitySequence entitySequence = givenEntitySequence("prefix",
                    EntityNameEnum.CRITERION, true);
            entitySequence.setNumberOfDigits(null);
            entitySequenceDAO.save(entitySequence);
            fail("It should throw an exception");
        } catch (IllegalArgumentException e) {
            // It should throw an exception
        }

    }

    @Test
    public void testCreateEntitySequenceWithNumberOfDigitsOutRange()
            throws Exception {
        try {
            EntitySequence entitySequence = givenEntitySequence("prefix",
                    EntityNameEnum.CRITERION, true);
            entitySequence.setNumberOfDigits(15);
            entitySequenceDAO.save(entitySequence);
            fail("It should throw an exception");
        } catch (IllegalArgumentException e) {
            // It should throw an exception
        }

    }

    @Test
    @NotTransactional
    public void testCreateTwoActiveEntitySequenceWithTheSameEntityName() {
        EntitySequence entitySequenceA = givenEntitySequence("prefixA",
                EntityNameEnum.CRITERION, true);
        saveEntitySequenceInTransaction(entitySequenceA);
        try {
            EntitySequence entitySequenceB = givenEntitySequence("prefixB",
                    EntityNameEnum.CRITERION, true);
            saveEntitySequenceInTransaction(entitySequenceB);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
        }
    }

    @Test
    @NotTransactional
    public void testCreateTwoEntitySequenceWithTheSameEntityName() {
        EntitySequence entitySequenceA = givenEntitySequence("prefixA",
                EntityNameEnum.LABEL, true);
        saveEntitySequenceInTransaction(entitySequenceA);
        try {
            EntitySequence entitySequenceB = givenEntitySequence("prefixB",
                    EntityNameEnum.LABEL, false);
            saveEntitySequenceInTransaction(entitySequenceB);
        } catch (ValidationException e) {
            fail("It shouldn't throw an exception");
        }
    }

    @Test
    @NotTransactional
    public void testCreateAndRemoveTwoEntitySequenceWithTheSameEntityName() {
        EntitySequence entitySequenceA = givenEntitySequence("prefixA",
                EntityNameEnum.MACHINE, true);
        saveEntitySequenceInTransaction(entitySequenceA);
        try {
            removeEntitySequenceInTransaction(entitySequenceA);
        } catch (ValidationException e) {
            fail("It shouldn't throw an exception");
        }
        try {
            EntitySequence entitySequenceB = givenEntitySequence("prefixB",
                    EntityNameEnum.MACHINE, true);
            saveEntitySequenceInTransaction(entitySequenceB);
        } catch (ValidationException e) {
            fail("It shouldn't throw an exception");
        }
    }

    private void saveEntitySequenceInTransaction(
            final EntitySequence entitySequence) {
        IOnTransaction<Void> createEntitySequenceTransaction = new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                entitySequenceDAO.save(entitySequence);
                return null;
            }
        };
        transactionService.runOnTransaction(createEntitySequenceTransaction);
    }

    private void removeEntitySequenceInTransaction(
            final EntitySequence entitySequence) {
        IOnTransaction<Void> createEntitySequenceTransaction = new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                try {
                    entitySequenceDAO.remove(entitySequence);
                } catch (InstanceNotFoundException e) {

                } catch (IllegalArgumentException e) {

                }
                return null;
            }
        };
        transactionService.runOnTransaction(createEntitySequenceTransaction);
    }

    private EntitySequence givenEntitySequence(String prefix,
            EntityNameEnum entityName, boolean active) {
        EntitySequence entitySequence = EntitySequence.create(prefix,
                entityName);
        entitySequence.setActive(active);
        return entitySequence;
    }
}
