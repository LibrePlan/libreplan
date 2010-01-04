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

package org.navalplanner.business.test.externalcompanies.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.externalcompanies.daos.IExternalCompanyDAO;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Test for {@link ExternalCompanyDAO}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 *
 */
@Transactional
public class ExternalCompanyDAOTest {

    @Autowired
    IExternalCompanyDAO externalCompanyDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Test
    public void testInSpringContainer() {
        assertNotNull(externalCompanyDAO);
    }

    @Test
    public void testSaveExternalCompany() {
        ExternalCompany externalCompany = createValidExternalCompany();
        externalCompanyDAO.save(externalCompany);
        assertTrue(externalCompany.getId() != null);
    }

    @Test
    public void testRemoveExternalCompany() throws InstanceNotFoundException {
        ExternalCompany externalCompany = createValidExternalCompany();
        externalCompanyDAO.save(externalCompany);
        externalCompanyDAO.remove(externalCompany.getId());
        assertFalse(externalCompanyDAO.exists(externalCompany.getId()));
    }

    @Test
    public void testListExternalCompanies() {
        int previous = externalCompanyDAO.list(ExternalCompany.class).size();
        ExternalCompany externalCompany = createValidExternalCompany();
        externalCompanyDAO.save(externalCompany);
        assertEquals(previous + 1, externalCompanyDAO.list(ExternalCompany.class).size());
    }

    @Test
    public void testFindUniqueByName() throws InstanceNotFoundException {
        ExternalCompany externalCompany = createValidExternalCompany();
        externalCompanyDAO.save(externalCompany);
        assertEquals(externalCompany.getId(),
                externalCompanyDAO.findUniqueByName(externalCompany.getName()).getId());
    }

    @Test
    public void testExistsByName() throws InstanceNotFoundException {
        ExternalCompany externalCompany = createValidExternalCompany();
        assertFalse(externalCompanyDAO.existsByName(externalCompany.getName()));
        externalCompanyDAO.save(externalCompany);
        assertTrue(externalCompanyDAO.existsByName(externalCompany.getName()));
    }

    @Test(expected=ValidationException.class)
    @NotTransactional
    public void testUniqueCompanyNameCheck() throws ValidationException {
        final ExternalCompany externalCompany1 = createValidExternalCompany();

        IOnTransaction<Void> createCompanyWithRepeatedName = new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                externalCompanyDAO.save(externalCompany1);
                return null;
            }
        };
        transactionService.runOnTransaction(createCompanyWithRepeatedName);
        //the second time we save the same object, a exception is thrown
        transactionService.runOnTransaction(createCompanyWithRepeatedName);
    }

    @Test(expected=ValidationException.class)
    @NotTransactional
    public void testUniqueCompanyNifCheck() throws ValidationException {
        final ExternalCompany externalCompany1 = createValidExternalCompany();

        IOnTransaction<Void> createCompany = new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                externalCompanyDAO.save(externalCompany1);
                return null;
            }
        };
        IOnTransaction<Void> createCompanyWithRepeatedNif = new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                ExternalCompany externalCompany2 = createValidExternalCompany();
                externalCompany2.setNif(externalCompany1.getNif());
                externalCompanyDAO.save(externalCompany2);
                return null;
            }
        };
        transactionService.runOnTransaction(createCompany);
        //the second object has the same cif, a exception is thrown when saving it
        transactionService.runOnTransaction(createCompanyWithRepeatedNif);
    }

    private ExternalCompany createValidExternalCompany() {
        return ExternalCompany.create(UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
    }
}
